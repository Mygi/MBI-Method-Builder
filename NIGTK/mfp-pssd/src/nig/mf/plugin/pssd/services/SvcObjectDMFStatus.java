package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.Exec;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.PluginService.Interface;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectDMFStatus extends PluginService {

	public static final String CMD_DMATTR = "dmattr";

	public static class State {
		/**
		 * REG    File not managed by DMF
		 */
		public static final String REG = "REG";
		/**
		 * MIG    Migrating file
		 */
		public static final String MIG = "MIG";
		/**
		 * DUL    Dual-state file
		 */	
		public static final String DUL = "DUL";
		/**
		 * OFL    Offline file
		 */
		public static final String OFL = "OFL";
		/**
		 * UNM    Unmigrating file
		 */
		public static final String UNM = "UNM";
		/**
		 * NMG    Nonmigratable file
		 */
		public static final String NMG = "NMG";
		/**
		 * PAR    Partial-state file
		 */
		public static final String PAR = "PAR";
		/**
		 * INV    DMF cannot determine the file's state
		 */
		public static final String INV = "INV";
	}
	
	private Interface _defn;

	public SvcObjectDMFStatus() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT, "The identity of the root object.", 1, 1));
		_defn.add(new Interface.Element("recursive", BooleanType.DEFAULT,
				"Set to true to check the object and all its descendants. Defaults to false.", 0, 1));
		_defn.add(new Interface.Element("where", StringType.DEFAULT, "When doing a recursive status test, this gives a query predicate that is appended (so you can use 'and/or' etc)  to that which selects objects of the given root citable ID", 0, 1));
	}

	public String name() {
		return "om.pssd.dmf.status";
	}

	public String description() {
		return "Check the specified object's content's DMF status if the content is on a DMF system.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		if (nig.mf.pssd.CiteableIdUtil.getIdDepth(id) < 
				nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH) {
			throw new Exception("Depth of citeable id must be at least " + 
					nig.mf.pssd.CiteableIdUtil.PROJECT_ID_DEPTH
					+ "(Project ID depth). ");
		}

		boolean recursive = args.booleanValue("recursive", false);
		String extraQuery = args.value("where");


		if (recursive) {
			showDMFStates(executor(), id, extraQuery, w);
		} else {
			showDMFState(executor(), id, w);
		}

	}
	
	private void showDMFStates(ServiceExecutor executor, String cid, String extraQuery, XmlWriter w) throws Throwable{
		int idx = 1;
		int size = 100;
		boolean complete = false;
		while (!complete) {
			XmlDocMaker doc = new XmlDocMaker("args");
			String query = "(cid starts with '" + cid + "' or cid = '" + cid + "') and asset has content";
			if (extraQuery!=null) query += " " + extraQuery;
			doc.add("where", query);
			doc.add("action", "get-cid");
			doc.add("size", size);
			doc.add("pdist", 0); // force to local server
			doc.add("idx", idx);
			XmlDoc.Element r = executor().execute("asset.query", doc.root(), null, null);
			Collection<String> ccids = r.values("cid");
			if(ccids!=null){
				for(String ccid : ccids){
					showDMFState(executor, ccid, w);
				}
			}
			if (r.value("cursor/total/@complete").equals("true")) {
				complete = true;
			}
			idx += size;
		}

	}

	private void showDMFState(ServiceExecutor executor, String cid, XmlWriter w) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute("asset.get", dm.root(), null, null);
		String url = r.value("asset/content/url");
		String size = r.value("asset/content/size");
		if (url != null) {
			if (url.startsWith("file:")) {
				String path = url.substring(5);
				String output = Exec.exec(CMD_DMATTR, path);
				if(output!=null){
					String [] attrs = new String[]{"id", cid, "path", path, "size", size};
					output=output.trim();
					String state = null;
					if(output.endsWith(State.REG)){						
						state =  State.REG;
					} else if(output.endsWith(State.MIG)){
						state =  State.MIG;
					} else if(output.endsWith(State.OFL)){
						state =  State.OFL;
					} else if(output.endsWith(State.UNM)){
						state =  State.UNM;
					} else if(output.endsWith(State.DUL)){
						state =  State.DUL;
					} else if(output.endsWith(State.INV)){
						state =  State.INV;
					} else if(output.endsWith(State.PAR)){
						state =  State.PAR;
					} else if(output.endsWith(State.NMG)){
						state =  State.NMG;
					}
					if(state!=null){
						w.add("state", attrs, state);
					}
				}
			}
		}
	}

}
