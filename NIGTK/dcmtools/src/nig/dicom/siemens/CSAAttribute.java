package nig.dicom.siemens;

import java.util.ArrayList;
import java.util.Iterator;

public class CSAAttribute {

	String name;
	int vm;
	String vr;
	int syngoDT;
	int nItems;
	String[] data;

	public CSAAttribute(String name, int vm, String vr, int syngoDT,
			int nItems, String[] data) {

		this.name = name;
		this.vm = vm;
		this.vr = vr;
		this.syngoDT = syngoDT;
		this.nItems = nItems;
		this.data = data;
	}

	public String dataString() {
		String str = "";
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				if (!data[i].equals("")) {
					str += data[i] + ",";
				}
			}
		}
		if (!str.equals("")) {
			str = str.substring(0, str.length() - 1);
		}
		return str;
	}

	public String toString() {
		return String
				.format(
						"<CSAElement name=\"%s\" VM=\"%d\" VR=\"%s\" SyngoDT=\"%d\" NumOfItems=\"%d\" data=\"%s\" />",
						name, vm, vr, syngoDT, nItems, dataString());
	}
	
	public String[] getData() {
		ArrayList<String> al = new ArrayList<String>(); 
		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				if (!data[i].equals("")) {
					al.add(data[i]);
				}
			}
		}
		String[] a = new String[al.size()];
		int i = 0;
		for(Iterator<String> it = al.iterator();it.hasNext();){
			a[i++] = (String) it.next();
		}
		return a;		
	}

}
