package org.sarge.jove.shader;

import java.lang.reflect.Array;
import java.nio.FloatBuffer;

import org.sarge.jove.common.Appendable;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.texture.Texture;
import org.sarge.jove.texture.TextureEntry;
import org.sarge.jove.util.BufferUtils;
import org.sarge.lib.util.Check;

/**
 * Shader parameter descriptor.
 * @author Sarge
 */
public class TextureShaderParameter extends ShaderParameter<Texture> {
	private final String name;
	private final ParameterType type;
	private final int loc;
	private final int size;
	private final int len;

	private Object value;
	private boolean dirty = true;

	/**
	 * Constructor.
	 * @param name		Parameter name
	 * @param type		Parameter type
	 * @param loc		Location
	 * @param size		Number of components per element
	 * @param len		Number of elements (one-or-more)
	 */
	public TextureShaderParameter( String name, ParameterType type, int loc, int size, int len ) {
		Check.notEmpty( name );
		Check.notNull( type );
		Check.zeroOrMore( loc );
		Check.oneOrMore( size );
		Check.oneOrMore( len );

		this.name = name;
		this.type = type;
		this.loc = loc;
		this.size = size;
		this.len = len;
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
	 * @return Parameter location
	 */
	public int getLocation() {
		return loc;
	}

	/**
	 * @return Number of components per element
	 */
	public int getSize() {
		return size;
	}

	/**
	 * @return Array length
	 */
	public int getLength() {
		return len;
	}

	/**
	 * @return Current value
	 * @see #isDirty()
	 */
	public Object getValue() {
		dirty = false;
		return value;
	}

	/**
	 * Sets and validates the parameter value and marks this parameter as modified.
	 * @param value New value
	 * @throws IllegalArgumentException if the value is <tt>null</tt>, does not match this parameter type, or is not of the correct length
	 */
	public void setValue( Object value ) {
		Check.notNull( value );

		// Verify arrays
		final Class<?> expected = type.getParameterClass();
		final Class<?> actual = value.getClass();
		if( len == 1 ) {
			if( actual.isArray() ) throw new IllegalArgumentException( "Invalid array: " + actual + ", expected " + expected );
		}
		else {
			if( !actual.isArray() ) throw new IllegalArgumentException( "Not an array: " + actual + ", expected " + expected );
			if( Array.getLength( value ) != len ) throw new IllegalArgumentException( "Incorrect array length: " + this );
		}

		// Determine component type
		final Class<?> component;
		if( len == 1 ) {
			/*
			if( expected.isPrimitive() ) {
				component = Util.toWrapper( expected );
				if( component == null ) throw new IllegalArgumentException( "Invalid primitive type: " + expected );
			}
			else {
				component = actual;
			}
			*/
//			if( !BufferDataType.class.isAssignableFrom( actual ) ) throw new IllegalArgumentException();
			component = expected;
		}
		else {
			component = actual.getComponentType();
		}

		// Verify correct type
		if( !expected.isAssignableFrom( component ) ) {
			throw new IllegalArgumentException( "Invalid parameter type: " + component + ", expected " + expected );
		}

		// Buffer data
		final Object data;
		switch( type ) {
		case FLOAT:
			if( size == 1 ) {
				data = value;
			}
			else {
				// TODO - array
				final FloatBuffer fb = BufferUtils.createFloatBuffer( size );
				final Appendable b = (Appendable) value;
				b.append( fb );
				data = fb;
			}
			break;

		case MATRIX:
			// TODO - array
			final FloatBuffer fb = BufferUtils.createFloatBuffer( size * size );
			final Matrix m = (Matrix) value;
			m.append( fb );
			fb.rewind();
			data = fb;
			break;

		case BOOLEAN:
			final boolean b = (boolean) value;
			data = b ? 1 : 0;
			break;

		case INTEGER:
			// TODO - array
			data = value;
			break;

		case TEXTURE:
			// TODO - array
			final TextureEntry t = (TextureEntry) value;
			data = t.getTextureUnit();
			break;

		default:
			throw new UnsupportedOperationException( type.toString() );
		}

		// Store value and mark parameter as updated
		this.value = data;
		dirty = true;
	}

	/**
	 * @return Whether the parameter value has changed since the last read via {@link #getValue()}
	 * @see #setValue(Object)
	 */
	protected boolean isDirty() {
		return dirty;
	}

	@Override
	public String toString() {
		return name;
	}
}
