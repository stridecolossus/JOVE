package org.sarge.jove.scene;

import java.time.Instant;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.control.Frame;

/**
 * A <i>frame counter</i> is an adapter for a render task that also tracks frame completion events and FPS.
 * @author Sarge
 */
public class FrameCounter implements Frame.Listener {
	private Instant next = Instant.EPOCH;
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
	public void completed(Frame frame) {
		++count;
		final Instant time = frame.start();
		if(time.isAfter(next)) {
			fps = count;
			count = 1;
			next = time.plusSeconds(1);
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
