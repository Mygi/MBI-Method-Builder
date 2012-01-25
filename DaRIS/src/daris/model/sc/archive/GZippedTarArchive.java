package daris.model.sc.archive;

public class GZippedTarArchive extends CompressedArchive {

	protected GZippedTarArchive() {

		super(Archive.Type.gzipped_tar);
	}

}
