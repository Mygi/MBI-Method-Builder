package daris.client.model.object.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.object.Attachment;
import daris.client.model.object.DObject;

public class ObjectAttach extends ObjectMessage<List<Attachment>> {

	private String _id;

	private List<LocalFile> _inputs;

	public ObjectAttach(DObject o, List<LocalFile> inputs) {

		_id = o.id();
		_inputs = inputs;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		for (LocalFile input : _inputs) {
			w.push("attachment");
			w.add("name", input.name());
			if (input.description() != null) {
				w.add("description", input.description());
			}
			w.pop();
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.attach";
	}

	@Override
	protected List<Attachment> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> aes = xe.elements("attachment");
			if (aes != null) {
				List<Attachment> as = new Vector<Attachment>(aes.size());
				for (XmlElement ae : aes) {
					as.add(new Attachment(ae.value("@id"), ae.value("@name"),
							null, null, null, -1));
				}
				if (!as.isEmpty()) {
					return as;
				}
			}
		}
		return null;
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
	protected List<LocalFile> inputs() {

		return _inputs;
	}

}
