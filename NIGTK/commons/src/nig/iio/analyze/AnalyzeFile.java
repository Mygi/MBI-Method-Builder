package nig.iio.analyze;

import java.io.DataOutput;
import java.io.File;
import java.io.InputStream;


public class AnalyzeFile {
	
	private File _hdrFile;
	private File _imgFile;
	private AnalyzeHeader _hdr;
	private boolean _bigEndian;
	
	public AnalyzeFile(String name, boolean bigEndian) {
		_hdr = new AnalyzeHeader();
		_hdrFile = new File(name + ".hdr");
		_imgFile = new File(name + ".img");
		_bigEndian = bigEndian;
	}
	
	public AnalyzeFile(String name, AnalyzeHeader hdr, boolean bigEndian){
		_hdr = hdr;
		_hdrFile = new File(name + ".hdr");
		_imgFile = new File(name + ".img");
		_bigEndian = bigEndian;
	}
	
	
	public void setHeader(AnalyzeHeader hdr){
		_hdr = hdr;
	}
	
	public AnalyzeHeader header(){
		return _hdr;
	}
	
	public File headerFile(){
		return _hdrFile;
	}
	
	public File imageFile(){
		return _imgFile;
	}
	
	public boolean isBigEndian(){
		return _bigEndian;
	}
	
	public boolean isLittleEndian(){
		return !_bigEndian;
	}
	
	public void saveHeader() throws Throwable{
		if(_hdr!=null&&_hdrFile!=null){
			_hdr.write(_hdrFile, _bigEndian);
		}
	}
	
	public void saveImage(InputStream in){
		
	}

}
