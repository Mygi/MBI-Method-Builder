package nig.mf.plugin.transcode;

import java.io.File;

import nig.io.FileUtils;
import nig.io.TempFileInputStream;
import nig.mf.plugin.util.AssetUtil;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.PluginService.Interface.Element;
import arc.mf.plugin.dtype.AssetType;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.mf.plugin.dtype.EnumType;
import arc.mime.NamedMimeType;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;

public class SvcTranscode extends PluginService {

	private Interface _defn;

	/**
	 * Constructor.
	 */
	public SvcTranscode() {

		_defn = new Interface();
		_defn.add(new Element("id", AssetType.DEFAULT, "The asset id.", 0, 1));
		_defn.add(new Element("cid", CiteableIdType.DEFAULT, "The asset citeable id.", 0, 1));
		_defn.add(new Element("to", new EnumType(Transcode.getMappingToMimeTypes()),
				"The mime type to convert/transcode to. " + " e.g. analyze/series/nl", 1, 1));

	}

	/**
	 * Returns the service name.
	 */
	public String name() {

		return "nig.transcode";

	}

	/**
	 * Returns the description about this service.
	 */
	public String description() {
		return "A service that transcode/covert image to a different format and output to external file system."
				+ " Note: This service is normally for testing purpose. "
				+ "To transcode/convert assets in Medialux system, you need to use asset.transcode service (transcode framework).";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		// Requires ACCESS. Because it does not write to an asset in Mediaflux system.
		// Instead, it outputs to an external file.
		return ACCESS_ACCESS;
	}

	public int minNumberOfOutputs() {
		return 1;
	}

	public int maxNumberOfOutputs() {
		return 1;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {

		String id = args.value("id");
		String cid = args.value("cid");
		if (id == null && cid == null) {
			throw new Exception("Asset id or citeable id is not specified.");
		}
		if (id != null && cid != null) {
			throw new Exception("Both asset id and citeable id are specified. Only need one of them.");
		}

		String toMime = args.value("to");

		XmlDocMaker doc = new XmlDocMaker("args");
		if (cid != null) {
			doc.add("cid", cid);
		}
		if (id != null) {
			doc.add("id", id);
		}
		XmlDoc.Element r = executor().execute("asset.get", doc.root(), null, null);
		if (cid == null) {
			cid = r.value("asset/cid");
		}
		if (id == null) {
			id = r.value("asset/@id");
		}
		if (r.element("asset/content") == null) {
			throw new Exception("No content found in asset(id=" + id + ", cid=" + cid + "). Nothing is done.");
		}
		String fromMime = r.value("asset/type");
		Transcode.Mapping m = Transcode.getMapping(fromMime, toMime);
		if (m == null) {
			throw new Exception("Could not find mapping from " + fromMime + " to " + toMime);
		}
		File dir = createTemporaryDirectory();
		File of = createTemporaryFile();
		NamedMimeType fromContentType = new NamedMimeType(r.stringValue("asset/content/type"));
		try {
			String mimeType = Transcode.transcode(AssetUtil.getContent(executor(), id, dir), fromContentType, m, of);
			out.output(0).setData(new TempFileInputStream(of), of.length(), mimeType);
		} finally {
			FileUtils.delete(dir);
		}

	}

}