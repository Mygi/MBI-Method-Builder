package daris.model.subject.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.object.messages.ObjectCreate;
import daris.model.project.ProjectRef;
import daris.model.subject.Subject;
import daris.model.subject.SubjectRef;

public class SubjectCreate extends ObjectCreate {

	public SubjectCreate(ProjectRef parent, SubjectRef subject) {

		super(parent, subject);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.subject.create";

	}

	@Override
	protected String objectTypeName() {

		return Subject.TYPE_NAME;

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		super.messageServiceArgs(w);

		// Encryption has been removed from subject creation
		//w.add("encryption", false);

		w.add("pid", parent().id());

		SubjectRef s = (SubjectRef) object();
		if (s.dataUse() != null) {
			w.add("data-use", s.dataUse());
		}
		if (s.method() != null) {
			w.add("method", s.method().id());
		}
		if (s.publicMetaToSave() != null) {
			w.add(s.publicMetaToSave(), true);
		}
		if (s.privateMetaToSave() != null) {
			w.add(s.privateMetaToSave(), true);
		}
	}

}
