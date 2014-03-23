package org.sarge.jove.common;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableColour extends Colour {
	/**
	 * Constructor.
	 * @param r Red
	 * @param g Green
	 * @param b Blue
	 * @param a Alpha
	 */
	public MutableColour( float r, float g, float b, float a ) {
		super( r, g, b, a );
	}

	/**
	 * Constructor given an array.
	 * @param array Colour expressed as 3 or 4 floating-point values
	 */
	public MutableColour( float[] array ) {
		super( array );
	}

	/**
	 * Copy constructor.
	 * @param col Colour to copy
	 */
	public MutableColour( Colour col ) {
		this( col.r, col.g, col.b, col.a );
	}

	/**
	 * Sets the red component.
	 * @param r Red
	 */
	public void setRed( float r ) {
		Check.isPercentile( r );
		this.r = r;
	}

	/**
	 * Sets the green component.
	 * @param g Green
	 */
	public void setGreen( float g ) {
		Check.isPercentile( g );
		this.g = g;
	}

	/**
	 * Sets the blue component.
	 * @param b Blue
	 */
	public void setBlue( float b ) {
		Check.isPercentile( b );
		this.b = b;
	}

	/**
	 * Sets the alpha component.
	 * @param a Alpha
	 */
	public void setAlpha( float a ) {
		Check.isPercentile( a );
		this.a = a;
	}

	/**
	 * Resets this colour to the given colour.
	 * @param col Colour to set
	 */
	public void set( Colour col ) {
		setRed( col.r );
		setGreen( col.g );
		setBlue( col.b );
		setAlpha( col.a );
	}

	/**
	 * Scales the <b>RGB</b> components of this colour by the given percentile.
	 * @param scale Scaling factor
	 */
	public void scale( float scale ) {
		r = scale( r, scale );
		g = scale( g, scale );
		b = scale( b, scale );
	}

	private static float scale( float f, float scale ) {
		return MathsUtil.clamp( f * scale );
	}
}
