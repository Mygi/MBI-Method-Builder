package daris.client.model.dicom;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class DicomIngestFCPRef extends ObjectRef<DicomIngestFCP> {

	public static DicomIngestFCPRef INSTANCE = new DicomIngestFCPRef();

	private DicomIngestFCPRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {
		// No arguments required.
	}

	@Override
	protected String resolveServiceName() {
		return "om.pssd.dicom.ingest.fcp.get";
	}

	@Override
	protected DicomIngestFCP instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			String fcp = xe.value("fcp");
			if (fcp != null) {
				return new DicomIngestFCP(fcp);
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {
		return "fcp";
	}

	@Override
	public String idToString() {
		return null;
	}

}
