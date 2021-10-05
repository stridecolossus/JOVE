package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.IntFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.vulkan.VkFenceCreateFlag;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.Work;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.lib.util.Check;

/**
 * A <i>frame runner</i> encapsulates a render loop.
 * <p>
 * The loop is comprised of the following steps:
 * <ol>
 * <li>acquire the next frame to be rendered</li>
 * <li>invoke {@link Frame#render(FrameState, View)} to render the frame</li>
 * <li>present the rendered frame</li>
 * <li>update the application logic via {@link Frame#update()}</li>
 * </ol>
 * <p>
 * A {@link Frame} is implemented by the client to render to a swapchain image and to update application logic (such as input event handling).
 * <p>
 * The {@link FrameState} provides basic synchronisation across the render loop:
 * <ul>
 * <li><i>ready</i> signals that a swapchain image is ready to be rendered</li>
 * <li><i>finished</i> is signalled when a frame render has completed</li>
 * <li>the <i>fence</i> controls access to in-flight swapchain images</li>
 * </ul>
 * <p>
 * Notes:
 * <ul>
 * <li>the convenience {@link FrameState#render(Command.Buffer)} can be used by a frame implementation to render a command sequence</li>
 * <li>this implementation assumes the render loop will be running on the main thread (in particular note that {@link Desktop#poll()} <b>must</b> be invoked on the main thread)</li>
 * <li>{@link #run()} is a blocking method (see {@link #stop})</li>
 * </ul>
 * <p>
 * Example:
 * <pre>
 *  // Create a rendering command sequence
 *  Command.Buffer seq = ...
 *
 *  // Create frame factory
 *  IntFunction<Frame> factory = n -> new Frame() {
 *      public void render(FrameState state, View view) {
 *          state.render(seq);
 *      }
 *
 *      public void update() {
 *          desktop.poll();
 *      }
 *  };
 *
 *  // Create runner
 *  Runner runner = new Runner(swapchain, 2, factory, queue);
 *  runner.run();
 *
 *  ...
 *
 *  // Stop runner
 *  runner.stop();
 * </pre>
 * @author Sarge
 */
public class Runner {
	/**
	 * A <i>frame state</i> tracks the synchronisation state of an <i>in-flight</i> frame during rendering.
	 */
	public static final class FrameState {
		private final Semaphore ready, finished;
		private final Fence fence;

//		private long start, elapsed;

		/**
		 * Constructor.
		 * @param dev Logical device
		 */
		private FrameState(LogicalDevice dev) {
			this.ready = dev.semaphore();
			this.finished = dev.semaphore();
			this.fence = Fence.create(dev, VkFenceCreateFlag.VK_FENCE_CREATE_SIGNALED_BIT);
		}

		/**
		 * @return Semaphore that signals this frame has been acquired from the swapchain and is ready for rendering
		 */
		public Semaphore ready() {
			return ready;
		}

		/**
		 * @return Semaphore that signals that this frame has been rendered
		 */
		public Semaphore finished() {
			return finished;
		}

		/**
		 * @return Fence used to synchronise access to this frame
		 */
		public Fence fence() {
			return fence;
		}

		/**
		 * Waits for this frame to be ready.
		 */
		private void waitFence() {
			fence.waitReady();
			fence.reset();
		}

		/*
		private static final long WAIT = 1000 / 60;

		private void update() {
			final long now = System.currentTimeMillis();
			elapsed = now - start;
			start = now;

			///////

			final long wait = 3 * (WAIT - elapsed);
			if(wait > 0) {
//				System.out.println("elapsed="+elapsed+" wait="+wait);
				try {
					Thread.sleep(wait);
				}
				catch(InterruptedException e) {
					e.printStackTrace();
				}
			}
		}

		public long elapsed() {
			return elapsed;
		}
		*/

