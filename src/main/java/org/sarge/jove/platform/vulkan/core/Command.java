package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.common.NativeObject.Handle;
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
	class Buffer implements NativeObject {
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
		@Override
		public Handle handle() {
			return handle;
		}
		// TODO - was package-private

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
			if(state != State.UNDEFINED) throw new IllegalStateException("Buffer is not ready for recording: " + this);

			// Start buffer
			final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
			info.flags = IntegerEnumeration.mask(flags);
			info.pInheritanceInfo = null;
			check(library().vkBeginCommandBuffer(handle, info));

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
			if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording: " + this);
			cmd.execute(library(), handle);
			return this;
		}

		/**
		 * Ends recording.
		 * @throws IllegalStateException if this buffer is not recording
		 * @throws IllegalArgumentException if no commands have been recorded
		 */
		public void end() {
			if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording: " + this);
			// TODO - count?
			check(library().vkEndCommandBuffer(handle));
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
			if(state != State.READY) throw new IllegalStateException("Buffer has not been recorded: " + this);
			final int mask = IntegerEnumeration.mask(flags);
			check(library().vkResetCommandBuffer(handle, mask));
			state = State.UNDEFINED;
		}

		/**
		 * Releases this buffer back to the pool.
		 */
		public synchronized void free() {
			pool.free(Set.of(this));
			pool.buffers.remove(this);
		}

		/**
		 * @return Vulkan API
		 */
		private VulkanLibrary library() {
			return pool.queue.device().library();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

	/**
	 * A <i>command pool</i> allocates and maintains command buffers that are used to perform work on a given {@link Queue}.
	 */
	class Pool extends AbstractVulkanObject {
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

		private final Queue queue;
		private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

		/**
		 * Constructor.
		 * @param handle 		Command pool handle
		 * @param queue			Work queue
		 */
		private Pool(Pointer handle, Queue queue) {
			super(handle, queue.device(), queue.device().library()::vkDestroyCommandPool);
			this.queue = notNull(queue);
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
			info.commandPool = super.handle();

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
		 * Resets this command pool.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final LogicalDevice dev = super.device();
			final int mask = IntegerEnumeration.mask(Arrays.asList(flags));
			check(dev.library().vkResetCommandPool(dev.handle(), this.handle(), mask));
		}

		/**
		 * Frees <b>all</b> command buffers in this pool.
		 */
		public synchronized void free() {
			free(buffers);
			buffers.clear();
		}

		/**
		 * Releases a set of command buffers back to this pool.
		 * @param buffers Buffers to release
		 */
		private void free(Collection<Buffer> buffers) {
			final LogicalDevice dev = super.device();
			dev.library().vkFreeCommandBuffers(dev.handle(), this.handle(), buffers.size(), Handle.toArray(buffers));
		}

		/**
		 * Destroys this command pool.
		 */
		@Override
		public synchronized void destroy() {
			buffers.clear();
			super.destroy();
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("handle", handle())
					.append("queue", queue)
					.append("buffers", buffers.size())
					.build();
		}
	}
}
