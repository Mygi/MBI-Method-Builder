package nig.mf.plugin.pssd.ni;


import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.StringType;
import arc.xml.XmlDocMaker;
import arc.xml.XmlWriter;
import arc.xml.XmlDoc.Element;

public class SvcDICOMUserCreate extends PluginService {

	private Interface _defn;

	public SvcDICOMUserCreate()  {
		_defn = new Interface();
		_defn.add(new Interface.Element("user", StringType.DEFAULT, "The DICOM username.", 1, 1));
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public Interface definition() {
		return _defn;
	}

	public String description() {
		return "Creates a standard  DICOM user and assigns the PSSD and NIG-PSSD DICOM ingest roles.  Each DICOM client AET must have a proxy DICOM user in the system.";
	}

	public String name() {
		return "nig.pssd.dicom.user.create";
	}

	public void execute(Element args, Inputs inputs, Outputs outputs, XmlWriter w) throws Throwable {
		// Inputs
		String user = args.value("user");

		// Create user
		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("user", user);
		executor().execute("om.pssd.dicom.user.create", dm.root());

		// Grant NIG-PSSD role
		dm = new XmlDocMaker("args");
		dm.add("domain", "dicom");
		dm.add("user", user);
		dm.add("role", "nig.pssd.dicom-ingest");
		executor().execute("om.pssd.user.role.grant", dm.root());
	}
}
