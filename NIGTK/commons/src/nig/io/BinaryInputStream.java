package nig.io;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BinaryInputStream extends FilterInputStream implements
		BinaryDataInput {

	/**
	 * Constructor.
	 * 
	 * @param in
	 *            InputStream
	 * @param littleEndian
	 *            true if it is little endian
	 */
	public BinaryInputStream(InputStream in, boolean littleEndian) {
		super(in instanceof BufferedInputStream ? in : new BufferedInputStream(
				in));
		_littleEndian = littleEndian;
	}

	/**
	 * Constructor. Assumes it is big endian.
	 * 
	 * @param in
	 *            InputStream
	 * 
	 */
	public BinaryInputStream(InputStream in) {
		this(in, false);
	}

	/**
	 * Number of bytes has been read.
	 */
	private long _read = 0;

	/**
	 * If it is little endian.
	 */
	private boolean _littleEndian = false;

	public synchronized int read() throws IOException {
		int b = in.read();
		_read++;
		return b;
	}

	public synchronized final int read(byte b[], int off, int len)
			throws IOException {
		return in.read(b, off, len);
	}

	public final int read(byte b[]) throws IOException {
		return read(b, 0, b.length);
	}

	public final void readFully(byte b[], int off, int len) throws IOException {
		if (len < 0) {
			throw new IndexOutOfBoundsException();
		}
		int n = 0;
		while (n < len) {
			int count = read(b, off + n, len - n);
			if (count < 0) {
				throw new EOFException();
			}
			n += count;
		}
	}

	public final void readFully(byte b[]) throws IOException {
		readFully(b, 0, b.length);
	}

	public synchronized long skip(long n) throws IOException {
		long count = skip(n);
		_read += count;
		return count;
	}

	public final int skipBytes(int n) throws IOException {
		int total = 0;
		int cur = 0;
		while ((total < n) && ((cur = (int) skip(n - total)) > 0)) {
			total += cur;
		}
		return total;
	}

	public final boolean readBoolean() throws IOException {
		int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (ch != 0);
	}

	public final byte readByte() throws IOException {
		int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return (byte) (ch);
	}

	public final int readUnsignedByte() throws IOException {
		int ch = read();
		if (ch < 0) {
			throw new EOFException();
		}
		return ch;
	}

	public final short readShort() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		if (_littleEndian) {
			return (short) ((ch2 << 8) + (ch1 << 0));
		} else {
			return (short) ((ch1 << 8) + (ch2 << 0));
		}
	}

	public final int readUnsignedShort() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		if (_littleEndian) {
			return (ch2 << 8) + (ch1 << 0);
		} else {
			return (ch1 << 8) + (ch2 << 0);
		}
	}

	public final char readChar() throws IOException {
		int ch1 = read();
		int ch2 = read();
		if ((ch1 | ch2) < 0) {
			throw new EOFException();
		}
		if (_littleEndian) {
			return (char) ((ch2 << 8) + (ch1 << 0));
		} else {
			return (char) ((ch1 << 8) + (ch2 << 0));
		}
	}

	private byte[] _intBuffer = new byte[4];

	public final int readInt() throws IOException {
		readFully(_intBuffer, 0, 4);
		if (_littleEndian) {
			return (((_intBuffer[3] & 0xff) << 24)
					| ((_intBuffer[2] & 0xff) << 16)
					| ((_intBuffer[1] & 0xff) << 8) | (_intBuffer[0] & 0xff));
		} else {
			return (((_intBuffer[0] & 0xff) << 24)
					| ((_intBuffer[1] & 0xff) << 16)
					| ((_intBuffer[2] & 0xff) << 8) | (_intBuffer[3] & 0xff));
		}
	}

	private byte[] _longBuffer = new byte[8];

	public final long readLong() throws IOException {
		readFully(_longBuffer, 0, 8);
		if (_littleEndian) {
			return (((long) _longBuffer[7] << 56)
					+ ((long) (_longBuffer[6] & 255) << 48)
					+ ((long) (_longBuffer[5] & 255) << 40)
					+ ((long) (_longBuffer[4] & 255) << 32)
					+ ((long) (_longBuffer[3] & 255) << 24)
					+ ((_longBuffer[2] & 255) << 16)
					+ ((_longBuffer[1] & 255) << 8) + ((_longBuffer[0] & 255) << 0));
		} else {
			return (((long) _longBuffer[0] << 56)
					+ ((long) (_longBuffer[1] & 255) << 48)
					+ ((long) (_longBuffer[2] & 255) << 40)
					+ ((long) (_longBuffer[3] & 255) << 32)
					+ ((long) (_longBuffer[4] & 255) << 24)
					+ ((_longBuffer[5] & 255) << 16)
					+ ((_longBuffer[6] & 255) << 8) + ((_longBuffer[7] & 255) << 0));
		}
	}

	public final float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	public final double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	public void readShortArray(short[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readShort();
		}
	}

	public void readShortArray(short[] array) throws IOException {
		readShortArray(array, 0, array.length);
	}

	public void readIntArray(int[] array, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readInt();
		}
	}

	public void readIntArray(int[] array) throws IOException {
		readIntArray(array, 0, array.length);
	}

	public void readFloatArray(float[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readFloat();
		}
	}

	public void readFloatArray(float[] array) throws IOException {
		readFloatArray(array, 0, array.length);
	}

	public void readLongArray(long[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readLong();
		}
	}

	public void readLongArray(long[] array) throws IOException {
		readLongArray(array, 0, array.length);
	}

	public void readDoubleArray(double[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			array[i] = readDouble();
		}
	}

	public void readDoubleArray(double[] array) throws IOException {
		readDoubleArray(array, 0, array.length);
	}

	private byte[] _skipBuffer = new byte[1024];

	public void skipFully(long n) throws IOException {
		long toSkip = n;
		while (toSkip > 0) {
			int c1 = toSkip > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) n;
			while (c1 > 0) {
				int c2 = c1 > _skipBuffer.length ? read(_skipBuffer) : read(
						_skipBuffer, 0, c1);
				if (c2 < 0) {
					throw new EOFException();
				}
				c1 -= c2;
			}
			toSkip -= c1;
		}
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

	public long getBytesRead() {
		return _read;
	}

}
