package nig.mf.plugin.pssd.dicom;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class LocalDicomAE {


	public static String getAETitle(ServiceExecutor executor) throws Throwable {
		XmlDoc.Element r = executor.execute("network.describe");
		return r.value("service[@type='dicom']/arg[@name='dicom.title']");
	}


	public static int getPort(ServiceExecutor executor) throws Throwable {
		XmlDoc.Element r = executor.execute("network.describe");
		return r.intValue("service[@type='dicom']/@port", -1);
	}
}
