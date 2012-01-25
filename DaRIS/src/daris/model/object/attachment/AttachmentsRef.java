package daris.model.object.attachment;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.model.object.PSSDObjectRef;

public class AttachmentsRef extends ObjectRef<List<Attachment>> {

	private String _id;

	public AttachmentsRef(PSSDObjectRef o) {

		this(o.id());
	}

	public AttachmentsRef(String id) {

		_id = id;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("id", _id);
	}

	@Override
	protected String resolveServiceName() {

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
								.value("@name"), ae.value("type"), ae
								.value("type/@ext"), ae.longValue("size", 0)));
					}
					return as;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "attachments";
	}

	@Override
	public String idToString() {

		return _id;
	}

}
