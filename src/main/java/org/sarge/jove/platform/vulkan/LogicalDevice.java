package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	public class Queue {
		private final Pointer queue;
		private final QueueFamily family;

		/**
		 * Constructor.
		 * @param handle Queue handle
		 * @param family Queue family
		 */
		private Queue(Pointer handle, QueueFamily family) {
			this.queue = notNull(handle);
			this.family = notNull(family);
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
		 * @return Logical device for this queue
		 */
		public LogicalDevice device() {
			return LogicalDevice.this;
		}

		/**
		 * Waits for this queue to become idle.
		 */
		public void waitIdle() {
			final VulkanLibrary api = device().parent.vulkan().api();
			check(api.vkQueueWaitIdle(queue));
		}
	}

	private final Pointer handle;
	private final PhysicalDevice parent;
	private final Map<QueueFamily, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle Device handle
	 * @param parent Parent physical device
	 * @param queues Work queues ordered by family
	 */
	LogicalDevice(Pointer handle, PhysicalDevice parent, Map<QueueFamily, List<Pointer>> queues) {
		this.handle = notNull(handle);
		this.parent = notNull(parent);
		this.queues = build(queues);
	}

	/**
	 * Creates work queue instances for the given handles.
	 */
	private Map<QueueFamily, List<Queue>> build(Map<QueueFamily, List<Pointer>> queues) {
		final Map<QueueFamily, List<Queue>> map = new HashMap<>();
		for(final var entry : queues.entrySet()) {
			final QueueFamily family = entry.getKey();
			final List<Queue> list = build(family, entry.getValue());
			map.put(family, List.copyOf(list));
		}
		return Map.copyOf(map);
	}

	/**
	 * Creates work queue instances for the given handles.
	 */
	private List<Queue> build(QueueFamily family, List<Pointer> handles) {
		return handles
				.stream()
				.map(ptr -> new Queue(ptr, family))
				.collect(toList());
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
		parent.vulkan().api().vkDeviceWaitIdle(handle);
	}

	/**
	 * Destroys this device.
	 */
	public void destroy() {
		final VulkanLibrary api = parent.vulkan().api();
		check(api.vkDestroyDevice(handle, null));
	}

	/**
	 * Builder for a logical device.
	 */
	public static class Builder {
		private PhysicalDevice parent;
		private VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final List<VkDeviceQueueCreateInfo> queues = new ArrayList<>();

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
		 */
		public Builder extension(String ext) {
			Check.notEmpty(ext);
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
			return queue(family, 1);
		}

		/**
		 * Adds the specified number of queues of the given family to this device.
		 * @param family	Queue family
		 * @param num		Number of queues
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public Builder queue(QueueFamily family, int num) {
			final float[] priorities = new float[num];
			Arrays.fill(priorities, 1);
			return queue(family, priorities);
		}

		/**
		 * Adds multiple queues of the given family with the specified work priorities to this device.
		 * @param family		Queue family
		 * @param priorities	Queue priorities
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 * @throws IllegalArgumentException if the priorities array is empty or any value is not a valid 0..1 percentile
		 */
		public Builder queue(QueueFamily family, float[] priorities) {
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
			queues.add(info);

			return this;
		}

		/**
		 * Constructs this logical device.
		 * @return New logical device
		 * @throws ServiceException if the device cannot be created or the required features are not supported by the physical device
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
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = StructureHelper.structures(queues);

			// Allocate device
			final Vulkan vulkan = parent.vulkan();
			final VulkanLibraryLogicalDevice api = vulkan.api();
			final PointerByReference logical = vulkan.pointer();
			check(api.vkCreateDevice(parent.handle(), info, null, logical));

			// Retrieve queues
			final Map<QueueFamily, List<Pointer>> map = new HashMap<>();
			for(VkDeviceQueueCreateInfo queueInfo : queues) {
				final QueueFamily family = parent.families().get(queueInfo.queueFamilyIndex);
				for(int n = 0; n < queueInfo.queueCount; ++n) {
					final PointerByReference ref = vulkan.pointer();
					api.vkGetDeviceQueue(logical.getValue(), family.index(), n, ref);
					map.computeIfAbsent(family, ignored -> new ArrayList<>()).add(ref.getValue());
				}
			}

			// Create logical device
			return new LogicalDevice(logical.getValue(), parent, map);
		}
	}
}
