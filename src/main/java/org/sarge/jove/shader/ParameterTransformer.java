package org.sarge.jove.shader;

import java.nio.Buffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.BufferFactory;

/**
 * Shader parameter transformer.
 * <p>
 * Transforms Java primitives and JOVE classes to NIO buffered data for upload to a {@link ShaderProgram}.
 *
 * @author Sarge
 */
class ParameterTransformer {
	private final Buffer buffer;

	/**
	 * Constructor.
	 * @param desc Parameter descriptor
	 */
	public ParameterTransformer( ParameterType.BufferType type, int size ) {
		switch( type ) {
		case FLOAT_BUFFER:
			buffer = BufferFactory.createFloatBuffer( size );
			break;

		case INTEGER_BUFFER:
			buffer = BufferFactory.createIntegerBuffer( size );
			break;

		default:
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * @return Floating-point buffered data
	 * @throws ClassCastException if this transformer does not handle floating-point data
	 */
	public FloatBuffer getFloatBuffer() {
		return (FloatBuffer) buffer;
	}

	/**
	 * @return Integer buffered data
	 * @throws ClassCastException if this transformer does not handle integer data
	 */
	public IntBuffer getIntegerBuffer() {
		return (IntBuffer) buffer;
	}

	/**
	 * Transforms floating-point data.
	 * @param values Floating-point values
	 * @throws ClassCastException if this transformer does not handle floating-point data
	 */
	public void transform( float... values ) {
		final FloatBuffer fb = getFloatBuffer();
		fb.clear();
		for( float f : values ) {
			fb.put( f );
		}
		fb.flip();
	}

	/**
	 * Transforms domain data.
	 * @param values Appendable values
	 * @throws ClassCastException if this transformer does not handle floating-point data
	 */
	public void transform( Bufferable... values ) {
		final FloatBuffer fb = getFloatBuffer();
		fb.clear();
		for( Bufferable app : values ) {
			app.append( fb );
		}
		fb.flip();
	}

	/**
	 * Transforms integer data.
	 * @param values Integer values
	 * @throws ClassCastException if this transformer does not handle integer data
	 */
	public void transform( int... values ) {
		final IntBuffer b = getIntegerBuffer();
		b.clear();
		for( int n : values ) {
			b.put( n );
		}
		b.flip();
	}
}
