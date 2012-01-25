package daris.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObject;
import daris.model.object.PSSDObjectRef;

/**
 * @author wilson
 * 
 */
public class ObjectDescribe extends ObjectMessage<XmlElement> {

	private String _proute;

	private String _id;

	private boolean _foredit;

	public ObjectDescribe(String id, String proute, boolean foredit) {

		_proute = proute;
		_id = id;
		_foredit = foredit;

	}

	public ObjectDescribe(PSSDObjectRef ref, boolean foredit) {

		this(ref.proute(), ref.id(), foredit);

	}

	public ObjectDescribe(PSSDObject o, boolean foredit) {

		this(o.proute(), o.id(), foredit);

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_proute != null) {
			w.add("id", new String[] { "proute", _proute }, _id);
		} else {
			w.add("id", _id);
		}
		w.add("foredit", _foredit);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.describe";

	}

	@Override
	protected XmlElement instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement oe = xe.element("object");
			return oe;
		}
		return null;

	}

	@Override
	protected String objectTypeName() {

		return "PSSDObject";
	}

	@Override
	protected String idToString() {

		return _id;
	}

}
