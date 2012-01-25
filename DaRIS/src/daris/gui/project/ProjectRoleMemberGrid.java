package daris.gui.project;

import java.util.List;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.combo.ComboBox;
import arc.gui.gwt.widget.combo.ComboBox.ChangeListener;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Transformer;
import daris.model.project.ProjectRef;
import daris.model.project.ProjectRoleMember;
import daris.model.project.ProjectRoleTypes;

public class ProjectRoleMemberGrid extends ListGrid<ProjectRoleMember> {

	private static class RoleMemberEntryTransformer extends
			Transformer<ProjectRoleMember, ListGridEntry<ProjectRoleMember>> {

		public static final Transformer<ProjectRoleMember, ListGridEntry<ProjectRoleMember>> INSTANCE = new RoleMemberEntryTransformer();

		@Override
		protected ListGridEntry<ProjectRoleMember> doTransform(ProjectRoleMember rm) throws Throwable {

			ListGridEntry<ProjectRoleMember> lge = new ListGridEntry<ProjectRoleMember>(rm);
			lge.set("id", rm.roleUser().id());
			lge.set("member", rm.roleUser().member());
			lge.set("dataUse", rm.dataUse());
			lge.set("roleType", rm.role());
			lge.setDescription("Project Role Member");
			return lge;

		}

	}

	private ProjectRef _project;

	private ProjectRoleMemberDataSource _ds;

	private FormEditMode _mode;

	private boolean _fireSelectionEvent = true;

	public ProjectRoleMemberGrid(ProjectRef project, FormEditMode mode) {

		super(ScrollPolicy.AUTO);
		_mode = mode;
		_ds = new ProjectRoleMemberDataSource(project, mode);
		setDataSource(new ListGridDataSource<ProjectRoleMember>(_ds, RoleMemberEntryTransformer.INSTANCE));
		_project = project;
		addColumnDefn("id", "id").setWidth(60);
		addColumnDefn("member", "member").setWidth(100);
		if (mode == FormEditMode.READ_ONLY) {
			addColumnDefn("roleType", "role type");
			addColumnDefn("dataUse", "data use");
		} else {

			addColumnDefn("roleType", "role type", "role type", new WidgetFormatter<Object, String>() {

				@Override
				public BaseWidget format(final Object context, String roleType) {

					ComboBox<String> combo = ProjectRoleTypeComboBox.create(roleType);
					combo.addChangeListener(new ChangeListener<String>() {

						@Override
						public void changed(ComboBox<String> cb) {

							ProjectRoleMember rm = (ProjectRoleMember) context;
							if (rm != null) {
								rm.setRole(cb.value());
								// if (isSelected(rm)) {
								// _project.addRoleMember(rm);
								// }
							}
						}
					});
					return combo;
				}
			}).setWidth(160);
			addColumnDefn("dataUse", "data use", "data use", new WidgetFormatter<Object, String>() {

				@Override
				public BaseWidget format(final Object context, String dataUse) {

					ComboBox<String> combo = DataUseComboBox.create(dataUse);
					combo.addChangeListener(new ChangeListener<String>() {

						@Override
						public void changed(ComboBox<String> cb) {

							ProjectRoleMember rm = (ProjectRoleMember) context;
							if (rm != null) {
								rm.setDataUse(cb.value());
								// if (isSelected(rm)) {
								// _project.addRoleMember(rm);
								// }
							}
						}
					});
					return combo;
				}
			}).setWidth(170);
		}
		setShowHeader(true);
		setHeight100();
		setWidth100();
		setFontSize(11);

		setCellSpacing(0);
		setCellPadding(2);
		setEmptyMessage("");
		setLoadingMessage("loading project role-members...");
		setCursorSize(10);
		if (mode == FormEditMode.READ_ONLY) {
			setMultiSelect(false);
		} else {
			setMultiSelect(true);
			setSelectionHandler(new SelectionHandler<ProjectRoleMember>() {

				@Override
				public void selected(ProjectRoleMember rm) {

					if (_fireSelectionEvent) {
						_project.setRoleMembers(ProjectRoleMemberGrid.this.selections());
					}
				}

				@Override
				public void deselected(ProjectRoleMember rm) {

					if (_fireSelectionEvent) {
						_project.setRoleMembers(ProjectRoleMemberGrid.this.selections());
					}
				}
			});
		}

	}

	protected ProjectRoleMember selected() {

		List<ProjectRoleMember> srms = selections();
		if (srms != null) {
			if (srms.size() > 0) {
				return srms.get(0);
			}
		}
		return null;
	}

	@Override
	protected void postLoad(long start, long end, long total, List<ListGridEntry<ProjectRoleMember>> entries) {

		if (_mode != FormEditMode.READ_ONLY) {
			if (entries != null) {
				_fireSelectionEvent = false;
				for (int i = 0; i < entries.size(); i++) {
					ListGridEntry<ProjectRoleMember> e = entries.get(i);
					ProjectRoleMember rm = e.data();
					if (rm.role() != null) {
						select(i);
					}
				}
				_project.setRoleMembers(ProjectRoleMemberGrid.this.selections());
				_fireSelectionEvent = true;
			}

		}
	}

	private boolean isSelected(ProjectRoleMember rm) {

		if (selections() != null) {
			return selections().contains(rm);
		}
		return false;
	}

	public String validate() {

		List<ProjectRoleMember> rms = selections();
		if (rms == null) {
			// no role-member, it is ok.
			return null;
		}
		if (rms.isEmpty()) {
			// no role-member, it is ok.
			return null;
		}
		for (ProjectRoleMember rm : rms) {
			if (rm.role() == null) {
				return "You need set the role for role-member(" + rm.roleUser().member() + ").";
			}
			if (rm.role().equals(ProjectRoleTypes.MEMBER) || rm.role().equals(ProjectRoleTypes.GUEST)) {
				if (rm.dataUse() == null) {
					return "No data-use is set for " + rm.role() + "(" + rm.roleUser().member() + ").";
				}
			}
		}
		return null;
	}

	public boolean valid() {

		if (validate() == null) {
			return true;
		}
		return false;
	}

}