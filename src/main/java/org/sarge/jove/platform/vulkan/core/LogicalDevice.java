package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.*;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice}.
 * @author Sarge
 */
public class LogicalDevice extends AbstractTransientNativeObject implements DeviceContext {
	private final PhysicalDevice parent;
	private final DeviceFeatures features;
	private final Supplier<DeviceLimits> limits = new LazySupplier<>(this::loadLimits);
	private final Map<Family, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent 		Parent physical device
	 * @param features		Features enabled on this device
	 * @param queues 		Work queues
	 */
	LogicalDevice(Pointer handle, PhysicalDevice parent, DeviceFeatures features, Map<Family, List<Queue>> queues) {
		super(handle);
		this.parent = notNull(parent);
		this.features = notNull(features);
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

	/**
	 * @return Features enabled on this device
	 */
	public DeviceFeatures features() {
		return features;
	}

	/**
	 * Initialises device limits for this device.
	 */
	private DeviceLimits loadLimits() {
		final VkPhysicalDeviceProperties props = parent.properties();
		return new DeviceLimits(props.limits, features);
	}

	@Override
	public DeviceLimits limits() {
		return limits.get();
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
		check(library().vkDeviceWaitIdle(this));
	}

 	@Override
	protected void release() {
		library().vkDestroyDevice(this, null);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("parent", parent)
				.append("queues", queues.size())
				.build();
	}

	/**
	 * A <i>required queue</i> is a transient descriptor for a work queue for this device.
	 */
	public record RequiredQueue(Family family, List<Percentile> priorities) {
		/**
		 * Constructor.
		 * @param family			Queue family
		 * @param priorities		Priority of each queue
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public RequiredQueue {
			Check.notNull(family);
			priorities = List.copyOf(notEmpty(priorities));
			if(priorities.size() > family.count()) {
				throw new IllegalArgumentException(String.format("Number of queues exceeds family: avaiable=%d requested=%d", priorities.size(), family.count()));
			}
		}

		/**
		 * Constructor for a single queue with a default priority.
		 * @param family Queue family
		 */
		public RequiredQueue(Family family) {
			this(family, 1);
		}

		/**
		 * Constructor for a group of queues with default priority.
		 * @param family		Queue family
		 * @param num			Number of required queues
		 */
		public RequiredQueue(Family family, int num) {
			this(family, Collections.nCopies(num, Percentile.ONE));
		}

		/**
		 * Populates the descriptor for this required work queue.
		 */
		private void populate(VkDeviceQueueCreateInfo info) {
			// Convert to floating-point array
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

	/**
	 * Builder for a logical device.
	 * <p>
	 * Usage:
	 * <pre>
	 * PhysicalDevice parent = ...
	 * Family family = ...
	 *
	 * // Init required device features
	 * VkPhysicalDeviceFeatures required = ...
	 *
	 * // Create device
	 * LogicalDevice dev = new Builder(parent)
	 *     .extension("extension")
	 *     .queue(new RequiredQueue(family))
	 *     .features(required)
	 *     .build();
	 * </pre>
	 */
	public static class Builder {
		private final PhysicalDevice parent;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final Map<Family, RequiredQueue> queues = new HashMap<>();
		private DeviceFeatures required = DeviceFeatures.EMPTY;

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
		 * Adds a required work queue for this device.
		 * @param queue Required queue
		 * @throws IllegalArgumentException if the queue family is not a member of the parent physical device
		 */
		public Builder queue(RequiredQueue queue) {
			final Family family = queue.family;
			if(!parent.families().contains(family)) {
				throw new IllegalArgumentException("Invalid queue family for this device: " + family);
			}
			queues.put(family, queue);
			return this;
		}

		/**
		 * Constructs this logical device.
		 * @return New logical device
		 * @throws ServiceException if the device cannot be created or the required features are not supported by the physical device
		 */
		public LogicalDevice build() {
			// Create descriptor
			final var info = new VkDeviceCreateInfo();

			// Add required features
			info.pEnabledFeatures = required.descriptor();

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
					.flatMap(required -> queues(handle.getValue(), required))
					.collect(groupingBy(Queue::family));

			// Create logical device
			return new LogicalDevice(handle.getValue(), parent, required, map);
		}

//		TODO - implement local by-ref classes?
//		private static class VkDeviceQueueCreateInfo2 extends VkDeviceQueueCreateInfo implements ByReference {
//		}

		/**
		 * Retrieves the work queues.
		 * @param dev			Logical device handle
		 * @param required		Required queue descriptor
		 * @return Work queues
		 */
		private Stream<Queue> queues(Pointer dev, RequiredQueue required) {
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

			return Arrays.stream(queues);
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
