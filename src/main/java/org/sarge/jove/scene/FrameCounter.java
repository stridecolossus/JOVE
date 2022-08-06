package org.sarge.jove.scene;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.render.FrameProcessor;

/**
 * A <i>frame counter</i> is an adapter for a render task that also tracks frame completion events and FPS.
 * @author Sarge
 */
public class FrameCounter implements FrameProcessor.Listener {
	private static final long SECOND = TimeUnit.SECONDS.toMillis(1);

	private long end;
	private int count;
	private int fps;

	/**
	 * @return Number of rendered frames during the current interval
	 */
	public int count() {
		return count;
	}

	/**
	 * @return Frames-per-second
	 */
	public int fps() {
		return fps;
	}

	@Override
	public void frame(long time, long elapsed) {
		if(time > end) {
			fps = count;
			count = 1;
			end = time + SECOND;
		}
		else {
			++count;
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("count", count)
				.append("FPS", fps)
				.build();
	}
}
