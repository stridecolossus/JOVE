package org.sarge.jove.model;

import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
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

	private Bufferable[] components;

	/**
	 * Constructor.
	 * @param components Vertex components
	 */
	public Vertex(Bufferable... components) {
		this.components = notNull(components);
	}

	/**
	 * Retrieves a vertex component by index.
	 * @param index Component index
	 * @return Vertex component
	 * @throws ArrayIndexOutOfBoundsException for an invalid index
	 */
	public Bufferable component(int index) {
		return components[index];
	}

	/**
	 * @return Vertex components
	 */
	public Stream<Bufferable> components() {
		return Arrays.stream(components);
	}

	/**
	 * Writes this vertex to the given buffer.
	 * @param buffer Buffer
	 */
	public void buffer(ByteBuffer buffer) {
		for(Bufferable obj : components) {
			obj.buffer(buffer);
		}
	}

	/**
	 * Transforms this vertex according to the given component indices.
	 * @param transform Resultant component indices
	 * @throws ArrayIndexOutOfBoundsException for an invalid component index
	 */
	public void transform(int[] transform) {
		final Bufferable[] prev = components;
		components = new Bufferable[transform.length];
		Arrays.setAll(components, n -> prev[transform[n]]);
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
