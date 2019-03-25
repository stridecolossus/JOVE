package org.sarge.jove.platform;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;

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