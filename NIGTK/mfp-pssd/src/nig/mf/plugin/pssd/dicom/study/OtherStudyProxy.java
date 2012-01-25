package nig.mf.plugin.pssd.dicom.study;

import arc.mf.plugin.ServiceExecutor;
import arc.mf.plugin.dicom.StudyProxy;

public class OtherStudyProxy extends StudyProxy {

	private String _ns;
	private long   _id;
	
	public OtherStudyProxy(String namespace,String studyId,long id) {
		super(studyId);
		
		_ns = namespace;
	}

	public long createAsset(ServiceExecutor executor) throws Throwable {
		// TODO Auto-generated method stub
		return 0;
	}

	public void destroyAsset(ServiceExecutor executor) throws Throwable {
		// TODO Auto-generated method stub
		
	}

}
