package org.sarge.jove.platform.vulkan;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;

import org.sarge.jove.platform.Resource;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;

/**
 * A <i>frame state</i> represents the synchronisation state of an <i>in-flight</i> frame.
 * @author Sarge
 */
public class FrameState extends AbstractEqualsObject {
	private final PointerHandle available;
	private final PointerHandle finished;
	private final Fence fence;
	private final Fence.Group group;

	/**
	 * Constructor.
	 * @param dev Logical device
	 */
	protected FrameState(LogicalDevice dev) {
		this.available = dev.semaphore();
		this.finished = dev.semaphore();
		this.fence = Fence.create(dev, true);
		this.group = new Fence.Group(dev, Arrays.asList(fence));
	}

	/**
	 * @return Semaphore indicating a frame is available
	 */
	public PointerHandle available() {
		return available;
	}

	/**
	 * @return Semaphore indicating the next frame has been rendered
	 */
	public PointerHandle finished() {
		return finished;
	}

	/**
	 * @return Fence
	 */
	public Fence fence() {
		return fence;
	}

	/**
	 * Destroys resources.
	 */
	private void destroy() {
		destroy(available);
		destroy(finished);
		destroy(fence);
	}

	/**
	 * Destroys a resource if present.
	 * @param handle Resource to destroy
	 */
	private static void destroy(PointerHandle handle) {
		if(handle != null) {
			handle.destroy();
		}
	}

	/**
	 * A <i>frame state tracker</i> manages in-flight frame rendering.
	 */
	interface FrameTracker extends Resource {
		/**
		 * Waits until this tracker is ready to start the next frame.
		 * @return Frame state
		 */
		FrameState waitReady();

		/**
		 * Submits the next frame.
		 * @return Frame state
		 */
		FrameState submit(Command.Buffer cmd);

		/**
		 * Default implementation.
		 */
		class DefaultFrameTracker extends AbstractObject implements FrameTracker {
			private final WorkQueue queue;
			private final FrameState[] frames;

			private int index;

			/**
			 * Constructor.
			 * @param dev			Device
			 * @param frames 		Number of in-flight frames
			 * @param queue			Graphics queue
			 */
			public DefaultFrameTracker(LogicalDevice dev, int frames, WorkQueue queue) {
				this.queue = notNull(queue);
				this.frames = create(frames, dev);
			}

			/**
			 * @return Index of the current frame
			 */
			public int index() {
				return index;
			}

			/**
			 * Initialises the in-flight frames.
			 */
			private static FrameState[] create(int num, LogicalDevice dev) {
				final FrameState[] frames = new FrameState[num];
				Arrays.setAll(frames, ignored -> new FrameState(dev));
				return frames;
			}

			@Override
			public FrameState waitReady() {
				final Fence.Group group = frames[index].group;
				group.wait(true);
				group.reset();
				return frames[index];
			}

			@Override
			public FrameState submit(Command.Buffer cmd) {
				// Create work entry for next frame
				final FrameState frame = frames[index];
				queue.work()
					.add(cmd)
					.wait(frame.available)
					.wait(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.signal(frame.finished)
					.fence(frame.fence)
					.build()
					.submit();

				// Move to next frame
				++index;
				if(index >= frames.length) {
					index = 0;
				}

				return frame;
			}

			@Override
			public void destroy() {
				for(FrameState f : frames) {
					f.destroy();
				}
			}
		}
	}
}
