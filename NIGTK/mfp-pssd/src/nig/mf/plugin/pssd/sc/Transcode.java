package nig.mf.plugin.pssd.sc;

import java.util.List;
import java.util.Vector;

import arc.xml.XmlDoc;

public class Transcode {

	public final String from;
	public final String to;

	public Transcode(String from, String to) {
		this.from = from;
		this.to = to;
	}

	public static Transcode instantiate(XmlDoc.Element te) throws Throwable {
		if (te != null) {
			String from = te.value("from");
			String to = te.value("to");
			if (from != null && to != null) {
				return new Transcode(from, to);
			}
		}
		return null;
	}

	public static List<Transcode> instantiate(List<XmlDoc.Element> tes)
			throws Throwable {
		if (tes != null) {
			if (!tes.isEmpty()) {
				List<Transcode> transcodes = new Vector<Transcode>();
				for (XmlDoc.Element te : tes) {
					Transcode transcode = instantiate(te);
					if (transcode != null) {
						transcodes.add(transcode);
					}
				}
				if (transcodes.size() > 0) {
					return transcodes;
				}
			}
		}
		return null;
	}
}
