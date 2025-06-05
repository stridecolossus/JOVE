package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;
import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * The <i>identity transformer</i> marshals an argument as-is, i.e. without any transformation.
 * @author Sarge
 */
public record IdentityTransformer<T>(ValueLayout layout) implements Transformer<T> {
	/**
	 * Constructor.
	 * @param layout Memory layout
	 */
	public IdentityTransformer {
		requireNonNull(layout);
	}

	@Override
	public Object marshal(Object arg, SegmentAllocator allocator) {
		return arg;
	}

	@Override
	public Object empty() {
		assert !layout.carrier().isPrimitive();
		return Transformer.super.empty();
	}

	@Override
	public Function<?, T> unmarshal() {
		return Function.identity();
	}

	/**
	 * Registers transformers for the built-in primitive types.
	 * @param registry Transformer registry
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void primitives(Registry registry) {
		final ValueLayout[] primitives = {
	    		JAVA_BOOLEAN,
	    		JAVA_BYTE,
	    		JAVA_CHAR,
	    		JAVA_SHORT,
	    		JAVA_INT,
	    		JAVA_LONG,
	    		JAVA_FLOAT,
	    		JAVA_DOUBLE
		};

		for(ValueLayout layout : primitives) {
    		final var transformer = new IdentityTransformer<>(layout);
    		final Class carrier = layout.carrier();
    		registry.add(carrier, transformer);
    	}
    }
}
