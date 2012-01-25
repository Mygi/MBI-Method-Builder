package daris.model.sc;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;


public class LayoutSetRef extends ObjectRef<List<Layout>>{
	
	private static LayoutSetRef _instance;

	public static LayoutSetRef get() {

		if (_instance == null) {
			_instance = new LayoutSetRef();
		}
		return _instance;
	}

	private LayoutSetRef() {

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		// No Arguments

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.shoppingcart.layout.list";
	}

	@Override
	protected List<Layout> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> les = xe.elements("layout");
			if (les != null) {
				Vector<Layout> ls = new Vector<Layout>();
				for (XmlElement le : les) {
					ls.add(new Layout(le));
				}
				return ls;
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "Layout";
	}

	@Override
	public String idToString() {

		return null;
	}

}
