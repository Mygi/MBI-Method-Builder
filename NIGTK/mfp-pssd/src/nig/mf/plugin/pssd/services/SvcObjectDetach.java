package nig.mf.plugin.pssd.services;

import java.util.Collection;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.BooleanType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectDetach extends PluginService {
	private Interface _defn;

	public SvcObjectDetach() {
		_defn = new Interface();
		_defn.add(new Interface.Element("id", CiteableIdType.DEFAULT,
				"The citeable identity of the object.", 1, 1));
		_defn.add(new Interface.Element("detach-all", BooleanType.DEFAULT,
				"Set to true to detach all attachments. Defaults to false", 0,
				1));
		_defn.add(new Interface.Element("aid", AssetType.DEFAULT,
				"The identity of the attachment asset to be detached.", 0,
				Integer.MAX_VALUE));
	}

	public String name() {
		return "om.pssd.object.detach";
	}

	public String description() {
		return "Removes and deletes an attachment from an object on the local server.";
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
		boolean detachAll = args.booleanValue("detach-all", false);
		Collection<String> aidsToDetach = args.values("aid");
		if (!detachAll && aidsToDetach == null) {
			throw new Exception(
					"Either aid argument or detach-all argument is required.");
		}

		Collection<String> aids = SvcObjectAttachmentGet.getAttachmentAssetIds(
				executor(), null, id);
		if (aids == null) {
			throw new Exception("No attachments found on object(id=" + id
					+ ").");
		}
		if (aidsToDetach == null && detachAll) {
			// No aid specified. detach all.
			aidsToDetach = aids;
		} else {
			// Check if the specified aid is valid.
			for (String aidToDetach : aidsToDetach) {
				if (!aids.contains(aidToDetach)) {
					throw new Exception(
							"Object(id="
									+ id
									+ ") does not have the specified attachement asset(asset_id="
									+ aidToDetach + ").");
				}
			}
		}

		detachAttachments(executor(), id, aidsToDetach);

	}

	public static void detachAttachments(ServiceExecutor executor, String id,
			Collection<String> aids) throws Throwable {

		for (String aid : aids) {
			removeAttachment(executor, id, aid);
			try {
				destroyAttachment(executor, aid);
			} catch (Throwable t) {
				SvcObjectAttach.addAttachment(executor, id, aid);
				throw t;
			}
		}
	}

	public static void removeAttachment(ServiceExecutor executor, String id,
			String aid) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.add("to", new String[] { "relationship", "attachment" }, aid);

		executor.execute("asset.relationship.remove", dm.root());
	}

	public static void destroyAttachment(ServiceExecutor executor, String aid)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", aid);

		executor.execute("asset.destroy", dm.root());
	}
}
