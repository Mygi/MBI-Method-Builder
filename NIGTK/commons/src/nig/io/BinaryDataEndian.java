package nig.io;

public interface BinaryDataEndian {
	
	boolean isBigEndian();
	
	boolean isLittleEndian();
	
	void setBigEndian();
	
	void setLittleEndian();

}
