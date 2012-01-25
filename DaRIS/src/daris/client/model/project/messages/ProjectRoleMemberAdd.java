package daris.client.model.project.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.project.Project;
import daris.client.model.project.ProjectRoleMember;

public class ProjectRoleMemberAdd extends ObjectMessage<Boolean>{

	private String _id;
	private ProjectRoleMember _roleMember;

	public ProjectRoleMemberAdd(String id,  ProjectRoleMember roleMember) {

		_id = id;
		_roleMember = roleMember;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("id", _id);
		w.push("role-member");
		w.add("member", _roleMember.member());
		w.add("role", _roleMember.role());
		if(_roleMember.dataUse()!=null){
			w.add("data-use", _roleMember.dataUse());
		}
		w.pop();
		
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.members.add";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;
	}

	@Override
	protected String objectTypeName() {

		return Project.TYPE_NAME;
	}

	@Override
	protected String idToString() {

		return _id;
	}


}
