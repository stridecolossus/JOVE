package org.sarge.jove.geometry;

import org.sarge.jove.common.Bufferable;

/**
 * Convenience base-class for geometry tuples.
 * @author Sarge
 */
interface Tuple extends Bufferable {
	/**
	 * Size of a geometry tuple.
	 */
	int SIZE = 3;

	@Override
	default int length() {
		return SIZE * Float.BYTES;
	}
}
