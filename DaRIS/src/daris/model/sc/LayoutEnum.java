package daris.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class LayoutEnum implements DynamicEnumerationDataSource<Layout> {
	private static LayoutEnum _instance;

	public static LayoutEnum get() {

		if (_instance == null) {
			_instance = new LayoutEnum();
		}
		return _instance;
	}

	private LayoutSetRef _lsr;

	private LayoutEnum() {

		_lsr = LayoutSetRef.get();
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String name,
			final DynamicEnumerationExistsHandler handler) {

		_lsr.resolve(new ObjectResolveHandler<List<Layout>>() {
			@Override
			public void resolved(List<Layout> ls) {

				if (ls != null) {
					for (Layout l : ls) {
						if (l.name().equals(name)) {
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
			final DynamicEnumerationDataHandler<Layout> handler) {

		_lsr.resolve(new ObjectResolveHandler<List<Layout>>() {
			@Override
			public void resolved(List<Layout> ls) {

				if (ls != null) {
					Vector<Value<Layout>> vs = new Vector<Value<Layout>>(ls
							.size());
					for (Layout l : ls) {
						vs.add(new Value<Layout>(l.name(), l.type().toString()
								+ (l.pattern() == null ? "" : ": "
										+ l.pattern()), l));
					}
					List<Value<Layout>> rvs = vs;
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

	public void first(final ObjectResolveHandler<Layout> rh) {

		_lsr.resolve(new ObjectResolveHandler<List<Layout>>() {
			@Override
			public void resolved(List<Layout> o) {

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