package daris.model.object;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;
import daris.model.repository.RepositoryRootRef;

public class PSSDObjectRefSetRef extends ObjectRef<PSSDObjectRefSet> {

	private PSSDObjectRef _parent;

	private int _size = Integer.MAX_VALUE;
	private boolean _foredit = false;

	public PSSDObjectRefSetRef(PSSDObjectRef parent) {

		_parent = parent;

	}

	@Override
	public String idToString() {

		return _parent.id();

	}

	@Override
	protected PSSDObjectRefSet instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			if (xe.elements("object") != null || xe.elements("proute") != null) {
				return new PSSDObjectRefSet(xe);
			}
		}
		return null;

	}

	@Override
	public String referentTypeName() {

		return "PSSDObjectSet";

	}

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		if (_parent instanceof RepositoryRootRef) {
			return;
		}
		w.add("id", ((PSSDObjectRef) _parent).id());
		w.add("isleaf", true);
		if (_size == Integer.MAX_VALUE) {
			w.add("size", "infinity");
		} else {
			w.add("size", _size);
		}
		w.add("foredit", _foredit);

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.collection.members";

	}

}
