package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.function.Supplier;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.*;

/**
 * The <i>frame composer</i> builds the render task for the next frame.
 * TODO - doc
 * @author Sarge
 */
public class FrameComposer {
	private final Pool pool;
	private final Supplier<List<Buffer>> sequence;
	private VkCommandBufferUsage[] flags = {VkCommandBufferUsage.ONE_TIME_SUBMIT};
	private VkSubpassContents contents = VkSubpassContents.SECONDARY_COMMAND_BUFFERS;

	/**
	 * Constructor.
	 * @param pool 			Command pool
	 * @param sequence		Factory for the render sequence
	 */
	public FrameComposer(Pool pool, Supplier<List<Buffer>> sequence) {
		this.pool = notNull(pool);
		this.sequence = notNull(sequence);
	}

	/**
	 * Sets the creation flags for the render task.
	 * Default is {@link VkCommandBufferUsage#ONE_TIME_SUBMIT}.
	 * @param flags Creation flags
	 */
	public void flags(VkCommandBufferUsage... flags) {
		this.flags = notNull(flags);
	}

	/**
	 * Sets the subpass contents for the render sequence.
	 * Default is {@link VkSubpassContents#SECONDARY_COMMAND_BUFFERS}.
	 * @param contents Subpass contents
	 */
	public void contents(VkSubpassContents contents) {
		this.contents = notNull(contents);
	}

	/**
	 * Composes the render task for the next frame.
	 * @param frame Frame buffer
	 * @return Render task
	 */
	public Command.Buffer compose(FrameBuffer frame) {
		final Command begin = frame.begin(contents);
		return pool
				.allocate(true)
				.begin(flags)
				.add(begin)
				.add(sequence.get())
				.add(FrameBuffer.END)
				.end();
	}
}
