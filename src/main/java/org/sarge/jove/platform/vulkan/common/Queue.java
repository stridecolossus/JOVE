package org.sarge.jove.platform.vulkan.common;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.Set;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.lib.util.Check;

/**
 * A <i>queue</i> is used to submit work to the hardware.
 * @author Sarge
 */
public record Queue(Handle handle, Family family) implements NativeObject {
	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param family	Family that this queue belongs to
	 */
	public Queue {
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
