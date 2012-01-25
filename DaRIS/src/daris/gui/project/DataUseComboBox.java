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
import daris.model.datause.DataUseSet;
import daris.model.datause.DataUseSetRef;

public class DataUseComboBox {
	private DataUseComboBox() {

	}

	private static class DataUseDataSource implements
			DataSource<ComboBoxEntry<String>> {
		private DataUseSetRef _dusr;

		public DataUseDataSource() {

			_dusr = DataUseSetRef.get();
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
				final DataLoadHandler<ComboBoxEntry<String>> lh) {

			_dusr.resolve(new ObjectResolveHandler<DataUseSet>() {
				@Override
				public void resolved(DataUseSet dataUses) {

					if (dataUses != null) {
						List<ComboBoxEntry<String>> entries = new Vector<ComboBoxEntry<String>>(
								dataUses.size());
						for (String du : dataUses) {
							entries.add(new ComboBoxEntry<String>(du));
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
					} else {
						lh.loaded(0, 0, 0, null, null);
					}
				}
			});
		}
	}

	public static ComboBox<String> create() {

		return new ComboBox<String>(new DataUseDataSource());
	}

	public static ComboBox<String> create(String dataUse) {

		ComboBox<String> combo = new ComboBox<String>(new DataUseDataSource());
		combo.setValue(dataUse, dataUse);
		return combo;
	}
}
