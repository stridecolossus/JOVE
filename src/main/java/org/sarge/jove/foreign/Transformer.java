package org.sarge.jove.foreign;

import java.lang.foreign.MemoryLayout;

/**
 * A <i>transformer</i> marshals a Java primitive or reference type to/from the corresponding native representation.
 * @author Sarge
 */
public sealed interface Transformer permits IdentityTransformer, AddressTransformer, ArrayTransformer {
	/**
	 * @return Native memory layout
	 */
	MemoryLayout layout();
}
