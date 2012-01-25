package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.pssd.*;


/**
 * This class does not appear to be properly implemented. It is not exposed in the interface.
 * No federation pass has been made
 * 
 * @author nebk
 *
 */
public class SvcStudiesPreCreate extends PluginService {
	private Interface _defn;

	public SvcStudiesPreCreate() throws Throwable {
		_defn = new Interface();
		_defn.add(new Interface.Element("sid",CiteableIdType.DEFAULT, "The identity of the Subject.", 1, 1));
		_defn.add(new Interface.Element("method",CiteableIdType.DEFAULT, "The identity of the method to be applied to the subject.", 1, 1));
		_defn.add(new Interface.Element("recurse",BooleanType.DEFAULT, "Create studies for all referenced methods? Defaults to false.", 0, 1));
	}

	public String name() {
		return "om.pssd.studies.precreate";
	}

	public String description() {
		return "Creates PSSD studies for a Subject based on a prescribed Method.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String sid = args.value("sid");
		String mid = args.value("method");
		
		// Make sure the parent is a subject..
		String type = nig.mf.pssd.plugin.util.PSSDUtil.typeOf(executor(),sid);
		if ( !type.equals(Subject.TYPE) ) {
			throw new Exception("Object " + sid + " [type=" + type + "] is not a " + Subject.TYPE);
		}
		
		
		// Get the project identifier..this is required for ACLs on the
		// study.
		String pid = nig.mf.pssd.CiteableIdUtil.getParentId(sid);

		/*
		Collection studies = studies(mid,args.booleanValue("recurse",false));
		if ( studies != null ) {
			for (Study s : studies) {
				
				String cid = CiteableIdUtil.generateCiteableID(executor(),sid);

				String name = s.type();
				String description = "Method [" + s.methodId() + "] " + s.methodName() + ", Step [" + s.step() + "] " + s.stepName();
				
				Study.create(executor(), cid, s.type(), name, description, s.methodId(), s.step(), null, pid);
			}
		}
		*/
	}
	
	/**
	 * Looks for studies in this method. Will recurse all referenced methods.
	 * 
	 * @param mid
	 * @return
	 * @throws Throwable
	 */
	
	/*
	private Collection studies(String mid,boolean recurse) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid",mid);
		dm.add("pdist",0);                 // Force local	
		
		Collection studies = null;
		
		XmlDoc.Element r = executor().execute("asset.get",dm.root());
		
		String mname = r.value("asset/meta/pssd-method/name");
		
		Collection ses = r.elements("asset/meta/pssd-method/step");
		if ( ses != null ) {
				
				int step = se.intValue("@id");
				String sname = se.value("name");
				
				Collection sses = se.values("study/type");
				if ( sses != null ) {
					for (String stype : sses) {
						if ( studies == null ) {
							studies = new Vector();
						}
						
						studies.add(new Study(stype,mid,mname,step,sname));
					}
				}

				if ( recurse ) {
					String smid = se.value("method");
					if ( smid != null ) {
						Collection mstudies = studies(smid,true);
						if ( mstudies != null ) {
							if ( studies == null ) {
								studies = mstudies;
							} else {
								studies.addAll(mstudies);
							}
						}
					}

					Collection bms = se.values("branch/method");
					if ( bms != null ) {
						for (String bmid : bms) {
							Collection mstudies = studies(bmid,true);
							if ( mstudies != null ) {
								if ( studies == null ) {
									studies = mstudies;
								} else {
									studies.addAll(mstudies);
								}
							}
						}
					}
				}
			}
		}

		return studies;
	}
	*/
	
}
