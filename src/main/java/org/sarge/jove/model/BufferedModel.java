package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.model.Model.AbstractModel;

/**
 * A <i>buffered model</i> TODO
 * @author Sarge
 */
public class BufferedModel extends AbstractModel {
	private final Bufferable vertices;
	private final Optional<Bufferable> index;

	/**
	 * Constructor.
	 * @param header		Model header
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedModel(Header header, Bufferable vertices, Bufferable index) {
		super(header);
		this.vertices = notNull(vertices);
		this.index = Optional.ofNullable(index);
	}

	@Override
	public boolean isIndexed() {
		return index.isPresent();
	}

	@Override
	public Bufferable vertexBuffer() {
		return vertices;
	}

	@Override
	public Optional<Bufferable> indexBuffer() {
		return index;
	}
}
