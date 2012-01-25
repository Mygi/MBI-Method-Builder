package daris.model.subject.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.object.messages.ObjectUpdate;
import daris.model.subject.Subject;
import daris.model.subject.SubjectRef;

public class SubjectUpdate extends ObjectUpdate {

	public SubjectUpdate(SubjectRef ref) {
		
		super(ref);
		
	}

	@Override
	protected String messageServiceName() {
		
		return "om.pssd.subject.update";
	}

	@Override
	protected String objectTypeName() {

		return Subject.TYPE_NAME;

	}
	
	@Override
	protected void messageServiceArgs(XmlWriter w) {

		super.messageServiceArgs(w);

		SubjectRef s = (SubjectRef) object();
		if (s.dataUse() != null) {
			w.add("data-use", s.dataUse());
		}
		if (s.publicMetaToSave() != null) {
			w.add(s.publicMetaToSave(), true);
		}
		if (s.privateMetaToSave() != null) {
			w.add(s.privateMetaToSave(), true);
		}

	}

}
