package daris.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class DestinationSetRef extends ObjectRef<List<Destination>> {

	private static DestinationSetRef _instance;

	public static DestinationSetRef get() {

		if (_instance == null) {
			_instance = new DestinationSetRef();
		}
		return _instance;
	}

	private DestinationSetRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		// No Arguments

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.destination.list";
	}

	@Override
	protected List<Destination> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> des = xe.elements("destination");
			if (des != null) {
				Vector<Destination> ds = new Vector<Destination>();
				for (XmlElement de : des) {
					ds.add(new Destination(de));
				}
				return ds;
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "destination";
	}

	@Override
	public String idToString() {

		return null;
	}

}
