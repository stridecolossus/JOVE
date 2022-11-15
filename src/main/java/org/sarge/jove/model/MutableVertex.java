package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * A <i>mutable vertex</i> is a convenience catch-all implementation for vertices comprising arbitrary vertex components.
 * <p>
 * Note that this implementation does <b>not</b> the vertex {@link #layout()} and therefore assumes that {@link DefaultMesh#validate(boolean)} is deactivated.
 * In general custom vertex implementations should be created to support a given use case, e.g. {@link SimpleVertex} or {@link DefaultVertex}.
 * <p>
 * @author Sarge
 */
public class MutableVertex implements Vertex {
	private Point pos;
	private Normal normal;
	private Coordinate2D coord;

	@Override
	public Point position() {
		return pos;
	}

	/**
	 * Sets the position of this vertex.
	 * @param pos Vertex position
	 */
	public void position(Point pos) {
		this.pos = notNull(pos);
	}

	/**
	 * @return Vertex normal
	 */
	public Normal normal() {
		return normal;
	}

	@Override
	public void normal(Normal normal) {
		this.normal = notNull(normal);
	}

	/**
	 * @return Texture coordinate
	 */
	public Coordinate2D coordinate() {
		return coord;
	}

	/**
	 * Sets the texture coordinate.
	 * @param coord Texture coordinate
	 */
	public void coordinate(Coordinate2D coord) {
		this.coord = notNull(coord);
	}

	@Override
	public Layout layout() {
		throw new UnsupportedOperationException();
	}

	public Bufferable[] array() {
		return new Bufferable[]{pos, normal, coord};
	}

	@Override
	public void buffer(ByteBuffer bb) {
		for(Bufferable b : array()) {
			if(b != null) {
				b.buffer(bb);
			}
		}
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(array());
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof MutableVertex that) &&
				Arrays.equals(this.array(), that.array());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(array()).build();
	}
}
