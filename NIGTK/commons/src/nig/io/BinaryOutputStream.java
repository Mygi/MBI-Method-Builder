package nig.io;

import java.io.BufferedOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class BinaryOutputStream extends FilterOutputStream implements
		BinaryDataOutput {

	protected long _bytesWritten = 0;
	private boolean _littleEndian = false;

	public BinaryOutputStream(OutputStream out) {
		super(out instanceof BufferedOutputStream ? out
				: new BufferedOutputStream(out));
	}

	public synchronized void write(int b) throws IOException {
		out.write(b);
		_bytesWritten++;
	}

	public synchronized void write(byte b[], int off, int len)
			throws IOException {
		out.write(b, off, len);
		_bytesWritten += len;
	}

	public void flush() throws IOException {
		if(out instanceof BufferedOutputStream){
			((BufferedOutputStream)out).flush();
		} else {
			out.flush();
		}
	}

	public final void writeBoolean(boolean v) throws IOException {
		write(v ? 1 : 0);
	}

	public final void writeByte(int v) throws IOException {
		write(v);
	}

	public final void writeShort(int v) throws IOException {
		if (_littleEndian) {
			write((v >>> 0) & 0xFF);
			write((v >>> 8) & 0xFF);
		} else {
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
	}

	public final void writeChar(int v) throws IOException {
		if (_littleEndian) {
			write((v >>> 0) & 0xFF);
			write((v >>> 8) & 0xFF);
		} else {
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
	}

	private byte _intBuffer[] = new byte[4];

	public final void writeInt(int v) throws IOException {
		if (_littleEndian) {
			_intBuffer[0] = (byte) ((v >>> 0) & 0xFF);
			_intBuffer[1] = (byte) ((v >>> 8) & 0xFF);
			_intBuffer[2] = (byte) ((v >>> 16) & 0xFF);
			_intBuffer[3] = (byte) ((v >>> 24) & 0xFF);
		} else {
			_intBuffer[0] = (byte) ((v >>> 24) & 0xFF);
			_intBuffer[1] = (byte) ((v >>> 16) & 0xFF);
			_intBuffer[2] = (byte) ((v >>> 8) & 0xFF);
			_intBuffer[3] = (byte) ((v >>> 0) & 0xFF);
		}
		write(_intBuffer, 0, 4);
	}

	private byte _longBuffer[] = new byte[8];

	public final void writeLong(long v) throws IOException {
		if (_littleEndian) {
			_longBuffer[0] = (byte) (v >>> 0);
			_longBuffer[1] = (byte) (v >>> 8);
			_longBuffer[2] = (byte) (v >>> 16);
			_longBuffer[3] = (byte) (v >>> 24);
			_longBuffer[4] = (byte) (v >>> 32);
			_longBuffer[5] = (byte) (v >>> 40);
			_longBuffer[6] = (byte) (v >>> 48);
			_longBuffer[7] = (byte) (v >>> 56);
		} else {
			_longBuffer[0] = (byte) (v >>> 56);
			_longBuffer[1] = (byte) (v >>> 48);
			_longBuffer[2] = (byte) (v >>> 40);
			_longBuffer[3] = (byte) (v >>> 32);
			_longBuffer[4] = (byte) (v >>> 24);
			_longBuffer[5] = (byte) (v >>> 16);
			_longBuffer[6] = (byte) (v >>> 8);
			_longBuffer[7] = (byte) (v >>> 0);
		}
		write(_longBuffer, 0, 8);
	}

	public final void writeFloat(float v) throws IOException {
		writeInt(Float.floatToIntBits(v));
	}

	public final void writeDouble(double v) throws IOException {
		writeLong(Double.doubleToLongBits(v));
	}

	public final void writeBytes(String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			write((byte) s.charAt(i));
		}
	}

	public final void writeChars(String s) throws IOException {
		int len = s.length();
		for (int i = 0; i < len; i++) {
			int v = s.charAt(i);
			write((v >>> 8) & 0xFF);
			write((v >>> 0) & 0xFF);
		}
	}

	public final long getBytesWritten() {
		return _bytesWritten;
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

	public void writeDoubleArray(double[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			writeDouble(array[i]);
		}
	}

	public void writeDoubleArray(double[] array) throws IOException {
		writeDoubleArray(array, 0, array.length);
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

	public void writeIntArray(int[] array, int off, int len) throws IOException {
		for (int i = off; i < off + len; i++) {
			writeInt(array[i]);
		}
	}

	public void writeIntArray(int[] array) throws IOException {
		writeIntArray(array, 0, array.length);
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

	public void writeShortArray(short[] array, int off, int len)
			throws IOException {
		for (int i = off; i < off + len; i++) {
			writeShort(array[i]);
		}
	}

	public void writeShortArray(short[] array) throws IOException {
		writeShortArray(array, 0, array.length);
	}
}
