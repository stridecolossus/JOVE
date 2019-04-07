package org.sarge.jove.model;

import static java.util.stream.Collectors.joining;

import java.util.Arrays;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.texture.TextureCoordinate.Coordinate2D;

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

	/**
	 * Creates a cube model.
	 * @param size Cube size
	 * @return Cube model
	 */
	public Model<MutableVertex> build(float size) {
		// Create concatenated list of front and back cube vertices
		final Quad.Builder builder = new Quad.Builder().size(size);
		final Quad front = builder.depth(-size).build();
		final Quad back = builder.depth(size).reverse().build();
//		final List<MutableVertex> vertices = new ArrayList<>(8);
//		vertices.addAll(front.vertices());
//		vertices.addAll(back.vertices());

		final Point[] vertices = {
			new Point(-size, -size, -size),
			new Point(-size, +size, -size),
			new Point(+size, -size, -size),
			new Point(+size, +size, -size),

			new Point(+size, -size, +size),
			new Point(+size, +size, +size),
			new Point(-size, -size, +size),
			new Point(-size, +size, +size),
		};

		System.out.println(Arrays.stream(vertices).map(Object::toString).collect(joining("\n")));
		System.out.println(front.vertices());
		System.out.println(back.vertices());

		// Init model
		final Model.Builder<MutableVertex> model = new Model.Builder<>()
			.primitive(Primitive.TRIANGLE_LIST)
			.component(Vertex.Component.NORMAL)
			.component(Vertex.Component.coordinate(2));

		// Build cube consisting of two triangles per face
		//for(int[] face : FACES) {
		for(int f = 0; f < FACES.length; ++f) {
//			switch(f) {
//			case 0:
//			case 1:
//			//case 2:
//			//case 3:
//			case 4:
//			case 5:
//				break;
//
//			default:
//				continue;
//			}
			final int[] face = FACES[f];

			final MutableVertex[] verts = new MutableVertex[4];
			for(int n = 0; n < face.length; ++n) {
				final Point pos = vertices[face[n]];
				final MutableVertex v = new MutableVertex(pos);
				//final MutableVertex v = vertices.get(face[n]);
				v.coordinates(Coordinate2D.QUAD.get(n));
				verts[n] = v;
			}
			//System.out.println("verts\n"+Arrays.stream(verts).map(Object::toString).collect(joining("\n")));

			model.add(verts[0]);
			model.add(verts[1]);
			model.add(verts[2]);

			model.add(verts[2]);
			model.add(verts[1]);
			model.add(verts[3]);

			//final var verts = Arrays.stream(face).mapToObj(vertices::get).toArray(MutableVertex[]::new);
			//final Quad quad = new Quad(verts);
			//quad.triangles().forEach(model::add);
		}

		// Construct cube
		return model.build();
	}
}
