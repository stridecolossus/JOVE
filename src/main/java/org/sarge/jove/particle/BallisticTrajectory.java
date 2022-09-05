package org.sarge.jove.particle;

import org.sarge.jove.geometry.*;
import org.sarge.jove.util.MathsUtil;

public class BallisticTrajectory {
	private final float sin, cos;

	// https://en.wikipedia.org/wiki/Projectile_motion#Velocity

	/**
	 * Constructor.
	 * @param angle
	 */
	public BallisticTrajectory(float angle, float speed) {
		this.sin = MathsUtil.sin(angle) * speed;
		this.cos = MathsUtil.sin(angle) * speed;
	}

	public Point position(float t, float g) {
		final float x = cos * t;
		final float y = sin * t - MathsUtil.HALF * g * t * t;
		return new Point(x, y, 0);
	}

	public Vector vector(float t, float g) {
		final float x = cos;
		final float y = sin - g * t;
		return new Vector(x, y, 0);
	}
}
