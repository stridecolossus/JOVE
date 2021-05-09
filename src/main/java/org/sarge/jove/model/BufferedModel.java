package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.ByteData.Source;
import org.sarge.jove.model.Model.AbstractModel;

/**
 * A <i>buffered model</i> is a default implementation with the vertices and index stored as {@link Bufferable} data objects.
 * @author Sarge
 */
public class BufferedModel extends AbstractModel {
	private final Source vertices;
	private final Optional<Source> index;

	/**
	 * Constructor.
	 * @param header		Model header
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedModel(Header header, Source vertices, Source index) {
		super(header);
		this.vertices = notNull(vertices);
		this.index = Optional.ofNullable(index);
	}

	@Override
	public boolean isIndexed() {
		return index.isPresent();
	}

	@Override
	public Source vertexBuffer() {
		return vertices;
	}

	@Override
	public Optional<Source> indexBuffer() {
		return index;
	}
}
