package nig.mf.plugin.pssd.services;

import nig.mf.plugin.pssd.PSSDObject;
import arc.mf.plugin.PluginService;
import arc.mf.plugin.dtype.CiteableIdType;
import arc.xml.XmlDoc;
import arc.xml.XmlWriter;

public class SvcDataSetCount extends PluginService {
	private Interface _defn;

	public SvcDataSetCount() throws Throwable {
		_defn = new Interface();
		Interface.Element me = new Interface.Element(
				"pid",
				CiteableIdType.DEFAULT,
				"The citeable id of the root/parent object. If not specified, all the datasets in the repository will be counted.",
				0, 1);
		me.add(new Interface.Attribute(
				"proute",
				CiteableIdType.DEFAULT,
				"In a federation, must specify the route to the peer that manages this citable id.  If not supplied, the object is assumed to be local.",
				0));
		_defn.add(me);
	}

	public String name() {
		return "om.pssd.dataset.count";
	}

	public String description() {
		return "Count the number of datasets belong to the specified object.";
	}

	public Interface definition() {
		return _defn;
	}

	public Access access() {
		return ACCESS_ACCESS;
	}

	public void execute(XmlDoc.Element args, Inputs in, Outputs out, XmlWriter w)
			throws Throwable {

		String pid = args.value("pid");
		String proute = args.value("id/@proute");
		int count = PSSDObject.countDataSets(executor(), proute, pid);
		w.add("count", count);
	}

}