package org.sarge.jove.model;

import static java.util.stream.Collectors.*;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.common.Layout.Component;
import org.sarge.jove.geometry.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;

/**
 * A <i>mutable vertex</i> is a convenience catch-all vertex implementation.
 * <p>
 * Note that this is a convenience, non-optimal implementation intended for simple use-cases where performance or storage considerations are not a concern.
 * In particular the {@link #layout()} of this vertex is determined on-demand.
 * Generally custom vertex sub-classes should be implemented to support a given use-case with a fixed, overridden vertex {@link #layout()}.
 * <p>
 * @see SimpleVertex
 * @see DefaultVertex
 * @author Sarge
 */
public class MutableVertex implements Vertex {
	private Point pos;
	private Normal normal;
	private Coordinate2D coord;
	private Colour col;

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

	/**
	 * @return Vertex colour
	 */
	public Colour colour() {
		return col;
	}

	/**
	 * Sets the vertex colour.
	 * @param col Colour
	 */
	public void colour(Colour col) {
		this.col = notNull(col);
	}

	private Bufferable[] array() {
		return new Bufferable[]{pos, normal, coord, col};
	}

	@Override
	public CompoundLayout layout() {
		return Arrays
				.stream(array())
				.filter(Objects::nonNull)
				.map(Component.class::cast)
				.map(Component::layout)
				.collect(collectingAndThen(toList(), CompoundLayout::new));
	}

	@Override
	public void buffer(ByteBuffer bb) {
		Arrays
				.stream(array())
				.filter(Objects::nonNull)
				.forEach(obj -> obj.buffer(bb));
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
