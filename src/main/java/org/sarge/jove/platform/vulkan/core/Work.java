package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineStageFlag;
import org.sarge.jove.platform.vulkan.VkSubmitInfo;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.util.Check;

import com.sun.jna.Memory;

/**
 * The <i>work</i> class defines a task to be submitted to a {@link Queue}.
 * @author Sarge
 */
@FunctionalInterface
public interface Work {
	/**
	 * Submits this work to the given queue.
	 */
	void submit();

	/**
	 * Adapter for a command that can be submitted immediately.
	 */
	abstract class ImmediateCommand implements Command {
		/**
		 * Helper - Wraps the given command as an immediate command.
		 * @param cmd Delegate command
		 * @return Immediate command
		 */
		public static ImmediateCommand of(Command cmd) {
			return new ImmediateCommand() {
				@Override
				public void execute(VulkanLibrary lib, Handle buffer) {
					cmd.execute(lib, buffer);
				}
			};
		}

		/**
		 * Submits this one-time command to the given pool.
		 * @param pool Command pool
		 * @param wait Whether to wait for completion
		 * @see Command#once(Command.Pool, Command)
		 */
		public void submit(Command.Pool pool, boolean wait) {
			// Allocate one-off buffer
			final Command.Buffer buffer = Command.once(pool, this);

			// Perform work
			try {
				final Work work = new Builder(pool.queue()).add(buffer).build();
				work.submit();
			}
			finally {
				buffer.free();
			}

			// Wait for work to complete
			if(wait) {
				pool.queue().waitIdle();
			}
		}

		/**
		 * Submits this one-time command to the given pool.
		 * @param pool Command pool
		 * @see Command#once(Command.Pool, Command)
		 */
		public void submit(Command.Pool pool) {
			submit(pool, false);
		}
	}

	/**
	 * Builder for work.
	 */
	public static class Builder {
		private final Queue queue;
		private final List<Command.Buffer> buffers = new ArrayList<>();
		private final Set<Semaphore> wait = new HashSet<>();
		private final Set<Semaphore> signal = new HashSet<>();
		private final Set<VkPipelineStageFlag> stages = new HashSet<>();

		/**
		 * Constructor.
		 * @param queue Submission queue
		 */
		public Builder(Queue queue) {
			this.queue = notNull(queue);
		}

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalStateException if the command buffer has not been recorded
		 * @throws IllegalArgumentException if any buffer does not match the queue family
		 */
		public Builder add(Command.Buffer buffer) {
			// Check buffer has been recorded
			if(!buffer.isReady()) throw new IllegalStateException("Command buffer has not been recorded: " + buffer);

			// Validate queue
			if(queue.family() != buffer.pool().queue().family()) {
				throw new IllegalArgumentException(String.format("Invalid queue for command buffer: queue=%s buffer=%s", queue, buffer));
			}

			// Add buffer to this work
			buffers.add(buffer);

			return this;
		}

		// TODO...

		public Builder wait(Semaphore semaphore) {
			Check.notNull(semaphore);
			wait.add(semaphore);
			return this;
		}

		public Builder signal(Semaphore semaphore) {
			Check.notNull(semaphore);
			signal.add(semaphore);
			return this;
		}

		public Builder stage(VkPipelineStageFlag stage) {
			Check.notNull(stage);
			stages.add(stage);
			return this;
		}

		// ...TODO

		/**
		 * Constructs this work.
		 * @return New work
		 * @throws IllegalArgumentException if no command buffers were added
		 */
		public Work build() {
			// Create submission descriptor
			if(buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");
			final VkSubmitInfo info = new VkSubmitInfo();

			// Populate command buffers
			info.commandBufferCount = buffers.size();
			info.pCommandBuffers = Handle.toPointerArray(buffers);

			// Populate wait semaphores
			info.waitSemaphoreCount = wait.size();
			info.pWaitSemaphores = Handle.toPointerArray(wait);

			// Populate signal semaphores
			info.signalSemaphoreCount = signal.size();
			info.pSignalSemaphores = Handle.toPointerArray(signal);

			// Populate pipeline stage flags (which for some reason is a pointer to an integer array)
			if(!stages.isEmpty()) {
				final int[] array = stages.stream().mapToInt(IntegerEnumeration::value).toArray();
				final Memory mem = new Memory(array.length * Integer.BYTES);
				mem.write(0, array, 0, array.length);
				info.pWaitDstStageMask = mem;
			}

			// Create work
			return () -> {
				final VulkanLibrary lib = queue.device().library();
				// TODO - multiple batches
				check(lib.vkQueueSubmit(queue.handle(), 1, new VkSubmitInfo[]{info}, null)); // TODO - fence
			};
		}
	}
}
