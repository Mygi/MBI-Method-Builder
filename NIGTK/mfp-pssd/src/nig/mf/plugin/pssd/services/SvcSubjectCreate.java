package nig.mf.plugin.pssd.services;


import java.util.Vector;

import nig.encrypt.EncryptionTypes.EncryptionType;
import nig.mf.plugin.pssd.ModelUser;
import nig.mf.plugin.pssd.Project;
import nig.mf.plugin.pssd.ProjectMember;
import nig.mf.plugin.pssd.Subject;
import nig.mf.plugin.pssd.util.PSSDUtils;
import nig.mf.plugin.util.XMLUtil;
import nig.mf.pssd.plugin.util.DistributedAsset;
import nig.mf.pssd.plugin.util.DistributedAssetUtil;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;



public class SvcSubjectCreate extends PluginService {
	
	private static class TemplateDefinitions {
		public XmlDoc.Element publicTemplate;
		public XmlDoc.Element privateTemplate;
	}
	
	private Interface _defn;

	public SvcSubjectCreate() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent (project).", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("subject-number", IntegerType.POSITIVE_ONE,
				"Specifies the subject number for the identifier. If not given, the next available subject is created. If specified, then there cannot be any other asset/object with this identity assigned. Used for importing subjects from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the subject-number is not given, fill in the Subject allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to true; use with care in federated envionment.", 0, 1));
