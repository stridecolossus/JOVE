package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.*;

/**
 * The <i>OBJ model</i> comprises the transient vertex data during parsing.
 * @author Sarge
 */
class ObjectModel {
	private final VertexComponentList<Point> positions = new VertexComponentList<>();
	private final VertexComponentList<Vector> normals = new VertexComponentList<>();
	private final VertexComponentList<Coordinate> coords = new VertexComponentList<>();
	private final List<Model> models = new ArrayList<>();
	private Model model = new DuplicateModel();
	private boolean empty = true;

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore if current group is empty
		if(empty) {
			return;
		}

		// Build current model group
		build();

		// Reset transient model
		positions.clear();
		normals.clear();
		coords.clear();
		empty = true;
	}

	/**
	 * Constructs the current object.
	 */
	private void build() {
		// Init model layout
		model.layout(Point.LAYOUT);
		if(!normals.isEmpty()) {
			model.layout(Model.NORMALS);
		}
		if(!coords.isEmpty()) {
			model.layout(Coordinate2D.LAYOUT);
		}

		// Add model
		models.add(model);

		// Start new model
		model = new DuplicateModel();
	}

	/**
	 * @return Vertex positions
	 */
	VertexComponentList<Point> positions() {
		return positions;
	}

	/**
	 * @return Normals
	 */
	VertexComponentList<Vector> normals() {
		return normals;
	}

	/**
	 * @return Texture coordinates
	 */
	VertexComponentList<Coordinate> coordinates() {
		return coords;
	}

	/**
	 * Adds a vertex to the current model.
	 * <ul>
	 * <li>the vertex position is mandatory</li>
	 * <li>texture coordinates and normals are optional, i.e. can be {@code null}</li>
	 * <li>indices start at <b>one</b> and can be negative</li>
	 * </ul>
	 * @param v			Vertex index
	 * @param vn		Normal index
	 * @param vt		Texture coordinate index
	 * @throws IndexOutOfBoundsException for an invalid index
	 * @see VertexComponentList#get(int)
	 */
	public void vertex(int v, Integer vn, Integer vt) {
		// Add vertex position
		final var components = new ArrayList<Bufferable>();
		components.add(positions.get(v));

		// Add optional normal
		if(vn != null) {
			components.add(normals.get(vn));
		}

		// Add optional texture coordinate
		if(vt != null) {
			components.add(coords.get(vt));
		}

		// Add vertex to model
		model.add(new Vertex(components));
		empty = false;
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<Model> models() {
		build();
		return new ArrayList<>(models);
	}
	// TODO - check all groups have same layout
}
