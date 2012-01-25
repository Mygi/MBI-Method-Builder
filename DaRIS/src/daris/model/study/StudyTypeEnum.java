package daris.model.study;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class StudyTypeEnum implements DynamicEnumerationDataSource<String> {

	private StudyTypesRef _types;

	public StudyTypeEnum() {

		this(null);
	}

	public StudyTypeEnum(String exMethodId) {

		_types = new StudyTypesRef(exMethodId);
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(final String name,
			final DynamicEnumerationExistsHandler handler) {

		_types.resolve(new ObjectResolveHandler<List<StudyType>>() {
			@Override
			public void resolved(List<StudyType> studyTypes) {

				if (studyTypes != null) {
					if (studyTypes.contains(new StudyType(name))) {
						handler.exists(name, true);
						return;
					}
				}
				handler.exists(name, false);
			}
		});
	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<String> handler) {

		_types.resolve(new ObjectResolveHandler<List<StudyType>>() {
			@Override
			public void resolved(List<StudyType> sts) {

				if (sts != null) {
					Vector<Value<String>> vs = new Vector<Value<String>>(sts
							.size());
					for (StudyType st : sts) {
						vs.add(new Value<String>(st.name(), st.name() + ": "
								+ st.description(), st.name()));
					}
					List<Value<String>> rvs = vs;
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
		});
	}
}
