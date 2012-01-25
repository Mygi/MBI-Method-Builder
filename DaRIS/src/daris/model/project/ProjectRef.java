package daris.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.object.ObjectMessageResponse;
import arc.mf.object.ObjectResolveHandler;
import daris.model.method.MethodRef;
import daris.model.object.PSSDObjectMeta;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;
import daris.model.project.messages.ProjectMetaDescribe;
import daris.model.project.messages.ProjectUpdate;
import daris.model.roleuser.RoleUser;
import daris.model.subject.SubjectRef;
import daris.model.subject.messages.SubjectCreate;
import daris.model.user.User;

public class ProjectRef extends PSSDObjectRef {

	private String _dataUse;

	private List<ProjectMember> _members;

	private List<MethodRef> _methods;

	private List<ProjectRoleMember> _roleMembers;

	public ProjectRef(XmlElement oe) {

		super(oe);
		parse(oe);
	}

	@Override
	protected void parse(XmlElement oe) {

		super.parse(oe);

		_dataUse = oe.value("data-use");

		_members = null;
		List<XmlElement> mes = oe.elements("member");
		if (mes != null) {
			_members = new Vector<ProjectMember>();
			for (XmlElement me : mes) {
				_members.add(new ProjectMember(new User(me), me.value("@role"),
						me.value("@data-use")));
			}
		}

		_roleMembers = null;
		List<XmlElement> rmes = oe.elements("role-member");
		if (rmes != null) {
			_roleMembers = new Vector<ProjectRoleMember>();
			for (XmlElement rme : rmes) {
				_roleMembers.add(new ProjectRoleMember(new RoleUser(rme), rme
						.value("@role"), rme.value("@data-use")));
			}
		}

		_methods = null;
		List<XmlElement> mdes = oe.elements("method");
		if (mdes != null) {
			addMethods(mdes);
		}
	}

	public ProjectRef() {

		this(null, null, null, null, false);
	}

	public ProjectRef(String proute, String id) {

		this(proute, id, null, null, false);
	}

	public ProjectRef(String proute, String id, String name,
			String description, boolean isleaf) {

		super(proute, id, name, description, isleaf);

	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef subject) {

		return new SubjectCreate(this, (SubjectRef) subject);

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		return new ProjectUpdate(this);

	}

	@Override
	public String referentTypeName() {

		return "project";
	}

	public List<MethodRef> methods() {

		return _methods;

	}

	public void setMethods(List<MethodRef> methods) {

		_methods = methods;
	}

	public boolean hasMethods() {

		if (methods() != null) {
			if (!methods().isEmpty()) {
				return true;
			}
		}
		return false;
	}

	public List<ProjectMember> members() {

		return _members;
	}

	public void setMembers(List<ProjectMember> members) {

		_members = members;
	}

	public List<ProjectRoleMember> roleMembers() {

		return _roleMembers;
	}

	public void setRoleMembers(List<ProjectRoleMember> roleMembers) {

		_roleMembers = roleMembers;
	}

	public String dataUse() {

		return _dataUse;
	}

	public void setDataUse(String dataUse) {

		_dataUse = dataUse;
	}

	// public void addMember(Member m) {
	//
	// if (_members == null) {
	// _members = new HashMap<User, Member>();
	// }
	// _members.put(m.user(), m);
	//
	// }
	//
	// public void removeMember(User u) {
	//
	// if (_members != null) {
	// _members.remove(u);
	// }
	// }
	//
	// public void removeMember(Member m) {
	//
	// if (_members != null) {
	// _members.remove(m.user());
	// }
	// }

	// private void addMembers(List<XmlElement> mes) {
	//
	// if (mes != null) {
	// for (int i = 0; i < mes.size(); i++) {
	// XmlElement me = mes.get(i);
	// User u = new User(me);
	// Member m = new Member(u, me.value("@role"),
	// me.value("@data-use"));
	// addMember(m);
	// }
	// }
	//
	// }

