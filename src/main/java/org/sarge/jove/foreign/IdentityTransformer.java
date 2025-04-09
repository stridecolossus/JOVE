package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toMap;

import java.lang.foreign.*;
import java.util.*;

/**
 * The <i>identity transformer</i> marshals an argument as-is to support the built-in Java primitives.
 * @author Sarge
 */
public record IdentityTransformer(MemoryLayout layout) implements Transformer {
	/**
	 * Constructor.
	 * @param layout Memory layout
	 */
	public IdentityTransformer {
		requireNonNull(layout);
	}

	/**
	 * Identity transformer for a {@link MemorySegment}.
	 */
	public static final IdentityTransformer SEGMENT = new IdentityTransformer(ValueLayout.ADDRESS);

	/**
	 * @return Primitive transformers indexed by type
	 */
	public static Map<Class<?>, IdentityTransformer> primitives() {
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
				.collect(toMap(ValueLayout::carrier, IdentityTransformer::new));
	}
}
