package daris.client.model.object.messages;

import java.util.List;
import java.util.Vector;

import daris.client.model.object.Attachment;
import daris.client.model.object.DObject;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ObjectAttachmentGet extends ObjectMessage<Boolean> {

	private String _id;
	private List<Attachment> _attachments;

	public ObjectAttachmentGet(DObject o) {

		_id = o.id();
		_attachments = null;
	}

	public ObjectAttachmentGet(DObject o, Attachment attachment) {

		_id = o.id();
		_attachments = new Vector<Attachment>(1);
		_attachments.add(attachment);
	}

	public ObjectAttachmentGet(DObject o, List<Attachment> attachments) {

		_id = o.id();
		_attachments = attachments;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		if (_attachments != null) {
			for (Attachment a : _attachments) {
				w.add("aid", a.assetId());
			}
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.attachment.get";
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

	@Override
	protected int numberOfOutputs() {

		return 1;
	}

	@Override
	protected void process(Boolean o, List<Output> outputs) {

		if (o) {
			if (outputs != null) {
				for (Output output : outputs) {
					String filename = _id;
					if (_attachments == null) {
						filename += "_attachments.zip";
					} else {
						if (_attachments.size() == 1) {
							Attachment a = _attachments.get(0);
							filename += "_" + a.name();
							if (a.extension() != null) {
								if (!filename.endsWith("." + a.extension())) {
									filename += "." + a.extension();
								}
							}
						} else {
							filename += "_attachments.zip";
						}
					}
					output.download(filename);
				}
			}
		}
	}

}
