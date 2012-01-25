package daris.model.exmethod.messages;

import java.util.List;
import java.util.Vector;

import daris.model.exmethod.ExMethod;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ExMethodStudyStepFind extends ObjectMessage<List<String[]>> {

	private String _exMethodId;
	private String _type;

	public ExMethodStudyStepFind(String exMethodId, String type) {
		_exMethodId = exMethodId;
		_type = type;
	}

	public ExMethodStudyStepFind(String exMethodId) {
		this(exMethodId, null);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		if (_type != null) {
			w.add("type", _type);
		}
		w.add("id", _exMethodId);
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.ex-method.study.step.find";
	}

	@Override
	protected List<String[]> instantiate(XmlElement xe) throws Throwable {
		if(xe!=null){
			List<XmlElement> ses = xe.elements("ex-method/step");
			if(ses!=null){
				List<String[]> steps = new Vector<String[]>();
				for(XmlElement se :ses){
					steps.add(new String[]{se.value("@type"), se.value()});
				}
				if(steps.size()>0){
					return steps;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return ExMethod.TYPE_NAME;
	}

	@Override
	protected String idToString() {
		return _exMethodId;
	}

}
