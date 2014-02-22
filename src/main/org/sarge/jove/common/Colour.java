package org.sarge.jove.common;

import java.nio.FloatBuffer;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.Converter;
import org.sarge.lib.util.HashCodeBuilder;
import org.sarge.lib.util.ToString;

/**
 * RGBA colour.
 * @author Sarge
 */
public class Colour implements Appendable {
	/**
	 * Number of floating-point values in a colour.
	 */
	public static final int SIZE = 4;

	/**
	 * Converts a comma-delimited RGBA string to a colour.
	 */
	public static Converter<Colour> CONVERTER = new Converter<Colour>() {
		@Override
		public Colour convert( String str ) throws NumberFormatException {
			final float[] array = MathsUtil.convert( str, 4 );
			return new Colour( array );
		}
	};

	/**
	 * White.
	 */
	public static final Colour WHITE = new Colour( 1, 1, 1, 1 );

	/**
	 * Black.
	 */
	public static final Colour BLACK = new Colour( 0, 0, 0, 1 );

	protected float r, g, b, a;

	/**
	 * Constructor.
	 * @param r Red
	 * @param g Green
	 * @param b Blue
	 * @param a Alpha
	 */
	public Colour( float r, float g, float b, float a ) {
		Check.isPercentile( r );
		Check.isPercentile( g );
		Check.isPercentile( b );
		Check.isPercentile( a );

		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	/**
	 * Constructor given an array.
	 * @param array Colour expressed as 3 or 4 floating-point values
	 */
	public Colour( float[] array ) {
		Check.notNull( array );
		if( ( array.length != 3 ) && ( array.length != 4 ) ) throw new IllegalArgumentException( "Expected 3 or 4 length array" );
		this.r = array[ 0 ];
		this.g = array[ 1 ];
		this.b = array[ 2 ];
		if( array.length == 3 ) {
			this.a = 1;
		}
		else {
			this.a = array[ 3 ];
		}
	}

	public float getRed() {
		return r;
	}

	public float getGreen() {
		return g;
	}

	public float getBlue() {
		return b;
	}

	public float getAlpha() {
		return a;
	}

	/**
	 * Fades a colour.
	 * @param fade Fade factors
	 * @return Faded colour
	 */
	public Colour fade( Colour fade ) {
		return new Colour(
			r * fade.r,
			g * fade.g,
			b * fade.b,
			a * fade.a
		);
	}


	@Override
	public void append( FloatBuffer buffer ) {
		buffer.put( r );
		buffer.put( g );
		buffer.put( b );
		buffer.put( a );
	}

	/**
	 * Converts this colour to an array.
	 * @param array Colour array
	 */
	public void toArray( float[] array ) {
		array[ 0 ] = r;
		array[ 1 ] = g;
		array[ 2 ] = b;
		array[ 3 ] = a;
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( obj == null ) return false;
		if( obj instanceof Colour ) {
			final Colour col = (Colour) obj;
			if( !MathsUtil.isEqual( this.r, col.r ) ) return false;
			if( !MathsUtil.isEqual( this.g, col.g ) ) return false;
			if( !MathsUtil.isEqual( this.b, col.b ) ) return false;
			if( !MathsUtil.isEqual( this.a, col.a ) ) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return HashCodeBuilder.hashCode( this );
	}

	@Override
	public String toString() {
		return ToString.toString( r, g, b, a );
	}
}
