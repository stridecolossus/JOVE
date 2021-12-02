package org.sarge.jove.model;

import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Component;
import org.sarge.jove.common.Layout;

/**
 * A <i>vertex</i> is a compound object comprised of a collection of <i>components</i> such as vertex positions, normals, texture coordinates, etc.
 * @see Component
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * Helper - Creates a vertex.
	 * @param components Vertex components
	 * @return New vertex
	 */
	public static Vertex of(Component... components) {
		return new Vertex(Arrays.asList(components));
	}

	private final List<Component> components;

	/**
	 * Constructor.
	 * @param components Vertex components
	 */
	public Vertex(List<Component> components) {
		this.components = List.copyOf(components);
	}

	/**
	 * @return Components of this vertex
	 */
	public List<Component> components() {
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
	 * Transforms this vertex to the given layout.
	 * TODO - identity, matches first, example
	 * @param layouts New layout
	 * @return Transformed vertex
	 * @throws IllegalArgumentException if this vertex does not contain <b>all</b> components matching the given layout
	 */
	public Vertex transform(List<Layout> layouts) {
		return layouts
				.stream()
				.map(this::map)
				.collect(collectingAndThen(toList(), Vertex::new));
	}

	/**
	 * Looks up the <b>first</b> vertex component matching the given layout.
	 * @param layout Layout to match
	 * @return Vertex component
	 * @throws IllegalArgumentException if this vertex does not contain a matching component
	 */
	private Component map(Layout layout) {
		for(Component c : components) {
			if(c.layout() == layout) {
				return c;
			}
		}
		throw new IllegalArgumentException("Invalid layout for this vertex: " + layout);
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
