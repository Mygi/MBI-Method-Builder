package daris.client.ui.user;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.mf.client.util.Transformer;
import arc.mf.object.ObjectMessageResponse;
import daris.client.model.user.User;
import daris.client.model.user.messages.UserDescribe;
import daris.client.ui.DObjectGUIRegistry;

public class UserGrid extends ListGrid<User> {

	private static class UserTransformer extends
			Transformer<User, ListGridEntry<User>> {

		public static final UserTransformer INSTANCE = new UserTransformer();

		private UserTransformer() {

		}

		@Override
		protected ListGridEntry<User> doTransform(User user) throws Throwable {

			if (user == null) {
				return null;
			}
			ListGridEntry<User> entry = new ListGridEntry<User>(user);
			entry.set("id", user.id());
			entry.set("authority", user.authority());
			entry.set("protocol", user.protocol());
			entry.set("domain", user.domain());
			entry.set("user", user.user());
			entry.set("name", user.name());
			entry.set("email", user.email());
			return entry;
		}

	}

	private static class UserListTransformer extends
			Transformer<List<User>, List<ListGridEntry<User>>> {

		public static final UserListTransformer INSTANCE = new UserListTransformer();

		private UserListTransformer() {

		}

		@Override
		protected List<ListGridEntry<User>> doTransform(List<User> users)
				throws Throwable {

			if (users == null) {
				return null;
			}
			if (users.isEmpty()) {
				return null;
			}
			List<ListGridEntry<User>> entries = new Vector<ListGridEntry<User>>(
					users.size());
			for (User user : users) {
				entries.add(UserTransformer.INSTANCE.transform(user));
			}
			return entries;
		}
	}

	private static class UserDataSource implements
			DataSource<ListGridEntry<User>> {

		public UserDataSource() {

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
				final DataLoadHandler<ListGridEntry<User>> lh) {

			new UserDescribe().send(new ObjectMessageResponse<List<User>>() {

				@Override
				public void responded(List<User> users) {

					if (users != null) {
						doLoad(f, start, end, users, lh);
					} else {
						lh.loaded(0, 0, 0, null, null);
					}
				}
			});

		}

		private void doLoad(Filter f, long start, long end, List<User> users,
				DataLoadHandler<ListGridEntry<User>> lh) {

			List<ListGridEntry<User>> es = UserListTransformer.INSTANCE
					.transform(users);
			int total = es.size();
			int start1 = start < 0 ? 0 : (start > total ? total : (int) start);
			int end1 = end > total ? total : (int) end;
			if (start1 < 0 || end1 > total || start1 > end) {
				lh.loaded(start, end, total, null, null);
			} else {
				es = es.subList(start1, end1);
				lh.loaded(start1, end1, total, es, DataLoadAction.REPLACE);
			}
		}
	}

	public UserGrid() {

		this(false);
	}

	public UserGrid(boolean allCols) {

		super(new UserDataSource(), ScrollPolicy.AUTO);
		if (allCols) {
			addColumnDefn("id", "ID").setWidth(60);
			addColumnDefn("authority", "Authority").setWidth(120);
			addColumnDefn("protocol", "Protocol").setWidth(60);
			addColumnDefn("domain", "Domain").setWidth(100);
			addColumnDefn("user", "User").setWidth(100);
			addColumnDefn("name", "Name").setWidth(120);
			addColumnDefn("email", "Email").setWidth(120);
		} else {
			addColumnDefn("authority", "Authority").setWidth(120);
			addColumnDefn("domain", "Domain").setWidth(100);
			addColumnDefn("user", "User").setWidth(100);
		}
		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(false);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading users from Mediaflux server...");
		setCursorSize(Integer.MAX_VALUE);

		setRowToolTip(new ToolTip<User>() {

			@Override
			public void generate(User u, ToolTipHandler th) {

				th.setTip(new HTML(u.toHTML()));
			}
		});

		/*
		 * enable drag from grid
		 */
		setObjectRegistry(DObjectGUIRegistry.get());
		enableRowDrag();
		refresh();
	}

}
