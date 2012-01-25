package daris.client.model.method;

import java.util.List;
import java.util.Vector;

import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import daris.client.model.project.Project;

import arc.mf.client.util.Transformer;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class MethodEnum implements DynamicEnumerationDataSource<MethodRef> {

	private static class MethodTransformer extends Transformer<MethodRef, Value<MethodRef>> {

		public static final MethodTransformer INSTANCE = new MethodTransformer();

		private MethodTransformer() {

		}

		@Override
		protected Value<MethodRef> doTransform(MethodRef m) throws Throwable {

			if (m == null) {
				return null;
			}
			return new Value<MethodRef>(m.id() + ": " + m.name(), m.description(), m);
		}

	}

	private static class MethodListTransformer extends Transformer<List<MethodRef>, List<Value<MethodRef>>> {

		public static final MethodListTransformer INSTANCE = new MethodListTransformer();

		private MethodListTransformer() {

		}

		@Override
		protected List<Value<MethodRef>> doTransform(List<MethodRef> ms) throws Throwable {

			if (ms == null) {
				return null;
			}
			if (ms.isEmpty()) {
				return null;
			}
			List<Value<MethodRef>> vs = new Vector<Value<MethodRef>>();
			for (MethodRef m : ms) {
				vs.add(MethodTransformer.INSTANCE.transform(m));
			}
			return vs;
		}
	}

	private MethodListRef _msr;
	private DObjectRef _project;

	public MethodEnum() {

		_project = null;
		_msr = new MethodListRef(true);
	}

	public MethodEnum(DObjectRef project) {

		_project = project;
		_msr = null;
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String value, final DynamicEnumerationExistsHandler handler) {

		if (value == null) {
			handler.exists(value, false);
			return;
		}
		final String id = value.split(":")[0];
		if (_msr != null) {
			_msr.resolve(new ObjectResolveHandler<List<MethodRef>>() {
				@Override
				public void resolved(List<MethodRef> ms) {

					handler.exists(value, contains(ms, id));
				}
			});
		} else {
			// _project!=null
			if (_project.needToResolve()) {
				_project.reset();
			}
			_project.resolve(new ObjectResolveHandler<DObject>() {

				@Override
				public void resolved(DObject o) {

					if (o == null) {
						handler.exists(value, false);
						return;
					}
					List<MethodRef> ms = ((Project) o).methods();
					handler.exists(value, contains(ms, id));
				}
			});
		}
	}

	private static boolean contains(List<MethodRef> ms, String id) {

		if (ms == null) {
			return false;
		}
		if (ms.isEmpty()) {
			return false;
		}
		for (MethodRef m : ms) {
			if (m.id().equals(id)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void retrieve(final String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<MethodRef> handler) {

		if (_msr != null) {
			_msr.reset();
			_msr.resolve(new ObjectResolveHandler<List<MethodRef>>() {

				@Override
				public void resolved(List<MethodRef> ms) {

					doRetrieve(start, end, handler, ms);
				}
			});
		} else {
			// _project!=null
			_project.reset();
			_project.resolve(new ObjectResolveHandler<DObject>() {

				@Override
				public void resolved(DObject o) {

					if (o == null) {
						handler.process(0, 0, 0, null);
						return;
					}
					List<MethodRef> ms = ((Project) o).methods();
					doRetrieve(start, end, handler, ms);
				}
			});

		}
	}

	private void doRetrieve(long start, long end, DynamicEnumerationDataHandler<MethodRef> handler, List<MethodRef> ms) {

		if (ms != null) {
			List<Value<MethodRef>> vs = MethodListTransformer.INSTANCE.transform(ms);
			List<Value<MethodRef>> rvs = vs;
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
