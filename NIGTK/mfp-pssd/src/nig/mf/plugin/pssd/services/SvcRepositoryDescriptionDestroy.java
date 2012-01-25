package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.xml.*;

import nig.mf.plugin.pssd.*;

public class SvcRepositoryDescriptionDestroy extends PluginService {
	
	private Interface _defn;

	public SvcRepositoryDescriptionDestroy() {
		
		// matches DocType pssd-repository-description
		
		_defn = new Interface();

		
	}

	public String name() {
		return "om.pssd.repository.description.destroy";
	}

	public String description() {
		return "Destroy the repository description record.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		RepositoryDescription.destroyRepositoryDescription(executor());
	}
}
