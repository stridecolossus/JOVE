package org.sarge.jove.particle;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.sarge.jove.geometry.BoundingBox;
import org.sarge.jove.geometry.Point;

public class BoundingBoxEmitterTest {
	@Test
	public void emit() {
		final BoundingBox box = new BoundingBox( new Point( 1, 2, 3 ), new Point( 4, 5, 6 ) );
		final Emitter emitter = new BoundingBoxEmitter( box );
		final Point pos = emitter.emit();
		assertNotNull( pos );
		assertTrue( box.contains( pos ) );
	}
}
