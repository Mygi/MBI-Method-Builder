package daris.gui.project;

import java.util.List;

import arc.gui.form.FormEditMode;
import arc.gui.gwt.widget.BaseWidget;
import arc.gui.gwt.widget.combo.ComboBox;
import arc.gui.gwt.widget.combo.ComboBox.ChangeListener;
import arc.gui.gwt.widget.event.SelectionHandler;
import arc.gui.gwt.widget.format.WidgetFormatter;
import arc.gui.gwt.widget.list.ListGrid;
import arc.gui.gwt.widget.list.ListGridColumn;
import arc.gui.gwt.widget.list.ListGridDataSource;
import arc.gui.gwt.widget.list.ListGridEntry;
import arc.gui.gwt.widget.scroll.ScrollPolicy;
import arc.mf.client.util.Transformer;
import arc.mf.object.ObjectMessageResponse;
import daris.model.project.ProjectMember;
import daris.model.project.ProjectRef;
import daris.model.project.ProjectRoleTypes;

public class ProjectMemberGrid extends ListGrid<ProjectMember> {

	private static class MemberEntryTransformer extends
			Transformer<ProjectMember, ListGridEntry<ProjectMember>> {

		public static final Transformer<ProjectMember, ListGridEntry<ProjectMember>> INSTANCE = new MemberEntryTransformer();

		@Override
		protected ListGridEntry<ProjectMember> doTransform(ProjectMember m) throws Throwable {

			ListGridEntry<ProjectMember> lge = new ListGridEntry<ProjectMember>(
					m);
			lge.set("id", m.user().id());
			lge.set("authority", m.user().authority());
			lge.set("protocol", m.user().protocol());
			lge.set("domain", m.user().domain());
			lge.set("user", m.user().user());
			lge.set("firstName", m.user().firstName());
			lge.set("middleName", m.user().middleName());
			lge.set("lastName", m.user().lastName());
			lge.set("email", m.user().email());
			lge.set("dataUse", m.dataUse());
			lge.set("roleType", m.role());
			lge.setDescription("Project Member");
			return lge;

		}

	}

	private ProjectRef _project;

	private ProjectMemberDataSource _ds;

	private FormEditMode _mode;

	private boolean _fireSelectionEvent = true;

