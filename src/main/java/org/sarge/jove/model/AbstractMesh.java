package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

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
			throw new IllegalArgumentException("Layout does not contain a vertex position: " + layout);
		}
		// TODO - is this relevant now?

		if(!primitive.isTriangle() && layout.contains(Normal.LAYOUT)) {
			throw new IllegalArgumentException("Vertex normals are not supported by the drawing primitive: " + primitive);
		}
	}
	// TODO - if can get rid of 1st check above and indexOf() below => no need for this skeleton at all?

	@Override
	public Primitive primitive() {
		return primitive;
	}

	@Override
	public List<Layout> layout() {
		return layout;
	}

	/**
	 * Looks up the index of given layout of this mesh.
	 * @param layout Mesh layout
	 * @return Layout index
	 * @throws IllegalArgumentException if the layout is not a member of this mesh
	 */
	public int indexOf(Layout layout) {
		final int index = this.layout.indexOf(layout);
		if(index < 0) {
			throw new IllegalArgumentException("Layout not present: " + layout);
		}
		return index;
	}
	// TODO - only relevant for mutable mesh? why else would it be needed?

	@Override
	public String toString() {
		return String.format("Mesh[primitive=%s layout=%s count=%d]", primitive, layout, count());
	}
}
