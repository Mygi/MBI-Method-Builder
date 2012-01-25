package daris.model.repository;

import arc.mf.client.xml.XmlElement;
import daris.model.object.PSSDObject;

public class RepositoryRoot extends PSSDObject {

	public static final String TYPE_NAME = "repository";

	public String uuid() {
		return id();
	}

	public String organization() {
		return description();
	}

	public RepositoryRoot(XmlElement xe) throws Throwable {
		this(xe.value("server/@proute"), xe.value("server/uuid"), xe.value("server/name"), xe
				.value("server/organization"));
	}

	public RepositoryRoot(String proute, String uuid, String name, String organization) {
		super(proute, uuid, name, organization, false, 0, false);
	}

	public String pid() {
		return null;
	}

	@Override
	public String typeName() {

		return RepositoryRoot.TYPE_NAME;

	}

}
