package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

/**
 * The <i>Vulkan render task</i> is used to render and present a frame to the swapchain.
 * <p>
 * This class orchestrates the components that collaborate to render a frame as follows:
 * <ol>
 * <li>Select the next in-flight frame to render</li>
 * <li>Acquire the next frame buffer to be rendered from the swapchain</li>
 * <li>Compose the render task for the selected frame and buffer</li>
 * <li>Submit the render task</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * If the acquire or present steps fail due to a {@link SwapchainInvalidated} the swapchain and frame buffers are recreated by {@link SwapchainAdapter#recreate()}.
 * <p>
 * The {@link #render()} method is generally used as the task for the rendering loop.
 * <p>
 * @author Sarge
 */
public class VulkanRenderTask implements TransientObject {
	private final FrameComposer composer;
	private final SwapchainAdapter adapter;
	private final VulkanFrame[] frames;
	private int next;

	/**
	 * Constructor.
	 * @param composer		Composer for the render task
	 * @param adapter		Swapchain adapter
	 * @param frames		Frame trackers
	 */
	public VulkanRenderTask(FrameComposer composer, SwapchainAdapter adapter, VulkanFrame[] frames) {
		this.composer = requireNonNull(composer);
		this.adapter = requireNonNull(adapter);
		this.frames = Arrays.copyOf(frames, frames.length);
	}

	/**
	 * Renders the next frame.
	 */
	public void render() {
		try {
			frame();
		}
		catch(SwapchainInvalidated e) {
			adapter.recreate();
		}
	}

	private void frame() {
		// Select next in-flight frame
		final VulkanFrame frame = frames[next];
		if(++next >= frames.length) {
			next = 0;
		}

		// Acquire frame buffer
		final Swapchain swapchain = adapter.swapchain();
		final int index = frame.acquire(swapchain);
		final FrameBuffer buffer = adapter.buffer(index);

		// Build and render task and present
		final Command.CommandBuffer render = composer.compose(index, buffer);
		frame.present(render, index, swapchain);
	}

	@Override
	public void destroy() {
		for(VulkanFrame f : frames) {
			f.destroy();
		}
	}
}
