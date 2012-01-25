package daris.model.sc.archive;

public class ISO9660Archive extends Archive {

	public enum IsoType {
		cd, dvd_single, dvd_double;

		public String toString() {

			return super.toString().replace("_", "-");
		}

		public static IsoType typeFor(String type) {

			if (type.equalsIgnoreCase("dvd-single")
					|| type.equalsIgnoreCase("dvd_single")) {
				return dvd_single;
			} else if (type.equalsIgnoreCase("dvd-double")
					|| type.equalsIgnoreCase("dvd_double")) {
				return dvd_double;
			} else {
				return cd;
			}
		}
	}

	protected ISO9660Archive() {

		this(IsoType.cd, null, null, true, true);
	}

	protected ISO9660Archive(IsoType isoType, String volumeName,
			String publisher, boolean enableJoliet, boolean enableRockridge) {

		super(Archive.Type.iso9660);
		setIsoType(isoType);
		setVolumeName(volumeName);
		setPublisher(publisher);
		setJoliet(enableJoliet);
		setRockridge(enableRockridge);
	}

	public void setRockridge(boolean enableRockridge) {

		setParameter("enable-rockridge", Boolean.toString(enableRockridge));
	}

	public void setJoliet(boolean enableJoliet) {

		setParameter("enable-joliet", Boolean.toString(enableJoliet));

	}

	public void setPublisher(String publisher) {

		setParameter("publisher", publisher);

	}

	public void setVolumeName(String volumeName) {

		setParameter("volume-name", volumeName);

	}

	public void setIsoType(IsoType isoType) {

		setParameter("iso-type", isoType.toString());

	}

	public IsoType isoType() {

		String sv = parameterValue("iso-type");
		if (sv == null) {
			return IsoType.cd;
		} else {
			return IsoType.typeFor(sv);
		}
	}

	public String volumeName() {

		return parameterValue("volume-name");
	}

	public String publisher() {

		return parameterValue("publisher");
	}

	public boolean isJoliet() {

		String sv = parameterValue("enable-joliet");
		if (sv == null) {
			return true;
		} else {
			return Boolean.parseBoolean(sv);
		}
	}

	public boolean isRockridge() {

		String sv = parameterValue("enable-rockridge");
		if (sv == null) {
			return true;
		} else {
			return Boolean.parseBoolean(sv);
		}
	}


}
