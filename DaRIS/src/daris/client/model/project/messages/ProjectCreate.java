package daris.client.model.project.messages;

import daris.client.model.object.messages.DObjectCreate;
import daris.client.model.project.Project;
import daris.client.model.repository.RepositoryRef;

public class ProjectCreate extends DObjectCreate {

	public ProjectCreate(Project o) {

		super(RepositoryRef.INSTANCE, o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.create";
	}

}
