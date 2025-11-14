package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.VkCommandBufferLevel.*;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.Command.Buffer.Stage;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>command</i> defines an operation performed on a work queue.
 * @author Sarge
 */
@FunctionalInterface
public interface Command {
	/**
	 * Records this command to the given buffer.
	 * @param buffer Command buffer
	 */
	void execute(Buffer buffer);

	/**
	 * A <i>command buffer</i> records a command sequence.
	 * Buffers are allocated by a {@link Pool}.
	 */
	class Buffer implements NativeObject {
		/**
		 * Buffer recording states.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#commandbuffers-lifecycle">lifecycle</a>
		 */
		enum Stage {
			INITIAL,
			RECORDING,
			EXECUTABLE,
			INVALID
		}

		private final Handle handle;
		private final Pool pool;
		private final boolean primary;

		private Stage stage = Stage.INITIAL;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 * @param primary		Whether this is a primary or secondary buffer
		 */
		Buffer(Handle handle, Pool pool, boolean primary) {
			this.handle = requireNonNull(handle);
			this.pool = requireNonNull(pool);
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
		 * @return Whether this is a primary or secondary buffer
		 */
		public boolean isPrimary() {
			return primary;
		}

		/**
		 * @return Recording stage of this buffer
		 */
		Stage stage() {
			return stage;
		}

		/**
		 * @return Whether this command buffer is ready for submission
		 * @see Stage#EXECUTABLE
		 */
		public boolean isReady() {
			return stage == Stage.EXECUTABLE;
		}

		/**
		 * Validates the state of this buffer.
		 * @param expected Expected state
		 * @throws IllegalStateException if this buffer is not in the expected state
		 */
		private void check(Stage expected) {
			if(stage != expected) {
				throw new IllegalStateException(String.format("Invalid buffer state: expected=%s actual=%s", expected, stage));
			}
		}

		/**
		 * Starts recording this primary buffer.
		 * @param flags Usage flags
		 * @see #begin(VkCommandBufferInheritanceInfo, VkCommandBufferUsage...)
		 */
		public Buffer begin(VkCommandBufferUsage... flags) {
			return begin(null, flags);
		}

		/**
		 * Starts recording this buffer.
		 * @param inheritance		Inheritance descriptor
		 * @param flags				Usage flags
		 */
		public Buffer begin(VkCommandBufferInheritanceInfo inheritance, VkCommandBufferUsage... flags) {
			// Check buffer can be recorded
			check(Stage.INITIAL);

			// Check inheritance descriptor provided for a secondary buffer
			if(primary ^ Objects.isNull(inheritance)) {
				throw new IllegalArgumentException("Mismatched inheritance descriptor: " + this);
			}

			// Init descriptor
			final var info = new VkCommandBufferBeginInfo();
			info.flags = new EnumMask<>(flags);
			info.pInheritanceInfo = inheritance;

			// Start buffer recording
			final Library library = pool.library();
			library.vkBeginCommandBuffer(this, info);

			// Start recording
			stage = Stage.RECORDING;

			return this;
		}

		/**
		 * Records a command to this buffer.
		 * @param command Command
		 * @throws IllegalStateException if this buffer is not recording
		 */
		public Buffer add(Command command) {
			check(Stage.RECORDING);
			command.execute(this);
			return this;
		}

		/**
		 * Records secondary command buffers to this buffer.
		 * TODO - can this ONLY be done on a primary buffer?
		 * @param buffers Secondary command buffers
		 * @throws IllegalArgumentException if any of {@link #buffers} is not a secondary buffer or is not ready for recording
		 */
		public void add(List<Buffer> buffers) {
			// Validate secondary buffers can be executed
			for(var buffer : buffers) {
				if(buffer.isPrimary()) {
					throw new IllegalArgumentException("Not a primary buffer: " + buffer);
				}
    			buffer.check(Stage.EXECUTABLE);
    		}

			// Record secondary buffers
			check(Stage.RECORDING);
			final Buffer[] array = buffers.toArray(Buffer[]::new);
    		final Library lib = pool.library();
    		lib.vkCmdExecuteCommands(this, array.length, array);
    	}

		/**
		 * Ends recording.
		 * @throws IllegalStateException if this buffer is not recording
		 */
		public Buffer end() {
			check(Stage.RECORDING);
			pool.library().vkEndCommandBuffer(Buffer.this);
			stage = Stage.EXECUTABLE;
			return this;
		}

		/**
		 * Resets this command buffer.
		 * @param flags Reset flags
		 * @throws IllegalStateException if this buffer has not been recorded
		 */
		public void reset(VkCommandBufferResetFlag... flags) {
			check(Stage.EXECUTABLE);
			// TODO - check pool has flag
			final EnumMask<VkCommandBufferResetFlag> mask = new EnumMask<>(flags);
			pool.library().vkResetCommandBuffer(this, mask);
			stage = Stage.INITIAL;
		}
		// TODO - should allocated buffers be invalidated? (ditto free)

		/**
		 * Releases this buffer back to the pool.
		 * @see CommandPool#free(Collection)
		 */
		public void free() {
			pool.free(Set.of(this));
			stage = Stage.INVALID;
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this);
		}

