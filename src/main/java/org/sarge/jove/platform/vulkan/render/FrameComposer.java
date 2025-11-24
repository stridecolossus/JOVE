package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Set;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * The <i>frame composer</i> builds the command buffer to render the next frame.
 * @author Sarge
 */
public class FrameComposer {
	private final Command.Pool pool;
	private final RenderSequence sequence;

	/**
	 * Constructor.
	 * @param pool			Command pool
	 * @param sequence		Rendering sequence
	 */
	public FrameComposer(Command.Pool pool, RenderSequence sequence) {
		this.pool = requireNonNull(pool);
		this.sequence = requireNonNull(sequence);
	}

	/**
	 * Composes the command buffer to render the next frame.
	 * @param index				Frame index
	 * @param framebuffer		Framebuffer to be rendered
	 * @return Render command buffer
	 */
	public Buffer compose(int index, Framebuffer framebuffer) {
		// Allocate command buffer
		final Buffer buffer = allocate(pool, index);

		// Init frame buffer
		final Command begin = framebuffer.begin(this.contents());

		// Build render sequence
		begin(buffer);
    		buffer.add(begin);
    			sequence.build(index, buffer);
    		buffer.add(framebuffer.end());
    	buffer.end();

    	// TODO - should we be releasing the buffer if a one-time submit?

		return buffer;
	}

	/**
	 * Allocates the next command buffer.
	 * The default implementation allocates a new buffer for each frame.
	 * @param pool		Command pool
	 * @param index		Frame index
	 * @return Command buffer
	 */
	protected Buffer allocate(Command.Pool pool, int index) {
		return pool.allocate(1, true).getFirst();
	}
	// TODO - this needs to return a factory

	/**
	 * @return Subpass contents for the rendering command
	 */
	protected VkSubpassContents contents() {
		return VkSubpassContents.INLINE;
	}

	/**
	 * Begins recording to the given buffer.
	 * The default implementation begins recording as a {@link VkCommandBufferUsage#ONE_TIME_SUBMIT} command.
	 * @param buffer Recording buffer
	 */
	protected void begin(Buffer buffer) {
		buffer.begin(null, Set.of(VkCommandBufferUsage.ONE_TIME_SUBMIT));
	}
}
