package daris.model.object.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.Output;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;
import daris.model.object.attachment.Attachment;

public class ObjectAttachmentGet extends ObjectMessage<Boolean> {

	private String _id;
	private List<Attachment> _attachments;

	public ObjectAttachmentGet(PSSDObjectRef o) {

		this(o.id());
	}

	public ObjectAttachmentGet(PSSDObjectRef o, Attachment attachment) {

		this(o.id(), attachment);
	}

	public ObjectAttachmentGet(PSSDObjectRef o, List<Attachment> attachments) {

		this(o.id(), attachments);
	}

	public ObjectAttachmentGet(String id) {

		_id = id;
		_attachments = null;
	}

	public ObjectAttachmentGet(String id, Attachment attachment) {

		_id = id;
		_attachments = new Vector<Attachment>(1);
		_attachments.add(attachment);
	}

	public ObjectAttachmentGet(String id, List<Attachment> attachments) {

		_id = id;
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
