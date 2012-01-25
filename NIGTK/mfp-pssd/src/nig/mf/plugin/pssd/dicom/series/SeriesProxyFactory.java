package nig.mf.plugin.pssd.dicom.series;


import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.*;
import nig.iio.dicom.SeriesMetadata;
import nig.mf.plugin.pssd.dicom.DicomIngestControls;
import nig.mf.plugin.pssd.dicom.study.PSSDStudyProxy;

public class SeriesProxyFactory {
	
	/**
	 * Creates the "right" study proxy for the incoming study.
	 * 
	 * @param executor
	 * @param studyUID
	 * @param dem
	 * @param ic
	 * @return
	 * @throws Throwable
	 */
	public static SeriesProxy createSeriesProxy(ServiceExecutor executor,DicomEngineContext ec,StudyProxy study, int id, DataElementMap dem,DicomIngestControls ic) throws Throwable {
		if ( study instanceof PSSDStudyProxy ) {
			PSSDStudyProxy psp = (PSSDStudyProxy)study;
		
			SeriesMetadata sm = SeriesMetadata.createFrom(dem);
			return new PSSDSeriesProxy(psp,id,sm);
		}
		
		
		DicomAssetEngine pss = ec.engine("pss");
		if ( pss == null ) {
			throw new Exception("No DICOM engine found to process the data.");
		}
		
		return pss.createSeriesProxy(executor, study, id, dem);
	}
	
}
