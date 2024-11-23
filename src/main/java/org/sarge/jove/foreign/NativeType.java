package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;

import org.sarge.jove.foreign.NativeMapper.*;

/**
 * A <i>native type</i> represents a parameter or return type of a native method.
 * The <i>returns</i> constructor argument is used to specify a method return type or a pass-by-reference parameter.
 * @see Returned
 */
class NativeType {
	private final Class<?> type;
	private final boolean returns;
	@SuppressWarnings("rawtypes") private final NativeMapper mapper;
//	@SuppressWarnings("rawtypes") private final ReturnMapper returnMapper;

	/**
	 * Constructor.
	 * @param type			Target type
	 * @param mapper		Native mapper
	 * @param returns		Whether this value is returned
	 * @throws IllegalArgumentException if {@link #mapper} does not provide a return mapper and this type is a method return value or a pass-by-reference parameter
	 */
	@SuppressWarnings("rawtypes")
	public NativeType(Class<?> type, NativeMapper<?> mapper, boolean returns) {
		this.type = requireNonNull(type);
		this.mapper = requireNonNull(mapper);
//		if(returns) {
//			this.returnMapper = (ReturnMapper) requireNonNull(mapper);
//		}
//		else {
//			this.returnMapper = null;
//		}
		this.returns = returns;
	}

	Class<?> type() {
		return type;
	}

	/**
	 * @return Whether this type can be returned as a method return value and/or a by-reference parameter
	 */
	public boolean isReturnType() {
		return returns;
	}

	/**
	 * @return Memory layout for this native type
	 */
	@SuppressWarnings("unchecked")
	public MemoryLayout layout() {
		return mapper.layout(type);
	}

	/**
	 * Marshals a method argument to the native layer.
	 * @param arg			Method argument
	 * @param context		Native context
	 * @return Native value
	 */
	public Object marshal(Object arg, NativeContext context) {
		return context.marshal(mapper, arg, type);
	}

	/**
	 * Unmarshals a returned native value.
	 * @param value Native value
	 * @return Return value
	 */
	@SuppressWarnings("unchecked")
	public Object unmarshal(Object value) {
		assert returns; // TODO - ???

		if(MemorySegment.NULL.equals(value)) {
			return null;
		}
		else {
			@SuppressWarnings("rawtypes")
			final var rm = (ReturnMapper) mapper;	// TODO - urgh
			return rm.unmarshal(value, type);
		}
	}

	/**
	 * Unmarshals a returned by-reference native value.
	 * @param value		Native value
	 * @param arg		Method argument
	 */
	@SuppressWarnings("unchecked")
	public void unmarshal(Object value, Object arg) {
//		// Ignore if not by-reference
//		if(!returns) {
//			return;
//		}
		assert returns;

		// Ignore if empty native value
		if(MemorySegment.NULL.equals(value)) {
			return;
		}

		// Otherwise unmarshal by-reference value
		@SuppressWarnings("rawtypes")
		final var ref = (ReturnedParameterMapper) mapper;	// TODO - urgh
		ref.unmarshal((MemorySegment) value, arg); // TODO - cast
	}
}
