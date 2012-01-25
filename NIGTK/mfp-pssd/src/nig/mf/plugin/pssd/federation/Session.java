package nig.mf.plugin.pssd.federation;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class Session {

	public static boolean isFederated(ServiceExecutor executor)
			throws Throwable {

		XmlDoc.Element r = executor.execute("system.session.self.describe");
		return r.element("session/federate") != null;
	}

}
