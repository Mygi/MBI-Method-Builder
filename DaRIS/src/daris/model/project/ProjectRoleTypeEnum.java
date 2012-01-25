package daris.model.project;

import java.util.List;
import java.util.Vector;

import arc.mf.dtype.DynamicEnumerationDataHandler;
import arc.mf.dtype.DynamicEnumerationDataSource;
import arc.mf.dtype.DynamicEnumerationExistsHandler;
import arc.mf.dtype.EnumerationType.Value;
import arc.mf.object.ObjectResolveHandler;

public class ProjectRoleTypeEnum implements
		DynamicEnumerationDataSource<String> {
	
	private static ProjectRoleTypeEnum _instance;

	public static ProjectRoleTypeEnum instance() {

		if (_instance != null) {
			_instance = new ProjectRoleTypeEnum();
		}
		return _instance;

	}

	private ProjectRoleTypeEnum() {

	}

	@Override
	public boolean supportPrefix() {

		return false;

	}

	@Override
	public void exists(final String roleType,
			final DynamicEnumerationExistsHandler handler) {

		ProjectRoleTypesRef.instance().resolve(
				new ObjectResolveHandler<List<String>>() {

					@Override
					public void resolved(List<String> roleTypes) {

						if (roleTypes != null) {
							if (roleTypes.contains(roleType)) {
								handler.exists(roleType, true);
								return;
							}
						}
						handler.exists(roleType, false);
					}
				});

	}

	@Override
	public void retrieve(String prefix, final long start, final long end,
			final DynamicEnumerationDataHandler<String> handler) {

		ProjectRoleTypesRef.instance().resolve(
				new ObjectResolveHandler<List<String>>() {

					@Override
					public void resolved(List<String> roleTypes) {

						if (roleTypes != null) {
							Vector<Value<String>> values = new Vector<Value<String>>(
									roleTypes.size());
							for (String roleType : roleTypes) {
								values.add(new Value<String>(roleType,
										roleType, roleType));
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
