package org.sarge.jove.foreign;

import java.lang.foreign.MemoryLayout;

/**
 * A <i>transformer</i> marshals a Java primitive or reference type to/from the corresponding native representation.
 * @author Sarge
 */
sealed interface Transformer permits Primitive, ReferenceTransformer {
	// TODO - is there much point in this now, only have TWO actual types, and primitive is trivial / pointless?

	/**
	 * @return Native memory layout
	 */
	MemoryLayout layout();
}
