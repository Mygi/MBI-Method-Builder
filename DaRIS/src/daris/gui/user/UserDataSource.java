package daris.gui.user;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.mf.object.ObjectResolveHandler;
import daris.model.user.User;
import daris.model.user.UsersRef;

public class UserDataSource implements DataSource<User> {

	private UsersRef _users;

	public UserDataSource() {

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
	public void load(final Filter f, final long start, final long end,
			final DataLoadHandler<User> lh) {

		_users.resolve(new ObjectResolveHandler<List<User>>() {
			@Override
			public void resolved(List<User> us) {
				
				List<User> users = us;
				if (users != null) {					
					if (f != null) {
						List<User> fusers = new Vector<User>();
						for (User u : users) {
							if (f.matches(u)) {
								fusers.add(u);
							}
						}
						users = fusers;
					}
					int start1 = (int) start;
					int end1 = (int) end;
					if (start1 > 0 || end1 < users.size()) {
						if (start1 >= users.size()) {
							users = null;
						} else {
							if (end1 > users.size()) {
								end1 = users.size();
							}
							users = users.subList(start1, end1);
						}
					}
					long total = users.size();
					lh.loaded(start1, end1, total, users,
							DataLoadAction.REPLACE);
				}
				lh.loaded(0, 0, 0, null, null);
			}
			
		});

	}

}
