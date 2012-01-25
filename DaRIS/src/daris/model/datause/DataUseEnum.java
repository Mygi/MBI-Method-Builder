package daris.model.datause;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class DataUseEnum implements DynamicEnumerationDataSource<String> {
	private static DataUseEnum _instance;

	public static DataUseEnum get() {

		if (_instance == null) {
			_instance = new DataUseEnum();
		}
		return _instance;
	}

	private DataUseSetRef _dusr;

	private DataUseEnum() {

		_dusr = DataUseSetRef.get();
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String dataUse,
			final DynamicEnumerationExistsHandler handler) {

		_dusr.resolve(new ObjectResolveHandler<DataUseSet>() {
			@Override
			public void resolved(DataUseSet dataUses) {

				if (dataUses != null) {
					if (dataUses.contains(dataUse)) {
						handler.exists(dataUse, true);
						return;
					}
				}
				handler.exists(dataUse, false);
			}
		});
	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<String> handler) {

		_dusr.resolve(new ObjectResolveHandler<DataUseSet>() {
			@Override
			public void resolved(DataUseSet dataUses) {

				if (dataUses != null) {
					Vector<Value<String>> values = new Vector<Value<String>>(
							dataUses.size());
					for (String dataUse : dataUses) {
						values.add(new Value<String>(dataUse, dataUse, dataUse));
					}
					List<Value<String>> rvs = values;
					int start1 = (int) start;
					int end1 = (int) end;
					long total = values.size();
					if (start1 > 0 || end1 < values.size()) {
						if (start1 >= values.size()) {
							rvs = null;
						} else {
							if (end1 > values.size()) {
								end1 = values.size();
							}
							rvs = values.subList(start1, end1);
						}
					}
					handler.process(start1, end1, total, rvs);
					return;
				}
				handler.process(0, 0, 0, null);
			}
		});
	}
}
