package nig.mf.plugin.pssd.ni;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.PluginService.Interface.Attribute;
import arc.mf.plugin.dtype.*;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import java.util.Collection;
import java.util.Vector;

import nig.mf.plugin.util.TemplateUtil;

/**
 * This plugin service returns the meta-data required to create a particular PSSD object type
 */

public class SvcMetadataGet extends PluginService {

	private Interface _defn;
	private static final String CID_IDENTIFIER = ".";
	private static final String PROJECT = "project";
	private static final String SUBJECT = "subject";
	private static final String STUDY = "study";
	private static final String CREATE_PROJECT_METHOD = "om.pssd.project.create";
	private static final String CREATE_SUBJECT_METHOD = "om.pssd.subject.create";
	private static final String CREATE_STUDY_METHOD = "om.pssd.study.create";
	private static final String METADATA_DESCRIBE = "om.pssd.type.metadata.describe";
	private static final String METHOD_LIST = "om.pssd.method.find";
	private static final String METHOD_SUBJECT_DESCRIBE = "om.pssd.method.subject.metadata.describe";
	private static final String METHOD_DESCRIBE = "om.pssd.method.describe";
	private static final String TEXTFIELD = "TextField";
	private static final String MENU = "Menu";
	private static final String CREATE_PROJECT = "Create Project";
	private static final String CREATE_SUBJECT = "Create Subject";
	private static final String CREATE_STUDY = "Create Study";
	private static final String STUDY_TYPE_DICTIONARY = "pssd.study.types";

	public SvcMetadataGet() {
		_defn = new Interface();
		// define interface here
		Element argID = new Element("nodeID", StringType.DEFAULT,
				"The type of node to create ('project', 'subject', study')", 1, 1);
		argID.add(new Attribute("parent", StringType.DEFAULT,
				"If nodeID=subject or study, this gives the CID of the  parent node", 0));
		argID.add(new Attribute("method", StringType.DEFAULT,
				"If nodeID=subject, this gives the CID of the Method to be executed", 0));
		argID.add(new Attribute("step", StringType.DEFAULT,
				"If nodeID=study, this gives the relative path (e.g. 1, 1.1 etc) of the step to be executed", 0));
		_defn.add(argID);
	}

	public String name() {
		return "nig.pssd.metadata.get";
	}

	public String description() {
		return "Returns the meta-data required to create a particular PSSD node type";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String nodeID = args.value("nodeID");
		String parentCID = args.value("nodeID/@parent");
		String methodCID = args.value("nodeID/@method");
		String stepCID = args.value("nodeID/@step");
		String cid_identifier = CID_IDENTIFIER;
		if (nodeID.equals(PROJECT)) {
			getCreateProjectMetadata(w);
		} else if (nodeID.equals(SUBJECT) && methodCID != null && !methodCID.equals("") && parentCID != null
				&& !parentCID.equals("")) {
			getCreateSubjectMetadata(w, methodCID, parentCID);
		} else if (nodeID.equals(STUDY) && stepCID != null && !stepCID.equals("") && parentCID != null
				&& !parentCID.equals("")) {
			getCreateStudyMetadata(w, parentCID, stepCID);
		}
	}

	/**
	 * create project metadata information
	 */
	public void getCreateProjectMetadata(XmlWriter w) throws Throwable {
		XmlDoc.Element metadataElement = new XmlDoc.Element("metadata");
		XmlDoc.Element formElement = new XmlDoc.Element("form");

		/*
		 * 1) return the service call to make to create the project 2) return the generic pssd metadata to create the
		 * form 3) return the domain specific metadata to create the form
		 */
		XmlDoc.Element serviceElement = new XmlDoc.Element("service");
		XmlDoc.Element serviceTitle = new XmlDoc.Element("title", CREATE_PROJECT);
		XmlDoc.Element serviceName = new XmlDoc.Element("name", CREATE_PROJECT_METHOD);
		serviceElement.add(serviceTitle);
		serviceElement.add(serviceName);

		/* creating the pssd metadata required by the om.pssd.project.create service */
		XmlDoc.Element interfaceElement = createInterfaceProjectPSSD();

		/* creating the generic domain specific metadata */
		XmlDoc.Element domainElement = createDomainProject();

		/* adding everything into the final writer */
		metadataElement.add(serviceElement);
		formElement.add(interfaceElement);
		if (domainElement != null)
			formElement.add(domainElement);

		metadataElement.add(formElement);

		w.add(metadataElement);

		return;
	}


