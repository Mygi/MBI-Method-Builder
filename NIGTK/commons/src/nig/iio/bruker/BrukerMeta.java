package nig.iio.bruker;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Vector;

public class BrukerMeta extends LinkedHashMap<String, BrukerMeta.Element> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 64258618307174503L;

	public static class Element {

		public final String name;

		public final String[] values;

		public final int[] dimensions;

		public Element(String name, String[] values, int[] dimensions) throws Exception {

			this.name = name;
			this.values = values;
			this.dimensions = dimensions;
			int p = 1;
			/*
			System.out.println("name=" + name);
			System.out.println("   values.size = " + values.length);
			System.out.println("   dimensions.size = " + dimensions.length);
			*/
			for (int i = 0; i < values.length; i++) {
				//System.out.println("   value=" + values[i]);
			}
			for (int i = 0; i < dimensions.length; i++) {
				//System.out.println("   dim=" + dimensions[i]);
				p *= dimensions[i];
			}
			if (p != values.length) {
				throw new Exception("Error parsing element: " + name + ". Wrong dimensions.");
			}

		}

		public Element(String name, String value) throws Exception {

			this(name, new String[] { value }, new int[] { 1 });

		}

		public String toString() {

			String s = name;
			if (dimensions.length > 1) {
				for (int i = 0; i < dimensions.length; i++) {
					s += "[" + dimensions[i] + "]";
				}
			} else {
				if (dimensions[0] > 1) {
					s += "[" + dimensions[0] + "]";
				}
			}
			s += " = ";
			for (int i = 0; i < values.length; i++) {
				s += values[i];
				if (i < values.length - 1) {
					s += ", ";
				}
			}

			return s;

		}

		public String stringValue() {

			String s = values[0];
			if (values.length == 1) {
				return s;
			} else {
				for (int i = 1; i < values.length; i++) {
					s += " " + values[i];
				}
				return s;
			}

		}

		public String[] stringValues() {

			return values;

		}

		public short[] shortValues() throws Throwable {

			short[] svs = new short[values.length];
			for (int i = 0; i < values.length; i++) {
				svs[i] = Short.parseShort(values[i]);
			}
			return svs;

		}

		public int[] intValues() throws Throwable {

			int[] ivs = new int[values.length];
			for (int i = 0; i < values.length; i++) {
				ivs[i] = Integer.parseInt(values[i]);
			}
			return ivs;

		}

		public float[] floatValues() throws Throwable {

			float[] fvs = new float[values.length];
			for (int i = 0; i < values.length; i++) {
				fvs[i] = Float.parseFloat(values[i]);
			}
			return fvs;

		}

		public double[] doubleValues() throws Throwable {

			double[] dvs = new double[values.length];
			for (int i = 0; i < values.length; i++) {
				dvs[i] = Double.parseDouble(values[i]);
			}
			return dvs;

		}

	}

	public BrukerMeta(File file) throws Throwable {
		readFile(file);
	}

	private void readFile(File file) throws Throwable {

		LineNumberReader reader = new LineNumberReader(new InputStreamReader(new FileInputStream(file)));
		try {
			String line = reader.readLine();
			while (line != null) {
				if (line.startsWith("##END=")) {
					break;
				} else if (line.startsWith("##$")) {
					String[] kv = line.substring(3).split("=");
					if (kv[1].startsWith("(<") && kv[1].endsWith(">)")) {
						kv[1] = kv[1].substring(1);
						kv[1] = kv[1].substring(0, kv[1].length() - 1);
						Vector<String> vector = new Vector<String>();
						if (kv[1].indexOf(",") != -1) {
							String[] vvs = kv[1].split(",");
							for (int i = 0; i < vvs.length; i++) {
								String vv = vvs[i];
								vv = vv.trim();
								if (vv.startsWith("<")) {
									vv = vv.substring(1);
								}
								if (vv.endsWith(">")) {
									vv = vv.substring(0, vv.length() - 1);
								}
								vector.add(vv);
							}
						} else {
							vector.add(kv[1].trim());
						}
						String[] values = new String[vector.size()];
						vector.toArray(values);
						put(kv[0], new Element(kv[0], values, new int[] { vector.size() }));
					} else if (kv[1].startsWith("( ") && kv[1].endsWith(" )")) {
						kv[1] = kv[1].substring(1);
						kv[1] = kv[1].substring(0, kv[1].length() - 1);
						kv[1] = kv[1].trim();
						if (kv[1].indexOf(",") != -1) {
							String[] ds = kv[1].split(",");
							int[] dims = new int[ds.length];
							for (int i = 0; i < ds.length; i++) {
								dims[i] = Integer.parseInt(ds[i].trim());
							}
							Vector<String> vector = new Vector<String>();
							String lines = "";
							line = reader.readLine();
							while (!line.startsWith("##") && !line.startsWith("$$")) {
								lines += line;
								line = reader.readLine();
							}
							String[] vs;
							if (lines.startsWith("<")) {
								vs = lines.split("> <");
								vs[0] = vs[0].substring(1);
								vs[vs.length - 1] = vs[vs.length - 1].substring(0, vs[vs.length - 1].length() - 1);
								dims = new int[] { vs.length };
							} else {
								vs = lines.split(" ");
							}
							for (int i = 0; i < vs.length; i++) {
								vector.add(vs[i].trim());
							}
							String[] values = new String[vector.size()];
							vector.toArray(values);
							put(kv[0], new Element(kv[0], values, dims));
							continue;
						} else {
							int[] dims = new int[] { Integer.parseInt(kv[1]) };
							Vector<String> vector = new Vector<String>();
							String lines = "";
							line = reader.readLine();
							while (!line.startsWith("##") && !line.startsWith("$$")) {
								lines += line;
								line = reader.readLine();
							}

							if (lines.startsWith("<") && lines.endsWith(">")) {
								String value = lines;
								value = value.substring(1);
								value = value.substring(0, value.length() - 1);
								dims[0] = 1;
								vector.add(value);
							} else {
								String delim = " ";
								if (lines.startsWith("(") && lines.endsWith(")")) {
									lines = lines.replaceAll(", ", ",");
									
									// Need a special character to split on. Can't use space because
									// strings have embedded spaces.  I think + is safe.
									delim = "\\+";
									lines = lines.replaceAll("\\) ", "\\)"+delim);
								}

								String[] vs = lines.split(delim);
								for (int i = 0; i < vs.length; i++) {
									vector.add(vs[i].trim());
								}
							}
							String[] values = new String[vector.size()];
							vector.toArray(values);
							put(kv[0], new Element(kv[0], values, dims));
							continue;
						}
					} else {
						put(kv[0], new Element(kv[0], new String[] { kv[1] }, new int[] { 1 }));
					}
				} else if (line.startsWith("##")) {
					String[] kv = line.substring(2).split("=");
					put(kv[0], new Element(kv[0], new String[] { kv[1] }, new int[] { 1 }));
				} else if (line.startsWith("$$")) {
					// do nothing
				}
				line = reader.readLine();
			}
		} finally {
			reader.close();
		}
	}

	public String toString() {

		String s = "";
		for (Iterator<String> it = keySet().iterator(); it.hasNext();) {
			String key = it.next();
			Element value = get(key);
			s += value.toString() + "\n";
		}
		return s;

	}

	public String getValueAsString(String name) {

		Element e = get(name);
		if (e == null) {
			return null;
		}
		return e.stringValue();

	}

	public String[] getValueAsStringArray(String name) {

		return get(name).values;

	}

	public short[] getValueAsShortArray(String name) throws Throwable {

		return get(name).shortValues();

	}

	public int[] getValueAsIntArray(String name) throws Throwable {

		return get(name).intValues();

	}

	public float[] getValueAsFloatArray(String name) throws Throwable {

		return get(name).floatValues();

	}

	public double[] getValueAsDoubleArray(String name) throws Throwable {

		return get(name).doubleValues();

	}

}
