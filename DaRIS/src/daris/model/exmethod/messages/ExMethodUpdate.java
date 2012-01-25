package daris.model.exmethod.messages;

import arc.mf.client.xml.XmlWriter;
import daris.model.exmethod.ExMethod;
import daris.model.exmethod.ExMethodRef;
import daris.model.object.messages.ObjectUpdate;

public class ExMethodUpdate extends ObjectUpdate {

	public ExMethodUpdate(ExMethodRef ref) {

		super(ref);

	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.ex-method.update";

	}

	@Override
	protected String objectTypeName() {

		return ExMethod.TYPE_NAME;

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		super.messageServiceArgs(w);
		// TODO: update state
		// ExMethodRef em = (ExMethodRef) object();
		// if(em.state()!=null) { w.add("state", em.state()); }

	}

}
