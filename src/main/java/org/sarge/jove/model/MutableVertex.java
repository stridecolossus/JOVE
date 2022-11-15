package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * A <i>mutable vertex</i> is a convenience catch-all implementation for vertices comprising arbitrary vertex components.
 * <p>
 * The components of this vertex (if present) are written in the following order by the {@link #buffer(ByteBuffer)} method:
 * <ol>
 * <li>position</li>
 * <li>normal</li>
 * <li>texture coordinate</li>
 * </ol>
 * <p>
 * Note that the {@link #layout()} of this vertex is determined on-demand.
 * <p>
 * @see SimpleVertex
 * @see DefaultVertex
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
		final var components = new ArrayList<Component>();
		if(pos != null) {
			components.add(Point.LAYOUT);
		}
		if(normal != null) {
			components.add(Normal.LAYOUT);
		}
		if(coord != null) {
			components.add(Coordinate2D.LAYOUT);
		}
		return new Layout(components);
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
