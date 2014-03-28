package org.sarge.jove.material;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.sarge.jove.app.RenderingSystem;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.scene.RenderContext;
import org.sarge.jove.shader.ShaderParameter;
import org.sarge.jove.shader.ShaderProgram;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureUnit;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.StrictMap;
import org.sarge.lib.util.StrictSet;

/**
 * Mutable implementation.
 * @author Sarge
 */
public class MutableMaterial implements Material {
	private final String name;
	private final Map<String, Object> parameters = new StrictMap<>();
	private final Map<String, TextureUnit> textures = new LinkedHashMap<>();
	private final Map<String, MaterialProperty> properties = new StrictMap<>();
	private final Map<String, RenderProperty> render = new StrictMap<>();
	private final Set<String> modified = new StrictSet<>();

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
	 * @param unit		Texture unit
	 * @throws IllegalArgumentException if the specified texture unit has already been used by this material
	 */
	public void set( String name, TextureUnit unit ) {
		Check.notEmpty( name );
		Check.notNull( unit );

		for( TextureUnit entry : textures.values() ) {
			if( entry.getTextureUnit() == unit.getTextureUnit() ) throw new IllegalArgumentException( "Duplicate texture unit: " + name );
		}

		textures.put( MATERIAL_PREFIX + name, unit );
	}

	/**
	 * Convenience method - Allocates the texture unit depending on insertion order.
	 * @param name		Texture parameter name
	 * @param tex		Texture
	 */
	public void set( String name, Texture tex ) {
		final int idx = textures.size();
		final TextureUnit unit = new TextureUnit( tex, idx );
		set( name, unit );
	}

	/**
	 * Sets a shader parameter.
	 * @param name		Parameter name
	 * @param value		Bufferable shader parameter value
	 */
	public void set( String name, Object value ) {
		final String key = MATERIAL_PREFIX + name;
		if( parameters.containsKey( key ) ) parameters.remove( key );
		parameters.put( key, value );
		modified.add( key );
	}

	/**
	 * Sets a material property.
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
		// TODO - what if no shader? get active one from ctx?
		final ShaderProgram program = shader;
shader.activate();

		// Update modified shader parameters
		for( String str : modified ) {
			final Object value = parameters.get( str );
			init( str, value, program );
		}
		modified.clear();

		// Init material properties
		init( PropertyScope.FRAME, ctx, program );
		init( PropertyScope.NODE, ctx, program );

		// Activate textures
		for( Entry<String, TextureUnit> unit : textures.entrySet() ) {
			// Activate texture
			final TextureUnit tex = unit.getValue();
			tex.activate();

			// Set texture shader parameter
			// TODO - surely this will repeat for EVERY frame? but we only need to do it once!
			init( unit.getKey(), tex.getTextureUnit(), program );
		}

		// Apply render properties
		final RenderingSystem sys = ctx.getRenderingSystem();
		for( RenderProperty p : render.values() ) {
			p.apply( sys );
		}

//		// Set active shader
//		if( shader != null ) {
//			shader.activate();
//		}
	}

	@Override
	public void reset( RenderContext ctx, Material prev ) {

		//

		// Reset textures
		for( TextureUnit tex : textures.values() ) {
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
	 * @param program	Target shader program
	 */
	private void init( PropertyScope scope, RenderContext ctx, ShaderProgram program ) {
		for( Entry<String, MaterialProperty> entry : properties.entrySet() ) {
			// Skip if not in this scope
			final MaterialProperty p = entry.getValue();
			if( p.getScope() != scope ) continue;

			// Init property
			final Object value = p.getValue( ctx );
			init( entry.getKey(), value, program );
		}
	}

	/**
	 * Sets a shader parameter.
	 */
	private void init( String key, Object value, ShaderProgram program ) {
		// Lookup shader parameter
		final ShaderParameter p = program.getParameter( key );
		if( p == null ) throw new IllegalArgumentException( "Unknown parameter: name=" + key + " mat=" + this.name );

		// Update shader parameter
		if( value instanceof Bufferable ) {
			p.set( (Bufferable) value, program );
		}
		else if( value instanceof Float ) {
			p.set( (float) value, program );
		}
		else if( value instanceof Integer ) {
			p.set( (int) value, program );
		}
		else if( value instanceof Long ) {
			p.set( (long) value, program );
		}
		else if( value instanceof Boolean ) {
			p.set( (boolean) value, program );
		}
		else {
			throw new UnsupportedOperationException( "Invalid shader parameter class: name=" + key + " mat=" + this.name );
		}
	}

	@Override
	public String toString() {
		return name;
	}
}
