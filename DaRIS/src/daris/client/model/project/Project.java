package daris.client.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.client.util.ActionListener;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.method.MethodRef;
import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.project.messages.ProjectCreate;
import daris.client.model.project.messages.ProjectMemberReplace;
import daris.client.model.project.messages.ProjectMetadataDescribe;
import daris.client.model.project.messages.ProjectRoleMemberReplace;
import daris.client.model.project.messages.ProjectUpdate;

public class Project extends DObject {

	public static final String TYPE_NAME = "project";

	private DataUse _dataUse;
	private List<MethodRef> _methods;
	private List<ProjectMember> _members;
	private List<ProjectRoleMember> _roleMembers;

	public Project() {

		super(null, null, null, null, false, 0, false);
	}

	public Project(XmlElement oe) {

		super(oe);

		assert (Project.TYPE_NAME.equals(oe.value("@type")));
		/*
		 * data-use
		 */
		_dataUse = DataUse.parse(oe.value("data-use"));
		/*
		 * methods
		 */
		List<XmlElement> mthdes = oe.elements("method");
		if (mthdes != null) {
			if (!mthdes.isEmpty()) {
				_methods = new Vector<MethodRef>(mthdes.size());
				for (XmlElement mde : mthdes) {
					_methods.add(new MethodRef(mde.value("id"), mde.value("name"), mde.value("description")));
				}
			}
		}
		/*
		 * members
		 */
		List<XmlElement> mbes = oe.elements("member");
		if (mbes != null) {
			if (!mbes.isEmpty()) {
				_members = new Vector<ProjectMember>(mbes.size());
				for (XmlElement mbe : mbes) {
					_members.add(new ProjectMember(mbe));
				}
			}
		}
		/*
		 * role-members
		 */
		List<XmlElement> rmbes = oe.elements("role-member");
		if (rmbes != null) {
			if (!rmbes.isEmpty()) {
				_roleMembers = new Vector<ProjectRoleMember>(rmbes.size());
				for (XmlElement rmbe : rmbes) {
					_roleMembers.add(new ProjectRoleMember(rmbe));
				}
			}
		}
	}

	public List<MethodRef> methods() {

		return _methods;
	}

	public void setMethods(List<MethodRef> methods) {

		_methods = methods;
	}

	public boolean hasMethods() {

		if (_methods == null) {
			return false;
		}
		return !_methods.isEmpty();
	}

	public DataUse dataUse() {

		return _dataUse;
	}

	public void setDataUse(DataUse dataUse) {

		_dataUse = dataUse;
	}

	public List<ProjectMember> members() {

		return _members;
	}

	public void addMember(ProjectMember pm) {

		if (_members == null) {
			_members = new Vector<ProjectMember>();
		}
		int index = -1;
		for (int i = 0; i < _members.size(); i++) {
			ProjectMember member = _members.get(i);
			if (member.user().equals(pm.user())) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			_members.remove(index);
		}
		_members.add(pm);
	}

	public void removeMember(ProjectMember pm) {

		if (pm == null) {
			return;
		}
		if (_members != null) {
			ProjectMember rm = null;
			for (ProjectMember m : _members) {
				if (m.user().equals(pm.user())) {
					rm = m;
					break;
				}
			}
			if (rm != null) {
				_members.remove(rm);
			}
		}
	}

	public List<ProjectRoleMember> roleMembers() {

		return _roleMembers;
	}

	public void addRoleMember(ProjectRoleMember prm) {

		if (_roleMembers == null) {
			_roleMembers = new Vector<ProjectRoleMember>();
		}
		int index = -1;
		for (int i = 0; i < _roleMembers.size(); i++) {
			ProjectRoleMember rm = _roleMembers.get(i);
			if (rm.member().equals(prm.member())) {
				index = i;
				break;
			}
		}
		if (index != -1) {
			_roleMembers.remove(index);
		}
		_roleMembers.add(prm);
	}

	public void removeRoleMember(ProjectRoleMember prm) {

		if (prm == null) {
			return;
		}
		if (_roleMembers != null) {
			ProjectRoleMember rrm = null;
			for (ProjectRoleMember rm : _roleMembers) {
				if (rm.member().equals(prm.member())) {
					rrm = rm;
					break;
				}
			}
			if (rrm != null) {
				_roleMembers.remove(rrm);
			}
		}
	}

