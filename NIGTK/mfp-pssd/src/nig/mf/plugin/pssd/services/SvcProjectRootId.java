package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;


public class SvcProjectRootId extends PluginService {
	private Interface _defn;

	public SvcProjectRootId() {
		_defn = null;
	}

	public String name() {
		return "om.pssd.project.root";
	}

	public String description() {
		return "Returns the root identifier for projects.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {
		String proute = null;
		w.add("id",nig.mf.pssd.plugin.util.CiteableIdUtil.projectIDRoot(executor(), proute));
	}
}
