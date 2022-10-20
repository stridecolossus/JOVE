package org.sarge.jove.io;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Data input stream with little-endian byte order.
 * <p>
 * Note that this class re-implements rather than extends {@link DataInputStream} since <b>all</b> methods are final and {@link DataInput} is only a marker interface.
 * <p>
 * @author Sarge
 */
public class LittleEndianDataInputStream extends InputStream implements DataInput {
	private static final int MASK = 0xff;
	private static final long LONG_MASK = MASK;

	/**
	 * Helper - Converts a little endian integer represented by the given byte array to a big endian integer.
	 * @param bytes		Byte array
	 * @param len		Number of bytes
	 * @return Big endian integer
	 */
	public static int convert(byte[] bytes, int offset, int len) {
		int value = bytes[offset] & MASK;
		for(int n = 1; n < len; ++n) {
			value = value | (bytes[offset + n] & MASK) << (n * 8);
		}
		return value;
	}

	private final DataInputStream in;
	private final byte[] buffer = new byte[8];

	/**
	 * Constructor.
	 * @param in Input stream
	 */
	public LittleEndianDataInputStream(InputStream in) {
		this.in = new DataInputStream(in);
	}

	@Override
	public int available() throws IOException {
		return in.available();
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte[] bytes, int off, int len) throws IOException {
		return in.read(bytes, off, len);
	}

	@Override
	public int skipBytes(int n) throws IOException {
		return in.skipBytes(n);
	}

	@Override
	public byte readByte() throws IOException {
		return in.readByte();
	}

	@Override
	public int readUnsignedByte() throws IOException {
		return in.readUnsignedByte();
	}

	@Override
	public char readChar() throws IOException {
		return (char) readUnsignedShort();
	}

	@Override
	public boolean readBoolean() throws IOException {
		return in.readBoolean();
	}

	@Override
	public short readShort() throws IOException {
		return (short) readUnsignedShort();
	}

	@Override
	public int readUnsignedShort() throws IOException {
		in.readFully(buffer, 0, Short.BYTES);
		return convert(buffer, 0, Short.BYTES);
	}

	@Override
	public int readInt() throws IOException {
		in.readFully(buffer, 0, Integer.BYTES);
		return convert(buffer, 0, Integer.BYTES);
	}

	@Override
	public long readLong() throws IOException {
		in.readFully(buffer, 0, Long.BYTES);
		return
				(long) (buffer[7]) 	   	<< 56 |
				(buffer[6] & LONG_MASK) << 48 |
				(buffer[5] & LONG_MASK) << 40 |
				(buffer[4] & LONG_MASK) << 32 |
				(buffer[3] & LONG_MASK) << 24 |
				(buffer[2] & LONG_MASK) << 16 |
				(buffer[1] & LONG_MASK) <<  8 |
				buffer[0] & LONG_MASK;
	}

	@Override
	public float readFloat() throws IOException {
		return Float.intBitsToFloat(readInt());
	}

	@Override
	public double readDouble() throws IOException {
		return Double.longBitsToDouble(readLong());
	}

	@Override
	public void readFully(byte[] bytes) throws IOException {
		in.read(bytes);
	}

	@Override
	public void readFully(byte[] bytes, int off, int len) throws IOException {
		in.readFully(bytes, off, len);
	}

	@Deprecated
	@Override
	public String readLine() throws IOException {
		throw new UnsupportedOperationException();
	}

	@Override
	public String readUTF() throws IOException {
		return in.readUTF();
	}

	@Override
	public void close() throws IOException {
		in.close();
	}
}
