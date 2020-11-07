package org.sarge.jove.scene;

import static org.sarge.jove.util.Check.notNull;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.MathsUtil;

public class OrbitCameraController {
	private final Camera cam;
	private final Dimensions dim;

	/**
	 * Constructor.
	 * @param cam
	 * @param dim
	 */
	public OrbitCameraController(Camera cam, Dimensions dim) {
		this.cam = notNull(cam);
		this.dim = notNull(dim);
	}

	private float radius = 1;	// TODO - scale?

	public void update(float x, float y) {
		final float dx = x / dim.width() * MathsUtil.TWO_PI;
		final float dz = y / dim.height() * MathsUtil.TWO_PI;

		final Point pos = new Point(
				MathsUtil.sin(dx) * radius,
				0,
				MathsUtil.cos(dz) * radius
		);

		cam.look(pos);
	}

	public void radius(float radius) {
		this.radius = radius;
	}

	public void zoom(float inc) {
		radius += inc;
	}
}