	@SuppressWarnings("rawtypes")
	public ProjectMemberGrid(ProjectRef project, FormEditMode mode) {

		super(ScrollPolicy.AUTO);
		_mode = mode;
		_ds = new ProjectMemberDataSource(project, mode);
		setDataSource(new ListGridDataSource<ProjectMember>(_ds,
				MemberEntryTransformer.INSTANCE));
		_project = project;

		ListGridColumn idColumn = addColumnDefn("id", "id");
		idColumn.setWidth(60);

		ListGridColumn authorityColumn = addColumnDefn("authority", "authority");
		authorityColumn.setWidth(60);
		
		ListGridColumn protocolColumn = addColumnDefn("protocol", "protocol");
		protocolColumn.setWidth(60);

		ListGridColumn domainColumn = addColumnDefn("domain", "domain");
		domainColumn.setWidth(60);

		ListGridColumn userColumn = addColumnDefn("user", "user");
		userColumn.setWidth(60);

		ListGridColumn firstNameColumn = addColumnDefn("firstName",
				"first name");
		firstNameColumn.setWidth(60);

		ListGridColumn middleNameColumn = addColumnDefn("middleName",
				"middle name");
		middleNameColumn.setWidth(80);

		ListGridColumn lastNameColumn = addColumnDefn("lastName", "last name");
		lastNameColumn.setWidth(60);

		ListGridColumn emailColumn = addColumnDefn("email", "email");
		emailColumn.setWidth(120);

		if (mode == FormEditMode.READ_ONLY) {
			addColumnDefn("roleType", "role type");
			addColumnDefn("dataUse", "data use");
		} else {
			ListGridColumn roleTypeColumn = addColumnDefn("roleType",
					"role type", "role type",
					new WidgetFormatter<Object, String>() {

						@Override
						public BaseWidget format(final Object context,
								String roleType) {

							ComboBox<String> combo = ProjectRoleTypeComboBox
									.create(roleType);
							combo.addChangeListener(new ChangeListener<String>() {

								@Override
								public void changed(ComboBox<String> cb) {

									ProjectMember m = (ProjectMember) context;
									if (m != null) {
										m.setRole(cb.value());
//										if (isSelected(m)) {
//											_project.addMember(m);
//										}
									}
								}
							});
							return combo;
						}
					});
			roleTypeColumn.setWidth(160);
			ListGridColumn dataUseColumn = addColumnDefn("dataUse", "data use",
					"data use", new WidgetFormatter<Object, String>() {

						@Override
						public BaseWidget format(final Object context,
								String dataUse) {

							ComboBox<String> combo = DataUseComboBox
									.create(dataUse);
							combo.addChangeListener(new ChangeListener<String>() {

								@Override
								public void changed(ComboBox<String> cb) {

									ProjectMember m = (ProjectMember) context;
									if (m != null) {
										m.setDataUse(cb.value());
//										if (isSelected(m)) {
//											_project.addMember(m);
//										}
									}
								}
							});
							return combo;
						}
					});
			dataUseColumn.setWidth(170);
		}
		setShowHeader(true);
		setHeight100();
		setWidth100();
		setFontSize(11);

		setCellSpacing(0);
		setCellPadding(2);
		setEmptyMessage("");
		setLoadingMessage("loading project members...");
		setCursorSize(500);
		if (mode == FormEditMode.READ_ONLY) {
			setMultiSelect(false);
		} else {
			setMultiSelect(true);
			setSelectionHandler(new SelectionHandler<ProjectMember>() {

				@Override
				public void selected(ProjectMember m) {

					if (_fireSelectionEvent) {
						_project.setMembers(ProjectMemberGrid.this.selections());
					}
				}

				@Override
				public void deselected(ProjectMember m) {

					if (_fireSelectionEvent) {
						_project.setMembers(ProjectMemberGrid.this.selections());
					}
				}
			});
		}

	}

	@Override
	protected void postLoad(long start, long end, long total,
			List<ListGridEntry<ProjectMember>> entries) {

		if (_mode != FormEditMode.READ_ONLY) {
			if (entries != null) {
				_fireSelectionEvent = false;
				for (int i = 0; i < entries.size(); i++) {
					ListGridEntry<ProjectMember> e = entries.get(i);
					ProjectMember m = e.data();
					if (m.role() != null) {
						select(i);
					}
				}
				_project.setMembers(ProjectMemberGrid.this.selections());
				_fireSelectionEvent = true;
			}

		}
	}

	private boolean isSelected(ProjectMember m) {

		if (selections() != null) {
			return selections().contains(m);
		}
		return false;
	}

	public String validate() {

		List<ProjectMember> ms = selections();
		if (ms == null) {
			return "No member is selected. You must select at lease one member.";
		}
		if (ms.isEmpty()) {
			return "No member is selected. You must select at lease one member.";
		}
		boolean hasProjectAdmin = false;
		for (ProjectMember m : ms) {
			if (m.role() == null) {
				return "You need set the role for member("
						+ m.user().toString() + ").";
			}
			if (m.role().equals(ProjectRoleTypes.MEMBER)
					|| m.role().equals(ProjectRoleTypes.MEMBER)) {
				if (m.dataUse() == null) {
					return "No data-use is set for " + m.role() + "("
							+ m.user() + ").";
				}
			}
			if (m.role().equals(ProjectRoleTypes.PROJECT_ADMINISTRATOR)) {
				hasProjectAdmin = true;
			}
		}
		if (!hasProjectAdmin) {
			return "No project-administrator is set. You must select and set at least one project-administrator.";
		}
		return null;
	}

	public boolean valid() {

		if (validate() == null) {
			return true;
		}
		return false;
	}
	
	public void commit(){
		
	}

}
