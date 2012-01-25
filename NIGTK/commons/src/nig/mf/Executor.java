package nig.mf;

import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * Interface to combine plugin and client executors
 * 
 * @author nebk
 *
 */
public interface Executor {
	
	/**
	 * Derived classes must implement this function.
	 * @param service  The name of the service to run (on the local host)
	 * @param args The agruments to supply to the service
	 * @return
	 * @throws Throwable
	 */
	XmlDoc.Element execute(String service, XmlDocMaker args) throws Throwable;

}
