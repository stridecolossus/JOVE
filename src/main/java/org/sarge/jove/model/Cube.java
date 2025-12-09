package org.sarge.jove.model;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * Builder for a cube.
 * @author Sarge
 */
public class Cube {
	// Cube vertices
	private final Point[] vertices = {
			// Front
			new Point(-1, -1, 1),
			new Point(-1, +1, 1),
			new Point(+1, -1, 1),
			new Point(+1, +1, 1),

			// Back
			new Point(-1, -1, -1),
			new Point(+1, -1, -1),
			new Point(-1, +1, -1),
			new Point(+1, +1, -1),
	};

	// Face indices
	private final int[][] faces = {
			{ 0, 1, 2, 3 }, // Front
			{ 4, 5, 6, 7 }, // Back
			{ 4, 6, 0, 1 }, // Left
			{ 2, 3, 5, 7 }, // Right
			{ 1, 6, 3, 7 }, // Bottom
			{ 4, 0, 5, 2 }, // Top
	};

	// Face normals
	private final Normal[] normals = {
			Axis.Z,
			Axis.Z.invert(),
			Axis.X,
			Axis.X.invert(),
			Axis.Y,
			Axis.Y.invert(),
	};

	// Face indices comprising a pair of counter-clockwise triangles
	private final int[] triangles = Quad.INDICES.stream().mapToInt(Integer::intValue).toArray();

	// Face texture coordinates
	private final Coordinate2D[] coordinates = Quad.COORDINATES.toArray(Coordinate2D[]::new);

	/**
	 * Constructs a cube of the given size about the origin comprising {@link Primitive#TRIANGLE} faces.
	 * @param Cube size
	 * @return Cube mesh
	 */
	public MutableMesh build(float size) {
		final MutableMesh mesh = new MutableMesh(Primitive.TRIANGLE, Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);

		for(int face = 0; face < faces.length; ++face) {
			for(int corner : triangles) {
				// Lookup triangle index for this corner of the face
				final int index = faces[face][corner];

				// Lookup vertex components
				final var v = new Vector(vertices[index]);
				final Point pos = new Point(v.multiply(size));
				final Normal normal = normals[face];
				final Coordinate2D coord = coordinates[corner];

				// Build vertex
				final Vertex vertex = new Vertex(pos, normal, coord);
				mesh.add(vertex);
			}
		}

		return mesh;
	}
}
