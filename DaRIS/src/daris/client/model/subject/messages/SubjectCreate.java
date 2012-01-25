package daris.client.model.subject.messages;

import arc.mf.client.xml.XmlWriter;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.subject.Subject;

public class SubjectCreate extends DObjectCreate {

	public SubjectCreate(DObjectRef po, Subject o) {

		super(po, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.subject.create";
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		DObjectRef po = parentObject();
		if (po.proute() != null) {
			w.add("pid", new String[] { "proute", po.proute() }, po.id());
		} else {
			w.add("pid", po.id());
		}
		super.messageServiceArgs(w);

	}

}
