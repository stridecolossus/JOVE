package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Layout;
import org.sarge.jove.model.Model.Header;

/**
 * A <i>model builder</i> is used to construct a vertex model.
 * <p>
 * Usage:
 * <pre>
 * 	Model model = new ModelBuilder(List.of(Point.LAYOUT, Vector.LAYOUT))
 * 		.primitive(Primitive.TRIANGLES)
 * 		.add(Vertex.of(new Point(...), new Vector(...))
 * 		.build();
 * </pre>
 * <p>
 * @see Vertex
 * @see Layout
 * @author Sarge
 */
public class ModelBuilder {
	protected final List<Vertex> vertices = new ArrayList<>();
	protected final List<Layout> layouts;
	protected Primitive primitive = Primitive.TRIANGLE_STRIP;

	/**
	 * Constructor.
	 * @param layouts Vertex layout
	 */
	public ModelBuilder(List<Layout> layouts) {
		this.layouts = List.copyOf(layouts);
	}

	/**
	 * Sets the drawing primitive (default is {@link Primitive#TRIANGLE_STRIP}).
	 * @param primitive Drawing primitive
	 */
	public ModelBuilder primitive(Primitive primitive) {
		this.primitive = notNull(primitive);
		return this;
	}

	/**
	 * Adds a vertex.
	 * @param vertex Vertex to add
	 */
	public ModelBuilder add(Vertex vertex) {
		assert vertex.transform(layouts).equals(vertex);
		vertices.add(vertex);
		return this;
	}

	// TODO - compute normals
	// - walk index -> faces (triangles) ~ primitive
	// - accumulate normal @ each vertex of each face (cross product)
	// - normalise all
	// - invalid if no normals
	// implies:
	// - operations on model?
	// - face iterator?
	// - normal accessor and accumulator/mutator

	/**
	 * Constructs this model.
	 * @return New model
	 */
	public Model build() {
		return build(vertices.size(), null);
	}

	/**
	 * Constructs this model.
	 * @param count		Number of vertices
	 * @param index		Index or {@code null} if not indexed
	 * @return New model
	 */
	protected final Model build(int count, int[] index) {
		final Header header = new Header(layouts, primitive, count);
		return new DefaultModel(header, vertices, index);
	}
}
