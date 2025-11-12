package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.Arrays;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.platform.vulkan.render.Swapchain.SwapchainInvalidated;

/**
 * The <i>Vulkan render task</i> renders and presents a frame to the swapchain.
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
public class VulkanRenderTask implements Runnable, TransientObject {
	private final FrameComposer composer;
	//private final SwapchainAdapter adapter;
	private final Swapchain swapchain;
	private final FrameBuffer.Group group;
	private final VulkanFrame[] frames;
	private int next;

	/**
	 * Constructor.
	 * @param swapchain			Swapchain
	 * @param group				Frame buffers
	 * @param composer			Composer for the render sequence
	 */
	public VulkanRenderTask(Swapchain swapchain, FrameBuffer.Group group, FrameComposer composer) {
		this.composer = requireNonNull(composer);
		this.swapchain = requireNonNull(swapchain);
		this.group = requireNonNull(group);
		this.frames = frames(swapchain);
	}

	private static VulkanFrame[] frames(Swapchain swapchain) {
		final var device = swapchain.device();
		final int number = swapchain.attachments().size();
		final var frames = new VulkanFrame[number];
		Arrays.setAll(frames, _ -> VulkanFrame.create(device));
		return frames;
	}

	@Override
	public void run() {
		try {
			frame();
		}
		catch(SwapchainInvalidated e) {
			// TODO
//			adapter.recreate();
			// group.recreate()
		}
	}

	private void frame() {
		// Select next in-flight frame
		final VulkanFrame frame = frames[next];
		if(++next >= frames.length) {
			next = 0;
		}

		// Acquire frame buffer
		//final Swapchain swapchain = adapter.swapchain();
		final int index = frame.acquire(swapchain);
		final FrameBuffer buffer = group.get(index);

		// Render frame
		final Command.Buffer sequence = composer.compose(index, buffer);
		frame.render(sequence);

		// Present frame
		frame.present(sequence, index, swapchain);
	}

	@Override
	public void destroy() {
		for(VulkanFrame f : frames) {
			f.destroy();
		}
	}
}
