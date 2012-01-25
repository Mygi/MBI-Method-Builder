package daris.client.model.fcp.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.fcp.FileCompilationProfile;

public class FCPList extends ObjectMessage<List<FileCompilationProfile>> {

	@Override
	protected void messageServiceArgs(XmlWriter w) {

	}

	@Override
	protected String messageServiceName() {
		return "om.pssd.fcp.list";
	}

	@Override
	protected List<FileCompilationProfile> instantiate(XmlElement xe)
			throws Throwable {
		if (xe != null) {
			List<XmlElement> fcpes = xe.elements("fcp");
			if (fcpes != null) {
				List<FileCompilationProfile> fcps = new Vector<FileCompilationProfile>(
						fcpes.size());
				for (XmlElement fcpe : fcpes) {
					FileCompilationProfile fcp = FileCompilationProfile.create(
							fcpe.value("id"), fcpe.value("name"),
							fcpe.value("description"));
					if (fcp != null) {
						fcps.add(fcp);
					}
				}
				if (!fcps.isEmpty()) {
					return fcps;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return null;
	}

}
