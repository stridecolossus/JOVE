package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.*;
import java.util.function.BiFunction;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>command</i> defines an operation performed on a work queue.
 * @author Sarge
 */
@FunctionalInterface
public interface Command {
	/**
	 * Performs this command.
	 * @param lib		Vulkan library
	 * @param buffer 	Command buffer
	 */
	void execute(VulkanLibrary lib, CommandBuffer buffer);

	/**
	 * Convenience method - Submits this command as a one-time operation and blocks until completed.
	 * @param pool Command pool
	 * @see Work#submit(Command, CommandPool)
	 */
	default CommandBuffer submit(CommandPool pool) {
		return Work.submit(this, pool);
	}

	/**
	 * A <i>command sequence</i> is a convenience abstraction for the process of recording rendering commands for a frame.
	 */
	interface Sequence {
		/**
		 * Records this command sequence to the given buffer.
		 * @param index 		Frame index
		 * @param buffer		Command buffer
		 */
		void record(int index, CommandBuffer buffer);

		/**
		 * Wraps this sequence with the given commands.
		 * @param before		Command executed before
		 * @param after			Command executed after
		 * @return Wrapped sequence
		 */
		default Sequence wrap(Command before, Command after) {
			return (index, buffer) -> {
				buffer.record(before);
				record(index, buffer);
				buffer.record(after);
			};
		}

		/**
		 * Creates a single command sequence.
		 * @param command Command
		 * @return Single command sequence
		 */
		static Sequence of(Command command) {
			return of(List.of(command));
		}

		/**
		 * Creates a sequence from the given list of commands.
		 * @param commands Commands
		 * @return Sequence
		 */
		static Sequence of(List<Command> commands) {
			return (index, buffer) -> {
				for(Command cmd : commands) {
					buffer.record(cmd);
				}
			};
		}
	}

	/**
	 * A <i>command buffer</i> is allocated by a {@link CommandPool} and used to record a command sequence.
	 * @see Sequence
	 */
	abstract class CommandBuffer implements NativeObject {
		/**
		 * Buffer recording states.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#commandbuffers-lifecycle">lifecycle</a>
		 */
		protected enum State {
			INITIAL,
			RECORDING,
			EXECUTABLE
		}

		private final Handle handle;
		private final CommandPool pool;

		private State state = State.INITIAL;

		/**
		 * Constructor.
		 * @param handle 		Command buffer handle
		 * @param pool			Parent pool
		 */
		protected CommandBuffer(Handle handle, CommandPool pool) {
			this.handle = requireNonNull(handle);
			this.pool = requireNonNull(pool);
		}

		@Override
		public final Handle handle() {
			return handle;
		}

		/**
		 * @return Parent command pool
		 */
		public final CommandPool pool() {
			return pool;
		}

		/**
		 * @return Whether this command buffer is ready for submission (i.e. has been recorded)
		 */
		public final boolean isReady() {
			return state == State.EXECUTABLE;
		}

		/**
		 * Validates the state of this buffer.
		 * @param expected Expected state
		 * @throws IllegalStateException if this buffer is not in the expected state
		 */
		protected final void validate(State expected) {
			if(state != expected) {
				throw new IllegalStateException(String.format("Invalid buffer state: expected=%s actual=%s", expected, state));
			}
		}

		/**
		 * Starts recording.
		 * @param inheritance		Inheritance descriptor for a secondary command buffer
		 * @param flags				Creation flags
		 * @return Command buffer recorder
		 */
		protected final CommandBuffer begin(VkCommandBufferInheritanceInfo inheritance, VkCommandBufferUsage... flags) {
			// Check buffer can be recorded
			validate(State.INITIAL);

			// Init descriptor
			final var info = new VkCommandBufferBeginInfo();
			info.flags = new EnumMask<>(flags);
			info.pInheritanceInfo = inheritance;

			// Start buffer recording
			final VulkanLibrary lib = pool.device().vulkan().library();
			lib.vkBeginCommandBuffer(this, info);

			// Start recording
			state = State.RECORDING;

			return this;
		}

