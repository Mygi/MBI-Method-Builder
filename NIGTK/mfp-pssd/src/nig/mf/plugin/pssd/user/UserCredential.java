package nig.mf.plugin.pssd.user;

public class UserCredential {

	private Authority _authority = null; // value and protocol
	private String _domain = null;
	private String _user = null;

	/**
	 * 
	 * 
	 * @param authority
	 * @param domain
	 * @param user
	 * @throws Throwable
	 */
	public UserCredential(Authority authority, String domain, String user) throws Throwable {

		_authority = authority;
		_domain = domain;
		_user = user;
	}

	/**
	 * 
	 * @param authority
	 * @param protocol
	 * @param domain
	 * @param user
	 * @throws Throwable
	 */
	public UserCredential(String authority, String protocol, String domain, String user) throws Throwable {

		if (authority != null) {
			_authority = new Authority(authority, protocol);
		} else {
			_authority = null;
		}
		_domain = domain;
		_user = user;
	}

	public boolean hasAuthority() {

		return _authority != null;
	}

	public Authority authority() {

		return _authority;
	}

	public String authorityName() {

		if (_authority != null) {
			return _authority.name();
		} else {
			return null;
		}
	}

	public String authorityProtocol() throws Throwable {

		if (_authority != null) {
			return _authority.protocol();
		} else {
			return null;
		}
	}

	public String domain() {

		return _domain;
	}

	public String user() {

		return _user;
	}

	public void setAuthority(Authority authority) {

		_authority = authority;
	}

	public void setAuthority(String authority, String protocol) {

		_authority = new Authority(authority, protocol);
	}

	public void setDomain(String domain) {

		_domain = domain;
	}

	public void setUser(String user) {

		_user = user;
	}

	@Override
	public String toString() {

		if (_authority != null) {
			return _authority.name() + ":" + _domain + ":" + _user;
		} else {
			return _domain + ":" + _user;
		}
	}

	/**
	 * parse user credential from string in the format of
	 * 'authority:domain:user' or 'domain:user'
	 * 
	 * @param credential
	 *            credential string in the format of 'authority:domain:user' or
	 *            'domain:user'
	 * @return
	 * @throws Throwable
	 */
	public static UserCredential parse(String credential) throws Throwable {

		String[] tokens = credential.split(":");
		if (tokens.length == 3) {
			return new UserCredential(tokens[0], null, tokens[1], tokens[2]);
		} else if (tokens.length == 2) {
			return new UserCredential(null, null, tokens[0], tokens[1]);
		} else {
			throw new Exception("Unexpected format for user credential:  " + credential);
		}
	}

}
