package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictList;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice} that can be used to perform work.
 * @author Sarge
 */
public class LogicalDevice extends PointerHandle {
	private final PhysicalDevice parent;
	private final Map<QueueFamily, List<WorkQueue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent		Parent physical device
	 * @param queues		Queues ordered by family
	 */
	LogicalDevice(Pointer handle, PhysicalDevice parent, Map<QueueFamily, List<WorkQueue>> queues) {
		super(handle);
		this.parent = notNull(parent);
		this.queues = Map.copyOf(queues);
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	/**
	 * @return Queues for this device ordered by family
	 */
	public Map<QueueFamily, List<WorkQueue>> queues() {
		return queues;
	}

	/**
	 * @param family Family
	 * @return Queues for this device in the given family
	 * @throws IllegalArgumentException if there are no queues in the given family
	 */
	public List<WorkQueue> queues(QueueFamily family) {
		final List<WorkQueue> handles = queues.get(family);
		if(handles == null) throw new IllegalArgumentException("No queues for family: " + family);
		return handles;
	}

	/**
	 * Retrieves a queue belonging to this device.
	 * @param family	Queue family
	 * @param index		Index
	 * @return Queue
	 * @throws IllegalArgumentException if there are no queues in the given family
	 * @throws IndexOutOfBoundsException if the given index is out-of-range
	 */
	public WorkQueue queue(QueueFamily family, int index) {
		return queues(family).get(index);
	}

	/**
	 * Creates a semaphore.
	 * @return New semaphore
	 */
	public PointerHandle semaphore() {
		// Allocate semaphore
		final VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
		final Vulkan vulkan = parent.vulkan();
		final VulkanLibrarySynchronize lib = vulkan.library();
		final PointerByReference semaphore = vulkan.factory().reference();
		check(lib.vkCreateSemaphore(super.handle(), info, null, semaphore));

		// Create semaphore
		return new LogicalDeviceHandle(semaphore.getValue(), LogicalDevice.this, ignored -> lib::vkDestroySemaphore);
	}

	/**
	 * Allocates device memory.
	 * @param reqs		Memory requirements
	 * @param flags		Flags
	 * @return Memory handle
	 * @throws ServiceException if the memory cannot be allocated
	 */
	public Pointer allocate(VkMemoryRequirements reqs, Set<VkMemoryPropertyFlag> flags) {
		// Find memory type
		final int type = parent.findMemoryType(flags);

		// Init memory descriptor
		final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
        info.allocationSize = reqs.size;
        info.memoryTypeIndex = type;

        // Allocate memory
        final Vulkan vulkan = parent.vulkan();
        final PointerByReference mem = vulkan.factory().reference();
        check(vulkan.library().vkAllocateMemory(super.handle(), info, null, mem));

        // Get memory handle
        return mem.getValue();
	}

	@Override
	public synchronized void destroy() {
		final VulkanLibrary lib = parent.vulkan().library();
		lib.vkDestroyDevice(super.handle(), null);
		super.destroy();
	}

	/**
	 * Builder for a logical device.
	 */
	public static class Builder extends Feature.AbstractBuilder<Builder> {
		private final PhysicalDevice parent;
		private final List<VkDeviceQueueCreateInfo> queues = new StrictList<>();

		private VkPhysicalDeviceFeatures features;

		/**
		 * Constructor.
		 * @param parent Parent physical device
		 */
		public Builder(PhysicalDevice parent) {
			super(parent.supported());
			this.parent = notNull(parent);
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
		 * Adds a single queue of the given family to this device (with the default priority of <b>one</b>).
		 * @param family Queue family
		 * @throws IllegalArgumentException if the queue family does not belong to the physical device
		 */
		public Builder queue(QueueFamily family) {
			// TODO - dedup, need to work out what the requirements for this actually are
			if(queues.stream().noneMatch(info -> info.queueFamilyIndex == family.index())) {
				queue(family, 1);
			}
			return this;
		}

		/**
		 * Adds multiple queues of the given family to this device (all with the default priority of <b>one</b>).
		 * @param family 	Queue family
		 * @param num		Number of queues
		 * @throws IllegalArgumentException if the queue family does not belong to the physical device
		 * @throws IllegalArgumentException if the number of queues is zero or exceeds the number available in the given family
		 */
		public Builder queue(QueueFamily family, int num) {
			final float[] priorities = new float[num];
			Arrays.fill(priorities, 1);
			return queue(family, priorities);
		}

		/**
		 * Adds multiple queues of the given family to this device with the specified priorities.
		 * @param family 			Queue family
		 * @param priorities		Queue priorities
		 * @throws IllegalArgumentException if the queue family does not belong to the physical device
		 * @throws IllegalArgumentException if any priority is not in the range 0..1
		 * @throws IllegalArgumentException if the number of queues is zero or exceeds the number available in the given family
		 */
		public Builder queue(QueueFamily family, float[] priorities) {
			// Validate
			if(!parent.families().contains(family)) {
				throw new IllegalArgumentException("Invalid queue family for device: " + family);
			}
			Check.notEmpty(priorities);
			for(float f : priorities) {
				Check.range(f, 0, 1);
			}
			if(priorities.length > family.count()) {
				throw new IllegalArgumentException(String.format("Number of requested queues exceeds number available in family: num=%d family=%s", priorities.length, family));
			}

			// Add queue descriptor
			final VkDeviceQueueCreateInfo info = new VkDeviceQueueCreateInfo();
			info.queueCount = priorities.length;
			info.queueFamilyIndex = family.index();
			info.pQueuePriorities = StructureHelper.floats(priorities);
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
			final VkDeviceCreateInfo info = new VkDeviceCreateInfo();

			// Add required features
			final var unsupported = parent.enumerateUnsupportedFeatures(features);
			if(!unsupported.isEmpty()) throw new ServiceException("Logical device requires features that are not supported byy the physical device: " + unsupported);
			info.pEnabledFeatures = features;

			// Add queue descriptors
			info.queueCreateInfoCount = queues.size();
			info.pQueueCreateInfos = StructureHelper.structures(queues);

			// Add required extensions
			final String[] extensions = super.extensions();
			info.ppEnabledExtensionNames = new StringArray(extensions);
			info.enabledExtensionCount = extensions.length;

			// Add validation layers
			final String[] layers = super.layers();
			info.ppEnabledLayerNames = new StringArray(layers);
			info.enabledLayerCount = layers.length;

			// Create device
			final Vulkan vulkan = parent.vulkan();
			final VulkanLibraryLogicalDevice lib = vulkan.library();
			final PointerByReference logical = vulkan.factory().reference();
			check(lib.vkCreateDevice(parent.handle(), info, null, logical));

			// Retrieve queue handles
			final Map<QueueFamily, List<WorkQueue>> map = new HashMap<>();
			for(VkDeviceQueueCreateInfo q : queues) {
				// Create entry for each family
				final QueueFamily family = parent.families().get(q.queueFamilyIndex);
				final List<WorkQueue> handles = map.computeIfAbsent(family, ignored -> new ArrayList<>());

				// Retrieve queue handles for this family
				for(int n = 0; n < q.queueCount; ++n) {
					// Retrieve queue handle
					final PointerByReference ref = vulkan.factory().reference();
					lib.vkGetDeviceQueue(logical.getValue(), q.queueFamilyIndex, n, ref);

					// Create work-queue
					final WorkQueue queue = new WorkQueue(ref.getValue(), parent.vulkan().library());
					handles.add(queue);
				}
			}

			// Create logical device
			return new LogicalDevice(logical.getValue(), parent, map);
			// TODO - resource/track
		}
	}
}
