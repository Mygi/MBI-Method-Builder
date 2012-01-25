package daris.model.exmethod;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;
import daris.model.study.StudyRef;

public class StepEnum implements DynamicEnumerationDataSource<String> {

	private StepsRef _steps;

	public StepEnum(ExMethodRef xm) {
		_steps = new StepsRef(xm);
	}

	public StepEnum(StudyRef study) {
		_steps = new StepsRef(study);
	}

	/**
	 * 
	 * @param id
	 *            ex-method id
	 * @param type
	 *            study type
	 */
	public StepEnum(String id, String type) {
		_steps = new StepsRef(id, type);
	}

	@Override
	public boolean supportPrefix() {
		return false;
	}

	@Override
	public void exists(final String step,
			final DynamicEnumerationExistsHandler handler) {
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

				if (ses != null) {
					Vector<Value<String>> values = new Vector<Value<String>>(
							ses.size());
					for (XmlElement se : ses) {
						values.add(new Value<String>(se.value() + ": "
								+ se.value("@type"), "step: " + se.value()
								+ "; type: " + se.value("@type"), se.value()));
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
