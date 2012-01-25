package daris.client.model.user;

import java.util.List;
import java.util.Vector;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlStringWriter;
import arc.mf.object.ObjectRef;

public class RoleUserListRef extends ObjectRef<List<RoleUser>> {

	@Override
	protected void resolveServiceArgs(XmlStringWriter w) {

		w.add("type", "role");

	}

	@Override
	protected String resolveServiceName() {

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
	public String referentTypeName() {

		return "list of users";
	}

	@Override
	public String idToString() {

		return null;
	}

}
