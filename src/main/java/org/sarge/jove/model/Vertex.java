package org.sarge.jove.model;

import static java.util.Objects.requireNonNull;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>vertex</i> is a mutable composition of the components of a mesh vertex.
 * @author Sarge
 */
public class Vertex implements Bufferable {
	private final List<Bufferable> components = new ArrayList<>();

	/**
	 * Constructor.
	 * @param components Vertex components
	 * @throws NullPointerException if any component is {@code null}
	 */
	public Vertex(Bufferable... components) {
		for(Bufferable c : components) {
			add(c);
		}
	}
	// TODO - better as immutable / record?

	/**
	 * @return Components of this vertex
	 */
	public List<Bufferable> components() {
		return new ArrayList<>(components);
	}

	/**
	 * Retrieves a component of this vertex by index.
	 * @param <T> Component type
	 * @param index Component index
	 * @return Component
	 * @throws IndexOutOfBoundsException if {@link #index} is invalid for this vertex
	 */
	@SuppressWarnings("unchecked")
	public <T> T component(int index) {
		return (T) components.get(index);
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(Bufferable b : components) {
			b.buffer(buffer);
		}
	}

	/**
	 * Removes a component from this vertex.
	 * @param index Component index
	 * @throws IndexOutOfBoundsException if {@link #index} is invalid for this vertex
	 */
	public void remove(int index) {
		components.remove(index);
	}
	// TODO - only used to strip normals? better to make this a record and return new vertex with mutated list?

	/**
	 * Adds a component to this vertex.
	 * @param component Component to add
	 */
	public void add(Bufferable component) {
		requireNonNull(component);
		components.add(component);
	}
	// TODO - ONLY used by OBJ => easier to just create mutable list -> vertex

	@Override
	public int hashCode() {
		return components.hashCode();
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
