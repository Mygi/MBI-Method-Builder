package nig.mf.plugin.pssd.services;

import java.util.Collection;

import nig.mf.plugin.pssd.Asset;
import nig.mf.plugin.pssd.util.PSSDObjectUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.StringType;
import arc.mf.plugin.dtype.XmlDocType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectAttach extends PluginService {
	private Interface _defn;

	public SvcObjectAttach() {

		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The identity of the local object to attach to.", 1, 1));
		Interface.Element ae = new Interface.Element("attachment",
				XmlDocType.DEFAULT, "The attachement to attach", 1,
				Integer.MAX_VALUE);
		ae.add(new Interface.Element("name", StringType.DEFAULT,
				"The name of the attachment file.", 1, 1));
		ae.add(new Interface.Element("description", StringType.DEFAULT,
				"The description of the attachment file.", 0, 1));
		_defn.add(ae);

	}

	public String name() {

		return "om.pssd.object.attach";
	}

	public String description() {

		return "Attach local files as attachments to the specified PSSD object. The name and uri of the attachment file must be supplied.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public int minNumberOfInputs() {

		return 1;
	}

	public int maxNumberOfInputs() {

		return Integer.MAX_VALUE;
	}

	public void execute(XmlDoc.Element args, Inputs inputs, Outputs out,
			XmlWriter w) throws Throwable {

		String id = args.value("id");

		Collection<XmlDoc.Element> aes = args.elements("attachment");
		if (aes.size() != inputs.size()) {
			throw new Exception(
					"The number of file inputs("
							+ inputs.size()
							+ ") and the number of attachments("
							+ aes.size()
							+ ") do not matach. Each attachment must have an input file.");
		}

		String namespace = PSSDObjectUtil.namespaceOf(executor(), id);
		int i = 0;
		for (XmlDoc.Element ae : aes) {
			String name = ae.value("name");
			String description = ae.value("description");
			String assetId = createAttachmentAsset(executor(), namespace, id,
					name, description, inputs.input(i));
			w.add("attachment", new String[] { "id", assetId, "name", name });
			i++;
		}
	}

	private String createAttachmentAsset(ServiceExecutor executor, String ns,
			String id, String name, String description, Input input)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("namespace", ns);
		// dm.add("name", name);
		if (description != null) {
			dm.add("description", description);
		}
		dm.push("related");
		dm.add("from", new String[] { "relationship", "attachment" },
				Asset.getIdByCid(executor, id));
		dm.pop();
		Inputs inputs = new Inputs(1);
		inputs.add(input);
		XmlDoc.Element r = executor.execute("asset.create", dm.root(), inputs,
				null);
		String aid = r.value("id");
		if(aid==null){
			throw new Exception("Failed to create attachment asset.");
		}
		dm = new XmlDocMaker("args");
		// prepend asset id to the name to avoid name conflicts.
		dm.add("name", aid + "_" + name);
		dm.add("id", aid);
		executor.execute("asset.set", dm.root());
		return aid;
	}

	public static void addAttachment(ServiceExecutor executor, String id,
			String aid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("to", new String[] { "relationship", "attachment" }, aid);
		executor.execute("asset.relationship.add", dm.root());
	}

}
