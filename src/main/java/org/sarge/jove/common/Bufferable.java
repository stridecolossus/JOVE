package org.sarge.jove.common;

import java.nio.ByteBuffer;
import java.util.Arrays;

/**
 * A <i>bufferable</i> is a data object that can be written to an NIO buffer.
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
	int length();

	/**
	 * Creates a compound bufferable object.
	 * @param objects Bufferable objects
	 * @return Compound bufferable
	 */
	static Bufferable of(Bufferable... objects) {
		final int len = Arrays.stream(objects).mapToInt(Bufferable::length).sum();

		return new Bufferable() {
			@Override
			public int length() {
				return len;
			}

			@Override
			public void buffer(ByteBuffer buffer) {
				for(Bufferable obj : objects) {
					obj.buffer(buffer);
				}
			}
		};
	}
}
