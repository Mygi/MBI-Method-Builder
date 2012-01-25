package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.dicom.DicomAssetHandlerFactory;

import arc.mf.plugin.PluginService;

import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcDICOMControls extends PluginService {
	private Interface _defn;

	public SvcDICOMControls() throws Throwable {
	}

	public String name() {
		return "om.pssd.dicom.controls";
	}

	public String description() {
		return "Lists the available DICOM controls for configuring the PSSD DICOM engine.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_MODIFY;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {
		
		DicomAssetHandlerFactory.list(w);
		
	}

}
