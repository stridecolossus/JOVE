package org.sarge.jove.platform.vulkan.present;

import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.Arrays;

import org.sarge.jove.common.TransientObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

/**
 * The <i>frame iterator</i> cycles through an array of in-flight frames.
 * @author Sarge
 */
public class FrameIterator implements TransientObject {
	private final FrameState[] frames;
	private int next;

	/**
	 * Constructor.
	 * @param device		Logical device
	 * @param count			Number of frames
	 */
	public FrameIterator(LogicalDevice device, int count) {
		requireOneOrMore(count);
		this.frames = new FrameState[count];
		Arrays.setAll(this.frames, index -> create(index, device));
	}

	/**
	 * Creates a new frame state instance.
	 * @param index		Frame index
	 * @param device	Logical device
	 * @return Frame state
	 * @see FrameState#create(int, LogicalDevice)
	 */
	protected FrameState create(int index, LogicalDevice device) {
		return FrameState.create(index, device);
	}

	/**
	 * @return Next frame
	 */
	public FrameState next() {
		if(next == frames.length) {
			next = 0;
		}
		return frames[next++];
	}

	@Override
	public void destroy() {
		for(FrameState f : frames) {
			f.destroy();
		}
	}
}
