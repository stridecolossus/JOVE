package org.sarge.jove.platform.obj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.DefaultModel;
import org.sarge.jove.model.IndexedBuilder;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing.
 * @author Sarge
 */
class ObjectModel {
	private final List<Point> positions = new VertexComponentList<>();
	private final List<Vector> normals = new VertexComponentList<>();
	private final List<Coordinate> coords = new VertexComponentList<>();
	private final Map<Vertex, Integer> map = new HashMap<>();
	private final List<DefaultModel> models = new ArrayList<>();
	private DefaultModel current;

	/**
	 * Constructor.
	 */
	public ObjectModel() {
		init();
	}

	/**
	 * Initialises the current group.
	 */
	private void init() {
		current = new IndexedBuilder(Primitive.TRIANGLES);
	}

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
		map.clear();
	}

	/**
	 * Constructs the current object.
	 */
	private void append() {
		// Ignore if current group is empty
		if(current.count() == 0) {
			return;
		}

		// Init model layout
		current.layout(Point.LAYOUT);
		if(!normals.isEmpty()) {
			current.layout(Vector.LAYOUT);
		}
		if(!coords.isEmpty()) {
			current.layout(Coordinate2D.LAYOUT);
		}

		// Add to models
		models.add(current);
		init();
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
		final Vertex.Builder builder = new Vertex.Builder();
		builder.position(positions.get(v));

		// Add optional normal
		if(vn != null) {
			builder.normal(normals.get(vn));
		}

		// Add optional texture coordinate
		if(vt != null) {
			builder.coordinate(coords.get(vt));
		}

		// Construct vertex
		current.add(builder.build());
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<Model> build() {
		append();
		return new ArrayList<>(models);
	}
	// TODO - check all groups have same layout
}
