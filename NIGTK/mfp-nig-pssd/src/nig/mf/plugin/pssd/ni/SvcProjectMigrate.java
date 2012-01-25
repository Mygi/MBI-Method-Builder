package nig.mf.plugin.pssd.ni;

import java.util.HashMap;
import java.util.Collection;

import nig.mf.plugin.util.AssetUtil;
import nig.mf.pssd.plugin.util.CiteableIdUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
//import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialized function to migrate a Project from one Method to another. The service creates the recipient Project
 * (using the input as a template) and then creates new Subjects according to the input Project template, but with the
 * newly specified Method. The service can optionally create Studies (per the input Project template) and move DataSets
 * from the input Project to the output (irreversible). Metadata can be specified to be located with the new Studies on
 * a per Method step basis. Document types can be specified to copy from the input Subject/RSubject to the output
 * Subject (no RSubjects are created, only Subjects).
 * 
 * @author nebk
 * 
 */
public class SvcProjectMigrate extends PluginService {
	private Interface _defn;
	private HashMap<String, String> studyStepMap_ = null;
	private HashMap<String, Collection<XmlDoc.Element>> studyMetaMap_ = null;
	
	private long t0Subject;
	private long t1Subject;
	//
	private double tSubject = 0;
	private double tDataSets = 0;
	private double tStudies = 0;
	

	public SvcProjectMigrate() {

		_defn = new Interface();
		_defn.add(new Element("pid", CiteableIdType.DEFAULT, "The citeable asset id of the Project to copy from.",
						1, 1));
		_defn.add(new Element("project-number", IntegerType.POSITIVE_ONE, "The project number for the new Project.", 1,
				1));
		_defn.add(new Element("method", CiteableIdType.DEFAULT, "The new Method", 1, 1));

		// Specify Subject Document Types to copy; we do it with this structure (rather than the inverse)
		// to facilitate some code
		// :public
		// :rsubject
		// :subject
		// :private
		// :rsubject
		// :subject

		Interface.Element me = new Interface.Element("public", XmlDocType.DEFAULT,
				"Document Types to copy to the public namespace of the new Subject", 0, 1);
		me.add(new Interface.Element("subject", StringType.DEFAULT,
				"Document Types residing in the public or prviate namespace on the Subject", 0, Integer.MAX_VALUE));
		me.add(new Interface.Element("rsubject", StringType.DEFAULT,
				"Document Types residing in the public or private namespace on the RSubject", 0, Integer.MAX_VALUE));
		_defn.add(me);
		//
		me = new Interface.Element("private", XmlDocType.DEFAULT,
				"Document Types to copy to the private namespace of the new Subject", 0, 1);
		me.add(new Interface.Element("subject", StringType.DEFAULT,
				"Document Types residing in the private or public namespace on the Subject", 0, Integer.MAX_VALUE));
		me.add(new Interface.Element("rsubject", StringType.DEFAULT,
				"Document Types residing in the private, identity or public namespace on the RSubject", 0, Integer.MAX_VALUE));
		_defn.add(me);

		// Now stuff related to Steps
		// :step -step <step>
		// :study -step <step> <study cid>
		me = new Interface.Element(
				"step",
				XmlDocType.DEFAULT,
				"Specifies the default and over-riding children Method steps (paths) to apply to Studies.For every Study in the input Project, a matching Study in the output Project is created for the given Step. Can only copy with one Study type. If not given, no Studies are created.",
				0, 1);
		me.add(new Interface.Attribute("step", CiteableIdType.DEFAULT,
				"Default Method step that applies to all Studies unless over-ridden by child element(s).", 1));
		Interface.Element me2 = new Interface.Element("study", CiteableIdType.DEFAULT,
				"Study (input) CID (with Method Step) that over-rides the default (parent)", 0, Integer.MAX_VALUE);
		me2.add(new Interface.Attribute("step", CiteableIdType.DEFAULT,
				"Method step that applies to the given Study; over-rides the default given by the parent.", 1));
		me.add(me2);
		_defn.add(me);

		// Specify optional meta-data. This needs to be matched per Step. It was too hard to combine it
		// with the above Step elements, so the user has to make it match. We supply one one ":meta -step" element
		// per Step
		me = new Element(
				"meta",
				XmlDocType.DEFAULT,
				"Optional Study Method metadata - a list of asset documents to be attached to all Studies (passes through to the 'meta' argument of om.pssd.study.create). Since this is Method/Step meta-data, the namespace attribute must be set. This is ExMethod dependent, so the service will add this; i.e. do not include -ns <ExMethod>_<Step>.",
				0, Integer.MAX_VALUE);
		me.add(new Interface.Attribute("step", CiteableIdType.DEFAULT,
				"Method step for which Studies will have this meta-data added to them.	", 1));
		me.setIgnoreDescendants(true);
		_defn.add(me);

		//
		_defn.add(new Element("othermeta", StringType.DEFAULT,
				"Copy the given document types from the input to the output Studies.", 0, Integer.MAX_VALUE));

		// Now stuff related to DataSets
		_defn.add(new Element("datasets", BooleanType.DEFAULT,
				"Move (not copy, this cannot be undone) the input DataSets to the output as well. Defaults to false.",
				0, 1));
	}

