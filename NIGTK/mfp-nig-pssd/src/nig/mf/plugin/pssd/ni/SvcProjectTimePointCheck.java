package nig.mf.plugin.pssd.ni;

import java.util.Collection;
import nig.mf.pssd.plugin.util.PSSDUtil;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

/**
 * Specialized function to check that Studies have self consistent time-point meta-data
 * 
 * @author nebk
 * 
 */
public class SvcProjectTimePointCheck extends PluginService {
	private Interface _defn;
	

	public SvcProjectTimePointCheck() {

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT, "The citeable asset id of the Project to check.", 1, 1));
	}

	public String name() {
		return "nig.pssd.project.time-point.check";
	}

	public String description() {
		return "Service to check Studies have self-consistent time-point meta-data. Lists only the studies that are inconsistent.  Looks at template, meta/pssd-study and meta/hfi.pssd.time-point";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ADMINISTER;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


		// Get project IDs and validate
		String pid = args.value("id");	
		PSSDUtil.isValidProject(executor(), pid, true);
		
		// Find all the Studies. Use the asset.get layer as we want to see the :template meta-data
		String query = "cid starts with '" + pid  + "' and model='om.pssd.study'";
		XmlDocMaker dm = new XmlDocMaker ("args");
		dm.add("where", query);
		dm.add("pdist", 0);
		XmlDoc.Element r = executor().execute("asset.query", dm.root());
		
		// Iterate over studies and check
		Collection<String> studies = r.values("id");
		if (studies != null) {
			for (String id : studies) {		
				checkStudy (id, w);
			}
		}
		
	}
	
	/**
	 * 
	 * @param id
	 * @param w
	 * @return true means ok
	 * @throws Throwable
	 */
	private void checkStudy (String id, XmlWriter w) throws Throwable {
		
		// Get Study
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0);
		XmlDoc.Element r = executor().execute("asset.get", dm.root());
		
		// Get items of interest
		String cid = r.value("asset/cid");
		XmlDoc.Element template = r.element("asset/template");
		XmlDoc.Element pssdStudy = r.element("asset/meta/pssd-study");
		XmlDoc.Element pssdTimePoint = r.element("asset/meta/hfi.pssd.time-point");
		
		// Compare
		String method = pssdStudy.value("method");
		String step = pssdStudy.value("method/@step");
		//
		boolean ok = true;
		if (template!=null) {
			String tNS = template.value("@ns");
			String[] s = tNS.split("_");
			String method2 = s[0];
			String step2 = s[1];
			if (!method.equals(method2) || !step.equals(step2)) ok = false;
		}
		//
		if (pssdTimePoint!=null) {
			String tpNS = pssdTimePoint.value("@ns");
			String[] s = tpNS.split("_");
			String method2 = s[0];
			String step2 = s[1];
			if (!method.equals(method2) || !step.equals(step2)) ok = false;
		}
		
		if (!ok) {
			w.add("id", cid);
		}
	}

}
