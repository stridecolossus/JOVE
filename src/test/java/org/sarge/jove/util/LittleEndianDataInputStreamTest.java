package org.sarge.jove.util;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LittleEndianDataInputStreamTest {
	private static final byte BYTE = 42;
	private static final long MASK = 0xff;

	private LittleEndianDataInputStream in;
	private byte[] bytes;

	@BeforeEach
	void before() {
		bytes = new byte[8];
		in = new LittleEndianDataInputStream(new ByteArrayInputStream(bytes));
	}

	private void init(long value, int size) {
		for(int n = 0; n < size; ++n) {
			bytes[n] = (byte) (value >> (n * 8));
		}
	}

	@Test
	void available() throws IOException {
		assertEquals(8, in.available());
	}

	@Test
	void skipBytes() throws IOException {
		bytes[1] = BYTE;
		in.skipBytes(1);
		assertEquals(BYTE, in.read());
		assertEquals(6, in.available());
	}

	@Test
	void read() throws IOException {
		bytes[0] = BYTE;
		assertEquals(BYTE, in.read());
		assertEquals(7, in.available());
	}

	@Test
	void readByte() throws IOException {
		bytes[0] = BYTE;
		assertEquals(BYTE, in.readByte());
		assertEquals(7, in.available());
	}

	@Test
	void readUnsignedByte() throws IOException {
		bytes[0] = BYTE;
		assertEquals(BYTE, in.readByte());
		assertEquals(7, in.available());
	}

	@Test
	void readChar() throws IOException {
		final char ch = 'A';
		init(ch, Character.BYTES);
		assertEquals(ch, in.readChar());
		assertEquals(6, in.available());
	}

	@Test
	void readBoolean() throws IOException {
		bytes[0] = 1;
		assertEquals(true, in.readBoolean());
		assertEquals(false, in.readBoolean());
		assertEquals(6, in.available());
	}

	@Test
	void readShort() throws IOException {
		init(Short.MAX_VALUE, Short.BYTES);
		assertEquals(Short.MAX_VALUE, in.readShort());
		assertEquals(6, in.available());
	}

	@Test
	void readUnsignedShort() throws IOException {
		init(Short.MAX_VALUE, Short.BYTES);
		assertEquals(Short.MAX_VALUE, in.readUnsignedShort());
		assertEquals(6, in.available());
	}

	@Test
	void readInt() throws IOException {
		init(Integer.MAX_VALUE, Integer.BYTES);
		assertEquals(Integer.MAX_VALUE, in.readInt());
		assertEquals(4, in.available());
	}

	@Test
	void readLong() throws IOException {
		init(Long.MAX_VALUE, Long.BYTES);
		assertEquals(Long.MAX_VALUE, in.readLong());
		assertEquals(0, in.available());
	}

	@Test
	void readFloat() throws IOException {
		final int bits = Float.floatToIntBits(Float.MAX_VALUE);
		init(bits, Float.BYTES);
		assertEquals(Float.MAX_VALUE, in.readFloat());
		assertEquals(4, in.available());
	}

	@Test
	void readDouble() throws IOException {
		final long bits = Double.doubleToLongBits(Double.MAX_VALUE);
		init(bits, Double.BYTES);
		assertEquals(Double.MAX_VALUE, in.readDouble());
		assertEquals(0, in.available());
	}

	@Test
	void readFully() throws IOException {
		final byte[] result = new byte[8];
		in.readFully(result);
		assertArrayEquals(bytes, result);
		assertEquals(0, in.available());
	}

	@Test
	void readFullyRange() throws IOException {
		final byte[] result = new byte[8];
		in.readFully(result, 0, 8);
		assertArrayEquals(bytes, result);
		assertEquals(0, in.available());
	}

	@Test
	void readLine() throws IOException {
		assertThrows(UnsupportedOperationException.class, () -> in.readLine());
	}

	@Test
	void readUTF() throws IOException {
		// Write UTF
		final String str = "string";
		final ByteArrayOutputStream temp = new ByteArrayOutputStream();
		final DataOutputStream out = new DataOutputStream(temp);
		out.writeUTF(str);

		// Init underlying stream
		final byte[] array = temp.toByteArray();
		System.arraycopy(array, 0, bytes, 0, array.length);

		// Check string
		assertEquals(str, in.readUTF());
	}
}

