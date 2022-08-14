package org.sarge.jove.common;

import java.nio.ByteBuffer;

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
}