	@Override
	public String typeName() {

		return Project.TYPE_NAME;
	}

	@Override
	protected DObjectCreate objectCreateMessage(DObjectRef po) {

		return new ProjectCreate(this);
	}

	@Override
	public void createServiceArgs(XmlWriter w) {

		super.createServiceArgs(w);
		w.add("data-use", _dataUse);
		if (_methods != null) {
			for (MethodRef m : _methods) {
				w.push("method");
				w.add("id", m.id());
				w.pop();
			}
		}
		if (_members != null) {
			for (ProjectMember pm : _members) {
				w.push("member");
				if (pm.user().authority() != null) {
					if (pm.user().protocol() != null) {
						w.add("authority", new String[] { "protocol", pm.user().protocol() }, pm.user().authority());
					} else {
						w.add("authority", pm.user().authority());
					}
				}
				w.add("domain", pm.user().domain());
				w.add("user", pm.user().user());
				w.add("role", pm.role());
				if (pm.dataUse() != null) {
					w.add("data-use", pm.dataUse());
				}
				w.pop();
			}
		}
		if (_roleMembers != null) {
			for (ProjectRoleMember prm : _roleMembers) {
				w.push("role-member");
				w.add("member", prm.member().member());
				w.add("role", prm.role());
				if (prm.dataUse() != null) {
					w.add("data-use", prm.dataUse());
				}
				w.pop();
			}
		}
	}

	@Override
	protected DObjectUpdate objectUpdateMessage() {

		return new ProjectUpdate(this);
	}

	@Override
	public void updateServiceArgs(XmlWriter w) {

		super.updateServiceArgs(w);
		w.add("data-use", _dataUse);
		if (_methods != null) {
			for (MethodRef m : _methods) {
				w.push("method");
				w.add("id", m.id());
				w.pop();
			}
		}
		if (_members != null) {
			for (ProjectMember pm : _members) {
				w.push("member");
				if (pm.user().authority() != null) {
					if (pm.user().protocol() != null) {
						w.add("authority", new String[] { "protocol", pm.user().protocol() }, pm.user().authority());
					} else {
						w.add("authority", pm.user().authority());
					}
				}
				w.add("domain", pm.user().domain());
				w.add("user", pm.user().user());
				w.add("role", pm.role());
				if (pm.dataUse() != null) {
					w.add("data-use", pm.dataUse());
				}
				w.pop();
			}
		}
		if (_roleMembers != null) {
			for (ProjectRoleMember prm : _roleMembers) {
				w.push("role-member");
				w.add("member", prm.member().member());
				w.add("role", prm.role());
				if (prm.dataUse() != null) {
					w.add("data-use", prm.dataUse());
				}
				w.pop();
			}
		}
	}

	public boolean hasMembers() {

		if (_members != null) {
			return !_members.isEmpty();
		}
		return false;
	}

	public boolean hasRoleMembers() {

		if (_roleMembers != null) {
			return !_roleMembers.isEmpty();
		}
		return false;
	}

	public boolean hasMembersOrRoleMembers() {

		return hasMembers() || hasRoleMembers();
	}

	public boolean hasAdminMember() {

		if (hasMembers()) {
			for (ProjectMember pm : _members) {
				if (pm.role().equals(ProjectRole.PROJECT_ADMINISTRATOR)) {
					return true;
				}
			}
		}
		if (hasRoleMembers()) {
			for (ProjectRoleMember rm : _roleMembers) {
				if (rm.role().equals(ProjectRole.PROJECT_ADMINISTRATOR)) {
					return true;
				}
			}
		}
		return false;
	}

	public void commitMembers(ObjectMessageResponse<Boolean> rh) {

		new ProjectMemberReplace(this).send(rh);
	}

	public void commitRoleMembers(ObjectMessageResponse<Boolean> rh) {

		new ProjectRoleMemberReplace(this).send(rh);
	}

	/**
	 * Set the meta data definition of a project object (to be created).
	 * 
	 * @param o
	 *            the project to be created.
	 */
	public static void setMetaForEdit(final Project o, final ActionListener al) {

		new ProjectMetadataDescribe().send(new ObjectMessageResponse<XmlElement>() {

			@Override
			public void responded(XmlElement metaForEdit) {

				o.setMetaForEdit(metaForEdit);
				al.executed(true);
			}
		});
	}

}
