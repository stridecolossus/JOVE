package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import java.util.function.Function;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.FloatArrayConverter;

/**
 * A <i>vertex component parser</i> parses an OBJ vertex component (vertices, normals, texture coordinates).
 * @param <T> Vertex component
 * @author Sarge
 */
class VertexComponentParser<T extends Bufferable> implements Parser {
	private final FloatArrayConverter<T> converter;
	private final Function<ObjectModel, VertexComponentList<T>> mapper;

	/**
	 * Constructor.
	 * @param converter		Array converter
	 * @param mapper		Vertex components
	 */
	public VertexComponentParser(FloatArrayConverter<T> converter, Function<ObjectModel, VertexComponentList<T>> mapper) {
		this.converter = notNull(converter);
		this.mapper = notNull(mapper);
	}

	@Override
	public void parse(String args, ObjectModel model) {
		final T value = converter.apply(args);
		final VertexComponentList<T> list = mapper.apply(model);
		list.add(value);
	}
}
