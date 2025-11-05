package org.sarge.jove.foreign;

import static java.lang.foreign.ValueLayout.*;

import java.lang.foreign.*;
import java.lang.reflect.Array;
import java.util.function.*;

/**
 * A <i>primitive transformer</i> marshals primitive arguments as-is, i.e. without any marshalling.
 * @param <T> Primitive type
 * @see NativeBooleanTransformer
 * @author Sarge
 */
public record PrimitiveTransformer<T>(ValueLayout layout) implements Transformer<T, T> {
	/**
	 * Constructor.
	 * @param layout Primitive layout
	 * @throws IllegalArgumentException if the layout is not primitive
	 */
	public PrimitiveTransformer {
		if(!layout.carrier().isPrimitive()) {
			throw new IllegalArgumentException();
		}
	}

	@Override
	public T marshal(T arg, SegmentAllocator allocator) {
		return arg;
	}

	@Override
	public Object empty() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<T, T> unmarshal() {
		return Function.identity();
	}

	@Override
	public Transformer<?, ?> array() {
		return array(this, layout);
	}

	/**
	 * Helper.
	 * Creates an primitive array transformer.
	 * @param delegate		Delegate transformer
	 * @param layout		Primitive layout
	 * @return Array transformer
	 */
	private static Transformer<?, ?> array(Transformer<?, ?> delegate, ValueLayout layout) {
		return new AbstractArrayTransformer(delegate) {
			@Override
			protected void marshal(Object array, int length, MemorySegment address, SegmentAllocator allocator) {
				MemorySegment.copy(array, 0, address, layout, 0L, length);
			}

			@Override
			public BiConsumer<MemorySegment, Object> update() {
				return (address, array) -> {
					final int length = Array.getLength(array);
					MemorySegment.copy(address, layout, 0L, array, 0, length);
				};
			}
		};
	}

	/**
	 * Specialised implementation that maps a Java {@code boolean} to a native integer.
	 * TODO - more explanation
	 */
	public record NativeBooleanTransformer() implements Transformer<Boolean, Integer> {
		@Override
		public MemoryLayout layout() {
			return JAVA_INT;
		}

		@Override
		public Integer marshal(Boolean arg, SegmentAllocator allocator) {
			return arg ? 1 : 0;
		}

		@Override
		public Object empty() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Function<Integer, Boolean> unmarshal() {
			return value -> value == 1 ? Boolean.TRUE : Boolean.FALSE;
		}

		@Override
		public Transformer<?, ?> array() {
			return PrimitiveTransformer.array(this, JAVA_INT);
		}
	}

	/**
	 * Registers transformers for the built-in primitive types.
	 * @param registry Transformer registry
	 * @see NativeBooleanTransformer
	 */
	@SuppressWarnings({"rawtypes", "unchecked"})
	public static void register(Registry registry) {
		final ValueLayout[] primitives = {
	    		JAVA_BYTE,
	    		JAVA_CHAR,
	    		JAVA_SHORT,
	    		JAVA_INT,
	    		JAVA_LONG,
	    		JAVA_FLOAT,
	    		JAVA_DOUBLE
		};

		for(ValueLayout layout : primitives) {
    		final var transformer = new PrimitiveTransformer<>(layout);
			final Class carrier = layout.carrier();
    		registry.register(carrier, transformer);
    	}

		registry.register(boolean.class, new NativeBooleanTransformer());
    }
	// TODO - also wrappers? otherwise add doc
}
