package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.util.*;

/**
 * A <i>primitive</i> transformer maps Java primitives to their corresponding native types.
 * @author Sarge
 */
record Primitive(MemoryLayout layout) implements Transformer {
	/**
	 * @return Primitive transformers indexed by type
	 */
	public static Map<Class<?>, Primitive> primitives() {
		final var primitives = List.of(
	    		JAVA_BOOLEAN,
	    		JAVA_BYTE,
	    		JAVA_CHAR,
	    		JAVA_SHORT,
	    		JAVA_INT,
	    		JAVA_LONG,
	    		JAVA_FLOAT,
	    		JAVA_DOUBLE
		);

		return primitives
				.stream()
				.collect(toMap(ValueLayout::carrier, Primitive::new));
	}
}
