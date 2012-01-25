package nig.iio.metadata;

import arc.xml.*;
import java.util.Collection;

import nig.mf.Executor;


/**
 * Base class for the framework to supply domain (method)-specific meta-data for
 * Subjects and R-SUbjects to the DICOM server and Bruker client.   The framework is Method driven 
 * so that only the meta-data driven by the Method is attached.  The plugin and client 
 * environment is supported via the Executor wrapper class.
 * 
 * Currently only Subjects are supported. As we add other objects, e.g. Studies,
 * the framework may need some additional modification (e.g. Study meta-data layout
 * is different)
 * 
 * See NIGMetaData as an example of a derived class for the Neuroimaging domain
 * 
 * @author nebk
 *
 */
public class DomainMetaData {
	
	// Constructor
	public DomainMetaData () 
	{
		//
	}
	
	
	/**
	 * Add meta-data to the Subject or RSubject objects by filling from the supplied meta-data
	 * if it does not pre-exist
	 * 
	 * @param executor
	 * @param studyCid
	 * @param sm
	 * @throws Throwable
	 */
	public void addSubjectMetaData (Executor executor, String subjectCID, ImageMetaDataContainer sm) throws Throwable {
		


		// Get the current and possible Subject meta-data attached to this object
        XmlDoc.Element current = describe(executor, subjectCID, false);
        XmlDoc.Element possible = describe(executor, subjectCID, true);
	
		// Update "public" and "private" elements of Subject
		updateSubject (executor, subjectCID, sm, "public", current, possible);
		updateSubject (executor, subjectCID, sm, "private", current, possible);
	
		// If the SUbject refers to an R-SUbject, look there as well
		String rsCID = current.value("object/r-subject");
		if (rsCID != null) {
			
			// Get the current and possible R-SUbject meta
	        current  = describe(executor, rsCID, false);
	        possible = describe(executor, rsCID, true);

			// Update "public", "private" and "identity" elements of R-Subject
			updateSubject (executor, rsCID, sm, "public", current, possible);
			updateSubject (executor, rsCID, sm, "private", current, possible);
			updateSubject (executor, rsCID, sm, "identity", current, possible);			
		}
	}
	
	
	private XmlDoc.Element describe (Executor exec, String id, boolean forEdit) throws Throwable {
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("foredit", forEdit);
		return exec.execute("om.pssd.object.describe", dm);

	}
	/**
	 * Extract current and possible meta-data for given privacyType
	 * and update the meta-data on the object.
	 * 
	 * @param executor
	 * @param id  Object CID
	 * @param sm
	 * @param privacyType "public", "private", "identity"
	 * @param rCurrent
	 * @param rPossible
	 * @throws Throwable
	 */
	private void updateSubject (Executor executor, String id, ImageMetaDataContainer sm, String privacyType, 
			XmlDoc.Element rCurrent, XmlDoc.Element rPossible) throws Throwable {

		// Get the possible document definitions
		if (rPossible==null) return;
		Collection<XmlDoc.Element> possible = rPossible.elements("object/"+privacyType+"/metadata");

		// Get the current attached documents
		XmlDoc.Element current = null;
		if (rCurrent!=null) current = rCurrent.element("object/"+privacyType); 

		// Update the meta-data on the possible documents by merging with the existing
		addTranslatedSubjectDocuments(executor, id, sm, privacyType, possible, current);
	}
	
	
	/**
	 * Find the documents that could be attached to an asset and map DICOM meta-data, where possible,
	 * to populate those documents.  
	 * 
	 * @param executor
	 * @param id The citeable ID of the object to update
	 * @param sm Study Metadata
	 * @param privacyType "public", "private", "identity" indicating which element of the meta-data structure
	 *          we are working with. This is needed when re-setting meta-data
	 * @param possibleMeta The meta-data that could be attached to the asset (:foredit true)
	 * @param currentMeta  The meta-data that are attached to the asset (:foredit false)
	 * @param dmOut
	 * @throws Throwable
	 */
	private void addTranslatedSubjectDocuments(Executor executor, String id, ImageMetaDataContainer sm, String privacyType, 
			Collection<XmlDoc.Element> possibleMeta, XmlDoc.Element currentMeta) throws Throwable {

		// Iterate over the possible document definitions
		if (possibleMeta!=null) {
			for (XmlDoc.Element el : possibleMeta) {
				// Get the possible document type
				String docType = el.value("@type");

				// Merge possible with current. Each document may need to handle the meta-data
				// in a different way ("add", "merge" etc) so each document is wrapped separately
				addTranslatedSubjectDocument (executor, id, sm, privacyType, docType, currentMeta);				
			}
		}
	}


	/**
	 * Update the meta-data on the  object for the given Document Type. This function must
	 * do the actual update with the appropriate service (e.g. om.pssd.subject.update)
	 * 
	 * @param id The citeable ID of the object to update
	 * @param sm The Study Metadata in the DICOM file
	 * @param privacyType "public", "private", "identity" indicating which element of the meta-data structure
	 *          we are working with. This is needed when re-setting meta-data
	 * @param docType the document type to write meta-data for.  The values must be mapped from the Study MetaData
	 * @param currentMeta  The meta-data that are attached to the asset (:foredit false)
	 * @throws Throwable
	 */
    protected void addTranslatedSubjectDocument (Executor executor, String id, ImageMetaDataContainer sm, String privacyType, 
    		String docType, XmlDoc.Element currentMeta) throws Throwable {
    	
    	throw new Exception ("Function must be implemented by derived class");
 
    }
	
}
