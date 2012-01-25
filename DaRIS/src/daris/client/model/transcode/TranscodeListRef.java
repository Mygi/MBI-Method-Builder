package daris.client.model.transcode;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class TranscodeListRef extends ObjectRef<List<Transcode>> {

	private String _from;
	private String _to;

	public TranscodeListRef(String from, String to) {
		_from = from;
		_to = to;
	}

	public TranscodeListRef(String from) {
		this(from, null);
	}
	
	public String from(){
		return _from;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		if (_from != null) {
			w.add("from", _from);
		}
		if (_to != null) {
			w.add("to", _to);
		}
	}

	@Override
	protected String resolveServiceName() {

		return "asset.transcode.describe";
	}

	@Override
	protected List<Transcode> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return Transcode.instantiate(xe.elements("transcode"));
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "transcode";
	}

	@Override
	public String idToString() {

		return "transcode";
	}
}