	public String name() {
		return "nig.pssd.project.migrate";
	}

	public String description() {
		return "Service to migrate a local Project to a new Method.  Service creates local Subjects, ExMethods, Studies (optional) and moves DataSets (optional).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		// See if we will move DataSets over as well
		boolean moveDataSets = args.booleanValue("datasets");

		// Method
		String methodId = args.value("method");

		// Get project IDs and validate
		String pidFrom = args.value("pid");	
		PSSDUtil.isValidProject(executor(), pidFrom, true);
		//
		if (PSSDUtil.isReplica(executor(), pidFrom)) {
			throw new Exception ("This project is a replica and cannot be migrated.");
		}
		if (PSSDUtil.hasRemoteChildren(executor(), pidFrom)) {
			throw new Exception ("This project has children on remote peers. Cannot proceed");
		}
		//
		long projectNumber = args.longValue("project-number", -1);
		
		// Get content/size sum for check
		XmlDoc.Element sizeFrom = null;
		if (moveDataSets) sizeFrom = AssetUtil.contentSizeSum(executor(), pidFrom);

		// Get Document Types to copy
		// This could be enhanced to auto-copy all existing Doc Types
		XmlDoc.Element publicDocTypes = args.element("public");
		XmlDoc.Element privateDocTypes = args.element("private");
		if (publicDocTypes == null && privateDocTypes == null) return;

		// Get default step to apply to all Studies unless over-ridden by children elements
		XmlDoc.Element stepMeta = args.element("step");
		String defaultStep = stepMeta.attribute("step").value();

		// Make a Hash Map of the over-ride Steps
		// <Study CID,Step Path>
		Collection<XmlDoc.Element> studySteps = stepMeta.elements("study"); 
		if (studySteps != null)
			createStudyStepHashMap(studySteps);

		// Study Documents to set on a per Study/Step basis
		// <Step Path, XML>
		Collection<XmlDoc.Element> studyMeta = args.elements("meta"); 
		if (studyMeta != null)
			createStudyMetaHashMap(studyMeta);

		// Study Document Types to copy
		Collection<String> otherMeta = args.values("othermeta");

		// Get the input Subjects
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", pidFrom);
		doc.add("pdist", 0);         // Force local
		XmlDoc.Element r = executor().execute("om.pssd.collection.members", doc.root());
		Collection<XmlDoc.Element> subjects = r.elements("object");
		if (subjects == null) {
			throw new Exception("Source project has no subjects.");
		}

		// Create recipient Project
		String pidTo = createProject(executor(), pidFrom, projectNumber, methodId);

		// Iterate over Subjects
		w.push("subjects");
		for (XmlDoc.Element subjectFrom : subjects) {
			tStudies = 0;
			tDataSets = 0;
			//
			t0Subject = System.currentTimeMillis();
			System.out.println("Migrating subject " + subjectFrom.value("id"));

			// Create output Subject and copy over desired Document Types
			String cid = createSubjectandStudies(executor(), methodId, pidTo, subjectFrom, publicDocTypes,
					privateDocTypes, defaultStep, studyMeta, otherMeta, moveDataSets);
			t1Subject = System.currentTimeMillis();
			System.out.println("subject times " + t0Subject + ", " + t1Subject);
			w.add("id", cid);
			System.out.println ("   Time");
			System.out.println("       Subject " + ((double)(t1Subject-t0Subject))/1000.0);
			System.out.println("       Study   " + tStudies);
			System.out.println("       DataSet " + tDataSets);
		}
		
		// Get size after migration
		if (moveDataSets) {
			XmlDoc.Element sizeTo = AssetUtil.contentSizeSum(executor(), pidTo);
			w.push("content-sizes");
			w.add("from", sizeFrom.value("value"));
			w.add("to", sizeTo.value("value"));
			w.pop();
		}
		//
		w.pop();
		System.out.println("Migration of " + pidFrom + " to " + pidTo + " completed");
	}

