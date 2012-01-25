package nig.iio.bruker;

import java.io.File;

public class Test {

	public static void main(String[] args) throws Throwable {
		 BrukerMeta acqp = new BrukerMeta(new File("D:/SampleData/pvdata/wilson/nmr/81.2.1z1/6/acqp"));
		 BrukerMeta method = new BrukerMeta(new File("D:/SampleData/pvdata/wilson/nmr/81.2.1z1/6/method"));
		 BrukerMeta reco = new BrukerMeta(new File("D:/SampleData/pvdata/wilson/nmr/81.2.1z1/6/pdata/1/reco"));
		 BrukerMeta subject = new BrukerMeta(new File("D:/SampleData/pvdata/wilson/nmr/81.2.1z1/subject"));

		 System.out.println(method.toString());
		 
//		 System.out.println(reco.get("RECO_wordtype"));
//		 System.out.println(reco.get("RECO_byte_order"));
//		 System.out.println(reco.get("RECO_size"));
//		 System.out.println(reco.get("RECO_transposition"));

//		 String studyID = subject.getValueAsString("SUBJECT_study_nr");
//		 System.out.println(studyID);
//		 String studyUID = subject.getValueAsString("SUBJECT_study_instance_uid");
//		 System.out.println(studyUID);
//		 String seriesUID = reco.getValueAsString("RECO_base_image_uid");
//		 System.out.println(seriesUID);
//		 String acqTime = acqp.getValueAsString("ACQ_time");
//		 System.out.println(acqTime);
//		 String protocol = acqp.getValueAsString("ACQ_protocol_name");
//		 System.out.println(protocol);
//		 String seriesDescription = acqp.getValueAsString("ACQ_scan_name");
//		 System.out.println(seriesDescription);

//		String s = "<A0 #1 B 0> <D #1 B 828> <D #2 B 828> <D #3 B 829> <D #4 B 826> <D #5 B 828> <D #6 B 830> <D #7 B 831> <D #8 B 828> <D #9 B 830> <D #10 B 831> <D #11 B 826> <D #12 B 827> <D #13 B 825> <D #14 B 833> <D #15 B 823> <D #16 B 829> <D #17 B 829> <D #18 B 827> <D #19 B 824> <D #20 B 821> <D #21 B 830> <D #22 B 822> <D #23 B 832> <D #24 B 831> <D #25 B 826> <D #26 B 831> <D #27 B 824> <D #28 B 832> <D #29 B 824> <D #30 B 828>";
//		String[] ss = s.split("> <");
//		for (int i = 0; i < ss.length; i++)
//			System.out.println(ss[i]);
	}
}
