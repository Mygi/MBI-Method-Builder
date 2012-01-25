package daris.model.project;

import java.util.List;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class ProjectRoleTypesRef extends ObjectRef<List<String>> {

	private static ProjectRoleTypesRef _instance;

	public static ProjectRoleTypesRef instance() {

		if (_instance == null) {
			_instance = new ProjectRoleTypesRef();
		}
		return _instance;

	}

	private ProjectRoleTypesRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.project.roles";

	}

	@Override
	protected List<String> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			if (xe.values("role") != null) {
				return xe.values("role");
			}
		}
		return null;

	}

	@Override
	public String referentTypeName() {

		return "project.roles";

	}

	@Override
	public String idToString() {

		return "project.roles";

	}

}
