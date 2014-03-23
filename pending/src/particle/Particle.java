package org.sarge.jove.particle;

import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Particle instance in a {@link ParticleSystem}.
 * @author Sarge
 */
public class Particle extends Vertex {
	private final long created;

	private Vector dir;

	/**
	 * Constructor.
	 * @param pos		Initial particle position
	 * @param dir		Initial vector
	 * @param col		Colour (optional)
	 * @param created	Creation time (ms)
	 */
	public Particle( Point pos, Vector dir, Colour col, long created ) {
		super( pos );
		Check.notNull( dir );
		this.dir = dir;
		this.col = col;
		this.created = created;
	}

	/**
	 * @return Current direction vector
	 */
	public Vector getDirection() {
		return dir;
	}

	/**
	 * Sets the particle direction.
	 * @param dir Direction
	 */
	public void setDirection( Vector dir ) {
		Check.notNull( dir );
		this.dir = dir;
	}

	/**
	 * Adds the given vector to the current direction of this particle.
	 * @param vec Movement vector
	 */
	public void add( Vector vec ) {
		dir = dir.add( vec );
	}

	/**
	 * Updates the position of this particle.
	 * @param speed Speed scalar
	 */
	public void update( float speed ) {
		pos = pos.add( dir.multiply( speed ) );
	}

	/**
	 * Sets the position of this particle.
	 * @param p Position
	 */
	public void setPosition( Point p ) {
		Check.notNull( p );
		pos = p;
	}

	/**
	 * Fades the colour of this particle.
	 * @param fade Fade scalar
	 */
	public void fade( Colour fade ) {
		col = col.fade( fade );
	}

	/**
	 * @return Creation time of this particle (ms)
	 */
	public long getCreationTime() {
		return created;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