		@Override
		public String toString() {
			return String.format("CommandBuffer[handle=%s primary=%b stage=%s pool=%s]", this.handle(), primary, stage, pool);
		}
	}

	/**
	 * A <i>command pool</i> allocates command buffers used to perform work on a given {@link WorkQueue}.
	 */
	class Pool extends VulkanObject {
		/**
		 * Creates a command pool for the given queue.
		 * @param device		Logical device
		 * @param queue			Work queue
		 * @param flags			Creation flags
		 */
		public static Pool create(LogicalDevice device, WorkQueue queue, VkCommandPoolCreateFlag... flags) {
			// Init pool descriptor
			final var info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = queue.family().index();
			info.flags = new EnumMask<>(flags);

			// Create pool
			final Library library = device.library();
			final Pointer handle = new Pointer();
			library.vkCreateCommandPool(device, info, null, handle);

			// Create domain object
			return new Pool(handle.get(), device, queue, library);
		}

		private final WorkQueue queue;
		private final Library library;
		private final List<Buffer> buffers = new ArrayList<>();

		/**
		 * Constructor.
		 * @param handle 		Command pool handle
		 * @param dev			Logical device
		 * @param queue			Work queue
		 * @param library		Command library
		 */
		Pool(Handle handle, LogicalDevice dev, WorkQueue queue, Library library) {
			super(handle, dev);
			this.queue = requireNonNull(queue);
			this.library = requireNonNull(library);
		}

		/**
		 * @return Work queue for this pool
		 */
		public WorkQueue queue() {
			return queue;
		}

		/**
		 * @return Buffers allocated by this pool
		 */
		public List<Buffer> buffers() {
			return Collections.unmodifiableList(buffers);
		}

		/**
		 * @return Command library
		 */
		Library library() {
			return library;
		}

		/**
		 * Allocates a number of command buffers from this pool.
		 * @param number		Number of buffers to allocate
		 * @param primary		Whether allocating primary or secondary buffers
		 * @return Allocated buffers
		 * @see VkCommandBufferLevel
		 */
		public List<Buffer> allocate(int number, boolean primary) {
			// Init descriptor
			final var info = new VkCommandBufferAllocateInfo();
			info.level = primary ? PRIMARY : SECONDARY;
			info.commandBufferCount = requireOneOrMore(number);
			info.commandPool = this.handle();

			// Allocate buffers
			final Handle[] handles = new Handle[number];
			library.vkAllocateCommandBuffers(this.device(), info, handles);

			// Create buffers
			final List<Buffer> allocated = Arrays
					.stream(handles)
					.map(handle -> new Buffer(handle, this, primary))
					.toList();

			// Record buffers allocated by this pool
			buffers.addAll(allocated);

			return allocated;
		}

