package daris.client.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.IDUtil;
import daris.client.model.exmethod.ExMethod;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;
import daris.client.model.repository.Repository;
import daris.client.model.repository.RepositoryRef;
import daris.client.model.study.Study;
import daris.client.model.subject.Subject;

public class CanCreate extends ObjectMessage<Boolean> {

	private String _object;
	private String _projectId;

	public CanCreate(String type, String projectId) {

		_object = type;
		_projectId = projectId;
	}

	public CanCreate(DObject parent) {

		if (parent instanceof Repository) {
			_projectId = null;
			_object = Project.TYPE_NAME;
		} else if (parent instanceof Project) {
			_projectId = parent.id();
			_object = Subject.TYPE_NAME;
		} else if (parent instanceof ExMethod) {
			_projectId = IDUtil.getParentId(parent.id(), 2);
			_object = Study.TYPE_NAME;
		} else {
			throw new AssertionError("Only project, subject and study creation are currently supported");
		}
	}

	public CanCreate(DObjectRef parent) {

		if (parent instanceof RepositoryRef) {
			_projectId = null;
			_object = Project.TYPE_NAME;
		} else {
			String typeName = parent.referentTypeName();
			if (typeName.equals(Project.TYPE_NAME)) {
				_projectId = parent.id();
				_object = Subject.TYPE_NAME;
			} else if (typeName.equals(ExMethod.TYPE_NAME)) {
				_projectId = IDUtil.getParentId(parent.id(), 2);
				_object = Study.TYPE_NAME;
			} else {
				throw new AssertionError("Only project, subject and study creation are currently supported");
			}
		}
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("object", _object);
		if (_projectId != null) {
			w.add("pid", _projectId);
		}
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.user.cancreate";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
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

		return _projectId;
	}

}