package org.sarge.jove.model;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collector;

import org.apache.commons.collections4.ListUtils;
import org.sarge.jove.io.Bufferable;

/**
 * A <i>vertex</i> is a compound object comprised of a collection of <i>components</i> such as vertex positions, normals, texture coordinates, etc.
 * @author Sarge
 */
public class Vertex implements Bufferable {
	private static final Collector<Bufferable, List<Bufferable>, Vertex> TRANSFORM = Collector.of(ArrayList::new, List::add, ListUtils::union, Vertex::new);

	/**
	 * Helper - Creates a vertex.
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
	 * @return Components of this vertex
	 */
	public List<Bufferable> components() {
		return components;
	}

	@Override
	public int length() {
		return components
				.stream()
				.mapToInt(Bufferable::length)
				.sum();
	}

	@Override
	public void buffer(ByteBuffer bb) {
		for(Bufferable c : components) {
			c.buffer(bb);
		}
	}

	/**
	 * Transforms this vertex to a new layout.
	 * @param layout Layout indices
	 * @return Transformed vertex
	 * @throws IndexOutOfBoundsException for an invalid layout index
	 */
	public Vertex transform(int[] layout) {
		return Arrays
				.stream(layout)
				.mapToObj(components::get)
				.collect(TRANSFORM);
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
