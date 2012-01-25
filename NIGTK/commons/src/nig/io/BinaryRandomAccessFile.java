package nig.io;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

public class BinaryRandomAccessFile implements BinaryDataInput,
		BinaryDataOutput, Closeable {

	private RandomAccessFile _raf;
	private boolean _littleEndian = false;

	public BinaryRandomAccessFile(File file, String mode, boolean littleEndian)
			throws FileNotFoundException {
		_raf = new RandomAccessFile(file, mode);
		_littleEndian = littleEndian;
	}

	public BinaryRandomAccessFile(String name, String mode, boolean littleEndian)
			throws FileNotFoundException {
		_raf = new RandomAccessFile(name, mode);
		_littleEndian = littleEndian;
	}

	public void readShortArray(short[] array) throws IOException {
		readShortArray(array, 0, array.length);
	}

	public void readShortArray(short[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readShort();
		}
	}

	public void readIntArray(int[] array) throws IOException {
		readIntArray(array, 0, array.length);
	}

	public void readIntArray(int[] array, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readInt();
		}
	}

	public void readFloatArray(float[] array) throws IOException {
		readFloatArray(array, 0, array.length);
	}

	public void readFloatArray(float[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readFloat();
		}
	}

	public void readLongArray(long[] array) throws IOException {
		readLongArray(array, 0, array.length);
	}

	public void readLongArray(long[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readLong();
		}
	}

	public void readDoubleArray(double[] array) throws IOException {
		readDoubleArray(array, 0, array.length);
	}

	public void readDoubleArray(double[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readDouble();
		}
	}

	public void skipFully(long n) throws IOException {
		_raf.seek(_raf.getFilePointer() + n);
	}

	public boolean isBigEndian() {
		return !_littleEndian;
	}

	public boolean isLittleEndian() {
		return _littleEndian;
	}

	public void setBigEndian() {
		_littleEndian = false;
	}

	public void setLittleEndian() {
		_littleEndian = true;
	}

	public long length() throws IOException {
		return _raf.length();
	}

	public int read() throws IOException {
		return _raf.read();
	}

	public int read(byte[] b) throws IOException {
		return _raf.read(b);
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return _raf.read(b, off, len);
	}

	public boolean readBoolean() throws IOException {
		return _raf.readBoolean();
	}

	public byte readByte() throws IOException {
		return _raf.readByte();
	}

	public char readChar() throws IOException {
		int ch1 = _raf.read();
		int ch2 = _raf.read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		return _littleEndian ? (char) ((ch2 << 8) + (ch1 << 0))
				: (char) ((ch1 << 8) + (ch2 << 0));
	}

	public double readDouble() throws IOException {
		if (_littleEndian) {
			return Double.longBitsToDouble(Long.reverseBytes(readLong()));
		} else {
			return readDouble();
		}
	}

	public float readFloat() throws IOException {
		if (_littleEndian) {
			return Float.intBitsToFloat(Integer.reverseBytes(readInt()));
		} else {
			return readFloat();
		}
	}

	public void readFully(byte[] b) throws IOException {
		readFully(b, 0, b.length);

	}

	public void readFully(byte[] b, int off, int len) throws IOException {
		_raf.readFully(b, off, len);
	}

	public int readInt() throws IOException {
		if (_littleEndian) {
			return Integer.reverseBytes(_raf.readInt());
		} else {
			return _raf.readInt();
		}
	}

	public String readLine() throws IOException {
		return _raf.readLine();
	}

	public long readLong() throws IOException {
		return _littleEndian ? Long.reverseBytes(_raf.readLong()) : _raf
				.readLong();
	}

	public short readShort() throws IOException {
		return _littleEndian ? Short.reverseBytes(_raf.readShort()) : _raf
				.readShort();
	}

	public String readUTF() throws IOException {
		return _raf.readUTF();
	}

	public int readUnsignedByte() throws IOException {
		return _raf.readUnsignedByte();
	}

	public int readUnsignedShort() throws IOException {
		int ch1 = _raf.read();
		int ch2 = _raf.read();
		if ((ch1 | ch2) < 0)
			throw new EOFException();
		return _littleEndian ? (ch2 << 8) + (ch1 << 0) : (ch1 << 8)
				+ (ch2 << 0);
	}

	public int skipBytes(int n) throws IOException {
		return _raf.skipBytes(n);
	}

	public void writeShortArray(short[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			writeShort(array[i]);
		}
	}

	public void writeShortArray(short[] array) throws IOException {
		writeShortArray(array, 0, array.length);
	}

	public void writeIntArray(int[] array, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			writeInt(array[i]);
		}
	}

	public void writeIntArray(int[] array) throws IOException {
		writeIntArray(array, 0, array.length);
	}

	public void writeFloatArray(float[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			writeFloat(array[i]);
		}
	}

	public void writeFloatArray(float[] array) throws IOException {
		writeFloatArray(array, 0, array.length);
	}

	public void writeLongArray(long[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			writeLong(array[i]);
		}
	}

	public void writeLongArray(long[] array) throws IOException {
		writeLongArray(array, 0, array.length);
	}

	public void writeDoubleArray(double[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			writeDouble(array[i]);
		}
	}

	public void writeDoubleArray(double[] array) throws IOException {
		writeDoubleArray(array, 0, array.length);
	}

	public void write(int b) throws IOException {
		_raf.write(b);
	}

	public void write(byte[] b) throws IOException {
		_raf.write(b);
	}

	public void write(byte[] b, int off, int len) throws IOException {
		_raf.write(b, off, len);
	}

	public void writeBoolean(boolean v) throws IOException {
		_raf.writeBoolean(v);
	}

	public void writeByte(int v) throws IOException {
		_raf.writeByte(v);
	}

	public void writeBytes(String s) throws IOException {
		_raf.writeBytes(s);
	}

	public void writeChar(int v) throws IOException {
		if (_littleEndian) {
			_raf.writeChar(((v >>> 8) & 0xFF) + ((v << 8) & 0xFF));
		} else {
			_raf.writeChar(v);
		}
	}

	public void writeChars(String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int v = s.charAt(i);
			writeChar(v);
		}
	}

	public void writeDouble(double v) throws IOException {
		if (_littleEndian) {
			_raf.writeLong(Long.reverseBytes(Double.doubleToLongBits(v)));
		} else {
			_raf.writeDouble(v);
		}
	}

	public void writeFloat(float v) throws IOException {
		if (_littleEndian) {
			_raf.writeInt(Integer.reverseBytes(Float.floatToIntBits(v)));
		} else {
			_raf.writeFloat(v);
		}
	}

	public void writeInt(int v) throws IOException {
		if (_littleEndian) {
			_raf.writeInt(Integer.reverseBytes(v));
		} else {
			_raf.writeInt(v);
		}
	}

	public void writeLong(long v) throws IOException {
		if (_littleEndian) {
			_raf.writeLong(Long.reverseBytes(v));
		} else {
			_raf.writeLong(v);
		}
	}

	public void writeShort(int v) throws IOException {
		if (_littleEndian) {
			_raf.writeShort(Short.reverseBytes((short) v));
		} else {
			_raf.writeShort(v);
		}
	}

	public void writeUTF(String str) throws IOException {
		_raf.writeUTF(str);
	}

	public void close() throws IOException {
		_raf.close();
	}

	public final long getFilePointer() throws IOException {
		return _raf.getFilePointer();
	}

	public final void seek(long pos) throws IOException {
		_raf.seek(pos);
	}

	public final FileChannel getChannel() {
		return _raf.getChannel();
	}

	public final FileDescriptor getFD() throws IOException {
		return _raf.getFD();
	}

	public void setLength(long newLength) throws IOException {
		_raf.setLength(newLength);
	}

}