		/**
		 * Resets this command pool.
		 * All allocated buffers are reset to the {@link Stage#INITIAL} state.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final var bits = new EnumMask<>(flags);
			library.vkResetCommandPool(this.device(), this, bits);
			update(buffers, Stage.INITIAL);
		}

		/**
		 * Releases a set of command buffers back to this pool.
		 * @param buffers Buffers to release
		 */
		public void free(Collection<Buffer> buffers) {
			final Buffer[] array = buffers.toArray(Buffer[]::new);
			library.vkFreeCommandBuffers(this.device(), this, array.length, array);
			update(buffers, Stage.INVALID);
			this.buffers.removeAll(buffers);
		}

		@Override
		protected Destructor<Pool> destructor() {
			return library::vkDestroyCommandPool;
		}

		@Override
		protected void release() {
			update(buffers, Stage.INVALID);
			buffers.clear();
		}

		private static void update(Collection<Buffer> buffers, Stage stage) {
    		for(Buffer b : buffers) {
    			b.stage = stage;
    		}
		}

		@Override
		public String toString() {
			return String.format("CommandPool[handle=%s queue=%s buffers=%d]", this.handle(), queue, buffers.size());
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
		 * @param pCommandPool		Returned command pool handle
		 * @return Result
		 */
		VkResult vkCreateCommandPool(LogicalDevice device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, Pointer pCommandPool);

		/**
		 * Destroys a command pool (and its buffers).
		 * @param device			Logical device
		 * @param commandPool		Command pool
		 * @param pAllocator		Allocator
		 */
		void vkDestroyCommandPool(LogicalDevice device, Pool commandPool, Handle pAllocator);

		/**
		 * Resets a command pool.
		 * @param device			Logical device
		 * @param commandPool		Command pool
		 * @param flags				Flags
		 * @return Result
		 */
		VkResult vkResetCommandPool(LogicalDevice device, Pool commandPool, EnumMask<VkCommandPoolResetFlag> flags);

		/**
		 * Allocates a number of command buffers.
		 * @param device			Logical device
		 * @param pAllocateInfo		Descriptor
		 * @param pCommandBuffers	Returned buffer handles
		 * @return Result
		 */
		VkResult vkAllocateCommandBuffers(LogicalDevice device, VkCommandBufferAllocateInfo pAllocateInfo, @Updated Handle[] pCommandBuffers);

		/**
		 * Releases a set of command buffers back to the pool.
		 * @param device				Logical device
		 * @param commandPool			Command pool
		 * @param commandBufferCount	Number of buffers
		 * @param pCommandBuffers		Command buffers
		 */
		void vkFreeCommandBuffers(LogicalDevice device, Pool commandPool, int commandBufferCount, Buffer[] pCommandBuffers);

		/**
		 * Starts recording.
		 * @param commandBuffer			Command buffer
		 * @param pBeginInfo			Descriptor
		 * @return Result
		 */
		VkResult vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

		/**
		 * Stops recording.
		 * @param commandBuffer Command buffer
		 * @return Result
		 */
		VkResult vkEndCommandBuffer(Buffer commandBuffer);

		/**
		 * Resets a command buffer.
		 * @param commandBuffer			Command buffer
		 * @param flags					Flags
		 * @return Result
		 */
		VkResult vkResetCommandBuffer(Buffer commandBuffer, EnumMask<VkCommandBufferResetFlag> flags);

		/**
		 * Executes secondary command buffers.
		 * @param commandBuffer			Primary command buffer
		 * @param commandBufferCount	Number of secondary buffers
		 * @param pCommandBuffers		Secondary buffers to execute
		 */
		void vkCmdExecuteCommands(Buffer commandBuffer, int commandBufferCount, Buffer[] pCommandBuffers);
	}
}
