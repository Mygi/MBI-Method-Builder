package daris.model.project.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectMeta;
import daris.model.project.ProjectMeta;

public class ProjectMetaDescribe extends  ObjectMessage<PSSDObjectMeta> {

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		// NO Arguments
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.metadata.describe";
	}

	@Override
	protected PSSDObjectMeta instantiate(XmlElement xe) throws Throwable {

		if(xe!=null){
			return new ProjectMeta(xe);
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "Project Metadata Definition";
	}

	@Override
	protected String idToString() {

		return "project-metadata-definition";
	}
}
