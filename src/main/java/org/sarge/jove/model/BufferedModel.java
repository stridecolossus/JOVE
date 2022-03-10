package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.zeroOrMore;

import java.util.List;
import java.util.Optional;

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
	 * @param primitive			Drawing primitive
	 * @param layout			Vertex layout
	 * @param count				Draw count
	 * @param vertices			Vertices
	 * @param index				Index
	 */
	public BufferedModel(Primitive primitive, List<Layout> layout, int count, Bufferable vertices, Bufferable index) {
		super(primitive, layout);
		this.count = zeroOrMore(count);
		this.vertices = notNull(vertices);
		this.index = notNull(index);
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public boolean isIntegerIndex() {
		return isIntegerIndex(vertices.length() / Layout.stride(layout));
	}

	@Override
	public Bufferable vertexBuffer() {
		return vertices;
	}

	@Override
	public Optional<Bufferable> indexBuffer() {
		if(index.length() == 0) {
			return Optional.empty();
		}
		else {
			return Optional.of(index);
		}
	}
}
