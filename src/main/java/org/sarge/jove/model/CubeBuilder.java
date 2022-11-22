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
	 * @return New cube mesh
	 */
	public DefaultMesh build() {
		final DefaultMesh model = new DefaultMesh(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Coordinate2D.LAYOUT));
		build(model);
		return model;
	}

	/**
	 * Constructs a cube using the given builder.
	 * @param mesh Mesh
	 * @see #vertex(Point, Vector, Coordinate, Colour)
	 */
	public void build(DefaultMesh mesh) {
		for(int face = 0; face < FACES.length; ++face) {
			for(int corner : TRIANGLES) {
				// Lookup triangle index for this corner of the face
				final int index = FACES[face][corner];

				// Lookup vertex components
				final Point pos = VERTICES[index].multiply(size);
				final Normal normal = NORMALS[face];
				final Coordinate2D coord = Coordinate2D.QUAD.get(corner);

				// Build vertex
				final Vertex vertex = vertex(pos, normal, coord);
				mesh.add(vertex);
			}
		}
	}

	/**
	 * Builds a cube vertex.
	 * This implementation creates a {@link DefaultVertex}, i.e. ignores the {@link #normal}.
	 * @param pos			Vertex position
	 * @param normal		Normal
	 * @param coord			Texture coordinate
	 * @return Cube vertex
	 */
	protected Vertex vertex(Point pos, Normal normal, Coordinate2D coord) {
		return new DefaultVertex(pos, coord);
	}
}
