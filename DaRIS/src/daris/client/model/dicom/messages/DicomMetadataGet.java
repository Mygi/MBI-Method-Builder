package daris.client.model.dicom.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.DicomElement;

public class DicomMetadataGet extends ObjectMessage<List<DicomElement>> {

	private String _assetId;
	private int _index;

	public DicomMetadataGet(String assetId) {

		this(assetId, 0);
	}

	public DicomMetadataGet(String assetId, int index) {

		_assetId = assetId;
		_index = index;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		String idx = "" + _index;
		w.add("id", new String[] { "idx", idx }, _assetId);
		w.add("defn", true);

	}

	@Override
	protected String messageServiceName() {

		return "dicom.metadata.get";
	}

	@Override
	protected List<DicomElement> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> des = xe.elements("de");
			if (des != null) {
				if (!des.isEmpty()) {
					List<DicomElement> es = new Vector<DicomElement>();
					for (XmlElement de : des) {
						es.add(new DicomElement(de));
					}
					return es;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "DICOM Metadata";
	}

	@Override
	protected String idToString() {

		return _assetId + "_" + _index;
	}

}
