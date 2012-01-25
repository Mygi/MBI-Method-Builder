package nig.mf.plugin.pssd;

import java.util.Collection;

import nig.mf.plugin.pssd.user.Authority;
import nig.mf.plugin.pssd.user.ModelUserRoleSet;
import nig.mf.plugin.pssd.user.UserCredential;
import arc.mf.plugin.ServerRoute;
import arc.mf.plugin.ServiceExecutor;
import arc.xml.XmlDoc;
import arc.xml.XmlDocMaker;

/**
 * Current user's session.
 * 
 * @author Jason Lohrey
 * 
 */
public class ModelUser {

	public static class ExNotAuthorized extends Throwable {
		public ExNotAuthorized(String role) {

			super("Not authorized - require the following role: " + role);
		}

		public ExNotAuthorized(String serverUUID, String role) {

			super("Not authorized on server " + serverUUID + " - requires the following role: " + role);
		}

	}

	/**
	 * Establish if the caller has the given role
	 * 
	 * @param route
	 * @param executor
	 * @param role
	 * @throws Throwable
	 */
	public static void checkHasRole(ServerRoute route, ServiceExecutor executor, String role) throws Throwable {

		if (hasRole(route, executor, role)) {
			return;
		}
		if (route == null) {
			throw new ExNotAuthorized(role);
		} else {
			throw new ExNotAuthorized(route.target(), role);
		}
	}

	/**
	 * Establish if the caller has the given role
	 * 
	 * @param route
	 * @param executor
	 * @param role
	 * @throws Throwable
	 */
	public static boolean checkHasRoleNoThrow(ServerRoute route, ServiceExecutor executor, String role)
			throws Throwable {

		return hasRole(route, executor, role);
	}

	public static String modelUserRoleName() {

		return Role.modelUserRoleName();
	}

	/**
	 * Establish if the given user has the given role
	 * 
	 * @param route
	 * @param executor
	 * @param authority
	 *            Can be null for local authority.
	 * @param domain
	 * @param user
	 * @param role
	 * @return
	 * @throws Throwable
	 */
	public static boolean hasRole(ServerRoute route, ServiceExecutor executor, Authority authority, String domain,
			String user, String role) throws Throwable {

		// What authority will we get on a remote server... Should be the peer.
		XmlDocMaker dm = new XmlDocMaker("args");
		if (authority != null) {
			dm.add(authority.toXmlElement());
		}
		dm.add("domain", domain);
		dm.add("user", user);
		dm.add("role", new String[] { "type", "role" }, role);
		XmlDoc.Element r = executor.execute(route, "user.describe", dm.root());
		if (r == null) {
			return false;
		}
		return (r.element("user") != null);
	}

	/**
	 * Establish if the calling user has the given role
	 * 
	 * @param route
	 * @param executor
	 * @param role
	 * @return
	 * @throws Throwable
	 */
	public static boolean hasRole(ServerRoute route, ServiceExecutor executor, String role) throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("role", new String[] { "type", "role" }, role);

		// When executed on a remote server, the authority that is used will be
		// numbered
		// explicitly by the peer. E.g. If this is called from 101 an executed
		// on
		// peer 1005 (peer 1005 actor.self.have) the authority used on peer 1005
		// will be 101
		// So it must be explicitly created there

		XmlDoc.Element r = executor.execute(route, "actor.self.have", dm.root());
		return r.booleanValue("role");
	}

	/**
	 * Establish if the specified role has been granted the given
	 * project-specific role
	 * 
	 * @param route
	 * @param executor
	 * @param role
	 * @param projectRole
	 * @return
	 * @throws Throwable
	 */
	public static boolean hasRole(ServerRoute route, ServiceExecutor executor, String role, String projectRole)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", "role");
		dm.add("name", role);
		dm.add("role", new String[] { "type", "role" }, projectRole);

		XmlDoc.Element r = executor.execute(route, "actor.have", dm.root());
		return r.booleanValue("actor/role");
	}

	public static ModelUserRoleSet selfRoles(ServerRoute sroute, ServiceExecutor executor) throws Throwable {

		XmlDoc.Element r = executor.execute(sroute, "actor.self.describe");
		Collection<String> roles = r.values("actor/role");
		if (roles == null) {
			return null;
		}
		return new ModelUserRoleSet(roles);
	}

	public static ModelUserRoleSet selfRoles(ServiceExecutor executor) throws Throwable {

		return selfRoles(null, executor);
	}

	public static ModelUserRoleSet userRoles(ServerRoute sroute, ServiceExecutor executor, UserCredential uc)
			throws Throwable {

		XmlDocMaker dm = new XmlDocMaker("args");
		dm.add("type", "user");
		dm.add("name", uc.toString());
		XmlDoc.Element r = executor.execute(sroute, "actor.describe", dm.root());
		Collection<String> roles = r.values("actor/role");
		if (roles == null) {
			return null;
		}
		return new ModelUserRoleSet(roles);
	}

	public static ModelUserRoleSet userRoles(ServiceExecutor executor, UserCredential uc) throws Throwable {

		return userRoles(null, executor, uc);
	}

}
