package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Normal;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractModel implements Model {
	private final Primitive primitive;
	private final Layout layout;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @throws IllegalArgumentException if the layout contains a {@link Normal#LAYOUT} but the drawing {@link #primitive()} does not support normals
	 * @see Primitive#isNormalSupported()
	 */
	protected AbstractModel(Primitive primitive, Layout layout) {
		this.primitive = notNull(primitive);
		this.layout = notNull(layout);
		validate();
	}

	private void validate() {
		if(!primitive.isNormalSupported() && isNormalLayout()) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}

	@Override
	public final Primitive primitive() {
		return primitive;
	}

	@Override
	public final Layout layout() {
		return layout;
	}

	/**
	 * @return Whether this model contains vertex normals
	 */
	protected boolean isNormalLayout() {
		return layout.components().stream().anyMatch(c -> c == Normal.LAYOUT);
	}

	@Override
	public int hashCode() {
		return Objects.hash(primitive, count(), layout);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Model that) &&
				(this.primitive == that.primitive()) &&
				(this.count() == that.count()) &&
				this.layout.equals(that.layout());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(primitive)
				.append(layout)
				.append("count", count())
				.append("indexed", isIndexed())
				.build();
	}
}
