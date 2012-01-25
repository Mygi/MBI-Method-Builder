package nig.mf.plugin.pssd.ni;

import java.util.Collection;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;


public class SvcSubjectDecrypt extends PluginService {
	
	
	private Interface _defn;

	public SvcSubjectDecrypt() {
		_defn = new Interface();		
		_defn.add(new Interface.Element("id",CiteableIdType.DEFAULT, "The citable identifier of the local parent Project or individual Subject.  If none given, all Subjects in all Projects will be decrypted.", 0, 1));
		_defn.add(new Interface.Element("doctype",StringType.DEFAULT, "The name of a specific document type to be encrypted. If none given, will try to decrypt all documents in the private element.", 0, Integer.MAX_VALUE));
	}
	

	public String name() {
		return "nig.pssd.subject.decrypt";
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
		Collection docTypes = args.values("doctype");
		
		// Decrypt
		boolean encrypt = false;
		SvcSubjectEncrypt.encryptDecrypt (executor(), id, docTypes, encrypt,  w);
	}
}

