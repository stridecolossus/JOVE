package org.sarge.jove.common;

import java.nio.FloatBuffer;

import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.ToString;

/**
 * Texture coordinates.
 * @author Sarge
 */
public class TextureCoord implements Bufferable {
	public static final int SIZE = 2;

	private final float s, t;

	/**
	 * Origin constructor.
	 */
	public TextureCoord() {
		this( 0, 0 );
	}

	/**
	 * Constructor.
	 * @param s
	 * @param t
	 */
	public TextureCoord( float s, float t ) {
		this.s = s;
		this.t = t;
	}

	/**
	 * Array constructor.
	 * @param array Texture coordinates as an array
	 */
	public TextureCoord( float[] array ) {
		if( array.length != 2 ) throw new IllegalArgumentException( "Expected texture coord array" );
		this.s = array[ 0 ];
		this.t = array[ 1 ];
	}

	@Override
	public int getComponentSize() {
		return SIZE;
	}

	@Override
	public void append( FloatBuffer buffer ) {
		buffer.put( s );
		buffer.put( t );
	}

	@Override
	public boolean equals( Object obj ) {
		if( obj == this ) return true;
		if( obj == null ) return false;
		if( obj instanceof TextureCoord ) {
			final TextureCoord coords = (TextureCoord) obj;
			if( !MathsUtil.isEqual( s, coords.s ) ) return false;
			if( !MathsUtil.isEqual( t, coords.t ) ) return false;
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public String toString() {
		return ToString.toString( s, t );
	}
}
