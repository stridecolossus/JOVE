package org.sarge.jove.geometry;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

public class CompoundVolumeTest {
	private BoundingVolume compound, vol;

	@Before
	public void before() {
		vol = mock( BoundingVolume.class );
		compound = new CompoundVolume( new BoundingVolume[]{ vol } );
	}

	@Test
	public void getCentre() {
		final Point centre = new Point( 1, 2, 3 );
		when( vol.getCentre() ).thenReturn( centre );
		assertEquals( centre, compound.getCentre() );
	}

	@Test
	public void contains() {
		final Point pt = new Point();
		compound.contains( pt );
		verify( vol ).contains( pt );
	}

	@Test
	public void intersects() {
		final Ray ray = new Ray( Point.ORIGIN, Vector.X_AXIS );
		compound.intersects( ray );
		verify( vol ).intersects( ray );
	}
}
