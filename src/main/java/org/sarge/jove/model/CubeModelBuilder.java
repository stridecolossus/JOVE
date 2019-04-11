package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.MutableVertex;

/**
 * Builder for a cube model.
 * @author Sarge
 */
public class CubeModelBuilder {
	// Face indices
	private static final int[][] FACES = {
		{ 0, 1, 2, 3 }, // Front
		{ 4, 5, 6, 7 }, // Back
		{ 6, 7, 0, 1 }, // Left
		{ 2, 3, 4, 5 }, // Right
		{ 6, 0, 4, 2 }, // Top
		{ 1, 7, 3, 5 }, // Bottom
	};

	// Face normals
	private static final Vector[] NORMALS = {
			Vector.Z_AXIS.invert(),
			Vector.Z_AXIS,
			Vector.X_AXIS.invert(),
			Vector.X_AXIS,
			Vector.Y_AXIS.invert(),
			Vector.Y_AXIS,
	};

	private static void add(Quad quad, List<Point> vertices) {
		quad.vertices().stream().map(MutableVertex::position).forEach(vertices::add);
	}

	/**
	 * Creates a cube model.
	 * @param size Cube size
	 * @return Cube model
	 */
	public Model<MutableVertex> build(float size) {
		// TODO - bit dumb, creates f/b quads twice!

		// Create front and back quads
		final Quad.Builder builder = new Quad.Builder().size(size);
		final Quad front = builder.depth(-size).build();
		final Quad back = builder.depth(size).reverse().build();

		// Create concatenated cube vertices
		final List<Point> vertices = new ArrayList<>(8);
		add(front, vertices);
		add(back, vertices);

		// Init model
		final Model.Builder<MutableVertex> model = new Model.Builder<MutableVertex>()
			.primitive(Primitive.TRIANGLE_LIST)
			.component(Vertex.Component.NORMAL)
			.component(Vertex.Component.coordinate(2));

		// Build cube faces
		for(int f = 0; f < FACES.length; ++f) {
			final int[] face = FACES[f];
			final Point[] array = Arrays.stream(face).mapToObj(vertices::get).toArray(Point[]::new);
			final Quad quad = new Quad(array, NORMALS[f]);
			quad.triangles().forEach(model::add);
		}

		// Construct cube
		return model.build();
	}
}
