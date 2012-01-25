package nig.dicom.siemens;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;

import com.pixelmed.dicom.AttributeList;
import com.pixelmed.dicom.DicomInputStream;
import com.pixelmed.dicom.ValueRepresentation;

public class CSAAttributeList {

	private LinkedHashMap<String, CSAAttribute> nameCSAAttributeMap = new LinkedHashMap<String, CSAAttribute>();
	private float[] CSAData;


	public CSAAttributeList(File f) throws Throwable {
		AttributeList al = new AttributeList();
		DicomInputStream dis;
		dis = new DicomInputStream(f);
		al.read(dis, CSAConstants.TAG_CSAImageHeaderInfo);
		readCSADataInfo(dis);
		dis.close();
		al.clear();
		
		dis = new DicomInputStream(f);
		al.read(dis, CSAConstants.TAG_CSASeriesHeaderInfo);
		readCSADataInfo(dis);
		dis.close();
		al.clear();
		
		dis = new DicomInputStream(f);
		al.read(dis, CSAConstants.TAG_CSAData);
		readCSAData(dis);
		dis.close();
		al.clear();
	}

	private void readCSADataInfo(DicomInputStream dis) throws Throwable {

		byte[] vr = new byte[2];
		int vl;
		if (dis.getTransferSyntaxInUse().isExplicitVR()) {
			dis.readInsistently(vr, 0, 2);

			if (ValueRepresentation.isShortValueLengthVR(vr)) {
				vl = dis.readSigned16();
			} else {
				dis.skipInsistently(2);
				vl = dis.readSigned32();
			}
		} else {
			vl = dis.readSigned32();
		}

		if (vl != 0xffffffff) {
			dis.mark(vl);
		}

		// Now ready to read the value

		byte[] buffer = new byte[4];
		// "SV10"
		dis.readInsistently(buffer, 0, 4);
		if (!bytesToString(buffer, 0, 4, true).equals("SV10")) {
			throw new Exception(
					"Error parsing CSA attribute. Could not found 'SV10'");
		}
		// "\4\3\2\1"
		dis.readInsistently(buffer, 0, 4);
		if (!(buffer[0] == 4 && buffer[1] == 3 && buffer[2] == 2 && buffer[3] == 1)) {
			throw new Exception(
					"Error parsing CSA attribute. Could not found '\4\3\2\1' or 04030201.");
		}

		// number of elements: int/4bytes
		int numOfElements = dis.readSigned32();

		// unused 0x0000004d (M)
		if (dis.readSigned32() != 0x4d) {
			throw new Exception(
					"Error parsing CSA attribute. Could not found  4d000000.");
		}
		for (int i = 0; i < numOfElements; i++) {
			CSAAttribute csaa = readCSAAttribute(dis);
			nameCSAAttributeMap.put(csaa.name, csaa);
		}
	}

	private CSAAttribute readCSAAttribute(DicomInputStream dis)
			throws Throwable, Throwable {
		byte[] buffer = new byte[64];

		// name: 64bytes string
		dis.readInsistently(buffer, 0, 64);
		String name = bytesToString(buffer, 0, 64, true);
		// vm: 4bytes int
		int vm = dis.readSigned32();
		// vr: 4bytes string the 3rd and 4th char are unused 0x00.
		dis.readInsistently(buffer, 0, 4);
		String vr = bytesToString(buffer, 0, 2, true);
		// syngoDT: 4bytes int
		int syngoDT = dis.readSigned32();
		// nItems: 4bytes int
		int nItems = dis.readSigned32();
		// padding x: 4bytes: cd 00 00 00 / 4d 00 00 00
		int x = dis.readSigned32();
		if (!(x == 0xcd || x == 0x4d)) {
			throw new Exception(
					"Error parsing CSA attribute. Could not found  4d000000 or cd000000.");
		}
		String[] data = new String[nItems];
		for (int i = 0; i < nItems; i++) {
			int[] xx = new int[4];
			// length: int same as xx[1] xx[3]
			xx[0] = dis.readSigned32();
			// length: int same as xx[0] xx[1]
			xx[1] = dis.readSigned32();
			// padding x: int 0x4d or 0xcd
			xx[2] = dis.readSigned32();
			// length: int same as xx[0] xx[1]
			xx[3] = dis.readSigned32();
			if (!(xx[2] == 0x4d || xx[2] == 0xcd)) {
				throw new Exception(
						" Error parsing CSA element item. Padding 0x4d or 0xcd for item length is not found.");
			}
			if (!(xx[0] == xx[1] && xx[1] == xx[3])) {
				throw new Exception(" Error parsing CSA element item length.");
			}
			int length = xx[0];
			buffer = new byte[length];
			dis.readInsistently(buffer, 0, length);
			data[i] = bytesToString(buffer, 0, length, true);
			// skip the padding 00
			dis.skip((4 - length % 4) % 4);
		}
		CSAAttribute csaa = new CSAAttribute(name, vm, vr, syngoDT, nItems,
				data);
		return csaa;
	}

	private static String bytesToString(byte[] b, int offset, int length,
			boolean trim) throws Throwable {
		if (b == null) {
			throw new NullPointerException();
		} else if (offset < 0 || length < 0 || length > b.length - offset) {
			throw new IndexOutOfBoundsException();
		}
		String s = "";
		for (int i = offset; i < offset + length; i++) {
			if (trim) {
				if (b[i] == 0) {
					break;
				}
			}
			s += (char) b[i];
		}
		if (trim) {
			s = s.trim();
		}
		return s;
	}

	private void readCSAData(DicomInputStream dis) throws IOException {
		byte[] vr = new byte[2];
		int vl;
		if (dis.getTransferSyntaxInUse().isExplicitVR()) {
			dis.readInsistently(vr, 0, 2);
			if (ValueRepresentation.isShortValueLengthVR(vr)) {
				vl = dis.readSigned16();
			} else {
				dis.skipInsistently(2);
				vl = dis.readSigned32();
			}
		} else {
			vl = dis.readSigned32();
		}

		CSAData = new float[vl / 4];
		dis.readFloat(CSAData, CSAData.length);
	}

	public CSAAttribute getCSAAttribute(String name) {
		return nameCSAAttributeMap.get(name);
	}

	public float[] getCSAData() {
		return CSAData;
	}

}
