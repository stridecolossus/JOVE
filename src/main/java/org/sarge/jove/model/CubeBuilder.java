package org.sarge.jove.model;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Vertex;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.util.MathsUtil;

/**
 * Builder for a cube constructed as a {@link Primitive#TRIANGLES}.
 * @author Sarge
 */
public class CubeBuilder {
	// Vertices
	private static final Point[] VERTICES = {
			// Front
			new Point(-1, -1, 1),
			new Point(-1, +1, 1),
			new Point(+1, -1, 1),
			new Point(+1, +1, 1),

			// Back
			new Point(+1, -1, -1),
			new Point(+1, +1, -1),
			new Point(-1, -1, -1),
			new Point(-1, +1, -1),
	};

	// Face indices
	private static final int[][] FACES = {
			{ 0, 1, 2, 3 }, // Front
			{ 4, 5, 6, 7 }, // Back
			{ 6, 7, 0, 1 }, // Left
			{ 2, 3, 4, 5 }, // Right
			{ 6, 0, 4, 2 }, // Top
			{ 1, 7, 3, 5 }, // Bottom
	};

	// Indices for the two triangles making up each face
	private static final int[] TRIANGLES = Stream
			.of(Quad.LEFT, Quad.RIGHT)
			.flatMap(List::stream)
			.mapToInt(Integer::intValue)
			.toArray();

	private final DefaultModel.Builder builder = new DefaultModel.Builder().primitive(Primitive.TRIANGLES);
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
	 * Constructs this cube.
	 * @return New cube model
	 */
	public Model build() {
		for(int[] face : FACES) {
			for(int corner : TRIANGLES) {
				// Lookup vertex for this triangle
				final int index = face[corner];
				final Point pos = VERTICES[index].scale(size);

				// Lookup texture coordinate for this corner
				final Coordinate2D tc = Quad.COORDINATES.get(corner);

				// Add quad vertex to model
				final Vertex v = new Vertex.Builder()
						.position(pos)
						.coordinate(tc)
						.build();
				builder.add(v);
			}
		}

		return builder.build();
	}
}
