package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;

/**
 * The <i>frame composer</i> builds the render task for the next frame.
 * <p>
 * The default configuration assumes that the render sequence is comprised of <i>secondary</i> command buffers.
 * The {@link #contents(VkSubpassContents)} method can be used to override the default behaviour.
 * <p>
 * @author Sarge
 */
public class FrameComposer {
	private final Command.Pool pool;
	//private final Sequence sequence;

	private VkCommandBufferUsage[] flags = {VkCommandBufferUsage.ONE_TIME_SUBMIT};
	private VkSubpassContents contents = VkSubpassContents.SECONDARY_COMMAND_BUFFERS;

	/**
	 * Constructor.
	 * @param pool 			Command pool
	 * @param sequence		Render sequence
	 */
	public FrameComposer(Command.Pool pool) { //, Sequence sequence) {
		this.pool = requireNonNull(pool);
//		this.sequence = requireNonNull(sequence);
	}

	/**
	 * Sets the creation flags for the render task.
	 * Default is {@link VkCommandBufferUsage#ONE_TIME_SUBMIT}.
	 * @param flags Creation flags
	 */
	public void flags(VkCommandBufferUsage... flags) {
		this.flags = requireNonNull(flags);
	}

	/**
	 * Sets the subpass contents for the render sequence.
	 * Default is {@link VkSubpassContents#SECONDARY_COMMAND_BUFFERS}.
	 * @param contents Subpass contents
	 */
	public void contents(VkSubpassContents contents) {
		this.contents = requireNonNull(contents);
	}

	/**
	 * Composes the render task for the next frame.
	 * @param index In-flight frame index
	 * @param frame Frame buffer
	 * @return Render task
	 */
	public Command.Buffer compose(int index, FrameBuffer frame) {
		// Allocate a primary command buffer
		final Command.Buffer buffer = pool
				.allocate(1, true)
				.getFirst();

		// Start recording
		buffer.begin(flags);

		// Create a render pass for the given frame buffer
		final Command begin = frame.begin(contents);
// TODO
//		final Sequence pass = sequence.wrap(begin, FrameBuffer.END);
//
//		// Record render pass
//		pass.record(index, buffer);

		// Finish recording
		buffer.end();

		return buffer;
	}
}
