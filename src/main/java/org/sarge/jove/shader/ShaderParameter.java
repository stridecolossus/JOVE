package org.sarge.jove.shader;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.texture.TextureUnit;
import org.sarge.lib.util.Check;
import org.sarge.lib.util.ToString;

/**
 * Shader parameter.
 * <p>
 * Notes:
 * <ul>
 * <li>Only supports integers and booleans with a component size of <b>one</b></li>
 * <li>Boolean arrays are not supported</li>
 * <li>Throws {@link ClassCastException} if the setter method does not match the parameter type</li>
 * </ul>
 * @author Sarge
 */
public class ShaderParameter {
	private final String name;
	private final ParameterType type;
	private final int size;
	private final int len;
	private final int loc;

	private ParameterTransformer trans;
	private boolean dirty;

	/**
	 * Constructor.
	 * @param name		Parameter name
	 * @param type		Type
	 * @param size		Component size 1..4
	 * @param len		Array length 1..n
	 * @param loc		Uniform location
	 */
	public ShaderParameter( String name, ParameterType type, int size, int len, int loc ) {
		Check.notEmpty( name );
		Check.notNull( type );
		Check.range( size, 1, 4 );
		Check.oneOrMore( len );
		Check.zeroOrMore( loc );

		switch( type ) {
		case MATRIX:
			if( size == 1 ) throw new IllegalArgumentException( "Matrix component size must be 2..4" );
			break;

		case INTEGER:
		case BOOLEAN:
		case TEXTURE:
			if( size != 1 ) throw new UnsupportedOperationException( "Only single component size is supported" );
			break;
		}

		if( ( type == ParameterType.BOOLEAN ) && ( len != 1 ) ) throw new UnsupportedOperationException( "Only single boolean supported" );

		this.name = name;
		this.type = type;
		this.size = size;
		this.len = len;
		this.loc = loc;

		init();
	}

	/**
	 * @return Parameter name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return Parameter type
	 */
	public ParameterType getType() {
		return type;
	}

	/**
	 * @return Transformer for this parameter
	 */
	protected ParameterTransformer getTransformer() {
		return trans;
	}

	/**
	 * @return Whether this parameter has been updated
	 */
	public boolean isDirty() {
		if( dirty ) {
			dirty = false;
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * Initialises the transformer.
	 */
	private void init() {
		if( trans == null ) {
			final int bufferSize;
			if( type == ParameterType.MATRIX ) {
				bufferSize = size * size;
			}
			else {
				bufferSize = size;
			}
			trans = new ParameterTransformer( type.getBufferType(), bufferSize * len );
		}
	}

	/**
	 * Verifies that the argument is valid for this parameter.
	 */
	@SuppressWarnings("hiding")
	private void check( ParameterType.BufferType type, int size, int len ) {
		if( this.type.getBufferType() != type ) {
			throw new IllegalArgumentException( "Wrong argument type: expected=" + this.type + " actual=" + type );
		}

		if( ( this.size * this.len ) != ( size * len ) ) {
			throw new IllegalArgumentException( "Wrong data size: expected=" + ( this.size * this.len ) + " actual=" + ( size * len ) );
		}
	}

	/**
	 * Sets a floating-point argument.
	 * @param f Floating-point value
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not a floating-point parameter
	 */
	public void set( float f, ShaderProgram shader ) {
		check( ParameterType.BufferType.FLOAT_BUFFER, 1, 1 );
		init();
		trans.transform( f );
		shader.setFloat( loc, size, trans.getFloatBuffer() );
		dirty = true;
	}

	/**
	 * Sets a floating-point array argument.
	 * @param f Floating-point values
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not a floating-point array parameter
	 */
	public void set( float[] array, ShaderProgram shader ) {
		check( ParameterType.BufferType.FLOAT_BUFFER, 1, array.length );
		init();
		trans.transform( array );
		shader.setFloat( loc, size, trans.getFloatBuffer() );
		dirty = true;
	}

	/**
	 * Sets an integer argument.
	 * @param n Integer value
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not an integer parameter
	 */
	public void set( int n, ShaderProgram shader ) {
		check( ParameterType.BufferType.INTEGER_BUFFER, 1, 1 );
		init();
		trans.transform( n );
		shader.setInteger( loc, trans.getIntegerBuffer() );
		dirty = true;
	}

	/**
	 * Sets an integer array argument.
	 * @param array Integer values
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not an integer array parameter
	 */
	public void set( int[] array, ShaderProgram shader ) {
		check( ParameterType.BufferType.INTEGER_BUFFER, 1, array.length );
		init();
		trans.transform( array );
		shader.setInteger( loc, trans.getIntegerBuffer() );
		dirty = true;
	}

	/**
	 * Sets a boolean argument.
	 * @param b Boolean value
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not a boolean parameter
	 */
	public void set( boolean b, ShaderProgram shader ) {
		check( ParameterType.BufferType.INTEGER_BUFFER, 1, 1 );
		init();
		trans.transform( b ? 1 : 0 );
		shader.setInteger( loc, trans.getIntegerBuffer() );
		dirty = true;
	}

	/**
	 * Sets a domain argument.
	 * @param value Appendable value
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not a floating-point parameter
	 */
	public void set( Bufferable value, ShaderProgram shader ) {
		check( ParameterType.BufferType.FLOAT_BUFFER, value.getComponentSize(), 1 );
		init();
		trans.transform( value );
		if( value instanceof Matrix ) {
			shader.setMatrix( loc, size, trans.getFloatBuffer() );
		}
		else {
			shader.setFloat( loc, size, trans.getFloatBuffer() );
		}
		dirty = true;
	}

	/**
	 * Sets a domain array argument.
	 * @param array Appendable values
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not a floating-point array parameter
	 */
	public void set( Bufferable[] array, ShaderProgram shader ) {
		check( ParameterType.BufferType.FLOAT_BUFFER, array[0].getComponentSize(), array.length );
		init();
		trans.transform( array );
		if( array instanceof Matrix[] ) {
			shader.setMatrix( loc, size, trans.getFloatBuffer() );
		}
		else {
			shader.setFloat( loc, size, trans.getFloatBuffer() );
		}
		dirty = true;
	}

	/**
	 * Sets a texture argument.
	 * @param entry Texture entry
	 * @param shader Shader program
	 * @throws IllegalArgumentException if this is not a floating-point parameter
	 */
	public void set( TextureUnit entry, ShaderProgram shader ) {
		check( ParameterType.BufferType.INTEGER_BUFFER, 1, 1 );
		init();
		trans.transform( entry.getTextureUnit() );
		shader.setInteger( loc, trans.getIntegerBuffer() );
		dirty = true;
	}

	/**
	 * Releases underlying buffers.
	 * Note that the buffers are automatically re-created when invoking a setter.
	 */
	public void dispose() {
		trans = null;
	}

	@Override
	public String toString() {
		return ToString.toString( this );
	}
}
