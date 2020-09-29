package org.sarge.jove.scene;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Matrix;
import org.sarge.lib.util.AbstractObject;

/**
 * A <i>viewport</i> defines the view window dimensions and projection.
 * @author Sarge
 */
public final class Viewport extends AbstractObject {
	private final Rectangle rect;
	private final float near, far;
	private final Projection projection;
	private final float width, height;

	/**
	 * Constructor.
	 * @param rect				Viewport rectangle
	 * @param near				Near plane distance
	 * @param far				Far plane distance
	 * @param projection		View projection
	 */
	public Viewport(Rectangle rect, float near, float far, Projection projection) {
		if(far < near) throw new IllegalArgumentException("Invalid near/far plane distance");
		this.rect = notNull(rect);
		this.near = zeroOrMore(near);
		this.far = zeroOrMore(far);
		this.projection = notNull(projection);
		this.height = projection.height(rect.size());
		this.width = height * rect.size().ratio();
	}

	/**
	 * @return Viewport rectangle
	 */
	public Rectangle rectangle() {
		return rect;
	}

	/**
	 * @return Near plane distance
	 */
	public float near() {
		return near;
	}

	/**
	 * @return Far plane distance
	 */
	public float far() {
		return far;
	}

	/**
	 * @return View projection
	 */
	public Projection projection() {
		return projection;
	}

	/**
	 * @return Viewport width
	 */
	public float width() {
		return width;
	}

	/**
	 * @return Viewport height
	 */
	public float height() {
		return height;
	}

	/**
	 * @return Projection matrix for this viewport
	 */
	public Matrix matrix() {
		return projection.matrix(near, far, rect.size());
	}
}
