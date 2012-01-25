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
import daris.model.project.ProjectMember;
import daris.model.project.ProjectMembersRef;
import daris.model.project.ProjectRef;
import daris.model.user.User;
import daris.model.user.UsersRef;

public class ProjectMemberDataSource implements DataSource<ProjectMember> {

	private UsersRef _users;

	private ProjectMembersRef _members;

	private FormEditMode _mode;

	public ProjectMemberDataSource(ProjectRef project, FormEditMode mode) {

		_members = project.id() != null ? new ProjectMembersRef(project, mode == FormEditMode.READ_ONLY ? false : true)
				: null;
		_mode = mode;
		_users = new UsersRef();
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
	public void load(final Filter f, final long start, final long end, final DataLoadHandler<ProjectMember> lh) {

		if (_members == null) {
			if (_mode != FormEditMode.READ_ONLY) {
				_users.resolve(new ObjectResolveHandler<List<User>>() {

					@Override
					public void resolved(List<User> users) {

						if (users != null) {
							Collection<ProjectMember> ms = toMembers(users, null);
							if (ms != null) {
								doLoad(ms, f, start, end, lh);
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
			_members.resolve(new ObjectResolveHandler<List<ProjectMember>>() {
				@Override
				public void resolved(final List<ProjectMember> members) {

					if (_mode != FormEditMode.READ_ONLY) {
						_users.resolve(new ObjectResolveHandler<List<User>>() {

							@Override
							public void resolved(List<User> users) {

								if (users != null) {
									Collection<ProjectMember> ms = toMembers(users, members);
									if (ms != null) {
										doLoad(ms, f, start, end, lh);
										return;
									}
								} else {
									lh.loaded(0, 0, 0, null, null);
								}
							}
						});
					} else {
						if (members != null) {
							doLoad(members, f, start, end, lh);
							return;
						} else {
							lh.loaded(0, 0, 0, null, null);
						}
					}
				}
			});
		}
	}

	private void doLoad(Collection<ProjectMember> ms, Filter f, long start, long end, DataLoadHandler<ProjectMember> lh) {

		List<ProjectMember> members = null;
		if (ms != null) {
			if (ms.size() > 0) {
				members = new Vector<ProjectMember>(ms.size());
				for (ProjectMember m : ms) {
					members.add(m);
				}
			}
		}

		if (f != null) {
			Vector<ProjectMember> fmembers = new Vector<ProjectMember>();
			for (ProjectMember m : members) {
				if (f.matches(m)) {
					fmembers.add(m);
				}
			}
			members = fmembers;
		}
		long total = members.size();
		int start1 = (int) start;
		int end1 = (int) end;
		if (start1 > 0 || end1 < members.size()) {
			if (start1 >= members.size()) {
				members = null;
			} else {
				if (end1 > members.size()) {
					end1 = members.size();
				}
				members = members.subList(start1, end1);
			}
		}
		lh.loaded(start1, end1, total, members, DataLoadAction.REPLACE);
	}

	private Collection<ProjectMember> toMembers(List<User> users, List<ProjectMember> members) {

		Map<User, ProjectMember> map = new HashMap<User, ProjectMember>();
		for (User user : users) {
			map.put(user, new ProjectMember(user, null, null));
		}
		if (members != null) {
			for (ProjectMember member : members) {
				map.put(member.user(), member);
			}
		}
		if (map.size() > 0) {
			return map.values();
		}
		return null;
	}

}
