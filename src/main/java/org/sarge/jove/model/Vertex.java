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
	 * @return Vertex position
	 */
	Point position();

	/**
	 * @return Layout of this vertex
	 */
	Layout layout();

	/**
	 * Builder for a vertex.
	 */
	class Builder {
		private Point pos;
		private Normal normal;
		private Coordinate2D coord;
		// TODO - colour?

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
		 * @throws IllegalArgumentException if the vertex is empty
		 */
		public Vertex build() {
			if(normal == null) {
				if(coord == null) {
					if(pos == null) throw new IllegalArgumentException("Vertex cannot be empty");
					return pos;
				}
				else {
					return new DefaultVertex(pos, coord);
				}
			}
			else {
				if(coord == null) {
					// TODO
					throw new UnsupportedOperationException();
				}
				else {
					// TODO - bit silly
					return new DefaultVertex(pos, coord).add(normal);
				}
			}
		}
	}
}
