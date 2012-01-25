package nig.iio.metadata;

import arc.xml.*;
import nig.iio.bruker.NIGBrukerIdentifierMetaData;
import nig.iio.dicom.StudyMetadata;
import nig.mf.Executor;

import java.util.Collection;
import java.util.Date;
import java.text.SimpleDateFormat;

/**
 * Supply the NIG animal subject domain meta-data for Studies, Subjects and R-SUbjects
 * to the DICOM server or Bruker client.    The framework is Method driven so that only the meta-data
 * driven by the Method is attached.
 * 
 * 
 * @author nebk
 *
 */
public class NIGMetaData extends DomainMetaData {

	private static final String AMRIF_FACILITY = "aMRIF";
	private static final String RCH_FACILITY = "RCH";
	private static final String DATE_FORMAT = "dd-MMM-yyyy";
     
	// Constructor
	public NIGMetaData () {
		//
	}
	
	/**
	 * Update the meta-data on the  object for the given Document Type. This function must
	 * do the actual update with the appropriate service (e.g. om.pssd.subject.update).
	 * This function over-rides the default implementation.
	 * 
	 * @param id The citeable ID of the object to update
	 * @param sm The DICOM Study Metadata or Bruker identifier metadata 
	 * @param privacyType "public", "private", "identity" indicating which element of the meta-data structure
	 *          we are working with. This is needed when re-setting meta-data
	 * @param docType the document type to write meta-data for.  The values must be mapped from the Study MetaData
	 * @param currentMeta  The meta-data that are attached to the asset (:foredit false)
	 * @throws Throwable
	 */
    protected void addTranslatedSubjectDocument (Executor executor, String id, ImageMetaDataContainer sm, String privacyType, 
    		String docType, XmlDoc.Element currentMeta) throws Throwable {
    	
    	String type = typeOf(executor, id);
    	if (type.equals("subject")) {                    // We may generalize to other objects
    		XmlDocMaker dm = null;
    		if (docType.equals("hfi.pssd.identity")) {
    			if (checkDocTypeExists(executor, "hfi.pssd.identity")) {
    				dm = new XmlDocMaker("args");
    				dm.add("id", id);
    				dm.push(privacyType);
    				boolean doIt = addPSSDIdentityOuter (sm, currentMeta, dm);
    				if (!doIt) dm = null;
    			}
    		} else if (docType.equals("hfi.pssd.animal.subject")) {
    			if (checkDocTypeExists(executor, "hfi.pssd.identity")) {
    				dm = new XmlDocMaker("args");
    				dm.add("id", id);
    				dm.push(privacyType);
    				boolean doIt = addPSSDAnimalSubjectOuter (sm, currentMeta, dm);
    				if (!doIt) dm = null;
    			}
    		}  	

    		// Update the SUbject
    		if (dm!=null) {
    			updateSubject(executor, dm);
    		}

    	} else {
    		throw new Exception("Meta-data updates are currently only supported for Subject objects ");
    	}
    }

	
	private String typeOf(Executor executor, String cid) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", cid);
		XmlDoc.Element r = executor.execute("asset.exists", dm);
		if (!r.booleanValue("exists")) return null;

