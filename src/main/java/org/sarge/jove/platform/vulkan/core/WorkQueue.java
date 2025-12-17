package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.Set;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.VkQueueFlags;

/**
 * A <i>work queue</i> is used to submit tasks to the hardware.
 * @see Work
 * @author Sarge
 */
public record WorkQueue(Handle handle, Family family) implements NativeObject {
	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param family	Family that this queue belongs to
	 */
	public WorkQueue {
		requireNonNull(handle);
		requireNonNull(family);
	}

	/**
	 * A <i>queue family</i> defines the properties of a group of queues.
	 */
	public record Family(int index, int count, Set<VkQueueFlags> flags) {
		/**
		 * Ignored queue family.
		 */
		public static final Family IGNORED = new Family(-1, 1, Set.of());

		/**
		 * Constructor.
		 * @param index		Family index
		 * @param count		Number of queues in this family
		 * @param flags		Queue flags
		 */
		public Family {
			requireOneOrMore(count);
			flags = Set.copyOf(flags);
		}
	}
}
