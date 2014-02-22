package org.sarge.jove.widget;

/**
 * Widget background.
 * @author Sarge
 */
public interface Background {
	/**
	 * Transparent background.
	 */
	Background TRANSPARENT = new Background() {
		public void render() {
			// Does nowt
		}
	};
	
	/**
	 * Renders this background.
	 */
	void render();
}
