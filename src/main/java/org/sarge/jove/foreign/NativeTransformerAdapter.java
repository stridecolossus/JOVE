package org.sarge.jove.foreign;

import static java.util.Objects.requireNonNull;

import java.lang.foreign.*;
import java.util.function.Function;

/**
 * A <i>native transformer adapter</i> handles {@code null} arguments.
 * @param <T> Domain type
 * @author Sarge
 */
public class NativeTransformerAdapter<T extends Object> implements NativeTransformer<T> {
	private final NativeTransformer<T> delegate;

	/**
	 * Constructor.
	 * @param delegate Delegate transformer
	 */
	public NativeTransformerAdapter(NativeTransformer<T> delegate) {
		this.delegate = requireNonNull(delegate);
	}

	@Override
	public MemoryLayout layout() {
		return delegate.layout();
	}

	@Override
	public Object marshal(T arg, SegmentAllocator allocator) {
		if(arg == null) {
			return MemorySegment.NULL;
		}
		else {
			return delegate.marshal(arg, allocator);
		}
	}

	@Override
	public Function<? extends Object, T> unmarshal() {
		@SuppressWarnings("unchecked")
		final Function<Object, T> unmarshal = (Function<Object, T>) delegate.unmarshal();

		return value -> {
			if(MemorySegment.NULL.equals(value)) {
				return null;
			}
			else {
				return unmarshal.apply(value);
			}
		};
	}
}