	// Could move this function into the PSSD package Project object and create a service to execute it
	private String createProject(ServiceExecutor executor, String pidFrom, long projectNumber, String methodId)
			throws Throwable {

		// Get old project details
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", pidFrom);
		XmlDoc.Element projectFrom = executor.execute("om.pssd.object.describe", doc.root());
		XmlDoc.Element membersFrom = executor.execute("om.pssd.project.members.list", doc.root());

		// Build XML for new Project
		doc = new XmlDocMaker("args");
		if (projectNumber != -1)
			doc.add("project-number", projectNumber);
		//
		String name = projectFrom.value("object/name");
		if (name != null)
			doc.add("name", name);
		//
		String description = projectFrom.value("object/description");
		if (description != null)
			doc.add("description", description);
		//
		doc.push("method");
		doc.add("id", methodId);
		doc.pop();
		//
		
		Collection<XmlDoc.Element> userMembersFrom = membersFrom.elements("member");
		if (userMembersFrom != null) {
			for (XmlDoc.Element member : userMembersFrom) {
				doc.push("member");
				XmlDoc.Element auth = makeAuthority(member.value("@authority"), member.value("@protocol"));
				if (auth != null) doc.add(auth);
				String domain = member.value("@domain");
				if (domain != null) doc.add("domain", domain);
				String user = member.value("@user");
				if (user != null) doc.add("user", user);
				String role = member.value("@member");
				if (role != null) doc.add("role", role);
				String dataUse = member.value("@data-use");
				if (dataUse != null) doc.add("data-use", dataUse);
				doc.pop();
			}
		}
		//
		Collection<XmlDoc.Element> roleMembersFrom = membersFrom.elements("role-member");
		if (roleMembersFrom != null) {
			for (XmlDoc.Element roleMember : roleMembersFrom) {
				doc.push("role-member");
				String  member = roleMember.value("@member");
				if (member != null) doc.add("member", member);
				String role = roleMember.value("@role");
				if (role != null) doc.add("role", role);
				String dataUse = roleMember.value("@data-use");
				if (dataUse != null) doc.add("data-use", dataUse);
				doc.pop();
			}
		}

		//
		String dataUse = projectFrom.value("object/data-use");
		if (dataUse != null)
			doc.add("data-use", dataUse);
		//
		XmlDoc.Element meta = projectFrom.element("object/meta");
		if (meta != null) {
			// The namespaces seem to be handled ok in this process
			// "meta" has namespaces embedded in it, whereas when
			// we create a Project we don't specify
			doc.add(meta);
		}
		//
		XmlDoc.Element r = executor.execute("om.pssd.project.create", doc.root());
		return r.value("id");
	}

	/**
	 * This function generates a HashMap of key=study CID and value = Step path from the user supplied values
	 * 
	 * @param studySteps
	 * @throws Throwable
	 */
	private void createStudyStepHashMap(Collection<XmlDoc.Element> studySteps) throws Throwable {
		studyStepMap_ = new HashMap<String, String>();
		for (XmlDoc.Element study : studySteps) {
			String key = study.value(); // Study CID
			String value = study.attribute("step").value(); // Step Path
			if (studyStepMap_.containsKey(key)) {
				throw new Exception("Duplicate Study/Step given");
			} else {
				studyStepMap_.put(key, value);
			}
		}
	}

	/**
	 * This function generates a HashMap of key=step and value = XmlDoc.Element documents from the user supplied values.
	 * This HashMap is used to add meta-data to Studies of the desired step.
	 * 
	 * @param studyMeta
	 *            Collection of :meta -step <step> elements
	 * @throws Throwable
	 */
	private void createStudyMetaHashMap(Collection<XmlDoc.Element> studyMeta) throws Throwable {

		studyMetaMap_ = new HashMap<String, Collection<XmlDoc.Element>>();
		for (XmlDoc.Element meta : studyMeta) {
			String key = meta.attribute("step").value(); // Step Path
			Collection<XmlDoc.Element> value = meta.elements();
			if (studyMetaMap_.containsKey(key)) {
				throw new Exception("Duplicate Step/meta given");
			} else {
				studyMetaMap_.put(key, value);
			}
		}
	}

