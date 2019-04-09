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
		private final Command.Pool pool;

		private State state = State.UNDEFINED;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 */
		Buffer(Pointer handle, Command.Pool pool) {
			this.handle = notNull(handle);
			this.pool = notNull(pool);
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
			check(pool.lib.vkBeginCommandBuffer(handle, info));

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
			cmd.execute(pool.lib, handle);
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
			check(pool.lib.vkEndCommandBuffer(handle));
			state = State.READY;
		}

		/**
		 * Records a one-off command buffer for a single command.
		 * @param cmd Command
		 * @see VkCommandBufferUsageFlag#VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT
		 */
		public void once(Command cmd) {
			begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);
			add(cmd);
			end();
		}

		/**
		 * Resets this command buffer.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer has not been recorded
		 */
		public void reset(VkCommandBufferResetFlag... flags) {
			if(state != State.READY) throw new IllegalStateException("Buffer has not been recorded");
			final int mask = IntegerEnumeration.mask(flags);
			check(pool.lib.vkResetCommandBuffer(handle, mask));
			state = State.UNDEFINED;
		}

		/**
		 * Releases this buffer back to the pool.
		 */
		public synchronized void free() {
			pool.free(new Pointer[]{handle});
			pool.buffers.remove(this);
		}
	}

	/**
	 * A <i>command pool</i> allocates and maintains command buffers.
	 */
	class Pool extends LogicalDeviceHandle {
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
			final Vulkan vulkan = dev.parent().vulkan();
			final VulkanLibrary lib = vulkan.library();
			final PointerByReference pool = vulkan.factory().reference();
			check(lib.vkCreateCommandPool(dev.handle(), info, null, pool));

			// Create wrapper
			return new Pool(pool.getValue(), dev);
		}

		private final VulkanLibrary lib;
		private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

		/**
		 * Constructor.
		 * @param handle 			Command pool handle
		 * @param dev				Device
		 */
		protected Pool(Pointer handle, LogicalDevice dev) {
			super(handle, dev, lib -> lib::vkDestroyCommandPool);
			this.lib = dev.parent().vulkan().library();
		}

		/**
		 * @return Command buffer handles
		 */
		public Stream<Buffer> buffers() {
			return buffers.stream();
		}

		/**
		 * Allocates a number of command buffers from this pool.
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
			final Vulkan vulkan = dev.parent().vulkan();
			final VulkanLibrary lib = vulkan.library();
			final Pointer[] handles = vulkan.factory().pointers(num);
			check(lib.vkAllocateCommandBuffers(dev.handle(), info, handles));

			// Create buffer wrappers
			final var list = Arrays.stream(handles).map(ptr -> new Buffer(ptr, this)).collect(toList());
			buffers.addAll(list);
			return list;
		}

		/**
		 * Helper - Allocates a one-off command buffer to process the given command.
		 * @param cmd Command
		 * @return One-off command buffer
		 * @see Buffer#once(Command)
		 */
		public Buffer allocate(Command cmd) {
			final Buffer buffer = allocate(1, true).iterator().next();
			buffer.once(cmd);
			return buffer;
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
			final Pointer[] array = buffers.stream().map(Buffer::handle).toArray(Pointer[]::new);
			free(array);
			buffers.clear();
		}

		/**
		 * Frees command buffers.
		 */
		private void free(Pointer[] array) {
			lib.vkFreeCommandBuffers(dev.handle(), super.handle(), array.length, array);
		}

		@Override
		protected void cleanup() {
			buffers.clear();
		}
	}
}
