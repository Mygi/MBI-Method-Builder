package daris.model.sc.archive;



public class TarArchive extends Archive {

	protected TarArchive() {

		super(Archive.Type.tar);
	}
	
	public static TarArchive DEFAULT = new TarArchive();
}
