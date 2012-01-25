package daris.client.model.project.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class ProjectMetadataDescribe extends ObjectMessage<XmlElement> {

	public ProjectMetadataDescribe() {

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.project.metadata.describe";
	}

	@Override
	protected XmlElement instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			return xe.element("meta");
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "project metadata definition";
	}

	@Override
	protected String idToString() {

		return null;
	}

}
