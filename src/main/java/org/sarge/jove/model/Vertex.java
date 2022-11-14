package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * A <i>vertex</i> is an element of a {@link Model}.
 * @author Sarge
 */
public interface Vertex extends Bufferable {
	/**
	 * @return Layout of this vertex
	 */
	Layout layout();

	/**
	 * @return Vertex position
	 */
	Point position();

	/**
	 * Sets the normal of this vertex.
	 * @param normal Vertex normal
	 * @throws UnsupportedOperationException if this vertex does not have a normal
	 */
	default void normal(Normal normal) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Builder for a vertex.
	 * TODO
	 */
	class Builder {
		private Point pos;
		private Normal normal;
		private Coordinate2D coord;

		/**
		 * Sets the vertex position.
		 * @param pos Vertex position
		 */
		public Builder add(Point pos) {
			this.pos = notNull(pos);
			return this;
		}

		/**
		 * Sets the vertex normal.
		 * @param normal Vertex normal
		 */
		public Builder add(Normal normal) {
			this.normal = notNull(normal);
			return this;
		}

		/**
		 * Sets the texture coordinate.
		 * @param coord Texture coordinate
		 */
		public Builder add(Coordinate2D coord) {
			this.coord = notNull(coord);
			return this;
		}

		/**
		 * Constructs this vertex.
		 * @return New vertex
		 * @throws IllegalArgumentException if the vertex does not have a position
		 */
		public Vertex build() {
			if(pos == null) throw new IllegalArgumentException("Vertex must have a position");

			if(normal == null) {
				if(coord == null) {
					return new SimpleVertex(pos);
				}
				else {
					return new DefaultVertex(pos, coord);
				}
			}
			else {
				final var vertex = new MutableVertex();
				vertex.position(pos);
				vertex.normal(normal);
				if(coord != null) {
					vertex.coordinate(coord);
				}
				return vertex;
			}
		}
	}
}
