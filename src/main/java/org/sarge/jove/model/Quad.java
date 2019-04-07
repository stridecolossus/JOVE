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
	private static final Coordinate2D[] QUAD = Coordinate2D.QUAD.toArray(Coordinate2D[]::new);

	private static final int[] TRIANGLES = {
		0, 1, 2,
		2, 1, 3
	};

	private final List<MutableVertex> vertices;

	/**
	 * Constructor.
	 * @param vertices Quad vertices
	 */
	public Quad(MutableVertex[] vertices) {
		this.vertices = Arrays.asList(vertices);
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
	 * Builder for a quad in the XY plane.
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
			final MutableVertex[] vertices = new MutableVertex[QUAD.length];
			for(int n = 0; n < QUAD.length; ++n) {
				// Create vertex coordinates
				final Coordinate2D coords = QUAD[n];
				final float x = size * (coords.u * 2 - 1);
				final float y = size * (coords.v * 2 - 1);

				// Create vertex position
				final float dx = reverse ? -x : x;
				final Tuple pos = swizzle.apply(new Point(dx, -y, depth));

				// Determine normal
				final Tuple normal = swizzle.apply(Vector.Z_AXIS.invert());

				// Create vertex
				final MutableVertex vertex = new MutableVertex();
				vertex.position(new Point(pos));
				vertex.normal(new Vector(normal));
				vertex.coordinates(coords);
				vertices[n] = vertex;
			}

			// Create quad
			return new Quad(vertices);
		}
	}
}
