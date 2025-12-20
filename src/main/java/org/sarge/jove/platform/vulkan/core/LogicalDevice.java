package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.util.Validation.requireNotEmpty;

import java.util.*;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.Pointer;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.util.*;

/**
 * The <i>logical device</i> is an instance of a {@link PhysicalDevice}.
 * @author Sarge
 */
public class LogicalDevice extends TransientNativeObject {
	private final Map<Family, List<WorkQueue>> queues;
	private final DeviceLimits limits;
	private final Library library;
	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param queues 		Work queues indexed by family
	 * @param limits		Device limits
	 * @param library		Device library
	 */
	LogicalDevice(Handle handle, Map<Family, List<WorkQueue>> queues, DeviceLimits limits, Library library) {
		super(handle);
		this.queues = Map.copyOf(queues);
		this.limits = requireNonNull(limits);
		this.library = requireNonNull(library);
	}

	/**
	 * @param <T> Library type
	 * @return Vulkan library
	 */
	@SuppressWarnings("unchecked")
	public <T> T library() {
		return (T) library;
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<Family, List<WorkQueue>> queues() {
		return queues;
	}

	/**
	 * Helper.
	 * Retrieves the <b>first</b> queue of the given family.
	 * @param family Queue family
	 * @return Queue
	 * @throws NoSuchElementException if the queue is not present
	 */
	public WorkQueue queue(Family family) {
		return queues.getOrDefault(family, List.of()).getFirst();
	}

	/**
	 * @return Device limits
	 */
	public DeviceLimits limits() {
		return limits;
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

	/**
	 * Builder for a logical device.
	 */
	public static class Builder {
		/**
		 * A <i>required queue</i> is a transient descriptor for a number of work queues to be created for a logical device.
		 * <p>
		 * Note that only <b>one</b> required queue can be configured for a given queue family.
		 * <p>
		 * @see PhysicalDevice.Selector
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
				priorities = List.copyOf(priorities);

				if(priorities.size() > family.count()) {
					throw new IllegalArgumentException("Number of queues exceeds family: available=%d requested=%d".formatted(family.count(), priorities.size()));
				}
			}

			/**
			 * Constructor for a single queue with a priority of {@code one}.
			 * @param family Queue family
			 */
			public RequiredQueue(Family family) {
				this(family, 1);
			}

			/**
			 * Constructor for a group of queues with an equal priority of {@code one}.
			 * @param family		Queue family
			 * @param count			Number of required queues
			 */
			public RequiredQueue(Family family, int count) {
				this(family, Collections.nCopies(count, Percentile.ONE));
			}

			/**
			 * @return Vulkan descriptor for this set of queues
			 */
			private VkDeviceQueueCreateInfo build() {
				final var info = new VkDeviceQueueCreateInfo();
				info.sType = VkStructureType.DEVICE_QUEUE_CREATE_INFO;
				info.flags = new EnumMask<>();
				info.queueCount = priorities.size();
				info.queueFamilyIndex = family.index();
				info.pQueuePriorities = array(priorities);
				return info;
			}

			/**
			 * Converts queue priorities to an array.
			 * @implNote There is no built-in helper to initialise a floating-point array or stream
			 */
			private static float[] array(List<Percentile> priorities) {
				final int length = priorities.size();
				final float[] array = new float[length];
				for(int n = 0; n < length; ++n) {
					array[n] = priorities.get(n).value();
				}
				return array;
			}

			/**
			 * Helper to retrieve the work queues for this device.
			 */
			private record Helper(Handle device, Library library) {
				/**
				 * Retrieves the work queues for the given device.
				 * @param queues Required queues
				 * @return Work queues indexed by family
				 */
				public Map<Family, List<WorkQueue>> queues(List<RequiredQueue> queues) {
					return queues
							.stream()
							.flatMap(this::queues)
							.collect(groupingBy(WorkQueue::family));
				}

				/**
				 * Retrieves work queues for the given specification.
				 */
				private Stream<WorkQueue> queues(RequiredQueue queue) {
					return IntStream
							.range(0, queue.priorities.size())
							.mapToObj(n -> queue(queue.family, n));
				}

				/**
				 * Retrieves a work queue.
				 * @param family	Queue family
				 * @param index		Queue index
				 * @return Work queue
				 */
				private WorkQueue queue(Family family, int index) {
					final var pointer = new Pointer();
					library.vkGetDeviceQueue(device, family.index(), index, pointer);
					return new WorkQueue(pointer.handle(), family);
				}
			}
		}

		private final PhysicalDevice device;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final Set<String> features = new HashSet<>();
		private final List<RequiredQueue> queues = new ArrayList<>();

		/**
		 * Constructor.
		 * @param device Parent physical device
		 */
		public Builder(PhysicalDevice device) {
			this.device = requireNonNull(device);
		}

		/**
		 * Adds an extension required for this device.
		 * @param extension Extension
		 * @throws IllegalArgumentException for the {@link DiagnosticHandler#EXTENSION_DEBUG_UTILS}
		 */
		public Builder extension(String extension) {
			if(DiagnosticHandler.EXTENSION.equals(extension)) {
				throw new IllegalArgumentException("Invalid extension for logical device: " + extension);
			}
			extensions.add(extension);
			return this;
		}

		/**
		 * Adds a validation layer required for this device.
		 * @param layer Validation layer
		 * @see Vulkan#STANDARD_VALIDATION
		 */
		public Builder layer(String layer) {
			layers.add(layer);
			return this;
		}

		/**
		 * Adds a feature required by this device.
		 * @param Required feature
		 */
		public Builder feature(String feature) {
			// TODO - check whether in parent device first? not much point in asking for it otherwise? => init this.features to parent.features?
			features.add(feature);
			return this;
		}

		/**
		 * Adds a required work queue for this device.
		 * Ignores a required queue with a family that has already been added.
		 * @param queue Required queue
		 * @throws IllegalArgumentException if the queue family is not a member of the parent physical device
		 */
		public Builder queue(RequiredQueue queue) {
			// Check queue family is a member of the physical device
			if(!device.families().contains(queue.family)) {
				throw new IllegalArgumentException("Invalid queue family for this device: " + queue.family);
			}

			// Ensure only one required queue per family
			final boolean distinct = queues
					.stream()
					.map(RequiredQueue::family)
					.noneMatch(queue.family::equals);

			if(distinct) {
				queues.add(queue);
			}

			return this;
		}

		/**
		 * Helper.
		 * Adds a required queue for the given selector.
		 * @param selector Physical device selector
		 * @throws IllegalStateException if the selected queue family is {@code null}
		 */
		public Builder queue(Selector selector) {
			final Family family = selector.family(device);
			if(family == null) {
				throw new IllegalStateException("No queue family selected");
			}
			return queue(new RequiredQueue(family));
		}

		/**
		 * Constructs this logical device.
		 * @param library Device library
		 * @return New logical device
		 */
		public LogicalDevice build(Library library) {
			// Create device
			final VkDeviceCreateInfo info = populate();
			final var pointer = new Pointer();
			library.vkCreateDevice(device, info, null, pointer);

			// Retrieve work queues
			final Handle handle = pointer.handle();
			final var helper = new RequiredQueue.Helper(handle, library);
			final Map<Family, List<WorkQueue>> map = helper.queues(queues);

			// Retrieve device limits
			final VkPhysicalDeviceProperties properties = device.properties();
			final var limits = new DeviceLimits(properties.limits);

			// Create domain object
			return new LogicalDevice(handle, map, limits, library);
		}

		/**
		 * @return Create descriptor for this device
		 */
		private VkDeviceCreateInfo populate() {
			// Add required extensions
			final var info = new VkDeviceCreateInfo();
			info.sType = VkStructureType.DEVICE_CREATE_INFO;
			info.enabledExtensionCount = extensions.size();
			info.ppEnabledExtensionNames = extensions.toArray(String[]::new);

			// Add validation layers
			info.enabledLayerCount = layers.size();
			info.ppEnabledLayerNames = layers.toArray(String[]::new);

			// Add required features
			final var required = new DeviceFeatures(features);
			info.pEnabledFeatures = new VkPhysicalDeviceFeatures[]{required.build()};

			// Add required queues
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = queues.stream().map(RequiredQueue::build).toArray(VkDeviceQueueCreateInfo[]::new);

			return info;
		}
	}

	/**
	 * Logical device API.
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

		/**
		 * Blocks until the given queue becomes idle.
		 * @param queue Queue
		 * @return Result
		 */
		VkResult vkQueueWaitIdle(WorkQueue queue);
	}
}
