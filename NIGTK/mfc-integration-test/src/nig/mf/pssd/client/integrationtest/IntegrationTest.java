package nig.mf.pssd.client.integrationtest;

import nig.io.EraserThread;
import nig.mf.client.util.ClientConnection;

import arc.mf.client.ServerClient;
import arc.xml.XmlDoc;
import arc.xml.XmlStringWriter;


public class IntegrationTest {



	public static void main(String[] args) throws Throwable {

		// Connect to server
		ServerClient.Connection cxn = ClientConnection.createServerConnection();	


		// Authenticate
		ClientConnection.interactiveAuthenticate(cxn, "system", "manager");

		// Check we have PSSD package
		if (!packageExists(cxn, "PSSD")) {
			throw new Exception ("The PSSD package has not been installed.  You must install it first");
		}

		//
		doIt (cxn);
	}


	private static boolean packageExists  (ServerClient.Connection cxn, String name) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("package", name);
		XmlDoc.Element r = cxn.execute("package.exists", w.document());
		return r.booleanValue("exists");
	}

	
	// The work
	
	private static boolean doIt (ServerClient.Connection cxn) throws Throwable {
		
		// Generate some objects
		String[] t = createProject (cxn);                             // pid & mid
		if (t.length!=2) {
			throw new Exception ("Failed to generate Project with given Method " + t[0]);
		}
		if (!assetExists(cxn, t[0], true)) {
			throw new Exception ("Cannot locate expected newly created Project with cid " + t[0]);
		}
		//
		String[] t2 = createSubjectAndExMethod (cxn, t[0], t[1]);     // sid & exmid
		if (t2.length!=2) {
			throw new Exception ("Failed to generate Subject and ExMethod " + t[0]);
		}
		if (!assetExists(cxn, t2[0], true)) {
			throw new Exception ("Cannot locate expected newly created Subject with cid " + t2[1]);
		}
		if (!assetExists(cxn, t2[1], true)) {
			throw new Exception ("Cannot locate expected newly created ExMethod with cid " + t2[1]);
		}
		//
		String stid = createStudy (cxn, t2[1]);                       // stid
		if (stid==null) {
			throw new Exception ("Failed to generate Study " + t[0]);
		}
		if (!assetExists(cxn, stid, true)) {
			throw new Exception ("Cannot locate expected newly created Study with cid " + t2[1]);
		}	

		// Use the new Java-based DICOM client
		
		// Check some data out with format conversion to a sink.
		
		// CHekc format conversions
		
		// Replication test - server needs to be pre-configured
	
		return true;
	}
	
	
	private static Boolean assetExists (ServerClient.Connection cxn, String id, Boolean pssdOnly) throws Throwable {
		XmlStringWriter w = new XmlStringWriter();
		w.add("size", 1);
		w.add("action", "get-meta");
		w.add("where", "(cid='"+id+"')");
		w.add("pdist", "0");           // Local only
		XmlDoc.Element r = cxn.execute("asset.query", w.document());             
		
		// Parse
		Boolean exists = false;
		if (r.element("asset")!=null) {

			// Is it a PSSD object ?
			exists = true;
			if (pssdOnly) {
				String type = r.value("asset/meta/pssd-object/type");
				if (type == null) exists = false;
			}
		}
		return exists;

	}

	private static String[] createProject (ServerClient.Connection cxn) throws Throwable {
		System.out.println("*** Create Project");
		String num = EraserThread.readString("   Enter project number to re-use; [] for new: ");
		if (num.length()==0) num = null;
		String mid = EraserThread.readString ("   Enter Method CID to use (1 step matching data to upload) : ");
		if (mid.length()==0) {
			throw new Exception ("No Method CID given");
		}
//
		XmlStringWriter w = new XmlStringWriter();
		if (num!=null) {
			w.add("project-number", num);
		} else {
			w.add("fillin", 1);
		}
		w.add("name", "Integration Test");
		w.add("description", "Integration Test");
		//
		w.push("method");
		w.add("id", mid);
		w.pop();
		//
		w.push("member");
		w.add("domain", "system");
		w.add("user", "manager");
		w.add("role", "project-administrator");
		w.pop();
		//
		w.add("data-use", "specific");
		
		// Let it fail if CID taken
		XmlDoc.Element r = cxn.execute("om.pssd.project.create", w.document());
		String pid = r.value("id");
		String[] t = {pid, mid};
		return t;
	}

	private static String[] createSubjectAndExMethod (ServerClient.Connection cxn, String pid, String mid) throws Throwable {
		System.out.println("*** Create Subject");
		String num = EraserThread.readString("   Enter subject number to re-use; [] for new: ");
		if (num.length()==0) num = null;
//
		XmlStringWriter w = new XmlStringWriter();
		if (num!=null) {
			w.add("subject-number", num);
		} else {
			w.add("fillin", 1);
		}
		//
		w.add("pid", pid);
		w.add("method", mid);
		w.add("name", "Test Subject");
		XmlDoc.Element r = cxn.execute("om.pssd.subject.create", w.document());
		String sid= r.value("id");
		String eid = r.value("id/@mid");
		String[] t = {sid, eid};
		return t;
	}
	
	private static String createStudy (ServerClient.Connection cxn, String eid) throws Throwable {
		System.out.println("*** Create Study");
	
		// Describe the first step of the ExMethod to get the type
		XmlStringWriter w = new XmlStringWriter();
		w.add("id", eid);
		w.add("step", 1 );
		XmlDoc.Element r = cxn.execute("om.pssd.ex-method.step.describe", w.document());
		String type = r.value("ex-method/step/study/type");
		System.out.println("Study type from first step="+type);
		//
		String num = EraserThread.readString("   Enter study number to re-use; [] for new");
		if (num.length()==0) num = null;
//
	    w = new XmlStringWriter();
		if (num!=null) {
			w.add("study-number", num);
		} else {
			w.add("fillin", 1);
		}
		//
		w.add("type", type);
		w.add("pid", eid);
		w.add("name", "Test Study");
		r = cxn.execute("om.pssd.study.create", w.document());
		return r.value("id");
	}

}
