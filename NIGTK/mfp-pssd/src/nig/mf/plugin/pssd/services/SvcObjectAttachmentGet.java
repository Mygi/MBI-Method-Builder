package nig.mf.plugin.pssd.services;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import nig.io.TempFileInputStream;
import nig.mf.MimeTypes;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcObjectAttachmentGet extends PluginService {
	private Interface _defn;

	public static final int BUFFER_SIZE = 2048;

	public SvcObjectAttachmentGet() {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",
				CiteableIdType.DEFAULT, "The identity of the PSSD object.", 1,
				1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.",
				0));
		_defn.add(me);
		me = new Interface.Element(
				"aid",
				AssetType.DEFAULT,
				"The identity of the attachment asset. if not specified, all the attachments to the object will be packaged into a zip archive.",
				0, Integer.MAX_VALUE);
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.object.attachment.get";
	}

	public String description() {
		return "Retrieves the data for a specified object attachment.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public int minNumberOfOutputs() {
		return 1;
	}

	public int maxNumberOfOutputs() {
		return 1;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String id = args.value("id");
		String proute = args.value("id/@proute");
		ServerRoute sroute = proute == null ? null : new ServerRoute(proute);
		Collection<String> aidsToGet = args.values("aid");

		Collection<String> aids = getAttachmentAssetIds(executor(), sroute, id);

		if (aids == null) {
			throw new Exception("Object(id=" + id
					+ ") does not have related attachement assets.");
		}

		if (aidsToGet == null) {
			// No aid specified. get them all.
			aidsToGet = aids;
		} else {
			// Check if the specified aid is valid.
			for (String aidToGet : aidsToGet) {
				if (!aids.contains(aidToGet)) {
					throw new Exception(
							"Object(id="
									+ id
									+ ") does not have the specified attachement asset(asset_id="
									+ aidToGet + ").");
				}
			}
			if(aidsToGet.size()==1){
				getAttachment(executor(), sroute, aidsToGet.iterator().next(), out);
				return;
			}
		}
		getAttachments(executor(), sroute, aidsToGet, out);
	}

	public static Collection<String> getAttachmentAssetIds(
			ServiceExecutor executor, ServerRoute sroute, String id)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		XmlDoc.Element r = executor.execute(sroute, "asset.get", dm.root());
		return r.values("asset/related[@type='attachment']/to");
	}

	public static void getAttachment(ServiceExecutor executor,
			ServerRoute sroute, String aid, Outputs out) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", aid);
		dm.add("pdist", 0); // Force local on whatever server it's executed
		executor.execute(sroute, "asset.get", dm.root(), null, out);
	}

	public static void getAttachments(ServiceExecutor executor,
			ServerRoute sroute, Collection<String> aids, Outputs out)
			throws Throwable {
		
		File of = PluginService.createTemporaryFile();
		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(
				new FileOutputStream(of)));
		byte[] buffer = new byte[BUFFER_SIZE];
		for (String aid : aids) {
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("id", aid);
			PluginService.Outputs os = new PluginService.Outputs(1);
			XmlDoc.Element r = executor.execute(sroute, "asset.get", dm.root(),
					null, os);
			String entryName = aid;
			String name = r.stringValue("asset/meta/mf-name/name");
			if (name != null) {
				entryName += "_" + name;
			}
			String ext = r.stringValue("asset/content/type/@ext");
			if (ext != null) {
				if (!entryName.endsWith("." + ext)) {
					entryName += "." + ext;
				}
			}
			ZipEntry entry = new ZipEntry(entryName);
			zos.putNextEntry(entry);
			BufferedInputStream is = new BufferedInputStream(os.output(0)
					.stream());

			int count;
			while ((count = is.read(buffer, 0, BUFFER_SIZE)) != -1) {
				zos.write(buffer, 0, count);
			}
			is.close();
		}
		zos.close();
		out.output(0).setData(new TempFileInputStream(of), of.length(),
				MimeTypes.ZIP);
	}

}
