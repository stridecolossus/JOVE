package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.*;
import java.util.function.Function;

/**
 *
 * @param <T>
 * @param <R>
 * @author Sarge
 */
public class PrimitiveNativeMapper<T> extends AbstractNativeMapper<T, T> {
	private static final Map<Class<?>, ValueLayout> PRIMITIVES = Map.of(
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
	 * Maps the given type to its corresponding native FFM layout.
	 * @param type Java or JOVE type
	 * @return FFM layout
	 */
	public static MemoryLayout map(Class<?> type) {
		return PRIMITIVES.getOrDefault(type, AddressLayout.ADDRESS);
	}

	/**
	 * @return Native mappers for all Java primitive types
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	public static Collection<? extends NativeMapper> mappers() {
		return PRIMITIVES
				.keySet()
				.stream()
				.map(PrimitiveNativeMapper::new)
				.toList();
	}

	private final Class<T> type;
	private final ValueLayout layout;

	/**
	 * Constructor.
	 * @param type Primitive type
	 * @throws IllegalArgumentException if {@link #type} is not primitive
	 */
	public PrimitiveNativeMapper(Class<T> type) {
		if(!type.isPrimitive()) throw new IllegalArgumentException("Not a primitive: " + type);
		this.type = requireNonNull(type);
		this.layout = PRIMITIVES.get(type);
	}

	@Override
	public Class<T> type() {
		return type;
	}

	@Override
	public MemoryLayout layout(Class<? extends T> type) {
		return layout;
	}

	@Override
	public Object marshal(T value, NativeContext context) {
		return value;
	}

	@Override
	public Object marshalNull(Class<? extends T> type) {
		// TODO - or return zero? or separate wrapper implementation?
		throw new UnsupportedOperationException();
	}

	@Override
	public Function<T, T> returns(Class<? extends T> target) {
		return Function.identity();
	}

	@Override
	public String toString() {
		return String.format("PrimitiveNativeMapper[%s]", type);
	}
}
