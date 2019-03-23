package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sarge.jove.platform.Handle;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictList;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice} that can be used to perform work.
 * @author Sarge
 */
public class LogicalDevice extends VulkanHandle {
	private final Map<QueueFamily, List<WorkQueue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param queues		Queues ordered by family
	 */
	LogicalDevice(VulkanHandle handle, Map<QueueFamily, List<WorkQueue>> queues) {
		super(handle);
		this.queues = Map.copyOf(queues);
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
	public Handle semaphore() {
		// Allocate semaphore
		final VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
		final Vulkan vulkan = Vulkan.instance();
		final VulkanLibrary lib = vulkan.library();
		final PointerByReference semaphore = vulkan.factory().reference();
		check(lib.vkCreateSemaphore(super.handle(), info, null, semaphore));

		// Create semaphore
		final Pointer handle = semaphore.getValue();
		return new VulkanHandle(handle, () -> lib.vkDestroySemaphore(super.handle(), handle, null));
	}

	/**
	 * Builder for a logical device.
	 */
	public static class Builder extends Feature.AbstractBuilder<Builder> {
		private final PhysicalDevice device;
		private final List<QueueFamily.Entry> entries = new StrictList<>();

		private VkPhysicalDeviceFeatures features;

		/**
		 * Constructor.
		 * @param device Parent physical device
		 */
		public Builder(PhysicalDevice device) {
			super(device.supported());
			this.device = notNull(device);
		}

		/**
		 * Sets the required features for this device.
		 * @param features Required features
		 */
		public Builder features(VkPhysicalDeviceFeatures features) {
			this.features = features;
			return this;
		}

		/**
		 * Adds a queue family used by this device.
		 * @param entry Queue family entry
		 * @throws IllegalArgumentException if the queue family does not belong to this device
		 */
		public Builder queue(QueueFamily.Entry entry) {
			if(!device.families().contains(entry.family())) throw new IllegalArgumentException("Invalid queue family for device: " + entry);
			entries.add(entry);
			return this;
		}

		/**
		 * Constructs this logical device.
		 * @return New logical device
		 */
		public LogicalDevice build() {
			// Create descriptor
			final VkDeviceCreateInfo info = new VkDeviceCreateInfo();
			info.pEnabledFeatures = features;

			// Add queue descriptors
			// TODO - convert to stream approach (could be helper on entry?) and use StructureHelper
			final VkDeviceQueueCreateInfo[] queueInfos = (VkDeviceQueueCreateInfo[]) new VkDeviceQueueCreateInfo().toArray(entries.size());
			for(int n = 0; n < queueInfos.length; ++n) {
				final VkDeviceQueueCreateInfo q = queueInfos[n];
				final QueueFamily.Entry entry = entries.get(0);
				final float[] priorities = entry.priorities();
				q.queueCount = priorities.length;
				q.queueFamilyIndex = entry.family().index();
				q.pQueuePriorities = StructureHelper.floats(priorities);
				q.write(); // <--- needed? TODO
			}
			info.queueCreateInfoCount = queueInfos.length;
			info.pQueueCreateInfos = queueInfos[0].getPointer();

			// Add required extensions
			final String[] extensions = super.extensions();
			info.ppEnabledExtensionNames = new StringArray(extensions);
			info.enabledExtensionCount = extensions.length;

			// Add validation layers
			final String[] layers = super.layers();
			info.ppEnabledLayerNames = new StringArray(layers);
			info.enabledLayerCount = layers.length;

			// Create device
			final Vulkan vulkan = Vulkan.instance();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference logical = vulkan.factory().reference();
			check(lib.vkCreateDevice(device.handle(), info, null, logical));

			// Retrieve queue handles
			final Map<QueueFamily, List<WorkQueue>> queues = new HashMap<>();
			for(QueueFamily.Entry entry : entries) {
				final List<WorkQueue> handles = queues.computeIfAbsent(entry.family(), ignored -> new ArrayList<>());
				for(int n = 0; n < entry.priorities().length; ++n) {
					// Lookup queue
					final PointerByReference ref = vulkan.factory().reference();
					lib.vkGetDeviceQueue(logical.getValue(), entry.family().index(), n, ref);

					// Create work-queue
					final WorkQueue queue = new WorkQueue(ref.getValue());
					handles.add(queue);
				}
			}

			// Create logical device
			final Pointer handle = logical.getValue();
			final Destructor destructor = () -> lib.vkDestroyDevice(handle, null);
			return new LogicalDevice(new VulkanHandle(handle, destructor), queues);
			// TODO - resource/track
		}
	}
}
