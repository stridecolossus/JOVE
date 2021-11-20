package org.sarge.jove.model;

import java.util.List;
import java.util.stream.Stream;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.util.MathsUtil;

/**
 * Builder for a cube constructed with {@link Primitive#TRIANGLES}.
 * @author Sarge
 */
public class CubeBuilder extends ModelBuilder {
	// Vertices
	private static final Point[] VERTICES = {
			// Front
			new Point(-1, +1, 1),
			new Point(-1, -1, 1),
			new Point(+1, +1, 1),
			new Point(+1, -1, 1),

			// Back
			new Point(+1, +1, -1),
			new Point(+1, -1, -1),
			new Point(-1, +1, -1),
			new Point(-1, -1, -1),
	};

	// Face indices
	private static final int[][] FACES = {
			{ 0, 1, 2, 3 }, // Front
			{ 4, 5, 6, 7 }, // Back
			{ 2, 3, 4, 5 }, // Right
			{ 6, 7, 0, 1 }, // Left
			{ 1, 7, 3, 5 }, // Bottom
			{ 6, 0, 4, 2 }, // Top
	};

	// Face normals
	private static final Vector[] NORMALS = {
			Vector.Z,
			Vector.Z.invert(),
			Vector.X,
			Vector.X.invert(),
			Vector.Y,
			Vector.Y.invert(),
	};

	// Face colours
	private static final Colour[] COLOURS = {
		new Colour(1, 0, 0),
		new Colour(0, 1, 0),
		new Colour(0, 0, 1),
		new Colour(1, 1, 0),
		Colour.BLACK,
		Colour.WHITE
	};

	// Indices of the two triangles for each face
	private static final int[] TRIANGLES = Stream
			.of(Quad.LEFT, Quad.RIGHT)
			.flatMap(List::stream)
			.mapToInt(Integer::intValue)
			.toArray();

	private float size = MathsUtil.HALF;

	/**
	 * Constructor.
	 */
	public CubeBuilder() {
		super.primitive(Primitive.TRIANGLES);
		layout(Point.LAYOUT, Model.NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT);
	}

	@Override
	public CubeBuilder primitive(Primitive primitive) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Sets the size of this cube.
	 * @param size Cube size
	 */
	public CubeBuilder size(float size) {
		this.size = size;
		return this;
	}

	@Override
	public Model build() {
		for(int face = 0; face < FACES.length; ++face) {
			for(int corner : TRIANGLES) {
				final int index = FACES[face][corner];
				final Point pos = VERTICES[index].scale(size);
				final Vector normal = NORMALS[face];
				final Coordinate coord = Quad.COORDINATES.get(corner);
				final Colour col = COLOURS[face];
				final Vertex vertex = Vertex.of(pos, normal, coord, col);
				super.add(vertex);
			}
		}
		return super.build();
	}
}
