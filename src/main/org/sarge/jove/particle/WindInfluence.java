package org.sarge.jove.particle;

import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.RandomUtil;

public class WindInfluence implements Influence {
	private static final Vector STUCK = new Vector();

	@Override
	public void apply( Particle p, long elapsed ) {

		// TODO - flag in particle
		if( p.getDirection().equals( STUCK ) ) return;

		//if( RandomUtil.nextFloat( 1 ) < 0.5 ) return;

		//final float t = System.currentTimeMillis() % 2000;
//		final Point pos = p.getPosition();
//		p.setPosition( new Point( pos.getX() + RandomUtil.nextFloat( 0.0001f ), pos.getY(), pos.getZ() ) );

		final Vector vec = new Vector( RandomUtil.nextFloat( -0.000005f, 0.000005f ), 0, 0 );
		p.add( vec );
	}
}
