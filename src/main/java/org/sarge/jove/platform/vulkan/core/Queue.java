package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>queue</i> is used to submit work to the hardware.
 * @author Sarge
 */
public class Queue implements NativeObject {
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
	public DeviceContext device() {
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

	/**
	 * A <i>queue reference</i>
	 * TODO
	 * @see Selector
	 */
	static abstract class Reference {
		/**
		 * @param dev Physical device
		 * @throws NoSuchElementException if the queue family does not exist
		 */
		abstract Family family();
	}

	/**
	 * A <i>queue family</i> defines the properties of a group of queues.
	 */
	public static class Family extends Reference {
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
		public final Family family() {
			return this;
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

	/**
	 * A <i>queue selector</i> is a helper class used to select the required work queues from the device.
	 * <p>
	 * Example usage:
	 * <pre>
	 *  // Define a selector for a queue
	 *  Selector selector = Selector.of(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
	 *
	 *  // Select a physical device that supports this queue
	 *  PhysicalDevice parent = PhysicalDevice.enumerate(...)
	 *      .filter(selector)
	 *      ...
	 *
	 *  // Specify the required queue properties for the logical device
	 *  LogicalDevice dev = new LogicalDevice.Builder(parent)
	 *  	.queue(selector, 3)
	 *  	...
	 *
	 *  // Retrieve the queue(s)
	 *  List&lt;Queue&gt; list = selector.list();
	 *  Queue queue = selector.queue();
	 * </pre>
	 * <p>
	 * @see PhysicalDevice#families()
	 * @see LogicalDevice#queues()
	 */
	public static class Selector extends Reference implements Predicate<PhysicalDevice> {
		/**
		 * Creates a selector for a queue with the given flags.
		 * @param flags Queue flag(s)
		 * @return New queue selector
		 * @see Family#flags()
		 */
		public static Selector of(VkQueueFlag... flags) {
			final var set = Arrays.asList(flags);
			return new Selector(family -> family.flags.containsAll(set));
		}

		/**
		 * Creates a selector for a queue that supports presentation to the given Vulkan surface.
		 * @param surface Vulkan surface
		 * @return New presentation selector
		 * @see Family#isPresentationSupported(org.sarge.jove.common.NativeObject.Handle)
		 */
		public static Selector of(Handle surface) {
			return new Selector(family -> family.isPresentationSupported(surface));
		}

		private final Predicate<Family> predicate;

		private Optional<Family> family = Optional.empty();
		private Queue queue;

		/**
		 * Constructor.
		 * @param predicate Queue predicate
		 */
		public Selector(Predicate<Family> predicate) {
			this.predicate = notNull(predicate);
		}

		@Override
		public boolean test(PhysicalDevice dev) {
			assert family.isEmpty();
			family = dev.families().stream().filter(predicate).findAny();
			return family.isPresent();
		}

		@Override
		Family family() {
			return family.orElseThrow();
		}

		/**
		 * Retrieves the work queues specified by this selector from the given device.
		 * @param dev Logical device
		 * @return Queues
		 * @throws NoSuchElementException if the queue is not present
		 */
		public List<Queue> list(LogicalDevice dev) {
			assert queue == null;
			final var list = dev.queues().get(family());
			if(list == null) throw new NoSuchElementException("Queue family is not available");
			return list;
		}

		/**
		 * Retrieves the <b>first</b> work queue specified by this selector from the given device.
		 * @param dev Logical device
		 * @return Queue
		 * @throws NoSuchElementException if the queue is not present
		 */
		public Queue queue(LogicalDevice dev) {
			if(queue == null) {
				final var list = list(dev);
				queue = list.get(0);
			}
			return queue;
		}
	}
}
