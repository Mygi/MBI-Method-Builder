package nig.mf.plugin.pssd.dicom.series;

import java.io.File;
import java.io.FileInputStream;

import arc.mf.plugin.PluginService;
import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.*;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import nig.mf.plugin.pssd.dicom.DICOMMetaUtil;
import nig.mf.plugin.pssd.dicom.study.PSSDStudyProxy;
import nig.iio.dicom.SeriesMetadata;
import nig.iio.dicom.StudyMetadata;




public class PSSDSeriesProxy extends SeriesProxy {

	private PSSDStudyProxy _study;
	private SeriesMetadata _sm;
	private boolean        _createdSeries;
	private String         _series;
	private StudyMetadata _studyMeta;


	public PSSDSeriesProxy(PSSDStudyProxy study,int id,SeriesMetadata sm) {
		super(id);

		_study = study;
		_sm = sm;
		_createdSeries = false;
		_studyMeta = _study.metaData();
	}


	/**
	 * The asset will be a 'derived' DataSet. If there is a matching (by UID) Bruker DataSet
	 *  use that to establish the name of this DataSet and to set its derivation origins
	 *  
	 */
	public long createAsset(ServiceExecutor executor, long study, File data,int imin,int imax,int size) throws Throwable {
		String sid = _study.id();

		// If this DICOM series already exists, we are going to overwrite it.
		_series = findExistingDICOMSeries(executor,sid,_sm.UID());
		String prot = _sm.protocol();
		String desc = _sm.description();
		String id = _sm.id();

		// Do the data originate from a Bruker scanner. 
		boolean isBruker = DICOMMetaUtil.isBrukerScanner(_studyMeta);

		// Set some DataSet metadata and especially the name of the DataSet
		XmlDocMaker dm = new XmlDocMaker("args");
		String name = setDataSetName(isBruker, prot, desc, _sm.UID(), id);
		if (name != null) dm.add("name", name);

		// See what is available for the object description
		if ( desc != null ) {
			dm.add("description",desc);
		} else if ( prot != null) {
			dm.add("description", prot);
		}

		// If this DataSet is derived from a Bruker DataSet, add that information
		// FInd whether there is a pre-existing Bruker series for this Study
		// If there is we will get the 'vid' parameter (to use in derivation specification)
		if (isBruker) {
			String brukerCID = findExistingBrukerSeries(executor, sid, _sm.UID());
			if (brukerCID != null) {
				String brukerVid = getExistingBrukerDetails (executor, brukerCID);
				if (brukerVid!=null) {
					dm.add("input", new String[] {"vid", brukerVid}, brukerCID);
				}
			}
		}	

		// We can get the ExMethod and Step from the Study, so might as well supply it..
		dm.push("method");
		dm.add("id",_study.exMethod());
		String step = _study.methodStep();
		dm.add("step",step);
		if (step == null) step = "1";               // Shouldn't happen but seems to sometimes...
		dm.pop();

		// Add meta-data	
		dm.push("meta");

		// Add Mediaflux generic DICOM meta-data
		dm.push("mf-dicom-series",new String[] { "ns", "dicom" });

		// ds.add(new XmlDoc.Element("idx",String.valueOf(idx)));
		dm.add("uid",_sm.UID());
		dm.add("id",_sm.id());

		if ( desc != null ) {
			dm.add("description", desc);
		}

		if ( _sm.modality() != null ) {
			dm.add("modality",_sm.modality());
		}

		if ( prot != null ) {
			dm.add("protocol",prot);
		}

		if ( _sm.creationTime() != null ) {
			dm.add("sdate",_sm.creationTime());
		}

		if ( imin != -1 ) {
			// MF 3.6.052 allows imin,imax>=0
			dm.add("imin",imin);
			dm.add("imax",imax);
		}

		dm.add("size",size);
		dm.pop();

		// Now add HFI supplementary DICOM meta-data
		// Although these are HFI definitions, it's not domain-specific
		// However, check that the Doc Type exists before using since
		// it is not a native MF DocType
		String docType = "hfi.pssd.dicom.series";
		if (nig.mf.pssd.plugin.util.PSSDUtil.checkDocTypeExists(executor, docType)) {
			double [] imagePosition = _sm.imagePosition();
			double [] imageOrientation = _sm.imageOrientation();
			dm.push(docType,new String[] { "ns", "dicom" });

			if (imagePosition != null) {
				int len = imagePosition.length;
				if (len <= 3)  {                         // DocType max 3
					dm.push("image_position_patient");
					for (int i=0; i<len; i++) {
						dm.add("value", imagePosition[i]);
					}
					dm.pop();
				}
			}
			//
			if (imageOrientation != null)  {   
				int len = imageOrientation.length;
				if (len <= 6)  {                         // DocType max 6
					dm.push("image_orientation_patient");
					for (int i=0; i<len; i++) {
						dm.add("value", imageOrientation[i]);
					}
					dm.pop();
				}
			}
			dm.pop();
		}
		//
		dm.pop();

		// Create the DataSet.  DICOM DataSets are always derived (i.e. non-native 
		// acquisition format). 
		FileInputStream is = new FileInputStream(data);
		try {
			PluginService.Input in = new PluginService.Input(is,data.length(),"application/zip",null);
			PluginService.Inputs ins = new PluginService.Inputs(in);

			if ( _series == null ) {
				dm.add("pid",sid);
				dm.add("type","dicom/series");
				XmlDoc.Element r = executor.execute("om.pssd.dataset.derivation.create",dm.root(),ins,null);
				_series = r.value("id");
				_createdSeries = true;
			} else {
				dm.add("id",_series);
				executor.execute("om.pssd.dataset.derivation.update",dm.root(),ins,null);
			}
		} finally {
			is.close();
		}

		return 0;
	}

