package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>command</i> defines an operation performed on a work queue.
 * @author Sarge
 */
@FunctionalInterface
public interface Command {
	/**
	 * Records this command to the given buffer.
	 * @param lib		Vulkan library
	 * @param buffer 	Command buffer
	 */
	void record(VulkanLibrary lib, Buffer buffer);

	/**
	 * An <i>immediate command</i> is a convenience base-class for one-off commands such as transfer operations.
	 */
	abstract class ImmediateCommand implements Command {
		/**
		 * Submits this as a <i>one time</i> command to the given pool and waits for completion.
		 * @param pool Command pool
		 * @see Buffer#submit()
		 * @see VkCommandBufferUsage#ONE_TIME_SUBMIT
		 */
		public Buffer submit(Pool pool) {
			return pool
					.allocate()
					.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
					.add(this)
					.end()
					.submit();
		}
	}

	/**
	 * A <i>command buffer</i> is allocated by a {@link Pool} and used to record a command sequence.
	 */
	class Buffer implements NativeObject {
		/**
		 * Buffer recording states.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#commandbuffers-lifecycle">lifecycle</a>
		 */
		private enum State {
			INITIAL,
			RECORDING,
			EXECUTABLE
		}

		private final Handle handle;
		private final Pool pool;
		private final boolean primary;

		private State state = State.INITIAL;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 * @param primary		Whether this is a primary or secondary command buffer
		 */
		Buffer(Handle handle, Pool pool, boolean primary) {
			this.handle = notNull(handle);
			this.pool = notNull(pool);
			this.primary = primary;
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
		public boolean isPrimary() {
			return primary;
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
		 * A <i>command buffer recorder</i> adds commands to a render sequence.
		 */
		public class Recorder {
			private Recorder() {
			}

			/**
			 * Records a command to this buffer.
			 * @param cmd Command
			 * @throws IllegalStateException if this buffer is not recording
			 * @see Command#record(VulkanLibrary, Buffer)
			 */
			public Recorder add(Command cmd) {
				final VulkanLibrary lib = pool.device().library();
				validate(State.RECORDING);
				cmd.record(lib, Buffer.this);
				return this;
			}

			/**
			 * Records a set of secondary command buffers.
			 * @param secondary Secondary buffers
			 * @throws IllegalStateException if this buffer is not recording or is a secondary buffer
			 * @throws IllegalStateException if any of {@link #secondary} have not been recorded
			 * @throws IllegalArgumentException if any of {@link #secondary} are {@link Buffer#isPrimary()}
			 */
			public Recorder add(List<Buffer> secondary) {
				// Validate
				if(!isPrimary()) throw new IllegalStateException("Secondary buffer cannot contain further secondary buffers");
				validate(State.RECORDING);
				for(var e : secondary) {
					if(e.isPrimary()) throw new IllegalArgumentException("Cannot add a primary command buffer as a secondary buffer: " + e);
					e.validate(State.EXECUTABLE);
				}

				// Record secondary buffers
				final Pointer array = NativeObject.array(secondary);
				final VulkanLibrary lib = pool.device().library();
				lib.vkCmdExecuteCommands(Buffer.this, secondary.size(), array);
				return this;
			}

			/**
			 * Ends recording.
			 * @throws IllegalStateException if this buffer is not recording
			 */
			public Buffer end() {
				validate(State.RECORDING);
				final VulkanLibrary lib = pool.device().library();
				check(lib.vkEndCommandBuffer(Buffer.this));
				state = State.EXECUTABLE;
				return Buffer.this;
			}
		}

		/**
		 * Starts recording to this command buffer.
		 * @param flags Creation flags
		 * @return New command buffer recorder
		 * @throws IllegalStateException if this buffer is not ready for recording
		 */
		public Recorder begin(VkCommandBufferUsage... flags) {
			// Check buffer can be recorded
			validate(State.INITIAL);

			// Init descriptor
			final var info = new VkCommandBufferBeginInfo();
			info.flags = BitMask.reduce(flags);
			info.pInheritanceInfo = null;

			// Start buffer recording
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkBeginCommandBuffer(this, info));

			// Start recording
			state = State.RECORDING;
			return new Recorder();
		}

		/**
		 * Helper - Submits this buffer and <b>blocks</b> until completion.
		 * @see Work#submit(Fence)
		 */
		public Buffer submit() {
			final Fence fence = Fence.create(pool.device());
			try {
				Work.of(this).submit(fence);
				fence.waitReady();
			}
			finally {
				fence.destroy();
				free();
			}
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
			final BitMask<VkCommandBufferResetFlag> mask = BitMask.reduce(flags);
			final VulkanLibrary lib = pool.device().library();
			check(lib.vkResetCommandBuffer(this, mask));
			state = State.INITIAL;
		}

		/**
		 * Releases this buffer back to the pool.
		 * @see Pool#free(Collection)
		 */
		public void free() {
			pool.free(Set.of(this));
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(handle)
					.append("primary", primary)
					.append(state)
					.append(pool)
					.build();
		}
	}

