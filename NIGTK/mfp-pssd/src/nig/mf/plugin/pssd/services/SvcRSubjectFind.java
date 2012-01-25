package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.RSubject;
import nig.mf.pssd.plugin.util.DistributedQuery.ResultAssetType;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.EnumType;
import arc.mf.plugin.dtype.IntegerType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;



public class SvcRSubjectFind extends PluginService {
	private Interface _defn;

	public SvcRSubjectFind() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("identity",XmlDocType.DEFAULT,"Identity metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);

		_defn.add(me);
		_defn.add(new Interface.Element("foredit",BooleanType.DEFAULT,"Indicates whether the object may be edited. If true, then a description of the structure of the data is returned. Defaults to 'false'.",0,1));
		// _defn.add(new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the research method from which this subject's metadata is defined. If specified, then the resultant data is merged/filtered with this structure.", 1, 1));
		_defn.add(new Interface.Element("asset-type", new EnumType(new String[] {"primary", "replica", "all"}), 
				"Specify type of asset to find. Defaults to all.", 0, 1));
		_defn.add(new Interface.Element("pdist",IntegerType.DEFAULT,"Specifies the peer distance for a distributed query. Defaults to all servers in a federated session.  Set to 0 for local only or infinity for all peers (regardless of whether session is federated or not).",0,1));
	}

	public String name() {
		return "om.pssd.r-subject.find";
	}

	public String description() {
		return "Attempts to find matching RSubjects based on the specified identity information. It does a distributed query in a federation.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		/*
		String mid = args.value("method");
		
		XmlDoc.Element mt = null;
		if ( mid != null ) {
			SvcRSubjectCreate.TemplateDefinitions tds = SvcRSubjectCreate.subjectTemplate(executor(), mid);
			mt = tds.identityTemplate;
		}
		*/
		
		XmlDoc.Element imeta = args.element("identity");	
		boolean forEdit = args.booleanValue("foredit",false);
		String pdist = args.value("pdist");
		String assetType = args.stringValue("asset-type", "all");
		
		
		//Distributed in federation
		ResultAssetType tp = ResultAssetType.instantiate(assetType);
		XmlDoc.Element r = RSubject.find(executor(), tp, pdist, imeta, forEdit);
		
		//String si = ( callerIsAPeer() ) ? peerCallRoute() : null;

		// The find over-rides role-based permissions so that the finder can 
		// find identities on pre-existing RSubjects.
		boolean showRSubjectIdentity = true;
		boolean showSubjectPrivate = false;
		SvcObjectFind.addPssdObjects(executor(),w, r, false, forEdit, showRSubjectIdentity, showSubjectPrivate);

	}
	
}
