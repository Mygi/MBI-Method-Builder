package nig.mf.plugin.pssd.ni;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.*;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import java.util.Vector;


/**
 * This plugin service returns  the meta-data attached to a specific node
 * It returns to a depth of 2.  E.g. if you request the meta-data for a Project,
 * you will get that plus all of the Subjects that sit below it.
 * 
 */

public class SvcMetadataDataGet extends PluginService{

	private Interface _defn;
	
	public SvcMetadataDataGet() {
		_defn = new Interface();
		// define interface here 
		Interface.Element me = new Interface.Element("cid",CiteableIdType.DEFAULT, "The identity of the pssd object.", 0, 1);
		_defn.add(me);
		Interface.Element argID = new Interface.Element("foredit",BooleanType.DEFAULT,"Retrieve node meta-data for edit (defaults to false)",0,1);
		_defn.add(argID);
	}
	
	public String name() {
		return "nig.pssd.metadata.data.get";
	}

	public String description() {
		return "Given the CID of a local node, this function returns the meta-data attached to that node";
	}

	public Interface definition() {
		return _defn;
	}
	
	public Access access() {
		return ACCESS_ACCESS;
	}

	/**
	 * Federation comments:
	 * If no parent CID is provided, this service will execute locally, but it will execute a 
	 * distributed service (om.pssd.collection.members)
	 * If the parent CID is provided, then the caller must execute this service in the server that
	 * hosts the CID.  This will execute a local (to that server) service (om.pssd.object.describe) 
	 * and then a distributed service (but still executed in the same server)
	 */
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
	    // Inputs and validate
		String cid = args.value("cid");
		Boolean forEdit = args.booleanValue("foredit",false);
 
		
		if(cid != null && !cid.equals(""))
		{
			// Get the model type for this object
			XmlDocMaker doc = new XmlDocMaker("args");
			doc.add("id", cid); 
			XmlDoc.Element obj_model = executor().execute("om.pssd.model.types.list", doc.root());

			// Get the description of this object
			// This is a local service in a Federation 
			doc.add("foredit", Boolean.toString(forEdit));
			XmlDoc.Element type_describe = executor().execute("om.pssd.object.describe", doc.root());
			String objectType = type_describe.value("object/@type");

			// Replace any r-subject element (holding the r-subject's CID) with the  actual description of r-subject
			XmlDoc.Element type_describe_obj = type_describe.element("object");    // reference :-)
			if(type_describe_obj.element("r-subject")!=null){

				// The user may not have permission to access the R-Subject
				// so catch the exception
				try {
					Boolean readOnly = true;
					String r_subject_cid = type_describe_obj.value("r-subject");

					XmlDocMaker doc1 = new XmlDocMaker("args");
					doc1.add("id", r_subject_cid);
					doc1.add("foredit", Boolean.toString(forEdit));
					XmlDoc.Element r_subject_obj = executor().execute("om.pssd.object.describe", doc1.root());

					// Now remove "r-subject" element from the reference
					type_describe_obj.remove(new XmlDoc.Element("r-subject",r_subject_cid));

					// Create new element and add contents of R-Subject description back into meta
					XmlDoc.Element r_subject_element = new XmlDoc.Element("r-subject");
					r_subject_element.addAll(r_subject_obj.element("object"));
					type_describe_obj.add(r_subject_element);
				} catch(Throwable t) {
					// Do nothing ; it's not an error
				}
			}


			// Create the output document into which all meta-data will be placed 
			XmlDoc.Element pssdObj = new XmlDoc.Element("pssd-object");

			// add the cid of the parent object 
			XmlDoc.Element cidElement = new XmlDoc.Element("cid", cid);
			pssdObj.add(cidElement);

			//  Object type for this parent specified by PSSD model
			XmlDoc.Element pssdModelTypes = new XmlDoc.Element("pssd-model-types-list");
			removeResultHeader(pssdModelTypes, obj_model, "type");
			XmlDoc.Attribute typeAttr = new XmlDoc.Attribute("type", objectType);
			pssdObj.add(typeAttr);
			//
			if(type_describe != null && !type_describe.toString().equals("</result>")) {
				//  elements for meta-data for this object 
				XmlDoc.Element pssdMeta = new XmlDoc.Element("pssd-meta");    

				//  element for meta-data for children (members) of this object  
				XmlDoc.Element pssdMember = new XmlDoc.Element("pssd-member");

				//  meta-data 
				getMetadataData(type_describe, pssdObj, pssdModelTypes, pssdMeta, pssdMember, cid, "object");
			}

			//  write it out as xml document
			w.add(pssdObj);
		} else {
			// Get values
			XmlDocMaker doc = new XmlDocMaker("args");
			XmlDoc.Element repo_describe = executor().execute("server.database.describe",doc.root());
			doc = new XmlDocMaker("args");
			// Don't confuse the result of this service with server.uuid
			XmlDoc.Element root = executor().execute("citeable.root.get",doc.root());
			XmlDoc.Element repoMeta = new XmlDoc.Element("repository-meta");
			repoMeta.add(root.element("cid"));
			//
			XmlDoc.Element repoObj = new XmlDoc.Element("repository-object");
			XmlDoc.Element pssdModelTypes = new XmlDoc.Element("pssd-model-types-list");
			XmlDoc.Element pssdMember = new XmlDoc.Element("pssd-member");
			//
			XmlDoc.Element repo_model = executor().execute("om.pssd.model.types.list", doc.root());
			removeResultHeader(pssdModelTypes, repo_model, "type");
			if(repo_describe != null && !repo_describe.toString().equals("</result>")) {
				getMetadataData(repo_describe, repoObj, pssdModelTypes, repoMeta, pssdMember, null, "database");
			}
			w.add(repoObj);
		}
	}
	
	/**
	 * Get the metadata of the pssd object and its children
	 * 
	 * @param type_describe
	 * @param pssdObj
	 * @param pssdMeta
	 * @param pssdMember
	 * @param cid
	 * @throws Throwable
	 */
	private void getMetadataData(XmlDoc.Element type_describe, XmlDoc.Element pssdObj,
			XmlDoc.Element pssdModelTypes, XmlDoc.Element pssdMeta, XmlDoc.Element pssdMember,
			String cid, String parentdoc) throws Throwable {

		pssdObj.add(pssdModelTypes);
		removeResultHeader(pssdMeta, type_describe, parentdoc);
		pssdObj.add(pssdMeta);
		buildCollectionMembers(pssdMember, cid);
		pssdObj.add(pssdMember);
	}

	
	/**
	 * This is to build the collection of members under this node
	 * @param pssdMember
	 * @param cid
	 * @throws Throwable
	 */
	private void buildCollectionMembers(XmlDoc.Element pssdMember, String cid) throws Throwable
	{
		XmlDoc.Element xmlMembers = null;
		XmlDocMaker doc = new XmlDocMaker("args");
		
		if(cid != null && !cid.equals("")) doc.add("id", cid);
		
        // Fetch the meta-data for all of the members (children) of the parent object 
		// This query is distributed in a federation
		xmlMembers = executor().execute("om.pssd.collection.members", doc.root());
		if(xmlMembers != null && !xmlMembers.toString().equals("</result>")) {
			removeResultHeader(pssdMember, xmlMembers, "object");
		}
		return;
	}
	
	/**
	 * This is to remove the top level result element
	 * @param parentdoc
	 * @param xmldoc
	 * @param nodeName
	 */
	private void removeResultHeader(XmlDoc.Element parentdoc, XmlDoc.Element xmldoc, String nodeName)
	{
		Vector xmlVec = new Vector();	
		try {
			xmlVec = xmldoc.elements(nodeName);
		} catch(Throwable t) {
			t.printStackTrace(System.out);
		}
	
		if(xmlVec != null)
		{
			int vecSize = xmlVec.size();
			for(int i = 0; i < vecSize; i++) {
				parentdoc.add((XmlDoc.Element) xmlVec.get(i));
			}
		}
		return;
	}
}


