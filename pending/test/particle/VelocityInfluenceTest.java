package org.sarge.jove.particle;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

public class VelocityInfluenceTest {
	@Test
	public void test() {
		final Influence inf = new VelocityInfluence( 2 );
		final Particle p = new Particle( new Point(), Vector.X_AXIS, null, 0 );
		inf.apply( p, 1 );
		assertEquals( Vector.X_AXIS.multiply( 2 ), p.getDirection() );
	}
}
