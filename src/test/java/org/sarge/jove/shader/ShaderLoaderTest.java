package org.sarge.jove.shader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Before;
import org.junit.Test;
import org.sarge.jove.app.RenderingSystem;
import org.sarge.lib.io.DataSource;
import org.sarge.lib.io.StringDataSource;

public class ShaderLoaderTest {
	private DataSource src;
	private RenderingSystem sys;
	private ShaderLoader loader;

	@Before
	public void before() {
		src = new StringDataSource();
		sys = mock( RenderingSystem.class );
		loader = new ShaderLoader( src, sys );
	}

	@Test
	public void loadShader() throws Exception {
		loader.load( Shader.Type.FRAGMENT, "shader.frag", "flags" );
		verify( sys ).createShader( Shader.Type.FRAGMENT, "flags" + "shader.frag" + "\n" );
	}

	@Test(expected=IllegalArgumentException.class)
	public void loadMissingExtension() throws Exception {
		loader.load( new String[]{ "cobblers" }, null );
	}

	@Test(expected=UnsupportedOperationException.class)
	public void loadUnknownExtension() throws Exception {
		loader.load( new String[]{ "cobblers.extension" }, null );
	}

	@Test
	public void loadShaderProgram() throws Exception {
		loader.load( new String[]{ "shader.frag", "shader.vert" }, null );
		verify( sys ).createShader( Shader.Type.FRAGMENT, "shader.frag\n" );
		verify( sys ).createShader( Shader.Type.VERTEX, "shader.vert\n" );
	}
}
