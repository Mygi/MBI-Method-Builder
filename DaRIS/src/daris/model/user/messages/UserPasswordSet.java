package daris.model.user.messages;

import arc.mf.client.xml.XmlElement;
import arc.mf.client.xml.XmlWriter;
import arc.mf.object.ObjectMessage;

public class UserPasswordSet extends ObjectMessage<Boolean> {
	private String _oldPassword;
	private String _password;
	
	public UserPasswordSet(String oldPassword, String password){
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

		return true;
	}

	@Override
	protected String objectTypeName() {

		return "password";
	}

	@Override
	protected String idToString() {

		return "password";
	}
}
