package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.*;
import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

public class SvcSubjectMethodFind extends PluginService {
	private Interface _defn;

	public SvcSubjectMethodFind() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the Subject.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//
		_defn.add(new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the Method, which the ExMethod is instantiating.", 1, 1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",0,1));
	}

	public String name() {
		return "om.pssd.subject.method.find";
	}

	public String description() {
		return "Finds the ExMethods for a Subject that are instances of the specified Method.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		DistributedAsset dSID = new DistributedAsset(args.element("id"));
		String sid = dSID.getCiteableID();
		
		String mid = args.value("method");
		String pdist = args.value("pdist");

		// Make sure the parent is a subject.  The Subject can be primary or replica. We don't care 
		// for this service
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(),dSID);
		if (type==null) {
			throw new Exception("The asset associated with " + dSID.toString() + " does not exist");
		}
		if ( !type.equals(Subject.TYPE) ) {
			throw new Exception("Object " + sid + " [type=" + type + "] is not a " + Subject.TYPE);
		}
		
		// Set up query. 
		String query = "cid starts with '" + sid + "' and xpath(pssd-ex-method/method/id)='" + mid + "'";
		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("action","get-cid");
		if (pdist!=null) dm.add("pdist",pdist); 	
		
		// Distributed in federation
		XmlDoc.Element r = executor().execute("asset.query",dm.root());
		Collection<String> ems = r.values("cid");
		if ( ems != null ) {
			for (String em : ems) {
				w.add("id",em);
			}
		}
	}
	
}
