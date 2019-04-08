package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Tuple;
import org.sarge.jove.geometry.Tuple.Swizzle;
import org.sarge.jove.geometry.Vector;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.texture.TextureCoordinate.Coordinate2D;

/**
 * A <i>quad</i> is a square set of vertices.
 * @author Sarge
 */
public final class Quad {
	/**
	 * Unit quad vertices in X-Y plane.
	 */
	private static final int[][] VERTICES = {
			{-1, -1},
			{-1, +1},
			{+1, -1},
			{+1, +1}
	};

	/**
	 * Quad triangle indices in counter-clockwise order.
	 */
	private static final int[] TRIANGLES = {
			0, 1, 2,
			2, 1, 3
	};

	private static final int LENGTH = 4;

	private final List<MutableVertex> vertices;

	/**
	 * Constructor.
	 * @param vertices		Quad vertices
	 * @param normal		Normal
	 * @throws IllegalArgumentException if the given array is not of length four
	 */
	public Quad(Point[] vertices, Vector normal) {
		if(vertices.length != LENGTH) throw new IllegalArgumentException("Quad must be comprised of 4 vertices");

		final MutableVertex[] quad = new MutableVertex[LENGTH];
		for(int n = 0; n < LENGTH; ++n) {
			final MutableVertex v = new MutableVertex(vertices[n]);
			v.normal(normal);
			v.coordinates(Coordinate2D.QUAD.get(n));
			quad[n] = v;
		}

		this.vertices = Arrays.asList(quad);
	}

	/**
	 * @return Quad vertices
	 */
	public List<MutableVertex> vertices() {
		return vertices;
	}

	/**
	 * Creates the two counter-clockwise triangles comprising this quad.
	 * @return Quad triangle vertices
	 */
	public Stream<MutableVertex> triangles() {
		return Arrays.stream(TRIANGLES).mapToObj(vertices::get);
	}

	/**
	 * Builder for a quad.
	 */
	public static class Builder {
		private float size = 1;
		private float depth = 0;
		private Tuple.Swizzle swizzle = Swizzle.NONE;
		private boolean reverse;

		/**
		 * Sets the size of this quad.
		 * @param size Size
		 */
		public Builder size(float size) {
			this.size = size;
			return this;
		}

		/**
		 * Sets the depth (or Z coordinate) of this quad.
		 * @param depth Depth
		 */
		public Builder depth(float depth) {
			this.depth = depth;
			return this;
		}

		/**
		 * Applies the given swizzle to the quad vertices.
		 * @param swizzle Swizzle
		 */
		public Builder swizzle(Tuple.Swizzle swizzle) {
			this.swizzle = notNull(swizzle);
			return this;
		}

		/**
		 * Builds a reverse facing quad.
		 */
		public Builder reverse() {
			this.reverse = true;
			return this;
		}

		/**
		 * Constructs this quad.
		 * @return New quad
		 */
		public Quad build() {
			// Build quad vertices
			final Point[] vertices = new Point[LENGTH];
			for(int n = 0; n < LENGTH; ++n) {
				final float x = VERTICES[n][0] * size;
				final float y = VERTICES[n][1] * size;
				final Tuple pos = swizzle.apply(new Point(reverse ? -x : x, y, depth));
				vertices[n] = new Point(pos);
			}

			// Determine normal
			final Tuple normal = swizzle.apply(Vector.Z_AXIS.invert());

			// Create quad
			return new Quad(vertices, new Vector(normal));
		}
	}
}
