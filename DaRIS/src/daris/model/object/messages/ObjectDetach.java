package daris.model.object.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;
import daris.model.object.attachment.Attachment;

public class ObjectDetach extends ObjectMessage<Boolean> {

	private String _id;
	private boolean _detachAll = false;
	private List<String> _aids = null;

	public ObjectDetach(List<Attachment> attachments, PSSDObjectRef o) {
		_id = o.id();
		_aids = new Vector<String>(attachments.size());
		for (Attachment a : attachments) {
			_aids.add(a.assetId());
		}
		_detachAll = false;
	}

	public ObjectDetach(PSSDObjectRef o, boolean detachAll) {
		this(o.id(), detachAll);
		assert detachAll == true;
	}

	public ObjectDetach(PSSDObjectRef o, List<String> aids) {
		this(o.id(), aids);
	}

	public ObjectDetach(String id, List<String> aids) {
		_id = id;
		_aids = aids;
		_detachAll = false;
	}

	public ObjectDetach(String id, boolean detachAll) {
		assert detachAll == true;
		_id = id;
		_aids = null;
		_detachAll = detachAll;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("id", _id);
		if (_detachAll) {
			w.add("detach-all", true);
		} else {
			for (String aid : _aids) {
				w.add("aid", aid);
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.detach";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "attachment";
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
