package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.NativeObject.TransientNativeObject;
import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkDeviceQueueCreateInfo;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkSemaphoreCreateInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary.VulkanStructure;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.util.Check;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice} that can be used to perform work.
 * @author Sarge
 */
public class LogicalDevice implements TransientNativeObject {
	private final Handle handle;
	private final PhysicalDevice parent;
	private final VulkanLibrary lib;
	private final DeviceFeatures features;
	private final Map<Queue.Family, List<Queue>> queues;
	private final MemoryAllocator allocator;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent 		Parent physical device
	 * @param features		Features supported by this device
	 * @param queues 		Work queues
	 */
	private LogicalDevice(Pointer handle, PhysicalDevice parent, DeviceFeatures features, Set<RequiredQueue> queues) {
		this.handle = new Handle(handle);
		this.parent = notNull(parent);
		this.lib = parent.instance().library();
		this.features = notNull(features);
		this.queues = queues.stream().flatMap(this::create).collect(groupingBy(Queue::family));
		this.allocator = MemoryAllocator.create(this);
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
	 * @return Device handle
	 */
	@Override
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
	public MemoryAllocator allocator() {
		return allocator;
	}

	/**
	 * Helper - Looks up the work queue(s) for the given family.
	 * @param family Queue family
	 * @return Queue(s)
	 * @throws IllegalArgumentException if this device does not contain queues with the given family
	 */
	public List<Queue> queues(Queue.Family family) {
		final var list = queues.get(family);
		if(list == null) throw new IllegalArgumentException("Queue family not present: " + family);
		return list;
	}

	/**
	 * Helper - Looks up the <b>first</b> work queue for the given family.
	 * @param family Queue family
	 * @return Queue
	 * @throws IllegalArgumentException if this device does not contain a queue with the given family
	 */
	public Queue queue(Queue.Family family) {
		return queues(family).get(0);
	}

	/**
	 * A <i>semaphore</i> is used to synchronise operations within or across command queues.
	 */
	public class Semaphore extends AbstractVulkanObject {
		/**
		 * Constructor.
		 * @param handle Semaphore handle
		 */
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
	public void destroy() {
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
	private record RequiredQueue(Queue.Family family, List<Float> priorities) {
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

			for(float f : priorities) {
				if((f < 0) || (f > 1)) {
					throw new IllegalArgumentException("Invalid queue priority: " + priorities);
				}
			}
		}

		/**
		 * Populates a descriptor for a queue required by this device.
		 */
		private void populate(VkDeviceQueueCreateInfo info) {
			// Allocate contiguous memory block for the priorities array
			final float[] array = ArrayUtils.toPrimitive(priorities.toArray(Float[]::new));
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
		private DeviceFeatures features = new DeviceFeatures(new VkPhysicalDeviceFeatures());

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
		 * @throws IllegalStateException if the feature is not supported by the parent physical device
		 * @see DeviceFeatures#check(DeviceFeatures)
		 */
		public Builder features(DeviceFeatures required) {
			parent.features().check(required);
			this.features = notNull(required);
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
		public Builder queue(Queue.Family family) {
			return queues(family, 1);
		}

		/**
		 * Adds the specified number of queues of the given family to this device.
		 * @param family	Queue family
		 * @param num		Number of queues
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 */
		public Builder queues(Queue.Family family, int num) {
			return queues(family, Collections.nCopies(num, 1f));
		}

		/**
		 * Adds multiple queues of the given family with the specified work priorities to this device.
		 * @param family		Queue family
		 * @param priorities	Queue priorities
		 * @throws IllegalArgumentException if the given family is not a member of the parent physical device
		 * @throws IllegalArgumentException if the specified number of queues exceeds that supported by the family
		 * @throws IllegalArgumentException if the priorities array is empty or any value is not a valid 0..1 percentile
		 */
		public Builder queues(Queue.Family family, List<Float> priorities) {
			if(!parent.families().contains(family)) {
				throw new IllegalArgumentException("Invalid queue family for this device: " + family);
			}
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
			info.pEnabledFeatures = features.get();

			// Add required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Add queue descriptors
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = VulkanStructure.populate(VkDeviceQueueCreateInfo::new, queues, RequiredQueue::populate);

			// Allocate device
			final VulkanLibrary lib = parent.instance().library();
			final PointerByReference logical = lib.factory().pointer();
			check(lib.vkCreateDevice(parent.handle(), info, null, logical));

			// Create logical device
			return new LogicalDevice(logical.getValue(), parent, features, queues);
		}
	}
}
