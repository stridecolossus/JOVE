package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.util.Optional;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;

/**
 * Template implementation.
 * @author Sarge
 */
public abstract class AbstractMesh implements Mesh {
	private final Primitive primitive;
	private final CompoundLayout layout;
	private final ByteSizedBufferable vertices;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @param vertices		Vertices
	 * @param index			Optional index
	 */
	protected AbstractMesh(Primitive primitive, CompoundLayout layout, ByteSizedBufferable vertices) {
		this.primitive = requireNonNull(primitive);
		this.layout = requireNonNull(layout);
		this.vertices = requireNonNull(vertices);
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
	public final ByteSizedBufferable vertices() {
		return vertices;
	}

	@Override
	public Optional<ByteSizedBufferable> index() {
		return Optional.empty();
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
