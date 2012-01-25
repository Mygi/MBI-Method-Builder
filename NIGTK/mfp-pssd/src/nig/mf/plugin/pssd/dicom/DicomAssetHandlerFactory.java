package nig.mf.plugin.pssd.dicom;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import arc.mf.plugin.dicom.DicomAssetEngine;
import arc.mf.plugin.dicom.DicomAssetEngineFactory;
import arc.xml.XmlWriter;

public class DicomAssetHandlerFactory implements DicomAssetEngineFactory {

	public static final String TYPE_NAME = "nig.dicom";
	public static final String DESCRIPTION = "Handles PSSD data - stores data in an ex-method and study for a project and subject. Calls 'pss' engine (if configured) to handle PSS and non-citeable DICOM data.";
	
	public Map<String, String> arguments() {
		Map<String,String> args = new TreeMap<String,String>();
		args.put("nig.dicom.asset.namespace.root", "The root namespace in which to create assets. If not specified, then root namespace will be used.");
		args.put("nig.dicom.id.by", "The method of identifying studies using P.S[.EM[.S]] (project, subject, ex-method, study) notation. If specified, one of [patient.id, patient.name, patient.name.first, patient.name.last, study.id].");
		args.put("nig.dicom.id.ignore-non-digits","Specifies whether non-digits in part of an element should be ignored when constructing a P.S.EM.S identifier. One of [false,true]. Defaults to false.");
		args.put("nig.dicom.id.prefix", "If specified, the value to be prepended to any P.S.EM.S identifier.");
		args.put("nig.dicom.subject.create", "If true, will auto-create Subjects if the identifier is of the form P.S and the Subject does not exist.");
		args.put("nig.dicom.subject.meta.set-service", "Service to populate domain-specific meta-data on Subject objects.");
		return args;
	}

	public Object createConfiguration(Map<String, String> args) throws Throwable {
		DicomIngestControls ic = new DicomIngestControls();
		if ( args == null ) {
			args = new HashMap<String,String>();
		}
		
		ic.configure(args);
		return ic;
	}

	public String description() {
		return DESCRIPTION;
	}

	public DicomAssetEngine instantate() {
		return new DicomAssetHandler();
	}

	public String type() {
		return TYPE_NAME;
	}

	public static void list (XmlWriter w) throws Throwable {
		DicomAssetHandlerFactory ah = new DicomAssetHandlerFactory();
		Map<String,String> map = ah.arguments();
		Set<String> keys = map.keySet();
		for (String key : keys) {
			String value = map.get(key);
			w.add(key,value);
		}
	}
}
