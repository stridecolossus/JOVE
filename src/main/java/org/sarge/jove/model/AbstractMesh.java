package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;

/**
 * Skeleton implementation.
 * @author Sarge
 */
public abstract class AbstractMesh implements Mesh {
	private final Primitive primitive;
	private final List<Layout> layout;

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @throws IllegalArgumentException if the layout contains {@link Normal#LAYOUT} but the drawing primitive does not support normals
	 */
	protected AbstractMesh(Primitive primitive, List<Layout> layout) {
		this.primitive = requireNonNull(primitive);
		this.layout = List.copyOf(layout);

		if(!layout.contains(Point.LAYOUT)) {
			throw new IllegalArgumentException("Layout does not contain a vertex position: " + this);
		}

		if(!primitive.isTriangle() && layout.contains(Normal.LAYOUT)) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}

	@Override
	public Primitive primitive() {
		return primitive;
	}

	@Override
	public List<Layout> layout() {
		return layout;
	}

	@Override
	public Optional<ByteBuffer> index() {
		return Optional.empty();
	}
}
