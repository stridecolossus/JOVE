package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Supplier;

import org.sarge.jove.platform.vulkan.VkCommandBufferUsage;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

/**
 * The <i>frame builder</i> creates and records the command sequence to render a frame.
 * @author Sarge
 */
public class FrameBuilder {
	/**
	 * A <i>recorder</i> is used to record a command sequence.
	 */
	public interface Recorder {
		/**
		 * Records a render sequence to the given command buffer.
		 * @param seq Command buffer
		 */
		void record(Buffer buffer);

		/**
		 * Wraps this recorder with a render pass for the given frame buffer.
		 * @param buffer Frame buffer
		 * @return Render pass recorder
		 */
		default Recorder render(FrameBuffer frame) {
			return buffer -> {
				// Start render pass
				buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT);
				buffer.add(frame.begin());

				// Record command sequence
				this.record(buffer);

				// End render pass
				buffer.add(FrameBuffer.END);
				buffer.end();
			};
		}
	}

	private final Supplier<Buffer> factory;
	private final Recorder recorder;

	/**
	 * Constructor.
	 * @param factory		Command buffer factory
	 * @param recorder		Command sequence recorder
	 */
	public FrameBuilder(Supplier<Buffer> factory, Recorder recorder) {
		this.factory = notNull(factory);
		this.recorder = notNull(recorder);
	}

	/**
	 * Builds the command sequence.
	 * @return Command sequence
	 */
	public Buffer build() {
		final Buffer buffer = factory.get();
		recorder.record(buffer);
		return buffer;
	}
}
