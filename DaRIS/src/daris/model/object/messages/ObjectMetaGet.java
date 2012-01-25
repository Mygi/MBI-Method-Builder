package daris.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObject;
import daris.model.object.PSSDObjectMeta;
import daris.model.object.PSSDObjectMetaFactory;
import daris.model.object.PSSDObjectRef;

/**
 * @author wilson
 * 
 */
public class ObjectMetaGet extends ObjectMessage<PSSDObjectMeta> {
	private String _proute;

	private String _id;

	private boolean _foredit;

	public ObjectMetaGet(String id, String proute, boolean foredit) {

		_proute = proute;
		_id = id;
		_foredit = foredit;
	}

	public ObjectMetaGet(PSSDObjectRef ref, boolean foredit) {

		this(ref.id(), ref.proute(), foredit);
	}

	public ObjectMetaGet(PSSDObject o, boolean foredit) {

		this(o.id(), o.proute(), foredit);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_proute != null) {
			w.add("proute", _proute);
		}
		w.add("id", _id);
		w.add("foredit", _foredit);
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.describe";
	}

	@Override
	protected PSSDObjectMeta instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			XmlElement oe = xe.element("object");
			if (oe != null) {
				return PSSDObjectMetaFactory.instantiate(oe);
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "XmlElement";
	}

	@Override
	protected String idToString() {

		return _id;
	}
}
