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
import daris.client.model.user.RoleUser;
import daris.client.model.user.messages.RoleUserDescribe;
import daris.client.ui.DObjectGUIRegistry;

public class RoleUserGrid extends ListGrid<RoleUser> {

	private static class RoleUserTransformer extends
			Transformer<RoleUser, ListGridEntry<RoleUser>> {

		public static final RoleUserTransformer INSTANCE = new RoleUserTransformer();

		private RoleUserTransformer() {

		}

		@Override
		protected ListGridEntry<RoleUser> doTransform(RoleUser ru)
				throws Throwable {

			if (ru == null) {
				return null;
			}
			ListGridEntry<RoleUser> entry = new ListGridEntry<RoleUser>(ru);
			entry.set("id", ru.id());
			entry.set("member", ru.member());
			return entry;
		}

	}

	private static class RoleUserListTransformer extends
			Transformer<List<RoleUser>, List<ListGridEntry<RoleUser>>> {

		public static final RoleUserListTransformer INSTANCE = new RoleUserListTransformer();

		private RoleUserListTransformer() {

		}

		@Override
		protected List<ListGridEntry<RoleUser>> doTransform(
				List<RoleUser> roleUsers) throws Throwable {

			if (roleUsers == null) {
				return null;
			}
			if (roleUsers.isEmpty()) {
				return null;
			}
			List<ListGridEntry<RoleUser>> entries = new Vector<ListGridEntry<RoleUser>>(
					roleUsers.size());
			for (RoleUser ru : roleUsers) {
				entries.add(RoleUserTransformer.INSTANCE.transform(ru));
			}
			return entries;
		}
	}

	private static class RoleUserDataSource implements
			DataSource<ListGridEntry<RoleUser>> {

		public RoleUserDataSource() {

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
				final DataLoadHandler<ListGridEntry<RoleUser>> lh) {

			new RoleUserDescribe()
					.send(new ObjectMessageResponse<List<RoleUser>>() {

						@Override
						public void responded(List<RoleUser> users) {

							if (users != null) {
								doLoad(f, start, end, users, lh);
							} else {
								lh.loaded(0, 0, 0, null, null);
							}
						}
					});

		}

		private void doLoad(Filter f, long start, long end,
				List<RoleUser> users,
				DataLoadHandler<ListGridEntry<RoleUser>> lh) {

			List<ListGridEntry<RoleUser>> es = RoleUserListTransformer.INSTANCE
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

	public RoleUserGrid() {

		super(new RoleUserDataSource(), ScrollPolicy.AUTO);
		addColumnDefn("id", "ID").setWidth(60);
		addColumnDefn("member", "Member").setWidth(120);
		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(false);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading role users from Mediaflux server...");
		setCursorSize(Integer.MAX_VALUE);

		setRowToolTip(new ToolTip<RoleUser>() {

			@Override
			public void generate(RoleUser u, ToolTipHandler th) {

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
