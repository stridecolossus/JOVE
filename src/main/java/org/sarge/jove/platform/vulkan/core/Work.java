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
	 * Convenience adapter for a command that can be submitted immediately.
	 */
	abstract class ImmediateCommand implements Command {
		/**
		 * Helper - Submits the given command immediately.
		 * @param cmd		Delegate command
		 * @param pool		Command pool
		 */
		public static void submit(Command cmd, Command.Pool pool) {
			final ImmediateCommand delegate = new ImmediateCommand() {
				@Override
				public void execute(VulkanLibrary lib, Handle buffer) {
					cmd.execute(lib, buffer);
				}
			};
			delegate.submit(pool);
		}

		/**
		 * Submits this one-time command to the given pool and waits for the command to complete.
		 * @param pool 		Command pool
		 * @param after		Post submit actions
		 * @see Command#once(Command.Pool, Command)
		 */
		public void submit(Command.Pool pool) {
			// Allocate one-off buffer
			final Command.Buffer buffer = Command.once(pool, this);

			try {
				// Submit work
				final Queue queue = pool.queue();
				final Work work = new Builder(queue).add(buffer).build();
				work.submit();

				// Wait for completion
				queue.waitIdle();
			}
			finally {
				// Cleanup
				buffer.free();
			}
		}
		// TODO - submitAndWait()?
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
