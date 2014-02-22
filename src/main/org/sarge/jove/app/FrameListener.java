package org.sarge.jove.app;

/**
 * Listener for frame update events.
 * @author Sarge
 */
public interface FrameListener {
	/**
	 * Notifies a new frame has been rendered.
	 * @param time			Frame time
	 * @param elapsed		Time since last frame (ms)
	 */
	void update( long time, long elapsed );
}
