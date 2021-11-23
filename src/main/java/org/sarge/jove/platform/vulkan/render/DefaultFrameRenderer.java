package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.Command.Pool;
import org.sarge.jove.platform.vulkan.core.Work;
import org.sarge.jove.platform.vulkan.core.Work.Batch;

/**
 * Default implementation that composes a frame builder and submits render tasks to the work queue.
 * @author Sarge
 */
public class DefaultFrameRenderer implements VulkanFrame.FrameRenderer {
	private final FrameBuilder builder;
	private final VkPipelineStage stage;

	/**
	 * Constructor.
	 * @param builder		Render sequence builder
	 * @param stage			Pipeline stage to render
	 */
	public DefaultFrameRenderer(FrameBuilder builder, VkPipelineStage stage) {
		this.builder = notNull(builder);
		this.stage = notNull(stage);
	}

	@Override
	public void render(VulkanFrame frame) {
		// Build render sequence
		final Buffer buffer = builder.build();
		final Pool pool = buffer.pool();

		// Create work submission
		final Work work = new Work.Builder(pool)
				.add(buffer)
				.wait(frame.available(), stage)
				.signal(frame.ready())
				.build();

		// Submit render sequence
		final Batch batch = work.batch();
		pool.submit(batch, frame.fence());
	}
	// TODO - how to handle multiple sequences?
}
