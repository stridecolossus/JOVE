package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.*;

/**
 * The <i>frame presenter</i> encapsulates the process of rendering and presenting a frame to the swapchain.
 * <p>
 * The {@link #render()} method comprises the following steps:
 * <ol>
 * <li>Acquire the next frame from the swapchain</li>
 * <li>Build the render sequence for the target frame buffer</li>
 * <li>Submit the render sequence to the presentation queue</li>
 * <li>Present the rendered frame to the swapchain via</li>
 * </ol>
 * <p>
 * Notes:
 * <ul>
 * <li>Submission of the rendering task is a blocking operation</li>
 * <li>This default implementation employs Vulkan synchronisation primitives to TODO</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class FramePresenter implements TransientObject {
	private final FrameSet frames;
	private final RenderSequence seq;
	private final Semaphore available, ready;
	private final Fence fence;
	private VkPipelineStage stage = VkPipelineStage.COLOR_ATTACHMENT_OUTPUT;

	/**
	 * Constructor.
	 * @param frames		Frame buffers
	 * @param seq			Rendering sequence
	 */
	public FramePresenter(FrameSet frames, RenderSequence seq) {
		final DeviceContext dev = frames.swapchain().device();
		this.frames = notNull(frames);
		this.seq = notNull(seq);
		this.available = Semaphore.create(dev);
		this.ready = Semaphore.create(dev);
		this.fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
	}

//	/**
//	 * Sets the pipeline stage for
//	 * @param stage
//	 */
//	public void setPipelineStage(VkPipelineStage stage) {
//		this.stage = notNull(stage);
//	}
// TODO - interface? with default/abstract template implementation?

	/**
	 * Renders and presents the next frame.
	 */
	public void render() {
		// TODO
		fence.waitReady();
		fence.reset();

		// Acquire next frame
		final Swapchain swapchain = frames.swapchain();
		final int index = swapchain.acquire(available, null);

		// Retrieve render sequence
		final FrameBuffer frame = frames.buffer(index);
		final Buffer buffer = seq.build(frame);
		final Pool pool = buffer.pool();

		// Submit render task
		new Work.Builder(pool)
				.add(buffer)
				.wait(available, stage)
				.signal(ready)
				.build()
				.submit(fence);

		// Block until frame is rendered
		fence.waitReady();

		// Present frame
		swapchain.present(pool.queue(), index, ready);
	}

	@Override
	public void destroy() {
		available.destroy();
		ready.destroy();
		fence.destroy();
	}
}
