package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.CompoundLayout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * A <i>default vertex</i> is a custom implementation for the common case of vertices that comprise a position and texture coordinate.
 * @author Sarge
 */
public class DefaultVertex implements Vertex {
 	private static final CompoundLayout LAYOUT = new CompoundLayout(Point.LAYOUT, Coordinate2D.LAYOUT);

	private final Point pos;
	private final Coordinate2D coord;

	/**
	 * Constructor.
	 * @param pos			Vertex position
	 * @param coord			Texture coordinate
	 */
	public DefaultVertex(Point pos, Coordinate2D coord) {
		this.pos = notNull(pos);
		this.coord = notNull(coord);
	}

	@Override
	public Point position() {
		return pos;
	}

	@Override
	public CompoundLayout layout() {
		return LAYOUT;
	}

	@Override
	public void buffer(ByteBuffer bb) {
		pos.buffer(bb);
		coord.buffer(bb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, coord);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DefaultVertex that) &&
				this.pos.equals(that.pos) &&
				this.coord.equals(that.coord);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(pos)
				.append(coord)
				.build();
	}
}
