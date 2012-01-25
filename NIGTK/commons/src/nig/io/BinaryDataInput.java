package nig.io;

import java.io.IOException;

public interface BinaryDataInput extends BinaryDataEndian {
	
    void readFully(byte b[]) throws IOException;

    void readFully(byte b[], int off, int len) throws IOException;

    int skipBytes(int n) throws IOException;

    boolean readBoolean() throws IOException;

    byte readByte() throws IOException;

    int readUnsignedByte() throws IOException;
    
    char readChar() throws IOException;

    short readShort() throws IOException;

    int readUnsignedShort() throws IOException;

    int readInt() throws IOException;

    long readLong() throws IOException;

    float readFloat() throws IOException;

    double readDouble() throws IOException;

	void readShortArray(short[] array, int off, int len) throws IOException;

	void readShortArray(short[] array) throws IOException;
	
	void readIntArray(int[] array, int off, int len) throws IOException;
	
	void readIntArray(int[] array) throws IOException;
	
	void readFloatArray(float[] array, int off, int len) throws IOException;
	
	void readFloatArray(float[] array) throws IOException;
	
	void readLongArray(long[] array, int off, int len) throws IOException;
	
	void readLongArray(long[] array) throws IOException;
	
	void readDoubleArray(double[] array, int off, int len) throws IOException;
	
	void readDoubleArray(double[] array) throws IOException;

	void skipFully(long n) throws IOException;

}
