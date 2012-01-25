package daris.model.user.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.PSSDObjectRef;
import daris.model.project.Project;
import daris.model.project.ProjectRef;
import daris.model.repository.RepositoryRootRef;
import daris.model.study.Study;
import daris.model.subject.Subject;

public class UserCanCreate extends ObjectMessage<Boolean> {

	private String _object;
	private String _pid;

	public UserCanCreate(PSSDObjectRef parent) {
		if (parent instanceof RepositoryRootRef) {
			_object = Project.TYPE_NAME;
			_pid = null;
		} else if (parent instanceof ProjectRef) {
			_object = Subject.TYPE_NAME;
			_pid = parent.id();
		} else if (parent instanceof ExMethodRef) {
			_object = Study.TYPE_NAME;
			_pid = IDUtil.getParentId(parent.id(), 2);
		} else {
			throw new AssertionError("Creating child object for "
					+ parent.referentTypeName() + " " + parent.id()
					+ " is not supported.");
		}
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("object", _object);
		if (_pid != null) {
			w.add("pid", _pid);
		}
	}

	@Override
	protected String messageServiceName() {
		
		return "om.pssd.user.cancreate";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {
		if(xe!=null){
			return xe.booleanValue("create", false);
		}
		return false;
	}

	@Override
	protected String objectTypeName() {
		return _object;
	}

	@Override
	protected String idToString() {
		return _pid;
	}

}
