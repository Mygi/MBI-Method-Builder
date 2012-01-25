package daris.client.model.object;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class DObjectChildrenRef extends ObjectRef<List<DObjectRef>> {

	private String _id;
	private String _proute;
	private boolean _sorted;
	private Comparator<DObjectRef> _comparator;

	DObjectChildrenRef(String id, String proute,
			Comparator<DObjectRef> comparator) {

		_id = id;
		_proute = proute;		
		_comparator = comparator;
		_sorted = false;
	}

	public void sort(Comparator<DObjectRef> comparator) {

		_comparator = comparator;
		if (resolved()) {
			Collections.sort(referent(), _comparator);
			_sorted = true;
		} else {
			_sorted = false;
			resolve();
		}
	}

	public boolean sorted() {

		return _sorted;
	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		if (_id != null) {
			if (_proute != null) {
				// TODO:
				w.add("id", _id);
			} else {
				w.add("id", _id);
			}
		}

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.collection.member.list";
	}

	@Override
	protected List<DObjectRef> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> oes = xe.elements("object");
			if (oes != null) {
				List<DObjectRef> os = new Vector<DObjectRef>();
				for (XmlElement oe : oes) {
					DObjectRef o = new DObjectRef(DObject.create(oe), false,
							false);
					os.add(o);
				}
				if (!os.isEmpty()) {
					if (_comparator != null) {
						Collections.sort(os, _comparator);
						_sorted = true;
					} else {
						Collections.sort(os);
					}
					return os;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "list of pssd-object members";
	}

	@Override
	public String idToString() {

		return _id;
	}

}
