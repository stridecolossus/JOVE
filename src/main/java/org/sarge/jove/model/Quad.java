package org.sarge.jove.model;

import static org.sarge.jove.model.Coordinate.Coordinate2D.*;

import java.util.List;

import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * Quad utilities.
 * @author Sarge
 */
public class Quad {
	/**
	 * Indices for a quad comprising a pair of counter-clockwise triangles.
	 * <p>
	 * Quad vertices are assumed to be arranged as follows (from the perspective of the viewer):
	 * <pre>
	 * 0 - 2
	 * | / |
	 * 1 - 3
	 * </pre>
	 */
	public static final List<Integer> INDICES = List.of(
			0, 1, 2,
			1, 3, 2
	);

	/**
	 * @return Quad texture coordinates
	 */
	public static final List<Coordinate2D> COORDINATES = List.of(
			TOP_LEFT,
			BOTTOM_LEFT,
			TOP_RIGHT,
			BOTTOM_RIGHT
	);

	/**
	 * Creates a quad centred on the origin with vertex normals pointing towards the viewer.
	 * @param size Quad size
	 * @return Quad
	 * @see #INDICES
	 */
	public static Mesh build(float size) {
		// Build vertices
		final Normal normal = Axis.Z.invert();
		final Vertex[] vertices = {
				new Vertex(new Point(-size, -size, 0), normal, TOP_LEFT),
				new Vertex(new Point(-size, +size, 0), normal, BOTTOM_LEFT),
				new Vertex(new Point(+size, -size, 0), normal, TOP_RIGHT),
				new Vertex(new Point(+size, +size, 0), normal, BOTTOM_RIGHT)
		};

		// Build quad mesh comprising two counter-clockwise triangles
		final MutableMesh mesh = new MutableMesh(Primitive.TRIANGLE, Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);
		for(int n : INDICES) {
			mesh.add(vertices[n]);
		}

		return mesh;
	}

	// TODO - helper, transform to rotate quad to face viewer?
}
