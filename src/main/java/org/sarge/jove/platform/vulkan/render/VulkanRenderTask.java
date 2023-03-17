package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;

import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.scene.core.RenderLoop;

/**
 * The <i>Vulkan render task</i> is used to render and present a frame to the swapchain.
 * <p>
 * The intention of this class is to use the {@link #render()} method as a task for the {@link RenderLoop}.
 * <p>
 * @author Sarge
 */
public class VulkanRenderTask {
	private final List<FrameBuffer> buffers;
	private final FrameSelector selector;
	private final FrameComposer composer;
	private final Swapchain swapchain;

	/**
	 * Constructor.
	 * @param buffers		Frame buffers
	 * @param selector		Selects the next frame to render
	 * @param composer		Composer for the render task
	 * @param swapchain		Swapchain
	 */
	public VulkanRenderTask(List<FrameBuffer> buffers, FrameSelector selector, FrameComposer composer, Swapchain swapchain) {
		this.buffers = List.copyOf(buffers);
		this.selector = notNull(selector);
		this.composer = notNull(composer);
		this.swapchain = notNull(swapchain);
	}

	/**
	 * Renders the next frame.
	 */
	public void render() {
		// Acquire next frame
		final VulkanFrame frame = selector.frame();
		final int index = frame.acquire(swapchain);
		final FrameBuffer fb = buffers.get(index);

		// Compose render task
		final Command.Buffer render = composer.compose(fb);

		// Present rendered frame
		frame.present(render, swapchain);
	}
}
