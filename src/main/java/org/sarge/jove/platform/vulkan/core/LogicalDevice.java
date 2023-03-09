package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.stream.*;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.*;
import org.sarge.jove.util.NativeHelper.PointerToFloatArray;
import org.sarge.lib.util.*;

import com.sun.jna.*;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice}.
 * @author Sarge
 */
public class LogicalDevice extends TransientNativeObject implements DeviceContext {
	private final PhysicalDevice parent;
	private final DeviceFeatures features;
	private final DeviceLimits limits;
	private final Map<Family, List<WorkQueue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent 		Parent physical device
	 * @param features		Features enabled on this device
	 * @param limits		Hardware limits
	 * @param queues 		Work queues indexed by family
	 */
	LogicalDevice(Handle handle, PhysicalDevice parent, DeviceFeatures features, DeviceLimits limits, Map<Family, List<WorkQueue>> queues) {
		super(handle);
		this.parent = notNull(parent);
		this.features = notNull(features);
		this.limits = notNull(limits);
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
	public DeviceFeatures features() {
		return features;
	}

	@Override
	public DeviceLimits limits() {
		return limits;
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<Family, List<WorkQueue>> queues() {
		return queues;
	}

	/**
	 * Helper - Retrieves the <i>first</i> queue of the given family.
	 * @param family Queue family
	 * @return Queue
	 * @throws IllegalArgumentException if the queue is not present
	 */
	public WorkQueue queue(Family family) {
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
			Check.notEmpty(priorities);
			if(priorities.size() > family.count()) {
				throw new IllegalArgumentException(String.format("Number of queues exceeds family: available=%d requested=%d", family.count(), priorities.size()));
			}
			priorities = List.copyOf(priorities);
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
			info.pQueuePriorities = new PointerToFloatArray(ArrayUtils.toPrimitive(array));
		}
	}

	/**
	 * Builder for a logical device.
	 * <p>
	 * Usage:
	 * <pre>
	 * // Determine required work queue family
	 * PhysicalDevice parent = ...
	 * Family family = parent.queues().stream().filter(...);
	 *
	 * // Init required device features
	 * DeviceFeatures features = DeviceFeatures.of(...);
	 *
	 * // Create device
	 * LogicalDevice dev = new Builder(parent)
	 *     .extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
	 *     .layer(ValidationLayer.STANDARD_VALIDATION)
	 *     .queue(new RequiredQueue(family))
	 *     .features(required)
	 *     .build();
	 * </pre>
	 */
	public static class Builder {
		private final PhysicalDevice parent;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final Set<String> features = new HashSet<>();
		private final Map<Family, RequiredQueue> queues = new HashMap<>();

		/**
		 * Constructor.
		 * @param parent Parent physical device
		 */
		public Builder(PhysicalDevice parent) {
			this.parent = notNull(parent);
		}

		/**
		 * Adds an extension required for this device.
		 * @param ext Extension name
		 * @throws IllegalArgumentException for {@link Handler#EXTENSION_DEBUG_UTILS}
		 */
		public Builder extension(String ext) {
			Check.notEmpty(ext);
			if(Handler.EXTENSION.equals(ext)) throw new IllegalArgumentException("Invalid extension for logical device: " + ext);
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
		 * Adds a feature required by this device.
		 * @param Required feature
		 */
		public Builder feature(String feature) {
			features.add(feature);
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
		 */
		public LogicalDevice build() {
			// Create descriptor
			final var info = new VkDeviceCreateInfo();

			// Add required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Add required features
			final var required = new DeviceFeatures(features);
			info.pEnabledFeatures = required.structure();

			// Add required queues
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = StructureCollector.pointer(queues.values(), new VkDeviceQueueCreateInfo(), RequiredQueue::populate);

			// Allocate device
			final Instance instance = parent.instance();
			final VulkanLibrary lib = instance.library();
			final PointerByReference ref = instance.factory().pointer();
			check(lib.vkCreateDevice(parent, info, null, ref));

			// Retrieve hardware limits
			final VkPhysicalDeviceProperties props = parent.properties();
			final var limits = new DeviceLimits(props.limits);

			// Retrieve work queues
			final Handle handle = new Handle(ref);
			final var helper = new QueueHelper(handle);
			final var map = helper.queues();

			// Create logical device
			return new LogicalDevice(handle, parent, required, limits, map);
		}

		/**
		 * Helper class for retrieval of the work queues for this device.
		 */
		private class QueueHelper {
			private final Handle dev;

			QueueHelper(Handle dev) {
				this.dev = dev;
			}

			/**
			 * @return Work queues for this device
			 */
			Map<Family, List<WorkQueue>> queues() {
				return queues
						.values()
						.stream()
						.flatMap(this::queues)
						.collect(groupingBy(WorkQueue::family));
			}

			/**
			 * Retrieves the work queues for the given requirements.
			 * @param queue Required work queue
			 * @return Queues
			 */
			private Stream<WorkQueue> queues(RequiredQueue queue) {
				return IntStream
        				.range(0, queue.priorities.size())
        				.mapToObj(n -> queue(queue.family, n));
			}

			/**
			 * Retrieves a work queue.
			 * @param family		Queue family
			 * @param index			Index
			 * @return Work queue
			 */
			private WorkQueue queue(Family family, int index) {
				final Instance instance = parent.instance();
				final Library lib = instance.library();
				final PointerByReference ref = instance.factory().pointer();
				lib.vkGetDeviceQueue(dev, family.index(), index, ref);
				return new WorkQueue(new Handle(ref), family);
			}
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
		 */
		void vkDestroyDevice(LogicalDevice device, Pointer pAllocator);

		/**
		 * Waits for the given device to become idle.
		 * @param device Logical device
		 * @return Result
		 */
		int vkDeviceWaitIdle(LogicalDevice device);

		/**
		 * Retrieves a work queue for the given logical device.
		 * @param device				Device handle
		 * @param queueFamilyIndex		Queue family index
		 * @param queueIndex			Queue index
		 * @param pQueue				Returned queue handle
		 */
		void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, PointerByReference pQueue);

		/**
		 * Submits work to a queue.
		 * @param queue					Queue
		 * @param submitCount			Number of submissions
		 * @param pSubmits				Work submissions
		 * @param fence					Optional fence
		 * @return Result
		 */
		int vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence);

		/**
		 * Waits for the given queue to become idle.
		 * @param queue Queue
		 * @return Result
		 */
		int vkQueueWaitIdle(WorkQueue queue);
	}
}
