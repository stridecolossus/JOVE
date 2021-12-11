package org.sarge.jove.model;

import java.util.List;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.Component;
import org.sarge.jove.util.MathsUtil;

/**
 * Builder for a cube constructed with {@link Primitive#TRIANGLES}.
 * @author Sarge
 */
public class CubeBuilder {
	// Vertices
	private static final Point[] VERTICES = {
			// Front
			new Point(-1, +1, 1),
			new Point(-1, -1, 1),
			new Point(+1, +1, 1),
			new Point(+1, -1, 1),

			// Back
			new Point(+1, +1, -1),
			new Point(+1, -1, -1),
			new Point(-1, +1, -1),
			new Point(-1, -1, -1),
	};

	// Face indices
	private static final int[][] FACES = {
			{ 0, 1, 2, 3 }, // Front
			{ 4, 5, 6, 7 }, // Back
			{ 2, 3, 4, 5 }, // Right
			{ 6, 7, 0, 1 }, // Left
			{ 1, 7, 3, 5 }, // Bottom
			{ 6, 0, 4, 2 }, // Top
	};

	// Face normals
	private static final Vector[] NORMALS = {
			Vector.Z,
			Vector.Z.invert(),
			Vector.X,
			Vector.X.invert(),
			Vector.Y,
			Vector.Y.invert(),
	};

	// Face colours
	private static final Colour[] COLOURS = {
		new Colour(1, 0, 0),
		new Colour(0, 1, 0),
		new Colour(0, 0, 1),
		new Colour(1, 1, 0),
		Colour.BLACK,
		Colour.WHITE
	};

	// Indices of the two triangles for each face
	private static final int[] TRIANGLES = Quad.strip(1, false).toArray();

	private final List<Component> components;

	private float size = MathsUtil.HALF;

	/**
	 * Constructor.
	 * @param Vertex components
	 */
	public CubeBuilder(List<Component> layout) {
		this.components = List.copyOf(layout);
	}

	/**
	 * Default constructor for a cube with vertex position and texture coordinates.
	 */
	public CubeBuilder() {
		this(List.of(Component.POSITION, Component.COORDINATE));
	}

	/**
	 * Sets the size of this cube.
	 * @param size Cube size
	 */
	public CubeBuilder size(float size) {
		this.size = size;
		return this;
	}

	/**
	 * Constructs this cube.
	 * @return New cube
	 */
	public Model build() {
		// Init model
		final DefaultModel model = new DefaultModel(Primitive.TRIANGLES);
		model.layout(components);

		// Build cube
		for(int face = 0; face < FACES.length; ++face) {
			for(int corner : TRIANGLES) {
				// Lookup triangle index for this corner of the face
				final int index = FACES[face][corner];

				// Lookup vertex components
				final Point pos = VERTICES[index].scale(size);
				final Vector normal = NORMALS[face];
				final Coordinate coord = Quad.COORDINATES.get(corner);
				final Colour col = COLOURS[face];

				// Add vertex
				final Vertex data = new Vertex(pos, normal, coord, col);
				final Vertex vertex = data.transform(components);
				model.add(vertex);
			}
		}

		return model;
	}
}
