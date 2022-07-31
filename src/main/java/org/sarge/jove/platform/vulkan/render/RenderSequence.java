package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.*;

import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * A <i>render sequence</i> is a builder for a command sequence to render to a frame buffer.
 * <p>
 * Usage:
 * <pre>
 * // Define a function or method to record the command sequence
 * void record(Command.Buffer) {
 *     ...
 * }
 *
 * // Create a sequence builder that allocates a new one-time buffer on demand
 * Command.Pool pool = ...
 * RenderSequence seq = new RenderSequence(pool::allocate, this::record, VkCommandBufferUsage.ONE_TIME_SUBMIT);
 *
 * // Build the rendering sequence for the next frame
 * FrameBuffer frame = ...
 * Command.Buffer buffer = seq.build(frame);
 * </pre>
 * @author Sarge
 */
public class RenderSequence {
	private final Supplier<Buffer> factory;
	private final Consumer<Buffer> sequence;
	private final VkCommandBufferUsage[] flags;

	/**
	 * Constructor.
	 * @param factory		Command buffer factory
	 * @param sequence		Render sequence recorder
	 * @param flags			Command buffer flags
	 */
	public RenderSequence(Supplier<Buffer> factory, Consumer<Buffer> sequence, VkCommandBufferUsage... flags) {
		this.factory = notNull(factory);
		this.sequence = notNull(sequence);
		this.flags = flags.clone();
	}

	/**
	 * Builds the command buffer to render to the given target frame buffer.
	 * @param frame Target frame buffer
	 * @return Command buffer
	 */
	public Buffer build(FrameBuffer frame) {
		// Allocate buffer
		final Buffer buffer = factory.get();

		// Build render sequence
		buffer.begin(flags);
		buffer.add(frame.begin());
		sequence.accept(buffer);
		buffer.add(FrameBuffer.END);
		buffer.end();

		return buffer;
	}
}
