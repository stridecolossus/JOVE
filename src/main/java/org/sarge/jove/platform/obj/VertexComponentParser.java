package org.sarge.jove.platform.obj;

import static java.util.Objects.requireNonNull;

import java.util.function.Function;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.FloatArrayParser;

/**
 * A <i>vertex component parser</i> parses an OBJ vertex component (vertices, normals, texture coordinates).
 * @param <T> Vertex component
 * @author Sarge
 */
class VertexComponentParser<T extends Bufferable> implements Parser {
	private final FloatArrayParser<T> converter;
	private final Function<ObjectModel, VertexComponentList<T>> mapper;

	/**
	 * Constructor.
	 * @param converter		Array converter
	 * @param mapper		Vertex components
	 */
	public VertexComponentParser(FloatArrayParser<T> converter, Function<ObjectModel, VertexComponentList<T>> mapper) {
		this.converter = requireNonNull(converter);
		this.mapper = requireNonNull(mapper);
	}

	@Override
	public void parse(String args, ObjectModel model) {
		final T value = converter.apply(args);
		final VertexComponentList<T> list = mapper.apply(model);
		list.add(value);
	}
}
