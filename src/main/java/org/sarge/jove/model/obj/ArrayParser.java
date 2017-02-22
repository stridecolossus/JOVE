package org.sarge.jove.model.obj;

import java.util.Scanner;
import java.util.function.Function;

import org.sarge.lib.util.Check;

/**
 * OBJ parser template-implementation for array-based floating-point data such as vertices, normals and texture-coordinates.
 * @author Sarge
 */
public abstract class ArrayParser<T> implements Parser {
	private final float[] array;
	private final Function<float[], T> ctor;

	/**
	 * Constructor.
	 * @param length		Number of elements
	 * @param ctor			Array constructor
	 */
	protected ArrayParser(int length, Function<float[], T> ctor) {
		Check.oneOrMore(length);
		Check.notNull(ctor);
		this.array = new float[length];
		this.ctor = ctor;
	}
	
	@Override
	public final void parse(Scanner scanner, ObjectModel model) {
		// Load values into array
		for(int n = 0; n < array.length; ++n) {
			array[n] = scanner.nextFloat();
		}
		
		// Convert to object
		final T obj = ctor.apply(array);

		// Add to model
		add(obj, model);
	}

	/**
	 * Adds the given object to the model.
	 * @param obj		Object to add
	 * @param model		Model
	 */
	protected abstract void add(T obj, ObjectModel model);
}
