package nig.mf.plugin.pssd.services;

import nig.mf.plugin.util.AssetRegistry;
import java.net.InetAddress;

import nig.mf.plugin.pssd.dicom.LocalDicomAE;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcDICOMAERegList extends PluginService {
	private Interface _defn;

	public SvcDICOMAERegList() {

		_defn = new Interface();
		_defn.add(new Interface.Element("type", new EnumType(new String[] { "local", "remote", "all" }),
				"The type of aes to list. Defaults to all.", 0, 1));
	}

	public String name() {

		return "om.pssd.dicom.ae.list";
	}

	public String description() {

		return "Lists the contents of the DICOM Application Entity registry";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String type = args.stringValue("type", "all");
		boolean showLocal = type.equals("local") || type.equals("all");
		boolean showRemote = type.equals("remote") || type.equals("all");
		if (showLocal) {
			showLocalAE(w);
		}
		if (showRemote) {
			showRemoteAEList(w);
		}
	}

	private void showLocalAE(XmlWriter w) throws Throwable {

		String localAET = LocalDicomAE.getAETitle(executor());
		if (localAET!=null) {
			int localPort = LocalDicomAE.getPort(executor());
			if (localPort > 0) {
				w.push("ae", new String[]{"type","local"});
				w.add("aet",localAET);
				w.add("host",InetAddress.getLocalHost().getHostName());
				w.add("port", localPort);
				w.pop();
			}
		}
	}

	private void showRemoteAEList(XmlWriter w) throws Throwable {

		// Find the Registry. Return if none yet.
		String id = AssetRegistry.findRegistry(executor(), SvcDICOMAEAdd.REGISTRY_ASSET_NAME);
		if (id == null)
			return;

		// List
		String xpath = "asset/meta/" + SvcDICOMAEAdd.DOCTYPE + "/ae";
		AssetRegistry.list(executor(), id, xpath, w);
	}
}
