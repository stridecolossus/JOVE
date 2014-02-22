package org.sarge.jove.material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.shader.ShaderParameter;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureEntry;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableMaterial implements Material {
	private final String name;
	private final Map<String, Object> parameters = new StrictMap<>();
	private final Map<String, TextureEntry> textures = new LinkedHashMap<>();
	private final Map<String, MaterialProperty> properties = new StrictMap<>();
	private final Map<String, RenderProperty> render = new StrictMap<>();
	private ShaderProgram shader;

	/**
	 * Constructor.
	 * @param name Material name
	 */
	public MutableMaterial( String name ) {
		Check.notEmpty( name );
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public ShaderProgram getShader() {
		return shader;
	}

	/**
	 * Sets the shader program for this material.
	 * @param shader Shader
	 */
	public void setShader( ShaderProgram shader ) {
		this.shader = shader;
	}

	/**
	 * Sets a texture or colour-map.
	 * @param name		Texture parameter name
	 * @param tex		Texture image or <tt>null</tt> if unused
	 * @throws IllegalArgumentException if the specified texture unit has already been used by this material
	 */
	public void set( String name, TextureEntry tex ) {
		Check.notEmpty( name );
		Check.notNull( tex );

		for( TextureEntry entry : textures.values() ) {
			if( entry.getTextureUnit() == tex.getTextureUnit() ) throw new IllegalArgumentException( "Duplicate texture unit: " + name );
		}

		textures.put( MATERIAL_PREFIX + name, tex );
	}

	/**
	 * Convenience method - Allocates the texture unit depending on insertion order.
	 * @param name		Texture parameter name
	 * @param tex		Texture
	 */
	public void set( String name, Texture tex ) {
		final int unit = textures.size();
		final TextureEntry entry = new TextureEntry( tex, unit );
		set( name, entry );
	}

	/**
	 * Sets a material property.
	 * @param name		Parameter name
	 * @param value		Shader parameter value
	 */
	public void set( String name, Object value ) {
		parameters.put( MATERIAL_PREFIX + name, value );
	}

	/**
	 * Sets a global material property.
	 * @param name		Property name
	 * @param p			Material property
	 */
	public void set( String name, MaterialProperty p ) {
		properties.put( GLOBAL_PREFIX + name, p );
	}

	/**
	 * Adds a render property.
	 * @param p Property
	 * @throws IllegalArgumentException for a duplicate property
	 */
	public void add( RenderProperty p ) {
		render.put( p.getType(), p );
	}

	@Override
	public Map<String, RenderProperty> getRenderProperties() {
		return render;
	}

	@Override
	public void apply( RenderContext ctx ) {
		// Init parameters and properties
		final ShaderProgram program = getShader();
		init( ctx, program );

		// Activate shader
		if( program != null ) {
			activate( program, ctx );
		}
	}

	/**
	 * Initialises parameters and properties of this material.
	 * @param ctx Rendering context
	 */
	protected void init( RenderContext ctx, ShaderProgram program ) {
		// Init global-scope shader parameters
		init( PropertyScope.GLOBAL, ctx, program );

		// Activate textures
		for( Entry<String, TextureEntry> entry : textures.entrySet() ) {
			// Activate texture
			final TextureEntry tex = entry.getValue();
			tex.activate();

			// Set texture shader parameter
			// TODO - surely this will repeat for EVERY frame? but we only need to do it once!
			init( entry.getKey(), tex, program );
		}

		// Apply render properties
		final RenderingSystem sys = ctx.getRenderingSystem();
		for( RenderProperty p : render.values() ) {
			p.apply( sys );
		}
	}

	/**
	 * Activates the given shader program.
	 * @param shader	Shader program
	 * @param ctx		Rendering context
	 */
	protected void activate( ShaderProgram program, RenderContext ctx ) {
		// Init frame and node-scope shader parameters
		init( PropertyScope.FRAME, ctx, program );
		init( PropertyScope.NODE, ctx, program );

		// Set active shader
		program.activate();
	}

	@Override
	public void update( RenderContext ctx ) {
		init( PropertyScope.NODE, ctx, shader );
		shader.update();
	}

	@Override
	public void reset() {
		// Reset textures
		for( TextureEntry tex : textures.values() ) {
			tex.reset();
		}

		// Reset shader program
		if( shader != null ) {
			shader.reset();
		}
	}

	/**
	 * Initialises material shader parameters.
	 * @param scope		Scope of properties to be initialised
	 * @param ctx		Rendering context
	 */
	protected void init( PropertyScope scope, RenderContext ctx, ShaderProgram program ) {
		switch( scope ) {
		case GLOBAL:
			for( Entry<String, Object> entry : parameters.entrySet() ) {
				init( entry.getKey(), entry.getValue(), program );
			}
			break;

		default:
			for( Entry<String, MaterialProperty> entry : properties.entrySet() ) {
				// Skip not in this scope
				final MaterialProperty p = entry.getValue();
				if( p.getScope() != scope ) continue;

				// Init property
				final Object value = p.getValue( ctx );
				init( entry.getKey(), value, program );
			}
			break;
		}
	}

	/**
	 * Initialises a property.
	 *
	 * TODO - dirty values?
	 *
	 */
	@SuppressWarnings("hiding")
	private void init( String name, Object value, ShaderProgram program ) {
		if( program == null ) throw new IllegalArgumentException( "No shader to initialise: " + name );

		// Lookup shader parameter
		final ShaderParameter p = program.getParameter( name );
		if( p == null ) throw new IllegalArgumentException( "Unknown parameter: name=" + name + " mat=" + this.name );

		// Init parameter
		p.setValue( value );
	}

	@Override
	public String toString() {
		return name;
	}
}
