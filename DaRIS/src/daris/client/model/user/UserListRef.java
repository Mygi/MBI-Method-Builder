package daris.client.model.user;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class UserListRef extends ObjectRef<List<User>> {

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("type", "user");

	}

	@Override
	protected String resolveServiceName() {

		return "om.pssd.user.describe";
	}

	@Override
	protected List<User> instantiate(XmlElement xe) throws Throwable {

		if (xe != null) {
			List<XmlElement> ues = xe.elements("user");
			if (ues != null) {
				List<User> us = new Vector<User>(ues.size());
				for (XmlElement ue : ues) {
					us.add(new User(ue));
				}
				if (!us.isEmpty()) {
					return us;
				}
			}
		}
		return null;
	}

	@Override
	public String referentTypeName() {

		return "list of role users";
	}

	@Override
	public String idToString() {

		return null;
	}

}
