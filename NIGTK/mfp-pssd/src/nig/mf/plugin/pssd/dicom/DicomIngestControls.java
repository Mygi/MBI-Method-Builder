package nig.mf.plugin.pssd.dicom;

import java.util.*;

/**
 * Ingestion controls for the NIG.DICOM asset processor.
 * 
 * @author Jason Lohrey
 *
 */
public class DicomIngestControls {
	
	/**
	 * Don't identify.
	 */
	public static final int ID_NONE = 0;

	/**
	 * Uniquely identify patient by DICOM element (0x0010,0x0020).
	 */
	public static final int ID_BY_PATIENT_ID = 1;

	/**
	 * Uniquely identify patient by DICOM element (0x0010,0x0010).
	 */
	public static final int ID_BY_PATIENT_FULL_NAME = 2;
	
	/**
	 * Uniquely identify patient by first name from DICOM element (0x0010,0x0010).
	 */
	public static final int ID_BY_PATIENT_FIRST_NAME = 3;

	/**
	 * Uniquely identify patient by last name from DICOM element (0x0010,0x0010).
	 */
	public static final int ID_BY_PATIENT_LAST_NAME = 4;

	/**
	 * Uniquely identify by last name from DICOM element (0x0020,0x0010).
	 */
	public static final int ID_BY_STUDY_ID = 5;
	

	
	public static class ExInvalidSetting extends Throwable {
		public ExInvalidSetting(String setting,String error) {
			super("NIG.DICOM: invalid argument [" + setting + "]: " + error);
		}
	}
	
	private int[]     _cidElements;
	private String    _cidPrefix;
	private int       _minCidDepth;
	private boolean   _ignoreNonDigits;
	private String    _ns;
	private String 	  _citableID;
	private Boolean   _autoSubjectCreate;
	private String    _subjectMetaService;
	
	public DicomIngestControls() {
		_ns = null;
		_cidElements     = null;
		_citableID = null;
		_autoSubjectCreate = false;
		
		// Minimum CID depth to be considered a CID..
		_minCidDepth  = 3;
		_ignoreNonDigits = false;
		_cidPrefix = null;
	}
	
	public String namespace() {
		return _ns;
	}
	
	public String cidPrefix() {
		return _cidPrefix;
	}
	
	public int minCidDepth() {
		return _minCidDepth;
	}
	
	public int[] cidElements() {
		return _cidElements;
	}
	
	public boolean ignoreNonDigits() {
		return _ignoreNonDigits;
	}
	public String citableID () {
		return _citableID;
	}
	public boolean autoSubjectCreate () {
		return _autoSubjectCreate;
	}
	public String subjectMetaService () {
		return _subjectMetaService;
	}
	
	/**
	 * Configure controls by reading either directly from the command line (e.g. dicom.ingest :arg -name nig.dicom.id.citable 1.2.3.4)
	 * or from the network configuration
	 * 
	 * @param args
	 * @throws Throwable
	 */
	protected void configure(Map<String,String> args) throws Throwable {
		
		// Root namespace for storing data.
		_ns = (String)args.get("nig.dicom.asset.namespace.root");
		
		//Either the Citable ID is directly specified by the caller or it is
		// extracted from the DICOM metadata
		_citableID = (String)args.get("nig.dicom.id.citable");
		if (_citableID == null) {
			String idBy = (String)args.get("nig.dicom.id.by");
			if ( idBy != null ) {
				StringTokenizer st = new StringTokenizer(idBy,",");
				_cidElements = new int[st.countTokens()];
			
				int i = 0;
				while ( st.hasMoreTokens() ) {
					String tok = st.nextToken();
				
					if ( tok.equalsIgnoreCase("patient.id") ) {
						_cidElements[i] = ID_BY_PATIENT_ID;
					} else if ( tok.equalsIgnoreCase("patient.name") ) {
						_cidElements[i] = ID_BY_PATIENT_FULL_NAME;
					} else if ( tok.equalsIgnoreCase("patient.name.first") ) {
						_cidElements[i] = ID_BY_PATIENT_FIRST_NAME;
					} else if ( tok.equalsIgnoreCase("patient.name.last") ) {
						_cidElements[i] = ID_BY_PATIENT_LAST_NAME;
					} else if ( tok.equalsIgnoreCase("study.id") ) {
						_cidElements[i] = ID_BY_STUDY_ID;
					} else {
						throw new ExInvalidSetting("nig.dicom.id.by","expected one of [patient.id, patient.name, patient.name.first, patient.name.last, study.id, citable.id] for id.patient.by - found: " + idBy);
					}
				
					i++;
				}
			
				String ignoreChars = (String)args.get("nig.dicom.id.ignore-non-digits");
				if ( ignoreChars != null ) {
					if ( ignoreChars.equalsIgnoreCase("true") ) {
						_ignoreNonDigits = true;
					} else if ( ignoreChars.equalsIgnoreCase("false") ) {
						_ignoreNonDigits = false;
					} else {
						throw new ExInvalidSetting("nig.dicom.id.ignore-non-digits","expected one of [true,false] - found: " + ignoreChars);
					}
				}	
			}
		}
		
		_cidPrefix = (String)args.get("nig.dicom.id.prefix");
		
		// Auto SUbject creation
		String subjectCreate = (String)args.get("nig.dicom.subject.create");
		if ( subjectCreate != null ) {
			if ( subjectCreate.equalsIgnoreCase("true") ) {
				_autoSubjectCreate = true;
			} else if ( subjectCreate.equalsIgnoreCase("false") ) {
				_autoSubjectCreate = false;
			} else {
				throw new ExInvalidSetting("nig.dicom.subject.create","expected one of [true,false] - found: " + subjectCreate);
			}
		}	

		// Service to update meta-data by parsing the DICOM meta-data in a domain-specific way
		_subjectMetaService = (String)args.get("nig.dicom.subject.meta.set-service");
		
	}
}
