package daris.client.ui.project;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.dnd.DropCheck;
import arc.gui.gwt.dnd.DropHandler;
import arc.gui.gwt.dnd.DropListener;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.HTML;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.list.ListGridRowContextMenuHandler;
import arc.gui.gwt.widget.list.ListGridRowDoubleClickHandler;
import arc.gui.gwt.widget.menu.ActionContextMenu;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.gui.gwt.widget.tip.ToolTip;
import arc.gui.gwt.widget.tip.ToolTipHandler;
import arc.gui.menu.ActionEntry;
import arc.gui.menu.Menu;
import arc.mf.client.util.Action;
import arc.mf.client.util.IsValid;
import arc.mf.client.util.MustBeValid;
import arc.mf.client.util.ObjectUtil;
import arc.mf.client.util.StateChangeListener;
import arc.mf.client.util.Transformer;
import arc.mf.client.util.Validity;
import arc.mf.object.ObjectMessageResponse;

import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;

import daris.client.model.project.DataUse;
import daris.client.model.project.Project;
import daris.client.model.project.ProjectMember;
import daris.client.model.project.ProjectRole;
import daris.client.model.project.messages.ProjectMemberList;
import daris.client.model.user.User;
import daris.client.ui.DObjectGUIRegistry;

public class ProjectMemberGrid extends ListGrid<ProjectMember> implements DropHandler, MustBeValid, StateChangeListener {

	private static class ProjectMemberTransformer extends Transformer<ProjectMember, ListGridEntry<ProjectMember>> {

		public static final ProjectMemberTransformer INSTANCE = new ProjectMemberTransformer();

		private ProjectMemberTransformer() {

		}

		@Override
		protected ListGridEntry<ProjectMember> doTransform(ProjectMember pm) throws Throwable {

			ListGridEntry<ProjectMember> pme = new ListGridEntry<ProjectMember>(pm);
			pme.set("authority", pm.user().authority());
			pme.set("domain", pm.user().domain());
			pme.set("user", pm.user().user());
			pme.set("role", pm.role());
			pme.set("dataUse", pm.dataUse());
			pme.set("id", pm.user().id());
			pme.set("name", pm.user().name());
			pme.set("protocol", pm.user().protocol());
			pme.set("email", pm.user().email());
			pme.set("userString", pm.user().toString());
			return pme;
		}
	}

	private static class ProjectMemberListTransformer extends
			Transformer<List<ProjectMember>, List<ListGridEntry<ProjectMember>>> {
		public static final ProjectMemberListTransformer INSTANCE = new ProjectMemberListTransformer();

		private ProjectMemberListTransformer() {

		};

		@Override
		protected List<ListGridEntry<ProjectMember>> doTransform(List<ProjectMember> pms) throws Throwable {

			if (pms != null) {
				if (!pms.isEmpty()) {
					List<ListGridEntry<ProjectMember>> pmes = new Vector<ListGridEntry<ProjectMember>>(pms.size());
					for (ProjectMember pm : pms) {
						pmes.add(ProjectMemberTransformer.INSTANCE.transform(pm));
					}
					return pmes;
				}
			}
			return null;
		}
	}

	private static class ProjectMemberDataSource implements DataSource<ListGridEntry<ProjectMember>> {

		private Project _o;
		private FormEditMode _mode;

		private ProjectMemberDataSource(Project o, FormEditMode mode) {

			_o = o;
			_mode = mode;
		}

		@Override
		public boolean isRemote() {

			if (_mode.equals(FormEditMode.CREATE)) {
				return true;
			}
			return false;
		}

		@Override
		public boolean supportCursor() {

			return false;
		}

		@Override
		public void load(final Filter f, final long start, final long end,
				final DataLoadHandler<ListGridEntry<ProjectMember>> lh) {

			if (_mode.equals(FormEditMode.CREATE)) {
				if (_o.members() == null) {
					lh.loaded(0, 0, 0, null, null);
				} else {
					doLoad(f, start, end, _o.members(), lh);
				}
			} else {
				new ProjectMemberList(_o).send(new ObjectMessageResponse<List<ProjectMember>>() {

					@Override
					public void responded(List<ProjectMember> pms) {

						if (pms != null) {
							if (!pms.isEmpty()) {
								doLoad(f, start, end, pms, lh);
								return;
							}
						}
						lh.loaded(0, 0, 0, null, null);
					}
				});
			}
		}

