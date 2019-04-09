package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.collection.StrictList;
import org.sarge.lib.collection.StrictSet;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.StringArray;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>logical device</i> is an instance of a {@link PhysicalDevice} that can be used to perform work.
 * @author Sarge
 */
public class LogicalDevice extends PointerHandle {
	/**
	 * Work submitted to a {@link Queue}.
	 */
	public interface Work {
		/**
		 * Submits this work to the queue.
		 */
		void submit();

		/**
		 * Builder for work to be submitted to this queue.
		 */
		public static class Builder {
			private final Queue queue;
			private final Collection<Pointer> buffers = new ArrayList<>();
			private final Collection<Integer> waitStages = new StrictSet<>();
			private final List<Pointer> waitSemaphores = new ArrayList<>();
			private final List<Pointer> signalSemaphores = new ArrayList<>();
			private Pointer fence;

			/**
			 * Constructor.
			 * @param queue Work queue
			 */
			protected Builder(Queue queue) {
				this.queue = notNull(queue);
			}

			/**
			 * Adds a command buffer to submit.
			 * @param buffer Buffer to submit
			 * @throws IllegalArgumentException if the command buffer has not been recorded
			 */
			public Builder add(Command.Buffer buffer) {
				if(!buffer.isReady()) throw new IllegalArgumentException("Command buffer has not been recorded: " + buffer);
				buffers.add(buffer.handle());
				return this;
			}

			/**
			 * Adds a pipeline wait stage.
			 * @param stage Wait stage
			 * @throws IllegalArgumentException for a duplicate wait state
			 */
			public Builder wait(VkPipelineStageFlag stage) {
				waitStages.add(stage.value());
				return this;
			}

			/**
			 * Adds a semaphore to wait on.
			 * @param semaphore Wait semaphore
			 */
			public Builder wait(PointerHandle semaphore) {
				waitSemaphores.add(semaphore.handle());
				return this;
			}

			/**
			 * Adds a semaphore to be signalled after execution.
			 * @param semaphore Signal semaphore
			 */
			public Builder signal(PointerHandle semaphore) {
				signalSemaphores.add(semaphore.handle());
				return this;
			}

			/**
			 * Sets the fence for this work.
			 * @param fence Fence
			 */
			public Builder fence(Fence fence) {
				this.fence = fence.handle();
				return this;
			}

			/**
			 * Constructs this work.
			 * @return New work
			 */
			public Work build() {
				// Init work descriptor
				final VkSubmitInfo info = new VkSubmitInfo();

				// Add wait stages
				// TODO - a lot if buggering about
				info.pWaitDstStageMask = StructureHelper.integers(ArrayUtils.toPrimitive(waitStages.toArray(Integer[]::new)));

				// Add command buffers to submit
				if(buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");
				info.commandBufferCount = buffers.size();
				info.pCommandBuffers = StructureHelper.pointers(buffers);

				// Add wait semaphores
				info.waitSemaphoreCount = waitSemaphores.size();
				info.pWaitSemaphores = StructureHelper.pointers(waitSemaphores);

				// Add signal semaphores
				info.signalSemaphoreCount = signalSemaphores.size();
				info.pSignalSemaphores = StructureHelper.pointers(signalSemaphores);

				// Create work
				return () -> {
					final VulkanLibraryLogicalDevice lib = queue.device().parent().vulkan().library();
					check(lib.vkQueueSubmit(queue.handle(), 1, new VkSubmitInfo[]{info}, fence));
				};
			}
		}
	}

	/**
	 * Work queue for this device.
	 */
	public class Queue extends Handle<Pointer> {
		private final QueueFamily family;

		/**
		 * Constructor.
		 * @param handle		Queue handle
		 * @param family		Queue family
		 */
		private Queue(Pointer handle, QueueFamily family) {
			super(handle);
			this.family = notNull(family);
		}

		/**
		 * @return Queue family
		 */
		public QueueFamily family() {
			return family;
		}

