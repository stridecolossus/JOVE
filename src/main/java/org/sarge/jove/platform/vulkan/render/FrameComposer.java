package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Set;
import java.util.function.Consumer;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * The <i>frame composer</i> builds the render sequence for the next frame according to the configured policy.
 * TODO
 * @author Sarge
 */
public class FrameComposer {
	/**
	 * The <i>buffer policy</i> specifies the properties of the render sequence.
	 */
	public record BufferPolicy(VkSubpassContents contents, Set<VkCommandBufferUsage> usage) {
		/**
		 * Default buffer policy that provides the an {VkSubpassContents#INLINE} render sequence submitted as a {@link VkCommandBufferUsage#ONE_TIME_SUBMIT} task.
		 */
		public static final BufferPolicy DEFAULT = new BufferPolicy(VkSubpassContents.INLINE, Set.of(VkCommandBufferUsage.ONE_TIME_SUBMIT));

		/**
		 * Constructor.
		 * @param contents		Specifies how the render sequence commands are provided
		 * @param usage			Behaviour of the command buffer for rendering
		 */
		public BufferPolicy {
			requireNonNull(contents);
			usage = Set.copyOf(usage);
		}
	}

	private final Command.Pool pool;
	private final BufferPolicy policy;
	private final Consumer<Buffer> sequence;

	/**
	 * Constructor.
	 * @param pool			Pool for rendering command buffers
	 * @param policy		Policy for command buffers
	 * @param sequence		Rendering sequence
	 */
	public FrameComposer(Command.Pool pool, BufferPolicy policy, Consumer<Buffer> sequence) {
		this.pool = requireNonNull(pool);
		this.policy = requireNonNull(policy);
		this.sequence = requireNonNull(sequence);
	}

	/**
	 * Composes the render sequence for the next frame.
	 * @param index Frame index
	 * @return Render sequence
	 */
	public Buffer compose(int index, Framebuffer framebuffer) {
		// Allocate command buffer
		final Buffer buffer = pool.allocate(1, true).getFirst();

		// Init frame buffer
		final Command begin = framebuffer.begin(policy.contents);

		// Build render sequence
		buffer.begin(null, policy.usage());
    		buffer.add(begin);
    			sequence.accept(buffer);
    		buffer.add(framebuffer.end());
    	buffer.end();

		return buffer;
	}

	// TODO - pre-generated array[]
}
