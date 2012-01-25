package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

public class SvcMethodDestroy extends PluginService {
	private Interface _defn;

	public SvcMethodDestroy() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT,"The identity of the method.  Must be managed by the local server.",1,1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance for a distributed query. Defaults to infinity in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",0,1));
	}

	public String name() {
		return "om.pssd.method.destroy";
	}

	public String description() {
		return "Destroys the identified Method.  Only destroys Methods managed by the local server and not in use by other objects. In a federation, all peers will be queried to see if the Method is in use. Can be forced to all peers, regarldess of session federation,  with pdist.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// May be primary or a replica.  
		DistributedAsset dID = new DistributedAsset(null, args.value("id"));
		
		// Peer distation
		String pdist = args.value("pdist");
		
		// Checks Primary Methods in primary objects and replica Methods in replica objects
		if ( Method.inUseByAnyMethod(executor(), false, dID, pdist)) {     // Methods
			throw new Method.ExInUseByMethod(dID.getCiteableID());
		}
		if ( Method.inUseByAnyMethod(executor(), true, dID, pdist)) {      // ExMethods
			throw new Method.ExInUseByExMethod(dID.getCiteableID());
		}
	
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",dID.getCiteableID());
		
		executor().execute("asset.destroy", dm.root());
	}
	
}