		/**
		 * Records a command to this buffer.
		 * @param command Command
		 * @throws IllegalStateException if this buffer is not recording
		 * @throws IllegalArgumentException if the given command is for a {@link SecondaryBufferCommand} but this is not a primary command buffer
		 * @see Command#execute(VulkanLibrary, CommandBuffer)
		 */
		public final CommandBuffer record(Command command) {
			validate(State.RECORDING);
			command.execute(pool.library(), CommandBuffer.this);
			return this;
		}

		/**
		 * Records secondary command buffers to this primary buffer.
		 * @param buffers Secondary command buffers
		 * @throws IllegalArgumentException if any of {@link #buffers} is not ready for recording
		 */
		protected final void execute(List<SecondaryBuffer> buffers) {
			// Validate secondary buffers can be executed
			for(var e : buffers) {
    			e.validate(State.EXECUTABLE);
    		}

			// Record secondary buffers
			final CommandBuffer[] array = buffers.toArray(CommandBuffer[]::new);
    		final Library lib = pool.library();
    		lib.vkCmdExecuteCommands(this, array.length, array);
    	}

		/**
		 * Ends recording.
		 * @throws IllegalStateException if this buffer is not recording
		 */
		public final CommandBuffer end() {
			validate(State.RECORDING);
			pool.library().vkEndCommandBuffer(CommandBuffer.this);
			state = State.EXECUTABLE;
			return CommandBuffer.this;
		}

		/**
		 * Resets this command buffer.
		 * @param flags Flags
		 * @throws IllegalStateException if this buffer has not been recorded
		 */
		public final void reset(VkCommandBufferResetFlag... flags) {
			validate(State.EXECUTABLE);
			// TODO - check pool has flag
			final EnumMask<VkCommandBufferResetFlag> mask = EnumMask.of(flags);
			pool.library().vkResetCommandBuffer(this, mask);
			state = State.INITIAL;
		}
		// TODO - should allocated buffers be invalidated? (ditto free)

		/**
		 * Releases this buffer back to the pool.
		 * @see CommandPool#free(Collection)
		 */
		public final void free() {
			pool.free(Set.of(this));
		}
	}

	/**
	 * A <i>primary</i> command buffer can be submitted to a queue to perform work.
	 * <p>
	 * Primary buffers can also contain secondary buffers using the {@link PrimaryBuffer#add(List)} method.
	 */
	class PrimaryBuffer extends CommandBuffer {
		/**
		 * Constructor.
		 */
		PrimaryBuffer(Handle handle, CommandPool pool) {
			super(handle, pool);
		}

		/**
		 * Starts recording to this command buffer.
		 * @param flags Creation flags
		 * @return Primary command buffer recorder
		 * @throws IllegalStateException if this buffer is not ready for recording
		 */
		public PrimaryBuffer begin(VkCommandBufferUsage... flags) {
			super.begin(null, flags);
			return this;
		}
		// TODO - can secondary buffers also do this?

		/**
		 * Records secondary command buffers to this primary buffer.
		 * @param buffers Secondary command buffers
		 * @throws IllegalArgumentException if any of the {@link #buffers} are not ready for recording
		 * @throws IllegalStateException if this buffer is not recording
		 */
		public PrimaryBuffer add(List<SecondaryBuffer> buffers) {
			validate(State.RECORDING);
			execute(buffers);
    		return this;
    	}
	}

	/**
	 * A <i>secondary</i> command buffer is a subroutine command sequence that be reused in a primary command buffer.
	 * <p>
	 * Secondary command buffers are commonly used to record render sequences offline and/or in parallel with the rendering process,
	 * which are then composed into a one-time primary command buffer during rendering.
	 * <p>
	 * @see PrimaryBuffer#add(List)
	 */
	class SecondaryBuffer extends CommandBuffer {
		/**
		 * Constructor.
		 */
		SecondaryBuffer(Handle handle, CommandPool pool) {
			super(handle, pool);
		}

