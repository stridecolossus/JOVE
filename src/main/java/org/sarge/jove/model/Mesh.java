package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Optional;
import java.util.function.IntSupplier;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

/**
 * A <i>mesh</i> is a renderable model comprising a vertex buffer and an optional index.
 * @author Sarge
 */
public final class Mesh {
	private final Primitive primitive;
	private final CompoundLayout layout;
	private final IntSupplier count;
	private final ByteSizedBufferable vertices;
	private final ByteSizedBufferable index;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @param count			Draw count
	 * @param vertices		Vertices
	 * @param index			Optional index
	 * @throws IllegalArgumentException if the layout contains {@link Normal#LAYOUT} but the drawing primitive is not {@link Primitive#isTriangle()}
	 * @throws IllegalStateException if the draw count is not valid for the rendering primitive
	 */
	public Mesh(Primitive primitive, CompoundLayout layout, IntSupplier count, ByteSizedBufferable vertices, ByteSizedBufferable index) {
		this.primitive = notNull(primitive);
		this.layout = notNull(layout);
		this.count = notNull(count);
		this.vertices = notNull(vertices);
		this.index = index;
		validateNormals();
	}

	private void validateNormals() {
		if(!primitive.isTriangle() && layout.contains(Normal.LAYOUT)) {
			throw new IllegalStateException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}

	/**
	 * Validates that this mesh can be rendered for the given draw count.
	 * @throws IllegalStateException if the layout does not contain a vertex position
	 * @throws IllegalStateException if the draw count is not valid for the rendering primitive
	 */
	public void validate() {
		if(!layout.contains(Point.LAYOUT)) {
			throw new IllegalStateException("Layout does not contain a vertex position: " + this);
		}

		if(!primitive.isValidVertexCount(count())) {
			throw new IllegalStateException("Invalid draw count for primitive: " + this);
		}
	}

	/**
	 * @return Drawing primitive
	 */
	public Primitive primitive() {
		return primitive;
	}

	/**
	 * @return Vertex layout
	 */
	public CompoundLayout layout() {
		return layout;
	}

	/**
	 * @return Draw count
	 */
	public int count() {
		return count.getAsInt();
	}

	/**
	 * @return Vertex buffer
	 */
	public ByteSizedBufferable vertices() {
		return vertices;
	}

	/**
	 * @return Whether this mesh has an index
	 */
	public boolean isIndexed() {
		return index != null;
	}

	/**
	 * @return Index buffer
	 */
	public Optional<ByteSizedBufferable> index() {
		return Optional.ofNullable(index);
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
				.append("count", count())
				.append("layout", layout)
				.build();
	}
}
