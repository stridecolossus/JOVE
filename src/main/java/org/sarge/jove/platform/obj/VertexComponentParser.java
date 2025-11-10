package org.sarge.jove.platform.obj;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.Scanner;
import java.util.function.Function;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>vertex component parser</i> parses an OBJ vertex component (vertices, normals, texture coordinates).
 * @param <T> Vertex component
 * @author Sarge
 */
class VertexComponentParser<T extends Bufferable> implements Parser {
	private final int size;
	private final Function<float[], T> constructor;
	private final VertexComponentList<T> list;

	/**
	 * Constructor.
	 * @param size				Component tuple size
	 * @param constructor		Array constructor
	 * @param list				Component list
	 */
	public VertexComponentParser(int size, Function<float[], T> constructor, VertexComponentList<T> list) {
		this.size = requireOneOrMore(size);
		this.constructor = requireNonNull(constructor);
		this.list = requireNonNull(list);
	}

	@Override
	public void parse(Scanner scanner) {
		// Parse array
		final float[] array = new float[size];
		for(int n = 0; n < size; ++n) {
			array[n] = scanner.nextFloat();
		}

		// Construct object from array
		final T value = constructor.apply(array);

		// Add to model
		list.add(value);
	}
}
