package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.List;
import java.util.function.Function;

import org.sarge.jove.common.Component;
import org.sarge.lib.util.Check;

/**
 * A <i>component parser</i> parses an OBJ vertex component instantiated from a floating-point array (i.e. vertices, normals and texture coordinates).
 * @author Sarge
 * @param <T> Model component type
 */
public class ComponentParser<T extends Component> implements Parser {
	private final float[] array;
	private final Function<float[], T> ctor;
	private final Function<ObjectModel, List<T>> mapper;

	/**
	 * Constructor.
	 * @param size			Component size
	 * @param ctor			Array constructor
	 * @param mapper		Extracts the component list from the model
	 */
	public ComponentParser(int size, Function<float[], T> ctor, Function<ObjectModel, List<T>> mapper) {
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

		// Create component using array constructor
		final T value = ctor.apply(array);

		// Add to model
		mapper.apply(model).add(value);
	}
}
