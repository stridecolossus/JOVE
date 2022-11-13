package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.geometry.Point;

/**
 * A <i>vertex</i> TODO
 * @author Sarge
 */
public interface Vertex extends Bufferable {
	/**
	 * @return Vertex position
	 */
	Point position();

	// TODO - common implementation for pos + coord (grid, cube)

	/**
	 *
	 */
	class DefaultVertex implements Vertex {
		private final Bufferable[] data;

		public DefaultVertex(Bufferable... data) {
			// TODO
			this.data = notNull(data);
		}

		public DefaultVertex(List<Bufferable> data) {
			this(data.toArray(Bufferable[]::new));
		}

		@Override
		public Point position() {
			return (Point) data[0];
		}

		@Override
		public void buffer(ByteBuffer bb) {
			for(Bufferable b : data) {
				b.buffer(bb);
			}
		}

		@Override
		public int hashCode() {
			return Arrays.hashCode(data);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof DefaultVertex that) &&
					Arrays.equals(this.data, that.data);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append(data).build();
		}
	}
}
