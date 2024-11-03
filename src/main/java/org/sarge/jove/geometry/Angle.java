package org.sarge.jove.geometry;

import org.sarge.jove.util.MathsUtility;

/**
 * An <i>angle</i> specifies a rotation angle and the associated sine-cosine values.
 * @author Sarge
 */
public class Angle {
	private float angle;
	private Cosine cosine;

	/**
	 * Constructor.
	 * @param angle Angle (radians)
	 */
	public Angle(float angle) {
		this.angle = angle;
	}

	/**
	 * @return Angle (radians)
	 */
	public float angle() {
		return angle;
	}

	/**
	 * Sets this angle.
	 * @param angle Angle (radians)
	 */
	public void set(float angle) {
		this.angle = angle;
		this.cosine = null;
	}

	/**
	 * @return Sine-cosine of this angle
	 * @see #provider()
	 */
	public Cosine cosine() {
		if(cosine == null) {
			cosine = this.provider().cosine(angle);
		}
		return cosine;
	}

	/**
	 * @return Cosine function
	 * @see Cosine.Provider#DEFAULT
	 */
	protected Cosine.Provider provider() {
		return Cosine.Provider.DEFAULT;
	}

	@Override
	public int hashCode() {
		return Float.hashCode(angle);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Angle that) &&
				MathsUtility.isApproxEqual(this.angle, that.angle);
	}

	@Override
	public String toString() {
		return MathsUtility.format(angle);
	}
}
