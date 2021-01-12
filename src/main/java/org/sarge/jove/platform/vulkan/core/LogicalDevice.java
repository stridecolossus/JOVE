package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Percentile;
import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkDeviceQueueCreateInfo;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.platform.vulkan.util.StructureCollector;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.LazySupplier;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice} that can be used to perform work.
 * @author Sarge
 */
public class LogicalDevice extends AbstractTransientNativeObject {
	private final PhysicalDevice parent;
	private final VulkanLibrary lib;
	private final DeviceFeatures features;
	private final Map<Queue.Family, List<Queue>> queues;
	private final Supplier<VulkanAllocator> allocator;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent 		Parent physical device
	 * @param features		Features supported by this device
	 * @param queues 		Work queues
	 */
	private LogicalDevice(Pointer handle, PhysicalDevice parent, DeviceFeatures features, Set<RequiredQueue> queues) {
		super(handle);
		this.parent = notNull(parent);
		this.lib = parent.instance().library();
		this.features = notNull(features);
		this.queues = queues.stream().flatMap(this::create).collect(groupingBy(Queue::family));
		this.allocator = new LazySupplier<>(() -> new VulkanAllocator(this));
	}

	/**
	 * Creates a set of work queues..
	 * @param info Descriptor for the queues
	 * @return New queues
	 */
	private Stream<Queue> create(RequiredQueue queue) {
		return IntStream.range(0, queue.priorities.size()).mapToObj(n -> create(n, queue.family));
	}

	/**
	 * Creates a new work queue.
	 * @param index		Queue index
	 * @param family	Queue family
	 * @return New queue
	 */
	private Queue create(int index, Queue.Family family) {
		final PointerByReference queue = lib.factory().pointer();
		lib.vkGetDeviceQueue(handle, family.index(), index, queue);
		return new Queue(queue.getValue(), this, family);
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
	 * @return Features supported by this device
	 */
	public DeviceFeatures features() {
		return features;
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<Queue.Family, List<Queue>> queues() {
		return queues;
	}

	/**
	 * @return Memory allocator for this device
	 */
	public VulkanAllocator allocator() {
		return allocator.get();
	}

	/**
	 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
	 */
	public class Semaphore extends AbstractVulkanObject {
		private Semaphore(Pointer handle) {
			super(handle, LogicalDevice.this, lib::vkDestroySemaphore);
		}
	}

	/**
	 * Creates a new semaphore.
	 * @return New semaphore
	 */
	public Semaphore semaphore() {
		final VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
		final PointerByReference handle = lib.factory().pointer();
		VulkanLibrary.check(lib.vkCreateSemaphore(this.handle(), info, null, handle));
		return new Semaphore(handle.getValue());
	}

	/**
	 * Waits for this device to become idle.
	 */
	public void waitIdle() {
		lib.vkDeviceWaitIdle(handle);
	}

 	@Override
	protected void release() {
		lib.vkDestroyDevice(handle, null);
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
	 * A <i>required queue</i> is a transient descriptor for a queue required by this device.
	 */
	private record RequiredQueue(Queue.Family family, List<Percentile> priorities) {
		/**
		 * Constructor.
		 * @param family			Queue family
		 * @param priorities		Priorities
		 */
		public RequiredQueue {
			Check.notNull(family);
			Check.notEmpty(priorities);
			if(priorities.size() > family.count()) {
				throw new IllegalArgumentException(String.format("Requested number of queues exceeds family pool size: num=%d family=%s", priorities.size(), family));
			}
		}

		/**
		 * Populates a descriptor for a queue required by this device.
		 */
		private void populate(VkDeviceQueueCreateInfo info) {
			// Convert percentile priorities to array
			final float[] array = ArrayUtils.toPrimitive(priorities.stream().map(Percentile::floatValue).toArray(Float[]::new));

			// Allocate contiguous memory block for the priorities array
			final Memory mem = new Memory(priorities.size() * Float.BYTES);
			mem.write(0, array, 0, array.length);

			// Populate queue descriptor
			info.queueCount = array.length;
			info.queueFamilyIndex = family.index();
			info.pQueuePriorities = mem;
		}
	}

	/**
	 * Builder for a logical device.
	 * <p>
	 * Note that the various {@link #queue(Queue.Family)} methods silently omit duplicates since the physical device may return the same family for a given queue specification.
	 */
	public static class Builder {
		private final PhysicalDevice parent;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final Set<RequiredQueue> queues = new HashSet<>();
		private VkPhysicalDeviceFeatures features;

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
		 * @param ref Queue reference
		 * @throws NoSuchElementException if the queue reference is invalid
		 * @throws IllegalArgumentException if the given family is not a member of the parent physical device
		 */
		public Builder queue(Queue.Reference ref) {
			return queues(ref, 1);
		}

		/**
		 * Adds the specified number of queues of the given family to this device.
		 * @param ref		Queue reference
		 * @param num		Number of queues
		 * @throws NoSuchElementException if the queue reference is invalid
		 * @throws IllegalArgumentException if the given family is not a member of the parent physical device
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public Builder queues(Queue.Reference ref, int num) {
			return queues(ref, Collections.nCopies(num, Percentile.ONE));
		}

		/**
		 * Adds multiple queues of the given family with the specified work priorities to this device.
		 * @param ref			Queue reference
		 * @param priorities	Queue priorities
		 * @throws NoSuchElementException if the queue reference is invalid
		 * @throws IllegalArgumentException if the given family is not a member of the parent physical device
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 * @throws IllegalArgumentException if the priorities array is empty or any value is not a valid 0..1 percentile
		 */
		public Builder queues(Queue.Reference ref, List<Percentile> priorities) {
			// Validate family for this device
			final Queue.Family family = ref.family();
			if(!parent.families().contains(family)) {
				throw new IllegalArgumentException("Invalid queue family for this device: " + family);
			}

			// Register required queue
			queues.add(new RequiredQueue(family, priorities));
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
			info.pQueueCreateInfos = StructureCollector.toPointer(queues, VkDeviceQueueCreateInfo::new, RequiredQueue::populate);

			// Allocate device
			final VulkanLibrary lib = parent.instance().library();
			final PointerByReference logical = lib.factory().pointer();
			check(lib.vkCreateDevice(parent.handle(), info, null, logical));

			// Create logical device
			return new LogicalDevice(logical.getValue(), parent, new DeviceFeatures(features), queues);
		}
	}
}
