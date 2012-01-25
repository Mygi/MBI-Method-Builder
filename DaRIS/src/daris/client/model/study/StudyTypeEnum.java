package daris.client.model.study;

import java.util.List;
import java.util.Vector;

import arc.mf.client.util.Transformer;
import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class StudyTypeEnum implements DynamicEnumerationDataSource<StudyType> {

	private static class StudyTypeTransformer extends
			Transformer<StudyType, Value<StudyType>> {

		public static final StudyTypeTransformer INSTANCE = new StudyTypeTransformer();

		private StudyTypeTransformer() {

		}

		@Override
		protected Value<StudyType> doTransform(StudyType st) throws Throwable {

			if (st == null) {
				return null;
			}
			return new Value<StudyType>(st.name(), st.name()
					+ (st.description() == null ? ""
							: (": " + st.description())), st);
		}
	}

	private static class StudyTypeListTransformer extends
			Transformer<List<StudyType>, List<Value<StudyType>>> {

		public static final StudyTypeListTransformer INSTANCE = new StudyTypeListTransformer();

		private StudyTypeListTransformer() {

		}

		@Override
		protected List<Value<StudyType>> doTransform(List<StudyType> sts)
				throws Throwable {

			if (sts == null) {
				return null;
			}
			if (sts.isEmpty()) {
				return null;
			}
			List<Value<StudyType>> stvs = new Vector<Value<StudyType>>(
					sts.size());
			for (StudyType st : sts) {
				stvs.add(StudyTypeTransformer.INSTANCE.transform(st));
			}
			return stvs;
		}

	}

	private StudyTypeListRef _sts;

	public StudyTypeEnum() {

		this(null);
	}

	public StudyTypeEnum(String exMethodId) {

		_sts = new StudyTypeListRef(exMethodId);
	}

	@Override
	public boolean supportPrefix() {

		return false;
	}

	@Override
	public void exists(String value,
			final DynamicEnumerationExistsHandler handler) {

		final String name = value;
		_sts.resolve(new ObjectResolveHandler<List<StudyType>>() {
			@Override
			public void resolved(List<StudyType> studyTypes) {

				if (studyTypes != null) {
					for (StudyType st : studyTypes) {
						if (st.name().equals(name)) {
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
			final DynamicEnumerationDataHandler<StudyType> handler) {

		_sts.resolve(new ObjectResolveHandler<List<StudyType>>() {
			@Override
			public void resolved(List<StudyType> sts) {

				if (sts == null) {
					handler.process(0, 0, 0, null);
					return;
				}
				if (sts.isEmpty()) {
					handler.process(0, 0, 0, null);
					return;
				}
				List<Value<StudyType>> stvs = StudyTypeListTransformer.INSTANCE
						.transform(sts);
				List<Value<StudyType>> rstvs = stvs;
				int start1 = (int) start;
				int end1 = (int) end;
				long total = stvs.size();
				if (start1 > 0 || end1 < stvs.size()) {
					if (start1 >= stvs.size()) {
						rstvs = null;
					} else {
						if (end1 > stvs.size()) {
							end1 = stvs.size();
						}
						rstvs = stvs.subList(start1, end1);
					}
				}
				handler.process(start1, end1, total, rstvs);
			}
		});
	}
}