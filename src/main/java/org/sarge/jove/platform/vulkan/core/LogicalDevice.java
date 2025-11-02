package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.sarge.lib.Validation.requireNotEmpty;

import java.util.*;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceFeatures;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.ValidationLayer;
import org.sarge.jove.util.EnumMask;

/**
 * The <i>logical device</i> is an instance of a {@link PhysicalDevice}.
 * @author Sarge
 */
public class LogicalDevice extends TransientNativeObject {
	private final Library library;
	private final Map<Family, List<WorkQueue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param library		Device library
	 * @param queues 		Work queues indexed by family
	 */
	LogicalDevice(Handle handle, Library library, Map<Family, List<WorkQueue>> queues) {
		super(handle);
		this.library = requireNonNull(library);
		this.queues = Map.copyOf(queues);
	}

	/**
	 * @return Device library
	 */
	public Library library() {
		return library;
	}
	// TODO

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<Family, List<WorkQueue>> queues() {
		return queues;
	}

	/**
	 * Blocks until this device becomes idle.
	 */
	public void waitIdle() {
		library.vkDeviceWaitIdle(this);
	}

	/**
	 * Blocks until the given queue becomes idle.
	 * @param queue Work queue
	 */
	public void waitIdle(WorkQueue queue) {
		library.vkQueueWaitIdle(queue);
	}

 	@Override
	protected void release() {
		library.vkDestroyDevice(this, null);
	}

 	@Override
 	public String toString() {
 		return String.format("LogicalDevice[%s]", this.handle());
 	}

	/**
	 * Transient descriptor for a work queue.
	 */
	public record RequiredQueue(Family family, float[] priorities) {
		/**
		 * Constructor.
		 * @param family			Queue family
		 * @param priorities		Priority of each queue
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public RequiredQueue {
			requireNonNull(family);
			priorities = Arrays.copyOf(priorities, priorities.length);

			if(priorities.length > family.count()) {
				throw new IllegalArgumentException("Number of queues exceeds family: available=%d requested=%d".formatted(family.count(), priorities.length));
			}

			for(float f : priorities) {
				if((f < 0) || (f > 1)) {
					throw new IllegalArgumentException("Invalid queue priority: " + f);
				}
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
		 * Constructor for a group of queues with equal priority.
		 * @param family		Queue family
		 * @param count			Number of required queues
		 */
		public RequiredQueue(Family family, int count) {
			this(family, priorities(count));
		}

		private static float[] priorities(int count) {
			final var priorities = new float[count];
			Arrays.fill(priorities, 1f);
			return priorities;
		}

		/**
		 * @return Vulkan descriptor for this required queue
		 */
		private VkDeviceQueueCreateInfo build() {
			final var info = new VkDeviceQueueCreateInfo();
			info.flags = new EnumMask<>();
			info.queueCount = priorities.length;
			info.queueFamilyIndex = family.index();
			info.pQueuePriorities = priorities;
			return info;
		}

		/**
		 * Retrieves the work queues specified for this family.
		 * @param device	Logical device
		 * @param library	Device library
		 * @return Work queues
		 */
		private Stream<WorkQueue> queues(Handle device, Library library) {
			return IntStream
					.range(0, priorities.length)
					.mapToObj(n -> queue(device, n, library));
		}

		/**
		 * Retrieves a work queue.
		 * @param device	Logical device
		 * @param index		Queue index
		 * @param library	Device library
		 * @return Work queue
		 */
		private WorkQueue queue(Handle device, int index, Library library) {
			final var handle = new Pointer();
			library.vkGetDeviceQueue(device, family.index(), index, handle);
			return new WorkQueue(handle.get(), family);
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
		private final List<RequiredQueue> queues = new ArrayList<>();

		/**
		 * Constructor.
		 * @param parent Parent physical device
		 */
		public Builder(PhysicalDevice parent) {
			this.parent = requireNonNull(parent);
		}

		/**
		 * Adds an extension required for this device.
		 * @param name Extension name
		 * @throws IllegalArgumentException for {@link DiagnosticHandler#EXTENSION_DEBUG_UTILS}
		 */
		public Builder extension(String name) {
			requireNotEmpty(name);
			if(DiagnosticHandler.EXTENSION.equals(name)) throw new IllegalArgumentException("Invalid extension for logical device: " + name);
			extensions.add(name);
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
			queues.add(queue);
			return this;
		}

		/**
		 * Constructs this logical device.
		 * @param library Device library
		 * @return New logical device
		 */
		public LogicalDevice build(Library library) {
			// Add required extensions
			final var info = new VkDeviceCreateInfo();
			info.ppEnabledExtensionNames = extensions.toArray(String[]::new);
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = layers.toArray(String[]::new);
			info.enabledLayerCount = layers.size();

			// Add required features
			final var required = new DeviceFeatures(features);
			info.pEnabledFeatures = required.build();

			// Add required queues
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = queues.stream().map(RequiredQueue::build).toArray(VkDeviceQueueCreateInfo[]::new);

			// Create device
			final var pointer = new Pointer();
			library.vkCreateDevice(parent, info, null, pointer);

			// Retrieve work queues
			final Handle handle = pointer.get();
			final Map<Family, List<WorkQueue>> map = queues
						.stream()
						.flatMap(e -> e.queues(handle, library))
						.collect(groupingBy(WorkQueue::family));

			// Create domain object
			return new LogicalDevice(handle, library, map);
		}
	}

	/**
	 * Logical device API.
	 */
	public interface Library {
		/**
		 * Creates a logical device.
		 * @param physicalDevice		Physical device handle
		 * @param pCreateInfo			Device descriptor
		 * @param pAllocator			Allocator
		 * @param device				Returned logical device handle
		 * @return Result
		 */
		VkResult vkCreateDevice(PhysicalDevice physicalDevice, VkDeviceCreateInfo pCreateInfo, Handle pAllocator, Pointer device);

		/**
		 * Destroys a logical device.
		 * @param device				Device handle
		 * @param pAllocator			Allocator
		 */
		void vkDestroyDevice(LogicalDevice device, Handle pAllocator);

		/**
		 * Blocks until the given device becomes idle.
		 * @param device Logical device
		 * @return Result
		 */
		VkResult vkDeviceWaitIdle(LogicalDevice device);

		/**
		 * Retrieves a work queue for the given logical device.
		 * @param device				Device handle
		 * @param queueFamilyIndex		Queue family index
		 * @param queueIndex			Queue index
		 * @param pQueue				Returned queue handle
		 */
		void vkGetDeviceQueue(Handle device, int queueFamilyIndex, int queueIndex, Pointer pQueue);

		// TODO - factor these to separate library?

		/**
		 * Submits work to a queue.
		 * @param queue					Queue
		 * @param submitCount			Number of submissions
		 * @param pSubmits				Work submissions
		 * @param fence					Optional fence
		 * @return Result
		 */
		VkResult vkQueueSubmit(WorkQueue queue, int submitCount, VkSubmitInfo[] pSubmits, Fence fence);

		/**
		 * Blocks until the given queue becomes idle.
		 * @param queue Queue
		 * @return Result
		 */
		VkResult vkQueueWaitIdle(WorkQueue queue);
	}
}