		dm.add("pdist",0);                 // Force local 	
		r = executor.execute("asset.get", dm);
		return r.stringValue("asset/meta/pssd-object/type", "unknown");
	}

	
	private boolean checkDocTypeExists (Executor executor, String docType) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", docType);
		XmlDoc.Element r = executor.execute("asset.doc.type.exists", dm);
		return r.booleanValue("exists");
    }
    
    private void updateSubject (Executor executor, XmlDocMaker dm) throws Throwable {
    	executor.execute("om.pssd.subject.update", dm);
    }



	private boolean addPSSDIdentityOuter ( ImageMetaDataContainer sm,  XmlDoc.Element currentMeta,  XmlDocMaker dm) throws Throwable {

		if (sm.hasDicomMetaData()) {
			return addPSSDIdentityDICOM (sm.dicomMetaData(), currentMeta, dm);
		} else if (sm.hasBrukerMetaData()) {
			return addPSSDIdentityBruker (sm.brukerMetaData(), currentMeta, dm);
		}
		return false;
	}
	
	private boolean addPSSDAnimalSubjectOuter (ImageMetaDataContainer meta,  XmlDoc.Element currentMeta,  XmlDocMaker dm) throws Throwable {

		if (meta.hasDicomMetaData()) {
			StudyMetadata sm = meta.dicomMetaData();
			
			// Get DICOM values. Convert DOB to String of correct format
			Date dob = sm.patientDateOfBirth();
			SimpleDateFormat df = new SimpleDateFormat(DATE_FORMAT);
			String dobString = null;
			if (dob != null) dobString = df.format(dob).toString();
			String gender = sm.patientSex();
			//
			return addPSSDAnimalSubject (dobString, gender, currentMeta, dm);
		} else if (meta.hasBrukerMetaData()) {
			NIGBrukerIdentifierMetaData sm = meta.brukerMetaData();
			//
			String dob =null;      //  Not available
			//
			String gender = sm.gender();
			if (gender.equalsIgnoreCase("M")) {
				gender = "male";
			} else if (gender.equalsIgnoreCase("F")) {
				gender = "female";
			} else {
				gender = "unknown";
			}
			return addPSSDAnimalSubject (dob, gender, currentMeta, dm);
		}
		return false;
	}

		
	/**
	 * Function to add the Subject ID (DICOM element (0010,0020)) to
	 * the SUbject meta-data if it does not already exist
	 * not already exist
	 * 
	 * @param executor
	 * @param currentMeta
	 * @param cid
	 * @throws Throwable
	 */	
	private boolean addPSSDIdentityDICOM (StudyMetadata sm,  XmlDoc.Element currentMeta,  XmlDocMaker dm) throws Throwable {
		if (sm==null) return false;
		
		// Extract DICOM meta data 
		String patientID = sm.patientID();
		if (patientID == null) return false;

		// Set type of identity; i.e. who supplied this identity
		String typeID = "Other";
		String scanFac = scannerFacility(sm);
		if (scanFac.equals(RCH_FACILITY)) {
			typeID = "RCH";
		} else if (scanFac.equals(AMRIF_FACILITY)) {
			typeID = "aMRIF";
		}	
		
		// Add/merge the identity if needed.
		return addMergeIdentity (currentMeta, patientID, typeID, dm);
	}




	/**
	 * Function to add the Subject ID (DICOM element (0010,0020)) to
	 * the SUbject meta-data if it does not already exist
	 * not already exist
	 * 
	 * @param executor
	 * @param currentMeta
	 * @param cid
	 * @throws Throwable
	 */	
	private boolean addPSSDIdentityBruker (NIGBrukerIdentifierMetaData sm,  XmlDoc.Element currentMeta,  XmlDocMaker dm) throws Throwable {
		if (sm==null) return false;
		
		// Extract Bruker meta data 
		String animalID = sm.animalID();
	
		// Set type of identity; i.e. who supplied this identity
		 // OK because this is a NIG class and should only be utilised at NIG (for now)
		// TODO: find some way of getting the actual station into here.
		String typeID = "aMRIF";        
			
		
		// Add/merge the identity if needed.
		return addMergeIdentity (currentMeta, animalID, typeID, dm);

	}
		
	private boolean addMergeIdentity (XmlDoc.Element currentMeta,  String subjectID, String typeID, XmlDocMaker dm) throws Throwable {


		// See if this specific identity already exists on the object
		Collection<XmlDoc.Element> identities = null;
		if (currentMeta!=null) {
			identities = currentMeta.elements("hfi.pssd.identity");
			if (identities != null) {
				for (XmlDoc.Element el : identities) {
					String id = el.value("id");
					String type = el.value("id/@type");

					// If we have this specific identity already, return
					if(id!=null&&type!=null) {
						if (id.equals(subjectID) && type.equals(typeID)) return false;
					}
				}
			}
		}

		// So we did not find this identity and need to add it.
		// If we have just one pre-existing identity, merge with it
		// Otherwise add a new one		
		dm.push("hfi.pssd.identity");
		if (identities!=null && identities.size()==1) {
			XmlDoc.Element identity = currentMeta.element("hfi.pssd.identity");
			Collection<XmlDoc.Element> els = identity.elements();
			for (XmlDoc.Element el : els) {
				dm.add(el);
			}					
		}
		//
		dm.add("id", new String[] { "type", typeID }, subjectID);		
		dm.pop();

		// We want to merge  this identity with others on the same document
		dm.pop();      // "public" or "private" pop
		dm.add("action", "merge");
		return true;
	}



	
	private boolean addPSSDAnimalSubject (String dob, String gender, XmlDoc.Element currentMeta,  XmlDocMaker dm) throws Throwable {

		// Get current meta-data for appropriate DocType
		if (currentMeta!=null) {
			XmlDoc.Element subjectMeta = currentMeta.element("hfi.pssd.animal.subject");

			// We assume that if the element is already set on the object that it is correct
			if (subjectMeta!=null) {
				String currGender = subjectMeta.value("gender");
				if (currGender!=null) gender = currGender;
				//
				String currDate = subjectMeta.value("birthDate");
				if (currDate != null) dob = currDate;
			}
		}
		
		// Set updated meta-data
		if (gender!=null || dob!=null) {
			dm.push("hfi.pssd.animal.subject");
			if (gender!=null) dm.add("gender", gender);
			if (dob != null) dm.add("birthDate", dob);
			dm.pop();
		} else {
			return false;
		}

		// Merge these details
        dm.pop();      // "public" or "private" pop
        dm.add("action", "merge");	
        return true;
	}
	
	
	
	/**
	 * What Facility are these data from?
	 * @param sm
	 * @return
	 */
	public String scannerFacility  (StudyMetadata sm) {
		String institution = sm.institution().toUpperCase();
		String station = sm.station().toUpperCase();
		//
		if (institution.contains("HOWARD FLOREY INSTITUTE") && station.equals("EPT")) {
			return AMRIF_FACILITY;
		} else if (institution.contains("CHILDREN") && institution.contains("RCH") &&
			      	station.equals("MRC35113")) {
			return RCH_FACILITY;
		} else {
			return institution;
		}
	}
}
