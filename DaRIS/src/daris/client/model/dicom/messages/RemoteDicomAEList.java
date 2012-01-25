package daris.client.model.dicom.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.dicom.DicomAE;

public class RemoteDicomAEList extends ObjectMessage<List<DicomAE>> {

	public RemoteDicomAEList() {

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("type", "remote");
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.dicom.ae.list";
	}

	@Override
	protected List<DicomAE> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> aes = xe.elements("ae");
			if (!aes.isEmpty()) {
				List<DicomAE> as = new Vector<DicomAE>();
				for (XmlElement ae : aes) {
					as.add(new DicomAE(ae));
				}
				return as;
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return null;
	}

	@Override
	protected String idToString() {

		return null;
	}

}