	// public Project.Member member(User user) {
	//
	// if (_members != null) {
	// return _members.get(user);
	// }
	// return null;
	// }
	//
	// public boolean isMember(User user) {
	//
	// if (member(user) != null) {
	// return true;
	// }
	// return false;
	// }
	//
	// public void setMembers(List<Member> members) {
	//
	// if (_members == null) {
	// _members = new HashMap<User, Member>();
	// } else {
	// _members.clear();
	// }
	// for (Member m : members) {
	// _members.put(m.user(), m);
	// }
	// }
	//
	// public Collection<RoleMember> roleMembers() {
	//
	// if (_roleMembers == null) {
	// return null;
	// }
	// Collection<RoleMember> rms = _roleMembers.values();
	// if (rms != null) {
	// if (!rms.isEmpty()) {
	// return rms;
	// }
	// }
	// return null;
	//
	// }
	//
	// public void addRoleMember(RoleMember rm) {
	//
	// if (_roleMembers == null) {
	// _roleMembers = new HashMap<RoleUser, RoleMember>();
	// }
	// _roleMembers.put(rm.roleUser(), rm);
	//
	// }

	// public void removeRoleMember(String rm) {
	//
	// if (_roleMembers != null) {
	// _roleMembers.remove(rm);
	// }
	// }

	// public Project.RoleMember roleMember(RoleUser roleUser) {
	//
	// if (_roleMembers != null) {
	// return _roleMembers.get(roleUser);
	// }
	// return null;
	// }
	//
	// public boolean isRoleMember(RoleUser roleUser) {
	//
	// if (roleMember(roleUser) != null) {
	// return true;
	// }
	// return false;
	// }

	// public void removeRoleMember(Project.RoleMember rm) {
	//
	// if (_roleMembers != null) {
	// _roleMembers.remove(rm.roleUser());
	// }
	// }
	//
	// private void addRoleMembers(List<XmlElement> rmes) {
	//
	// if (rmes != null) {
	// for (int i = 0; i < rmes.size(); i++) {
	// XmlElement rme = rmes.get(i);
	// RoleUser ru = new RoleUser(rme.value("@id"),
	// rme.value("@member"));
	// RoleMember rm = new RoleMember(ru, rme.value("@role"),
	// rme.value("@data-use"));
	// addRoleMember(rm);
	// }
	// }
	//
	// }
	//
	// public void setRoleMembers(List<RoleMember> roleMembers) {
	//
	// if (_roleMembers == null) {
	// _roleMembers = new HashMap<RoleUser, RoleMember>();
	// } else {
	// _roleMembers.clear();
	// }
	// for (RoleMember rm : roleMembers) {
	// _roleMembers.put(rm.roleUser(), rm);
	// }
	// }

	public void addMethod(MethodRef m) {

		if (_methods == null) {
			_methods = new Vector<MethodRef>();
		}
		_methods.add(m);
	}

	private void addMethod(XmlElement mde) {

		try {
			addMethod(new MethodRef(mde.value("id"), mde.value("name"),
					mde.value("description")));
		} catch (Throwable t) {
			throw new AssertionError(t.getMessage());
		}
	}

	private void addMethods(List<XmlElement> mdes) {

		if (mdes != null) {
			for (int i = 0; i < mdes.size(); i++) {
				XmlElement mde = mdes.get(i);
				addMethod(mde);
			}
		}

	}

	public void metaForCreate(final ObjectResolveHandler<XmlElement> rh) {

		new ProjectMetaDescribe()
				.send(new ObjectMessageResponse<PSSDObjectMeta>() {

					@Override
					public void responded(PSSDObjectMeta r) {

						if (r != null) {
							ProjectMeta meta = (ProjectMeta) r;
							XmlElement me = meta.getMeta();
							rh.resolved(me);
							return;
						}
						rh.resolved(null);
					}
				});
	}
}
