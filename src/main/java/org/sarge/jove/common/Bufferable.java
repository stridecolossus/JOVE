package org.sarge.jove.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;

/**
 * A <i>bufferable</i> object can be written to an NIO buffer.
 * @author Sarge
 */
public interface Bufferable {
	/**
	 * Writes this object to the given buffer.
	 * @param buffer Buffer
	 */
	void buffer(ByteBuffer buffer);

	/**
	 * @return Length of this object (bytes)
	 */
	long length();

	/**
	 * Writes a bufferable object to the given output stream.
	 * @param obj Bufferable object
	 * @param out Output stream
	 * @throws IOException if the object cannot be written
	 */
	static void write(Bufferable obj, OutputStream out) throws IOException {
		final ByteBuffer bb = ByteBuffer.allocate((int) obj.length());
		obj.buffer(bb);
		out.write(bb.array());
		out.flush();
	}

	/**
	 * Reads a bufferable object from the given input stream.
	 * <p>
	 * The loaded bufferable attempts to copy the data to a destination buffer without intermediate data allocation or transfers.
	 * i.e. The input stream is not read until {@link #buffer(ByteBuffer)} is invoked and users must ensure that the stream is not closed in the meantime.
	 * <p>
	 * Note that the stream is closed once this object has been buffered.
	 * <p>
	 * @param in Input stream
	 * @return New bufferable
	 * @throws RuntimeException if the bufferable cannot be read
	 */
	static Bufferable read(InputStream in) {
		return new Bufferable() {
			@Override
			public long length() {
				try {
					return in.available();
				}
				catch(IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				try {
					in.read(buffer.array());
					in.close();
				}
				catch(IOException e) {
					throw new RuntimeException(e);
				}
			}
		};
	}
}
