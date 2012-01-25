package nig.iio.metadata;

import nig.mf.Executor;


/**
 * Separate the research domain dependent meta-data additions on SUbjects and Studies into domain-specific
 * modules.  The idea is that a Subject once created, may need extra meta-data located on it
 * by a server or client process (and they extract that meta-data from file headers_
 * 
 * Each module must add Subject and Study meta-data for a specific domain. 
 * However, they must be fully driven by the Methods so that they only add meta-data
 * as required by the Method.  The NIG module shows how to do this in a robust way.
 * 
 * The framework uses an executor wrapper so that it can be used in both Plugin (e.g. DICOM server)
 * and Client (e.g. Bruker client) frameworks.  This combined world is also why the meta-data
 * are supplied in the container class, ImageMetaDataContainer
 * 
 * TODO: we could add a DICOM control and allow that to specify a string (e.g. "nig")
 * which translates into which "package" of extra meta-data you want to try to locate
 * on the SUbject/Study. Then we will have a full framework.
 * 
 * @author nebk
 *
 */
public class DomainMetaManager {

	/**
	 * Add domain-specific subject meta-data
	 * 
	 * @param executor
	 * @param subjectCId
	 * @param sm
	 * @throws Throwable
	 */
	public static void addSubjectMetaData (Executor executor, String subjectCid, ImageMetaDataContainer sm) throws Throwable {
		

		// Neuroimaging domain		
		NIGMetaData nmd = new NIGMetaData();
		nmd.addSubjectMetaData(executor, subjectCid, sm);
		
		// Other domains
	}

	public static void addStudyMetaData (Executor executor, String studyCid, ImageMetaDataContainer sm) throws Throwable {
		
		//
	}



}
