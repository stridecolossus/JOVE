package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.*;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.PointerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.lib.Percentile;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice}.
 * @author Sarge
 */
public class LogicalDevice extends TransientNativeObject implements DeviceContext {
	private final PhysicalDevice parent;
	private final DeviceFeatures features;
	private final VkPhysicalDeviceLimits limits;
	private final Map<Family, List<WorkQueue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent 		Parent physical device
	 * @param features		Features enabled on this device
	 * @param limits		Hardware limits
	 * @param queues 		Work queues indexed by family
	 */
	LogicalDevice(Handle handle, PhysicalDevice parent, DeviceFeatures features, VkPhysicalDeviceLimits limits, Map<Family, List<WorkQueue>> queues) {
		super(handle);
		this.parent = requireNonNull(parent);
		this.features = requireNonNull(features);
		this.limits = requireNonNull(limits);
		this.queues = Map.copyOf(queues);
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	/**
	 * @return Vulkan
	 */
	@Override
	public Vulkan vulkan() {
		return parent.instance().vulkan();
	}

	/**
	 * @return Features of this device
	 */
	public DeviceFeatures features() {
		return features;
	}

	/**
	 * @return Device limits
	 */
	public VkPhysicalDeviceLimits limits() {
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

//	@Override
	public void waitIdle() {
		this.vulkan().library().vkDeviceWaitIdle(this);
	}

 	@Override
	protected void release() {
		this.vulkan().library().vkDestroyDevice(this, null);
	}

	/**
	 * A <i>required queue</i> is a transient descriptor for a work queue.
	 */
	public record RequiredQueue(Family family, List<Percentile> priorities) {
		/**
		 * Constructor.
		 * @param family			Queue family
		 * @param priorities		Priority of each queue
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public RequiredQueue {
			requireNonNull(family);
			requireNotEmpty(priorities);
			if(priorities.size() > family.count()) {
				throw new IllegalArgumentException("Number of queues exceeds family: available=%d requested=%d".formatted(family.count(), priorities.size()));
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
		 * Constructor for a group of queues with a default priority.
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
			final float[] array = new float[priorities.size()];
			for(int n = 0; n < array.length; ++n) {
				array[n] = priorities.get(n).floatValue();
			}

			// Populate queue descriptor
			info.queueCount = array.length;
			info.queueFamilyIndex = family.index();
			info.pQueuePriorities = array;

			throw new UnsupportedOperationException(); // TODO - pQueuePriorities: primitive array => pointer?
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
			this.parent = requireNonNull(parent);
		}

		/**
		 * Adds an extension required for this device.
		 * @param ext Extension name
		 * @throws IllegalArgumentException for {@link DiagnosticHandler#EXTENSION_DEBUG_UTILS}
		 */
		public Builder extension(String ext) {
			requireNotEmpty(ext);
			if(DiagnosticHandler.EXTENSION.equals(ext)) throw new IllegalArgumentException("Invalid extension for logical device: " + ext);
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
			info.ppEnabledExtensionNames = extensions.toArray(String[]::new);
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = layers.toArray(String[]::new);
			info.enabledLayerCount = layers.size();

			// Add required features
			final var required = new DeviceFeatures(features);
			info.pEnabledFeatures = required.structure();

			// Add required queues
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = null; // TODO - StructureCollector.pointer(queues.values(), new VkDeviceQueueCreateInfo(), RequiredQueue::populate);

			// Allocate device
			//final Instance instance = parent.instance();
			//final VulkanLibrary lib = instance.library();
			final Vulkan vulkan = parent.instance().vulkan();
			final PointerReference ref = vulkan.factory().pointer();
			vulkan.library().vkCreateDevice(parent, info, null, ref);

			// Retrieve hardware limits
			final VkPhysicalDeviceProperties props = parent.properties();

			// Retrieve work queues
			final Handle handle = ref.handle();
			final var helper = new QueueHelper(vulkan, handle);
			final var map = helper.queues();

			// Create logical device
			return new LogicalDevice(handle, parent, required, props.limits, map);
		}

		/**
		 * Helper class for retrieval of the work queues for this device.
		 */
		private class QueueHelper {
			private final Vulkan vulkan;
			private final Handle dev;

			QueueHelper(Vulkan vulkan, Handle dev) {
				this.vulkan = vulkan;
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
				final PointerReference ref = vulkan.factory().pointer();
				vulkan.library().vkGetDeviceQueue(dev, family.index(), index, ref);
				return new WorkQueue(ref.handle(), family);
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
		int vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, PointerReference device);

		/**
		 * Destroys a logical device.
		 * @param device				Device handle
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDevice(LogicalDevice device, Handle pAllocator);

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
		void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, PointerReference pQueue);

		/**
		 * Submits work to a queue.
		 * @param queue					Queue
		 * @param submitCount			Number of submissions
		 * @param pSubmits				Work submissions
		 * @param fence					Optional fence
		 * @return Result
		 */
		int vkQueueSubmit(WorkQueue queue, int submitCount, Collection<VkSubmitInfo> pSubmits, Fence fence);

		/**
		 * Waits for the given queue to become idle.
		 * @param queue Queue
		 * @return Result
		 */
		int vkQueueWaitIdle(WorkQueue queue);
	}
}
