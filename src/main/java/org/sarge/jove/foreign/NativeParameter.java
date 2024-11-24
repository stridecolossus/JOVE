package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;

/**
 * A <i>native type</i> represents a parameter or return type of a native method.
 * The <i>returns</i> constructor argument is used to specify a method return type or a pass-by-reference parameter.
 * @see Returned
 */
class NativeParameter {
	private final Class<?> type;
	private final boolean returns;
	private final NativeMapper mapper;

	/**
	 * Constructor.
	 * @param type			Target type
	 * @param mapper		Native mapper
	 * @param returns		Whether this value can be returned as a by-reference parameter
	 * @throws IllegalArgumentException TODO
	 */
	@SuppressWarnings("rawtypes")
	public NativeParameter(Class<?> type, NativeMapper mapper, boolean returns) {
		this.type = requireNonNull(type);
		this.mapper = requireNonNull(mapper);
		this.returns = returns;

	}

	protected Class<?> type() {
		return type;
	}

	/**
	 * @return Whether this type can be returned as a by-reference parameter
	 */
	public boolean isReturnedParameter() {
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
	public Object returns(Object value) {
		if(MemorySegment.NULL.equals(value)) {
			return null;
		}
		else {
			return mapper.returns(type).apply(value);
		}
	}

	/**
	 * Unmarshals a returned by-reference native value.
	 * @param value		Native value
	 * @param arg		Method argument
	 */
	@SuppressWarnings("unchecked")
	public void unmarshal(MemorySegment address, Object arg) {
		assert returns;

		if(MemorySegment.NULL.equals(address)) {
			return;
		}
		else {
			mapper.unmarshal(type).accept(address, arg);
		}
	}
}
