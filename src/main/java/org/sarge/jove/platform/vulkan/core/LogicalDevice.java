package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkDeviceQueueCreateInfo;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.QueueFamily;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice} that can be used to perform work.
 * @author Sarge
 */
public class LogicalDevice {
	/**
	 * A <i>work queue</i> is used to submit work to this logical device.
	 */
	public class Queue {
		private final Handle queue;
		private final QueueFamily family;

		/**
		 * Constructor.
		 * @param handle 	Queue handle
		 * @param family 	Queue family
		 */
		private Queue(Pointer handle, QueueFamily family) {
			this.queue = new Handle(handle);
			this.family = notNull(family);
		}

		/**
		 * @return Queue handle
		 */
		public Handle handle() {
			return queue;
		}

		/**
		 * @return Queue family
		 */
		public QueueFamily family() {
			return family;
		}

		/**
		 * @return Logical device
		 */
		public LogicalDevice device() {
			return LogicalDevice.this;
		}

		/**
		 * Waits for this queue to become idle.
		 */
		public void waitIdle() {
			check(lib.vkQueueWaitIdle(queue));
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj instanceof Queue that) &&
					queue.equals(that.queue) &&
					family.equals(that.family);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("queue", queue)
					.append("family", family)
					.append("dev", device())
					.build();
		}
	}

	/**
	 * Transient wrapper for a queue descriptor.
	 */
	private record QueueWrapper(VkDeviceQueueCreateInfo info, QueueFamily family) {
		// Record
	}

	private final Handle handle;
	private final PhysicalDevice parent;
	private final VulkanLibrary lib;
	private final Map<QueueFamily, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle Device handle
	 * @param parent Parent physical device
	 * @param queues Work queues
	 */
	private LogicalDevice(Pointer handle, PhysicalDevice parent, List<QueueWrapper> queues) {
		this.handle = new Handle(handle);
		this.parent = notNull(parent);
		this.lib = parent.instance().library();
		this.queues = queues.stream().flatMap(this::create).collect(groupingBy(Queue::family));
	}

	/**
	 * Creates the work queues for the given wrapper.
	 * @param wrapper Queue wrapper
	 * @return Queues
	 */
	private Stream<Queue> create(QueueWrapper wrapper) {
		return IntStream
				.range(0, wrapper.info.queueCount)
				.mapToObj(n -> create(n, wrapper.family));
	}

	/**
	 * Creates a new work queue.
	 * @param index		Queue index
	 * @param family	Queue family
	 * @return New queue
	 */
	private Queue create(int index, QueueFamily family) {
		final PointerByReference queue = lib.factory().pointer();
		lib.vkGetDeviceQueue(handle, family.index(), index, queue);
		return new Queue(queue.getValue(), family);
	}

	/**
	 * @return Device handle
	 */
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	/**
	 * @return Vulkan library
	 */
	public VulkanLibrary library() {
		return parent.instance().library();
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<QueueFamily, List<Queue>> queues() {
		return queues;
	}

	/**
	 * Helper - Looks up the work queue(s) for the given family.
	 * @param family Queue family
	 * @return Queue(s)
	 * @throws IllegalArgumentException if this device does not contain queues with the given family
	 */
	public List<Queue> queues(QueueFamily family) {
		final var list = queues.get(family);
		if(list == null) throw new IllegalArgumentException("");
		return list;
	}

	/**
	 * Helper - Looks up the <b>first</b> work queue for the given family.
	 * @param family Queue family
	 * @return Queue
	 * @throws IllegalArgumentException if this device does not contain a queue with the given family
	 */
	public Queue queue(QueueFamily family) {
		return queues(family).get(0);
	}

	/**
	 * Waits for this device to become idle.
	 */
	public void waitIdle() {
		lib.vkDeviceWaitIdle(handle);
	}

	/**
	 * Destroys this device.
	 */
	public void destroy() {
		check(lib.vkDestroyDevice(handle, null));
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", handle)
				.append("parent", parent)
				.append("queues", queues.size())
				.build();
	}

	/**
	 * Builder for a logical device.
	 */
	public static class Builder {
		private PhysicalDevice parent;
		private VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final List<QueueWrapper> queues = new ArrayList<>();

		/**
		 * Sets the parent of this device.
		 * @param parent Parent physical device
		 */
		public Builder parent(PhysicalDevice parent) {
			this.parent = notNull(parent);
			return this;
		}

		/**
		 * Sets the required features for this device.
		 * @param features Required features
		 */
		public Builder features(VkPhysicalDeviceFeatures features) {
			this.features = notNull(features);
			return this;
		}

		/**
		 * Adds an extension required for this device.
		 * @param ext Extension name
		 * @throws IllegalArgumentException for {@link VulkanLibrary#EXTENSION_DEBUG_UTILS}
		 */
		public Builder extension(String ext) {
			Check.notEmpty(ext);
			if(VulkanLibrary.EXTENSION_DEBUG_UTILS.equals(ext)) throw new IllegalArgumentException("Invalid extensions for logical device: " + ext);
			extensions.add(ext);
			return this;
		}

		/**
		 * Adds a validation layer required for this device.
		 * @param layer Validation layer
		 */
		public Builder layer(ValidationLayer layer) {
			layers.add(layer.name());
			return this;
		}

		/**
		 * Adds a <b>single</b> queue of the given family to this device.
		 * @param family Queue family
		 */
		public Builder queue(QueueFamily family) {
			return queues(family, 1);
		}

		/**
		 * Adds the specified number of queues of the given family to this device.
		 * @param family	Queue family
		 * @param num		Number of queues
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public Builder queues(QueueFamily family, int num) {
			final float[] priorities = new float[num];
			Arrays.fill(priorities, 1);
			return queues(family, priorities);
		}

		/**
		 * Adds multiple queues of the given family with the specified work priorities to this device.
		 * @param family		Queue family
		 * @param priorities	Queue priorities
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 * @throws IllegalArgumentException if the priorities array is empty or any value is not a valid 0..1 percentile
		 */
		public Builder queues(QueueFamily family, float[] priorities) {
			// Validate priorities
			Check.notEmpty(priorities);
			if(priorities.length > family.count()) throw new IllegalArgumentException(String.format("Requested number of queues exceeds family pool size: num=%d family=%s", priorities.length, family));
			for(float f : priorities) {
				if((f < 0) || (f > 1)) throw new IllegalArgumentException("Invalid queue priority: " + Arrays.toString(priorities));
			}

			// Allocate contiguous memory block for the priorities
			final Memory mem = new Memory(priorities.length * Float.BYTES);
			mem.write(0, priorities, 0, priorities.length);

			// Init descriptor
			final VkDeviceQueueCreateInfo info = new VkDeviceQueueCreateInfo();
			info.queueCount = priorities.length;
			info.queueFamilyIndex = family.index();
			info.pQueuePriorities = mem;

			// Add queue
			queues.add(new QueueWrapper(info, family));

			return this;
		}

		/**
		 * Constructs this logical device.
		 * @return New logical device
		 * @throws ServiceException if the device cannot be created or the required features are not supported by the physical device
		 * @throws IllegalArgumentException if the parent device has not been populated
		 * @throws IllegalArgumentException if any requested queue is not a member of the physical device
		 */
		public LogicalDevice build() {
			// Create descriptor
			if(parent == null) throw new IllegalArgumentException("Parent physical device not specified");
			final VkDeviceCreateInfo info = new VkDeviceCreateInfo();

			// Add required features
			info.pEnabledFeatures = features;

			// Add required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Add queue descriptors
			if(!queues.stream().map(QueueWrapper::family).map(QueueFamily::device).allMatch(parent::equals)) throw new IllegalArgumentException("Invalid queue family for the parent device");
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = StructureHelper.structures(queues.stream().map(QueueWrapper::info).collect(toList()));

			// Allocate device
			final VulkanLibrary lib = parent.instance().library();
			final PointerByReference logical = lib.factory().pointer();
			check(lib.vkCreateDevice(parent.handle(), info, null, logical));

			// Create logical device
			return new LogicalDevice(logical.getValue(), parent, queues);
		}
	}
}
