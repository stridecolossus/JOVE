package org.sarge.jove.model;

import java.nio.ByteBuffer;

import org.sarge.jove.common.CompoundLayout;
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

	/**
	 *
	 * TODO - use bit pattern
	 *
	 * range 0..7
	 * bits -Z X Y
	 * generates front face (at least) with counter-clockwise order
	 *
	 * =>
	 *
	 * no need for array of positions or face indices
	 * and probably ditto normals?
	 *
	 */

//	@Test
//	void test() {
//		for(int n = 0; n < 8; ++n) {
//			float x = (n & 2) != 0 ? +1 : -1;
//			float y = (n & 1) != 0 ? +1 : -1;
//			float z = (n & 4) != 0 ? -1 : +1;
//			System.out.println(n+" "+x+","+y+","+z);
//		}
//	}


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
	 * Constructs a cube mesh comprised of {@link Primitive#TRIANGLE}.
	 * @return Cube mesh
	 * @see #vertex(Point, Normal, Coordinate2D)
	 */
	public Mesh build() {
		final MeshBuilder builder = new MeshBuilder(Primitive.TRIANGLE, new CompoundLayout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT));
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
				builder.add(vertex);
			}
		}

		return builder.mesh();
	}

	/**
	 * Builds a cube vertex.
	 * Override for a custom vertex implementation.
	 * @param pos			Vertex position
	 * @param normal		Normal
	 * @param coord			Texture coordinate
	 * @return Cube vertex
	 */
	protected Vertex vertex(Point pos, Normal normal, Coordinate2D coord) {
		// TODO - 1. silly ref to builder 2. replace with general vertex implementation?
		return new Vertex(pos) {
			@Override
			public void buffer(ByteBuffer bb) {
				pos.buffer(bb);
				normal.buffer(bb);
				coord.buffer(bb);
			}
		};
	}
}
