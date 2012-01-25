package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.EnumType;
import arc.xml.*;

import nig.mf.plugin.pssd.*;

public class SvcRepositoryDescribe extends PluginService {

	private Interface _defn;

	public SvcRepositoryDescribe() {

		// matches DocType pssd-repository-description

		_defn = new Interface();
		Interface.Element me = new Interface.Element("date-format", new EnumType(new String[] { "MF", "ANDS"}),
				"date format type; MF = standard MF DD-MMM-YYYY, ANDS = yyy-mm-dd", 0, 1);
		_defn.add(me);
	}

	public String name() {

		return "om.pssd.repository.describe";
	}

	public String description() {

		return "Describes the repository; includes database, UUID, CID root and the repository description record if set.";
	}

	public Interface definition() {

		return _defn;
	}

	public Access access() {

		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String dateFormat = args.stringValue("date-format", "MF");
		if (!dateFormat.equalsIgnoreCase("MF") && !dateFormat.equalsIgnoreCase("ANDS")) {
			throw new Exception("Illegal date format type");
		}
		Boolean useANDSDateFormat = dateFormat.equalsIgnoreCase("ANDS");

		
		
		// Server identity
		XmlDoc.Element r = executor().execute("server.identity");
		XmlDoc.Element sie = r.element("server");
		String uuid = r.value("server/uuid");

		// Database
		r = executor().execute("server.database.describe");
		XmlDoc.Element de = r.element("database");

		// Citeable id root for projects & methods
		r = executor().execute("citeable.named.id.describe");
		String projectIdRoot = r.value("id[@name='pssd.project']");
		String methodIdRoot = r.value("id[@name='pssd.method']");

		// Repository descrition
		w.push("repository");
		w.add("id", new String[] { "method-id-root", methodIdRoot,
				"project-id-root", projectIdRoot }, uuid);

		// Repository
		r = RepositoryDescription.getRepositoryDescription(executor(), useANDSDateFormat);
		if (r != null) {
			w.add(r, false);
		}

		w.add(sie);
		w.add(de);
		w.pop();

	}
}
