package org.sarge.jove.scene;

import java.util.function.Supplier;

import org.sarge.jove.scene.RenderLoop.Task;
import org.sarge.lib.util.Check;

/**
 * The <i>render task</i> encapsulates the process of rendering multiple concurrent <i>in-flight</i> frames.
 * @author Sarge
 */
public class RenderTask implements Task {
	/**
	 * A <i>frame</i> tracks the progress of an <i>in-flight</i> frame.
	 */
	public interface Frame {
		/**
		 * Renders the next in-flight frame.
		 */
		void render();

		/**
		 * Releases resources.
		 */
		void destroy();
	}

	private final Frame[] frames;
	private int current;

	/**
	 * Constructor.
	 * @param count				Number of in-flight frames
	 * @param factory			Frame factory
	 */
	public RenderTask(int count, Supplier<? extends Frame> factory) {
		Check.oneOrMore(count);
		this.frames = new Frame[count];
		for(int n = 0; n < count; ++n) {
			frames[n] = factory.get();
		}
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
	 * Destroys this render task and any resources.
	 */
	public void close() {
		for(Frame f : frames) {
			f.destroy();
		}
	}
}
