package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.oneOrMore;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Work.Batch;
import org.sarge.jove.util.IntegerEnumeration;

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
	 * @param buffer 	Command buffer
	 */
	void execute(VulkanLibrary lib, Buffer buffer);

	/**
	 * Helper - Submits this as a <i>one time</i> command to the given pool and waits for completion.
	 * @param pool Command pool
	 * @return New command buffer
	 * @see Work#submit(Command, Pool, Fence)
	 */
	default Buffer submitAndWait(Pool pool) {
		// Submit work
		final Fence fence = Fence.create(pool.device());
		final Buffer buffer = Work.submit(this, pool, fence);

		// Wait for completion
		fence.waitReady();

		// Release resources
		fence.destroy();
		buffer.free();

		return buffer;
	}

	/**
	 * A <i>command buffer</i> is allocated by a {@link Pool} and used to record a command sequence.
	 */
	class Buffer implements NativeObject {
		/**
		 * Buffer state.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#commandbuffers-lifecycle">lifecycle</a>
		 */
		private enum State {
			INITIAL,
			RECORDING,
			EXECUTABLE
		}

		private final Handle handle;
		private final Pool pool;

		private State state = State.INITIAL;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 */
		protected Buffer(Pointer handle, Pool pool) {
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
		 * @return Whether this is a primary or secondary command buffer
		 */
		@SuppressWarnings("static-method")
		public boolean isPrimary() {
			return true;
		}

		/**
		 * @return Whether this command buffer is ready for submission (i.e. has been recorded)
		 */
		public boolean isReady() {
			return state == State.EXECUTABLE;
		}

		/**
		 * Validates the state of this buffer.
		 * @param expected Expected state
		 * @throws IllegalStateException if this buffer is not in the expected state
		 */
		private void validate(State expected) {
			if(state != expected) {
				throw new IllegalStateException(String.format("Invalid buffer state: expected=%s actual=%s", expected, state));
			}
		}

		/**
		 * Starts command buffer recording.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer is not ready for recording
		 */
		public Buffer begin(VkCommandBufferUsage... flags) {
			// Check buffer can be recorded
			validate(State.INITIAL);

			// Init descriptor
			final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
			info.flags = IntegerEnumeration.mask(flags);
			info.pInheritanceInfo = null;

			// Start buffer recording
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkBeginCommandBuffer(this, info));

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
			validate(State.RECORDING);
			cmd.execute(pool.device().library(), this);
			return this;
		}

		/**
		 * Adds secondary buffers.
		 * @param secondary Secondary buffers
		 */
		public Buffer add(List<SecondaryBuffer> secondary) {
			validate(State.RECORDING);
			final Pointer array = NativeObject.array(secondary);
			final VulkanLibrary lib = pool.device().library();
			lib.vkCmdExecuteCommands(this, secondary.size(), array);
			return this;
		}

		/**
		 * Ends recording.
		 * @throws IllegalStateException if this buffer is not recording
		 * @throws IllegalArgumentException if no commands have been recorded
		 */
		public Buffer end() {
			validate(State.RECORDING);
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkEndCommandBuffer(this));
			state = State.EXECUTABLE;
			return this;
		}

		/**
		 * Resets this command buffer.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer has not been recorded
		 */
		public void reset(VkCommandBufferResetFlag... flags) {
			validate(State.EXECUTABLE);
			// TODO - check pool has flag
			final int mask = IntegerEnumeration.mask(flags);
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkResetCommandBuffer(this, mask));
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
					.append("primary", isPrimary())
					.build();
		}
	}

	/**
	 * Secondary command buffer.
	 */
	class SecondaryBuffer extends Buffer {
		private SecondaryBuffer(Pointer handle, Pool pool) {
			super(handle, pool);
		}

		@Override
		public final boolean isPrimary() {
			return false;
		}

		@Override
		public Buffer add(List<SecondaryBuffer> secondary) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * A <i>command pool</i> allocates command buffers that are used to perform work on a given {@link Queue}.
	 */
	class Pool extends AbstractVulkanObject {
		/**
		 * Creates a command pool for the given queue.
		 * @param dev		Logical device
		 * @param queue		Work queue
		 * @param flags		Flags
		 */
		public static Pool create(DeviceContext dev, Queue queue, VkCommandPoolCreateFlag... flags) {
			// Init pool descriptor
			final var info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = queue.family().index();
			info.flags = IntegerEnumeration.mask(flags);

			// Create pool
			final VulkanLibrary lib = dev.library();
			final PointerByReference pool = dev.factory().pointer();
			check(lib.vkCreateCommandPool(dev, info, null, pool));

			// Create pool
			return new Pool(pool.getValue(), dev, queue);
		}

		private final Queue queue;
		private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

		/**
		 * Constructor.
		 * @param handle 		Command pool handle
		 * @param dev			Logical device
		 * @param queue			Work queue
		 */
		private Pool(Pointer handle, DeviceContext dev, Queue queue) {
			super(handle, dev);
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
		// TODO - will we ever need/use this? better to leave to specific applications, caches, etc?

		/**
		 * Allocates a number of command buffers from this pool.
		 * @param num			Number of buffers to allocate
		 * @param primary		Whether primary or secondary
		 * @param ctor			Constructor
		 * @return Allocated buffers
		 */
		private <T extends Buffer> List<T> allocate(int num, boolean primary, BiFunction<Pointer, Pool, T> ctor) {
			// Init descriptor
			final VkCommandBufferAllocateInfo info = new VkCommandBufferAllocateInfo();
			info.level = primary ? VkCommandBufferLevel.PRIMARY : VkCommandBufferLevel.SECONDARY;
			info.commandBufferCount = oneOrMore(num);
			info.commandPool = this.handle();

			// Allocate buffers
			final DeviceContext dev = super.device();
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = new Pointer[num];
			check(lib.vkAllocateCommandBuffers(dev, info, handles));

			// Create buffers
			final List<T> list = Arrays
					.stream(handles)
					.map(ptr -> ctor.apply(ptr, this))
					.collect(toList());

			// Register buffers
			buffers.addAll(list);

			return list;
		}

		/**
		 * Allocates a single primary command buffer from this pool.
		 * @return New command buffer
		 */
		public Buffer allocate() {
			return allocate(1).get(0);
		}

		/**
		 * Allocates a number of primary command buffers from this pool.
		 * @param num Number of buffers to allocate
		 * @return New command buffers
		 */
		public List<Buffer> allocate(int num) {
			return allocate(num, true, Buffer::new);
		}

		/**
		 * Allocates a number of secondary command buffers from this pool.
		 * @param num Number of secondary buffers to allocate
		 * @return New secondary buffers
		 */
		public List<SecondaryBuffer> secondary(int num) {
			return allocate(num, false, SecondaryBuffer::new);
		}

		/**
		 * Resets this command pool.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final int mask = IntegerEnumeration.mask(flags);
			final DeviceContext dev = super.device();
			check(dev.library().vkResetCommandPool(dev, this, mask));
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
			dev.library().vkFreeCommandBuffers(dev, this, buffers.size(), NativeObject.array(buffers));
		}

		/**
		 * Submits a batch of work submissions to this pool.
		 * @param batch Work batch to submit
		 * @param fence Optional fence
		 */
		public void submit(Batch batch, Fence fence) {
			final VulkanLibrary lib = super.device().library();
			final VkSubmitInfo[] array = batch.submit();
			check(lib.vkQueueSubmit(queue, array.length, array, fence));
		}

		/**
		 * Helper - Waits for the work queue to become idle.
		 * @see Queue#waitIdle(VulkanLibrary)
		 */
		public void waitIdle() {
			final VulkanLibrary lib = super.device().library();
			queue.waitIdle(lib);
		}

		@Override
		protected Destructor<Pool> destructor(VulkanLibrary lib) {
			return lib::vkDestroyCommandPool;
		}

		@Override
		protected void release() {
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

	/**
	 * Vulkan command pool and buffer API.
	 */
	interface Library {
		/**
		 * Creates a command pool.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pCommandPool		Returned command pool
		 * @return Result code
		 */
		int vkCreateCommandPool(DeviceContext device, VkCommandPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pCommandPool);

		/**
		 * Destroys a command pool (and its buffers).
		 * @param device			Logical device
		 * @param commandPool		Command pool
		 * @param pAllocator		Allocator
		 */
		void vkDestroyCommandPool(DeviceContext device, Pool commandPool, Pointer pAllocator);

		/**
		 * Resets a command pool.
		 * @param device			Logical device
		 * @param commandPool		Command pool
		 * @param flags				Flags
		 * @return Result code
		 */
		int vkResetCommandPool(DeviceContext device, Pool commandPool, int flags);

		/**
		 * Allocates a number of command buffers.
		 * @param device			Logical device
		 * @param pAllocateInfo		Descriptor
		 * @param pCommandBuffers	Returned buffer handles
		 * @return Result code
		 */
		int vkAllocateCommandBuffers(DeviceContext device, VkCommandBufferAllocateInfo pAllocateInfo, Pointer[] pCommandBuffers);

		/**
		 * Releases a set of command buffers back to the pool.
		 * @param device				Logical device
		 * @param commandPool			Command pool
		 * @param commandBufferCount	Number of buffers
		 * @param pCommandBuffers		Buffer handles
		 */
		void vkFreeCommandBuffers(DeviceContext device, Pool commandPool, int commandBufferCount, Pointer pCommandBuffers);

		/**
		 * Starts recording.
		 * @param commandBuffer			Command buffer
		 * @param pBeginInfo			Descriptor
		 * @return Result code
		 */
		int vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

		/**
		 * Stops recording.
		 * @param commandBuffer Command buffer
		 * @return Result code
		 */
		int vkEndCommandBuffer(Buffer commandBuffer);

		/**
		 * Resets a command buffer.
		 * @param commandBuffer			Command buffer
		 * @param flags					Flags
		 * @return Result code
		 */
		int vkResetCommandBuffer(Buffer commandBuffer, int flags);

		/**
		 * Executes secondary command buffers.
		 * @param commandBuffer			Primary command buffer
		 * @param commandBufferCount	Number of secondary buffers
		 * @param pCommandBuffers		Secondary buffers to execute
		 */
		void vkCmdExecuteCommands(Buffer commandBuffer, int commandBufferCount, Pointer pCommandBuffers);
	}
}