	public XmlDoc.Element createInterfaceProjectPSSD() throws Throwable {
		/* creating the name field in the create project interface */
		XmlDoc.Element categoryElement = new XmlDoc.Element("category");
		categoryElement.add(new XmlDoc.Attribute("type", "interface.metadata"));

		XmlDoc.Attribute minOccurs1 = new XmlDoc.Attribute("min-occurs", 1);
		XmlDoc.Attribute maxOccurs1 = new XmlDoc.Attribute("max-occurs", 1);
		XmlDoc.Attribute maxOccursInf = new XmlDoc.Attribute("max-occurs", "infinity");

		// "name"
		XmlDoc.Element formField = new XmlDoc.Element("field");
		formField.add(minOccurs1);
		formField.add(maxOccurs1);
		formField.add(new XmlDoc.Element("type", TEXTFIELD));
		formField.add(new XmlDoc.Element("name", "name"));
		formField.add(new XmlDoc.Element("description", "The name of the Project"));
		categoryElement.add(formField);

		// "description"
		formField = new XmlDoc.Element("field");
		formField.add(minOccurs1);
		formField.add(maxOccurs1);
		formField.add(new XmlDoc.Element("type", TEXTFIELD));
		formField.add(new XmlDoc.Element("name", "description"));
		formField.add(new XmlDoc.Element("description", "A description of the Project"));
		categoryElement.add(formField);

		// "method"
		formField = new XmlDoc.Element("field");
		formField.add(minOccurs1);
		formField.add(maxOccursInf);
		formField.add(new XmlDoc.Element("type", MENU));
		formField.add(new XmlDoc.Element("name", "method"));
		formField.add(new XmlDoc.Element("description", "The Method(s) to select for this Project"));

		XmlDoc.Element restriction = new XmlDoc.Element("restriction");
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("for", SUBJECT);

		XmlDoc.Element methodsList = executor().execute(METHOD_LIST, doc.root());
		Collection<XmlDoc.Element> coMethods = methodsList.elements("id");
		if (coMethods != null) {
			for (XmlDoc.Element methodID : coMethods) {
				doc = new XmlDocMaker("args");
				doc.add("id", methodID.value());

				XmlDoc.Element methodDesc = executor().execute(METHOD_DESCRIBE, doc.root());

				XmlDoc.Element restrictValue = new XmlDoc.Element("value", methodDesc.value("method/@id"));
				restrictValue.add(new XmlDoc.Attribute("name", methodDesc.value("method/name")));
				restriction.add(restrictValue);
			}
			formField.add(restriction);
		}
		categoryElement.add(formField);

		// "data-use"
		addDataUseElement(categoryElement, true);

		return categoryElement;
	}

	public XmlDoc.Element createDomainProject() throws Throwable {
		XmlDoc.Element category = new XmlDoc.Element("category");
		category.add(new XmlDoc.Attribute("type", "domain.metadata"));
		createDomainMetadata(category, PROJECT);

		return category;
	}

	/* create subject metadata information */
	public void getCreateSubjectMetadata(XmlWriter w, String method, String parentCID) throws Throwable {
		XmlDoc.Element metadataElement = new XmlDoc.Element("metadata");
		XmlDoc.Element formElement = new XmlDoc.Element("form");

		XmlDoc.Element serviceElement = new XmlDoc.Element("service");
		XmlDoc.Element serviceTitle = new XmlDoc.Element("title", CREATE_SUBJECT);
		XmlDoc.Element serviceName = new XmlDoc.Element("name", CREATE_SUBJECT_METHOD);
		serviceElement.add(serviceTitle);
		serviceElement.add(serviceName);

		/* creating the metadata needed by the om.pssd.subject.create service */
		XmlDoc.Element interfaceElement = createInterfaceSubjectPSSD(parentCID, method);

		/* creating the generic domain specific metadata */
		XmlDoc.Element domainElement = createDomainSubject();

		/* creating the method metadata */
		XmlDoc.Element methodElement = createMethodSubject(method);

		/* adding everthing into the final writer */
		metadataElement.add(serviceElement);
		formElement.add(interfaceElement);
		if (domainElement != null)
			formElement.add(domainElement);
		if (methodElement != null)
			formElement.add(methodElement);

		metadataElement.add(formElement);

		w.add(metadataElement);
		return;
	}

