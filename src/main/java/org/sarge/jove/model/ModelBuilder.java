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
 * 	Model model = new ModelBuilder()
 * 		.layout(Point.LAYOUT)
 * 		.layout(Model.NORMALS)
 * 		.primitive(Primitive.TRIANGLES)
 * 		.add(Vertex.of(...))
 * 		.build();
 * </pre>
 * <p>
 * @see Vertex
 * @see Layout
 * @author Sarge
 */
public class ModelBuilder {
	protected final List<Vertex> vertices = new ArrayList<>();
	private final List<Layout> layout = new ArrayList<>();
	private Primitive primitive = Primitive.TRIANGLE_STRIP;
	private boolean validate = true;

	/**
	 * Adds a vertex component layout to this model.
	 * @param layout Layout
	 * @throws IllegalStateException if the model contains vertex data
	 */
	public ModelBuilder layout(Layout layout) {
		if(!vertices.isEmpty()) throw new IllegalStateException("Cannot modify model layout after adding vertex data");
		this.layout.add(notNull(layout));
		return this;
	}

	/**
	 * Adds vertex component layouts.
	 * @param layouts Layouts
	 */
	public ModelBuilder layout(Layout... layouts) {
		for(Layout layout : layouts) {
			layout(layout);
		}
		return this;
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
	 * @see #validate(Vertex)
	 */
	public ModelBuilder add(Vertex vertex) {
		if(validate) {
			validate(vertex);
		}
		vertices.add(vertex);
		return this;
	}

	/**
	 * Sets whether vertices are validated against the layout of this model.
	 * @param validate Whether to validate vertices (default is {@code true})
	 */
	public ModelBuilder validate(boolean validate) {
		this.validate = validate;
		return this;
	}

	/**
	 * Validates that the given vertex matches the layout for this model.
	 * @param vertex Vertex to validate
	 * @throws IllegalArgumentException if the vertex does not match the layout of this model
	 */
	private void validate(Vertex vertex) {
		if(vertex.length() != Layout.stride(layout)) {
			throw new IllegalArgumentException("Invalid vertex for this layout: " + vertex);
		}
	}
	// TODO - this should be better than just comparing length, but bufferable does not have layout

	// TODO
	// compute normals
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
		return build(null, vertices.size());
	}

	/**
	 * Constructs this model.
	 * @param index		Index or {@code null} if not indexed
	 * @param count		Number of vertices
	 * @return New model
	 */
	protected final Model build(int[] index, int count) {
		final Header header = new Header(layout, primitive, count);
		return DefaultModel.of(header, vertices, index);
	}
}
