package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc.Element;
import arc.xml.XmlWriter;

public class SvcDICOMDataSetCount extends PluginService {

	private Interface _defn;

	public SvcDICOMDataSetCount() throws Throwable {

		_defn = new Interface();
		Interface.Element me = new Interface.Element(
				"pid",
				CiteableIdType.DEFAULT,
				"The citealbe id of the root/parent object. If not specified, all the dicom datasets in the repository will be counted.",
				0, 1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable ID.  If not supplied, the object is assumed to be local.",
				0));
		_defn.add(me);
	}

	@Override
	public Access access() {

		return ACCESS_ACCESS;
	}

	@Override
	public Interface definition() {

		return _defn;
	}

	@Override
	public String description() {

		return "Count the number of dicom datasets in the specified pssd object.";
	}

	@Override
	public void execute(Element args, Inputs inputs, Outputs outputs,
			XmlWriter w) throws Throwable {

		String pid = args.value("pid");
		String proute = args.value("id/@proute");
		int count = PSSDObject.countDicomDataSets(executor(), proute, pid);
		w.add("count", count);
	}

	@Override
	public String name() {

		return "om.pssd.dicom.dataset.count";
	}

}
