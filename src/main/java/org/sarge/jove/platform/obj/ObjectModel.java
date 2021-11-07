package org.sarge.jove.platform.obj;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Component;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.IndexedBuilder;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.ModelBuilder;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing.
 * @author Sarge
 */
public class ObjectModel {
	private final List<Point> positions = new VertexComponentList<>();
	private final List<Vector> normals = new VertexComponentList<>();
	private final List<Coordinate> coords = new VertexComponentList<>();
	private final List<Vertex> vertices = new ArrayList<>();
	private final List<Model> models = new ArrayList<>();

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Build current model group
		append();

		// Reset transient model
		positions.clear();
		normals.clear();
		coords.clear();
		vertices.clear();
	}

	/**
	 * Constructs the model for the current object.
	 */
	private void append() {
		// Ignore if current group is empty
		if(vertices.isEmpty()) {
			return;
		}

		// Create new model builder
		final ModelBuilder builder = builder();
		builder.primitive(Primitive.TRIANGLES);

		// Init model layout
		builder.layout(Point.LAYOUT);
		if(!normals.isEmpty()) {
			builder.layout(Vector.NORMALS);
		}
		if(!coords.isEmpty()) {
			builder.layout(Coordinate2D.LAYOUT);
		}

		// Add vertex data
		for(Vertex v : vertices) {
			builder.add(v);
		}

		// Add to models
		models.add(builder.build());
	}

	/**
	 * Creates a builder for a new group.
	 * @return New builder
	 */
	@SuppressWarnings("static-method")
	protected ModelBuilder builder() {
		return new IndexedBuilder();
	}

	/**
	 * Adds a vertex position.
	 * @param v Vertex position
	 */
	void position(Point v) {
		positions.add(v);
	}

	/**
	 * Adds a normal.
	 * @param vn Vertex normal
	 */
	void normal(Vector vn) {
		normals.add(vn);
	}

	/**
	 * Adds a texture coordinate.
	 * @param vt Texture coordinate
	 */
	void coordinate(Coordinate vt) {
		coords.add(vt);
	}

	/**
	 * Adds a vertex to the current model.
	 * <ul>
	 * <li>the vertex position is mandatory</li>
	 * <li>texture coordinate and normal are optional, i.e. can be {code null}</li>
	 * <li>indices start at <b>one</b> and can be negative</li>
	 * </ul>
	 * @param v			Vertex index
	 * @param vn		Normal index
	 * @param vt		Texture coordinate index
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public void vertex(int v, Integer vn, Integer vt) {
		// Add vertex position
		final List<Component> components = new ArrayList<>();
		components.add(positions.get(v));

		// Add optional normal
		if(vn != null) {
			components.add(normals.get(vn));
		}

		// Add optional texture coordinate
		if(vt != null) {
			components.add(coords.get(vt));
		}

		// Construct vertex
		final Vertex vertex = new Vertex(components);
		vertices.add(vertex);
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<Model> build() {
		append();
		return new ArrayList<>(models);
	}
}