		private void doLoad(Filter f, long start, long end, List<ProjectMember> pms,
				final DataLoadHandler<ListGridEntry<ProjectMember>> lh) {

			if (pms != null) {
				Collections.sort(pms);
			}
			List<ListGridEntry<ProjectMember>> pmes = ProjectMemberListTransformer.INSTANCE.transform(pms);
			int total = pms.size();
			int start0 = (int) start;
			int end0 = (int) end;
			if (start0 > 0 || end0 < total) {
				if (start0 >= total) {
					pmes = null;
				} else {
					if (end0 > total) {
						end0 = total;
					}
					pmes = pmes.subList(start0, end0);
				}
			}
			lh.loaded(start0, end0, pmes == null ? 0 : total, pmes, DataLoadAction.REPLACE);
		}
	}

	private Project _o;

	private FormEditMode _mode;

	public ProjectMemberGrid(Project o, FormEditMode mode) {

		super(new ProjectMemberDataSource(o, mode), ScrollPolicy.AUTO);
		_o = o;
		_mode = mode;
		if (_mode.equals(FormEditMode.READ_ONLY)) {
			addColumnDefn("name", "Name").setWidth(100);
			addColumnDefn("authority", "Authority").setWidth(120);
			addColumnDefn("domain", "Domain").setWidth(100);
			addColumnDefn("user", "User").setWidth(100);
			addColumnDefn("role", "Role").setWidth(120);
			addColumnDefn("dataUse", "Data Use").setWidth(80);
		} else {
			addColumnDefn("role", "Role").setWidth(120);
			addColumnDefn("domain", "Domain").setWidth(100);
			addColumnDefn("user", "User").setWidth(100);
			addColumnDefn("dataUse", "Data Use").setWidth(80);
		}

		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(false);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading project members...");
		setCursorSize(Integer.MAX_VALUE);

		setRowToolTip(new ToolTip<ProjectMember>() {

			@Override
			public void generate(ProjectMember pm, ToolTipHandler th) {

				th.setTip(new HTML(pm.toHTML()));

			}
		});

		/*
		 * 
		 */
		setObjectRegistry(DObjectGUIRegistry.get());

		/*
		 * make drop target, and enable row drag (to delete member)
		 */
		if (!_mode.equals(FormEditMode.READ_ONLY)) {
			setRowDoubleClickHandler(new ListGridRowDoubleClickHandler<ProjectMember>() {

				@Override
				public void doubleClicked(final ProjectMember pm, DoubleClickEvent event) {

					if (pm == null) {
						return;
					}
					int x = event.getClientX();
					int y = event.getClientY();
					ProjectMemberRoleSelector.showAt(x, y, new ProjectMemberRoleSelector.RoleSelectionListener() {

						@Override
						public void roleSelected(ProjectRole role, DataUse dataUse) {

							if (role != null) {
								pm.setRole(role);
								pm.setDataUse(dataUse);
								_o.addMember(pm);
								commitChangesAndRefresh();
							}
						}
					});
				}
			});

			setRowContextMenuHandler(new ListGridRowContextMenuHandler<ProjectMember>() {

				@Override
				public void show(final ProjectMember pm, ContextMenuEvent event) {

					final int x = event.getNativeEvent().getClientX();
					final int y = event.getNativeEvent().getClientY();
					Menu menu = new Menu("Member");
					menu.setShowTitle(true);
					menu.add(new ActionEntry("Remove", new Action() {

						@Override
						public void execute() {

							_o.removeMember(pm);
							commitChangesAndRefresh();
						}
					}));
					menu.add(new ActionEntry("Set role and data-use", new Action() {

						@Override
						public void execute() {

							ProjectMemberRoleSelector.showAt(x, y,
									new ProjectMemberRoleSelector.RoleSelectionListener() {

										@Override
										public void roleSelected(ProjectRole role, DataUse dataUse) {

											if (role != null) {
												if (!role.equals(pm.role())
														|| !ObjectUtil.equals(dataUse, pm.dataUse())) {
													pm.setRole(role);
													pm.setDataUse(dataUse);
													_o.addMember(pm);
													commitChangesAndRefresh();
												}
											}
										}
									});
						}
					}));
					ActionContextMenu am = new ActionContextMenu(menu);
					NativeEvent ne = event.getNativeEvent();
					am.showAt(ne);
				}
			});
			enableDropTarget(false);
			setDropHandler(this);
			enableRowDrag();
		}
	}

