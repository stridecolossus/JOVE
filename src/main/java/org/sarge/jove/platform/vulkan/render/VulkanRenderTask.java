package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.Command;
import org.sarge.jove.scene.core.RenderLoop;
import org.sarge.lib.util.Check;

/**
 * The <i>Vulkan render task</i> is used to render and present a frame to the swapchain.
 * <p>
 * This class orchestrates the components that collaborate to render a frame as follows:
 * <ol>
 * <li>Select the next in-flight frame to render</li>
 * <li>Acquire the next frame buffer to be rendered from the swapchain</li>
 * <li>Invoke the frame composer to build the render task for the selected frame and buffer</li>
 * <li>Render the frame</li>
 * <li>Present the completed frame to the swapchain</li>
 * </ol>
 * <p>
 * The {@link #render()} method is generally used as the task for the {@link RenderLoop}.
 * <p>
 * @author Sarge
 */
public class VulkanRenderTask implements TransientObject {
	private final List<FrameBuffer> buffers;
	private final VulkanFrame[] frames;
	private final FrameComposer composer;
	private final Swapchain swapchain;
	private int next;

	/**
	 * Constructor.
	 * @param buffers		Frame buffers
	 * @param frames		In-flight frames
	 * @param composer		Composer for the render task
	 * @param swapchain		Swapchain
	 * @throws IllegalArgumentException if {@link #buffers} or {@link #frames} is empty
	 */
	public VulkanRenderTask(List<FrameBuffer> buffers, VulkanFrame[] frames, FrameComposer composer, Swapchain swapchain) {
		Check.notEmpty(buffers);
		Check.notEmpty(frames);
		this.buffers = List.copyOf(buffers);
		this.frames = Arrays.copyOf(frames, frames.length);
		this.composer = notNull(composer);
		this.swapchain = notNull(swapchain);
	}

	/**
	 * Renders the next frame.
	 */
	public void render() {
		// Select next frame
		final VulkanFrame frame = frames[next];

		// Acquire next frame buffer
		final int index = frame.acquire(swapchain);
		final FrameBuffer fb = buffers.get(index);

		// Compose render task
		final Command.Buffer render = composer.compose(next, fb);

		// Present rendered frame
		frame.present(render);

		// Move to next frame
		if(++next >= frames.length) {
			next = 0;
		}
	}

	@Override
	public void destroy() {
		for(var frame : frames) {
			frame.destroy();
		}
	}
}
