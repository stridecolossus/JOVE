package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.model.Vertex.DefaultVertex;

/**
 * The <i>OBJ model</i> comprises the transient vertex data during parsing.
 * @author Sarge
 */
class ObjectModel {
	private final VertexComponentList<Point> positions = new VertexComponentList<>();
	private final VertexComponentList<Normal> normals = new VertexComponentList<>();
	private final VertexComponentList<Coordinate> coords = new VertexComponentList<>();
	private final List<DefaultModel> models = new ArrayList<>();
	private DefaultModel model;

	/**
	 * Starts a new object group.
	 */
	public void start() {
		// Ignore if current group is empty
		if(positions.isEmpty()) {
			return;
		}

		// Build current model group
		build();

//		// Reset transient model
//		positions.clear();
//		normals.clear();
//		coords.clear();
	}

	/**
	 * Constructs the current object.
	 */
	private void build() {
		// Init model layout
		final var layout = new ArrayList<Component>();
		layout.add(Point.LAYOUT);
		if(!normals.isEmpty()) {
			layout.add(Normal.LAYOUT);
		}
		if(!coords.isEmpty()) {
			layout.add(Coordinate2D.LAYOUT);
		}

		// Start new model
		model = new DuplicateModel(new Layout(layout));
		models.add(model);
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
	VertexComponentList<Normal> normals() {
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
		final var components = new ArrayList<Bufferable>();
		final Point pos = positions.get(v);
		components.add(pos);
		if(vn != null) {
			components.add(normals.get(vn));
		}
		if(vt != null) {
			components.add(coords.get(vt));
		}
		model.add(new DefaultVertex(components));
		// TODO - builder?
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<DefaultModel> models() {
//		build();
		return new ArrayList<>(models);
	}
	// TODO - check all groups have same layout
}
