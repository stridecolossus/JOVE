package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Layout;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;

/**
 * A <i>vertex</i> is a mutable compound object representing vertex data in a model.
 * <p>
 * A vertex is essentially a wrapper for an array of bufferable vertex components such as position, normals, etc.
 * The {@link #transform(int[])} mutator can be used to remove or reorder vertex components.
 * <p>
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
	 * Helper - Creates a vertex with the given components.
	 * @param components Vertex components
	 * @return New vertex
	 */
	public static Vertex of(Bufferable... components) {
		final Vertex vertex = new Vertex();
		for(Bufferable b : components) {
			vertex.add(b);
		}
		return vertex;
	}

	private final List<Bufferable> components = new ArrayList<>();

	/**
	 * Retrieves a vertex component by index.
	 * @param index Component index
	 * @return Vertex component
	 * @throws IndexOutOfBoundsException for an invalid index
	 */
	public Bufferable component(int index) {
		return components.get(index);
	}

	/**
	 * @return Vertex components
	 */
	public Stream<Bufferable> components() {
		return components.stream();
	}

	/**
	 * Adds a component to this vertex.
	 * @param component Vertex component
	 */
	public void add(Bufferable component) {
		components.add(notNull(component));
	}

	/**
	 * Writes this vertex to the given buffer.
	 * @param bb Buffer
	 */
	public void buffer(ByteBuffer bb) {
		for(Bufferable b : components) {
			b.buffer(bb);
		}
	}

	/**
	 * Transforms this vertex according to the given component indices.
	 * @param transform Resultant component indices
	 * @throws ArrayIndexOutOfBoundsException for an invalid component index
	 */
	public void transform(int[] transform) {
		// Reset vertex
		final Bufferable[] prev = components.toArray(Bufferable[]::new);
		components.clear();

		// Apply components transform
		for(int n : transform) {
			components.add(prev[n]);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(components);
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Vertex that) &&
				this.components.equals(that.components);
	}

	@Override
	public String toString() {
		return components.toString();
	}
}
