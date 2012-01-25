package daris.client.model.object.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Attachment;
import daris.client.model.object.DObject;

public class ObjectDetach extends ObjectMessage<Boolean> {

	private String _id;
	private boolean _detachAll = false;
	private List<Attachment> _attachments = null;

	public ObjectDetach(DObject o, List<Attachment> attachments) {

		_id = o.id();
		_attachments = attachments;
		_detachAll = false;
	}

	public ObjectDetach(String id, boolean detachAll) {

		assert detachAll == true;
		_id = id;
		_attachments = null;
		_detachAll = detachAll;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		if (_detachAll) {
			w.add("detach-all", true);
		} else {
			for (Attachment attachment : _attachments) {
				w.add("aid", attachment.assetId());
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.detach";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return xe != null;
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
