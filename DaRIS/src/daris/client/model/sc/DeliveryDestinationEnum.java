package daris.client.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class DeliveryDestinationEnum implements
		DynamicEnumerationDataSource<DeliveryDestination> {

	private static DeliveryDestinationEnum _instance;

	public static DeliveryDestinationEnum instance() {
		if (_instance == null) {
			_instance = new DeliveryDestinationEnum();
		}
		return _instance;
	}

	public static void reset() {
		if (_instance != null) {
			_instance._dests.reset();
		}
	}

	private DeliveryDestinationListRef _dests;

	private DeliveryDestinationEnum() {
		_dests = new DeliveryDestinationListRef();
	}

	@Override
	public boolean supportPrefix() {
		return false;
	}

	@Override
	public void exists(final String name,
			final DynamicEnumerationExistsHandler handler) {
		if (name == null) {
			handler.exists(name, false);
			return;
		}
		_dests.resolve(new ObjectResolveHandler<List<DeliveryDestination>>() {

			@Override
			public void resolved(List<DeliveryDestination> dests) {
				if (dests != null) {
					for (DeliveryDestination d : dests) {
						if (name.equals(d.name())) {
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
	public void retrieve(final String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<DeliveryDestination> handler) {
		_dests.resolve(new ObjectResolveHandler<List<DeliveryDestination>>() {

			@Override
			public void resolved(List<DeliveryDestination> dests) {
				if (dests == null) {
					handler.process(0, 0, 0, null);
				} else {
					doRetrieve(start, end, handler, dests);
				}
			}
		});
	}

	private void doRetrieve(long start, long end,
			DynamicEnumerationDataHandler<DeliveryDestination> handler,
			List<DeliveryDestination> ds) {

		if (ds != null) {
			List<Value<DeliveryDestination>> vs = new Vector<Value<DeliveryDestination>>(
					ds.size());
			for (DeliveryDestination d : ds) {
				Value<DeliveryDestination> v = new Value<DeliveryDestination>(
						d.name(), d.name(), d);
				vs.add(v);
			}
			List<Value<DeliveryDestination>> rvs = vs;
			int start1 = (int) start;
			int end1 = (int) end;
			long total = vs.size();
			if (start1 > 0 || end1 < vs.size()) {
				if (start1 >= vs.size()) {
					rvs = null;
				} else {
					if (end1 > vs.size()) {
						end1 = vs.size();
					}
					rvs = vs.subList(start1, end1);
				}
			}
			handler.process(start1, end1, total, rvs);
			return;
		}
		handler.process(0, 0, 0, null);
	}

}
