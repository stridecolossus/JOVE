package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;
import static org.sarge.jove.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>queue</i> is used to submit work to the hardware.
 */
public class Queue implements NativeObject {
	/**
	 * A <i>queue family</i> defines the properties of a group of queues.
	 */
	public static class Family {
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

		/**
		 * Helper - Create a predicate for a queue family that supports presentation to the given surface.
		 * @param surface Vulkan surface
		 * @return Presentation predicate
		 * @see #isPresentationSupported(org.sarge.jove.common.NativeObject.Handle)
		 */
		public static Predicate<Family> predicate(Handle surface) {
			return family -> family.isPresentationSupported(surface);
		}

		private final PhysicalDevice dev;
		private final int index;
		private final int count;
		private final Set<VkQueueFlag> flags;

		/**
		 * Constructor.
		 * @param dev		Physical device
		 * @param index		Family index
		 * @param count		Number of queues
		 * @param flags		Queue flags
		 */
		Family(PhysicalDevice dev, int index, int count, Set<VkQueueFlag> flags) {
			this.dev = notNull(dev);
			this.index = zeroOrMore(index);
			this.count = oneOrMore(count);
			this.flags = Set.copyOf(flags);
		}

		/**
		 * @return Physical device of this queue family
		 */
		PhysicalDevice device() {
			return dev;
		}

		/**
		 * @return Number of queues in this family
		 */
		public int count() {
			return count;
		}

		/**
		 * @return Queue family index
		 */
		public int index() {
			return index;
		}

		/**
		 * @return Flags for this family
		 */
		public Set<VkQueueFlag> flags() {
			return flags;
		}

		/**
		 * @param surface Rendering surface
		 * @return Whether this family supports presentation to the given surface
		 */
		public boolean isPresentationSupported(Handle surface) {
			final VulkanLibrary lib = dev.instance().library();
			final IntByReference supported = lib.factory().integer();
			check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(dev.handle(), index, surface, supported));
			return VulkanBoolean.of(supported.getValue()).toBoolean();
		}

		@Override
		public int hashCode() {
			return Objects.hash(dev, index());
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Family that) &&
					(this.device() == that.device()) &&
					(this.index == that.index);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("index", index).build();
		}
	}

	private final Handle handle;
	private final LogicalDevice dev;
	private final Family family;

	/**
	 * Constructor.
	 * @param handle 		Queue handle
	 * @param dev			Parent logical device
	 * @param family 		Queue family
	 */
	Queue(Pointer handle, LogicalDevice dev, Family family) {
		this.dev = notNull(dev);
		this.handle = new Handle(handle);
		this.family = notNull(family);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Queue family
	 */
	public Family family() {
		return family;
	}

	/**
	 * @return Logical device
	 */
	public LogicalDevice device() {
		return dev;
	}

	/**
	 * Waits for this queue to become idle.
	 */
	public void waitIdle() {
		check(dev.library().vkQueueWaitIdle(handle));
	}

	@Override
	public int hashCode() {
		return Objects.hash(handle, family);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj instanceof Queue that) &&
				handle.equals(that.handle) &&
				family.equals(that.family);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", handle)
				.append("family", family)
				.build();
	}
}
