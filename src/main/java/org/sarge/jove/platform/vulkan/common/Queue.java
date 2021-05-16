package org.sarge.jove.platform.vulkan.common;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.Set;
import java.util.function.Predicate;

import org.sarge.jove.common.NativeObject;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.lib.util.Check;

/**
 * A <i>queue</i> is used to submit work to the hardware.
 * @author Sarge
 */
@SuppressWarnings("unused")
public record Queue(Handle handle, DeviceContext device, Family family) implements NativeObject {
	/**
	 * Constructor.
	 * @param handle	Handle
	 * @param device	Device
	 * @param family	Family that this queue belongs to
	 */
	public Queue {
		Check.notNull(handle);
		Check.notNull(device);
		Check.notNull(family);
	}

	/**
	 * Waits for this queue to become idle.
	 */
	public void waitIdle() {
		check(device.library().vkQueueWaitIdle(handle));
	}

	/**
	 * A <i>queue family</i> defines the properties of a group of queues.
	 */
	public static record Family(int index, int count, Set<VkQueueFlag> flags) {
		/**
		 * Index for the <i>ignored</i> queue family.
		 */
		public static final int IGNORED = (~0);

		/**
		 * Helper - Creates a queue family predicate for the given flags.
		 * @param flags Queue flags
		 * @return Queue flags predicate
		 */
		public static Predicate<Family> predicate(VkQueueFlag... flags) {
			final var list = Arrays.asList(flags);
			return family -> family.flags().containsAll(list);
		}
		// TODO - needed?

		/**
		 * Constructor.
		 * @param index		Family index
		 * @param count		Number of queues in this family
		 * @param flags		Queue flags
		 */
		public Family(int index, int count, Set<VkQueueFlag> flags) {
			this.index = zeroOrMore(index);
			this.count = oneOrMore(count);
			this.flags = Set.copyOf(flags);
		}
	}
}
