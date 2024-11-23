package org.sarge.jove.common;

import java.lang.foreign.MemorySegment;
import java.util.Collection;

import org.sarge.jove.foreign.*;

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

	/**
	 *
	 */
	public static class NativeObjectMapper extends AbstractNativeMapper<NativeObject> {
		public NativeObjectMapper() {
			super(NativeObject.class);
		}

		@Override
		public MemorySegment marshal(NativeObject obj, NativeContext __) {
			return obj.handle().address();
		}
	}
}
