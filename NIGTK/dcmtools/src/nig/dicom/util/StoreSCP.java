package nig.dicom.util;

import java.io.File;
import java.io.IOException;

import com.pixelmed.dicom.DicomException;
import com.pixelmed.network.DicomNetworkException;
import com.pixelmed.network.ReceivedObjectHandler;
import com.pixelmed.network.StorageSOPClassSCPDispatcher;

public class StoreSCP {

	static int port;
	static File rdir;
	static String aet;

	public static void main(String[] args) {

		try {
			for (int i = 0; i < args.length;) {
				if (args[i].equals("-port")) {
					port = Integer.parseInt(args[i + 1]);
					i += 2;
				} else if (args[i].equals("-rd")
						|| args[i].equals("--received-dir")) {
					rdir = new File(args[i + 1]);
					i += 2;
				} else if (args[i].equals("-aet")) {
					aet = args[i + 1];
					i += 2;
				}
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
		if (rdir == null) {
			System.err
					.println("Error: no directory specified for received files.");
			printUsage();
			System.exit(1);
		}
		if (port <= 0) {
			System.err.println("Error: port number " + port + " is not valid.");
			printUsage();
			System.exit(1);
		}
		try {
			new Thread((Runnable) new StorageSOPClassSCPDispatcher(port, aet,
					rdir, new MyReceivedObjectHandler(), 0)).start();
			System.out.println("StoreSCP started with following options:");
			System.out.println("port: " + port);
			System.out.println("AETitle: " + aet);
			System.out.println("FolderReceived:" + rdir.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace(System.err);
		}

	}

	private static void printUsage() {

		System.out.println("Usage:");
		System.out.println("\t STORESCP    [options]");
		System.out.println("Options:");
		System.out
				.println("\t -port  <port>               service port number");
		System.out
				.println("\t -rd  <received-directory>   the directory to save received files");
		System.out.println("\t -aet  <aetitle>             our AETitle");

	}

	private static class MyReceivedObjectHandler extends ReceivedObjectHandler {
		public void sendReceivedObjectIndication(String dicomFileName,
				String transferSyntax, String callingAETitle)
				throws DicomNetworkException, DicomException, IOException {

			File dstDir = new File(rdir.getAbsolutePath() + "/"
					+ callingAETitle);
			if (!dstDir.exists()) {
				dstDir.mkdirs();
			}
			File f = new File(dicomFileName);
			try {
				DicomFileOrganize.organizeDicomFile(f, dstDir);
			} finally {
				f.delete();
			}
		}
	}

}
