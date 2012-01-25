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
import daris.client.model.project.ProjectRole;
import daris.client.model.project.ProjectRoleMember;
import daris.client.model.project.messages.ProjectRoleMemberList;
import daris.client.model.user.RoleUser;
import daris.client.ui.DObjectGUIRegistry;

public class ProjectRoleMemberGrid extends ListGrid<ProjectRoleMember>
		implements DropHandler, MustBeValid {

	private static class ProjectRoleMemberTransformer extends
			Transformer<ProjectRoleMember, ListGridEntry<ProjectRoleMember>> {

		public static final ProjectRoleMemberTransformer INSTANCE = new ProjectRoleMemberTransformer();

		private ProjectRoleMemberTransformer() {

		}

		@Override
		protected ListGridEntry<ProjectRoleMember> doTransform(
				ProjectRoleMember prm) throws Throwable {

			ListGridEntry<ProjectRoleMember> prme = new ListGridEntry<ProjectRoleMember>(
					prm);
			prme.set("id", prm.member().id());
			prme.set("member", prm.member().member());
			prme.set("role", prm.role());
			prme.set("dataUse", prm.dataUse());
			return prme;
		}
	}

	private static class ProjectRoleMemberListTransformer
			extends
			Transformer<List<ProjectRoleMember>, List<ListGridEntry<ProjectRoleMember>>> {
		public static final ProjectRoleMemberListTransformer INSTANCE = new ProjectRoleMemberListTransformer();

		private ProjectRoleMemberListTransformer() {

		};

		@Override
		protected List<ListGridEntry<ProjectRoleMember>> doTransform(
				List<ProjectRoleMember> prms) throws Throwable {

			if (prms != null) {
				if (!prms.isEmpty()) {
					List<ListGridEntry<ProjectRoleMember>> prmes = new Vector<ListGridEntry<ProjectRoleMember>>(
							prms.size());
					for (ProjectRoleMember pm : prms) {
						prmes.add(ProjectRoleMemberTransformer.INSTANCE
								.transform(pm));
					}
					return prmes;
				}
			}
			return null;
		}
	}

	private static class ProjectRoleMemberDataSource implements
			DataSource<ListGridEntry<ProjectRoleMember>> {

		private Project _o;
		private FormEditMode _mode;

		private ProjectRoleMemberDataSource(Project o, FormEditMode mode) {

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
				final DataLoadHandler<ListGridEntry<ProjectRoleMember>> lh) {

			if (_mode.equals(FormEditMode.CREATE)) {
				if (_o.roleMembers() == null) {
					lh.loaded(0, 0, 0, null, null);
				} else {
					doLoad(f, start, end, _o.roleMembers(), lh);
				}
			} else {
				new ProjectRoleMemberList(_o)
						.send(new ObjectMessageResponse<List<ProjectRoleMember>>() {

							@Override
							public void responded(List<ProjectRoleMember> prms) {

								if (prms != null) {
									if (!prms.isEmpty()) {
										doLoad(f, start, end, prms, lh);
										return;
									}
								}
								lh.loaded(0, 0, 0, null, null);
							}
						});
			}
		}

		private void doLoad(Filter f, long start, long end,
				List<ProjectRoleMember> prms,
				final DataLoadHandler<ListGridEntry<ProjectRoleMember>> lh) {

			if (prms != null) {
				Collections.sort(prms);
			}
			List<ListGridEntry<ProjectRoleMember>> prmes = ProjectRoleMemberListTransformer.INSTANCE
					.transform(prms);
			int total = prms.size();
			int start0 = (int) start;
			int end0 = (int) end;
			if (start0 > 0 || end0 < total) {
				if (start0 >= total) {
					prmes = null;
				} else {
					if (end0 > total) {
						end0 = total;
					}
					prmes = prmes.subList(start0, end0);
				}
			}
			lh.loaded(start0, end0, prmes == null ? 0 : total, prmes,
					DataLoadAction.REPLACE);
		}
	}

	private Project _o;

	private FormEditMode _mode;

	public ProjectRoleMemberGrid(Project o, FormEditMode mode) {

		super(new ProjectRoleMemberDataSource(o, mode), ScrollPolicy.AUTO);
		_o = o;
		_mode = mode;
		addColumnDefn("role", "Role").setWidth(120);
		addColumnDefn("member", "Member").setWidth(120);
		addColumnDefn("dataUse", "Data Use").setWidth(80);

		fitToParent();
		setShowHeader(true);
		setShowRowSeparators(true);
		setMultiSelect(false);
		setFontSize(10);
		setCellSpacing(0);
		setCellPadding(1);
		setEmptyMessage("");
		setLoadingMessage("Loading project role-members...");
		setCursorSize(Integer.MAX_VALUE);

		setRowToolTip(new ToolTip<ProjectRoleMember>() {

			@Override
			public void generate(ProjectRoleMember prm, ToolTipHandler th) {

				th.setTip(new HTML(prm.toHTML()));

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
			setRowDoubleClickHandler(new ListGridRowDoubleClickHandler<ProjectRoleMember>() {

				@Override
				public void doubleClicked(final ProjectRoleMember prm,
						DoubleClickEvent event) {

					if (prm == null) {
						return;
					}
					int x = event.getClientX();
					int y = event.getClientY();
					ProjectMemberRoleSelector
							.showAt(x,
									y,
									new ProjectMemberRoleSelector.RoleSelectionListener() {

										@Override
										public void roleSelected(
												ProjectRole role,
												DataUse dataUse) {

											if (role != null) {
												prm.setRole(role);
												prm.setDataUse(dataUse);
												_o.addRoleMember(prm);
												commitChangesAndRefresh();
											}
										}
									});
				}
			});

			setRowContextMenuHandler(new ListGridRowContextMenuHandler<ProjectRoleMember>() {

				@Override
				public void show(final ProjectRoleMember prm, ContextMenuEvent event) {

					final int x = event.getNativeEvent().getClientX();
					final int y = event.getNativeEvent().getClientY();
					Menu menu = new Menu("Role Member");
					menu.setShowTitle(true);
					menu.add(new ActionEntry("Remove", new Action() {

						@Override
						public void execute() {

							_o.removeRoleMember(prm);
							commitChangesAndRefresh();
						}
					}));
					menu.add(new ActionEntry("Set role and data-use",
							new Action() {

								@Override
								public void execute() {

									ProjectMemberRoleSelector
											.showAt(x,
													y,
													new ProjectMemberRoleSelector.RoleSelectionListener() {

														@Override
														public void roleSelected(
																ProjectRole role,
																DataUse dataUse) {

															if (role != null) {
																if (!role
																		.equals(prm
																				.role())
																		|| !ObjectUtil
																				.equals(dataUse,
																						prm.dataUse())) {
																	prm.setRole(role);
																	prm.setDataUse(dataUse);
																	_o.addRoleMember(prm);
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

				ProjectRoleMemberGrid.this.refresh();
			}
		});
	}

	private void commitChanges(final Action postAction) {

		if (_mode.equals(FormEditMode.UPDATE)) {
			_o.commitMembers(new ObjectMessageResponse<Boolean>() {

				@Override
				public void responded(Boolean r) {

					if (r) {
						postAction.execute();
					}
				}
			});
		} else {
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
		if (o instanceof RoleUser || o instanceof ProjectRoleMember) {
			return DropCheck.CAN;
		}
		return DropCheck.CANNOT;
	}

	@Override
	public void drop(final BaseWidget target, final List<Object> objects,
			final DropListener dl) {

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
					if (o instanceof RoleUser) {
						_o.addRoleMember(new ProjectRoleMember(((RoleUser) o), role,
								dataUse));
					} else if (o instanceof ProjectRoleMember) {
						ProjectRoleMember prm = ((ProjectRoleMember) o);
						prm.setRole(role);
						prm.setDataUse(dataUse);
						_o.addRoleMember(prm);
					}
				}
				commitChangesAndRefresh();
				dl.dropped(DropCheck.CAN);
			}

		};

		ProjectMemberRoleSelector.showAt(target.absoluteLeft() + target.width()
				/ 2, target.absoluteTop() + target.height() / 2, rsl);

	}

	private List<StateChangeListener> _cls;

	@Override
	public boolean changed() {

		if (_mode.equals(FormEditMode.READ_ONLY)) {
			return false;
		}
		if (_mode.equals(FormEditMode.CREATE) && _o.hasRoleMembers()) {
			return true;
		}
		return false;
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

}