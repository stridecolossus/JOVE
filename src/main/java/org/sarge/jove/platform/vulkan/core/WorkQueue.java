package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.*;

import java.util.Set;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>work queue</i> is used to submit tasks to the hardware.
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
	 * Blocks until this queue becomes idle.
	 * @param vulkan Vulkan API
	 */
	public void waitIdle(VulkanLibrary vulkan) {
		vulkan.vkQueueWaitIdle(this);
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
		 * Queue flag mapper.
		 */
		private static final ReverseMapping<VkQueueFlag> MAPPING = new ReverseMapping<>(VkQueueFlag.class);

		/**
		 * Helper - Creates a new queue family from the given descriptor.
		 * @param index				Family index
		 * @param properties		Descriptor
		 * @return New queue family
		 */
		public static Family of(int index, VkQueueFamilyProperties properties) {
			final Set<VkQueueFlag> flags = properties.queueFlags.enumerate(MAPPING);
			return new Family(index, properties.queueCount, flags);
		}

		/**
		 * Constructor.
		 * @param index		Family index
		 * @param count		Number of queues in this family
		 * @param flags		Queue flags
		 */
		public Family {
			requireZeroOrMore(index);
			requireOneOrMore(count);
			flags = Set.copyOf(flags);
		}
	}
}
