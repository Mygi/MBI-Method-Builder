package daris.client.model.user.messages;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;
import daris.client.model.user.RoleUser;

public class RoleUserDescribe extends ObjectMessage<List<RoleUser>> {

	public RoleUserDescribe() {

	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		w.add("type", "role");
	}

	@Override
	protected String messageServiceName() {

		return "om.pssd.user.describe";
	}

	@Override
	protected List<RoleUser> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> rues = xe.elements("role-user");
			if (rues != null) {
				List<RoleUser> rus = new Vector<RoleUser>(rues.size());
				for (XmlElement rue : rues) {
					rus.add(new RoleUser(rue));
				}
				if (!rus.isEmpty()) {
					return rus;
				}
			}
		}
		return null;
	}

	@Override
	protected String objectTypeName() {

		return "list of role-users";
	}

	@Override
	protected String idToString() {

		return null;
	}

}
