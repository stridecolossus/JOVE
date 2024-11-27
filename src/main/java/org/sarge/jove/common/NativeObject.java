package org.sarge.jove.common;

import java.lang.foreign.*;
import java.util.Collection;

import org.sarge.jove.foreign.AbstractNativeTransformer;

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
	 * Native transformer for the handle of a native object.
	 */
	public static class NativeObjectTransformer extends AbstractNativeTransformer<NativeObject, MemorySegment> {
		@Override
		public Class<NativeObject> type() {
			return NativeObject.class;
		}

		@Override
		public MemorySegment transform(NativeObject obj, SegmentAllocator allocator) {
			return obj.handle().address();
		}
	}
}
