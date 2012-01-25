package nig.mf.plugin.util;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDocMaker;
import arc.xml.XmlDoc.Element;
import nig.mf.Executor;

public class PluginExecutor implements Executor {

	private ServiceExecutor _se;
	
	public PluginExecutor(ServiceExecutor se){
		_se =se ;
	}
	
	public Element execute(String service, XmlDocMaker args) throws Throwable {
		return _se.execute(null, service, args.root(), null, null);
	}
	

}
