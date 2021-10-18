package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

import org.sarge.jove.common.Vertex;
import org.sarge.lib.util.Check;

/**
 * A <i>vertex component parser</i> parses an OBJ vertex component (vertices, normals, texture coordinates).
 * @param <T> Vertex component
 * @author Sarge
 */
class VertexComponentParser<T extends Vertex.Component> implements Parser {
	private final float[] array;
	private final Function<float[], T> ctor;
	private final BiConsumer<ObjectModel, T> consumer;

	/**
	 * Constructor.
	 * @param size			Size
	 * @param ctor			Array constructor
	 * @param consumer		Consumer for the parsed component
	 */
	public VertexComponentParser(int size, Function<float[], T> ctor, BiConsumer<ObjectModel, T> consumer) {
		Check.oneOrMore(size);
		this.array = new float[size];
		this.ctor = notNull(ctor);
		this.consumer = notNull(consumer);
	}

	@Override
	public void parse(String[] args, ObjectModel model) {
		// Validate
		if(args.length != array.length) {
			throw new IllegalArgumentException(String.format("Invalid number of elements: expected=%d actual=%d", array.length, args.length));
		}

		// Convert to array
		for(int n = 0; n < array.length; ++n) {
			array[n] = Float.parseFloat(args[n].trim());
		}

		// Create object using array constructor
		final T value = ctor.apply(array);

		// Add to model
		consumer.accept(model, value);
	}
}
