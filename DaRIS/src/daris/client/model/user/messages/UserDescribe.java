package daris.client.model.user.messages;

import java.util.List;
import java.util.Vector;

import daris.client.model.user.User;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class UserDescribe extends ObjectMessage<List<User>> {
	private String _authority;
	private String _protocol;
	private String _domain;

	public UserDescribe(String authority, String protocol, String domain) {

		_authority = authority;
		_protocol = protocol;
		_domain = domain;
	}

	public UserDescribe(String domain) {

		this(null, null, domain);
	}

	public UserDescribe() {

		this(null, null, null);
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {

		if (_authority != null) {
			if (_protocol != null) {
				w.add("authority", new String[] { "protocol", _protocol },
						_authority);
			} else {
				w.add("authority", _authority);
			}

		}
		if (_domain != null) {
			w.add("domain", _domain);
		}

	}

	@Override
	protected String messageServiceName() {

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
	protected String objectTypeName() {

		return "list of users";
	}

	@Override
	protected String idToString() {

		return null;
	}

}
