package nig.mf.pssd.client.dicom;


import java.io.File;
import java.io.PrintStream;

import arc.mf.client.ServerClient;

import nig.mf.client.util.ClientConnection;


/**
 * Client to upload data to a DICOM server with the PSSD engine with Mediaflux service interface
 * 
 * @author nebk
 *
 */

public class DicomMF {



	public static void main(String[] args) throws Throwable {


		// Parse Inputs
		if (args.length==0) {
			printHelp(System.out);
			System.exit(1);
		}
		//
		String id = null;                       // Citable ID 
		File dir = null;                        // Directory holding data
		Boolean check = true;                   // Check supplied files are DICOM
		Boolean clean = true;                   // Clean up temporary files
		String domain = "system";
		String user = "manager";
		String host = "localhost";
		String transport = "https";
		String port = "8443";
		Boolean anonName = true;
		String metaSetService = "nig.pssd.subject.meta.set";
		//
		for (int i = 0; i < args.length; i++) {
			if (args[i].equalsIgnoreCase("-help")) {
				printHelp(System.out);
				System.exit(0);
			} else if (args[i].equalsIgnoreCase("-id")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -id option must specify a CID");
					System.exit(1);
				}
				id = new String(args[i]);
			} else if (args[i].equalsIgnoreCase("-dir")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -dir option must specify a directory String");
					System.exit(1);
				}
				dir = new File(args[i]);
			} else if (args[i].equalsIgnoreCase("-k")) {
				clean = false;
			} else if (args[i].equalsIgnoreCase("-nochk")) {
				check = false;
			} else if (args[i].equalsIgnoreCase("-noanon")) {
				anonName = false;
			} else if (args[i].equalsIgnoreCase("-domain")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -domain option must specify a String");
					System.exit(1);
				}
				domain = args[i];
			} else if (args[i].equalsIgnoreCase("-user")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -user option must specify a String");
					System.exit(1);
				}
				user = args[i];
			} else if (args[i].equalsIgnoreCase("-host")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -host option must specify a String");
					System.exit(1);
				}
				host = args[i];
			} else if (args[i].equalsIgnoreCase("-port")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -port option must specify a String");
					System.exit(1);
				}
				port = args[i];
			} else if (args[i].equalsIgnoreCase("-transport")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -transport option must specify a String");
					System.exit(1);
				}
				transport = args[i];
			} else if (args[i].equalsIgnoreCase("-metasetservice")) {
				if (++i == args.length) {
					System.err.println("DicomMF: error: -metasetservice option must specify a String");
					System.exit(1);
				}
				metaSetService = args[i];
				if (metaSetService.equalsIgnoreCase("none")) metaSetService = null;
			} else {
				System.err.println("DicomMF: error: unexpected argument" + args[i]);
				printHelp(System.err);
				System.exit(1);
			}
		}
		
		//
		if (dir==null) {
			System.err.println("DicomMF: you must supply argument -dir");
			System.exit(1);
		}
		if (id==null) {
			System.err.println("DicomMF: you must supply argument -id");
			System.exit(1);
		}

		System.out.println("Supplied Arguments:");
		System.out.println("  directory        = " + dir);
		System.out.println("  check            = " + check);
		System.out.println("  id               = " + id);
		System.out.println("  anon name        = " + anonName);
		System.out.println("  meta set service = " + metaSetService);
		System.out.println("  cleanup          = " + clean);
		System.out.println("");
		System.out.println("  host             = " + host);
		System.out.println("  port             = " + port);
		System.out.println("  transport        = " + transport);
		System.out.println("  domain           = " + domain);
		System.out.println("  user             = " + user);
		
		System.out.println("\n\n");


		// Connect to server
		ServerClient.Connection cxn = ClientConnection.createServerConnection(host, port, transport);

		// Authenticate
		ClientConnection.interactiveAuthenticate(cxn, domain, user);
		
		// Upload via DICOM client to DICOM server.
		Dicom.uploadMF(cxn, id, check, clean, dir, metaSetService, anonName);
	}



	private static void printHelp (PrintStream os) {
		os.println("DicomClient");
		os.println();
		os.println("Synopsis:");
		os.println("     Uploads DICOM data to the specified Mediaflux DICOM server PSSD or PSS engine.");
		os.println();
		os.println("Usage:");
		os.println("   " + DicomMF.class.getName() + " [options..] ");
		os.println();
		os.println("Options:");
		os.println("   -help       Displays this help.");
		os.println("   -dir        Directory holding DICOM files. Is traversed recursively to find all files.");
		os.println("   -nochk      Don't check files  found in 'dir' are DICOM; speeds the process up");
		os.println("   -k          Don't clean up intermediary files");
		os.println("");
		os.println("   -id         Citeable ID of parent object to store the data under in a PSSD tree.");
		os.println("               Should ALWAYS be a full CID with leading UUID.NS components");
		os.println("               If not given, then the data are uploaded with no ID specification");
		os.println("   -metasetservice");
		os.println("               Service to set domain-sepcific meta-data on PSSD objects. Defaults to");
		os.println("                  nig.pssd.subject.meta.set. Set to 'none' if none required");
		os.println("");
		os.println("   -host       Mediaflux host (default is 'localhost')");
		os.println("   -port       Mediaflux port (default is '8443'");
		os.println("   -transport  Mediaflux transport (default is 'https'); one of 'https', 'http', 'tcpip'");
		os.println("   -domain     Domain (default is 'system') of Mediaflux user to authenticate with");
		os.println("   -user       Mediaflux user (default is 'manager') to authenticate with");
		os.println();
	}

}
