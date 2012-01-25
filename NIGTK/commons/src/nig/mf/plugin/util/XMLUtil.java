package nig.mf.plugin.util;

import java.util.Collection;
import nig.encrypt.*;
import nig.encrypt.EncryptionTypes.EncryptionType;

import java.util.Vector;

import arc.xml.XmlDoc;

/**
 * 
 * Mediaflux XML handling utilities
 * 
 * @author nebk
 */

public class XMLUtil {


	/**
	 * Recursively replace any instances where the value of an element has the given old String.
	 * 
	 * @param doc The document to search
	 * @param oldValue  The value of the String to replace
	 * @param newValue  The new value of the replaced String
	 * @param exact If true, the String to be replaced must match exactly.  Otherwise, the 
	 * String to be replaced must just be contained and that contained SubString will be replaced.
	 * @return true if replacement occurred else false
	 * 
	 */
	public static boolean replaceString (XmlDoc.Element doc, String oldValue, String newValue, boolean exact) {
		if (doc==null) return false;
		Vector<XmlDoc.Element> els = doc.elements();
		if (els==null) return false;
		boolean doReplace = false;

		for (XmlDoc.Element el : els) {

			if (el.hasValue()) {
				String val = el.value();
				if (!exact) {
					if (val.contains(oldValue)) {
						val = val.replace(oldValue, newValue);
						el.setValue(val);
						doReplace = true;
					}
				} else {
					if (val.equals(oldValue)) {
						el.setValue(newValue);
						doReplace = true;
					}
				}
			}

			// Recurse down
			boolean doReplace2 = replaceString (el, oldValue, newValue, exact);
			if (doReplace2) doReplace = true;
		}

		return doReplace;
	}
	
	
	/**
	 * Iterate through a collection of XML documents and encrypt the values of all
	 * elements of all documents.  Each document gets the attribute encryption="encrypted"
	 * added to it. 
	 * 
	 * If a document is already encrypted does nothing.
	 * 
	 * @param type The type of encryption
	 * @param docs The collection of documents
	 */
	public static boolean encryptXML (EncryptionType type, Collection<XmlDoc.Element> docs) {
		if (docs==null) return false;
		//
		boolean somethingEncrypted = false;
		for (XmlDoc.Element doc : docs) {
			
			// See if we have already encrypted this document
			XmlDoc.Attribute encryptAttr  = doc.attribute(EncryptionTypes.ENCRYPTED_ATTR);
			String attVal = null;
			if (encryptAttr!=null) attVal = encryptAttr.value();
			
			// If already encrypted skip
			if (attVal==null || !attVal.equals(EncryptionTypes.ENCRYPTED_VALUE)) {
				XMLUtil.encryptXML(type, doc);  
				doc.add(new XmlDoc.Attribute(EncryptionTypes.ENCRYPTED_ATTR, EncryptionTypes.ENCRYPTED_VALUE));
				somethingEncrypted = true;
			}
		}
		return somethingEncrypted;
	}

	
	
	/**
	 * Recursively encrypt the values of all elements in the XML document
	 * 
	 * @param doc
	 */
	public static void encryptXML (EncryptionType type, XmlDoc.Element doc) {
		if (doc==null) return;
		//
		Vector<XmlDoc.Element> els = doc.elements();
		if (els==null) return;

		for (XmlDoc.Element el : els) {

			if (el.hasValue()) {
				String eVal = encryptString(type, el.value());
				el.setValue(eVal);
			}

			// Recurse down
			encryptXML (type, el);
		}		   
	}
	

	/**
	 * Recursively decrypt the values of all elements in the XML document
	 * 
	 * @param doc 
	 * @param isDefinition Document is an XML definition, not just the instantiation of the document.
	 */
	public static void decryptXML (EncryptionType type, XmlDoc.Element doc, boolean isDefinition) {
		if (doc==null) return;
		//
		Vector<XmlDoc.Element> els = doc.elements();
		if (els==null) return;

		for (XmlDoc.Element el : els) {
			if (isDefinition) {
				if (el.name().equals("value")) {
					String eVal = decryptString(type, el.value());
					el.setValue(eVal);			
				}
			} else {
				if (el.hasValue()) {
					String eVal = decryptString(type, el.value());
					el.setValue(eVal);
				}
			}

			// Recurse down
			decryptXML (type, el, isDefinition);
		}		   
	}

		
	/**
	 * Encrypt String.  The caller must ensure the String is not null
	 * as no check is made
	 * 
	 * @param type
	 * @param str
	 * @return
	 */
	private static String encryptString (EncryptionType type, String str) {
		switch (type) {
		   case BASE_64: return Base64Coder.encodeString(str); 
		   case ROT13: return ROT13.encodeString(str);
		}
		return null;
	}

	/**
	 * Decrypt String.  The caller must ensure the String is not null
	 * as no check is made
	 * 
	 * @param type
	 * @param str
	 * @return
	 */
		private static String decryptString (EncryptionType type, String str) {
		switch (type) {
			case BASE_64: return Base64Coder.decodeString(str);
			case ROT13: return ROT13.decodeString(str);
		}
		return null;
	}
}
