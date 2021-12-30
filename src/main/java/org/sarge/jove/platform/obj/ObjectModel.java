package org.sarge.jove.platform.obj;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.MutableModel;
import org.sarge.jove.model.Vertex;

/**
 * The <i>OBJ model</i> holds the transient vertex data during parsing.
 * @author Sarge
 */
class ObjectModel {
	private final List<Point> positions = new VertexComponentList<>();
	private final List<Vector> normals = new VertexComponentList<>();
	private final List<Coordinate> coords = new VertexComponentList<>();
	private final List<Model> models = new ArrayList<>();
	private MutableModel current = new DuplicateVertexModel();

	// TODO - extends MutableModel + above + linked list to previous model(s)?

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore if current group is empty
		if(current.isEmpty()) {
			return;
		}

		// Build current model group
		buildCurrentGroup();

		// Reset transient model
		positions.clear();
		normals.clear();
		coords.clear();
	}

	/**
	 * Constructs the current object.
	 */
	private void buildCurrentGroup() {
		// Init model layout
		final List<Layout> layout = new ArrayList<>();
		layout.add(Point.LAYOUT);
		if(!normals.isEmpty()) {
			layout.add(Vertex.NORMALS);
		}
		if(!coords.isEmpty()) {
			layout.add(Coordinate2D.LAYOUT);
		}
		// TODO - transform vertices

		// Add model
		models.add(current);

		// Start new model
		current = new DuplicateVertexModel();
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
		final List<Bufferable> components = new ArrayList<>();
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
		final Vertex vertex = new Vertex(components.toArray(Bufferable[]::new));
		current.add(vertex);
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<Model> build() {
		buildCurrentGroup();
		return new ArrayList<>(models);
	}
	// TODO - check all groups have same layout
}
