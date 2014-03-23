package org.sarge.jove.material;

import java.nio.FloatBuffer;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.util.BufferFactory;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Point-sprite render property.
 * @author Sarge
 */
public class PointSpriteProperty implements RenderProperty {
	private final FloatBuffer attenuation;
	private final float threshold;

	/**
	 * Constructor.
	 * @param attenuation		Attenuation, array of 3 floats
	 * @param threshold			Fade threshold
	 */
	public PointSpriteProperty( float[] attenuation, float threshold ) {
		Check.notEmpty( attenuation );
		if( attenuation.length != 3 ) throw new IllegalArgumentException( "Expected array of 3 floats" );
		Check.zeroOrMore( threshold );

		final FloatBuffer fb = BufferFactory.createFloatBuffer( 4 );
		fb.put( attenuation[ 0 ] );
		fb.put( attenuation[ 1 ] );
		fb.put( attenuation[ 2 ] );
		fb.put( 1 );

		this.attenuation = fb.asReadOnlyBuffer();
		this.threshold = threshold;
	}

	/**
	 * Convenience constructor for default configuration.
	 */
	public PointSpriteProperty() {
		this( new float[]{ 1, 0, 0.01f }, 60 );
	}

	public FloatBuffer getAttenuation() {
		attenuation.rewind();
		return attenuation;
	}

	public float getFadeThreshold() {
		return threshold;
	}

	@Override
	public String getType() {
		return "point-sprite";
	}

	@Override
	public void apply( RenderingSystem sys ) {
		sys.setPointSprites( this );
	}

	@Override
	public void reset( RenderingSystem sys ) {
		sys.setPointSprites( null );
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
