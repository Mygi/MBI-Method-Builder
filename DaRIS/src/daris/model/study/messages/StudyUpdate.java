package daris.model.study.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.object.messages.ObjectUpdate;
import daris.model.study.Study;
import daris.model.study.StudyRef;

public class StudyUpdate extends ObjectUpdate {

	public StudyUpdate(StudyRef ref) {
		
		super(ref);
		
	}

	@Override
	protected String messageServiceName() {
		
		return "om.pssd.study.update";
		
	}

	@Override
	protected String objectTypeName() {
		
		return Study.TYPE_NAME;
	
	}
	
	@Override
	protected void messageServiceArgs(XmlWriter w) {

		super.messageServiceArgs(w);

		StudyRef s = (StudyRef) object();
		if (s.studyType() != null) {
			w.add("type", s.studyType());
		}
		if(s.methodId()!=null && s.methodStep()!=null){
			w.push("method");
			w.add("id", s.methodId());
			w.add("step", s.methodStep());
			w.pop();
		}
		if (s.metaToSave() != null) {
			w.add(s.metaToSave(), true);
		}
	}

}
