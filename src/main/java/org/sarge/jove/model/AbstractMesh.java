package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Normal;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractMesh implements Mesh {
	protected final Primitive primitive;
	protected final CompoundLayout layout;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @throws IllegalArgumentException if the layout contains {@link Normal#LAYOUT} but the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	protected AbstractMesh(Primitive primitive, CompoundLayout layout) {
		this.primitive = notNull(primitive);
		this.layout = notNull(layout);
		validate();
	}

	private void validate() {
		if(!primitive.isTriangle() && layout.contains(Normal.LAYOUT)) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}

	@Override
	public final Primitive primitive() {
		return primitive;
	}

	@Override
	public final CompoundLayout layout() {
		return layout;
	}

	@Override
	public int hashCode() {
		return Objects.hash(primitive, count(), layout);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Mesh that) &&
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
