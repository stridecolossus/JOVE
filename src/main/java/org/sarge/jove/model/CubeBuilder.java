package org.sarge.jove.model;

import org.sarge.jove.common.*;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.*;
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

	// Indices for the two counter-clockwise triangles of each face
	private static final int[] TRIANGLES = IndexFactory.TRIANGLES.indices(1).toArray();

	private float size = MathsUtil.HALF;

	/**
	 * Sets the size of this cube.
	 * @param size Cube size
	 */
	public CubeBuilder size(float size) {
		this.size = size;
		return this;
	}

	/**
	 * Constructs a cube with a default vertex layout (vertices and texture coordinates).
	 * @return New cube model
	 */
	public Model build() {
		final var builder = new Model.Builder()
				.primitive(Primitive.TRIANGLES)
				.layout(Point.LAYOUT)
				.layout(Coordinate2D.LAYOUT);

		build(builder);

		return builder.build();
	}

	/**
	 * Constructs a cube using the given builder.
	 * @param builder Model builder
	 */
	public void build(Model.Builder builder) {
		final var filter = builder.filter();
		for(int face = 0; face < FACES.length; ++face) {
			for(int corner : TRIANGLES) {
				// Lookup triangle index for this corner of the face
				final int index = FACES[face][corner];

				// Lookup vertex components
				final Point pos = VERTICES[index].scale(size);
				final Vector normal = NORMALS[face];
				final Coordinate coord = Coordinate2D.QUAD.get(corner);
				final Colour col = COLOURS[face];

				// Build vertex
				final Vertex vertex = Vertex
						.of(pos, normal, coord, col)
						.map(filter);

				// Add to cube
				builder.add(vertex);
			}
		}
	}
}
