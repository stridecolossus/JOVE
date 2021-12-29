package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkDeviceQueueCreateInfo;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceLimits;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.FloatArray;
import org.sarge.jove.util.ReferenceFactory;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Percentile;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice}.
 * @author Sarge
 */
public class LogicalDevice extends AbstractTransientNativeObject implements DeviceContext {
	private final PhysicalDevice parent;
	private final VulkanLibrary lib;
	private final Map<Family, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent 		Parent physical device
	 * @param queues 		Work queues
	 */
	LogicalDevice(Pointer handle, PhysicalDevice parent, Map<Family, List<Queue>> queues) {
		super(new Handle(handle));
		this.parent = parent;
		this.lib = parent.instance().library();
		this.queues = Map.copyOf(queues);
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	@Override
	public VulkanLibrary library() {
		return parent.instance().library();
	}

	@Override
	public ReferenceFactory factory() {
		return parent.instance().factory();
	}

	@Override
	public VkPhysicalDeviceLimits limits() {
		return parent.properties().limits();
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<Family, List<Queue>> queues() {
		return queues;
	}

	/**
	 * Helper - Retrieves the <i>first</i> queue of the given family.
	 * @param family Queue family
	 * @return Queue
	 * @throws IllegalArgumentException if the queue is not present
	 */
	public Queue queue(Family family) {
		final var list = queues.get(family);
		if((list == null) || list.isEmpty()) {
			throw new IllegalArgumentException(String.format("Queue not present: required=%s available=%s", family, queues.keySet()));
		}
		return list.get(0);
	}

	/**
	 * Waits for this device to become idle.
	 */
	public void waitIdle() {
		check(lib.vkDeviceWaitIdle(this));
	}

 	@Override
	protected void release() {
		lib.vkDestroyDevice(this, null);
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
	 * <p>
	 * Usage:
	 * <pre>
	 * 	PhysicalDevice parent = ...
	 * 	Family family = ...
	 *
	 * 	// Init required device features
	 * 	VkPhysicalDeviceFeatures required = ...
	 *
	 *	// Create device
	 * 	LogicalDevice dev = new Builder(parent)
	 * 		.extension("extension")
	 * 		.queue(family, Percentile.ONE)
	 * 		.features(required)
	 * 		.build();
	 * </pre>
	 * <p>
	 * Note that the various {@link #queue(Family)} methods silently omit duplicates since the physical device may return the same family for a given queue specification.
	 */
	public static class Builder {
		/**
		 * Transient descriptor for required work queues.
		 */
		private record RequiredQueue(Family family, List<Percentile> priorities) {
			private void populate(VkDeviceQueueCreateInfo info) {
				// Convert to floating-point
				final Float[] array = priorities
						.stream()
						.map(Percentile::floatValue)
						.toArray(Float[]::new);

				// Populate queue descriptor
				info.queueCount = array.length;
				info.queueFamilyIndex = family.index();
				info.pQueuePriorities = new FloatArray(ArrayUtils.toPrimitive(array));
			}
		}

		private final PhysicalDevice parent;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final Map<Family, RequiredQueue> queues = new HashMap<>();
		private DeviceFeatures required;

		/**
		 * Constructor.
		 * @param parent Parent physical device
		 */
		public Builder(PhysicalDevice parent) {
			this.parent = notNull(parent);
		}

		/**
		 * Sets the features required by this logical device.
		 * @param required Required features
		 */
		public Builder features(DeviceFeatures required) {
			this.required = notNull(required);
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
		 * @throws IllegalArgumentException if the given family is not a member of the parent physical device
		 */
		public Builder queue(Family family) {
			return queues(family, 1);
		}

		/**
		 * Adds the specified number of queues of the given family to this device.
		 * @param family 		Queue family
		 * @param num			Number of queues
		 * @throws IllegalArgumentException if the given family is not a member of the parent physical device
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public Builder queues(Family family, int num) {
			return queues(family, Collections.nCopies(num, Percentile.ONE));
		}

		/**
		 * Adds multiple queues of the given family with the specified work priorities to this device.
		 * @param family 		Queue family
		 * @param priorities	Queue priorities
		 * @throws IllegalArgumentException if the given family is not a member of the physical device
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public Builder queues(Family family, List<Percentile> priorities) {
			// Validate
			if(!parent.families().contains(family)) {
				throw new IllegalArgumentException("Invalid queue family for this device: " + family);
			}
			if(priorities.isEmpty()) {
				throw new IllegalArgumentException("Queue priorities cannot be empty");
			}
			if(priorities.size() > family.count()) {
				throw new IllegalArgumentException(String.format("Number of queues exceeds family: avaiable=%d requested=%d", priorities.size(), family.count()));
			}

			// Register required queue
			queues.put(family, new RequiredQueue(family, priorities));
			return this;
		}

		/**
		 * Constructs this logical device.
		 * @return New logical device
		 * @throws ServiceException if the device cannot be created or the required features are not supported by the physical device
		 */
		public LogicalDevice build() {
			// Create descriptor
			final VkDeviceCreateInfo info = new VkDeviceCreateInfo();

			// Add required features
			info.pEnabledFeatures = DeviceFeatures.populate(required);

			// Add required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Add queue descriptors
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = StructureHelper.pointer(queues.values(), VkDeviceQueueCreateInfo::new, RequiredQueue::populate);

			// Allocate device
			final Instance instance = parent.instance();
			final VulkanLibrary lib = instance.library();
			final ReferenceFactory factory = instance.factory();
			final PointerByReference handle = factory.pointer();
			check(lib.vkCreateDevice(parent, info, null, handle));

			// Retrieve required queues
			final Map<Family, List<Queue>> map = queues
					.values()
					.stream()
					.map(required -> queues(handle.getValue(), required))
					.map(Arrays::asList)
					.flatMap(List::stream)
					.collect(groupingBy(Queue::family));

			// Create logical device
			return new LogicalDevice(handle.getValue(), parent, map);
		}

		/**
		 * Retrieves the work queues.
		 * @param dev			Logical device handle
		 * @param required		Required queue descriptor
		 * @return Work queues
		 */
		private Queue[] queues(Pointer dev, RequiredQueue required) {
			// Init library
			final Instance instance = parent.instance();
			final Library lib = instance.library();
			final PointerByReference ref = instance.factory().pointer();

			// Retrieve queues
			final int count = required.priorities.size();
			final Queue[] queues = new Queue[count];
			for(int n = 0; n < count; ++n) {
				lib.vkGetDeviceQueue(dev, required.family.index(), n, ref);
				queues[n] = new Queue(new Handle(ref.getValue()), required.family);
			}

			return queues;
		}
	}

	/**
	 * Vulkan logical device API.
	 */
	interface Library {
		/**
		 * Creates a logical device.
		 * @param physicalDevice		Physical device handle
		 * @param pCreateInfo			Device descriptor
		 * @param pAllocator			Allocator
		 * @param device				Returned logical device handle
		 * @return Result
		 */
		int vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference device);

		/**
		 * Destroys a logical device.
		 * @param device				Device handle
		 * @param pAllocator			Allocator
		 * @return Result
		 */
		void vkDestroyDevice(LogicalDevice device, Pointer pAllocator);

		/**
		 * Waits for the given device to become idle.
		 * @param device Logical device
		 * @return Result code
		 */
		int vkDeviceWaitIdle(LogicalDevice device);

		/**
		 * Retrieves logical device queue handle(s).
		 * @param device				Device handle
		 * @param queueFamilyIndex		Queue family index
		 * @param queueIndex			Queue index
		 * @param pQueue				Returned queue handle
		 */
		void vkGetDeviceQueue(Pointer device, int queueFamilyIndex, int queueIndex, PointerByReference pQueue);

		/**
		 * Submits work to a queue.
		 * @param queue					Queue
		 * @param submitCount			Number of submissions
		 * @param pSubmits				Work submissions
		 * @param fence					Optional fence
		 * @return Result code
		 */
		int vkQueueSubmit(Queue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence);

		/**
		 * Waits for the given queue to become idle.
		 * @param queue Queue
		 * @return Result code
		 */
		int vkQueueWaitIdle(Queue queue);
	}
}
