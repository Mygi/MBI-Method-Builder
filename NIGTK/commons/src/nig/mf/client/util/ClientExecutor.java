package nig.mf.client.util;

import java.util.Collection;

import nig.mf.Executor;
import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;
import arc.xml.XmlStringWriter;
import arc.xml.XmlDoc.Element;

public class ClientExecutor implements Executor {

	private ServerClient.Connection _se;
	
	public ClientExecutor(ServerClient.Connection se){
		_se =se ;
	}
	
	public Element execute(String service, XmlDocMaker args) throws Throwable {
		
		// Convert to String for the ServerClient interface.
		// The ServiceExecutor (plugin) just needs args.root()
		// Very annoying way to do this and not fast.
   		Collection<XmlDoc.Element> els = args.root().elements();
		XmlStringWriter w = new XmlStringWriter();
		for (XmlDoc.Element el : els) {
			w.add(el);
		}
		
		// Execute
		return _se.execute(null, service, w.document());
	}


}
