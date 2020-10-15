package org.sarge.jove.common;

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
}
