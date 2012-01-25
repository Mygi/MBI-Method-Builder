package daris.model.method;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;
import daris.model.object.PSSDObject;
import daris.model.project.Project;
import daris.model.project.ProjectRef;

public class MethodEnum implements DynamicEnumerationDataSource<MethodRef> {

	private ProjectRef _project;

	public MethodEnum(String proute, String projectId) {

		this(new ProjectRef(proute, projectId));
	}

	public MethodEnum(ProjectRef project) {

		this._project = project;
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void retrieve(final String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<MethodRef> handler) {

		if (_project != null) {
			_project.reset();
			_project.resolve(new ObjectResolveHandler<PSSDObject>() {

				@Override
				public void resolved(PSSDObject o) {

					if (o != null) {
						List<Method> ms = ((Project) o).methods();
						if (ms != null) {
							List<MethodRef> mrs = new Vector<MethodRef>(ms
									.size());
							for (Method m : ms) {
								mrs.add(new MethodRef(m.id(), m.name(), m
										.description()));
							}
							if (mrs.size() > 0) {
								doRetrieve(prefix, start, end, handler, mrs);
								return;
							}
						}
					}
					handler.process(0, 0, 0, null);
				}
			});
		} else {
			MethodsRef methods = MethodsRef.instance();
			methods.reset();
			methods.resolve(new ObjectResolveHandler<List<MethodRef>>() {

				@Override
				public void resolved(List<MethodRef> mrs) {

					if (mrs != null) {
						doRetrieve(prefix, start, end, handler, mrs);
						return;
					}
					handler.process(0, 0, 0, null);
				}
			});
		}
	}

	private void doRetrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<MethodRef> handler,
			List<MethodRef> mrs) {

		Vector<Value<MethodRef>> vs = new Vector<Value<MethodRef>>(mrs.size());
		for (MethodRef mr : mrs) {
			vs.add(new Value<MethodRef>(mr.id() + ": " + mr.name(), mr
					.description(), mr));
		}
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
	}

	@Override
	public void exists(String value,
			final DynamicEnumerationExistsHandler handler) {

		final String id = value.split(":")[0];
		if (_project != null) {
			_project.resolve(new ObjectResolveHandler<PSSDObject>() {

				@Override
				public void resolved(PSSDObject o) {

					if (o != null) {
						List<Method> ms = ((Project) o).methods();
						if (ms != null) {
							for (Method m : ms) {
								if (m.id().equals(id)) {
									handler.exists(id, true);
									return;
								}
							}

						}
					}
					handler.exists(id, false);
				}
			});
		} else {
			MethodsRef methods = MethodsRef.instance();
			methods.resolve(new ObjectResolveHandler<List<MethodRef>>() {

				@Override
				public void resolved(List<MethodRef> mrs) {

					if (mrs != null) {
						for (MethodRef mr : mrs) {
							if (mr.id().equals(id)) {
								handler.exists(id, true);
								return;
							}
						}
					}
					handler.exists(id, false);
				}
			});
		}
	}
}
