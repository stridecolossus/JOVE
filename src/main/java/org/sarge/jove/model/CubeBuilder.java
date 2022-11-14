package org.sarge.jove.model;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.util.MathsUtil;

/**
 * Builder for a cube constructed with {@link Primitive#TRIANGLES}.
 * @author Sarge
 */
public class CubeBuilder {
	// Vertices (ordered as quad strips)
	private static final Point[] VERTICES = {
			// Front
			new Point(-1, -1, 1),
			new Point(+1, -1, 1),
			new Point(-1, +1, 1),
			new Point(+1, +1, 1),

			// Back
			new Point(+1, -1, -1),
			new Point(-1, -1, -1),
			new Point(+1, +1, -1),
			new Point(-1, +1, -1),
	};

	// Face indices
	private static final int[][] FACES = {
			{ 0, 1, 2, 3 }, // Front
			{ 4, 5, 6, 7 }, // Back
			{ 1, 4, 3, 6 }, // Right
			{ 5, 0, 7, 2 }, // Left
			{ 3, 6, 2, 7 }, // Bottom
			{ 0, 5, 1, 4 }, // Top
	};

	// Face normals
	private static final Normal[] NORMALS = {
			Axis.Z,
			Axis.Z.invert(),
			Axis.X,
			Axis.X.invert(),
			Axis.Y,
			Axis.Y.invert(),
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
	public DefaultModel build() {
		final DefaultModel model = new DefaultModel(Primitive.TRIANGLES, new Layout(Point.LAYOUT, Coordinate2D.LAYOUT));
		build(model);
		return model;
	}

	/**
	 * Constructs a cube using the given builder.
	 * @param builder Model builder
	 * @see #vertex(Point, Vector, Coordinate, Colour)
	 */
	public void build(DefaultModel model) {
		for(int face = 0; face < FACES.length; ++face) {
			for(int corner : TRIANGLES) {
				// Lookup triangle index for this corner of the face
				final int index = FACES[face][corner];

				// Lookup vertex components
				final Point pos = VERTICES[index].multiply(size);
				final Normal normal = NORMALS[face];
				final Coordinate2D coord = Coordinate2D.QUAD.get(corner);
				final Colour col = COLOURS[face];

				// Build vertex
				final Vertex vertex = vertex(pos, normal, coord, col);
				model.add(vertex);
			}
		}
	}

	/**
	 * Builds a cube vertex.
	 * This implementation creates a {@link MutableVertex}, i.e. ignores the {@link #normal} and {@link #col} arguments.
	 * @param pos			Vertex position
	 * @param normal		Normal
	 * @param coord			Texture coordinate
	 * @param col			Colour
	 * @return New default vertex
	 * @see MutableVertex
	 */
	protected Vertex vertex(Point pos, Normal normal, Coordinate2D coord, Colour col) {
		return new DefaultVertex(pos, coord);
	}
}
