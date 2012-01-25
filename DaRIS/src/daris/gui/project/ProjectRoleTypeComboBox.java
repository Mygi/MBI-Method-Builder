package daris.gui.project;

import java.util.List;
import java.util.Vector;

import arc.gui.gwt.data.DataLoadAction;
import arc.gui.gwt.data.DataLoadHandler;
import arc.gui.gwt.data.DataSource;
import arc.gui.gwt.data.filter.Filter;
import arc.gui.gwt.widget.combo.ComboBox;
import arc.gui.gwt.widget.combo.ComboBoxEntry;
import arc.mf.object.ObjectResolveHandler;
import daris.model.project.ProjectRoleTypesRef;

public class ProjectRoleTypeComboBox {

	private ProjectRoleTypeComboBox() {

	}

	private static class ProjectRoleTypeDataSource implements
			DataSource<ComboBoxEntry<String>> {

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
				final DataLoadHandler<ComboBoxEntry<String>> lh) {

			ProjectRoleTypesRef.instance().resolve(
					new ObjectResolveHandler<List<String>>() {
						@Override
						public void resolved(List<String> rts) {

							List<String> roleTypes = rts;
							if (roleTypes != null) {
								List<ComboBoxEntry<String>> entries = new Vector<ComboBoxEntry<String>>(
										roleTypes.size());
								for (String roleType : roleTypes) {
									entries.add(new ComboBoxEntry<String>(
											roleType));
								}

								if (f != null) {
									List<ComboBoxEntry<String>> fentries = new Vector<ComboBoxEntry<String>>();
									for (ComboBoxEntry<String> e : entries) {
										if (f.matches(e)) {
											fentries.add(e);
										}
									}
									entries = fentries;
								}
								int total = entries.size();
								int start1 = (int) start;
								int end1 = (int) end;
								if (start1 > 0 || end1 < entries.size()) {
									if (start1 >= entries.size()) {
										entries = null;
									} else {
										if (end1 > entries.size()) {
											end1 = entries.size();
										}
										entries = entries.subList(start1, end1);
									}
								}
								lh.loaded(start1, end1, total, entries,
										DataLoadAction.REPLACE);
								return;
							}
							lh.loaded(0, 0, 0, null, null);
						}
					});

		}

	}

	public static ComboBox<String> create(String roleType) {

		ComboBox<String> combo = new ComboBox<String>(
				new ProjectRoleTypeDataSource());
		combo.setValue(roleType, roleType);
		return combo;

	}

	public static ComboBox<String> create() {

		return new ComboBox<String>(new ProjectRoleTypeDataSource());

	}

}
