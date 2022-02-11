package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vertex</i> is a mutable object comprising the position, normal, texture coordinate and colour of a model vertex.
 * @author Sarge
 */
public class Vertex {
	/**
	 * Layout for vertex normals.
	 */
	public static final Layout NORMALS = Layout.floats(Vector.SIZE);

	/**
	 * Default vertex layout.
	 */
	public static final List<Layout> LAYOUT = List.of(Point.LAYOUT, NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT);

	/**
	 * Creates a simple vertex.
	 * @param pos Vertex position
	 * @return New simple vertex
	 */
	public static Vertex of(Point pos) {
		return new Vertex().position(pos);
	}

	private Point pos;
	private Vector normal;
	private Coordinate coord;
	private Colour col;

	/**
	 * @return Vertex position or {@code null} if none
	 */
	public Point position() {
		return pos;
	}

	/**
	 * Sets the position of this vertex.
	 * @param pos Vertex position
	 */
	public Vertex position(Point pos) {
		this.pos = notNull(pos);
		return this;
	}

	/**
	 * @return Vertex normal or {@code null} if none
	 */
	public Vector normal() {
		return normal;
	}

	/**
	 * Sets the normal of this vertex.
	 * @param normal Vertex normal
	 */
	public Vertex normal(Vector normal) {
		this.normal = notNull(normal);
		return this;
	}

	/**
	 * @return Texture coordinate of this vertex or {@code null} if none
	 */
	public Coordinate coordinate() {
		return coord;
	}

	/**
	 * Sets the texture coordinate of this vertex.
	 * @param coord Texture coordinate
	 */
	public Vertex coordinate(Coordinate coord) {
		this.coord = notNull(coord);
		return this;
	}

	/**
	 * @return Colour of this vertex normal or {@code null} if none
	 */
	public Colour colour() {
		return col;
	}

	/**
	 * Sets the colour of this vertex.
	 * @param col Vertex colour
	 */
	public Vertex colour(Colour col) {
		this.col = notNull(col);
		return this;
	}

	/**
	 * Writes this vertex to the given buffer.
	 * @param bb Buffer
	 */
	public void buffer(ByteBuffer bb) {
		buffer(pos, bb);
		buffer(normal, bb);
		buffer(coord, bb);
		buffer(col, bb);
	}

	private static void buffer(Bufferable obj, ByteBuffer bb) {
		if(obj != null) {
			obj.buffer(bb);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, normal, coord, col);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				Objects.equals(this.pos, that.pos) &&
				Objects.equals(this.normal, that.normal) &&
				Objects.equals(this.coord, that.coord) &&
				Objects.equals(this.col, that.col);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append(pos)
				.append(normal)
				.append(coord)
				.append(col)
				.build();
	}
}
