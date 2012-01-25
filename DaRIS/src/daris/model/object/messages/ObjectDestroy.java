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
public class ObjectDestroy extends ObjectMessage<Boolean> {

	private String _proute;
	private String _id;
	private boolean _checkRemoteChildren = false;
	private boolean _allowDestroyProject = false;
	private boolean _destroyCiteableID = false;

	public ObjectDestroy(String proute, String id) {

		_proute = proute;
		_id = id;

	}

	public ObjectDestroy(String id) {

		this(null, id);

	}

	public ObjectDestroy(PSSDObjectRef ref) {

		this(ref.proute(), ref.id());

	}

	@Override
	protected String idToString() {
		return _id;
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {

		return true;

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_proute == null) {
			w.add("id", _id);
		} else {
			// w.add("id", new String[] { "proute", _proute }, _id);
			// TODO: current proute is not used in om.pssd.object.destroy service. 
			// TODO: check with Neil.
			w.add("id", _id);
		}
		w.add("destroy", _allowDestroyProject);
		w.add("destroyCID", _destroyCiteableID);
		w.add("check-remote-children", _checkRemoteChildren);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.object.destroy";

	}

	@Override
	protected String objectTypeName() {

		return "boolean";

	}

}
