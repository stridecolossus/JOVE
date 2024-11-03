package org.sarge.jove.scene.graph;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.scene.volume.*;

/**
 *
 * @author Sarge
 */
public class LocalVolume {
	private static final Volume NONE = new EmptyVolume();

	private Volume vol;

	LocalVolume() {
	}

	/**
	 * @return Bounding volume
	 */
	public Volume volume() {
		if(vol == null) {
			return NONE;
		}
		else {
			return vol;
		}
	}

	/**
	 * Sets a literal bounding volume.
	 * @param vol Bounding volume
	 */
	public void set(Volume vol) {
		this.vol = requireNonNull(vol);
	}

	/**
	 *
	 * @author Sarge
	 */
	public static class UpdateVisitor {

		public void update(Node node) {

		}
	}
}
