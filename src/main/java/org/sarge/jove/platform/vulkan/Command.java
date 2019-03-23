package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.lib.util.AbstractEqualsObject;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>command</i> encapsulates an atomic piece of work performed on a {@link Command.Buffer}.
 * @author Sarge
 */
public interface Command {
	/**
	 * Executes this command.
	 * @param lib		Vulkan API
	 * @param buffer 	Command buffer handle
	 */
	void execute(VulkanLibrary lib, Pointer buffer);

	/**
	 * A <i>command buffer</i> is allocated by a {@link Pool} and used to execute commands.
	 */
	class Buffer extends AbstractEqualsObject {
		/**
		 * Buffer state.
		 */
		private enum State {
			UNDEFINED,
			RECORDING,
			READY,
		}

		private final Pointer handle;
		private final VulkanLibrary lib;

		private State state = State.UNDEFINED;

		/**
		 * Constructor.
		 * @param handle Command buffer handle
		 */
		Buffer(Pointer handle, VulkanLibrary lib) {
			this.handle = notNull(handle);
			this.lib = notNull(lib);
		}

		/**
		 * @return Command buffer handle
		 */
		Pointer handle() {
			return handle;
		}

		/**
		 * @return Whether this command buffer is ready for submission (i.e. has been recorded)
		 */
		public boolean isReady() {
			return state == State.READY;
		}

		/**
		 * Starts command buffer recording.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer is not ready for recording
		 * @throws ServiceException if recording cannot be started
		 */
		public Buffer begin(VkCommandBufferUsageFlag... flags) {
			// Check buffer can be recorded
			if(state != State.UNDEFINED) throw new IllegalStateException("Buffer is not ready for recording");

			// Start buffer
			final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
			info.flags = IntegerEnumeration.mask(flags);
			info.pInheritanceInfo = null;
			check(lib.vkBeginCommandBuffer(handle, info));

			// Start recording
			state = State.RECORDING;
			return this;
		}

		/**
		 * Adds a command.
		 * @param cmd Command
		 * @throws IllegalStateException if this buffer is not recording
		 */
		public Buffer add(Command cmd) {
			if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording");
			cmd.execute(lib, handle);
			return this;
		}

		/**
		 * Ends recording.
		 * @throws IllegalStateException if this buffer is not recording
		 * @throws IllegalArgumentException if no commands have been recorded
		 */
		public void end() {
			if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording");
			// TODO - count?
			check(lib.vkEndCommandBuffer(handle));
			state = State.READY;
		}

		/**
		 * Resets this command buffer.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer has not been recorded
		 */
		public void reset(VkCommandBufferResetFlag... flags) {
			if(state != State.READY) throw new IllegalStateException("Buffer has not been recorded");
			final int mask = IntegerEnumeration.mask(flags);
			check(lib.vkResetCommandBuffer(handle, mask));
			state = State.UNDEFINED;
		}
	}

	/**
	 * A <i>command pool</i> allocates and maintains command buffers.
	 */
	class Pool extends VulkanHandle {
		/**
		 * Constructor.
		 * @param dev			Device
		 * @param family		Queue family
		 * @param flags			Flags
		 */
		public static Pool create(LogicalDevice dev, QueueFamily family, VkCommandPoolCreateFlag... flags) {
			// Init pool descriptor
			final VkCommandPoolCreateInfo info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = family.index(); // TODO - check correct family for device?
			info.flags = IntegerEnumeration.mask(Arrays.asList(flags));

			// Create pool
			final Vulkan vulkan = Vulkan.instance();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference pool = vulkan.factory().reference();
			check(lib.vkCreateCommandPool(dev.handle(), info, null, pool));

			// Create wrapper
			final Pointer handle = pool.getValue();
			final Destructor destructor = () -> lib.vkDestroyCommandPool(dev.handle(), handle, null);
			return new Pool(handle, destructor, dev, lib);
			// TODO - need family in pool?
		}

		private final LogicalDevice dev;
		private final VulkanLibraryCommandBuffer lib;
		private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

		/**
		 * Constructor.
		 * @param handle 			Command pool native handle
		 * @param destructor		Destructor
		 * @param dev				Device
		 * @param lib				Vulkan API
		 */
		protected Pool(Pointer handle, Destructor destructor, LogicalDevice dev, VulkanLibrary lib) {
			super(handle, destructor);
			this.dev = notNull(dev);
			this.lib = notNull(lib);
		}

		/**
		 * @return Command buffer handles
		 */
		public Stream<Buffer> buffers() {
			return buffers.stream();
		}

		/**
		 * Allocates a number of command buffers in this pool.
		 * @param num			Number of buffers to allocate
		 * @param primary		Whether primary or secondary
		 * @return Allocated buffers
		 */
		public List<Buffer> allocate(int num, boolean primary) {
			// Init descriptor
			final VkCommandBufferAllocateInfo info = new VkCommandBufferAllocateInfo();
			info.level = primary ? VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
			info.commandBufferCount = num;
			info.commandPool = super.handle();

			// Allocate buffers
			final Vulkan vulkan = Vulkan.instance();
			final VulkanLibrary lib = vulkan.library();
			final Pointer[] handles = vulkan.factory().pointers(num);
			check(lib.vkAllocateCommandBuffers(dev.handle(), info, handles));

			// Create buffer wrappers
			final var list = Arrays.stream(handles).map(ptr -> new Buffer(ptr, lib)).collect(toList());
			buffers.addAll(list);
			return list;
		}

		/**
		 * Resets this command pool.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final int mask = IntegerEnumeration.mask(Arrays.asList(flags));
			check(lib.vkResetCommandPool(dev.handle(), super.handle(), mask));
		}

		/**
		 * Frees <b>all</b> command buffers in this pool.
		 * TODO - need to be able to free subset?
		 */
		public synchronized void free() {
			if(!buffers.isEmpty()) {
				final Pointer[] array = buffers.stream().map(Buffer::handle).toArray(Pointer[]::new);
				lib.vkFreeCommandBuffers(dev.handle(), super.handle(), buffers.size(), array);
				buffers.clear();
			}
		}

		@Override
		protected void cleanup() {
			buffers.clear();
		}
	}
}

