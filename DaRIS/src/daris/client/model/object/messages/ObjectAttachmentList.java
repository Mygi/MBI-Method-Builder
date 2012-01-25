package daris.client.model.object.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Attachment;
import daris.client.model.object.DObject;

public class ObjectAttachmentList extends ObjectMessage<List<Attachment>> {

	private String _id;

	public ObjectAttachmentList(DObject o) {

		_id = o.id();
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		
		w.add("id", _id);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.attachment.list";
	}

	@Override
	protected List<Attachment> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> aes = xe.elements("attachment");
			if (aes != null) {
				if (!aes.isEmpty()) {
					List<Attachment> as = new Vector<Attachment>(aes.size());
					for (XmlElement ae : aes) {
						as.add(new Attachment(ae.value("@id"), ae
								.value("name"), ae.value("description"), ae.value("type"), ae
								.value("type/@ext"), ae.longValue("size", 0)));
					}
					return as;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "list of attachments";
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
