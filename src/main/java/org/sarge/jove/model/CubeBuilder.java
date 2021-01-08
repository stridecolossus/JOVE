package org.sarge.jove.model;

import java.util.stream.Stream;

import org.sarge.jove.geometry.Coordinate.Coordinate2D;
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

	// Indices for the two triangles per face quad
	private static final int[] TRIANGLES = Stream.concat(Quad.LEFT.stream(), Quad.RIGHT.stream()).mapToInt(Integer::intValue).toArray();

	// Default layout
	private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE);
	// TODO - setter? compute normals?

	/**
	 * Convenience method to create a unit-cube.
	 * @return New cube
	 */
	public static Model create() {
		return new CubeBuilder().size(MathsUtil.HALF).build();
	}

	private final Model.Builder builder = new Model.Builder().primitive(Primitive.TRIANGLES).layout(LAYOUT);
	private float size = 1;

	/**
	 * Sets the size of this cube (default is {@code one}).
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
				// Lookup cube vertex for this triangle
				final int index = face[corner];
				final Point pos = VERTICES[index].scale(size);

				// Lookup texture coordinate for this corner
				final Coordinate2D tc = Quad.COORDINATES.get(corner);

				// Build vertex
				final Vertex v = new Vertex.Builder()
						.position(pos)
						.coords(tc)
						.build();

				// Add quad vertex to model
				builder.add(v);
			}
		}
		return builder.build();
	}
}
