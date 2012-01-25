package daris.client.model.user.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class UserSelfPasswordSet extends ObjectMessage<Boolean> {

	private String _oldPassword;
	private String _password;

	public UserSelfPasswordSet(String oldPassword, String password) {
		_oldPassword = oldPassword;
		_password = password;
	}

	@Override
	protected void messageServiceArgs(XmlWriter w) {
		w.add("old-password", _oldPassword);
		w.add("password", _password);
	}

	@Override
	protected String messageServiceName() {
		return "user.self.password.set";
	}

	@Override
	protected Boolean instantiate(XmlElement xe) throws Throwable {
		if (xe != null) {
			return true;
		}
		return null;
	}

	@Override
	protected String objectTypeName() {
		return null;
	}

	@Override
	protected String idToString() {
		return null;
	}

}
