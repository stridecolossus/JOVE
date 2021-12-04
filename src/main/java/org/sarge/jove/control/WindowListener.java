package org.sarge.jove.control;

/**
 * A <i>window listener</i> is a handler for window state changes.
 * @author Sarge
 */
public interface WindowListener {
	/**
	 * Notifies the mouse cursor has entered or left the window.
	 * @param enter Whether cursor has entered or left the window
	 */
	void cursor(boolean enter);

	/**
	 * Notifies that the window has gained or lost focus.
	 * @param focus Whether gained or lost focus
	 */
	void focus(boolean focus);

	/**
	 * Notifies that the window has been minimised (or iconified) or has been restored.
	 * @param min Whether window is minimised
	 */
	void minimised(boolean min);

	/**
	 * Notifies that the window has been resized.
	 * @param width			Width
	 * @param height		Height
	 */
	void resize(int width, int height);

	/**
	 * Skeleton implementation that silently consumes all events.
	 */
	abstract class AbstractWindowListener implements WindowListener {
		@Override
		public void cursor(boolean enter) {
			// Ignored
		}

		@Override
		public void focus(boolean focus) {
			// Ignored
		}

		@Override
		public void minimised(boolean min) {
			// Ignored
		}

		@Override
		public void resize(int width, int height) {
			// Ignored
		}
	}
}
