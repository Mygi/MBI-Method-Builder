package nig.io;

import java.io.IOException;

public interface BinaryDataOutput extends BinaryDataEndian {
	
    void write(int b) throws IOException;

    void write(byte b[]) throws IOException;

    void write(byte b[], int off, int len) throws IOException;

    void writeBoolean(boolean v) throws IOException;

    void writeByte(int v) throws IOException;

    void writeShort(int v) throws IOException;

    void writeChar(int v) throws IOException;

    void writeInt(int v) throws IOException;

    void writeLong(long v) throws IOException;

    void writeFloat(float v) throws IOException;

    void writeDouble(double v) throws IOException;

    void writeBytes(String s) throws IOException;

    void writeChars(String s) throws IOException;

	void writeShortArray(short[] array, int off, int len) throws IOException;
	
	void writeShortArray(short[] array) throws IOException;

	void writeIntArray(int[] array, int off, int len) throws IOException;
	
	void writeIntArray(int[] array) throws IOException;

	void writeFloatArray(float[] array, int off, int len) throws IOException;
	
	void writeFloatArray(float[] array) throws IOException;

	void writeLongArray(long[] array, int off, int len) throws IOException;
	
	void writeLongArray(long[] array) throws IOException;

	void writeDoubleArray(double[] array, int off, int len) throws IOException;
	
	void writeDoubleArray(double[] array) throws IOException;
	
}
