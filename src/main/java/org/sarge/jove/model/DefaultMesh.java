package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.scene.volume.Bounds;
import org.sarge.lib.util.Check;

/**
 * A <i>default mesh</i> is a mutable implementation used to construct a renderable model.
 * <p>
 * Vertex normals can be automatically computed using the {@link #compute()} method.
 * <p>
 * @see IndexedMesh
 * @author Sarge
 */
public class DefaultMesh extends AbstractMesh {
	private final List<Vertex> vertices = new ArrayList<>();

	/**
	 * Constructor.
	 * @param primitive 	Drawing primitive
	 * @param layout		Vertex layout
	 * @throws IllegalArgumentException if the layout contains {@link Normal#LAYOUT} but the drawing primitive is not {@link Primitive#isTriangle()}
	 */
	public DefaultMesh(Primitive primitive, CompoundLayout layout) {
		super(primitive, layout);
	}

	/**
	 * Adds a vertex to this mesh.
	 * @param vertex Vertex to add
	 */
	public DefaultMesh add(Vertex vertex) {
		Check.notNull(vertex);
		vertices.add(vertex);
		return this;
	}
	// TODO - validation (by length?)

	@Override
	public int count() {
		return vertices.size();
	}

	/**
	 * Retrieve a vertex.
	 * @param index Vertex index
	 * @return Vertex
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public Vertex vertex(int index) {
		return vertices.get(index);
	}

	@Override
	public final ByteSizedBufferable vertices() {
		return new ByteSizedBufferable() {
    		@Override
    		public int length() {
    			return vertices.size() * DefaultMesh.this.layout().stride();
    		}

    		@Override
    		public void buffer(ByteBuffer bb) {
    			for(Vertex v : vertices) {
    				v.buffer(bb);
    			}
    		}
    	};
	}

	/**
	 * Calculates the bounds of this mesh.
	 * @return Mesh bounds
	 * @throws IllegalStateException if the layout does not contain a {@link Point#LAYOUT} component
	 * @throws IllegalStateException if {@link #count()} is not valid for the drawing primitive
	 */
	public final Bounds bounds() {
		checkMesh();

		final var bounds = new Bounds.Builder();
		for(Vertex v : vertices) {
			final Point p = v.position();
			bounds.add(p);
		}

		return bounds.build();
		// TODO - parallel? requires spliterator to join bounds?
	}

	/**
	 * Computes vertex normals for this mesh.
	 * @throws IllegalStateException if the layout does not contain a {@link Point#LAYOUT} component
	 * @throws IllegalStateException if {@link #count()} is not valid for the drawing primitive
	 * @see ComputeNormals
	 */
	public void compute() {
		checkMesh();
		final var compute = new ComputeNormals(this);
		compute.compute();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("vertices", vertices.size())
				.build();
	}
}