		/**
		 * Starts recording a render pass to this secondary command buffer.
		 * Note that the creation flag for this recording is assumed to be {@link VkCommandBufferUsage#RENDER_PASS_CONTINUE}.
		 * @param pass Render pass
		 * @throws IllegalStateException if this buffer is not ready for recording
		 */
		public SecondaryBuffer begin(Handle pass) {
			// Check can be recorded
			validate(State.INITIAL);

			// Init inheritance
			final var info = new VkCommandBufferInheritanceInfo();
			info.renderPass = requireNonNull(pass);
			info.subpass = 0; // TODO - subpass index, query stuff

			// Begin recording
			super.begin(info, VkCommandBufferUsage.RENDER_PASS_CONTINUE);

			return this;
		}

		/**
		 * Convenience method to create a command sequence for this secondary buffer.
		 * @return Secondary buffer command sequence
		 */
		public Sequence sequence() {
			return (__, buffer) -> buffer.execute(List.of(this));
		}

		// TODO
		public Sequence sequence(Sequence sequence, int index, RenderPass pass) {
			begin(pass.handle());
			sequence.record(index, this);
			end();
			return (__, buffer) -> buffer.execute(List.of(this));
		}
	}

	/**
	 * A <i>command pool</i> allocates command buffers used to perform work on a given {@link WorkQueue}.
	 */
	class CommandPool extends VulkanObject {
		/**
		 * Creates a command pool for the given queue.
		 * @param dev		Logical device
		 * @param queue		Work queue
		 * @param flags		Creation flags
		 */
		public static CommandPool create(DeviceContext dev, WorkQueue queue, VkCommandPoolCreateFlag... flags) {
			// Init pool descriptor
			final var info = new VkCommandPoolCreateInfo();
			info.queueFamilyIndex = queue.family().index();
			info.flags = EnumMask.of(flags);

			// Create pool
			final Vulkan vulkan = dev.vulkan();
			final NativeReference<Handle> pool = vulkan.factory().pointer();
			vulkan.library().vkCreateCommandPool(dev, info, null, pool);

			// Create domain object
			return new CommandPool(pool.get(), dev, queue);
		}

		private final WorkQueue queue;

		/**
		 * Constructor.
		 * @param handle 		Command pool handle
		 * @param dev			Logical device
		 * @param queue			Work queue
		 */
		CommandPool(Handle handle, DeviceContext dev, WorkQueue queue) {
			super(handle, dev);
			this.queue = requireNonNull(queue);
		}

		/**
		 * @return Work queue for this pool
		 */
		public WorkQueue queue() {
			return queue;
		}

		public VulkanLibrary library() {
			return this.device().vulkan().library();
		}

		/**
		 * Allocates a number of primary command buffers.
		 * @param num Number of buffers to allocate
		 * @return Primary command buffers
		 */
		public List<PrimaryBuffer> primary(int num) {
			return allocate(num, true, PrimaryBuffer::new);
		}

		/**
		 * Convenience method to allocate a single primary command buffer.
		 * @return Primary buffer
		 */
		public PrimaryBuffer primary() {
			return primary(1).get(0);
		}

		/**
		 * Allocates a number of secondary command buffers.
		 * @param num Number of buffers to allocate
		 * @return Secondary command buffers
		 */
		public List<SecondaryBuffer> secondary(int num) {
			return allocate(num, false, SecondaryBuffer::new);
		}

		/**
		 * Convenience method to allocate a single secondary command buffer.
		 * @return Secondary buffer
		 */
		public SecondaryBuffer secondary() {
			return secondary(1).get(0);
		}

