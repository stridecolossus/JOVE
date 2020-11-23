package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;

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

	// Triangle indices for a face
	private static final int[] LEFT = {0, 1, 2};
	private static final int[] RIGHT = {2, 1, 3};

	// Quad texture coordinates
	private static final Coordinate2D[] QUAD = Coordinate2D.QUAD.toArray(Coordinate2D[]::new);

	// Default layout
	private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE);
	// TODO - setter?

	/**
	 * Convenience factory method to create a unit-cube.
	 * @return New cube
	 */
	public static Model create() {
		return new CubeBuilder().size(1).build();
	}

	private final Model.Builder builder = new Model.Builder().primitive(Primitive.TRIANGLES).layout(LAYOUT);
	private float size = 1;
	private boolean clockwise = false;

	/**
	 * Sets the size of this cube (default is {@code one}).
	 * @param size Cube size
	 */
	public CubeBuilder size(float size) {
		this.size = size;
		return this;
	}

	/**
	 * Sets the triangle winding order (default is {@code false}, anti-clockwise).
	 * @param clockwise Whether triangles are clockwise or anti-clockwise
	 */
	public CubeBuilder order(boolean clockwise) {
		this.clockwise = clockwise;
		return this;
	}

	/**
	 * Creates a model for this cube.
	 * @return New cube model
	 */
	public Model build() {
		// Add two triangles for each cube face
		for(int[] face : FACES) {
			add(face, LEFT);
			add(face, RIGHT);
		}

		// Construct model
		return builder.build();
	}

	/**
	 * Adds a triangle.
	 * @param face			Quad face indices
	 * @param triangle		Triangle indices within this face
	 */
	private void add(int[] face, int[] triangle) {
		// Build triangle vertices
		final List<Vertex> vertices = new ArrayList<>(3);
		for(int n = 0; n < 3; ++n) {
			// Lookup vertex position for this triangle
			final int index = face[triangle[n]];
			final Point pos = VERTICES[index].scale(size);

			// Lookup texture coordinate
			final Coordinate2D coord = QUAD[triangle[n]];

			// Build vertex
			final Vertex v = new Vertex.Builder()
					.position(pos)
					.coords(coord)
					.build();

			// Add to model
			vertices.add(v);
		}

		// Reverse for clockwise winding order
		if(clockwise) {
			Collections.reverse(vertices);
		}

		// Add to model
		vertices.forEach(builder::add);
	}
}
