package daris.client.model.dicom;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class DicomAEListRef extends ObjectRef<List<DicomAE>> {

	private static DicomAEListRef _instance;

	public static DicomAEListRef instance() {

		if (_instance == null) {
			_instance = new DicomAEListRef();
		}
		return _instance;
	}

	private DicomAEListRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("type", "remote");

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.dicom.ae.list";
	}

	@Override
	protected List<DicomAE> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> aes = xe.elements("ae");
			if (aes != null) {
				if (!aes.isEmpty()) {
					List<DicomAE> as = new Vector<DicomAE>();
					for (XmlElement ae : aes) {
						as.add(new DicomAE(ae));
					}
					return as;
				}
			}
		}
		return new Vector<DicomAE>();
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
