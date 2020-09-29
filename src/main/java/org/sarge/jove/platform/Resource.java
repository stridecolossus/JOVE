package org.sarge.jove.platform;

import static org.sarge.jove.util.Check.notNull;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;

import com.sun.jna.FromNativeContext;
import com.sun.jna.NativeMapped;
import com.sun.jna.Pointer;

/**
 * A <i>resource</i> is a platform-specific object allocated by a service.
 * @author Sarge
 */
@FunctionalInterface
public interface Resource {
	/**
	 * Destroys this resource.
	 * @throws IllegalStateException if this resource has already been destroyed
	 */
	void destroy();

	/**
	 * A <i>handle</i> is an accessor for a native resource.
	 * @param <T> Handle type
	 */
	class Handle<T> extends AbstractEqualsObject implements Resource {
		private T handle;

		/**
		 * Constructor.
		 * @param handle Resource handle
		 */
		public Handle(T handle) {
			this.handle = notNull(handle);
		}

		/**
		 * @return Whether this resource has been destroyed
		 */
		public boolean isDestroyed() {
			return handle == null;
		}

		/**
		 * @return Handle
		 * @throws IllegalStateException if this handle has been destroyed
		 */
		public T handle() {
			if(handle == null) throw new IllegalStateException("Handle has been destroyed: " + this);
			return handle;
		}

		@Override
		public synchronized void destroy() {
			if(handle == null) throw new IllegalStateException("Handle has already been destroyed: " + this);
			handle = null;
		}
	}

	/**
	 * A <i>pointer handle</i> is a resource implemented using a JNA pointer as a native handle.
	 */
	class PointerHandle extends Handle<Pointer> implements NativeMapped {
		/**
		 * Constructor.
		 * @param handle Pointer handle
		 */
		public PointerHandle(Pointer handle) {
			super(handle);
		}

		@Override
		public Class<?> nativeType() {
			return Pointer.class;
		}

		@Override
		public Object toNative() {
			return super.handle;
		}

		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * A <i>resource tracker</i> is a utility that can be used by a service implementation to maintain resources allocated by that service.
	 * <p>
	 * Usage:
	 * <pre>
	 * // Create tracker
	 * final Resource.Tracker<Resource> tracker = new Resource.Tracker<>();
	 *
	 * // Allocate and register resource
	 * final Resource res = ...
	 * tracker.add(res);
	 *
	 * ...
	 *
	 * // Remove a destroyed resource
	 * res.destroy();
	 * tracker.remove(res);
	 *
	 * ...
	 *
	 * // Check orphaned resources at cleanup
	 * if(tracker.size() > 0) {
	 *     System.err.println("orphans=" + tracker.stream().collect(toList()));
	 * }
	 *
	 * // Destroy orphaned resources
	 * tracker.destroy();
	 *
	 * </pre>
	 */
	class Tracker<T extends Resource> {
		private final Set<T> resources = ConcurrentHashMap.newKeySet();

		/**
		 * @return Number of allocated resources
		 */
		public int size() {
			return resources.size();
		}

		/**
		 * @return Allocated resources
		 */
		public Stream<T> stream() {
			return resources.stream();
		}

		/**
		 * Registers an allocated resource.
		 * @param res Resource to add
		 * @throws IllegalArgumentException for a duplicate resource
		 */
		public void add(T res) {
			resources.add(res);
		}

		/**
		 * Removes a destroyed resource.
		 * @param res Resource to remove
		 * @throws IllegalArgumentException if the resource has not been registered
		 */
		public void remove(T res) {
			resources.remove(res);
		}

		/**
		 * Destroys all registered resources.
		 */
		public synchronized void destroy() {
			resources.forEach(Resource::destroy);
			resources.clear();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("size", size()).toString();
		}
	}
}
