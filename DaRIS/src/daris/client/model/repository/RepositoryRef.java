package daris.client.model.repository;

import daris.client.model.object.DObject;
import daris.client.model.object.DObjectRef;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;

public class RepositoryRef extends DObjectRef {

	public static final RepositoryRef INSTANCE = new RepositoryRef();

	private RepositoryRef() {

		super(null, null, false, false);
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

	}
	
	@Override
	protected void resolveServiceArgs(XmlStringWriter w, boolean lock) {
		
	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.repository.describe";
	}

	@Override
	protected DObject instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement re = xe.element("repository");
			return new Repository(re);
		}
		return null;
	}

	@Override
	public boolean resolved() {

		return referent() != null;
	}

	@Override
	public String referentTypeName() {

		return "repository";
	}

	@Override
	public String idToString() {

		return null;
	}
	
}
