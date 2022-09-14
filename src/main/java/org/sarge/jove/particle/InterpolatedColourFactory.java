package org.sarge.jove.particle;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Colour;
import org.sarge.jove.util.Interpolator;
import org.sarge.lib.util.Element;

/**
 * The <i>interpolated colour factory</i> generates colours by interpolating over a range.
 * @author Sarge
 */
public class InterpolatedColourFactory implements ColourFactory {
	private final Colour start, end;
	private final Interpolator interpolator;

	/**
	 * Constructor.
	 * @param start				Starting colour
	 * @param end				End colour
	 * @param interpolator		Interpolator
	 */
	public InterpolatedColourFactory(Colour start, Colour end, Interpolator interpolator) {
		this.start = notNull(start);
		this.end = notNull(end);
		this.interpolator = notNull(interpolator);
	}

	public InterpolatedColourFactory(Colour start, Colour end) {
		this(start, end, Interpolator.LINEAR); // TODO - identity
	}

	@Override
	public Colour colour(float t) {
		return start.interpolate(end, interpolator.interpolate(t));
	}

	/**
	 * Loads an interpolated colour factory.
	 * @param e Element
	 * @return Interpolated colour factory
	 */
	public static ColourFactory load(Element e) {
		final Colour start = e.child("start").text().transform(Colour.CONVERTER);
		final Colour end = e.child("end").text().transform(Colour.CONVERTER);
		return new InterpolatedColourFactory(start, end); // TODO - interpolator
	}
}