		/**
		 * @return Logical device owning this queue
		 */
		public LogicalDevice device() {
			return LogicalDevice.this;
		}

		/**
		 * @return New work builder
		 */
		public Work.Builder work() {
			return new Work.Builder(this);
		}

		/**
		 * Helper - Submits a command to this queue.
		 * @param buffer Command buffer
		 */
		public void submit(Command.Buffer buffer) {
			new Work.Builder(this).add(buffer).build().submit();
		}

		/**
		 * Waits for this queue to become idle.
		 */
		public void waitIdle() {
			final VulkanLibrary lib = device().parent().vulkan().library();
			check(lib.vkQueueWaitIdle(super.handle()));
		}

		@Override
		public final synchronized void destroy() {
			throw new UnsupportedOperationException();
		}
	}

	private final PhysicalDevice parent;
	private final Map<QueueFamily, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle 		Device handle
	 * @param parent		Parent physical device
	 * @param queues		Queue handles ordered by family
	 */
	protected LogicalDevice(Pointer handle, PhysicalDevice parent, Map<QueueFamily, List<Pointer>> queues) {
		super(handle);
		this.parent = notNull(parent);
		this.queues = build(queues);
	}

	/**
	 * Builds the work queues for this device.
	 */
	private Map<QueueFamily, List<Queue>> build(Map<QueueFamily, List<Pointer>> queues) {
		final Map<QueueFamily, List<Queue>> map = new HashMap<>();
		for(QueueFamily family : queues.keySet()) {
			final var list = queues.get(family).stream().map(handle -> new Queue(handle, family)).collect(toList());
			map.put(family, list);
		}
		return Map.copyOf(map);
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	/**
	 * Retrieves all queues.
	 * @return All queues for this device ordered by family
	 */
	public Map<QueueFamily, List<Queue>> queues() {
		return queues;
	}

	/**
	 * Retrieve all queues of the given family.
	 * @param family Family
	 * @return Queues for this device in the given family
	 * @throws IllegalArgumentException if there are no queues in the given family
	 */
	public List<Queue> queues(QueueFamily family) {
		final var results = queues.get(family);
		if(results == null) throw new IllegalArgumentException("No queues for family: " + family);
		return results;
	}

	/**
	 * Retrieves a queue belonging to this device.
	 * @param family	Queue family
	 * @param index		Index
	 * @return Queue
	 * @throws IllegalArgumentException if there are no queues in the given family
	 */
	public Queue queue(QueueFamily family, int index) {
		final var list = queues(family);
		Check.range(index, 0, list.size());
		return list.get(index);
	}

	/**
	 * Retrieves the <b>first</b> queue of the given family belonging to this device.
	 * @param family Queue family
	 * @return Queue
	 * @throws IllegalArgumentException if there are no queues in the given family
	 */
	public Queue queue(QueueFamily family) {
		return queues(family).get(0);
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

			// Allocate device
			final Vulkan vulkan = parent.vulkan();
			final VulkanLibraryLogicalDevice lib = vulkan.library();
			final PointerByReference logical = vulkan.factory().reference();
			check(lib.vkCreateDevice(parent.handle(), info, null, logical));

			// Retrieve queues
			final Map<QueueFamily, List<Pointer>> map = new HashMap<>();
			for(VkDeviceQueueCreateInfo queueInfo : queues) {
				final QueueFamily family = parent.families().get(queueInfo.queueFamilyIndex);
				for(int n = 0; n < queueInfo.queueCount; ++n) {
					final PointerByReference ref = vulkan.factory().reference();
					lib.vkGetDeviceQueue(logical.getValue(), family.index(), n, ref);
					map.computeIfAbsent(family, ignored -> new ArrayList<>()).add(ref.getValue());
				}
			}

			// Create logical device
			// TODO - resource/track
			return new LogicalDevice(logical.getValue(), parent, map);
		}
	}
}
