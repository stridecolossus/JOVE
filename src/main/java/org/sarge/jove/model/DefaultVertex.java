package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.lib.util.Check;

/**
 * Default implementation for a vertex comprised of a position and texture coordinate.
 * The {@link #add(Normal)} method wraps this vertex with a normal.
 * @author Sarge
 */
public class DefaultVertex implements Vertex {
	private static final Layout LAYOUT = new Layout(Point.LAYOUT, Coordinate2D.LAYOUT);

	private final Point pos;
	private final Coordinate2D coord;

	/**
	 * Constructor.
	 * @param pos		Vertex position
	 * @param coord		Texture coordinate
	 */
	public DefaultVertex(Point pos, Coordinate2D coord) {
		this.pos = notNull(pos);
		this.coord = notNull(coord);
	}

	/**
	 * Copy constructor.
	 * @param vertex Vertex to copy
	 */
	protected DefaultVertex(DefaultVertex vertex) {
		this(vertex.pos, vertex.coord);
	}

	@Override
	public final Point position() {
		return pos;
	}

	/**
	 * @return Vertex normal or {@code null} if none
	 */
	protected Normal normal() {
		return null;
	}

	/**
	 * Adds a normal to this vertex.
	 * @param normal Vertex normal
	 * @return New vertex
	 * @throws IllegalStateException if this vertex already has a normal
	 */
	public DefaultVertex add(Normal normal) {
		Check.notNull(normal);
		return new DefaultVertex(this) {
			private static final Layout NORMAL_LAYOUT = new Layout(Point.LAYOUT, Normal.LAYOUT, Coordinate2D.LAYOUT);

			@Override
			protected Normal normal() {
				return normal;
			}

			@Override
			public DefaultVertex add(Normal normal) {
				throw new IllegalStateException("Vertex already has a normal");
			}

			@Override
			public Layout layout() {
				return NORMAL_LAYOUT;
			}

			@Override
			public void buffer(ByteBuffer bb) {
				pos.buffer(bb);
				normal.buffer(bb);
				coord.buffer(bb);
			}
		};
	}

	@Override
	public Layout layout() {
		return LAYOUT;
	}

	@Override
	public void buffer(ByteBuffer bb) {
		pos.buffer(bb);
		coord.buffer(bb);
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, coord, normal());
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof DefaultVertex that) &&
				this.pos.equals(that.pos) &&
				this.coord.equals(that.coord) &&
				Objects.equals(this.normal(), that.normal());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(pos)
				.append(normal())
				.append(coord)
				.build();
	}
}
