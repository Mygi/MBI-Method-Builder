package nig.mf.plugin.pssd.dicom.series;

import java.io.File;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.*;
import nig.mf.plugin.pssd.dicom.study.PSSStudyProxy;

public class PSSSeriesProxy extends SeriesProxy {

	private PSSStudyProxy _study = null;
	
	public PSSSeriesProxy(PSSStudyProxy study,int id) {
		super(id);
		
		_study = study;
	}

	public long createAsset(ServiceExecutor executor, long study, File data, int imin,int imax, int size) throws Throwable {
		// TODO Auto-generated method stub
		return 0;
	}

	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		// TODO Auto-generated method stub
		
	}

}
