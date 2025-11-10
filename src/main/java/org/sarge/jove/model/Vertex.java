package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.geometry.*;

/**
 * A <i>vertex</i> is a mutable composition of the components of a mesh vertex.
 * @author Sarge
 */
public class Vertex implements Bufferable {
	private final Point pos;
	private Normal normal;
	private Coordinate coordinate;

	/**
	 * Constructor.
	 * @param pos Vertex position
	 */
	public Vertex(Point pos) {
		this.pos = requireNonNull(pos);
	}

	// TODO
	public Vertex(Point pos, Normal normal, Coordinate coordinate) {
		this(pos);
		normal(normal);
		coordinate(coordinate);
	}

	// TODO - reintro builder?

	/**
	 * @return Vertex position
	 */
	public Point position() {
		return pos;
	}
	// TODO - mutable but mandatory?

	/**
	 * @return Vertex normal
	 */
	public Normal normal() {
		return normal;
	}

	/**
	 * Sets the vertex normal.
	 * @param normal Vertex normal
	 */
	public void normal(Normal normal) {
		this.normal = normal;
	}

	/**
	 * @return Texture coordinate
	 */
	public Coordinate coordinate() {
		return coordinate;
	}

	/**
	 * Sets the texture coordinate of this vertex.
	 * @param coordinate Texture coordinate
	 */
	public void coordinate(Coordinate coordinate) {
		this.coordinate = coordinate;
	}

	/**
	 * Determines the layout of this vertex.
	 * @return Vertex layout
	 */
	public List<Layout> layout() {
		final var layout = new ArrayList<Layout>();
		layout.add(Point.LAYOUT);
		if(normal != null) {
			layout.add(Normal.LAYOUT);
		}
		if(coordinate != null) {
			layout.add(coordinate.layout());
		}
		return layout;
	}

	@Override
	public void buffer(ByteBuffer bb) {
		pos.buffer(bb);
		if(normal != null) {
			normal.buffer(bb);
		}
		if(coordinate != null) {
			coordinate.buffer(bb);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(pos, normal, coordinate);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				this.pos.equals(that.pos) &&
				Objects.equals(this.normal, that.normal) &&
				Objects.equals(this.coordinate, that.coordinate);
	}
}
