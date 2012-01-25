package nig.iio.dicom;


import java.util.Date;


import arc.mf.plugin.dicom.DataElementMap;
import arc.mf.plugin.dicom.DicomDateTime;


public class SeriesMetadata {

	private String _id;
	private String _uid;
	private String _protocol;
	private String _description;
	private String _modality;
	private Date   _date;
	private double [] _image_position;
	private double [] _image_orientation;
	
	/**
	 * Create the  metadata to be stored with a series
	 * 
	 * @param dem
	 * @return
	 * @throws Throwable
	 */
	public static SeriesMetadata createFrom(DataElementMap dem) throws Throwable {
		SeriesMetadata sm = new SeriesMetadata();
		sm.restore(dem);
		return sm;
	}
	
	public String UID() {
		return _uid;
	}
	
	public String id() {
		return _id;
	}

	public String protocol() {
		return _protocol;
	}
	
	public String description() {
		return _description;
	}
	
	public Date creationTime() {
		return _date;
	}
	
	public String modality() {
		return _modality;
	}
	public double [] imagePosition () {
		return _image_position;
	}
	public double [] imageOrientation () {
		return _image_orientation;
	}
	
	
	private void restore(DataElementMap dem) throws Throwable {
		_uid = dem.stringValue(DicomElements.SERIES_UID);
		_id = dem.stringValue(DicomElements.SERIES_NUMBER);

		Date   seriesDate = dem.dateValue(DicomElements.SERIES_DATE);
		String seriesTime = dem.stringValue(DicomElements.SERIES_TIME);
		
		_date = DicomDateTime.dateTime(seriesDate,seriesTime);
		if ( _date == null ) {
			Date   studyDate = dem.dateValue(DicomElements.STUDY_DATE);
			String studyTime = dem.stringValue(DicomElements.STUDY_TIME);
			
			_date = DicomDateTime.dateTime(studyDate,studyTime);
		}

		_description = dem.stringValue(DicomElements.SERIES_DESCRIPTION);
		// Give ourselves something to hang onto if there was no other description.
		if ( _description != null ) {
			_description = _description.replaceAll("\\^"," - ");
		} 

		_protocol = dem.stringValue(DicomElements.PROTOCOL_NAME);
		_modality = dem.stringValue(DicomElements.MODALITY);
		
		
		// Image orientation parameters. We assume these don't change
		// from slice to slice.
		// New MF doubleValues functions not working yet,

		_image_position = dem.doubleValues(DicomElements.IMAGE_POSITION_PATIENT);
		_image_orientation = dem.doubleValues(DicomElements.IMAGE_ORIENTATION_PATIENT);
	}
}
