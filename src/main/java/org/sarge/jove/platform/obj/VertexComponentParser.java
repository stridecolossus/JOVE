package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.sarge.jove.common.Bufferable;
import org.sarge.lib.util.Check;

/**
 * A <i>vertex component parser</i> parses an OBJ vertex component (vertices, normals, texture coordinates).
 * @param <T> Vertex component
 * @author Sarge
 */
class VertexComponentParser<T extends Bufferable> implements Parser {
	private final float[] array;
	private final Function<float[], T> ctor;
	private final VertexComponentList<T> list;

	/**
	 * Constructor.
	 * @param size		Size
	 * @param ctor		Array constructor
	 * @param list		Vertex components
	 */
	public VertexComponentParser(int size, Function<float[], T> ctor, VertexComponentList<T> list) {
		Check.oneOrMore(size);
		this.array = new float[size];
		this.ctor = notNull(ctor);
		this.list = notNull(list);
	}

	@Override
	public void parse(String[] args, ObjectModel model) {
		// Validate
		if(args.length != array.length + 1) {
			throw new IllegalArgumentException(String.format("Invalid number of elements: expected=%d actual=%d", array.length, args.length - 1));
		}

		// Convert to array
		for(int n = 0; n < array.length; ++n) {
			array[n] = Float.parseFloat(args[n + 1]);
		}

		// Create component
		final T value = ctor.apply(array);

		// Add to model
		list.add(value);
	}
}
