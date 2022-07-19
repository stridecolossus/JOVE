package org.sarge.jove.model;

import static java.util.stream.Collectors.joining;

import java.nio.ByteBuffer;
import java.util.*;

import org.sarge.jove.common.Bufferable;

/**
 * A <i>vertex</i> is compound bufferable objects comprising a set of arbitrary vertex components.
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * Creates a new vertex from the given component array.
	 * @param components Vertex components
	 * @return New vertex
	 */
	public static Vertex of(Bufferable... components) {
		return new Vertex(Arrays.asList(components));
	}

	private final List<Bufferable> components;

	/**
	 * Constructor.
	 * @param components Vertex components
	 */
	public Vertex(List<Bufferable> components) {
		this.components = List.copyOf(components);
	}

	/**
	 * Retrieves a component from this vertex by index.
	 * @param <T> Component type
	 * @param index Component index
	 * @return Vertex component
	 * @throws IndexOutOfBoundsException if the index is invalid for this vertex
	 * @throws ClassCastException if the specified component is not the expected type
	 */
	@SuppressWarnings("unchecked")
	public <T> T component(int index) {
		return (T) components.get(index);
	}

	@Override
	public int length() {
		return components.stream().mapToInt(Bufferable::length).sum();
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(Bufferable b : components) {
			b.buffer(buffer);
		}
	}

	/**
	 * Converts the data of this vertex to match the specified component types.
	 * @param types Required component types
	 * @return New vertex
	 */
	public Vertex map(List<Class<? extends Bufferable>> types) {
		final List<Bufferable> result = components
				.stream()
				.filter(e -> types.contains(e.getClass()))
				.toList();

		return new Vertex(result);
	}

	/**
	 * Tests whether the components of this vertex <b>exactly</b> match the given required types.
	 * @param types Required component types
	 * @return Whether this vertex matches the given component types
	 */
	public boolean matches(List<Class<? extends Bufferable>> types) {
		final int count = types.size();
		if(components.size() != count) {
			return false;
		}

		for(int n = 0; n < count; ++n) {
			if(components.get(n).getClass() != types.get(n)) {
				return false;
			}
		}

		return true;
	}

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
		return components
				.stream()
				.map(Bufferable::toString)
				.collect(joining(","));
	}
}
