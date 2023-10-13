package org.sarge.jove.model;

import java.util.Optional;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.Normal;

/**
 * A <i>mesh</i> is a renderable model comprising a vertex buffer and optional index.
 * @author Sarge
 */
public class DefaultMesh extends AbstractMesh {
	private final int count;
	private final ByteSizedBufferable index;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @param count			Draw count
	 * @param vertices		Vertices
	 * @param index			Optional index
	 * @throws IllegalArgumentException if the layout does not contain a vertex position
	 * @throws IllegalArgumentException if the layout contains {@link Normal#LAYOUT} but the drawing primitive is not {@link Primitive#isTriangle()}
	 * @throws IllegalStateException if the draw {@link #count} is not valid for the rendering primitive
	 */
	public DefaultMesh(Primitive primitive, CompoundLayout layout, int count, ByteSizedBufferable vertices, ByteSizedBufferable index) {
		super(primitive, layout, vertices);
		Mesh.validate(primitive, layout);
		this.count = Mesh.validate(primitive, count);
		this.index = index;
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public Optional<ByteSizedBufferable> index() {
		return Optional.ofNullable(index);
	}
}
