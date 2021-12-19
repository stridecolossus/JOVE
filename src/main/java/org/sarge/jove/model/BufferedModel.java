package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Layout;

/**
 * A <i>buffered model</i> is composed of {@link Bufferable} objects for the vertex and index buffers.
 * @author Sarge
 */
public class BufferedModel extends AbstractModel {
	private final int count;
	private final Bufferable vertices;
	private final Bufferable index;

	/**
	 * Constructor.
	 * @param layout			Vertex layout
	 * @param primitive			Drawing primitive
	 * @param count				Draw count
	 * @param vertices			Vertices
	 * @param index				Index
	 */
	public BufferedModel(List<Layout> layout, Primitive primitive, int count, Bufferable vertices, Bufferable index) {
		super(primitive, layout);
		this.count = zeroOrMore(count);
		this.vertices = notNull(vertices);
		this.index = notNull(index);
	}

	/**
	 * Copy constructor.
	 * @param model Model to copy
	 */
	public BufferedModel(Model model) {
		this(model.layout(), model.primitive(), model.count(), model.vertexBuffer(), model.indexBuffer());
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public boolean isIndexed() {
		return index.length() > 0;
	}

	@Override
	public Bufferable vertexBuffer() {
		return vertices;
	}

	@Override
	public Bufferable indexBuffer() {
		return index;
	}
}
