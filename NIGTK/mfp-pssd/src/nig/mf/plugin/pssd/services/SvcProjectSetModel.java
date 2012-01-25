package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Project;
import nig.mf.pssd.plugin.util.DistributedAsset;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;


/**
 * Service to recursively set the data model in a project tree.  Currently only allowed to modify  local objects
 * as this is the current federation policy.  This could be relaxed, but the use case is from an archive
 * and local restore.
 * 
 * I think this is now fixed in archive/restore process.
 * 
 * @author nebk
 *
 */
public class SvcProjectSetModel extends PluginService {
	private Interface _defn;

	public SvcProjectSetModel() {

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT, "The  citable ID of the Project.",1,1));
	}

	public String name() {
		return "om.pssd.project.model.set";
	}

	public String description() {
		return "Recursively set the model of all local objects in a Project managed by the local server. Object that belong to the Project but are remote, will not be affected. This is used after the archive/restore process that discards the model.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Validate project CID. Can be primary or replica as this is a service function
		DistributedAsset dID = new DistributedAsset(null, args.value("id"));
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dID);
		if (type==null) {
			throw new Exception("The asset associated with " + dID.toString() + " does not exist");
		}
		if ( !type.equals(Project.TYPE) ) {
			throw new Exception("Object " + dID.getCiteableID() + " [type=" + type + "] is not a " + Project.TYPE);
		}

		// Set locally
		set (executor(), dID.getCiteableID());
	}
	
	private void set (ServiceExecutor executor, String cid) throws Throwable {
		
		//  Get the object
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		dm.add("pdist",0);                 // Force local 		
		XmlDoc.Element r = executor().execute("asset.get", dm.root());
		if (r==null) return;
		//
		String model = r.value("asset/model");
        String type = r.value("asset/meta/pssd-object/type");

        // Set the model
        if (model==null) {
        	setModel (executor, cid, type);
        }
        
        // Fetch local children only
        dm = new XmlDocMaker("args");
		dm.add("id", cid);
		dm.add("pdist", "0");       
		r = executor().execute("om.pssd.collection.member.list", dm.root());
		if (r==null) return;
		//
		Collection<String> children = r.values("object/id");
		if (children==null) return;

		// Iterate and set children
		for (String id : children) {
			set(executor, id);
		}
	
	}
	
	
	private void setModel (ServiceExecutor executor, String cid, String type) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		//
		if (type.equals("project")) {
			dm.add("model", "om.pssd.project");
		} else if (type.equals("subject")) {
			dm.add("model", "om.pssd.subject");
		} else if (type.equals("ex-method")) {
			dm.add("model", "om.pssd.ex-method");
		} else if (type.equals("study")) {
			dm.add("model", "om.pssd.study");
		} else if (type.equals("dataset")) {
			dm.add("model", "om.pssd.dataset");
		} else if (type.equals("dataobject")) {
			dm.add("model", "om.pssd.dataobject");
		} else {
			throw new Exception ("Object has unknown type : " + type);
		}
		// Set the model
		executor.execute("asset.set", dm.root());
	}


}