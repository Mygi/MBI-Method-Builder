package nig.mf.plugin.util;

import java.util.Collection;

import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;

public class TemplateUtil {

	/**
	 * 
	 * Replace any restrictions in dictionaries by the dictionary entries.
	 * 
	 * 
	 * @param executor
	 * @param doc
	 * @throws Throwable
	 */
	@SuppressWarnings("unchecked")
	public static void replaceDictionaries(ServiceExecutor executor, XmlDoc.Element doc) throws Throwable {

		Collection<XmlDoc.Element> dicts = doc.allElements("dictionary");
		if (dicts == null) {
			return;
		}
		for (XmlDoc.Element dict : dicts) {
			Collection<String> terms = DictionaryUtil.getTerms(executor, dict.value());
			if (terms != null) {
				XmlDoc.Element parent = dict.parent();
				parent.remove(dict);
				for (String term : terms) {
					parent.add(new XmlDoc.Element("value", term));
				}
			}
		}
	}	
}
