package org.sarge.jove.scene.particle;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.volume.Bounds;
import org.sarge.jove.util.*;
import org.sarge.lib.element.Element;

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
		this.range = Vector.between(box.min(), box.max()).multiply(MathsUtil.HALF);
		this.randomiser = notNull(randomiser);
	}

	@Override
	public Point position() {
		final Vector vec = randomiser.vector().multiply(range);
		return new Point(vec).add(centre);
	}

	/**
	 * Loads a box position factory from the given element.
	 * @param e					Element
	 * @param randomiser		Randomiser
	 * @return Box position factory
	 */
	public static PositionFactory load(Element e, Randomiser randomiser) {
		final Point min = e.child("min").text().transform(Point.CONVERTER);
		final Point max = e.child("max").text().transform(Point.CONVERTER);
		final Bounds bounds = new Bounds(min, max);
		return new BoxPositionFactory(bounds, randomiser);
	}
}
