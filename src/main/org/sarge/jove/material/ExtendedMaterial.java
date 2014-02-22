package org.sarge.jove.material;

import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.lib.util.Check;

/**
 * Inherited material.
 * @author Sarge
 */
public class ExtendedMaterial extends MutableMaterial {
	private final MutableMaterial parent;

	/**
	 * Constructor.
	 * @param name 		Material name
	 * @param mat		Base material
	 */
	public ExtendedMaterial( String name, MutableMaterial mat ) {
		super( name );
		Check.notNull( mat );
		this.parent = mat;
	}

	@Override
	public ShaderProgram getShader() {
		final ShaderProgram shader = super.getShader();
		if( shader == null ) {
			return parent.getShader();
		}
		else {
			return shader;
		}
	}

	@Override
	public void apply( RenderContext ctx ) {
		// Init this and parent(s) parameters
		final ShaderProgram program = getShader();
		parent.init( ctx, program );
		parent.init( PropertyScope.FRAME, ctx, program );
		parent.init( PropertyScope.NODE, ctx, program );

		// Delegate
		super.apply( ctx );
	}

	@Override
	public void update( RenderContext ctx ) {
		parent.update( ctx );
		super.update( ctx );
	}

	@Override
	public void reset() {
		parent.reset();
		super.reset();
	}
}
