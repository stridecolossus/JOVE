package org.sarge.jove.platform.vulkan.common;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkCommandBufferAllocateInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferBeginInfo;
import org.sarge.jove.platform.vulkan.VkCommandBufferLevel;
import org.sarge.jove.platform.vulkan.VkCommandBufferResetFlag;
import org.sarge.jove.platform.vulkan.VkCommandBufferUsageFlag;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateFlag;
import org.sarge.jove.platform.vulkan.VkCommandPoolCreateInfo;
import org.sarge.jove.platform.vulkan.VkCommandPoolResetFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.core.Work;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>command</i> defines a piece of work to be performed on the hardware.
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
	 * Helper - Submits this as a <i>one time</i> command to the given pool and waits for completion.
	 * @param pool Command pool
	 * @see Work#submit(Command, Command.Pool)
	 */
	default void submit(Command.Pool pool) {
		Work.submit(this, pool);
	}

	/**
	 * A <i>command buffer</i> is allocated by a {@link Pool} and used to record commands.
	 */
	class Buffer implements NativeObject {
		/**
		 * Buffer state.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#commandbuffers-lifecycle">lifecycle</a>
		 */
		private enum State {
			INITIAL,
			RECORDING,
			EXECUTABLE,
		}

		private final Handle handle;
		private final Pool pool;

		private State state = State.INITIAL;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 */
		private Buffer(Pointer handle, Pool pool) {
			this.handle = new Handle(handle);
			this.pool = notNull(pool);
		}

		@Override
		public Handle handle() {
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
			return state == State.EXECUTABLE;
		}

		/**
		 * Starts command buffer recording.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer is not ready for recording
		 */
		public Buffer begin(VkCommandBufferUsageFlag... flags) {
			// Check buffer can be recorded
			if(state != State.INITIAL) throw new IllegalStateException("Buffer is not ready for recording: " + this);

			// Init descriptor
			final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
			info.flags = IntegerEnumeration.mask(flags);
			info.pInheritanceInfo = null;

			// Start buffer recording
			final VulkanLibrary lib = pool.device().library();
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
			if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording: " + this);
			cmd.execute(pool.device().library(), handle);
			return this;
		}

		/**
		 * Ends recording.
		 * @throws IllegalStateException if this buffer is not recording
		 * @throws IllegalArgumentException if no commands have been recorded
		 */
		public Buffer end() {
			if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording: " + this);
			// TODO - count?
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkEndCommandBuffer(handle));
			state = State.EXECUTABLE;
			return this;
		}

		/**
		 * Resets this command buffer.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer has not been recorded
		 */
		public void reset(VkCommandBufferResetFlag... flags) {
			if(state != State.EXECUTABLE) throw new IllegalStateException("Buffer has not been recorded: " + this);
			final int mask = IntegerEnumeration.mask(flags);
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkResetCommandBuffer(handle, mask));
			state = State.INITIAL;
		}

		/**
		 * Releases this buffer back to the pool.
		 */
		public synchronized void free() {
			pool.free(Set.of(this));
			pool.buffers.remove(this);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append("handle", handle)
					.append("state", state)
					.append("pool", pool)
					.build();
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
			final var info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = queue.family().index();
			info.flags = IntegerEnumeration.mask(flags);

			// Create pool
			final DeviceContext dev = queue.device();
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
			super(handle, queue.device());
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
			info.commandBufferCount = oneOrMore(num);
			info.commandPool = this.handle();

			// Allocate buffers
			final DeviceContext dev = queue.device();
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = lib.factory().array(num);
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
			final int mask = IntegerEnumeration.mask(flags);
			final DeviceContext dev = super.device();
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
			final DeviceContext dev = super.device();
			dev.library().vkFreeCommandBuffers(dev.handle(), this.handle(), buffers.size(), Handle.toArray(buffers));
		}

		@Override
		protected Destructor destructor(VulkanLibrary lib) {
			return lib::vkDestroyCommandPool;
		}

		@Override
		protected void release() {
			// Note buffers are released automatically by the pool
			buffers.clear();
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
