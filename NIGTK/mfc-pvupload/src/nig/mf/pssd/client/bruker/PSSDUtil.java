package nig.mf.pssd.client.bruker;

import arc.mf.client.ServerClient;
import arc.xml.XmlStringWriter;

public class PSSDUtil {

	// These are dictionary terms (pssd.study.types). They could be anything but this
	// is what they are for our NIG installation.  I don't know how to avoid setting
	// them here.
	public static final String DICOM_STUDY_TYPE = "Magnetic Resonance Imaging";
	public static final String BRUKER_STUDY_TYPE = "Magnetic Resonance Imaging";
	public static final String BRUKER_META_NAMESPACE = "bruker";

	public static final String BRUKER_LOG_FILE = "bruker";
	public static final int LOG_ERROR = 1;
	public static final int LOG_WARNING = 2;
	public static final int LOG_INFO = 3;

	/**
	 * Send a notification email from the mediaflux server to specified email address.
	 * 
	 * @param cxn
	 * @param to
	 * @param subject
	 * @param body
	 * @throws Throwable
	 */
	public static void mail(ServerClient.Connection cxn, String to, String subject, String body) throws Throwable {

		XmlStringWriter w = new XmlStringWriter();
		w.add("async", "true");
		w.add("to", to);
		w.add("subject", subject);
		w.add("body", body);
		cxn.execute("mail.send", w.document());

	}

	/**
	 * Write message to mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @param type
	 * @throws Throwable
	 */
	private static void log(ServerClient.Connection cxn, String msg, int type) throws Throwable {

		String event = "info";
		if (type == LOG_ERROR) {
			event = "error";
		} else if (type == LOG_WARNING) {
			event = "warning";
		} else if (type == LOG_INFO) {
			event = "info";
		}
		XmlStringWriter w = new XmlStringWriter();
		w.add("app", BRUKER_LOG_FILE);
		w.add("event", event);
		w.add("msg", msg);
		cxn.execute("server.log", w.document());

	}

	/**
	 * Write error message into mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @throws Throwable
	 */
	public static void logError(ServerClient.Connection cxn, String msg) throws Throwable {

		log(cxn, msg, LOG_ERROR);

	}

	/**
	 * write warning message into mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @throws Throwable
	 */
	public static void logWarning(ServerClient.Connection cxn, String msg) throws Throwable {

		log(cxn, msg, LOG_WARNING);

	}

	/**
	 * write informational message into mediaflux server log.
	 * 
	 * @param cxn
	 * @param msg
	 * @throws Throwable
	 */
	public static void logInfo(ServerClient.Connection cxn, String msg) throws Throwable {

		log(cxn, msg, LOG_INFO);

	}

}
