package daris.model.datause;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class DataUseSetRef extends ObjectRef<DataUseSet> {

	private static DataUseSetRef _instance;

	public static DataUseSetRef get() {

		if (_instance == null) {
			_instance = new DataUseSetRef();
		}
		return _instance;

	}

	private DataUseSetRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.project.data-use.roles";

	}

	@Override
	protected DataUseSet instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			if(xe.values("data-use")!=null){
				return new DataUseSet(xe);
			}
		}
		return null;

	}

	@Override
	public String referentTypeName() {

		return "DataUseSet";

	}

	@Override
	public String idToString() {

		return "DataUseSet";

	}

}
