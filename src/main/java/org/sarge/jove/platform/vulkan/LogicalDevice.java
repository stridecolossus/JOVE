package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;

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
	public static class Queue {
		private final Pointer queue;
		private final QueueFamily family;
		private final VulkanLibrary lib;

		/**
		 * Constructor.
		 * @param handle 	Queue handle
		 * @param family 	Queue family
		 * @param lib		Vulkan library
		 */
		private Queue(Pointer handle, QueueFamily family, VulkanLibrary lib) {
			this.queue = notNull(handle);
			this.family = notNull(family);
			this.lib = notNull(lib);
		}

		/**
		 * @return Queue handle
		 */
		Pointer handle() {
			return queue;
		}

		/**
		 * @return Queue family
		 */
		public QueueFamily family() {
			return family;
		}

		/**
		 * Waits for this queue to become idle.
		 */
		public void waitIdle() {
			check(lib.vkQueueWaitIdle(queue));
		}
	}

	private final Pointer handle;
	private final PhysicalDevice parent;
	private final Map<QueueFamily, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle Device handle
	 * @param parent Parent physical device
	 * @param queues Work queues
	 */
	private LogicalDevice(Pointer handle, PhysicalDevice parent, List<Queue> queues) {
		this.handle = notNull(handle);
		this.parent = notNull(parent);
		this.queues = queues.stream().collect(groupingBy(Queue::family));
	}

	/**
	 * @return Device handle
	 */
	Pointer handle() {
		return handle;
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<QueueFamily, List<Queue>> queues() {
		return queues;
	}

	/**
	 * Waits for this device to become idle.
	 */
	public void waitIdle() {
		parent.instance().library().vkDeviceWaitIdle(handle);
	}

	/**
	 * Destroys this device.
	 */
	public void destroy() {
		final VulkanLibrary api = parent.instance().library();
		check(api.vkDestroyDevice(handle, null));
	}

	/**
	 * Builder for a logical device.
	 */
	public static class Builder {
		/**
		 * Transient wrapper for a queue descriptor.
		 */
		private record QueueWrapper(VkDeviceQueueCreateInfo info, QueueFamily family) {
			/**
			 * Creates the work queues for this descriptor.
			 * @param lib Vulkan library
			 * @param dev Parent logical device handle
			 * @return Work queues
			 */
			public Stream<Queue> create(VulkanLibrary lib, Pointer dev) {
				return IntStream
						.range(0, info.queueCount)
						.mapToObj(n -> create(n, lib, dev));
			}

			/**
			 * Creates a new work queue.
			 * @param index		Queue index
			 * @param lib		Vulkan library
			 * @param dev		Parent logical device handle
			 * @return New queue
			 */
			private Queue create(int index, VulkanLibrary lib, Pointer dev) {
				final PointerByReference handle = lib.factory().pointer();
				lib.vkGetDeviceQueue(dev, family.index(), index, handle);
				return new Queue(handle.getValue(), family, lib);
			}
		}

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

			// Enumerate work queues
			final var list = queues
					.stream()
					.flatMap(q -> q.create(lib, logical.getValue()))
					.collect(toList());

			// Create logical device
			return new LogicalDevice(logical.getValue(), parent, list);
		}
	}
}
