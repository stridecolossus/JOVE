package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

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
	private final Swapchain swapchain;
	private final FrameBuilder builder;
	private final Semaphore available, ready;
	private final Fence fence;
	private VkPipelineStage stage = VkPipelineStage.COLOR_ATTACHMENT_OUTPUT;

	/**
	 * Constructor.
	 * @param swapchain		Swapchain
	 * @param builder		Frame builder
	 */
	public FramePresenter(Swapchain swapchain, FrameBuilder builder) {
		final DeviceContext dev = swapchain.device();
		this.swapchain = notNull(swapchain);
		this.builder = notNull(builder);
		this.available = Semaphore.create(dev);
		this.ready = Semaphore.create(dev);
		this.fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
	}

	/**
	 *
	 * TODO - interface...
	 *
	 * frame in flight
	 * - available, ready
	 * - fence
	 * - submit(buffer)
	 * - waitReady()
	 *
	 * purpose
	 * - encapsulates active frame
	 * - handles sync
	 *
	 * PresentTaskBuilder
	 * - move here?
	 *
	 */

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
	public void render(RenderSequence seq) {
		// TODO
		fence.waitReady();
		fence.reset();

		// Acquire next frame
		final int index = swapchain.acquire(available, null);

		// Submit render task
		final Buffer buffer = builder.build(index, seq);
		submit(buffer);

		// Block until frame is rendered
		fence.waitReady();

		// Present frame
		final Queue queue = buffer.pool().queue();
		swapchain.present(queue, index, ready);
	}

	/**
	 * Submits the render task for the next frame.
	 * @param buffer Render task
	 */
	protected void submit(Buffer buffer) {
		new Work.Builder(buffer.pool())
				.add(buffer)
				.wait(available, stage)
				.signal(ready)
				.build()
				.submit(fence);
	}

	@Override
	public void destroy() {
		available.destroy();
		ready.destroy();
		fence.destroy();
	}
}
