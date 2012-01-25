package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.XMLUtil;
import nig.encrypt.EncryptionTypes;
import nig.encrypt.EncryptionTypes.EncryptionType;

import nig.mf.plugin.pssd.*;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery;

import java.util.*;

/**
 * This service is the core service that finds PSSD objects, applies application
 * level role-based filtering, and reformats the native asset.get meta-data in
 * the PSSD structure. Just about everything should go via this service or its
 * static version thereof.
 * 
 * This service and its static functions are only called in the context of
 * viewing or finding objects, not creating them.
 * 
 * @author jason
 * 
 */
public class SvcObjectFind extends PluginService {

	private Interface _defn;

	public SvcObjectFind() {

		_defn = new Interface();
		_defn.add(new Interface.Element("type", new EnumType(new String[] { Project.TYPE, Subject.TYPE, ExMethod.TYPE,
				Study.TYPE, DataSet.TYPE, DataObject.TYPE, RSubject.TYPE }),
				"The type of the object(s) to restrict the search, if any.", 0, 1));
		_defn.add(new Interface.Element("text", StringType.DEFAULT, "Arbitrary search text for free text query.", 0, 1));
		_defn.add(new Interface.Element("idx", LongType.POSITIVE_ONE, "Cursor position. Defaults to 1", 0, 1));
		_defn.add(new Interface.Element("size", IntegerType.POSITIVE_ONE, "Cursor size. Defaults to 100", 0, 1));
		_defn.add(new Interface.Element(
				"foredit",
				BooleanType.DEFAULT,
				"Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",
				0, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] { "primary", "replica", "all" }),
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element(
				"pdist",
				IntegerType.DEFAULT,
				"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",
				0, 1));
	}

	public String name() {

		return "om.pssd.object.find";
	}

	public String description() {

		return "Returns objects that match the given search parameters. It does a distributed query in a federation.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String type = args.value("type");
		String text = args.value("text");
		boolean forEdit = args.booleanValue("foredit", false);
		String pdist = args.value("pdist");
		String assetType = args.stringValue("asset-type", "all");

		// Setup query
		String query;

		if (type == null) {
			query = "xpath(pssd-object) has value"; // "cid starts with named id '"
													// +
													// CiteableIdUtil.PROJECT_CID_ROOT_NAME
													// + "'";
		} else {
			query = "xpath(pssd-object/type)='" + type + "'";
		}

		if (text != null) {
			query += " and text contains '" + text + "'";
		}

		// Primary/replica/both (default)
		DistributedQuery.appendResultAssetTypePredicate(query, DistributedQuery.ResultAssetType.instantiate(assetType));

		// Set up service call
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where", query);
		if (forEdit) {
			dm.add("action", "get-template-meta");
		} else {
			dm.add("action", "get-meta");
		}

		dm.add("idx", args.longValue("idx", 1));
		dm.add("size", args.intValue("size", 100));

		dm.add("get-related-meta", "true");
		dm.add("related-type", "attachment");
		if (pdist != null) {
			dm.add("pdist", pdist);
		}

		// asset.query is distributed in a federation
		// We will need to do some clever filtering so that we don't end up with
		// an intractable mess
		// of primary and replica objects in a federated environment
		XmlDoc.Element r = executor().execute("asset.query", dm.root());

		// Parse the XML and reformat
		addPssdObjects(executor(), w, r, false, forEdit);
	}

	public static void addPssdObjects(ServiceExecutor executor, XmlWriter w, XmlDoc.Element r, boolean isleaf,
			boolean forEdit) throws Throwable {

		addPssdObjects(executor, w, r, isleaf, forEdit, false, false);
	}

	/**
	 * This function takes a collection of meta-data as provided by asset.query,
	 * and reformats it for the PSSD data model, applying any PSSD role-based
	 * filtering in the process
	 * 
	 * @param executor
	 * @param w
	 *            The returned meta-data
	 * @param r
	 *            The input meta-data from asset.query (could contain remote
	 *            objects)
	 * @param isleaf
	 *            If true, identify whether nodes are leaf nodes or not
	 * @param forEdit
	 *            If true, then the returned meta-data will be used for editing,
	 *            in which case a description of the meta-data is returned
	 *            rather than just a specification
	 * @param showRSubjectIdentity
	 *            If true, show the Identity meta-data on an RSubject object
	 *            regardless of the user's role
	 * @param showSubjectPrivate
	 *            If true, show the "private" field meta-data on a Subject
	 *            object regardless of the user's role
	 * @throws Throwable
	 */
	public static void addPssdObjects(ServiceExecutor executor, XmlWriter w, XmlDoc.Element r, boolean isleaf,
			boolean forEdit, boolean showRSubjectIdentity, boolean showSubjectPrivate) throws Throwable {

		// Iterate through all 'asset' items
		Collection<XmlDoc.Element> assets = r.elements("asset");
		if (assets != null) {
			for (XmlDoc.Element assetMeta : assets) {

				// This meta-data tells us what kind of PSSD object we are
				// working with
				XmlDoc.Element poe = assetMeta.element("meta/pssd-object");
				if (poe != null) {
					String type = poe.value("type");

					// Create the distributed asset. The object could be
					// anywhere in the federation.
					DistributedAsset dAsset = new DistributedAsset(assetMeta.value("@proute"), assetMeta.value("cid"));
					dAsset.setReplicaId(assetMeta.value("rid"));

					// Some objects (currently Subject only) may be filtered out
					// entirely (currently
					// for ethics/data-use restrictions). Therefore, before we
					// start adding the
					// generic PSSD meta-data to the output, we must apply this
					// filtering
					if (!dropObject(executor, type, dAsset, assetMeta)) {

						// Establish if the current user has the authorization
						// to edit the meta-data for
						// this object (regardless if they *want* to edit it or
						// not [forEdit])
						boolean editable = isEditable(executor, type, dAsset);

						// Start accumulating the PSSD formatted meta-data.
						// Start with the basic/required
						// meta-data for all objects
						w.push("object", new String[] { "type", type, "editable", Boolean.toString(editable),
								"version", assetMeta.value("@version") });

						// Regardless of whether it is called locally or as a
						// peer, asset.query
						// will give me back a sensible server route in a
						// federation
						addPssdObject(w, assetMeta);

						// Add meta-data appropriately for the type of this
						// object
						boolean addMeta = true;
						boolean addObject = true;
						if (type.equalsIgnoreCase(Project.TYPE)) {
							addPssdProject(executor, w, dAsset, assetMeta, forEdit);
						} else if (type.equalsIgnoreCase(Subject.TYPE)) {
							addObject = addPssdSubject(executor, dAsset, w, assetMeta, forEdit, showSubjectPrivate);
						} else if (type.equalsIgnoreCase(RSubject.TYPE)) {
							addPssdRSubject(executor, dAsset, w, assetMeta, forEdit, showRSubjectIdentity);
							addMeta = false;
						} else if (type.equalsIgnoreCase(ExMethod.TYPE)) {
							addPssdExMethod(executor, w, assetMeta, forEdit);
						} else if (type.equalsIgnoreCase(Study.TYPE)) {
							addPssdStudy(w, assetMeta, forEdit);
						} else if (type.equalsIgnoreCase(DataSet.TYPE)) {
							addPssdDataSet(w, assetMeta);
						} else if (type.equalsIgnoreCase(DataObject.TYPE)) {
							addPssdDataObject(w, assetMeta);
						} else if (type.equalsIgnoreCase(Method.TYPE)) {
							addPssdMethod(executor, w, assetMeta);
						}

						// If wanted, identify if node is a leaf or not
						if (isleaf) {
							XmlDocMaker dm = new XmlDocMaker("args");
							String query = "cid in '" + dAsset.getCiteableID() + "'";
							dm.add("where", query);
							dm.add("size", "1");

							r = executor.execute("asset.query", dm.root());
							if (r.count("id") == 0) {
								w.add("isleaf", true);
							} else {
								w.add("isleaf", false);
							}
						}

						// Now add the rest of the domain-specific extended
						// meta-data (filters out
						// the required object-specific meta-data just
						// consumed).
						// Some types of objects don't have extended metadata..
						if (addMeta) {
							addPssdMeta(w, assetMeta, type);
						}

						addPssdAttachments(w, assetMeta);
						w.pop();
					}
				}
			}
		}

	}

	/**
	 * Determine if this object should be dropped entirely from the return
	 * Currently we filter only on the 'data-use' criterion. This function
	 * scrutinizes roles that are assigned to the user on the server that
	 * manages the asset.
	 * 
	 * @param executor
	 * @param type
	 *            type of PSSD object
	 * @param id
	 *            CID of object
	 * @param ae
	 * @return
	 * @throws Throwable
	 */

	private static boolean dropObject(ServiceExecutor executor, String type, DistributedAsset dAsset, XmlDoc.Element ae)
			throws Throwable {

		if (type.equalsIgnoreCase(Subject.TYPE)) {

			// Find the parent project so that we know the CID for
			// project-specific roles
			// and what server to check roles on. Because this class is only
			// used in a viewing/finding
			// context, we can look for primary first and then replica project
			// parents.
			Boolean readOnly = true;
			DistributedAsset dProjectAsset = dAsset.getParentProject(readOnly);
			if (dProjectAsset == null) {
				throw new Exception("Cannot find a parent Project of the given object");
			}
			ServerRoute projectRoute = dProjectAsset.getServerRouteObject();
			String projectCID = dProjectAsset.getCiteableID();

			// CHeck the user's roles on the server managing the Project
			// A project-admin can see/do anything and by definition a Subject
			// admin is allowed
			// to access all Subjects
			if (ModelUser.hasRole(projectRoute, executor, Project.subjectAdministratorRoleName(projectCID)))
				return false;

			// Get the data-use specification for this Subject. If null, then no
			// restrictions apply
			String subjectDataUse = ae.value("meta/pssd-subject/data-use");
			if (subjectDataUse == null)
				return false;

			// Now filter. The user's data-use role must not exceed the
			// subject's specification
			// If the user has no 'data-use' role, this code behaves as if they
			// had 'specific'
			// However, we only actually need the 'extended' and 'unspecified'
			// roles to be given
			// to a user as 'specific' is implicit
			if (subjectDataUse.equals(Project.CONSENT_SPECIFIC_ROLE_NAME)) {
				if (ModelUser.hasRole(projectRoute, executor, Project.extendedUseRoleName(projectCID))
						|| ModelUser.hasRole(projectRoute, executor, Project.unspecifiedUseRoleName(projectCID)))
					return true;
			} else if (subjectDataUse.equals(Project.CONSENT_EXTENDED_ROLE_NAME)) {
				if (ModelUser.hasRole(projectRoute, executor, Project.unspecifiedUseRoleName(projectCID)))
					return true;
			} else if (subjectDataUse.equals(Project.CONSENT_UNSPECIFIED_ROLE_NAME)) {
				return false;
			}

		}
		//
		return false;

	}

	/**
	 * Is the user authorized to edit the given PSSD object?
	 * 
	 * @param executor
	 * @param type
	 *            Type of PSSD object
	 * @param dCID
	 *            Distributed CID of PSSD object
	 * @return
	 * @throws Throwable
	 */
	private static boolean isEditable(ServiceExecutor executor, String type, DistributedAsset dAsset) throws Throwable {

		// Find the parent project so that we know the CID for project-specific
		// roles
		// and what server to check roles on. Because this class is only used in
		// a viewing/finding
		// context, we can look for primary first and then replica project
		// parents.
		Boolean readOnly = true;
		DistributedAsset dProject = dAsset.getParentProject(readOnly); // Null
																		// tested
																		// for
																		// earlier

		// Find the highest-level (Project Admin for Projects, Subject-Admin for
		// Subjects,
		// currently Member (perhaps should be an admin) for other objects)
		// project-specific
		// role names for each object type
		String projectCID = dProject.getCiteableID();
		String role = null;
		if (type.equalsIgnoreCase(Project.TYPE)) {
			role = Project.projectAdministratorRoleName(projectCID);
		} else if (type.equalsIgnoreCase(Subject.TYPE)) {
			role = Project.subjectAdministratorRoleName(projectCID);
		} else if (type.equalsIgnoreCase(ExMethod.TYPE)) {
			role = Project.memberRoleName(projectCID);
		} else if (type.equalsIgnoreCase(Study.TYPE)) {
			role = Project.memberRoleName(projectCID);
		} else if (type.equalsIgnoreCase(DataSet.TYPE)) {
			role = Project.memberRoleName(projectCID);
		} else if (type.equalsIgnoreCase(DataObject.TYPE)) {
			role = Project.memberRoleName(projectCID);
		}
		if (role == null) {
			return false;
		}

		// If the user has the above role, they are authorized to edit the
		// meta-data
		// for this object. So now see if the user holds this role on the server
		// managing
		// the Project
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, role);
		XmlDoc.Element r = executor.execute(dProject.getServerRouteObject(), "actor.self.have", dm.root());
		return r.booleanValue("role");
	}

	/**
	 * Add basic and required meta-data common to all PSSD objects
	 * 
	 * @param w
	 * @param ae
	 * @throws Throwable
	 */
	public static void addPssdObject(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		XmlDoc.Element poe = ae.element("meta/pssd-object");
		String id = ae.value("cid");
		String rid = ae.value("rid");

		// Set proute. In a distributed query, this will be filled in even for a
		// local object
		// However, a simple asset.get (e.g. om.pssd.object.describe) on a local
		// object
		// will not fill in proute even in a federated session. I could fill it
		// in with
		// the local server UUID...
		String proute = ae.value("@proute");
		//
		String name = poe.value("name");
		String description = poe.value("description");
		w.add("id", new String[] { "proute", proute, "asset", ae.attribute("id").value(), "rid", rid }, id);

		if (name != null) {
			w.add("name", name);
		}

		if (description != null) {
			w.add("description", description);
		}
		XmlDoc.Element lock = ae.element("lock");
		if (lock != null) {
			w.add(lock, true);
		}
	}

	/**
	 * Add meta-data specific to PSSD Project objects
	 * 
	 * @param executor
	 * @param w
	 * @param dAsset
	 *            distributed citeable asset for Project objects
	 * @param ae
	 *            Holds the input meta-data as returned by asset.query
	 * @param editable
	 * @throws Throwable
	 */
	public static void addPssdProject(ServiceExecutor executor, XmlWriter w, DistributedAsset dAsset,
			XmlDoc.Element ae, boolean editable) throws Throwable {

		// Indicate if the user is allowed to create Subject objects for this
		// Project
		boolean screate;
		if (editable) {
			screate = true;
		} else {
			// See if user us allowed to create Subjects
			screate = canCreateSubjects(executor, dAsset);
		}
		w.add("subject-create", screate);

		// Get the required meta-data for Project objects (Methods, Team members
		// etc)
		XmlDoc.Element pe = ae.element("meta/pssd-project");
		if (pe == null) {
			return;
		}

		// Get, and expand (dereference), the methods that are registered with
		// this Project
		// Adds a "method" element
		Collection<XmlDoc.Element> methods = pe.elements("method");
		if (methods != null) {
			for (XmlDoc.Element me : methods) {

				String id = me.value("id");
				String notes = me.value("notes");

				// Dereference the Method.. This invokes a call to
				// the server that manages this Method
				addMethod(executor, w, dAsset.getServerRouteObject(), id, notes);
			}
		}

		// TODO:
		// Get the project team members and de-reference role members
		// Now that we have dropped the pssd-projec/member meta-data, we should
		// probably drop the presentation of member meta-data (done via roles)
		XmlDocMaker dm = new XmlDocMaker("args");
		String proute = dAsset.getServerRoute();
		dm.add("id", new String[] { "proute", proute }, dAsset.getCiteableID());
		dm.add("dereference", false);
		XmlDoc.Element r = executor.execute("om.pssd.project.members.list", dm.root());
		if (r != null) {

			// user members
			Collection<XmlDoc.Element> members = r.elements("member");
			if (members != null) {
				for (XmlDoc.Element me : members) {
					w.add(me);
				}
			}

			// role members
			Collection<XmlDoc.Element> roleMembers = r.elements("role-member");
			if (roleMembers != null) {
				for (XmlDoc.Element me : roleMembers) {
					w.add(me);
				}
			}
		}

		// Data-use
		String dataUse = pe.value("data-use");
		if (dataUse != null) {
			w.add("data-use", dataUse);
		}

	}

	/**
	 * 
	 * Add a PSSD Method object's meta-data. There is no federation impact on
	 * this function
	 * 
	 * @param executor
	 * @param w
	 * @param projectRoute
	 *            server route to Project object
	 * @param mid
	 * @param notes
	 * @throws Throwable
	 */
	private static void addMethod(ServiceExecutor executor, XmlWriter w, ServerRoute projectRoute, String mid,
			String notes) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");

		// We require that the Method exists on the server that manages the
		// parent Project object.
		// If it does not we will get an exception.
		dm.add("id", mid);
		try {
			XmlDoc.Element r = executor.execute(projectRoute, "om.pssd.method.describe", dm.root());

			String name = r.value("method/name");
			String description = r.value("method/description");

			// I think it's wrong to have this information here. The addObject
			// function has already
			// added the basic id/name layer and this appears to be redundant.
			// Probably too hard
			// to eradicate at this point.... [nebk; Jul2010]
			w.push("method", new String[] { "asset", r.value("method/@asset"), "version", r.value("method/@version") });
			w.add("id", mid);
			w.add("name", name);

			if (description != null) {
				w.add("description", description);
			}

			if (notes != null) {
				w.add("notes", notes);
			}

			w.pop();
		} catch (Throwable t) {

			// Fall back if we can't find the Method to de-reference it
			w.push("method");
			w.add("id", mid);
			if (notes != null) {
				w.add("notes", notes);
			}
			w.pop();
		}
	}

	/**
	 * Is the current user authorized to create Subject objects for this Project
	 * ?
	 * 
	 * @param executor
	 * @param dAsset
	 *            Distributed asset for Project object
	 * @return
	 * @throws Throwable
	 */
	private static boolean canCreateSubjects(ServiceExecutor executor, DistributedAsset dAsset) throws Throwable {

		String role = Project.subjectAdministratorRoleName(dAsset.getCiteableID());

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, role);

		// Ask the server that manages the Project what the user's roles are
		XmlDoc.Element r = executor.execute(dAsset.getServerRouteObject(), "actor.self.have", dm.root());
		return r.booleanValue("role");
	}

	/**
	 * Add meta-data specific to PSSD Subject objects
	 * 
	 * @param executor
	 * @param dAsset
	 *            Distributed citeable asset for a Subject
	 * @param w
	 * @param ae
	 *            Holds the input meta-data as returned by asset.query
	 * @param forEdit
	 * @param showSubjectPrivate
	 *            If true, show the "private" field meta-data on the Subject
	 *            object regardless of the user's role
	 * @return If true then show the meta-data for this subject. If false, the
	 *         data-use filtering dictates that the user can't see this subject
	 *         at all.
	 * @throws Throwable
	 */
	public static boolean addPssdSubject(ServiceExecutor executor, DistributedAsset dSubject, XmlWriter w,
			XmlDoc.Element ae, boolean forEdit, boolean showSubjectPrivate) throws Throwable {

		// Get all of the meta-data
		XmlDoc.Element me = ae.element("meta");
		if (me == null) {
			return true;
		}

		// Find the parent project so that we know the CID for project-specific
		// roles
		// and what server to check roles on. Because this class is only used in
		// a viewing/finding
		// context, we can look for primary first and then replica project
		// parents.
		Boolean readOnly = true;
		DistributedAsset dProject = dSubject.getParentProject(readOnly); // Null
																			// tested
																			// for
																			// earlier
		String projectCID = dProject.getCiteableID();
		ServerRoute projectRoute = dProject.getServerRouteObject();

		// We are going to apply some role-based authorisation to the Subject
		// Ask the server that manages ther Project if the user is a Subject
		// administrator
		boolean admin = ModelUser.hasRole(projectRoute, executor, Project.subjectAdministratorRoleName(projectCID));

		// Iterate through all of the "meta" elements and find the required
		// Subject-specific
		// meta-data.
		List<XmlDoc.Element> mes = me.elements();
		if (mes != null) {

			// First find all of the public meta-data (it's in ns=pssd.public)
			// and
			// add it to the "public" element
			boolean pushed = false;
			for (int i = 0; i < mes.size(); i++) {
				XmlDoc.Element se = mes.get(i);
				String ns = se.value("@ns");
				if (ns != null && ns.equals("pssd.public")) {
					if (!pushed) {
						w.push("public");
						pushed = true;
					}
					w.add(se);
				}
			}

			if (pushed) {
				w.pop();
			}

			// Now find the private metadata and add it to the "private" element
			// User role may be over-ridden
			if (showSubjectPrivate || admin) {
				pushed = false;
				for (int i = 0; i < mes.size(); i++) {
					XmlDoc.Element se = (XmlDoc.Element) mes.get(i);
					String ns = se.value("@ns");
					if (ns != null && ns.equals("pssd.private")) {
						if (!pushed) {
							w.push("private");
							pushed = true;
						}
						// Decrypt private fields if they have been encrypted
						XmlDoc.Attribute encryptAttr = se.attribute(EncryptionTypes.ENCRYPTED_ATTR);
						if (encryptAttr != null && encryptAttr.value().equals(EncryptionTypes.ENCRYPTED_VALUE)) {
							XMLUtil.decryptXML(EncryptionType.BASE_64, se, forEdit);
						}
						w.add(se);
					}
				}
				if (pushed)
					w.pop();
			}
		}

		// Now add the Method (with which this Subject was created) to the
		// "method" element
		String method = ae.value("meta/pssd-subject/method");
		if (method != null) {
			addMethod(executor, w, projectRoute, method, null);
		}

		// If there is an RSubject, add it to the "r-subject" element
		String rsid = ae.value("meta/pssd-subject/r-subject");
		if (rsid != null) {
			w.add("r-subject", rsid);
		}

		// If there is a data-use element, add it too
		// Data-use specification for this Subject
		String subjectDataUse = ae.value("meta/pssd-subject/data-use");
		if (subjectDataUse != null) {
			w.add("data-use", subjectDataUse);
		}

		return true;

	}

	/**
	 * Add meta-data specific to PSSD RSubject objects. There is federation
	 * impact in this function.
	 * 
	 * @param executor
	 * @param dAsset
	 *            Distributed citeable asset of RSubject object
	 * @param w
	 * @param ae
	 *            Holds the input meta-data as returned by asset.query
	 * @param forEdit
	 * @param showRSubjectIdentity
	 *            If true, show the Identity meta-data on the RSubject object
	 *            regardless of the user's role
	 * @throws Throwable
	 */
	public static void addPssdRSubject(ServiceExecutor executor, DistributedAsset dRSubject, XmlWriter w,
			XmlDoc.Element ae, boolean forEdit, boolean showRSubjectIdentity) throws Throwable {

		// Get all of the meta-data
		XmlDoc.Element me = ae.element("meta");
		if (me == null) {
			return;
		}

		// Iterate through elements
		List<XmlDoc.Element> mes = me.elements();
		String cid = dRSubject.getCiteableID();
		ServerRoute rSubjectRoute = dRSubject.getServerRouteObject();
		if (mes != null) {

			// TODO -- filter based on access permissions..
			// Find the public metadata..
			boolean pushed = false;

			// Generic PSSD R-Subject administrator (we don't use this role as
			// yet
			// as we administer R-Subjects per project). Validate the role
			// on the server that manages the RSubject
			boolean admin = ModelUser.hasRole(rSubjectRoute, executor, PSSDUtils.SUBJECT_ADMIN_ROLE_NAME);

			if (!admin) {
				// R-Subject admin access for this specific RSubject
				// Generally this is given to the user who created the RSubject
				admin = ModelUser.hasRole(rSubjectRoute, executor, RSubject.administratorRoleName(cid));
			}

			// Generic R-Subject PSSD guest (e.g. other Project.SUbject admin)
			boolean guest = ModelUser.hasRole(rSubjectRoute, executor, PSSDUtils.SUBJECT_GUEST_ROLE_NAME);

			if (!guest) {
				// Guest access for this specific R-Subject
				guest = ModelUser.hasRole(rSubjectRoute, executor, RSubject.guestRoleName(cid));
			}

			// SHow the R-Subject Identity information
			// Possibly the R-Subject guest should not be able to see this
			if (showRSubjectIdentity || admin || guest) {
				for (int i = 0; i < mes.size(); i++) {
					XmlDoc.Element se = mes.get(i);
					String ns = se.value("@ns");
					if (ns != null && ns.equals("pssd.identity")) {
						if (!pushed) {
							w.push("identity");
							pushed = true;
						}

						w.add(se);
					}
				}

				if (pushed) {
					w.pop();
				}
			}

			// Find the public metadata..
			pushed = false;

			if (admin || guest) {
				for (int i = 0; i < mes.size(); i++) {
					XmlDoc.Element se = (XmlDoc.Element) mes.get(i);
					String ns = se.value("@ns");
					if (ns != null && ns.equals("pssd.public")) {
						if (!pushed) {
							w.push("public");
							pushed = true;
						}

						w.add(se);
					}
				}

				if (pushed) {
					w.pop();
				}
			}

			// Find the private metadata..
			pushed = false;

			if (admin) {
				for (int i = 0; i < mes.size(); i++) {
					XmlDoc.Element se = (XmlDoc.Element) mes.get(i);
					String ns = se.value("@ns");
					if (ns != null && ns.equals("pssd.private")) {
						if (!pushed) {
							w.push("private");
							pushed = true;
						}

						w.add(se);
					}
				}

				if (pushed) {
					w.pop();
				}
			}
		}

		// The Method must be managed by the same server as the parent Project
		String method = ae.value("meta/pssd-subject/method");
		if (method != null) {
			Boolean readOnly = true;
			DistributedAsset dProject = dRSubject.getParentProject(readOnly);
			addMethod(executor, w, dProject.getServerRouteObject(), method, null);
		}

		XmlDoc.Element se = ae.element("meta/pssd-state");
		if (se != null) {
			w.push("states");
			w.add(se, false);
			w.pop();
		}
	}

	/**
	 * Add the ExMethod meta-data. There is no federation impact in this
	 * function
	 * 
	 * @param executor
	 * @param w
	 * @param ae
	 *            Meta-data for this asset
	 * @param forEdit
	 * @throws Throwable
	 */
	public static void addPssdExMethod(ServiceExecutor executor, XmlWriter w, XmlDoc.Element ae, boolean forEdit)
			throws Throwable {

		ExMethod em = new ExMethod();
		em.parseAssetMeta(ae);

		Method m = em.method();

		w.push("method");
		w.add("id", m.id());
		w.add("name", m.name());
		if (m.description() != null) {
			w.add("description", m.description());
		}

		if (m.authors() != null) {
			w.add("author", m.authors());
		}

		m.saveSteps(w);

		w.pop();
		// This status appears to be the overall state of he ExMethod
		// and appears to always be INCOMPLETE
		w.add("state", em.status());
		em.saveSteps(w);

		// w.pop();

		/*
		 * String method = ae.value("meta/pssd-ex-method/method"); if ( method
		 * != null ) { addMethod(executor,w,method,null); }
		 * 
		 * XmlDoc.Element em = ae.element("meta/pssd-ex-method"); if ( em !=
		 * null ) { w.add(em,false); }
		 */
	}

	public static void addPssdMethod(ServiceExecutor executor, XmlWriter w, XmlDoc.Element ae) throws Throwable {

		SvcMethodFind.describeMethodElement(executor, w, ae, false);
	}

	/**
	 * Add a PSSD STudy object's meta-data. There is no federation impact in
	 * this function.
	 * 
	 * @param w
	 * @param ae
	 * @param forEdit
	 * @throws Throwable
	 */
	public static void addPssdStudy(XmlWriter w, XmlDoc.Element ae, boolean forEdit) throws Throwable {

		XmlDoc.Element se = ae.element("meta/pssd-study");
		if (se == null) {
			return;
		}

		w.add("type", se.value("type"));

		/*
		 * XmlDoc.Element we = se.element("workflow"); if ( we != null ) {
		 * w.add(we); }
		 */

		XmlDoc.Element me = se.element("method");
		if (me != null) {

			w.push("method");
			w.add("id", me.value());
			w.add("step", me.value("@step"));

			me = ae.element("meta");
			if (me != null) {

				// Filter out metadata that applies only to the method.
				String id = ae.value("cid");
				String mid = nig.mf.pssd.CiteableIdUtil.getParentId(id);
				String mns = mid + "_";

				boolean pushed = false;

				List<XmlDoc.Element> mes = me.elements();
				if (mes != null) {
					// TODO -- filter based on access permissions..
					// Find the public metadata..
					for (int i = 0; i < mes.size(); i++) {
						XmlDoc.Element sme = (XmlDoc.Element) mes.get(i);
						String ns = sme.value("@ns");
						if (ns != null && ns.startsWith(mns)) {

							if (!pushed) {
								w.push("meta");
								pushed = true;
							}

							w.add(sme);
						}
					}

					if (pushed) {
						w.pop();
					}

				}

			}

			w.pop();
		}

	}

	/**
	 * Add a PSSD DataSet object's meta-data. There is no federation impact on
	 * this function
	 * 
	 * @param w
	 * @param ae
	 * @throws Throwable
	 */
	public static void addPssdDataSet(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		XmlDoc.Element se = ae.element("meta/pssd-dataset");
		if (se == null) {
			return;
		}

		w.push("source");
		w.add("type", se.value("type"));
		w.pop();

		addPssdValueId(w, ae);

		addPssdDataSetAcquisition(w, ae);
		addPssdDataSetDerivation(w, ae);
		addPssdDataSetTransform(w, ae);

		String mimeType = ae.value("type");
		if (mimeType != null) {
			w.add("type", mimeType);
		}

		addPssdDataSetContent(w, ae);
	}

	public static void addPssdValueId(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		String vid = ae.value("content/@stime");
		if (vid == null) {
			vid = ae.value("meta/@stime");
		}

		if (vid == null) {
			vid = ae.value("stime");
		}

		w.add("vid", vid);
	}

	public static void addPssdDataSetAcquisition(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		XmlDoc.Element pae = ae.element("meta/pssd-acquisition");

		if (pae == null) {
			return;
		}

		w.push("acquisition");

		XmlDoc.Element se = pae.element("subject");

		if (se != null) {
			w.push("subject");
			w.add("id", se.value());
			w.add("state", se.value("@state"));
			w.pop();
		}

		XmlDoc.Element me = pae.element("method");
		if (me != null) {
			w.push("method");
			w.add("id", me.value());
			w.add("step", me.value("@step"));
			w.pop();
		}

		w.pop();

	}

	public static void addPssdDataSetDerivation(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		XmlDoc.Element pde = ae.element("meta/pssd-derivation");
		if (pde == null) {
			return;
		}

		w.push("derivation");

		XmlDoc.Element ie = pde.element("input");
		if (ie != null) {
			w.add("input", new String[] { "vid", ie.value("@vid") }, ie.value());
		}

		XmlDoc.Element me = pde.element("method");
		if (me != null) {
			w.push("method");
			w.add("id", me.value());
			w.add("step", me.value("@step"));
			w.pop();
		}

		w.pop();
	}

	private static void addPssdDataSetTransform(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		XmlDoc.Element te = ae.element("meta/pssd-transform");
		if (te == null) {
			return;
		}

		String id = te.value("id");
		String notes = te.value("notes");

		if (id != null || notes != null) {
			w.push("transform");

			if (id != null) {
				w.add("id", id);
			}

			if (notes != null) {
				w.add("notes", notes);
			}

			w.pop();
		}
	}

	public static void addPssdDataSetContent(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		XmlDoc.Element ce = ae.element("content");
		if (ce == null) {
			return;
		}

		w.push("data");
		w.add(ce, false);
		w.pop();
	}

	/**
	 * Add a PSSD DataObject's object's meta-data. There is no federation impact
	 * on this function
	 * 
	 * @param w
	 * @param ae
	 * @throws Throwable
	 */
	public static void addPssdDataObject(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		addPssdDataSetContent(w, ae);
	}

	public static void addPssdMeta(XmlWriter w, XmlDoc.Element ae, String type) throws Throwable {

		XmlDoc.Element meta = ae.element("meta");
		if (meta == null) {
			return;
		}

		Collection<XmlDoc.Element> mes = meta.elements();
		if (mes == null) {
			return;
		}

		boolean pushMeta = true;

		for (XmlDoc.Element me : mes) {
			if (isPssdObjectMeta(me, type)) {
				if (pushMeta) {
					w.push("meta");
					pushMeta = false;
				}

				w.add(me);
			}
		}

		if (!pushMeta) {
			w.pop();
		}
	}

	public static void addPssdAttachments(XmlWriter w, XmlDoc.Element ae) throws Throwable {

		Collection<XmlDoc.Element> res = ae.elements("related[@type='attachment']/asset");
		if (res == null) {
			return;
		}

		for (XmlDoc.Element re : res) {
			String id = re.value("@id");
			w.push("attachment", new String[] { "id", id });
			// TODO - other details..
			w.pop();
		}
	}

	/**
	 * 
	 * @param me
	 * @param type
	 * @return
	 * @throws Throwable
	 */
	public static boolean isPssdObjectMeta(XmlDoc.Element me, String type) throws Throwable {

		String tag = me.value("@tag");
		if (tag != null) {
			if (tag.equalsIgnoreCase("pssd.meta")) {
				return true;
			}
		}

		String ns = me.value("@ns");
		if (ns == null) {
			return false;
		}

		if (ns.equalsIgnoreCase(Metadata.modelNameForType(type))) {
			return true;
		}

		return false;
	}

}
