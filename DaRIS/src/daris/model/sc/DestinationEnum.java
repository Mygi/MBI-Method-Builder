package daris.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class DestinationEnum implements
		DynamicEnumerationDataSource<Destination> {
	private static DestinationEnum _instance;

	public static DestinationEnum get() {

		if (_instance == null) {
			_instance = new DestinationEnum();
		}
		return _instance;
	}

	private DestinationSetRef _dsr;

	private DestinationEnum() {

		_dsr = DestinationSetRef.get();
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String name,
			final DynamicEnumerationExistsHandler handler) {

		_dsr.resolve(new ObjectResolveHandler<List<Destination>>() {
			@Override
			public void resolved(List<Destination> ds) {

				if (ds != null) {
					for (Destination d : ds) {
						if (d.name().equals(name)) {
							handler.exists(name, true);
							return;
						}
					}
				}
				handler.exists(name, false);
			}
		});
	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<Destination> handler) {

		_dsr.resolve(new ObjectResolveHandler<List<Destination>>() {
			@Override
			public void resolved(List<Destination> ds) {

				if (ds != null) {
					Vector<Value<Destination>> vs = new Vector<Value<Destination>>(
							ds.size());
					for (Destination d : ds) {
						vs.add(new Value<Destination>(d.name(), d.url(), d));
					}
					List<Value<Destination>> rvs = vs;
					int start1 = (int) start;
					int end1 = (int) end;
					long total = vs.size();
					if (start1 >= 0 || end1 < vs.size()) {
						if (start1 >= vs.size()) {
							rvs = null;
						} else {
							if (end1 >= vs.size()) {
								end1 = vs.size() - 1;
							}
							rvs = vs.subList(start1, end1 + 1);
						}
					}
					handler.process(start1, end1, total, rvs);
					return;
				}
				handler.process(0, 0, 0, null);
			}
		});
	}

	public void first(final ObjectResolveHandler<Destination> rh) {

		_dsr.resolve(new ObjectResolveHandler<List<Destination>>() {
			@Override
			public void resolved(List<Destination> o) {

				if (o != null) {
					if (o.size() > 0) {
						rh.resolved(o.get(0));
						return;
					}
				}
				rh.resolved(null);
			}
		});
	}

}