	/*
	 * public XmlDoc.Element createInterfaceSubjectPSSD(String parentCID, String method) throws Throwable {
	 * 
	 * // The name field XmlDoc.Element categoryElement = new XmlDoc.Element("category"); categoryElement.add(new
	 * XmlDoc.Attribute("type", "interface.metadata")); // XmlDoc.Attribute minOccurs = new
	 * XmlDoc.Attribute("min-occurs", 0); XmlDoc.Attribute maxOccurs = new XmlDoc.Attribute("max-occurs", 1); //
	 * XmlDoc.Element formField = new XmlDoc.Element("field"); formField.add(minOccurs); formField.add(maxOccurs); //
	 * XmlDoc.Element fieldType = new XmlDoc.Element("type", TEXTFIELD); XmlDoc.Element fieldName = new
	 * XmlDoc.Element("name", "name"); XmlDoc.Element fieldValue = new XmlDoc.Element("value");
	 * 
	 * formField.add(fieldType); formField.add(fieldName); categoryElement.add(formField);
	 * 
	 * // The description field formField = new XmlDoc.Element("field"); formField.add(minOccurs);
	 * formField.add(maxOccurs); // fieldType = new XmlDoc.Element("type", TEXTFIELD); fieldName = new
	 * XmlDoc.Element("name", "description");
	 * 
	 * formField.add(fieldType); formField.add(fieldName); categoryElement.add(formField);
	 * 
	 * // The method field formField = new XmlDoc.Element("field"); fieldType = new XmlDoc.Element("type", TEXTFIELD);
	 * fieldName = new XmlDoc.Element("name", "method"); fieldValue = new XmlDoc.Element("value", method);
	 * 
	 * formField.add(fieldType); formField.add(fieldName); formField.add(fieldValue); categoryElement.add(formField);
	 * 
	 * // The parent Project cid formField = new XmlDoc.Element("field"); fieldType = new XmlDoc.Element("type",
	 * TEXTFIELD); fieldName = new XmlDoc.Element("name", "pid"); fieldValue = new XmlDoc.Element("value", parentCID);
	 * 
	 * formField.add(fieldType); formField.add(fieldName); formField.add(fieldValue); categoryElement.add(formField);
	 * 
	 * // The data-use element addDataUseElement (categoryElement, false);
	 * 
	 * return categoryElement; }
	 */
	public XmlDoc.Element createInterfaceSubjectPSSD(String parentCID, String method) throws Throwable {

		// The name field
		XmlDoc.Element categoryElement = new XmlDoc.Element("category");
		categoryElement.add(new XmlDoc.Attribute("type", "interface.metadata"));
		//
		XmlDoc.Attribute minOccurs = new XmlDoc.Attribute("min-occurs", 0);
		XmlDoc.Attribute maxOccurs = new XmlDoc.Attribute("max-occurs", 1);

		// Name
		XmlDoc.Element formField = new XmlDoc.Element("field");
		formField.add(minOccurs);
		formField.add(maxOccurs);
		formField.add(new XmlDoc.Element("type", TEXTFIELD));
		formField.add(new XmlDoc.Element("name", "name"));
		formField.add(new XmlDoc.Element("description", "The name of the Subject"));
		categoryElement.add(formField);

		// The description field
		formField = new XmlDoc.Element("field");
		formField.add(minOccurs);
		formField.add(maxOccurs);
		//
		formField.add(new XmlDoc.Element("type", TEXTFIELD));
		formField.add(new XmlDoc.Element("name", "description"));
		formField.add(new XmlDoc.Element("description", "A description of the subject"));
		categoryElement.add(formField);

		// The method field
		formField = new XmlDoc.Element("field");
		formField.add(new XmlDoc.Element("type", TEXTFIELD));
		formField.add(new XmlDoc.Element("name", "method"));
		formField.add(new XmlDoc.Element("value", method));
		formField.add(new XmlDoc.Element("description", "The Method to create the subject with"));
		categoryElement.add(formField);

		// The parent Project cid
		formField = new XmlDoc.Element("field");
		formField.add(new XmlDoc.Element("type", TEXTFIELD));
		formField.add(new XmlDoc.Element("name", "pid"));
		formField.add(new XmlDoc.Element("value", parentCID));
		formField.add(new XmlDoc.Element("description", "The parent Project object CID"));
		categoryElement.add(formField);

		// The data-use element
		addDataUseElement(categoryElement, false);

		return categoryElement;
	}

