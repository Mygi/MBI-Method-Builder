package daris.model.object.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.model.object.PSSDObjectRef;

/**
 * 
 * @author wilson
 * 
 */
public class ObjectExists extends ObjectMessage<Boolean> {

	private String _id;
	private String _proute;

	public ObjectExists(String proute, String id) {

		_proute = proute;
		_id = id;

	}

	public ObjectExists(String id) {

		this(null, id);

	}

	public ObjectExists(PSSDObjectRef ref) {

		this(ref.proute(), ref.id());

	}
	
	@Override
	protected String idToString() {

		return _id;

	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			if (xe.element("exists") != null) {
				return xe.booleanValue("exists");
			}
		}
		return false;

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_proute == null) {
			w.add("id", _id);
		} else {
			w.add("id", new String[] { "proute", _proute }, _id);
		}

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.exists";

	}

	@Override
	protected String objectTypeName() {

		return "boolean";

	}

}
