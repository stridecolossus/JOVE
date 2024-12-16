package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.Map;
import java.util.function.Function;

/**
 * The <i>identity native transformer</i> does <b>not</b> apply any transformation to native method arguments.
 * This class is intended for primitives or raw pointers (i.e. a {@link MemorySegment}).
 * @author Sarge
 */
public record PrimitiveNativeTransformer<T>(ValueLayout layout) implements NativeTransformer<T, T> {
	@SuppressWarnings("rawtypes")
	private static final Map<Class, ValueLayout> PRIMITIVES = Map.of(
        	byte.class,		ValueLayout.JAVA_BYTE,
        	char.class,		ValueLayout.JAVA_CHAR,
        	boolean.class,	ValueLayout.JAVA_BOOLEAN,
        	int.class,		ValueLayout.JAVA_INT,
        	short.class,	ValueLayout.JAVA_SHORT,
        	long.class,		ValueLayout.JAVA_LONG,
        	float.class,	ValueLayout.JAVA_FLOAT,
        	double.class,	ValueLayout.JAVA_DOUBLE
    );

	/**
	 * Creates a transformer for the given primitive type.
	 * @param <T> Primitive type
	 * @param type Primitive type
	 * @return Primitive transformer
	 * @throws IllegalArgumentException if the given type is not a supported primitive
	 */
	public static <T> PrimitiveNativeTransformer<T> of(Class<T> type) {
		final ValueLayout layout = PRIMITIVES.get(type);
		if(layout == null) throw new IllegalArgumentException("Not a supported primitive: " + type);
		return new PrimitiveNativeTransformer<>(layout);
	}

	/**
	 * Constructor.
	 * @param layout Primitive memory layout
	 */
	public PrimitiveNativeTransformer {
		requireNonNull(layout);
	}

	// TODO
	/**
	 * Register transformers for the built-in primitive types.
	 * @param registry TODO
	 */
	@SuppressWarnings("unchecked")
	public static void register(TransformerRegistry registry) {
		for(var e : PRIMITIVES.entrySet()) {
			final Class<?> type = e.getKey();
			final var transformer = PrimitiveNativeTransformer.of(e.getKey());
			registry.register(type, transformer);
		}
	}


	@Override
	public T transform(T value, ParameterMode parameter, SegmentAllocator allocator) {
		if(value == null) throw new IllegalArgumentException("Primitive values cannot be NULL");
		return value;
	}

	@Override
	public Function<T, T> returns() {
		return Function.identity();
	}
}
