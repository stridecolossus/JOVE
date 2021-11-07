package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Set;
import java.util.function.IntFunction;

import org.sarge.jove.control.RenderLoop.Task;
import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.common.Command.Buffer;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.core.Work;
import org.sarge.lib.util.Check;

/**
 * The <i>render task</i> encapsulates the process of rendering multiple in-flight frames.
 * <p>
 * Usage:
 * <pre>
 * 	Queue presentation = ...
 * 	List<Command.Buffer> buffers = ...
 * 	RenderLoop loop = new RenderLoop(swapchain, 2, buffers::get, presentation);
 * 	loop.run();
 * </pre>
 * @author Sarge
 */
public class RenderTask implements Task {
	/**
	 * A <i>frame</i> tracks the progress of an <i>in-flight</i> frame.
	 */
	private class Frame {
		private final Semaphore available, ready;
		private final Fence fence;

		private Frame() {
			final DeviceContext dev = swapchain.device();
			available = Semaphore.create(dev);
			ready = Semaphore.create(dev);
			fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
		}

		/**
		 * Renders the next in-flight frame.
		 */
		private void render() {
			// Wait for any previous work to complete
			fence.waitReady();

			// Retrieve next swapchain image index
			final int index = swapchain.acquire(available, null);

			// Wait on swapchain image if still in use by another frame
			active[index].fence.waitReady();
			active[index] = this;

			// Clear synchronisation
			fence.reset();

			// Render frame
			final Buffer buffer = factory.apply(index);
			new Work.Builder(buffer.pool())
					.add(buffer)
					.wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
					.signal(ready)
					.build()
					.submit(fence);

			// Present frame
			swapchain.present(presentation, index, Set.of(ready));
		}

		/**
		 * Release resources.
		 */
		private void close() {
			available.destroy();
			ready.destroy();
			fence.destroy();
		}
	}

	// Presentation
	private final Swapchain swapchain;
	private final Queue presentation;
	private final IntFunction<Buffer> factory;

	// In-flight frame state
	private final Frame[] frames;
	private final Frame[] active;
	private int current;

	/**
	 * Constructor.
	 * @param swapchain				Swapchain
	 * @param frames				Number of in-flight frames
	 * @param factory				Factory for command buffers
	 * @param presentation			Presentation queue
	 */
	public RenderTask(Swapchain swapchain, int frames, IntFunction<Buffer> factory, Queue presentation) {
		Check.oneOrMore(frames);
		this.swapchain = notNull(swapchain);
		this.presentation = notNull(presentation);
		this.factory = notNull(factory);
		this.frames = new Frame[frames];
		this.active = new Frame[swapchain.count()];
		init();
	}

	private void init() {
		Arrays.setAll(frames, ignored -> new Frame());
		Arrays.fill(active, frames[0]);
	}

	@Override
	public void execute() {
		// Render next frame
		final Frame frame = frames[current];
		frame.render();

		// Move to next in-flight frame
		if(++current >= frames.length) {
			current = 0;
		}
	}

	/**
	 * Destroys this render loop and any resources.
	 */
	public void close() {
		for(Frame f : frames) {
			f.close();
		}
	}
}