//
		me = new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the (primary) research method for this subject. In a federation, must be managed by the same server as the parent Project.", 1, 1);
     	_defn.add(me);
     	//
		me = new Interface.Element("r-subject",CiteableIdType.DEFAULT, "The identity of the r-subject that this subject represents. In a federation, must be managed by the same server as the parent Project.", 0, 1);
		_defn.add(me);
		//
		addInterfaceDefn (_defn);
		
		// Turn off this layer of encryption as the implementation is flawed (element value checking against dictionary/enum values
		// will fail as the value is encrypted and the dictionary/enum not)
		//_defn.add(new Element("encryption", BooleanType.DEFAULT, "Encrypt documents in the private namespace (recommended). Default is true.", 0, 1));
	
		// NB: 'method', 'r-subject' and 'data-use' have to match elements in the pssd-subject document type
	}
	
	// This function re-used by SvcSubjectUpdate
	public static void addInterfaceDefn(Interface defn) {

		defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this subject.",0, 1));
		defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the subject.", 0, 1));
		
		Element me = new Element("public",XmlDocType.DEFAULT,"Optional project public metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);
		defn.add(me);
		
		me = new Element("private",XmlDocType.DEFAULT,"Optional project private metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);
		defn.add(me);

		defn.add(new Interface.Element("data-use", 
				new EnumType(new String [] {Project.CONSENT_SPECIFIC_ROLE_NAME, 
						                    Project.CONSENT_EXTENDED_ROLE_NAME, 
						                    Project.CONSENT_UNSPECIFIED_ROLE_NAME}), 
						                    "Data (re)use specification for this Subject. Defaults to none", 0, 1));
		
	}

	public String name() {
		return "om.pssd.subject.create";
	}

	public String description() {
		return "Creates a PSSD subject and associated executing (primary) ExMethod object (will re-use pre-existing CIDs or create new) on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	/**
	 * Create a subject on the local server.  The citable ID may be managed by a remote
	 * server which must be accessed to allocate the new citable ID
	 * 
	 */
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Distributed ID for parent Project. It must be a primary or we are not allowed
		// to create children under it.
		DistributedAsset dPID = new DistributedAsset (args.element("pid"));
		dPID.print("Project");
		
		// Validate
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dPID);
		if (type==null) {
			throw new Exception("The asset associated with " + dPID.toString() + " does not exist");
		}
		if ( !type.equals(Project.TYPE) ) {
			throw new Exception("Object " + dPID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
		}
		if (dPID.isReplica()) {
			throw new Exception ("The supplied parent Project is a replica and this service cannot create its child");
		}

		// If the user does not give subject-number,  we may want to fill in 
		// any holes in the allocator space for subjects as sometimes we use 
		// re-use projects CIDs
		long subjectNumber = args.longValue("subject-number", -1);		
        boolean fillIn = args.booleanValue("fillin", true);

        // We are going to create the Subject locally.  The owner of the Project must 
        // authorise the user to be able to create Subjects because s/he is the primary
        // manager of the project.   Therefore we must check the user's role with that server. 
        // A primary project will always be managed by the same server on which that object exists.
        if (!dPID.isLocal()) {
        	Boolean isAdmin = (ModelUser.hasRole(dPID.getServerRouteObject(), executor(), 
        						Project.subjectAdministratorRoleName(dPID.getCiteableID())) ||
        			ModelUser.hasRole(dPID.getServerRouteObject(), executor(), 
        						PSSDUtils.OBJECT_ADMIN_ROLE_NAME));
        	if (!isAdmin) {
        		throw new Exception ("User not authorised: requires '" + Project.subjectAdministratorRoleName(dPID.getCiteableID()) +
        				"' or '" + PSSDUtils.OBJECT_ADMIN_ROLE_NAME + " role on server managing Project : " + dPID.getManagingServerUUID());
        	}
        }
        
		// Because the Subject is going to be created locally, the user must have 
		// the Subject admin role locally as well.  The roles have to be distributed.
        Boolean isAdmin = (ModelUser.hasRole(null, executor(), Project.subjectAdministratorRoleName(dPID.getCiteableID())) ||
        		ModelUser.hasRole(null, executor(), PSSDUtils.OBJECT_ADMIN_ROLE_NAME));
        if (!isAdmin) {
        	throw new Exception ("User not authorised: requires '" + Project.subjectAdministratorRoleName(dPID.getCiteableID()) +
        			"' or '" + PSSDUtils.OBJECT_ADMIN_ROLE_NAME + " role on local server"); 
        }
	
		// Create the Subject on the local server. The Method will be checked in this call (which is
		// static and re-used by other services) to be managed by the same server as the Project
		//boolean encryptOnCreate = args.booleanValue("encryption", true);
        
        // Turned off as implementation flawed (see notes above in interface definition)
        boolean encryptOnCreate = false;
        
		String actionOnUpdate = "replace";
		String cid = createOrUpdateSubjectAsset(executor(), args, null, dPID, subjectNumber, fillIn, encryptOnCreate, actionOnUpdate);
		DistributedAsset dCID = new DistributedAsset(null, cid); 

		// Create the ExMethod on the local server
		String emid = null;
		try {
			long exMethodNumber = -1;			
			DistributedAsset dMID = new DistributedAsset(dPID.getServerRoute(), args.value("method"));
			emid = SvcExMethodCreate.create(executor(), dCID, exMethodNumber, dMID, fillIn);
		} catch ( Throwable t ) {
			cleanUp(executor(), cid);
			throw t;
		}
		
		w.add("id",new String[] { "mid", emid },cid);
	}
	
	/** 
	 * Create (locally) or update Subject object (anywhere in Federation)
	 * 
	 * @param executor
	 * @param args
	 * @param id is CID of subject if updating (null if creating).  The subject must be on the local server.
	 * @param dPID is Distributed CID of parent Project (both updating and creating)
	 * @param subjectNumber is the desired child Subject ID when creating new.  If null, next available is used.
	 * @param encryptOnCreate indicates that when creating a subject whether encryption should be used or not
	 * @return
	 * @throws Throwable
	 */
	public static String createOrUpdateSubjectAsset(ServiceExecutor executor, XmlDoc.Element args, String id, DistributedAsset dPID, 
			long subjectNumber, boolean fillIn, boolean encryptOnCreate, String actionOnUpdate) throws Throwable {

		if (!actionOnUpdate.equals("merge") && !actionOnUpdate.equals("replace") && !actionOnUpdate.equals("add")) {
			throw new Exception("Illegal value for action: must be 'add', 'merge' or 'replace'");
		}

		
		// The Method must be on the same server as the Project (as the Project object
		// specifies the Methods)
		String mid = args.value("method");       // Used in creating only, not updating

		if (mid!=null) {
			String pdist = null;                 // Local query on project server
			if (!DistributedAssetUtil.assetExists(executor, dPID.getServerRoute(), pdist, mid, ResultAssetType.primary, 
					false, true, null)) {
				throw new Exception ("The Method object (" + mid + ") cannot be found; it must be managed by the same server as the Project object");
			}
		}
	
		// The R-Subject must also be managed by the same server as the Project. This is possibly
		// not really required (and easily relaxed).  Because we have already checked that the Project
		// is Primary, the supplying and managing server are the same. So the ServerRoute for the Project
		// is all we need to locate the RSubject
		String rsid = args.value("r-subject");
		if (rsid!=null) {
			if (!DistributedAssetUtil.assetExists(executor, dPID.getServerRoute(), null, rsid, ResultAssetType.primary, 
					false, true, null)) {
				throw new Exception ("The RSubject object (" + rsid + ") must be managed by the same server as the Project object");
			}
		}

		//
		String dataUse = args.value("data-use");  
		XmlDocMaker dm = new XmlDocMaker("args");
		
		// If ID is null we will make a new Subject, else update the existing one
		// Any existing Subject must be on the local server.
		String cid = null;
		if ( id == null ) {
			if (dPID==null) {
				throw new Exception("Both id and dPID are null; this is a coding error");
			}
			
			// Generate CID for new Subject, filling in allocator space if desired
			// The parent could be anywhere in the federation
			cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateCiteableID(executor, dPID, subjectNumber, fillIn);
			dm.add("cid", cid);
			//
			dm.add("namespace", PSSDUtils.namespace(executor, dPID));
			dm.add("model",Subject.MODEL);
			dm.push("meta");
		} else {
			dm.add("cid",id);
			dm.push("meta",new String[] { "action", actionOnUpdate });
		}
	
		// Set the standard PSSD required meta-data for an object.  Must always set when
		// creating.
		String name = args.value("name");
		String desc = args.value("description");
		if (id==null || (name!=null || desc!=null)) {
			PSSDUtils.setObjectMeta(dm, Subject.TYPE, name, desc);
		}
		
		// Set the rest of the generic Subject meta-data
		// WIth the current interface meta-data specified for subject.create and subject.update this is currently null
		PSSDUtils.setObjectOptionalMeta(dm, args.element("meta"),"om.pssd.subject");

		// Set method and r-subject meta-data which matches the pssd-subject Document Type
		// This code is a total mess... The presence of the 'add' action complicates it even further
		if ( mid != null || rsid != null || dataUse != null) {
			boolean pushed = false;
			if ( mid != null ) { 
				dm.push("pssd-subject");
				pushed = true;
				dm.add("method", mid);
			} else {
				if (id!=null) {
					// Get the Method meta for the pre-existing Subject. 
					// This is a required field for pssd-subject so we must add it back in
					// as its not editable via the update interface
					if (!actionOnUpdate.equals("add")) {
						dm.push("pssd-subject");
						pushed = true;
						dm.add("method", getExisting(executor, id, "object/method/id"));  
					}
				}
			}

			if (rsid != null) {
				if (!pushed) {
					dm.push("pssd-subject");
					pushed = true;
				}
				dm.add("r-subject",rsid);
			} else {
				/*
				if (id!=null) {
					// Get the R-Subject meta-data from the pre-existing local Subject
					dm.add("r-subject", getExisting(executor, id, "object/r-subject"));
				}
				 */
			}

			// Make sure any data-use is self-consistent with the Project specification
			if (dataUse != null) {

				// Check that the subject data-use is consistent with any Project data-use				
				String projectDataUse = Project.getProjectDataUse(executor, dPID);
				String dataUse2 = ProjectMember.setValidSubjectDataUse(projectDataUse, dataUse);

				if (!pushed) {
					dm.push("pssd-subject");
					pushed = true;
				}
				dm.add("data-use", dataUse2);
			} else {
				/*
				if (id!=null) {
					// Get the data-use meta from the pre-existing local Subject. 
					dm.add("data-use", getExisting(executor, id, "data-use"));
				}
				 */
			}

			if (pushed) dm.pop();
		}

		// Set public and private.If updating, if these are unspecified they won't change
		XmlDoc.Element pme = args.element("public");
		if ( pme != null ) {
			PSSDUtils.setMetaNamespace(pme,"pssd.public");
			dm.add(pme,false);
		}

		pme = args.element("private");
		if ( pme != null ) {
			// Encrypt values of all elements in all documents
			Vector docs = pme.elements();
			if (id==null) {
				if (encryptOnCreate) XMLUtil.encryptXML(EncryptionType.BASE_64, docs);
			} else {
				XMLUtil.encryptXML(EncryptionType.BASE_64, docs);	
			}

			PSSDUtils.setMetaNamespace(pme,"pssd.private");
			dm.add(pme,false);
		}
		dm.pop(); // meta

		// If creating the Subject, add in the prototype information from the method 
		if ( id == null ) {
			TemplateDefinitions tds = subjectTemplate(executor,dPID.getServerRoute(), mid);
			if ( tds.publicTemplate != null ) {
				dm.push("template",new String[] { "ns", "pssd.public" });
				dm.addAll(tds.publicTemplate.elements("metadata"));
				dm.pop();
			}

			if ( tds.privateTemplate != null ) {
				// Should this be encrypted too ?
				dm.push("template",new String[] { "ns", "pssd.private" });
				dm.addAll(tds.privateTemplate.elements("metadata"));
				dm.pop();
			}

			// Add the ACLs
			PSSDUtils.addSubjectACLs(dm, dPID.getCiteableID());

			// Create asset on local server
			XmlDoc.Element r = executor.execute("asset.create",dm.root());
			if (cid==null) {
				return r.value("cid");         
			} else {
				return cid;               // If we create with a known cid asset.create does not return it.
			}
		} else {

			// Update asset on local server
			executor.execute("asset.set",dm.root());
			return id;
		}
	}
	
	/*
	private XmlDoc.Element prototypeMethodSubject(String mid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",mid);
		
		dm.add("pdist",0);                 // Force local 	
		XmlDoc.Element r = executor().execute("asset.get",dm.root());
		return r.element("asset/meta/pssd-method-subject");
	}
	*/
	
	
	/**
	 * Get specified element from pre-existing Subject
	 * 
	 * @param id The citeable id of the object
	 * @param xpath The xpath to extract the desired element from
	 * 
	 */
	private static String getExisting (ServiceExecutor executor, String cid, String xpath) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		
		XmlDoc.Element r = executor.execute("om.pssd.object.describe",dm.root());
		return r.value(xpath);
	}
	

	

	/**
	 * 
	 * @param executor
	 * @param proute Route to the Method object
	 * @param mid  CID of the Method
	 * @return
	 * @throws Throwable
	 */
	private static TemplateDefinitions subjectTemplate(ServiceExecutor executor, String proute, String mid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", mid);
		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		
		// Method must be managed by same server as Project
		XmlDoc.Element r = executor.execute(new ServerRoute(proute), "asset.get",dm.root());
		
		TemplateDefinitions tds = new TemplateDefinitions();
		tds.publicTemplate = r.element("asset/meta/pssd-method-subject/public");
		tds.privateTemplate = r.element("asset/meta/pssd-method-subject/private");
		
		return tds;
	}
	
	private void cleanUp(ServiceExecutor executor, String id) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",id);
		XmlDoc.Element r = executor.execute("om.pssd.object.exists", dm.root());
		if (r.booleanValue("exists")) {
			executor.execute("om.pssd.object.destroy",dm.root());
		}
	}
}

