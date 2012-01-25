package daris.model.exmethod.messages;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.exmethod.ExMethod;

public class ExMethodStudyTypeList extends ObjectMessage<List<String>> {

	private String _exMethodId;

	public ExMethodStudyTypeList(String exMethodId) {
		_exMethodId = exMethodId;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("id", _exMethodId);
	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.ex-method.study.type.list";
	}

	@Override
	protected List<String> instantiate(XmlElement xe) throws Throwable {
		return xe.values("type/name");
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
