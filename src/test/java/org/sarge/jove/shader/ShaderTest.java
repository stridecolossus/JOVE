package org.sarge.jove.shader;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class ShaderTest {
	@Test
	public void test() {
		assertEquals( Shader.Type.VERTEX, Shader.Type.getType( "vert" ) );
		assertEquals( Shader.Type.FRAGMENT, Shader.Type.getType( "frag" ) );
		assertEquals( Shader.Type.GEOMETRY, Shader.Type.getType( "geom" ) );
		assertEquals( Shader.Type.TESSELATION_EVALUATION, Shader.Type.getType( "eval" ) );
		assertEquals( Shader.Type.TESSELATION, Shader.Type.getType( "tess" ) );
		assertEquals( null, Shader.Type.getType( "cobblers" ) );
	}
}
