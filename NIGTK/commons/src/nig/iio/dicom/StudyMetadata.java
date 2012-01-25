package nig.iio.dicom;

import java.util.Date;

import arc.mf.plugin.dicom.DataElementMap;
import arc.mf.plugin.dicom.DicomDateTime;
import arc.mf.plugin.dicom.DicomPersonName;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.iio.dicom.DicomElements;

public class StudyMetadata {

	private String _uid;
	private String _id;

	private String _desc;
	private Date   _date;

	private String _modality;
	private String _patientID;

	private String _rpn;
	private String _institution;
	private String _station;
	private String _manufacturer;

	private boolean _havePatientDetails;
	private DicomPersonName _patientName;
	private Date    _dob;
	private double  _patientAge;
	private double  _patientWeight;
	private double  _patientLength;
	private String  _patientSex;

	/**
	 * Create the study metadata to be stored with a study.
	 * 
	 * @param dem
	 * @return
	 * @throws Throwable
	 */
	public static StudyMetadata createFrom(DataElementMap dem) throws Throwable {
		StudyMetadata sm = new StudyMetadata();
		sm.restore(dem);
		return sm;
	}

	/**
	 * Create the study meta-data from an Xml element. USed when resconstructing
	 * from the service level interface
	 * 
	 * @param dem
	 * @return
	 * @throws Throwable
	 */
	public static StudyMetadata createFrom(XmlDoc.Element meta) throws Throwable {
		StudyMetadata sm = new StudyMetadata();
		sm.restore(meta);
		return sm;
	}


	public String UID() {
		return _uid;
	}

	public String id() {
		return _id;
	}

	public String institution() {
		return _institution;
	}

	public String station() {
		return _station;
	}

	public String manufacturer () {
		return _manufacturer;
	}

	/**
	 * Hospital ID.
	 * 
	 * @return
	 */
	public String patientID() {
		return _patientID;
	}

	/**
	 * Date of study.
	 * 
	 * @return
	 */
	public Date date() {
		return _date;
	}

	/**
	 * Data modality.
	 * 
	 * @return
	 */
	public String modality() {
		return _modality;
	}

	public String description() {
		return _desc;
	}

	/** 
	 * Any patient specific details set in the DICOM data..
	 * 
	 * @return
	 */
	public boolean havePatientDetails() {
		return _havePatientDetails;
	}

	/**
	 * Referring physician.
	 * 
	 * @return
	 */
	public String rpn() {
		return _rpn;
	}

	public DicomPersonName patientName() {
		return _patientName;
	}

	public Date patientDateOfBirth() {
		return _dob;
	}

	/**
	 * Length in metres.
	 * 
	 * @return
	 */
	public double patientLength() {
		return _patientLength;
	}

	/**
	 * Weight in KGs.
	 * 
	 * @return
	 */
	public double patientWeight() {
		return _patientWeight;
	}

	/**
	 * Age in years.
	 * 
	 * @return
	 */
	public double patientAge() {
		return _patientAge;
	}

	public String patientSex() {
		return _patientSex;
	}

	/**
	 * Convert to an XmlDoc.Element for transmission to the service layer
	 * 
	 * @return
	 * @throws Throwable
	 */
	public XmlDoc.Element toXML () throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("dicom");
		if (_uid!=null) dm.add("uid", _uid);
		if (_id!=null) dm.add("id", _id);
		if (_desc!=null) dm.add("description", _desc);
		if (_date!=null) dm.add("date", _date);
		if (_modality!=null) dm.add("modality", _modality);
		if (_rpn!=null) dm.add("rpn", _rpn);
		if (_institution!=null) dm.add("institution", _institution);
		if (_station!=null) dm.add("station", _station);
		if (_manufacturer!=null) dm.add("manufacturer", _manufacturer);
		//
		dm.push("subject");
		// These ones are in mf-dicom-study
		if (_patientSex!=null) dm.add("sex", _patientSex);

		if (_patientAge>0.0) dm.add("age", _patientAge);
		if (_patientWeight>0.0) dm.add("weight", _patientWeight);
		if (_patientLength>0.0) dm.add("size", _patientLength);

