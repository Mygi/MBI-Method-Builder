package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.*;
import arc.mf.plugin.*;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.method.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

public class SvcStudyCreate extends PluginService {
	private Interface _defn;

	public SvcStudyCreate() throws Throwable {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("pid",CiteableIdType.DEFAULT, "The identity of the parent (ex-method).", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		//
		_defn.add(new Interface.Element(
				"study-number",
				IntegerType.POSITIVE_ONE,
				"Specifies the study number for the identifier. If not given, the next available study is created. If specified, then there cannot be any other asset/object with this citable ID assigned. Used for importing studies from another system.",
				0, 1));
		_defn.add(new Element("fillin", BooleanType.DEFAULT, "If the study-number is not given, fill in the Study allocator space (re-use allocated CIDs with no assets), otherwise create the next available CID at the end of the CID pool. Defaults to true; use with care in federated envionment.", 0, 1));
		_defn.add(new Interface.Element("step",CiteableIdType.DEFAULT,"The step within the method that resulted in this study.",0,1));

		addInterfaceDefn(_defn);
	}

	public static void addInterfaceDefn(Interface defn) throws Throwable {
		defn.add(new Interface.Element("type",new DictionaryEnumType(Study.TYPE_DICTIONARY), "The type of the study. If not specified, then method must be specified.", 0, 1));
		defn.add(new Interface.Element("name",StringType.DEFAULT, "The name of this study. If not specified, then method must be specified.", 0, 1));
		defn.add(new Interface.Element("description",StringType.DEFAULT, "An arbitrary description for the study.", 0, 1));
		
		Interface.Element me = new Element("meta",XmlDocType.DEFAULT,"Optional metadata - a list of asset documents. If the metadata belongs to a method, then it must have an 'ns' attribute which corresponds to the 'ExMethod CID_Step ID' (e.g. 1.1.1.1_1.1).",0,1);
		me.setIgnoreDescendants(true);

		defn.add(me);
	}
	
	public String name() {
		return "om.pssd.study.create";
	}

	public String description() {
		return "Creates a PSSD Study on the local server.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		
		// Distributed ID for parent ExMethod. It must be a primary or we are not allowed
		// to create children under it.
		DistributedAsset dEID = new DistributedAsset (args.element("pid"));
		
		// Validate
		String type =nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(), dEID);
		if (type==null) {
			throw new Exception("The asset associated with " + dEID.toString() + " does not exist");
		}
		if ( !type.equals(ExMethod.TYPE) ) {
			throw new Exception("Object " + dEID.getCiteableID() + " [type=" + type + "] is not an " + ExMethod.TYPE);
		}
		if (dEID.isReplica()) {
			throw new Exception ("The supplied parent ExMethod is a replica and this service cannot create its child");
		}
	
		// If the user does not give study-number,  we may want to fill in any holes in the allocator space 
        boolean fillIn = args.booleanValue("fillin", true);
		long studyNumber = args.longValue("study-number", -1);
		
		// Find the parent primary Project.  In the creation context we must find the Primary parent project
		Boolean readOnly = false;
		DistributedAsset dPID = dEID.getParentProject(readOnly);
		if (dPID==null) {
			throw new Exception ("Cannot find primary Project parent of the given ExMethod");
		}
			
		type = args.value("type");
		String name = args.value("name");
		String description = args.value("description");
		String step = args.value("step");
		
		/*
		String exMethod = dEID.getCiteableID();

		if ( name == null || type == null ) {
			if ( exMethod == null ) {
				throw new Exception("Must specify method if not specifying name and type.");
			}
			
			Study s = studyFor(exMethod,step);
			if ( s == null ) {
				throw new Exception("Method " + method + ", step " + step + " not found or has no associated study.");
			}

			if ( type == null ) {
				type = s.type();
			}
			
			if ( name == null ) {
				name = s.type();
			}
			
			if ( description == null ) {
				description = "Method [" + s.methodId() + "] " + s.methodName() + ", Step [" + s.step() + "] " + s.stepName();
			}
		}
		*/
		
		String cid = Study.create(executor(), dEID, studyNumber, type, name, description, step, args.element("meta"), dPID, fillIn);
		w.add("id",cid);
	}
	
	/**
	 * Create Study object from ExMethod CID and Step path
	 * 
	 * @param exMethod CID
	 * @param step path
	 * @return
	 * @throws Throwable
	 */
	private Study studyFor(String exMethod,String step) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",exMethod);		
		dm.add("pdist",0);                 // Force local 	
		XmlDoc.Element r = executor().execute("asset.get",dm.root());
		
		// nebk: I don't think this code is correct.  The input step is an expanded path; what is in
		// the ExMethod is just the steps per subMethod, so the result of this query will be null.
		XmlDoc.Element se = r.element("asset/meta/pssd-method/step[@id='" + step + "']");
		if ( se == null ) {
			return null;
		}

		String mname = r.value("asset/meta/pssd-method/name");
		
		String sname = se.value("name");
		String type  = se.value("study/type");
		if ( type == null ) {
			return null;
		}
		
		return new Study(type,exMethod,mname,step,sname);
	}
	
}
