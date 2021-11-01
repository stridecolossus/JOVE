package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.groupingBy;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkDeviceCreateInfo;
import org.sarge.jove.platform.vulkan.VkDeviceQueueCreateInfo;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Percentile;

import com.sun.jna.Memory;
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
		private final PhysicalDevice parent;
		private final Set<String> extensions = new HashSet<>();
		private final Set<String> layers = new HashSet<>();
		private final Map<Family, List<Percentile>> queues = new HashMap<>();
		private VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();

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
			if(priorities.size() > family.count()) {
				throw new IllegalArgumentException(String.format("Number of queues exceeds family: avaiable=%d requested=%d", priorities.size(), family.count()));
			}
//			if(queues.containsKey(family)) {
//				throw new IllegalArgumentException("Duplicate required queue family: " + family);
//			}

			// Register required queue
			queues.put(family, List.copyOf(priorities));
			return this;
		}

		/**
		 * Populates a required queue descriptor.
		 */
		public static void populate(Entry<Family, List<Percentile>> queue, VkDeviceQueueCreateInfo info) {
			// Convert priorities to array
			final Float[] floats = queue.getValue().stream().map(Percentile::floatValue).toArray(Float[]::new);
			final float[] array = ArrayUtils.toPrimitive(floats);

			// Allocate contiguous memory block for the priorities array
			final Memory mem = new Memory(array.length * Float.BYTES);
			mem.write(0, array, 0, array.length);
			// TODO - why is this required if using primitives?

			// Populate queue descriptor
			info.queueCount = array.length;
			info.queueFamilyIndex = queue.getKey().index();
			info.pQueuePriorities = mem;
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
			info.pEnabledFeatures = features;

			// Add required extensions
			info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
			info.enabledExtensionCount = extensions.size();

			// Add validation layers
			info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
			info.enabledLayerCount = layers.size();

			// Add queue descriptors
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = StructureHelper.first(queues.entrySet(), VkDeviceQueueCreateInfo::new, Builder::populate);

			// Allocate device
			final VulkanLibrary lib = parent.instance().library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateDevice(parent, info, null, handle));

			// Retrieve required queues
			class RequiredQueue {
				private final Family family;
				private final int count;

				private RequiredQueue(Entry<Family, List<Percentile>> entry) {
					this.family = entry.getKey();
					this.count = entry.getValue().size();
				}

				/**
				 * Retrieves the required work queues.
				 * @return Work queues
				 */
				private Stream<Queue> stream() {
					return IntStream.range(0, count).mapToObj(this::create);
				}

				/**
				 * Retrieves a work queue.
				 * @param index Queue index
				 * @return New queue
				 */
				private Queue create(int index) {
					final PointerByReference ref = lib.factory().pointer();
					lib.vkGetDeviceQueue(handle.getValue(), family.index(), index, ref);
					return new Queue(new Handle(ref.getValue()), family);
				}
			}
			final Map<Family, List<Queue>> map = queues
					.entrySet()
					.stream()
					.map(RequiredQueue::new)
					.flatMap(RequiredQueue::stream)
					.collect(groupingBy(Queue::family));

			// Create logical device
			return new LogicalDevice(handle.getValue(), parent, map);
		}
	}
}