		/**
		 * Allocates a number of command buffers from this pool.
		 * @param num				Number of buffers to allocate
		 * @param primary			Whether to allocate primary or secondary buffers
		 * @param constructor		Constructor method
		 * @return Allocated buffers
		 */
		private <T extends CommandBuffer> List<T> allocate(int num, boolean primary, BiFunction<Handle, CommandPool, T> constructor) {
			// Init descriptor
			final var info = new VkCommandBufferAllocateInfo();
			info.level = primary ? VkCommandBufferLevel.PRIMARY : VkCommandBufferLevel.SECONDARY;
			info.commandBufferCount = requireOneOrMore(num);
			info.commandPool = this.handle();

			// Allocate buffers
			final DeviceContext dev = super.device();
			final Handle[] handles = new Handle[num];
			dev.vulkan().library().vkAllocateCommandBuffers(dev, info, handles);

			// Create buffers
			return Arrays
					.stream(handles)
					.map(handle -> constructor.apply(handle, this))
					.toList();
		}

		/**
		 * Resets this command pool.
		 * @param flags Reset flags
		 */
		public void reset(VkCommandPoolResetFlag... flags) {
			final var bits = EnumMask.of(flags);
			final DeviceContext dev = super.device();
			this.library().vkResetCommandPool(dev, this, bits);
		}

		/**
		 * Releases a set of command buffers back to this pool.
		 * @param buffers Buffers to release
		 */
		public void free(Collection<CommandBuffer> buffers) {
			final CommandBuffer[] array = buffers.toArray(CommandBuffer[]::new);
			final DeviceContext dev = super.device();
			this.library().vkFreeCommandBuffers(dev, this, array.length, array);
		}

		@Override
		protected Destructor<CommandPool> destructor(VulkanLibrary lib) {
			return lib::vkDestroyCommandPool;
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
		int vkCreateCommandPool(DeviceContext device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, NativeReference<Handle> pCommandPool);

		/**
		 * Destroys a command pool (and its buffers).
		 * @param device			Logical device
		 * @param commandPool		Command pool
		 * @param pAllocator		Allocator
		 */
		void vkDestroyCommandPool(DeviceContext device, CommandPool commandPool, Handle pAllocator);

		/**
		 * Resets a command pool.
		 * @param device			Logical device
		 * @param commandPool		Command pool
		 * @param flags				Flags
		 * @return Result
		 */
		int vkResetCommandPool(DeviceContext device, CommandPool commandPool, EnumMask<VkCommandPoolResetFlag> flags);

		/**
		 * Allocates a number of command buffers.
		 * @param device			Logical device
		 * @param pAllocateInfo		Descriptor
		 * @param pCommandBuffers	Buffer handles
		 * @return Result
		 */
		int vkAllocateCommandBuffers(DeviceContext device, VkCommandBufferAllocateInfo pAllocateInfo, @Returned Handle[] pCommandBuffers);

		/**
		 * Releases a set of command buffers back to the pool.
		 * @param device				Logical device
		 * @param commandPool			Command pool
		 * @param commandBufferCount	Number of buffers
		 * @param pCommandBuffers		Command buffers
		 */
		void vkFreeCommandBuffers(DeviceContext device, CommandPool commandPool, int commandBufferCount, CommandBuffer[] pCommandBuffers);

		/**
		 * Starts recording.
		 * @param commandBuffer			Command buffer
		 * @param pBeginInfo			Descriptor
		 * @return Result
		 */
		int vkBeginCommandBuffer(CommandBuffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);

		/**
		 * Stops recording.
		 * @param commandBuffer Command buffer
		 * @return Result
		 */
		int vkEndCommandBuffer(CommandBuffer commandBuffer);

		/**
		 * Resets a command buffer.
		 * @param commandBuffer			Command buffer
		 * @param flags					Flags
		 * @return Result
		 */
		int vkResetCommandBuffer(CommandBuffer commandBuffer, EnumMask<VkCommandBufferResetFlag> flags);

		/**
		 * Executes secondary command buffers.
		 * @param commandBuffer			Primary command buffer
		 * @param commandBufferCount	Number of secondary buffers
		 * @param pCommandBuffers		Secondary buffers to execute
		 */
		void vkCmdExecuteCommands(CommandBuffer commandBuffer, int commandBufferCount, CommandBuffer[] pCommandBuffers);
	}
}
