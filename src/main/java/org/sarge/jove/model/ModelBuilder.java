package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Layout;
import org.sarge.jove.common.Layout.MutableCompoundLayout;
import org.sarge.jove.common.Vertex;
import org.sarge.jove.model.Model.Header;

/**
 * Builder for a model.
 * <p>
 * Usage:
 * <pre>
 * 	// Create builder
 * 	ModelBuilder builder = new ModelBuilder()
 * 		.layout(Point.LAYOUT)
 * 		.layout(Vector.NORMALS)
 * 		.layout(Coordinate2D.LAYOUT)
 * 		.primitive(Primitive.TRIANGLES)
 * 		.validate(true);
 *
 * 	// Add vertex data
 * 	Vertex vertex = ...
 * 	builder.add(vertex);
 * 	...
 *
 * 	// Build model
 * 	Model model = builder.build();
 * </pre>
 * <p>
 * @author Sarge
 */
public class ModelBuilder {
	protected final List<Vertex> vertices = new ArrayList<>();
	protected final MutableCompoundLayout layout = new MutableCompoundLayout();
	private Primitive primitive = Primitive.TRIANGLE_STRIP;
	private boolean validate;

	/**
	 * Adds a vertex component layout.
	 * @param layout Vertex component layout
	 * @throws IllegalArgumentException for a duplicate component layout
	 * @throws IllegalStateException if the model contains vertex data
	 */
	public ModelBuilder layout(Layout layout) {
		if(!isEmpty()) throw new IllegalStateException("Cannot modify model layout after adding vertex data");
		this.layout.add(layout);
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
	 * @return Whether this model is empty
	 */
	public boolean isEmpty() {
		return vertices.isEmpty();
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
	 * @param validate Whether to validate vertices (default is {@code false})
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
		final MutableCompoundLayout compound = new MutableCompoundLayout();
		vertex.layout().forEach(compound::add);
		if(!compound.equals(layout)) {
			throw new IllegalArgumentException(String.format("Invalid vertex for this layout: expected=%s actual=%s", layout, compound));
		}
	}

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
		return new DefaultModel(new Header(layout, primitive, count), vertices, index);
	}
}