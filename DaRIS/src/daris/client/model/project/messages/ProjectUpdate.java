package daris.client.model.project.messages;

import daris.client.model.object.messages.DObjectUpdate;
import daris.client.model.project.Project;

public class ProjectUpdate extends DObjectUpdate {

	public ProjectUpdate(Project o) {

		super(o);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.update";
	}

}
