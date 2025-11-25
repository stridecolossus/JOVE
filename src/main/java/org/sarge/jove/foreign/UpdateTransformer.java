package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.*;

/**
 * An <i>update transformer</i> is an adapter for a by-reference parameter.
 * <p>
 * A by-reference parameter has the following constraints:
 * <ul>
 * <li>Can logically <b>only</b> be implemented for native types represented by {@link MemorySegment}</li>
 * <li>The {@link #layout()} is {@link AddressLayout#ADDRESS}</li>
 * <li>Cannot be returned from a native method</li>
 * </ul>
 * <p>
 * @author Sarge
 */
class UpdateTransformer<T> implements Transformer<T, MemorySegment> {
	private final Transformer<T, MemorySegment> delegate;
	private final BiConsumer<MemorySegment, T> update;

	/**
	 * Constructor.
	 * @param delegate Delegate transformer
	 * @throws UnsupportedOperationException if {#link delegate} cannot be used as a by-reference parameter
	 */
	public UpdateTransformer(Transformer<T, MemorySegment> delegate) {
		this.delegate = requireNonNull(delegate);
		this.update = delegate.update();
	}

	@Override
	public MemoryLayout layout() {
		return AddressLayout.ADDRESS;
	}

	@Override
	public MemorySegment marshal(T arg, SegmentAllocator allocator) {
		return delegate.marshal(arg, allocator);
	}

	@Override
	public Object empty() {
		return MemorySegment.NULL;
	}

	@Override
	public Function<MemorySegment, T> unmarshal() {
		throw new RuntimeException();
	}

	@Override
	public BiConsumer<MemorySegment, T> update() {
		return update;
	}
}
