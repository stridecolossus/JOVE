package org.sarge.jove.common;

import java.nio.ByteBuffer;

/**
 * A <i>byte source</i> is a wrapper for a byte array.
 * @author Sarge
 */
public interface ByteSource {
	/**
	 * Writes this source to the given sink.
	 * @param sink Sink
	 */
	void write(Sink sink);

	/**
	 * @return This source as an array
	 */
	byte[] toByteArray();

	/**
	 * A <i>byte sink</i> is a writable target for a byte source.
	 */
	interface Sink {
		/**
		 * Writes the given array to this sink.
		 * @param array Byte array
		 */
		void write(byte[] array);

		/**
		 * Writes the given buffer to this sink.
		 * @param buffer Byte buffer
		 */
		void write(ByteBuffer buffer);
	}
	// TODO - write(OutputStream) => no need for toByteArray()

	/**
	 * Creates a byte source for the given array.
	 * @param array Byte array
	 * @return Array source
	 */
	static ByteSource of(byte[] array) {
		return new ByteSource() {
			@Override
			public void write(Sink sink) {
				sink.write(array);
			}

			@Override
			public byte[] toByteArray() {
				return array;
			}
		};
	}

	/**
	 * Creates a byte source from the given buffer.
	 * @param buffer Byte buffer
	 * @return Byte buffer source
	 */
	static ByteSource of(ByteBuffer buffer) {
		return new ByteSource() {
			@Override
			public void write(Sink sink) {
				buffer.rewind();
				sink.write(buffer);
			}

			@Override
			public byte[] toByteArray() {
				if(buffer.isDirect()) {
					final int len = buffer.rewind().remaining();
					final byte[] array = new byte[len];
					buffer.get(array);
					return array;
				}
				else {
					return buffer.array();
				}
			}
		};
	}
}