		/**
		 * Helper - Creates a work submission to render to this frame.
		 * @param buffer Render command sequence
		 * @return Render work
		 * @see Work#submit(Fence)
		 */
		public Work create(Command.Buffer buffer) {
			return new Work.Builder()
					.add(buffer)
					.wait(ready, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
					.signal(finished)
					.build();
		}

		/**
		 * Helper - Creates and submits the given render sequence to this frame.
		 * @param buffer Render command sequence
		 * @see #work(Command.Buffer)
		 */
		public void render(Command.Buffer buffer) {
			final Work work = create(buffer);
			work.submit(fence, null); // TODO - lib!!!
		}

		/**
		 * Releases the synchronisation resources of this frame.
		 */
		private void destroy() {
			ready.destroy();
			finished.destroy();
			fence.destroy();
		}
	}

	/**
	 * A <i>frame</i> renders to the next swapchain image.
	 */
	public interface Frame {
		/**
		 * Renders the next frame.
		 * <p>
		 * The {@link FrameState#render(Command.Buffer)} helper can be used to submit a rendering command sequence to this frame.
		 * <p>
		 * @param state			Frame state
		 * @param view			Swapchain view
		 */
		void render(FrameState state, View view);

		/**
		 * Updates state after a frame has been rendered.
		 * @return Whether to continue execution of this runner
		 */
		boolean update();
	}

	// Configuration
	private final Swapchain swapchain;
	private final Queue queue;
	private final Frame[] frames;

	// State
	private final FrameState[] states;
	private final AtomicBoolean running = new AtomicBoolean();
	private int current;

	/**
	 * Constructor.
	 * @param swapchain		Swapchain
	 * @param size			Number of in-flight frames (one-or-more)
	 * @param factory		Factory for frames to be rendered
	 * @param queue			Presentation queue
	 */
	public Runner(Swapchain swapchain, int size, IntFunction<Frame> factory, Queue queue) {
		Check.oneOrMore(size);
		this.swapchain = notNull(swapchain);
		this.queue = notNull(queue);
		this.frames = IntStream.range(0, swapchain.views().size()).mapToObj(factory::apply).toArray(Frame[]::new);
		this.states = Stream.generate(() -> new FrameState((LogicalDevice) swapchain.device())).limit(size).toArray(FrameState[]::new);
		// TODO - cast! maybe pass in a factory?
	}

	/**
	 * @return Whether currently running
	 */
	protected boolean isRunning() {
		return running.get();
	}

	/**
	 * Runs this frame render loop on the current thread.
	 */
	public final void start() {
//		//
//		final long now = System.currentTimeMillis();
//		for(FrameState state : states) {
//			state.start = now;
//		}

		// Start running
		running.set(true);

		// Render loop
		while(running.get()) {
			frame();
		}
	}

	/**
	 * Renders the next frame.
	 */
	protected void frame() {
		// Start next in-flight frame
		final FrameState state = states[current];
		state.waitFence();
//		state.update();

		// Acquire next swapchain image
		// TODO - using same fence for both cases, does this work? need 1. for frame itself and 2. for image being rendered (pass null to acquire)?
		final int index = swapchain.acquire(state.ready(), state.fence());
		state.waitFence();

		// Render frame
		final Frame frame = frames[index];
		final View view = swapchain.views().get(index);
		frame.render(state, view);

		// Present frame
		swapchain.present(queue, Set.of(state.finished()));

		// Update application logic
		final boolean result = frame.update();
		if(!result) {
			stop();
		}

		// Select next frame
		if(++current >= states.length) {
			current = 0;
		}
	}

	/**
	 * Gracefully stops this runner.
	 */
	public final void stop() {
		running.set(false);
	}

	/**
	 * Releases the frame-tracker resources for this runner.
	 * @throws IllegalStateException if still running
	 */
	public void destroy() {
		if(running.get()) throw new IllegalStateException("Cannot destroy when running");

		for(FrameState f : states) {
			f.destroy();
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("running", running.get()).build();
	}
}

