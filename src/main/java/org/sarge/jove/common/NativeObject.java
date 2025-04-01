package org.sarge.jove.common;

import java.lang.foreign.SegmentAllocator;
import java.util.Collection;
import java.util.function.Function;

import org.sarge.jove.foreign.ReferenceTransformer;

/**
 * A <i>native object</i> is a resource created by the native layer referenced by a {@link Handle}.
 * @author Sarge
 */
public interface NativeObject {
	/**
	 * @return Handle
	 */
	Handle handle();

	/**
	 * Helper - Transforms the given collection of native objects to an array of handles.
	 * @param objects Native objects
	 * @return Handles
	 */
	static Handle[] handles(Collection<? extends NativeObject> objects) {
		return objects
				.stream()
				.map(NativeObject::handle)
				.toArray(Handle[]::new);
	}
	// TODO - used? needed? here?

	/**
	 * Transformer for native objects.
	 */
	public static class NativeObjectTransformer implements ReferenceTransformer<NativeObject> {
		@Override
		public Object marshal(NativeObject arg, SegmentAllocator allocator) {
			return arg.handle().address();
		}

		@Override
		public Function<? extends Object, NativeObject> unmarshal() {
			throw new UnsupportedOperationException();
		}
	}
}
