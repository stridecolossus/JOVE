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
	 *
	 * @param filter
	 * @return
	 */
	public Vertex map(List<?> filter) {
		final var result = components
				.stream()
				.filter(c -> filter.contains(c.getClass()))
				.toList();

		return new Vertex(result);
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
