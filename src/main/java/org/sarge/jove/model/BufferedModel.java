package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;

/**
 * A <i>buffered model</i> TODO
 * @author Sarge
 */
public class BufferedModel implements Model {
	private final Header header;
	private final Bufferable vertices;
	private final Optional<Bufferable> index;

	/**
	 * Constructor.
	 * @param header		Model header
	 * @param vertices		Vertex buffer
	 * @param index			Optional index buffer
	 */
	public BufferedModel(Header header, Bufferable vertices, Bufferable index) {
		this.header = notNull(header);
		this.vertices = notNull(vertices);
		this.index = Optional.ofNullable(index);
	}

	@Override
	public Header header() {
		return header;
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(header)
				.append("index", isIndexed())
				.build();
	}
}
