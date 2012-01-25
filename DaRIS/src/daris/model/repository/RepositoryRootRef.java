package daris.model.repository;

import arc.mf.client.util.Action;
import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectMessageResponse;
import daris.model.object.PSSDObjectRef;
import daris.model.object.messages.ObjectCreate;
import daris.model.object.messages.ObjectUpdate;
import daris.model.project.ProjectRef;
import daris.model.project.messages.ProjectCreate;

public class RepositoryRootRef extends PSSDObjectRef {

	private static RepositoryRootRef _instance;

	public static RepositoryRootRef instance() {

		if (_instance == null) {
			_instance = new RepositoryRootRef();
		}
		return _instance;

	}

	private String _uuid;

	private String _organization;

	private RepositoryRootRef() {

		super(null, null, null, null, false);
	}

	@Override
	public String idToString() {

		return "root";
	}

	@Override
	protected RepositoryRoot instantiate(XmlElement xe) throws Throwable {

		RepositoryRoot o = new RepositoryRoot(xe);
		_uuid = o.uuid();
		_organization = o.organization();
		setId(o.uuid());
		setName(o.name());
		setDescription(o.organization());
		setProute(o.proute());
		return o;

	}

	public String organization() {

		return _organization;
	}

	@Override
	public String referentTypeName() {

		return "repository";
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

	}

	@Override
	protected String resolveServiceName() {

		return "server.identity";
	}

	public String uuid() {

		return _uuid;
	}

	@Override
	protected ObjectCreate getChildCreateMessage(PSSDObjectRef project) {

		return new ProjectCreate(this, (ProjectRef) project);

	}

	@Override
	protected ObjectUpdate getUpdateMessage() {

		// TODO:
		return null;

	}

	@Override
	public void destroy(final Action action) {

		// DO NOTHING

	}

	@Override
	public void exists(ObjectMessageResponse<Boolean> rh) {

		// The repository root should always exist.
		rh.responded(true);

	}

}
