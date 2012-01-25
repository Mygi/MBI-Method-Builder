package daris.client.model.exmethod;

import java.util.List;
import java.util.Vector;

import daris.client.model.study.Study;

import arc.mf.client.util.Transformer;
import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class StepEnum implements DynamicEnumerationDataSource<String> {

	private static class StepElementTransformer extends Transformer<XmlElement, Value<String>> {

		public static StepElementTransformer INSTANCE = new StepElementTransformer();

		private StepElementTransformer() {

		}

		@Override
		protected Value<String> doTransform(XmlElement se) throws Throwable {

			if (se != null) {
				String step = se.value();
				String type = se.value("@type");
				if (step != null && type != null) {
					return new Value<String>(step + ": " + type, "step: " + step + "; type: " + type, step);
				}
			}
			return null;
		}
	}

	private static class StepElementListTransformer extends Transformer<List<XmlElement>, List<Value<String>>> {

		public static final StepElementListTransformer INSTANCE = new StepElementListTransformer();

		private StepElementListTransformer() {

		}

		@Override
		protected List<Value<String>> doTransform(List<XmlElement> ses) throws Throwable {

			if (ses != null) {
				if (!ses.isEmpty()) {
					List<Value<String>> vs = new Vector<Value<String>>();
					for (XmlElement se : ses) {
						Value<String> v = StepElementTransformer.INSTANCE.transform(se);
						if (v != null) {
							vs.add(v);
						}
					}
					if (!vs.isEmpty()) {
						return vs;
					}
				}
			}
			return null;
		}

	}

	private StepElementListRef _steps;

	public StepEnum(ExMethod exMethod) {

		_steps = new StepElementListRef(exMethod);
	}

	public StepEnum(Study study) {

		_steps = new StepElementListRef(study);
	}

	public StepEnum(String id, String type) {

		_steps = new StepElementListRef(id, type);
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String value, final DynamicEnumerationExistsHandler handler) {

		final String step = value.split(":")[0];
		_steps.resolve(new ObjectResolveHandler<List<XmlElement>>() {

			@Override
			public void resolved(List<XmlElement> ses) {

				if (ses != null) {
					for (XmlElement se : ses) {
						if (se.value().equals(step)) {
							handler.exists(step, true);
							return;
						}
					}
				}
				handler.exists(step, false);
			}
		});

	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<String> handler) {

		_steps.resolve(new ObjectResolveHandler<List<XmlElement>>() {

			@Override
			public void resolved(List<XmlElement> ses) {

				if (ses == null) {
					handler.process(0, 0, 0, null);
					return;
				}
				List<Value<String>> values = StepElementListTransformer.INSTANCE.transform(ses);
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
			}

		});
	}

}
