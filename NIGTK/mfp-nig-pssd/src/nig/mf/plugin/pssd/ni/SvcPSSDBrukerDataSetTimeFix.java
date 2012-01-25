package nig.mf.plugin.pssd.ni;

import java.util.Collection;
import java.util.Vector;

import nig.mf.plugin.util.DateUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcPSSDBrukerDataSetTimeFix extends PluginService {
	private Interface _defn;

	public SvcPSSDBrukerDataSetTimeFix() {

		_defn = new Interface();
		_defn.add(new Element("id", CiteableIdType.DEFAULT,
				"The parent CID under which to look for Bruker DataSets.  Defaults to all DataSets", 0, 1));
	}

	public String name() {
		return "nig.pssd.bruker-dataset.time.fix";
	}

	public String description() {
		return "This one-shot service fixes hfi-bruker-series/acqTime from HH:MM:SS DD-MMM-YYYY to the standard  DD-MMM-YYYY HH:MM:SS";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String id = args.value("id");


		XmlDocMaker doc = new XmlDocMaker("args");
		String query = "xpath(hfi-bruker-series/acqTime) has value";
		if (id!=null) query += " and cid starts with '" + id + "'";
		doc.add("where", query);
		doc.add("pdist", 0);    // Force local
		doc.add("action", "get-cid");
		doc.add("size", "infinity");
		XmlDoc.Element r = executor().execute("asset.query", doc.root());
		//
		// Iterate and fix
		if (r!=null) {
			Collection<String> dataSets = r.values("cid");
			if (dataSets!=null) {
				for (String id2 : dataSets) {
					String id3 = fixDataSetTime (executor(), id2);	
					if (id3!=null) w.add("id", id3);
				}				
			}
		}
	}
	
	private String fixDataSetTime (ServiceExecutor executor, String id) throws Throwable {
		
		// Get meta
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("cid", id);
		XmlDoc.Element r = executor.execute("asset.get", doc.root());
		if (r==null) return null;
		
		// We must replace the whole document...
		XmlDoc.Element r2 = r.element("asset/meta/hfi-bruker-series");
		if (r2==null) return null;

		Vector<XmlDoc.Element> els = r2.elements();
		if (els==null) return null;

		for (XmlDoc.Element el : els) {
			if (el.name().equals("acqTime")) {
				String timeIn = el.value();
				if (timeIn==null) return null;
				try {
					String timeOut = DateUtil.convertDateString(timeIn,  
							                 "HH:mm:ss dd MMM yyyy", "dd-MMM-yyyy HH:mm:ss");
					el.setValue(timeOut);
				} catch (Exception e) {
					// Failure may mean the time has already been converted
					// as it's not in the right format. 
					return null;
				}
			}
		}	
		
		// Replace
		doc = new XmlDocMaker("args");
		doc.add("cid", id);
		doc.push("meta", new String[] {"action", "replace"});
		doc.add(r2);
		doc.pop();
        executor.execute("asset.set", doc.root());
        return id;
	}

	


}