	/**
	 * Create the SUbjects and optionally Studies from the input Project template
	 * 
	 * @param executor
	 * @param methodId
	 * @param pidTo
	 *            is the project CID to copy to
	 * @param subjectMetaFrom
	 *            is the XML of the subject we are copying from
	 * @param subDocTypes
	 *            are the Doc Types to copy from the Subject
	 * @param rSubDocTypes
	 *            are the Doc Types to copy from the RSubject
	 * @param moveDataSets
	 *            if true *move* DataSets over to the new parent Study
	 * @throws Throwable
	 */
	private String createSubjectandStudies(ServiceExecutor executor, String methodId, String pidTo,
			XmlDoc.Element subjectMetaFrom, XmlDoc.Element publicDocTypes, XmlDoc.Element privateDocTypes,
			String defaultStep, Collection<XmlDoc.Element> studyMeta, Collection<String> otherMeta, boolean moveDataSets) throws Throwable {

		// Get the CIDs of the input Subject and RSubject
		String cidSubjectFrom = subjectMetaFrom.value("id");
		String cidRSubjectFrom = subjectMetaFrom.value("r-subject");

		// Get any RSubject meta-data
		XmlDoc.Element rSubjectMetaFrom = null;
		if (cidRSubjectFrom != null) {
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", cidRSubjectFrom);
			XmlDoc.Element t = executor.execute("om.pssd.object.describe", doc.root());
			rSubjectMetaFrom = t.element("object");
		}

		// Find subject number
		String subjectNumber = CiteableIdUtil.getLastSection(cidSubjectFrom);

		// Create meta-data document for new Subject
		XmlDocMaker docMaker = new XmlDocMaker("args");
		docMaker.add("pid", pidTo);
		docMaker.add("subject-number", subjectNumber);
		docMaker.add("method", methodId);
		//
		String name = subjectMetaFrom.value("name");
		if (name != null)
			docMaker.add("name", name);
		String description = subjectMetaFrom.value("description");
		if (description != null)
			docMaker.add("description", description);
		String dataUse = subjectMetaFrom.value("data-use");
		if (dataUse != null)
			docMaker.add("data-use", dataUse);

		// Copy desired Documents to the output Subject
		addSubjectDocuments(docMaker, publicDocTypes, privateDocTypes, subjectMetaFrom, rSubjectMetaFrom);

		// Create the Subject and ExMethod
		XmlDoc.Element r = executor.execute("om.pssd.subject.create", docMaker.root());

		// Get the CID of the Subject and ExMethod
		long t0Study = System.currentTimeMillis();
		XmlDoc.Element id = r.element("id");
		String cidSubjectTo = id.value();
		XmlDoc.Attribute att = id.attribute("mid");
		String cidExMethodTo = att.value();

		// Now generate Studies if desired.
		createStudies(executor, cidSubjectFrom, cidExMethodTo, defaultStep, studyMeta, otherMeta, moveDataSets);
		long t1Study = System.currentTimeMillis();
		tStudies = ((double)(t1Study-t0Study)) / 1000.0;

		// Return subject cid
		return cidSubjectTo;

	}

