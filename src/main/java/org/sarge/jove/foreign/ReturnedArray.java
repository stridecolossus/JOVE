package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.lang.reflect.Array;

/**
 * A <i>returned array</i> is a helper for an array returned by a native method.
 * <p>
 * A native method that returns an array is an edge-case for Java since the length of the resultant array cannot be determined from the return value.
 * Generally the length is provided separately, often returned as an integer-by-reference parameter in the same method.
 * The {@link #get(int, Class)} method extracts the actual array given the length.
 * <p>
 * @param <T> Array component type
 * @author Sarge
 */
public class ReturnedArray<T> {
	private final MemorySegment address;
	private final Registry registry;

	/**
	 * Constructor.
	 * @param address		Off-heap array
	 * @param registry		Transformer registry
	 */
	protected ReturnedArray(MemorySegment address, Registry registry) {
		this.address = requireNonNull(address);
		this.registry = requireNonNull(registry);
	}

	/**
	 * Extracts the Java array.
	 * <p>
	 * @implNote An invalid array {@link #length} will result in a <b>JVM crash</b> since accessing the off-heap elements is an unsafe operation.
	 * <p>
	 * @param length		Array length
	 * @param type			Component type
	 * @return Array
	 * @throws IllegalArgumentException if the component type is not supported
	 */
	public T[] get(int length, Class<? extends T> type) {
		// Allocate array
		@SuppressWarnings("unchecked")
		final T[] array = (T[]) Array.newInstance(type, length);

		// Unmarshal array elements
		if(length > 0) {
			final var transformer = (ArrayTransformer) registry.get(type.arrayType());
			final MemorySegment data = address.reinterpret(length * transformer.layout().byteSize());
			transformer.update(data, array);
		}

		return array;
	}

	/**
	 * Transformer for a returned array.
	 */
	static final class ReturnedArrayTransformer implements AddressTransformer<ReturnedArray<?>, MemorySegment> {
		private final Registry registry;

		/**
		 * Constructor.
		 * @param registry Transformer registry
		 */
		public ReturnedArrayTransformer(Registry registry) {
			this.registry = requireNonNull(registry);
		}

		@Override
		public Object marshal(ReturnedArray<?> arg, SegmentAllocator allocator) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ReturnedArray<?> unmarshal(MemorySegment address) {
			return new ReturnedArray<>(address, registry);
		}
	}
}
