package daris.client.model.dicom;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class LocalAERef extends ObjectRef<DicomAE> {

	private static LocalAERef _instance;

	public static LocalAERef instance() {

		if (_instance == null) {
			_instance = new LocalAERef();
		}
		return _instance;
	}

	private LocalAERef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("type", "local");

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.dicom.ae.list";
	}

	@Override
	protected DicomAE instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement ae = xe.element("ae[@type='local']");
			return new DicomAE(ae);
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return null;
	}

	@Override
	public String idToString() {

		return null;
	}

}