	/**
	 * A <i>command pool</i> allocates command buffers used to perform work on a given {@link Queue}.
	 */
	class Pool extends AbstractVulkanObject {
		/**
		 * Creates a command pool for the given queue.
		 * @param dev		Logical device
		 * @param queue		Work queue
		 * @param flags		Creation flags
		 */
		public static Pool create(DeviceContext dev, Queue queue, VkCommandPoolCreateFlag... flags) {
			// Init pool descriptor
			final var info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = queue.family().index();
			info.flags = BitMask.reduce(flags);

			// Create pool
			final VulkanLibrary lib = dev.library();
			final PointerByReference pool = dev.factory().pointer();
			check(lib.vkCreateCommandPool(dev, info, null, pool));

			// Create pool
			return new Pool(new Handle(pool), dev, queue);
		}

		private final Queue queue;

		/**
		 * Constructor.
		 * @param handle 		Command pool handle
		 * @param dev			Logical device
		 * @param queue			Work queue
		 */
		private Pool(Handle handle, DeviceContext dev, Queue queue) {
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
		 * Allocates a number of command buffers from this pool.
		 * @param num			Number of buffers to allocate
		 * @param level			Buffer level
		 * @return Allocated buffers
		 */
		public List<Buffer> allocate(int num, boolean primary) {
			// Init descriptor
			final var info = new VkCommandBufferAllocateInfo();
			info.level = primary ? VkCommandBufferLevel.PRIMARY : VkCommandBufferLevel.SECONDARY;
			info.commandBufferCount = oneOrMore(num);
			info.commandPool = this.handle();

			// Allocate buffers
			final DeviceContext dev = super.device();
			final VulkanLibrary lib = dev.library();
			final Pointer[] handles = new Pointer[num];
			check(lib.vkAllocateCommandBuffers(dev, info, handles));

			// Create buffers
			return Arrays
					.stream(handles)
					.map(Handle::new)
					.map(handle -> new Buffer(handle, this, primary))
					.toList();
		}

		/**
		 * Allocates a single primary command buffer from this pool.
		 * @return New command buffer
		 */
		public Buffer allocate() {
			final List<Buffer> buffers = allocate(1, true);
			return buffers.get(0);
		}

		/**
		 * Allocates a number of primary command buffers from this pool.
		 * @param num Number of buffers to allocate
		 * @return New command buffers
		 */
		public List<Buffer> allocate(int num) {
			return allocate(num, true);
		}

		/**
		 * Resets this command pool.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final BitMask<VkCommandPoolResetFlag> bits = BitMask.reduce(flags);
			final DeviceContext dev = super.device();
			check(dev.library().vkResetCommandPool(dev, this, bits));
		}

		/**
		 * Releases a set of command buffers back to this pool.
		 * @param buffers Buffers to release
		 */
		public void free(Collection<Buffer> buffers) {
			final DeviceContext dev = super.device();
			dev.library().vkFreeCommandBuffers(dev, this, buffers.size(), NativeObject.array(buffers));
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
		public String toString() {
			return new ToStringBuilder(this)
					.appendSuper(super.toString())
					.append("queue", queue)
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
		 * @return Result
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
		 * @return Result
		 */
		int vkResetCommandPool(DeviceContext device, Pool commandPool, BitMask<VkCommandPoolResetFlag> flags);

		/**
		 * Allocates a number of command buffers.
		 * @param device			Logical device
		 * @param pAllocateInfo		Descriptor
		 * @param pCommandBuffers	Returned buffer handles
		 * @return Result
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
		 * @return Result
		 */
		int vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

		/**
		 * Stops recording.
		 * @param commandBuffer Command buffer
		 * @return Result
		 */
		int vkEndCommandBuffer(Buffer commandBuffer);

		/**
		 * Resets a command buffer.
		 * @param commandBuffer			Command buffer
		 * @param flags					Flags
		 * @return Result
		 */
		int vkResetCommandBuffer(Buffer commandBuffer, BitMask<VkCommandBufferResetFlag> flags);

		/**
		 * Executes secondary command buffers.
		 * @param commandBuffer			Primary command buffer
		 * @param commandBufferCount	Number of secondary buffers
		 * @param pCommandBuffers		Secondary buffers to execute
		 */
		void vkCmdExecuteCommands(Buffer commandBuffer, int commandBufferCount, Pointer pCommandBuffers);
	}
}
