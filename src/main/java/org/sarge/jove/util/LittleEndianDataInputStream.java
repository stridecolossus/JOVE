package org.sarge.jove.util;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Data input stream with little-endian byte order.
 * <p>
 * Note that this class re-implements input stream rather than extending {@link DataInputStream} since <b>all</b> methods are final and {@link DataInput} is only a marker interface.
 * <p>
 * @author Sarge
 */
public class LittleEndianDataInputStream extends InputStream implements DataInput {
	private static final int MASK = 0xff;

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
		return
				(buffer[1] & MASK) <<  8 |
				(buffer[0] & MASK);
	}

	@Override
	public int readInt() throws IOException {
		in.readFully(buffer, 0, Integer.BYTES);
		return
				(buffer[3]) 	   << 24 |
				(buffer[2] & MASK) << 16 |
				(buffer[1] & MASK) <<  8 |
				(buffer[0] & MASK);
	}

	@Override
	public long readLong() throws IOException {
		in.readFully(buffer, 0, Long.BYTES);
		return
				(long) (buffer[7]) 	   	  << 56 |
				(long) (buffer[6] & MASK) << 48 |
				(long) (buffer[5] & MASK) << 40 |
				(long) (buffer[4] & MASK) << 32 |
				(long) (buffer[3] & MASK) << 24 |
				(long) (buffer[2] & MASK) << 16 |
				(long) (buffer[1] & MASK) <<  8 |
				buffer[0] & MASK;
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