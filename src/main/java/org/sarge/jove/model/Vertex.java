package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vertex</i> is a mutable bufferable object comprising some or all of the following components:
 * <ul>
 * <li>vertex position</li>
 * <li>normal</li>
 * <li>texture coordinate</li>
 * <li>colour</li>
 * </ul>
 * <p>
 * The {@link #NORMALS} is a special case layout for vertices that contain normals.
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
	public static final List<Layout> LAYOUT = List.of(Point.LAYOUT, NORMALS, Coordinate2D.LAYOUT, Colour.LAYOUT);

	/**
	 * Components of this vertex.
	 */
	public enum Component {
		POSITION,
		NORMAL,
		COORDINATE,
		COLOUR
	}

	private static final List<Component> COMPONENTS = Arrays.asList(Component.values());
	private static final int SIZE = COMPONENTS.size();

	/**
	 * Transforms the given vertices to retain only the specified vertex components.
	 * @param vertices			Vertices
	 * @param components		Components to retain
	 */
	public static void retain(Collection<Vertex> vertices, Collection<Component> components) {
		// Determine components to remove
		final int[] index = COMPONENTS
				.stream()
				.filter(Predicate.not(components::contains))
				.mapToInt(Component::ordinal)
				.toArray();

		// Clear removed fields
		for(Vertex v : vertices) {
			for(int n : index) {
				v.components[n] = null;
			}
		}
	}

	private final Bufferable[] components = new Bufferable[SIZE];

	/**
	 * Sets the position of this vertex.
	 * @param pos Vertex position
	 */
	public Vertex position(Point pos) {
		components[0] = notNull(pos);
		return this;
	}

	/**
	 * @return Optional normal
	 */
	public Vector normal() {
		return (Vector) components[1];
	}

	/**
	 * Sets the normal of this vertex.
	 * @param normal Vertex normal
	 */
	public Vertex normal(Vector normal) {
		components[1] = notNull(normal);
		return this;
	}

	/**
	 * Sets the texture coordinate of this vertex.
	 * @param coord Texture coordinate
	 */
	public Vertex coordinate(Coordinate coord) {
		components[2] = notNull(coord);
		return this;
	}

	/**
	 * Sets the colour of this vertex.
	 * @param col Vertex colour
	 */
	public Vertex colour(Colour col) {
		components[3] = notNull(col);
		return this;
	}

	/**
	 * @return Vertex components
	 */
	public Stream<Bufferable> components() {
		return Arrays.stream(components).filter(Objects::nonNull);
	}

	@Override
	public int length() {
		// TODO - is this ever actually used?
		return components().mapToInt(Bufferable::length).sum();
	}

	@Override
	public void buffer(ByteBuffer bb) {
		final Stream<Bufferable> components = this.components();
		components.forEach(c -> c.buffer(bb));
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(components);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				Arrays.equals(this.components, that.components);
	}

	@Override
	public String toString() {
		return Arrays.toString(components);
	}
}
