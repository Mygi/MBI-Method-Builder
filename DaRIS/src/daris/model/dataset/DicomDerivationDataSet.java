package daris.model.dataset;

import arc.mf.client.xml.XmlElement;

public class DicomDerivationDataSet extends DerivationDataSet{

	private int _size;
	
	public DicomDerivationDataSet(XmlElement xe) throws Throwable {
		super(xe);
		_size = xe.intValue("meta/mf-dicom-series/size", 0);
	}
	
	public int size(){
		return _size;
	}

}