	/**
	 * Create the Studies using the input Project as a template
	 * 
	 * @param executor
	 * @param cidSubjectFrom
	 * @param cidExMethodTo
	 * @param step
	 * @param studyMeta
	 * @param moveDataSets
	 * @throws Throwable
	 */
	private void createStudies(ServiceExecutor executor, String cidSubjectFrom, String cidExMethodTo,
			String defaultStep, Collection<XmlDoc.Element> studyMeta, Collection<String> otherMeta, boolean moveDataSets) throws Throwable {

		// Bug out if no default Step
		if (defaultStep == null) return;

		// Get input ExMethod CID
		XmlDocMaker docMaker = new XmlDocMaker("args");
		docMaker.add("id", cidSubjectFrom);
		docMaker.add("pdist", 0);        // Force local
		XmlDoc.Element r = executor.execute("om.pssd.collection.members", docMaker.root());
		if (r == null) return; // No input ExMethod -> no Studies
		//
		Collection<XmlDoc.Element> exMethods = r.elements("object");
		if (exMethods.size() != 1) {
			throw new Exception("The input Subject (" + cidSubjectFrom + ") has more than one ExMethod; cannot handle");
		}

		// OK now we know we have just one ExMEthod, get it's ID.
		String cidExMethodFrom = r.value("object/id");

		// Get list of input Studies
		docMaker = new XmlDocMaker("args");
		docMaker.add("id", cidExMethodFrom);
		r = executor.execute("om.pssd.collection.members", docMaker.root());
		if (r == null) return; // No Studies to copy

		// Iterate over input Studies
		Collection<XmlDoc.Element> studiesFrom = r.elements("object");
		if (studiesFrom == null) return;
		//
		for (XmlDoc.Element studyMetaFrom : studiesFrom) {
			String cidStudyFrom = studyMetaFrom.value("id");
			String name = studyMetaFrom.value("name");
			String description = studyMetaFrom.value("description");
			String type = studyMetaFrom.value("type");
			System.out.println ("   Study " + cidStudyFrom);

			// Generate output Study and set Method meta-data given by caller
			docMaker = new XmlDocMaker("args");

			// Try to re-use the Study number
			// Find subject number
			String studyNumber = CiteableIdUtil.getLastSection(cidStudyFrom);
			docMaker.add("study-number", studyNumber);
			docMaker.add("pid", cidExMethodTo);
			//
			if (name != null) docMaker.add("name", name);
			if (description != null) docMaker.add("description", description);

			// Set correct step; use default or over-ride Study/Step
			String stepTo = defaultStep;
			if (studyStepMap_ != null) {
				if (studyStepMap_.containsKey(cidStudyFrom)) stepTo = studyStepMap_.get(cidStudyFrom);
			}
			docMaker.add("step", stepTo);

			// Stick on the step-specific Study meta-data the user has specified.
			if (studyMetaMap_ != null) {

				// Get any meta-data documents for this step
				if (studyMetaMap_.containsKey(stepTo)) {
					docMaker.push("meta");
					Collection<XmlDoc.Element> docs = studyMetaMap_.get(stepTo);
					for (XmlDoc.Element doc : docs) {
						XmlDoc.Attribute ns = doc.attribute("ns");
						String t = cidExMethodTo + "_" + stepTo;

						// We must set the namespace of the meta-data to be <ExMethodCid>_<Step>
						// Because each ExMethod has a different CID, the user can't supply this.
						if (ns != null) {
							ns.setValue(t);
						} else {
							ns = new XmlDoc.Attribute("ns", t);
							doc.add(ns);
						}
						docMaker.add(doc);
					}
					docMaker.pop();
				}
			}

			// Create output study
			r = executor.execute("om.pssd.study.create", docMaker.root());
			String cidStudyTo = r.value("id");

			// Copy the other desired meta-data. It's easier this way than trying to combine with Study meta
			if (otherMeta != null) {

				docMaker = new XmlDocMaker("args");
				docMaker.add("id", cidStudyTo);
				if (name != null) docMaker.add("name", name);
				if (description != null) docMaker.add("description", description);
				docMaker.add("type", type);
				docMaker.push("meta");

				// Iterate over specified Document Types
				for (String docType : otherMeta) {

					// Find document on input Study and copy if exists
					XmlDoc.Element docIn = studyMetaFrom.element("meta/" + docType);
					if (docIn != null) {
						// Remove attributes we don't want
						XmlDoc.Attribute attr = docIn.attribute("id");
						if (attr != null) {
							docIn.remove(attr);
						}
						docMaker.add(docIn);
					}
				}

				// Update study
				executor.execute("om.pssd.study.update", docMaker.root());
			}

			// Move (not copy) over DataSets and meta-data if desired
			if (moveDataSets) {
				long t0DataSet = System.currentTimeMillis();

				// FInd all input children DataSets
				docMaker = new XmlDocMaker("args");
				docMaker.add("id", cidStudyFrom);
				XmlDoc.Element r2 = executor.execute("om.pssd.collection.members", docMaker.root());

				// Move each DataSet for this Study and any children
				Collection<String> dataSets = null;
				if (r2!=null) dataSets = r2.values("object/id");
				if (dataSets != null) {
					moveDataSets (executor, dataSets, cidStudyTo);

					// Unfortunately we are not quite done... WHen a DataSet is created, we specify the ExMethod/step
					// for which it was created. We now need to update these as well as they have now changed :-(
					docMaker = new XmlDocMaker("args");
					docMaker.add("id", cidStudyTo);
					r2 = executor.execute("om.pssd.collection.members", docMaker.root());
					if (r2!=null) {
	
						// Fix up each DataSet's meta-data
						dataSets = r2.values("object/id");
						setDataSetsMeta (executor, dataSets, cidExMethodTo, stepTo);
					}
				}
				long t1DataSet = System.currentTimeMillis();
				tDataSets += ((double)(t1DataSet-t0DataSet))/1000.0;
			}
		}
	}
	
