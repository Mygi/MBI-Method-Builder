package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import nig.mf.plugin.pssd.util.PSSDUtils;
import arc.mf.plugin.*;
import arc.mf.plugin.atomic.AtomicOperation;
import arc.mf.plugin.dtype.*;
import arc.xml.*;

public class SvcRSubjectCreate extends PluginService {
	
	public static class TemplateDefinitions {
		public XmlDoc.Element identityTemplate;
		public XmlDoc.Element publicTemplate;
		public XmlDoc.Element privateTemplate;
	}

	private Interface _defn;

	public SvcRSubjectCreate() {
		_defn = new Interface();
		_defn.add(new Interface.Element("namespace",StringType.DEFAULT, "The namespace in which to create this subject. Defaults to 'pssd'.", 0, 1));
		_defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this subject.", 0, 1));
		_defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the subject.", 0, 1));
		_defn.add(new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the research method from which this subject's metadata is defined.", 1, 1));

		Interface.Element de = new Interface.Element("administration",XmlDocType.DEFAULT,"Administration access context for subject. If not specified, then explict grant to caller.",0,1);
		de.add(new Interface.Element("project",CiteableIdType.DEFAULT,"The project that has implicit access. If specified, all subject administrators in the project will be given administration rights for this subject.",0,1));
		_defn.add(de);
		
		Interface.Element me = new Interface.Element("identity",XmlDocType.DEFAULT,"Identity metadata - a list of asset documents.",1,1);
		me.setIgnoreDescendants(true);
		_defn.add(me);
		
		me = new Interface.Element("public",XmlDocType.DEFAULT,"Optional public metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);
		_defn.add(me);

		me = new Interface.Element("private",XmlDocType.DEFAULT,"Optional private (accessible to subject administrator only) metadata - a list of asset documents.",0,1);
		me.setIgnoreDescendants(true);
		_defn.add(me);

	}

	public String name() {
		return "om.pssd.r-subject.create";
	}

	public String description() {
		return "Creates a PSSD reusable subject on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public static final Object LOCK = new Object();
	
	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		String mid = args.value("method");
		
		// Make the service execution atomic, because there are quite a few
		// bits to be created.
		ServiceOp sop = new ServiceOp(args,subjectTemplate(executor(),mid));
		executor().execute(LOCK, sop);
		w.add("id",new String[] { "proute", peerCallRoute() },sop.cid());
	}
	
	public static TemplateDefinitions subjectTemplate(ServiceExecutor executor,String mid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",mid);
		
		dm.add("pdist",0);                 // Force local on whatever server it's executed		
		XmlDoc.Element r = executor.execute("asset.get",dm.root());
		
		TemplateDefinitions tds = new TemplateDefinitions();
		tds.identityTemplate = r.element("asset/meta/pssd-method-rsubject/identity");
		tds.publicTemplate = r.element("asset/meta/pssd-method-rsubject/public");
		tds.privateTemplate = r.element("asset/meta/pssd-method-rsubject/private");
		
		return tds;
	}
	

	public static class ServiceOp implements AtomicOperation {

		private XmlDoc.Element _args;
		private String         _cid;
		private TemplateDefinitions _tds;
		
		public ServiceOp(XmlDoc.Element args,TemplateDefinitions tds) {
			_args = args;
			_cid = null;
			_tds = tds;
		}
		
		public String cid() {
			return _cid;
		}
		
		public boolean execute(ServiceExecutor executor) throws Throwable {
			
			String proute = null;
			_cid = nig.mf.pssd.plugin.util.CiteableIdUtil.generateRSubjectID(executor, proute);
			
			createSubjectRoles(executor,_cid);
			
			// long id = createPrivateAsset(executor,_args,_cid);
			
			createSubjectAsset(executor,_args,_cid,_tds);
			return true;
		}
		
		private void createSubjectRoles(ServiceExecutor executor,String cid) throws Throwable {
			PSSDUtils.createRole(executor,RSubject.administratorRoleName(cid));
			PSSDUtils.createRole(executor,RSubject.stateRoleName(cid));
			PSSDUtils.createRole(executor,RSubject.guestRoleName(cid));
		}
		
		/*
		private long createPrivateAsset(ServiceExecutor executor,XmlDoc.Element args,String cid) throws Throwable {
			XmlDoc.Element meta = args.element("private");
			if ( meta == null ) {
				return -1;
			}
			
			XmlDocMaker dm = new XmlDocMaker("args");
			
			dm.add("namespace",args.stringValue("namespace","pssd"));
			dm.add("model",RSubject.MODEL);
			
			dm.push("meta");
			PSSDUtils.setObjectOptionalMeta(dm, meta,"pssd.private");
			dm.pop();
			
			PSSDUtils.addIdentityACLs(dm, cid);
			
			XmlDoc.Element r = executor.execute("asset.create",dm.root());
			return r.longValue("id");
		}
		*/
		
		private void createSubjectAsset(ServiceExecutor executor,XmlDoc.Element args, String cid,TemplateDefinitions tds) throws Throwable {
			XmlDocMaker dm = new XmlDocMaker("args");
			
			dm.add("cid",cid);
			dm.add("namespace",args.stringValue("namespace","pssd"));
			dm.add("model",RSubject.MODEL);
			
			// Template information..
			
			// Identity information is the minimal identification information
			// required to describe an r-subject when creating them.
			//
			if ( tds.identityTemplate != null ) {
				dm.push("template",new String[] { "ns", "pssd.identity" });
				dm.addAll(tds.identityTemplate.elements("metadata"));
				dm.pop();
			}
			
			if ( tds.publicTemplate != null ) {
				dm.push("template",new String[] { "ns", "pssd.public" });
				dm.addAll(tds.publicTemplate.elements("metadata"));
				dm.pop();
			}

			// Private information --
			// 
			// The identity information should be available to subject
			// administrators.
			//
			if ( tds.privateTemplate != null ) {
				dm.push("template",new String[] { "ns", "pssd.private" });
				dm.addAll(tds.privateTemplate.elements("metadata"));
				dm.pop();
			}

			// Combine the identity and public metadata..			
			XmlDoc.Element ie = args.element("identity");
			XmlDoc.Element pe = args.element("public");
			
			dm.push("meta");
			PSSDUtils.setObjectMeta(dm, RSubject.TYPE, args.value("name"), args.value("description"));
			
			if ( ie != null ) {
				PSSDUtils.setObjectOptionalMeta(dm, ie,"pssd.identity");
			}
			
			if ( pe != null ) {
				PSSDUtils.setObjectOptionalMeta(dm, pe,"pssd.public");
			}
			
			dm.pop();
			
			PSSDUtils.addRSubjectACLs(dm, cid);
			
			/*
			if ( id != -1 ) {
				dm.push("related");
				dm.add("to",new String[] { "relationship", "pssd-private" },id);
				dm.pop();
			}
			*/
			
			executor.execute("asset.create",dm.root());
			
			// Link administration with some project?
			String pid = _args.value("administration/project");
			if ( pid != null ) {
				SvcRSubjectAdminAdd.grantAccessToProject(executor, cid, pid);
			}
		}
	}
}
