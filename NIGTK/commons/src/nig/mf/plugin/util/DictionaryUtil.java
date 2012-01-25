package nig.mf.plugin.util;

import java.util.Collection;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

public class DictionaryUtil {

	@SuppressWarnings("unchecked")
	public static Collection<String> getTerms(ServiceExecutor executor, String dictionary) throws Throwable {

		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("dictionary", dictionary);
		XmlDoc.Element r = executor.execute("dictionary.entries.list", doc.root());
		return r.values("term");

	}
	
	/**
	 * Finds the definition of a term in a dictionary
	 * 
	 * @param executor
	 * @param term
	 * @param dictionary
	 * @throws Throwable
	 */
	public static String typeToDefinition (ServiceExecutor executor, String term, String dictionary) throws Throwable {
		//
		XmlDocMaker doc = new XmlDocMaker("args");
		doc.add("dictionary", dictionary);
		doc.add("term", term);
		XmlDoc.Element r = executor.execute("dictionary.entry.describe", doc.root());
		return r.stringValue("entry/definition");	
	}
}
