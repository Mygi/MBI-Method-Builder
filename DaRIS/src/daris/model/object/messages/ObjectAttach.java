package daris.model.object.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.file.LocalFile;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.attachment.Attachment;

public class ObjectAttach extends ObjectMessage<Attachment> {

	private String _id;

	private LocalFile _input;

	public ObjectAttach(String id, LocalFile input) {
		_id = id;
		_input = input;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("id", _id);
		w.add("name", _input.name());
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.attach";
	}

	@Override
	protected Attachment instantiate(XmlElement xe) throws Throwable {
		
		if (xe != null) {
			XmlElement ae = xe.element("attachment");
			if (ae != null) {
				return new Attachment(ae.value("@id"), ae.value("@name"), null,
						null, -1);
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

		List<LocalFile> inputs = new Vector<LocalFile>(1);
		inputs.add(_input);
		return inputs;
	}

}
