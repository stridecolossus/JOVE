package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;

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
	 * Builder for a quad in the XZ plane.
	 */
	public static class Builder {
		private float size = 1;
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
		 * Applies the given swizzle to the quad vertices.
		 * @param swizzle Swizzle
		 */
		public Builder swizzle(Tuple.Swizzle swizzle) {
			this.swizzle = notNull(swizzle);
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
		 * Constructs this quad.
		 * @return New quad
		 */
		public Quad build() {
			// Build quad corner vertices
			final Vertex[] vertices = new Vertex[VERTICES.length];
			for(int n = 0; n < vertices.length; ++n) {
				// Calculate vertex coordinates
				final float x = size * (VERTICES[n][0] * 2 - 1);
				final float z = size * (VERTICES[n][1] * 2 - 1);

				// Create vertex position
				final float dx = reverse ? -x : +x;
				final Tuple pos = swizzle.apply(new Point(dx, 0, z));

				// Determine normal
				final Vector normal = reverse ? Vector.Z_AXIS : Vector.Z_AXIS.invert();

				// Create vertex
				// TODO - need to reverse coords?
				final MutableVertex vertex = new MutableVertex();
				vertex.position(new Point(pos));
				vertex.normal(normal);
				vertex.coordinates(new Coordinate2D(VERTICES[n][0], VERTICES[n][1])); // TODO - array ctor
				vertices[n] = vertex;
			}

			// Create quad
			return new Quad(vertices);
		}
	}
}
