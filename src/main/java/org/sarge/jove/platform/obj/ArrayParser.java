package org.sarge.jove.platform.obj;

import static org.sarge.jove.util.Check.notNull;
import static org.sarge.jove.util.Check.oneOrMore;

import java.util.function.Function;

import org.sarge.jove.platform.obj.ObjectModel.ComponentList;

/**
 * An <i>array parser</i> parses an OBJ vertex component that is instantiated from a floating-point array (e.g. a vertex, normal or texture coordinate).
 * @author Sarge
 * @param <T> Model component type
 */
public class ArrayParser<T> implements Parser {
	private final int size;
	private final Function<float[], T> ctor;
	private final Function<ObjectModel, ComponentList<T>> mapper;

	/**
	 * Constructor.
	 * @param size			Expected size of the data
	 * @param ctor			Array constructor
	 * @param mapper		Extracts the component list from the model
	 */
	public ArrayParser(int size, Function<float[], T> ctor, Function<ObjectModel, ComponentList<T>> mapper) {
		this.size = oneOrMore(size);
		this.ctor = notNull(ctor);
		this.mapper = notNull(mapper);
	}

	@Override
	public void parse(String[] args, ObjectModel model) {
		// Validate
		if(args.length != size) {
			throw new IllegalArgumentException(String.format("Invalid number of tokens: expected=%d actual=%d", size, args.length));
		}

		// Convert to array
		final float[] array = new float[size];
		for(int n = 0; n < size; ++n) {
			array[n] = Float.parseFloat(args[n].trim());
		}

		// Create object using array constructor
		final T value = ctor.apply(array);

		// Add to transient model
		mapper.apply(model).add(value);
	}
}
