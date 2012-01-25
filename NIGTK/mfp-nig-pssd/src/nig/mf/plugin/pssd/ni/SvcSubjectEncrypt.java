package nig.mf.plugin.pssd.ni;

import java.util.Collection;
import java.util.Vector;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.mf.plugin.util.XMLUtil;
import nig.mf.pssd.plugin.util.PSSDUtil;
import nig.encrypt.EncryptionTypes;
import nig.encrypt.EncryptionTypes.EncryptionType;


public class SvcSubjectEncrypt extends PluginService {


	private Interface _defn;

	public SvcSubjectEncrypt() {
		_defn = new Interface();		
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The citable identifier of the local parent Project or individual Subject.  If none given, all Subjects in all Projects will be encrypted.", 0, 1));
		_defn.add(new Interface.Element("doctype",StringType.DEFAULT, "The name of a specific document type to be encrypted. If none given, will try to encrypt all documents in the private element.", 0, Integer.MAX_VALUE));
	}


	public String name() {
		return "nig.pssd.subject.encrypt";
	}

	public String description() {
		return "Encrypts the values of any elements (if not already encrypted) in documents stored in the 'private' element of the Subject. Applied to all child Subjects if Project supplied else just the given Subject.  Only operates on local objects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String id = args.value("id");
		Collection<String> docTypes = args.values("doctype");

		// Encrypt
		boolean encrypt = true;
		encryptDecrypt (executor(), id, docTypes, encrypt,  w);
	}


	public static void encryptDecrypt (ServiceExecutor executor, String id, Collection<String> docTypes, boolean encrypt, XmlWriter w) throws Throwable {
		if (id==null) {

			// Get the list of projects
			XmlDocMaker dm = new XmlDocMaker("args");
			dm.add("pdist", 0);         // Force local
			XmlDoc.Element r = executor.execute("om.pssd.collection.members", dm.root());
			if (r==null) return;
			Collection<XmlDoc.Element> projects = r.elements("object/id");
			if (projects==null) return;

			// Encrypt/Decrypt
			for (XmlDoc.Element project : projects) {
				String projectCid = project.value();
			
				if (PSSDUtil.isReplica(executor, projectCid)) {
					throw new Exception ("The given Project is a replica. Cannot modify its children Subjects.");
				}
				encryptDecryptProject(executor, projectCid, docTypes, encrypt, w);
			}	
		} else {
			// Make sure the parent is a Project or Subject
			boolean isProject = PSSDUtil.isValidProject(executor, id, false);
			boolean isSubject = PSSDUtil.isValidSubject(executor, id, false);
			if ( !isProject && !isSubject) {
				throw new Exception("Object " + id + "  is neither a Project nor a Subject");
			}

			// Encrypt/Decrypt
			if (isProject) {
				if (PSSDUtil.isReplica(executor, id)) {
					throw new Exception ("The given Project object is a replica. Cannot modify its children Subjects.");
				}
				encryptDecryptProject (executor, id, docTypes, encrypt, w);
			} else {
				if (PSSDUtil.isReplica(executor, id)) {
					throw new Exception ("The given Subject object is a replica. Cannot modify it.");
				}
				encryptDecryptSubject (executor, id, docTypes, encrypt, w);
			}
		}
	}


	private static void encryptDecryptProject (ServiceExecutor executor, String id, Collection<String> docTypes, boolean encrypt, XmlWriter w) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("id", id);
		dm.add("pdist", 0);      // Force local
		XmlDoc.Element r = executor.execute("om.pssd.collection.members", dm.root());
		if (r==null) return;
		//
		Collection<XmlDoc.Element> subjects = r.elements("object/id");
		if (subjects==null) return;
		//
		for (XmlDoc.Element subject : subjects) {
			String subjectCid = subject.value();
			if (PSSDUtil.isReplica(executor, subjectCid)) {
				w.add("subject", "The Subject object '" + subject + "' is a replica. Cannot modify it.");
			} else {
				encryptDecryptSubject(executor, subjectCid, docTypes, encrypt, w);
			}
		}	
	}

	private static void encryptDecryptSubject (ServiceExecutor executor, String id, Collection<String> docTypes, boolean encrypt, XmlWriter w) throws Throwable {

		// Get old
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("cid", id);
		XmlDoc.Element r = executor.execute("asset.get", dm.root());
		if (r==null) return;

		XmlDoc.Element doc = r.element("asset/meta");
		if (doc==null) return;
		Vector<XmlDoc.Element> docs = doc.elements();

		// Iterate over documents and populate new meta
		dm = new XmlDocMaker("args");
		dm.add("cid", id);
		dm.push("meta", new String[]{"action","replace"});
		//
		boolean some = false;
		for (XmlDoc.Element el : docs) {
			String ns = el.value("@ns");
			String docType = el.name();

			// We are only dealing with things in the private namespace
			if (ns!=null && ns.equals("pssd.private")) {
				if (wantDocument(docTypes, docType)) {
					XmlDoc.Attribute encryptAttr  = el.attribute(EncryptionTypes.ENCRYPTED_ATTR);
					String attVal = null;
					if (encryptAttr!=null) attVal = encryptAttr.value();

					if (encrypt) {
						// Encrypt if not already encrypted
						if (attVal==null || !attVal.equals(EncryptionTypes.ENCRYPTED_VALUE)) {			
							XMLUtil.encryptXML(EncryptionType.BASE_64, el);
							el.add(new XmlDoc.Attribute(EncryptionTypes.ENCRYPTED_ATTR, EncryptionTypes.ENCRYPTED_VALUE));
							dm.add(el);
							some = true;
						}
					} else {
						// Decrypt if encrypted
						if (attVal!=null && attVal.equals(EncryptionTypes.ENCRYPTED_VALUE)) {	
							boolean isDefinition = false;
							XMLUtil.decryptXML(EncryptionType.BASE_64, el, isDefinition);
							el.remove(new XmlDoc.Attribute(EncryptionTypes.ENCRYPTED_ATTR, EncryptionTypes.ENCRYPTED_VALUE));
							dm.add(el);
							some = true;
						}
					}
				}			
			}
		}
		dm.pop();	
		//
		if (some) {		
			// Replace documents
			executor.execute("asset.set", dm.root());
			w.add("id", id);
		}
	}

	private static boolean wantDocument (Collection<String> docTypes, String docType) {
		if (docTypes==null) return true;
		for (String docType2 : docTypes) {
			if (docType2.equals(docType)) return true;		
		}
		return false;
	}
}

