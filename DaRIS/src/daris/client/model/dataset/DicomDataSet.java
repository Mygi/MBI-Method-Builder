package daris.client.model.dataset;

import arc.mf.client.xml.XmlElement;

public class DicomDataSet extends DerivationDataSet {

	private int _size;

	public DicomDataSet(XmlElement ddse) {

		super(ddse);
		try {
			_size = ddse.intValue("meta/mf-dicom-series/size", 0);
		} catch (Throwable e) {
			_size = 0;
		}
	}

	public int size() {

		return _size;
	}

}
