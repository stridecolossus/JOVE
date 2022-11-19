package org.sarge.jove.platform.obj;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * The <i>OBJ model</i> comprises the transient vertex data during parsing.
 * @author Sarge
 */
class ObjectModel {
	private final VertexComponentList<Point> positions = new VertexComponentList<>();
	private final VertexComponentList<Normal> normals = new VertexComponentList<>();
	private final VertexComponentList<Coordinate2D> coords = new VertexComponentList<>();
	private final List<DefaultMesh> models = new ArrayList<>();
	private DefaultMesh model;

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
		model = new RemoveDuplicateMesh(new Layout(layout));
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
	VertexComponentList<Coordinate2D> coordinates() {
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
		// Init vertex
		final var vertex = new MutableVertex() {
			@Override
			public Layout layout() {
				return model.layout();
			}
		};
		final Point pos = positions.get(v);
		vertex.position(pos);

		// Add optional normal
		if(vn != null) {
			final Normal normal = normals.get(vn);
			vertex.normal(normal);
			validate(Normal.LAYOUT);
		}

		// Add optional texture coordinate
		if(vt != null) {
			final Coordinate2D coord = coords.get(vt);
			vertex.coordinate(coord);
			validate(Coordinate2D.LAYOUT);
		}

		// Add vertex
		model.add(vertex);
	}

	/**
	 * @throws IllegalArgumentException if the given component is invalid for the current model layout
	 */
	private void validate(Component c) {
		if(!model.layout().contains(c)) {
			throw new IllegalArgumentException("Invalid vertex layout: " + c);
		}
	}

	/**
	 * Constructs the model(s).
	 * @return Model(s)
	 */
	public List<DefaultMesh> models() {
//		build();
		return new ArrayList<>(models);
	}
	// TODO - check all groups have same layout
}
