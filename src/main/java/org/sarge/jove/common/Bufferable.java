package org.sarge.jove.common;

import java.nio.ByteBuffer;

import org.sarge.jove.util.BufferHelper;

import com.sun.jna.Structure;

/**
 * A <i>bufferable</i> object can be written to an NIO buffer.
 * @author Sarge
 */
public interface Bufferable {
	/**
	 * @return Length of this object (bytes)
	 */
	int length();

	/**
	 * Writes this object to the given buffer.
	 * @param bb Buffer
	 */
	void buffer(ByteBuffer bb);

	/**
	 * Creates a bufferable wrapping the given array.
	 * @param bytes Byte array
	 * @return Bufferable array
	 */
	static Bufferable of(byte[] bytes) {
		return new Bufferable() {
			@Override
			public int length() {
				return bytes.length;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				BufferHelper.write(bytes, bb);
			}
		};
	}

	/**
	 * Creates a bufferable wrapping the given JNA structure.
	 * @param struct Structure
	 * @return Bufferable structure
	 */
	static Bufferable of(Structure struct) {
		return new Bufferable() {
			@Override
			public int length() {
				return struct.size();
			}

			@Override
			public void buffer(ByteBuffer bb) {
				final byte[] array = struct.getPointer().getByteArray(0, struct.size());
				BufferHelper.write(array, bb);
			}
		};
	}
}
