package org.sarge.jove.platform.obj;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Vertex;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.DefaultModel.Builder;
import org.sarge.jove.model.DefaultModel.IndexedBuilder;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing.
 * @author Sarge
 */
public class ObjectModel {
	private final List<Point> vertices = new VertexComponentList<>();
	private final List<Vector> normals = new VertexComponentList<>();
	private final List<Coordinate> coords = new VertexComponentList<>();
	private final List<Builder> builders = new ArrayList<>();

	private Builder current;

	public ObjectModel() {
		add();
	}

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore if the current group is empty
		if(vertices.isEmpty()) {
			return;
		}

		// Reset transient model
		vertices.clear();
		normals.clear();
		coords.clear();

		// Start new model
		add();
	}

	/**
	 * Adds a new model builder.
	 */
	private void add() {
		current = builder();
		current.primitive(Primitive.TRIANGLES);
		current.clockwise(true);
		builders.add(current);
	}

	/**
	 * Creates a builder for a new group.
	 * @return New builder
	 */
	@SuppressWarnings("static-method")
	protected Builder builder() {
		return new IndexedBuilder();
	}

	/**
	 * Adds a vertex position.
	 * @param v Vertex position
	 */
	void vertex(Point v) {
		vertices.add(v);
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
		final var vertex = new Vertex.Builder();
		vertex.position(vertices.get(v));

		// Add optional normal
		if(vn != null) {
			vertex.normal(normals.get(vn));
		}

		// Add optional texture coordinate
		if(vt != null) {
			vertex.coordinate(coords.get(vt));
		}

		// Add vertex
		current.add(vertex.build());
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 * @throws IllegalArgumentException if the models cannot be constructed
	 */
	public Stream<Model> build() {
		return builders.stream().map(Builder::build);
	}
}
