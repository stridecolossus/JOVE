package org.sarge.jove.platform.vulkan.present;

import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.Arrays;

import org.sarge.jove.common.AbstractTransientObject;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;

/**
 * The <i>frame iterator</i> cycles through an array of in-flight frames.
 * The size of the array is usually the same as the number of swapchain attachments.
 * @see FrameState
 * @author Sarge
 */
public class FrameIterator extends AbstractTransientObject {
	private final FrameState[] frames;
	private int next;

	/**
	 * Constructor.
	 * @param device		Logical device
	 * @param count			Number of in-flight frames
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
	protected void release() {
		for(FrameState f : frames) {
			f.destroy();
		}
	}
}
