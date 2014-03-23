package org.sarge.jove.model;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

public class CubeBuilderTest {
	private CubeBuilder builder;

	@Before
	public void before() {
		builder = new CubeBuilder( MeshLayout.create( Primitive.TRIANGLES, "VN0", false ) );
	}

	@Test
	public void build() {
		final MeshBuilder cube = builder.create( 3 );
		assertNotNull( cube );
		assertEquals( 2 * 6, cube.getFaceCount() );
	}
}