	public XmlDoc.Element createDomainSubject() throws Throwable {
		XmlDoc.Element category = new XmlDoc.Element("category");
		category.add(new XmlDoc.Attribute("type", "domain.metadata"));
		createDomainMetadata(category, SUBJECT);

		return category;
	}

	public XmlDoc.Element createMethodSubject(String method) throws Throwable {
		XmlDoc.Element category = new XmlDoc.Element("category");
		category.add(new XmlDoc.Attribute("type", "method.metadata"));

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", method);
		XmlDoc.Element method_describe = executor().execute(METHOD_SUBJECT_DESCRIBE, doc.root());

		if (method_describe != null && !method_describe.toString().equals("</result>")) {
			TemplateUtil.replaceDictionaries(executor(), method_describe);
			category.add(method_describe.element("method"));
		}
		return category;
	}

	/* create study metadata information */
	public void getCreateStudyMetadata(XmlWriter w, String parentCID, String stepCID) throws Throwable {

		XmlDoc.Element metadataElement = new XmlDoc.Element("metadata");
		XmlDoc.Element formElement = new XmlDoc.Element("form");

		XmlDoc.Element serviceElement = new XmlDoc.Element("service");
		XmlDoc.Element serviceTitle = new XmlDoc.Element("title", CREATE_STUDY);
		XmlDoc.Element serviceName = new XmlDoc.Element("name", CREATE_STUDY_METHOD);
		serviceElement.add(serviceTitle);
		serviceElement.add(serviceName);

		/* creating the metadata required by the om.pssd.study.create service interface */
		XmlDoc.Element interfaceElement = createInterfaceStudy(parentCID, stepCID);

		/* creating the generic domain specific metadata */
		XmlDoc.Element domainElement = createDomainStudy();

		/* creating the method-specific metadata */
		XmlDoc.Element methodElement = createMethodStudy(parentCID, stepCID);

		/* adding everthing into the final writer */
		metadataElement.add(serviceElement);
		formElement.add(interfaceElement);
		if (domainElement != null)
			formElement.add(domainElement);
		if (methodElement != null)
			formElement.add(methodElement);

		metadataElement.add(formElement);

		w.add(metadataElement);
		return;
	}

	public XmlDoc.Element createInterfaceStudy(String parentCID, String stepCID) throws Throwable {
		XmlDoc.Element categoryElement = new XmlDoc.Element("category");
		categoryElement.add(new XmlDoc.Attribute("type", "interface.metadata"));

		// Attributes
		XmlDoc.Attribute minOccurs0 = new XmlDoc.Attribute("min-occurs", 0);
		XmlDoc.Attribute minOccurs1 = new XmlDoc.Attribute("min-occurs", 1);
		XmlDoc.Attribute maxOccurs1 = new XmlDoc.Attribute("max-occurs", 1);

		// "name"
		XmlDoc.Element formField = new XmlDoc.Element("field");
		formField.add(minOccurs0);
		formField.add(maxOccurs1);
		XmlDoc.Element fieldType = new XmlDoc.Element("type", TEXTFIELD);
		XmlDoc.Element fieldName = new XmlDoc.Element("name", "name");
		XmlDoc.Element fieldValue = new XmlDoc.Element("value");
		formField.add(fieldType);
		formField.add(fieldName);
		categoryElement.add(formField);

		// "description"
		formField = new XmlDoc.Element("field");
		formField.add(minOccurs0);
		formField.add(maxOccurs1);
		fieldType = new XmlDoc.Element("type", TEXTFIELD);
		fieldName = new XmlDoc.Element("name", "description");
		formField.add(fieldType);
		formField.add(fieldName);
		categoryElement.add(formField);

		// The Study type will be supplied by the Method so don't include it here.
		// creating the type field, allowed values are from the specified dictionary
		/*
		 * formField = new XmlDoc.Element("field"); fieldType = new XmlDoc.Element("type", MENU); fieldName = new
		 * XmlDoc.Element("name", "type");
		 * 
		 * XmlDoc.Element restriction = new XmlDoc.Element("restriction"); Collection dictionaryValues =
		 * getDictionaryValues(STUDY_TYPE_DICTIONARY); if(dictionaryValues != null) { Iterator dictionaryValuesIt =
		 * dictionaryValues.iterator(); while(dictionaryValuesIt.hasNext()) {
		 * restriction.add((XmlDoc.Element)dictionaryValuesIt.next()); } }
		 * 
		 * formField.add(fieldType); formField.add(fieldName); formField.add(restriction);
		 * categoryElement.add(formField);
		 */

		// ExMethod cid
		formField = new XmlDoc.Element("field");
		formField.add(minOccurs1);
		formField.add(maxOccurs1);
		fieldType = new XmlDoc.Element("type", TEXTFIELD);
		fieldName = new XmlDoc.Element("name", "pid");
		fieldValue = new XmlDoc.Element("value", parentCID);

		formField.add(fieldType);
		formField.add(fieldName);
		formField.add(fieldValue);
		categoryElement.add(formField);

		return categoryElement;
	}

