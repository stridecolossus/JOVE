package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.VkCommandBufferAllocateInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferBeginInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferLevel;
import org.sarge.jove.platform.vulkan.VkCommandBufferResetFlag;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateFlag;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkCommandPoolResetFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Queue;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>command</i> encapsulates a piece of work performed on a {@link Command.Buffer}.
 * @author Sarge
 */
@FunctionalInterface
public interface Command {
	/**
	 * Executes this command.
	 * @param lib		Vulkan library
	 * @param buffer 	Command buffer handle
	 */
	void execute(VulkanLibrary lib, Handle buffer);

	/**
	 * A <i>command buffer</i> is allocated by a {@link Pool} and used record and execute commands.
	 */
	class Buffer {
		/**
		 * Buffer state.
		 */
		private enum State {
			UNDEFINED,
			RECORDING,
			READY,
		}

		private final Handle handle;
		private final Pool pool;

		private State state = State.UNDEFINED;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 */
		private Buffer(Pointer handle, Pool pool) {
			this.handle = new Handle(handle);
			this.pool = notNull(pool);
		}

		/**
		 * @return Command buffer handle
		 */
		Handle handle() {
			return handle;
		}

		/**
		 * @return Parent command pool
		 */
		public Pool pool() {
			return pool;
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
		 * Records a one-time-submit command to this buffer.
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
			pool.free(new Handle[]{handle});
			pool.buffers.remove(this);
		}
	}

	/**
	 * A <i>command pool</i> allocates and maintains command buffers that are used to perform work on a given {@link Queue}.
	 */
	class Pool {
		/**
		 * Creates a command pool for the given queue.
		 * @param queue		Work queue
		 * @param flags		Flags
		 */
		public static Pool create(Queue queue, VkCommandPoolCreateFlag... flags) {
			// Init pool descriptor
			final VkCommandPoolCreateInfo info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = queue.family().index();
			info.flags = IntegerEnumeration.mask(Arrays.asList(flags));

			// Create pool
			final LogicalDevice dev = queue.device();
			final VulkanLibrary lib = dev.library();
			final PointerByReference pool = lib.factory().pointer();
			check(lib.vkCreateCommandPool(dev.handle(), info, null, pool));

			// Create pool
			return new Pool(pool.getValue(), queue);
		}

		private final Handle handle;
		private final Queue queue;
		private final VulkanLibrary lib;
		private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

		/**
		 * Constructor.
		 * @param handle 		Command pool handle
		 * @param queue			Work queue
		 */
		private Pool(Pointer handle, Queue queue) {
			this.handle = new Handle(handle);
			this.queue = notNull(queue);
			this.lib = queue.device().library();
		}

		/**
		 * @return Pool handle
		 */
		Handle handle() {
			return handle;
		}

		/**
		 * @return Work queue for this pool
		 */
		public Queue queue() {
			return queue;
		}

		/**
		 * @return Command buffers
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
			info.commandPool = handle;

			// Allocate buffers
			final LogicalDevice dev = queue.device();
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = lib.factory().pointers(num);
			check(lib.vkAllocateCommandBuffers(dev.handle(), info, handles));

			// Create buffers
			final var list = Arrays
					.stream(handles)
					.map(ptr -> new Buffer(ptr, this))
					.collect(toList());

			// Register buffers
			buffers.addAll(list);

			return list;
		}

		/**
		 * Allocates a number of primary command buffers from this pool.
		 * @param num Number of buffers to allocate
		 * @return Allocated buffers
		 */
		public List<Buffer> allocate(int num) {
			return allocate(num, true);
		}

		/**
		 * Allocates a primary command buffer from this pool.
		 * @return New command buffer
		 */
		public Buffer allocate() {
			return allocate(1).get(0);
		}

		/**
		 * Helper - Allocates a one-time primary buffer to with the given command.
		 * @param cmd Command
		 * @return New command buffer
		 * @see Buffer#once(Command)
		 */
		public Buffer allocate(Command cmd) {
			final Buffer buffer = allocate();
			buffer.once(cmd);
			return buffer;
		}

		/**
		 * Resets this command pool.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final int mask = IntegerEnumeration.mask(Arrays.asList(flags));
			check(lib.vkResetCommandPool(queue.device().handle(), handle, mask));
		}

		/**
		 * Frees <b>all</b> command buffers in this pool.
		 */
		public synchronized void free() {
			final Handle[] array = buffers.stream().map(Buffer::handle).toArray(Handle[]::new);
			free(array);
			buffers.clear();
		}

		/**
		 * Frees command buffers.
		 */
		private void free(Handle[] array) {
			lib.vkFreeCommandBuffers(queue.device().handle(), handle, array.length, array);
		}

		/**
		 * Destroys this command pool.
		 */
		public synchronized void destroy() {
			buffers.clear();
			lib.vkDestroyCommandPool(queue.device().handle(), handle, null);
		}
	}
}
