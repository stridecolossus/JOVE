package org.sarge.jove.platform.obj;

import static org.sarge.jove.util.Check.notNull;

import java.util.function.Function;

import org.sarge.jove.platform.obj.ObjectModel.ComponentList;
import org.sarge.jove.util.Check;

/**
 * An <i>array parser</i> parses an OBJ vertex component that is instantiated from a floating-point array (e.g. a vertex, normal or texture coordinate).
 * @author Sarge
 * @param <T> Model component type
 */
public class ArrayParser<T> implements Parser {
	private final float[] array;
	private final Function<float[], T> ctor;
	private final Function<ObjectModel, ComponentList<T>> mapper;

	/**
	 * Constructor.
	 * @param size			Expected size of the data
	 * @param ctor			Array constructor
	 * @param mapper		Extracts the component list from the model
	 */
	public ArrayParser(int size, Function<float[], T> ctor, Function<ObjectModel, ComponentList<T>> mapper) {
		Check.oneOrMore(size);
		this.array = new float[size];
		this.ctor = notNull(ctor);
		this.mapper = notNull(mapper);
	}

	@Override
	public void parse(String[] args, ObjectModel model) {
		// Validate
		if(args.length != array.length) {
			throw new IllegalArgumentException(String.format("Invalid number of tokens: expected=%d actual=%d", array.length, args.length));
		}

		// Convert to array
		for(int n = 0; n < array.length; ++n) {
			array[n] = Float.parseFloat(args[n].trim());
		}

		// Create object using array constructor
		final T value = ctor.apply(array);

		// Add to transient model
		mapper.apply(model).add(value);
	}
}