	public XmlDoc.Element createDomainStudy() throws Throwable {
		XmlDoc.Element category = new XmlDoc.Element("category");
		category.add(new XmlDoc.Attribute("type", "domain.metadata"));
		createDomainMetadata(category, STUDY);

		return category;
	}

	public XmlDoc.Element createMethodStudy(String parentCID, String stepCID) throws Throwable {
		XmlDoc.Element category = new XmlDoc.Element("category");
		category.add(new XmlDoc.Attribute("type", "method.metadata"));

		// Call PSSD method to describe this step
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("id", parentCID);
		doc.add("step", stepCID);
		XmlDoc.Element step = executor().execute("om.pssd.ex-method.step.describe", doc.root());
		if (step != null) {
			TemplateUtil.replaceDictionaries(executor(), step);
			category.add(step.element("ex-method"));
		}
		return category;
	}

	/**
	 * Pass in a pssd object type, and will get the domain metadata
	 * 
	 * @param pssdType
	 * @return
	 */
	private void createDomainMetadata(XmlDoc.Element parentNode, String pssdType) throws Throwable {
		XmlDocMaker doc = new XmlDocMaker("args");
		XmlDoc.Element metadata_describe = null;
		doc.add("type", pssdType);

		metadata_describe = executor().execute(METADATA_DESCRIBE, doc.root());

		if (metadata_describe != null && !metadata_describe.toString().equals("</result>")) {
			Vector<XmlDoc.Element> metaData = metadata_describe.elements("metadata");
			if (metaData != null) {
				for (XmlDoc.Element el: metaData) {
					TemplateUtil.replaceDictionaries(executor(), el);
					parentNode.add(el);
				}
			}
		}
		return;
	}

	/**
	 * Add the 'data-use' element. It is used in the interface category for vreating Projects and Subjects
	 * 
	 * @param categoryElement
	 * @throws Throwable
	 */
	private void addDataUseElement(XmlDoc.Element categoryElement, boolean mandatory) throws Throwable {
		int minOccurs = 0;
		if (mandatory)
			minOccurs = 1;
		XmlDoc.Element formField = new XmlDoc.Element("field");
		formField.add(new XmlDoc.Attribute("min-occurs", minOccurs));
		formField.add(new XmlDoc.Attribute("max-occurs", 1));
		XmlDoc.Element fieldType = new XmlDoc.Element("type", MENU);
		XmlDoc.Element fieldName = new XmlDoc.Element("name", "data-use");
		formField
				.add(new XmlDoc.Element(
						"description",
						"The data use specification: specific (use only for original intent), extended (use for related projects), unspecified (use for any research"));
		//
		XmlDoc.Element restriction = new XmlDoc.Element("restriction");
		restriction.add(new XmlDoc.Element("value", "specific"));
		restriction.add(new XmlDoc.Element("value", "extended"));
		restriction.add(new XmlDoc.Element("value", "unspecified"));
		//
		formField.add(fieldType);
		formField.add(fieldName);
		formField.add(restriction);
		categoryElement.add(formField);
	}

}
