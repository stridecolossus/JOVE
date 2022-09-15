package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractModel implements Model {
	private final Primitive primitive;
	private final Layout layout;

	/**
	 * Constructor.
	 * @param primitive		Drawing primitive
	 * @param layout		Vertex layout
	 * @throws IllegalArgumentException if the layout contains {@link Model#NORMALS} but normals are not supported by the given {@link #primitive}
	 * @see Primitive#isNormalSupported()
	 */
	protected AbstractModel(Primitive primitive, Layout layout) {
		this.primitive = notNull(primitive);
		this.layout = notNull(layout);
		validate();
	}

	private void validate() {
		final boolean normals = layout.components().stream().anyMatch(e -> e == NORMALS);
		if(normals && !primitive.isNormalSupported()) {
			throw new IllegalArgumentException("Vertex normals are not supported by primitive: " + primitive);
		}
	}

	/**
	 * @param count Draw count
	 * @throws IllegalArgumentException if {@link #count} is invalid for the drawing primitive of this model
	 */
	protected void validate(int count) {
		if(!primitive.isValidVertexCount(count)) {
			throw new IllegalArgumentException(String.format("Invalid number of model vertices %d for primitive %s", count, primitive));
		}
	}

	@Override
	public Primitive primitive() {
		return primitive;
	}

	@Override
	public Layout layout() {
		return layout;
	}

	@Override
	public Optional<Bufferable> index() {
		return Optional.empty();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(primitive)
				.append("count", count())
				.append(layout)
				.append("indexed", isIndexed())
				.build();
	}
}