		// These ones are in mf-dicom-patient
		if (_patientID!=null) dm.add("id", _patientID);
		if (_patientName!=null) dm.add("name", _patientName.toString());
		if (_dob!=null) dm.add("dob", _dob);
		dm.pop();
		//
		return dm.root();
	}

	/**
	 * Convert from an XmlDoc.Element from  the service layer interface
	 * 
	 * @return
	 * @throws Throwable
	 */
	private void restore(XmlDoc.Element meta) throws Throwable {
		
		// It could be that only a subset of DICOM elements are provided
		_uid =  meta.value("uid");
		_id = meta.value("id");
		_desc = meta.value("description");
		_date = meta.element("date").hasValue() ? meta.dateValue("date") : null;
		_modality = meta.value("modality");
		_rpn = meta.value("dicom/rpn");
		_institution = meta.value("institution");
		_station = meta.value("station");
		_manufacturer = meta.value ("manufacturer");
		//
		XmlDoc.Element subject = meta.element("subject");
		if (subject!=null) {
			_patientSex = subject.value("sex");
			_patientAge = meta.element("age")!=null ? meta.doubleValue("age") : -1;
			_patientWeight = meta.element("weight")!=null ? meta.doubleValue("weight") : -1;
			_patientLength = meta.element("size")!=null ? meta.doubleValue("size") : -1;
			_patientID = subject.value("id");	
			String t = subject.value("name");
			if (t!=null) {
				_patientName = new DicomPersonName(t, null, null, null, null, null);
			} else {
				_patientName = null;
			}
			_dob = meta.element("dob")!=null ? meta.dateValue("dob") : null;
		}
	}

	private StudyMetadata() {
		_desc = null;
		_date = null;
		_rpn = null;
		_havePatientDetails = false;
		_patientName = null;
		_patientAge = -1;
		_patientWeight = -1;
		_patientLength = -1;
		_patientSex = null;

		_institution = null;
		_station     = null;
		_manufacturer = null;
	}

	private void restore(DataElementMap dem) throws Throwable {
		_uid = dem.stringValue(DicomElements.STUDY_UID);
		_id  = dem.stringValue(DicomElements.STUDY_ID);

		_patientID     = dem.stringValue(DicomElements.PATIENT_ID,"UnknownPatientID");

		// The patient name may have been overridden.
		Object patientNameObj = dem.valueOf(DicomElements.PATIENT_NAME);
		if ( patientNameObj == null ) {
			_patientName = null;
		} else if ( patientNameObj instanceof DicomPersonName ) {
			_patientName = (DicomPersonName)patientNameObj;
		} else {
			_patientName = DicomPersonName.parse(patientNameObj.toString());
		}

		if ( _patientName != null ) {
			_havePatientDetails = true;
		}

		_patientAge    = DicomDateTime.ageStringToYears(dem.stringValue(DicomElements.PATIENT_AGE));
		if ( _patientAge != -1 ) {
			_havePatientDetails = true;
		}

		_patientWeight = dem.doubleValue(DicomElements.PATIENT_WEIGHT,-1);
		if ( _patientWeight != -1 ) {
			_havePatientDetails = true;
		}

		_patientLength = dem.doubleValue(DicomElements.PATIENT_SIZE,-1);
		if ( _patientLength != -1 ) {
			_havePatientDetails = true;
		}

		Date patientBirthDate = dem.dateValue(DicomElements.PATIENT_BIRTH_DATE);
		String patientBirthTime = dem.stringValue(DicomElements.PATIENT_BIRTH_TIME);

		_dob = ( patientBirthDate == null ) ? null : DicomDateTime.dateTime(patientBirthDate,patientBirthTime);
		if ( _dob != null ) {
			_havePatientDetails = true;
		}

		_patientSex    = dem.stringValue(DicomElements.PATIENT_SEX);
		if ( _patientSex != null ) {
			if ( _patientSex.equalsIgnoreCase("m")) {
				_patientSex = "male";
			} else if ( _patientSex.equalsIgnoreCase("f") ) {
				_patientSex = "female";
			} else {
				_patientSex = "other";
			}

			_havePatientDetails = true;
		}

		_desc = dem.stringValue(DicomElements.STUDY_DESCRIPTION);
		if ( _desc != null ) {
			_desc = _desc.replaceAll("\\^"," - ");
		}

		Date   studyDate = dem.dateValue(DicomElements.STUDY_DATE);
		String studyTime = dem.stringValue(DicomElements.STUDY_TIME);

		if ( studyDate == null || studyTime == null ) {
			throw new Exception("No study date and/or time - probably corrupt data.");
		}

		_date = DicomDateTime.dateTime(studyDate,studyTime);

		// String modality  = dem.stringValue(DicomElements.MODALITY);
		_rpn       = dem.stringValue(DicomElements.REFERRING_PHYSICIANS_NAME);

		_institution = dem.stringValue(DicomElements.INSTITUTION_NAME,"UnknownInstitution");
		_station = dem.stringValue(DicomElements.STATION_NAME,"UnknownStation");
		_manufacturer = dem.stringValue(DicomElements.EQUIPMENT_MANUFACTURER, "UnknownManufacturer");
		_modality = dem.stringValue(DicomElements.MODALITY);
	}
}