	private void moveDataSets (ServiceExecutor executor, Collection<String> dataSets, String cidStudyTo) throws Throwable {

		for (String cidDataSet : dataSets) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", cidDataSet);
			dm.add("pid", cidStudyTo);
			dm.add("preserve", true);
			executor.execute("om.pssd.dataset.move", dm.root());
		}
	}

	private void setDataSetsMeta (ServiceExecutor executor, Collection<String> dataSets, String cidExMethodTo, String stepTo) throws Throwable {

		for (String cidDataSet : dataSets) {
	
			// Get the DataSet meta-data
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", cidDataSet);
			XmlDoc.Element r3 = executor.execute("om.pssd.object.describe", dm.root());
			String dataSetType = r3.value("object/source/type");

			// Set
			XmlDocMaker dm2 = new XmlDocMaker("args");
			dm2.add("id", cidDataSet);
			dm2.push("method");
			dm2.add("id", cidExMethodTo);
			dm2.add("step", stepTo);
			dm2.pop();
	
			if (dataSetType.equals("derivation")) {
				r3 = executor.execute("om.pssd.dataset.derivation.update", dm2.root());
			} else if (dataSetType.equals("primary")) {
				r3 = executor.execute("om.pssd.dataset.primary.update", dm2.root());
			} else {
				// This would be a bad place to throw an exception...
				System.out.println("SvcProjectMigrate.createStudies: unexpected dataset type = " + dataSetType);
			}
		}
	}


	/**
	 * Add all of the specified documents to the output Subject
	 * 
	 * @param docMaker
	 * @param publicDocTypes
	 * @param privateDocTypes
	 * @param subjectMetaFrom
	 * @param rSubjectMetaFrom
	 * @throws Throwable
	 */
	private void addSubjectDocuments (XmlDocMaker docMaker, XmlDoc.Element publicDocTypes, XmlDoc.Element privateDocTypes, 
			XmlDoc.Element subjectMetaFrom, XmlDoc.Element rSubjectMetaFrom) throws Throwable {
		
		// :public
		//    :rsubject
		//    :subject
		// :private
		//    :rsubject
		//    :subject
	
	  	// Now copy desired Document Types to "public" on the Subject
		// The input documents may come from any of the public or private
		// areas, regardless of whether they are destined for public or private
		// in the output.  This is because the input and output Methods may
		// not map public to public, private to private.  The specification
		// in calling this service has ultimate control over this (and it
		// it is unlikely that the same doc type resides in multiple places)
	  	if (publicDocTypes != null) {
	  		Collection<XmlDoc.Element> subjectDocTypes = publicDocTypes.elements("subject");
	  		Collection<XmlDoc.Element> rSubjectDocTypes = publicDocTypes.elements("rsubject");
	  		
	  		// OK the user has provided some Document Types to copy
	  		if (rSubjectDocTypes!=null || subjectDocTypes!=null) {
	  			docMaker.push("public");
	  			
	  			// Find the Subject public or private Documents and copy to "public" output
	  			if (subjectDocTypes!=null && subjectMetaFrom!=null) {
	  				XmlDoc.Element doc = subjectMetaFrom.element("public");
	  				addDocuments (docMaker, subjectDocTypes, doc);
	  			}
	  			if (subjectDocTypes!=null && subjectMetaFrom!=null) {
	  				XmlDoc.Element doc = subjectMetaFrom.element("private");
	  				addDocuments (docMaker, subjectDocTypes, doc);
	  			}
	  			
	  			// Find the  RSubject public, or private Documents and copy to "public" output
	  			if (rSubjectDocTypes!=null && rSubjectMetaFrom!=null) {
	  				XmlDoc.Element doc = rSubjectMetaFrom.element("public");
	  				addDocuments (docMaker, rSubjectDocTypes, doc);
	  			}
	  			if (rSubjectDocTypes!=null && rSubjectMetaFrom!=null) {
	  				XmlDoc.Element doc = rSubjectMetaFrom.element("private");
	  				addDocuments (docMaker, rSubjectDocTypes, doc);
	  			}
	  			if (rSubjectDocTypes!=null && rSubjectMetaFrom!=null) {
	  				XmlDoc.Element doc = rSubjectMetaFrom.element("identity");
	  				addDocuments (docMaker, rSubjectDocTypes, doc);
	  			}

	  			//
	  			docMaker.pop();
	  		}
	  	}
	  	
	  	// Now copy desired Document Types to "private" on the Subject
	  	// See notes for "public" output above
	  	if (privateDocTypes != null) {
	  		Collection<XmlDoc.Element> subjectDocTypes = privateDocTypes.elements("subject");
	  		Collection<XmlDoc.Element> rSubjectDocTypes = privateDocTypes.elements("rsubject");
	  		
	  		// OK the user has provided some Document Types to copy
	  		if (rSubjectDocTypes!=null || subjectDocTypes!=null) {
	  			docMaker.push("private");
	  			
	  			// Find the Subject public/private Documents and copy to "private" output
	  			if (subjectDocTypes!=null && subjectMetaFrom!=null) {
	  				XmlDoc.Element doc = subjectMetaFrom.element("public");
	  				addDocuments (docMaker, subjectDocTypes, doc);
	  			}
	  			if (subjectDocTypes!=null && subjectMetaFrom!=null) {
	  				XmlDoc.Element doc = subjectMetaFrom.element("private");
	  				addDocuments (docMaker, subjectDocTypes, doc);
	  			}
	  			
	  			// Find the  RSubject public/private Documents and copy to "private" output
	  			if (rSubjectDocTypes!=null && rSubjectMetaFrom!=null) {
	  				XmlDoc.Element doc = rSubjectMetaFrom.element("public");
	  				addDocuments (docMaker, rSubjectDocTypes, doc);
	  			}
	  			if (rSubjectDocTypes!=null && rSubjectMetaFrom!=null) {
	  				XmlDoc.Element doc = rSubjectMetaFrom.element("private");
	  				addDocuments (docMaker, rSubjectDocTypes, doc);
	  			}
	  			// Find the  RSubject identity Documents and copy to "private" output
	  			if (rSubjectDocTypes!=null && rSubjectMetaFrom!=null) {
	  				XmlDoc.Element doc = rSubjectMetaFrom.element("identity");
	  				addDocuments (docMaker, rSubjectDocTypes, doc);
	  			}
	  			//
	  			docMaker.pop();
	  		}
	  	}
	}
	
	
	
	/**
	 * 
	 * @param docMaker
	 * @param docTypes
	 *            The document types the user specified tp copy
	 * @param metaFrom
	 *            The documents on the input object that we can copy from
	 * @throws Throwable
	 */
	private void addDocuments(XmlDocMaker docMaker, Collection<XmlDoc.Element> docTypes, XmlDoc.Element metaFrom) throws Throwable {

		// Iterate through the documents attached to the object and copy
		// the ones the user has specified to the output
		if (metaFrom != null && docTypes != null) {
			Collection<XmlDoc.Element> docs = metaFrom.elements();
			for (XmlDoc.Element doc : docs) {
				String name = doc.name();

				// Remove attributes we don't want
				// om.pssd.subject.create will add the ns etc that it wants
				XmlDoc.Attribute attr = doc.attribute("ns");
				if (attr != null) {
					doc.remove(attr);
				}
				attr = doc.attribute("id");
				if (attr != null) {
					doc.remove(attr);
				}
				attr = doc.attribute("tag");
				if (attr != null) {
					doc.remove(attr);
				}

				// See if we want this one. Do low-tech for loop...
				boolean keep = false;
				for (XmlDoc.Element t : docTypes) {
					if (t.value().equals(name)) {
						keep = true;
						break;
					}
				}
				if (keep) {
					docMaker.add(doc);
				}
			}
		}
	}
	
	private XmlDoc.Element makeAuthority (String authority, String protocol) {
		if (authority!=null) {
			XmlDoc.Element e = new XmlDoc.Element ("authority", authority);
			if (protocol!=null) {
				XmlDoc.Attribute a = new XmlDoc.Attribute("protocol", protocol);
				e.add(a);
			}
			return e;
		}
		return null;
	}

}
