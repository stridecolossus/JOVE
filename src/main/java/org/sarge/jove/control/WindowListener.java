package org.sarge.jove.control;

/**
 * A <i>window listener</i> is a handler for window state changes.
 * @author Sarge
 */
public interface WindowListener {
	/**
	 * State change types.
	 */
	public enum Type {
		ENTER,
		FOCUS,
		ICONIFIED,
		CLOSED
	}

	/**
	 * Notifies a window state change.
	 * @param type		State change type
	 * @param state		State
	 */
	void state(Type type, boolean state);
}