	private void commitChangesAndRefresh() {

		commitChanges(new Action() {

			@Override
			public void execute() {

				ProjectMemberGrid.this.refresh();
			}
		});
	}

	private void commitChanges(final Action postAction) {

		if (_mode.equals(FormEditMode.UPDATE)) {
			_o.commitMembers(new ObjectMessageResponse<Boolean>() {

				@Override
				public void responded(Boolean r) {

					if (r) {
						_changed = true;
						ProjectMemberGrid.this.notifyOfChangeInState();
						postAction.execute();
					}
				}
			});
		} else {
			notifyOfChangeInState();
			if (postAction != null) {
				postAction.execute();
			}
		}
	}

	@Override
	public DropCheck checkCanDrop(Object o) {

		if (_mode.equals(FormEditMode.READ_ONLY)) {
			return DropCheck.CANNOT;
		}
		if (o instanceof User || o instanceof ProjectMember) {
			return DropCheck.CAN;
		}
		return DropCheck.CANNOT;
	}

	@Override
	public void drop(final BaseWidget target, final List<Object> objects, final DropListener dl) {

		if (objects == null) {
			dl.dropped(DropCheck.CANNOT);
			return;
		}
		if (objects.isEmpty()) {
			dl.dropped(DropCheck.CANNOT);
			return;
		}

		final ProjectMemberRoleSelector.RoleSelectionListener rsl = new ProjectMemberRoleSelector.RoleSelectionListener() {
			@Override
			public void roleSelected(ProjectRole role, DataUse dataUse) {

				if (role == null) {
					dl.dropped(DropCheck.CANNOT);
					return;
				}
				for (Object o : objects) {
					if (o instanceof User) {
						_o.addMember(new ProjectMember(((User) o), role, dataUse));
					} else if (o instanceof ProjectMember) {
						ProjectMember pm = ((ProjectMember) o);
						pm.setRole(role);
						pm.setDataUse(dataUse);
						_o.addMember(pm);
					}
				}
				commitChangesAndRefresh();
				dl.dropped(DropCheck.CAN);
			}

		};

		ProjectMemberRoleSelector.showAt(target.absoluteLeft() + target.width() / 2,
				target.absoluteTop() + target.height() / 2, rsl);

	}

	private List<StateChangeListener> _cls;
	private boolean _changed = false;

	@Override
	public boolean changed() {

		if (_mode.equals(FormEditMode.READ_ONLY)) {
			return false;
		}
		return _changed;
	}

	@Override
	public void addChangeListener(StateChangeListener listener) {

		if (_cls == null) {
			_cls = new Vector<StateChangeListener>();
		}
		_cls.add(listener);
	}

	@Override
	public void removeChangeListener(StateChangeListener listener) {

		if (_cls != null) {
			_cls.remove(listener);
		}
	}

	@Override
	public Validity valid() {

		if (_mode.equals(FormEditMode.READ_ONLY)) {
			return IsValid.INSTANCE;
		}

		if (!_o.hasMembersOrRoleMembers()) {
			return new Validity() {

				@Override
				public boolean valid() {

					return false;
				}

				@Override
				public String reasonForIssue() {

					return "No member or role members are set.";
				}
			};
		}
		if (!_o.hasAdminMember()) {
			return new Validity() {

				@Override
				public boolean valid() {

					return false;
				}

				@Override
				public String reasonForIssue() {

					return "No project-administrator member or role member is set.";
				}
			};
		}
		return IsValid.INSTANCE;
	}

	@Override
	public void notifyOfChangeInState() {

		if (_cls == null) {
			return;
		}

		for (StateChangeListener cl : _cls) {
			cl.notifyOfChangeInState();
		}
	}

}
