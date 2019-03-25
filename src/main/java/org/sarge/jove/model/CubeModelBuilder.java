package org.sarge.jove.model;

import java.util.ArrayList;
import java.util.List;

import org.sarge.jove.model.Model.Builder;
import org.sarge.jove.model.Vertex.MutableVertex;

/**
 * Builder for a cube model.
 * @author Sarge
 */
public class CubeModelBuilder {
	private static final int[][] FACES = new int[][] {
		{ 0, 1, 2, 3 }, // Front
		{ 4, 5, 6, 7 }, // Back
		{ 6, 7, 0, 1 }, // Left
		{ 2, 3, 4, 5 }, // Right
		{ 6, 0, 4, 2 }, // Top
		{ 1, 7, 3, 5 }, // Bottom
	};

	/**
	 * Creates a cube model.
	 * @param size Cube size
	 * @return Cube model
	 */
	public Model<MutableVertex> build(float size) {
		// Create concatenated list of front and back cube vertices
		final Quad.Builder quad = new Quad.Builder().size(size);
		final List<Vertex> front = quad.build().vertices();
		final List<Vertex> back = quad.reverse().build().vertices();
		final List<Vertex> vertices = new ArrayList<>(front);
		vertices.addAll(back);

		// Build cube consisting of two triangles per face
		final Builder<MutableVertex> model = new Builder<>().primitive(Primitive.TRIANGLE);
		for(int[] face : FACES) {
			add(vertices, face, 0, model);
			add(vertices, face, 1, model);
		}

		// Construct cube and generate normals
		// TODO - bit pointless calculating normals as they are implicit per face?
		return model.build().computeNormals();
	}

	/**
	 * Adds a triangle.
	 * @param face		Face indices
	 * @param start		Start index (0 or 1)
	 * @param model		Model builder
	 */
	private static void add(List<Vertex> vertices, int[] face, int start, Builder<MutableVertex> model) {
		for(int n = start; n < start + 3; ++n) {
			// TODO - need to sort this hierarchy
			model.add((MutableVertex) vertices.get(n));
		}
	}
}