	/**
	 * Find out if this DICOM data already exists as a DICOM DataSet under this Study
	 * Use the UID to find it.
	 * 
	 * @param executor
	 * @param study
	 * @param uid
	 * @return citable ID of DICOM DataSet
	 * @throws Throwable
	 */
	private String findExistingDICOMSeries(ServiceExecutor executor,String studyCID,String uid) throws Throwable {
		// Native MF Doc Type
		String query = "cid in '" + studyCID + "' and xpath(mf-dicom-series/uid)='" + uid + "'";

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("action","get-cid");
		dm.add("pdist", 0);		
		XmlDoc.Element r = executor.execute("asset.query",dm.root());
		return r.value("cid");
	}

	/**
	 * Find out if this DICOM data already exists as a Bruker DataSet under this Study
	 * Use the UID to find it.
	 * 
	 * @param executor
	 * @param studyCID
	 * @param uid
	 * @return citable ID of primary Bruker DataSet
	 * @throws Throwable
	 */
	private String findExistingBrukerSeries(ServiceExecutor executor,String studyCID,String uid) throws Throwable {
		// NIG Doc Type 
		String docType = "hfi-bruker-series";
		if (!nig.mf.pssd.plugin.util.PSSDUtil.checkDocTypeExists(executor, docType)) return null;

		String query = "cid in '" + studyCID + "' and xpath(" + docType + "/uid)='" + uid + "'";		
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("where",query);
		dm.add("action","get-cid");
		dm.add("pdist", 0);
		XmlDoc.Element r = executor.execute("asset.query",dm.root());
		return r.value("cid");
	}

	/**
	 * Fish out the "vid" from the existing matched Bruker DataSet
	 * @param executor
	 * @param cid
	 * @return 
	 * @throws Throwable
	 */
	private String getExistingBrukerDetails(ServiceExecutor executor, String cid) throws Throwable {
		if (cid==null) return null;
		//
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", cid);
		XmlDoc.Element el = executor.execute("om.pssd.object.describe", dm.root());
		return el.value("object/vid");
	}

	/**
	 * Set the meta-data that describes the primary DataSet that this one will
	 * be derived from
	 * 
	 * @param dm
	 * @param brukerCID
	 * @throws Throwable
	 */
	private void setInputDerivation (ServiceExecutor executor, XmlDocMaker dm, String brukerCID, String vid) throws Throwable {			
		dm.add("input", new String[] {"vid", vid}, brukerCID);
	}


	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		if ( !_createdSeries ) {
			return;
		}

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id",_series);

		executor.execute("om.pssd.object.destroy",dm.root());

		_createdSeries = false;	
	}



	private String setDataSetName (boolean isBruker, String prot, String desc, String uid, String id) {

		String name = null;
		if (isBruker) {

			// The data originate from a Bruker scanner.
			// The bit shifted id gives back the integer by which the folders
			// are named in the ParaVision console machine and by which the aMRIF staff 
			// identify data.
			name = "" + (Integer.parseInt(id)>>16);
			if (prot!=null) name += "_" + prot;
		} else {

			// Name the DataSet, based on the combination of protocol
			// and description.  Generally the protocol may stay fixed
			// and the description varies (e.g. describes online processing).
			if (prot != null && desc != null) {
				if (prot.equals(desc)) {
					name = prot;
				} else {
					name = prot + "_" + desc;
				}
			} else if (prot != null) {
				name = prot;
			} else if (desc != null) {
				name = desc;	
			}	
			// Fall back on Series number or UID if really needed for Series name
			if (name == null) {
				if (id != null) {
					name = id;
				}else if (uid != null) {
					name = uid;
				}
			}
		}
		return name;	
	}
}
