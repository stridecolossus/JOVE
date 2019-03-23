package org.sarge.jove.model;

import java.util.Arrays;
import java.util.List;

import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Tuple;
import org.sarge.jove.geometry.Tuple.Swizzle;
import org.sarge.jove.texture.TextureCoordinate;

/**
 * A <i>quad</i> is a square set of vertices.
 * @author Sarge
 */
public final class Quad {
	private static final float[][] VERTICES = {{0, 1}, {0, 0}, {1, 1}, {1, 0}};

	private final List<Vertex> vertices;

	/**
	 * Constructor.
	 * @param vertices Quad vertices
	 */
	private Quad(Vertex[] vertices) {
		this.vertices = Arrays.asList(vertices);
	}

	/**
	 * @return Quad vertices in clockwise order
	 */
	public List<Vertex> vertices() {
		return vertices;
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
			this.swizzle = swizzle;
			return this;
		}

		/**
		 * Creates a reverse quad, i.e. back-to-front.
		 */
		public Builder reverse() {
			reverse = true;
			return this;
		}

		/**
		 * Sets the size of this quad.
		 * @param size Size
		 */
		public Quad build() {
			// Build quad corner vertices
			final Vertex[] vertices = new Vertex[VERTICES.length];
			for(int n = 0; n < vertices.length; ++n) {
				// Calculate vertex coordinates
				final float x = size * (VERTICES[n][0] * 2 - 1);
				final float y = size * (VERTICES[n][1] * 2 - 1);
				final float z = size * depth;

				// Create vertex position
				final float dx = reverse ? -x : +x;
				final Tuple pos = swizzle.apply(new Tuple(dx, y, z));

				// Create vertex
				vertices[n] = new Vertex(new Point(pos)).coords(TextureCoordinate.of(VERTICES[n]));
			}

			// Create quad
			return new Quad(vertices);
		}
	}
}
