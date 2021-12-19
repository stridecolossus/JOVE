package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
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
 * TODO
 * A <i>vertex</i> is a compound object comprised of:
 * <ul>
 * <li>vertex position</li>
 * <li>normal</li>
 * <li>texture coordinate</li>
 * <li>colour</li>
 * </ul>
 * <p>
 * TODO
 * Notes that all components are optional except for the vertex position.
 * <p>
 * TODO
 * The {@link Component} enumeration specifies the elements comprising a vertex and is used to {@link #transform(List)} a vertex to a different layout.
 * <p>
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * Vertex normals component.
	 */
	public static final Layout NORMALS = Layout.floats(Vector.SIZE);

	/**
	 * Default vertex layout.
	 */
	public static final List<Layout> DEFAULT_LAYOUT = List.of(Point.LAYOUT, NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT);

	private Point pos;
	private Vector normal;
	private Coordinate coord;
	private Colour col;

	/**
	 * @return Vertex position
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
	 * @return Optional normal
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
	 * @return Optional texture coordinate
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
	 * @return Optional colour
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
	 * @return Vertex components
	 */
	public List<Bufferable> components() {
		final List<Bufferable> components = new ArrayList<>();
		components.add(pos);
		components.add(normal);
		components.add(coord);
		components.add(col);
		components.removeIf(Objects::isNull);
		return components;
	}

	// TODO
	public void retain(List<Layout> layout) {
	}

	@Override
	public int length() {
		final List<Bufferable> components = this.components();
		return components.stream().mapToInt(Bufferable::length).sum();
	}

	@Override
	public void buffer(ByteBuffer bb) {
		final List<Bufferable> components = this.components();
		components.forEach(c -> c.buffer(bb));
	}

	@Override
	public int hashCode() {
		return Objects.hash(components());
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				this.components().equals(that.components());
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(components()).build();
	}
}
