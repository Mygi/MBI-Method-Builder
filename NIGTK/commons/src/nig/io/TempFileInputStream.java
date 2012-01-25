package nig.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TempFileInputStream extends FileInputStream {
	
	private File _file;

	public TempFileInputStream(File file) throws FileNotFoundException {

		super(file);
		_file = file;

	}

	public void close() throws IOException {
	
		super.close();
		_file.delete();

	}
}
