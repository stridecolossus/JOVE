package org.sarge.jove.foreign;

import java.lang.foreign.MemoryLayout;
import java.lang.invoke.*;

/**
 * A <i>transformer</i> marshals Java types to/from the corresponding native representation.
 * @author Sarge
 */
public sealed interface Transformer permits IdentityTransformer, DefaultTransformer {
    /**
     * @return Memory layout of this type
     */
	MemoryLayout layout();

    /**
     * Inserts a zero byte offset coordinate into the given method handle at index <b>one</b>.
     * @param handle Method handle
     * @return Method handle with no byte offsets
     */
	static VarHandle removeOffset(VarHandle handle) {
		return MethodHandles.insertCoordinates(handle, 1, 0L);
	}
}
