package org.sarge.jove.scene.particle;

import static java.util.Objects.requireNonNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.volume.Bounds;
import org.sarge.jove.util.*;

/**
 * The <i>box position factory</i> generates particles randomly within a box (or rectangle).
 * @author Sarge
 */
public class BoxPositionFactory implements PositionFactory {
	private final Point centre;
	private final Vector range;
	private final Randomiser randomiser;

	/**
	 * Constructor.
	 * @param box				Box
	 * @param randomiser		Randomiser
	 */
	public BoxPositionFactory(Bounds box, Randomiser randomiser) {
		this.centre = box.centre();
		this.range = Vector.between(box.min(), box.max()).multiply(MathsUtility.HALF);
		this.randomiser = requireNonNull(randomiser);
	}

	@Override
	public Point position() {
		// TODO - revert to component-wise multiply() on vector?
		final Vector vec = randomiser.vector();
		final float x = vec.x * range.x;
		final float y = vec.y * range.y;
		final float z = vec.z * range.z;
		return centre.add(new Vector(x, y, z));
	}
}
