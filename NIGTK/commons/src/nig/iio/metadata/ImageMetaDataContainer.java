package nig.iio.metadata;

import arc.xml.XmlDoc;
import nig.iio.bruker.NIGBrukerIdentifierMetaData;
import nig.iio.dicom.StudyMetadata;


/**
 * Container to hold DICOM and Bruker meta-data
 * The Bruker meta-data is just a small subset parsed out of SUbject identifiers by the Bruker client
 * It is specific to the Neuroimaging group but has to be in the commons package as the Bruker client is generic
 * 
 * This container is used in the framework that the DICOM server and Bruker client
 * use to locate meta-data on Subjects in research domain specific processes (See class DomainMetaManager)
 * 
 * Other private containers could be added.
 * 
 * @author nebk
 *
 */
public class ImageMetaDataContainer {
	
	private StudyMetadata _dicomMeta = null;                     // DICOM
	private NIGBrukerIdentifierMetaData _brukerMeta = null;      // Bruker

	

	public ImageMetaDataContainer (StudyMetadata dicomStudyMetaData) throws Throwable {
		_dicomMeta = dicomStudyMetaData;
	}

	public ImageMetaDataContainer (NIGBrukerIdentifierMetaData brukerIdentifierMetaData) throws Throwable {
		_brukerMeta = brukerIdentifierMetaData;
	}

	
	
	public StudyMetadata dicomMetaData () {
		return _dicomMeta;
	}
	
	public NIGBrukerIdentifierMetaData brukerMetaData () {
		return _brukerMeta;
	}
	
	public boolean hasDicomMetaData () {
		return _dicomMeta != null;
	}
	
	public boolean hasBrukerMetaData () {
		return _brukerMeta != null;
	}
	
	/**
	 * Convert to XML.  Only implemented for StudyMetadata container at this point
	 * 
	 * @return
	 * @throws Throwable
	 */
	public XmlDoc.Element toXML () throws Throwable {
		if (_dicomMeta != null) {
			return _dicomMeta.toXML();
		} else if (_brukerMeta != null) {
			// TODO: implement if needed. See pattern in StudyMetadata
			throw new Exception("Function not implemented");
		} else {
			return null;
		}
	}
	
	/**
	 * Reconstruct from XML.   Only implemented for StudyMetadata container at this point

	 * @param meta
	 * @throws Throwable
	 */
	public void fromXML (XmlDoc.Element meta) throws Throwable {
		if (_dicomMeta != null) {
			_dicomMeta = StudyMetadata.createFrom (meta);
		} else if (_brukerMeta != null) {
			// TODO: implement if needed. See pattern in StudyMetadata
			throw new Exception("Function not implemented");
		} else {
			_dicomMeta = null;
			_brukerMeta = null;
		}
	}
}
