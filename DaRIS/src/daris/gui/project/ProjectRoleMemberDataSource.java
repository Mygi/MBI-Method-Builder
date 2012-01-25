package daris.gui.project;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.mf.object.ObjectResolveHandler;
import daris.model.project.ProjectRef;
import daris.model.project.ProjectRoleMember;
import daris.model.project.ProjectRoleMembersRef;
import daris.model.roleuser.RoleUser;
import daris.model.roleuser.RoleUsersRef;

public class ProjectRoleMemberDataSource implements DataSource<ProjectRoleMember> {

	private RoleUsersRef _roleUsers;

	private ProjectRoleMembersRef _roleMembers;

	private FormEditMode _mode;

	public ProjectRoleMemberDataSource(ProjectRef project, FormEditMode mode) {

		_roleMembers = project.id() != null ? new ProjectRoleMembersRef(project) : null;
		_mode = mode;
		_roleUsers = new RoleUsersRef(false);
	}

	@Override
	public boolean isRemote() {

		return true;
	}

	@Override
	public boolean supportCursor() {

		return false;
	}

	@Override
	public void load(final Filter f, final long start, final long end, final DataLoadHandler<ProjectRoleMember> lh) {

		if (_roleMembers == null) {
			if (_mode != FormEditMode.READ_ONLY) {
				_roleUsers.resolve(new ObjectResolveHandler<List<RoleUser>>() {

					@Override
					public void resolved(List<RoleUser> roleUsers) {

						if (roleUsers != null) {
							Collection<ProjectRoleMember> rms = toRoleMembers(roleUsers, null);
							if (rms != null) {
								doLoad(rms, f, start, end, lh);
								return;
							}
						} else {
							lh.loaded(0, 0, 0, null, null);
						}
					}
				});
			} else {
				lh.loaded(0, 0, 0, null, null);
			}
		} else {
			_roleMembers.resolve(new ObjectResolveHandler<List<ProjectRoleMember>>() {

				@Override
				public void resolved(final List<ProjectRoleMember> roleMembers) {

					if (_mode != FormEditMode.READ_ONLY) {
						_roleUsers.resolve(new ObjectResolveHandler<List<RoleUser>>() {

							@Override
							public void resolved(List<RoleUser> roleUsers) {

								if (roleUsers != null) {
									Collection<ProjectRoleMember> rms = toRoleMembers(roleUsers, roleMembers);
									if (rms != null) {
										doLoad(rms, f, start, end, lh);
										return;
									}
								} else {
									lh.loaded(0, 0, 0, null, null);
								}
							}
						});
					} else {
						if (roleMembers != null) {
							doLoad(roleMembers, f, start, end, lh);
							return;
						} else {
							lh.loaded(0, 0, 0, null, null);
						}
					}
				}
			});
		}
	}

	private void doLoad(Collection<ProjectRoleMember> roleMembers, Filter f, long start, long end,
			DataLoadHandler<ProjectRoleMember> lh) {

		List<ProjectRoleMember> rms = null;
		if (roleMembers != null) {
			if (roleMembers.size() > 0) {
				rms = new Vector<ProjectRoleMember>(roleMembers.size());
				for (ProjectRoleMember m : roleMembers) {
					rms.add(m);
				}
			}
		}

		if (f != null) {
			Vector<ProjectRoleMember> frms = new Vector<ProjectRoleMember>();
			for (ProjectRoleMember rm : rms) {
				if (f.matches(rm)) {
					frms.add(rm);
				}
			}
			rms = frms;
		}
		long total = rms.size();
		int start1 = (int) start;
		int end1 = (int) end;
		if (start1 > 0 || end1 < rms.size()) {
			if (start1 >= rms.size()) {
				rms = null;
			} else {
				if (end1 > rms.size()) {
					end1 = rms.size();
				}
				rms = rms.subList(start1, end1);
			}
		}
		lh.loaded(start1, end1, total, rms, DataLoadAction.REPLACE);
	}

	private Collection<ProjectRoleMember> toRoleMembers(List<RoleUser> roleUsers, List<ProjectRoleMember> roleMembers) {

		Map<RoleUser, ProjectRoleMember> map = new HashMap<RoleUser, ProjectRoleMember>();
		for (RoleUser roleUser : roleUsers) {
			map.put(roleUser, new ProjectRoleMember(roleUser, null, null));
		}

		if (roleMembers != null) {
			for (ProjectRoleMember rm : roleMembers) {
				map.put(rm.roleUser(), rm);
			}
		}

		if (map.size() > 0) {
			return map.values();
		}
		return null;
	}

}
