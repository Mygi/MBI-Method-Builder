package nig.mf.plugin.pssd.services;

import arc.mf.plugin.*;
import arc.mf.plugin.dtype.*;
import arc.xml.*;
import nig.iio.dicom.DicomElements;
import nig.mf.plugin.pssd.method.*;
import nig.mf.pssd.plugin.util.DistributedAsset;

import java.util.*;

public class SvcExMethodStudyStepFind extends PluginService {
	private Interface _defn;

	public SvcExMethodStudyStepFind() throws Throwable {
		_defn = new Interface();
		Interface.Element me = new Interface.Element("id",CiteableIdType.DEFAULT, "The identity of the executing ExMethod object.", 1, 1);
		me.add(new Interface.Attribute("proute", CiteableIdType.DEFAULT,
				"In a federation, specifies the route to the peer that manages this citable ID.  If not supplied, then the object will be assumed to be local.", 0));
		_defn.add(me);
		_defn.add(new Interface.Element("type",StringType.DEFAULT,"The type of Study for which to find steps within the method(s).",0,1));

		// Flatten out the dicom/modality structure in pssd-method. Put back if need be.
		Interface.Element me2 = new Interface.Element("dicom-modality", new DictionaryEnumType(DicomElements.DICOM_MODALITY_DICTIONARY), "The DICOM modality for which to find steps within the method(s). If there is no DICOM modality registered with a step, this argument has no effect on the selection of that step.", 0, Integer.MAX_VALUE);
		me2.add(new Interface.Attribute("explicit", BooleanType.DEFAULT,
				"The DICOM modality must be held explicitly by the step. Otherwise (default), Steps  with no modality will also be found", 0));
		_defn.add(me2);
	}

	public String name() {
		return "om.pssd.ex-method.study.step.find";
	}

	public String description() {
		return "Finds Study Action steps that create the specified type of Study within an executing method.";

	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w) throws Throwable {


		// Find the ExMethod.  Can be primary or replica; we are not editing it here.
		DistributedAsset dEID = new DistributedAsset(args.element("id"));

		// Instantiate ExMethod from asset
		ExMethod em = ExMethod.lookup(executor(), dEID);

		// Regenerate Method object
		Method m = em.method();
		if ( m != null ) {

			String type = args.value("type");
			String dicomModality = args.value("dicom-modality");
			Boolean explicit = args.booleanValue("dicom-modality/@explicit", false);
			
			// TODO: push the dicom-modality test down a layer into studyActionStepPaths
			List<Method.StudyAction> sas = m.studyActionStepPaths(type);
			if ( sas != null ) {
				w.push("ex-method",new String[] { "id", dEID.getCiteableID() });
				for ( int i=0; i < sas.size(); i++ ) {
					Method.StudyAction sa = sas.get(i);

					// If we have the dicom element, flatten it out in the presentation
					XmlDoc.Element dicom = sa.dicom();
					String dicomModalityStep = null;
					if (dicom!=null) dicomModalityStep = dicom.value("modality");
					//
					boolean keep = true;
					if (dicomModality != null) {
						if (dicomModalityStep != null) {
							if (!dicomModality.equals(dicomModalityStep)) keep = false;
						} else {
							if (explicit) keep = false;
						}
					}
					//
					if (keep) {
						w.add("step",new String[] {"type", sa.type(), "dicom-modality", dicomModalityStep},
								sa.path());
					}
				}
				w.pop();
			}
		}

	}

}
