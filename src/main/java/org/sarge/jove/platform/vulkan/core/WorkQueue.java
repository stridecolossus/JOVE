package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.Set;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntEnum;
import org.sarge.lib.util.Check;

/**
 * A <i>work queue</i> is used to submit tasks to the hardware.
 * @author Sarge
 */
public record WorkQueue(Handle handle, WorkQueue.Family family) implements NativeObject {
	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param family	Family that this queue belongs to
	 */
	public WorkQueue {
		Check.notNull(handle);
		Check.notNull(family);
	}

	/**
	 * Waits for this queue to become idle.
	 * @param lib Vulkan API
	 */
	public void waitIdle(VulkanLibrary lib) {
		check(lib.vkQueueWaitIdle(this));
	}

	/**
	 * A <i>queue family</i> defines the properties of a group of queues.
	 */
	public record Family(int index, int count, Set<VkQueueFlag> flags) {
		/**
		 * Index for the <i>ignored</i> queue family.
		 */
		public static final int IGNORED = (~0);

		/**
		 * Helper - Creates a new queue family from the given descriptor.
		 * @param index		Family index
		 * @param props		Descriptor
		 * @return New queue family
		 */
		public static Family of(int index, VkQueueFamilyProperties props) {
			final var mapping = IntEnum.reverse(VkQueueFlag.class);
			final Set<VkQueueFlag> flags = props.queueFlags.enumerate(mapping);
			return new Family(index, props.queueCount, flags);
		}

		/**
		 * Constructor.
		 * @param index		Family index
		 * @param count		Number of queues in this family
		 * @param flags		Queue flags
		 */
		public Family {
			Check.zeroOrMore(index);
			Check.oneOrMore(count);
			flags = Set.copyOf(flags);
		}
	}
}
