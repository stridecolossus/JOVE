package org.sarge.jove.platform.obj;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.util.FloatSupport.ArrayConverter;

/**
 * A <i>vertex component parser</i> parses an OBJ vertex component (vertices, normals, texture coordinates).
 * @param <T> Vertex component
 * @author Sarge
 */
class VertexComponentParser<T extends Bufferable> implements Parser {
	private final ArrayConverter<T> converter;
	private final VertexComponentList<T> list;

	/**
	 * Constructor.
	 * @param converter		Array converter
	 * @param list			Vertex components
	 */
	public VertexComponentParser(ArrayConverter<T> converter, VertexComponentList<T> list) {
		this.converter = notNull(converter);
		this.list = notNull(list);
	}

	@Override
	public void parse(String args, ObjectModel model) {
		final T value = converter.apply(args);
		list.add(value);
	}
}
