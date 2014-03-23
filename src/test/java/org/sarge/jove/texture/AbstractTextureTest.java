package org.sarge.jove.texture;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class AbstractTextureTest {
	private AbstractTexture texture;

	@Before
	public void before() {
		texture = new AbstractTexture() {
			private boolean activated;

			{
				setResourceID( 42 );
			}

			@Override
			protected void delete( int id ) {
				assertEquals( 42, id );
			}

			@Override
			protected void bind( int id, int unit ) {
				if( activated ) {
					assertEquals( 0, id );
				}
				else {
					assertEquals( 42, id );
					activated = true;
				}
				assertEquals( 1, unit );
			}
		};
	}

	@Test
	public void lifecycle() {
		texture.activate( 1 );
		texture.reset( 1 );
		texture.release();
	}
}
