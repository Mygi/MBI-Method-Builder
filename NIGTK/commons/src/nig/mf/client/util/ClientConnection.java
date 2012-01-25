package nig.mf.client.util;



import nig.io.EraserThread;
import arc.mf.client.RemoteServer;
import arc.mf.client.ServerClient;

public class ClientConnection  {


	/**
	 * Open a connection to a Mediaflux server by reading properties set in the 
	 * command line call to the executing Java class
	 * 
	 * @return
	 * @throws Throwable
	 */
	public static ServerClient.Connection createServerConnection(String host, String portS, String transport) throws Throwable {
		boolean useHttp = false;
		boolean encrypt = false;
		if (host == null) {
			throw new Exception("Mediaflux host not specfied");
		}

		if (portS == null) {
			throw new Exception("Mediaflux port not specified");
		}
		int port = Integer.parseInt(portS);

		if (transport == null) {
			throw new Exception("Mediaflux transport not specified");
		}

		if (transport.equalsIgnoreCase("TCPIP")) {
			useHttp = false;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTP")) {
			useHttp = true;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTPS")) {
			useHttp = true;
			encrypt = true;
		} else {
			throw new Exception("Unexpected transport: " + transport + ", expected one of [tcpip,http,https]");
		}

		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = server.open();
		return cxn;
	}

	
	public static ServerClient.Connection createServerConnection() throws Throwable {
		boolean useHttp = false;
		boolean encrypt = false;
		String host = System.getProperty("mf.host");
		if (host == null) {
			throw new Exception("Cannot find system property 'mf.host'");
		}

		String p = System.getProperty("mf.port");
		if (p == null) {
			throw new Exception("Cannot find system property 'mf.port'");
		}
		int port = Integer.parseInt(p);

		String transport = System.getProperty("mf.transport");
		if (transport == null) {
			throw new Exception("Cannot find system property 'mf.transport'");
		}

		if (transport.equalsIgnoreCase("TCPIP")) {
			useHttp = false;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTP")) {
			useHttp = true;
			encrypt = false;
		} else if (transport.equalsIgnoreCase("HTTPS")) {
			useHttp = true;
			encrypt = true;
		} else {
			throw new Exception("Unexpected transport: " + transport + ", expected one of [tcpip,http,https]");
		}

		RemoteServer server = new RemoteServer(host, port, useHttp, encrypt);
		ServerClient.Connection cxn = server.open();
		return cxn;
	}





	/**
	 * Authenticate to the server by prompting interactively for the p/w
	 *
	 * @param cxn
	 * @throws Throwable
	 */
	public static void interactiveAuthenticate (ServerClient.Connection cxn, String domain, String user) throws Throwable {
		EraserThread et = new EraserThread("Enter password for " + domain+":"+user +":");
		Thread mask = new Thread(et);
		mask.start();
		String pw = EraserThread.readString(null);
		et.stopMasking();

		// Authenticate
		cxn.connect(domain, user, pw);
	}
	
	

}
