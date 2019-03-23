package org.sarge.jove.material;

import static org.sarge.lib.util.Check.notNull;

import java.nio.FloatBuffer;
import java.util.function.Supplier;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Tuple;
import org.sarge.jove.material.Material.Property;
import org.sarge.jove.material.Shader.Parameter;
import org.sarge.jove.util.BufferFactory;

/**
 * Material property binder for a {@link Bufferable} object such as a matrix or vector.
 * @author Sarge
 * @see Parameter#set(FloatBuffer)
 * TODO - add arrays? or is that just a compound bufferable?
 */
public class BufferPropertyBinder implements Property.Binder {
	private static final int MATRIX_SIZE = Matrix.DEFAULT_ORDER * Matrix.DEFAULT_ORDER;

	/**
	 * Creates a buffer property binder for a matrix with a {@link Matrix#DEFAULT_ORDER}.
	 * @param matrix Matrix supplier
	 * @return Matrix property binder
	 */
	public static BufferPropertyBinder matrix(Supplier<Matrix> matrix) {
		return new BufferPropertyBinder(MATRIX_SIZE, matrix);
	}

	/**
	 * Creates a buffer property binder for a tuple.
	 * @param tuple Tuple supplier
	 * @return Tuple property binder
	 */
	public static BufferPropertyBinder tuple(Supplier<Tuple> tuple) {
		return new BufferPropertyBinder(Tuple.SIZE, tuple);
	}

	/**
	 * Creates a buffer property binder for a tuple.
	 * @param tuple Tuple supplier
	 * @return Tuple property binder
	 */
	public static BufferPropertyBinder colour(Supplier<Colour> col) {
		return new BufferPropertyBinder(Colour.SIZE, col);
	}

	private final Supplier<? extends Bufferable> supplier;
	private final FloatBuffer buffer;

	/**
	 * Constructor.
	 * @param size			Buffer size
	 * @param supplier 		Bufferable supplier
	 */
	public BufferPropertyBinder(int size, Supplier<? extends Bufferable> supplier) {
		this.supplier = notNull(supplier);
		this.buffer = BufferFactory.floatBuffer(size);
	}

	@Override
	public int size() {
		return buffer.capacity();
	}

	@Override
	public void apply(Parameter param) {
		// Buffer value
		supplier.get().buffer(buffer);
		buffer.flip();

		// Set parameter value
		param.set(buffer);

		// Prepare for next iteration
		buffer.flip();
	}
}
