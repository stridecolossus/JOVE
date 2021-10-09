package org.sarge.jove.common;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.Vector;
import org.sarge.lib.util.Check;

/**
 * A <i>vertex</i> is a compound object comprised of a collection of {@link Component} such as vertex positions, normals, texture coordinates, etc.
 * @author Sarge
 */
public class Vertex implements Bufferable {
	/**
	 * A <i>vertex component</i> is a bufferable object that can be comprised in a vertex.
	 */
	public interface Component extends Bufferable {
		/**
		 * @return Layout of this component
		 */
		Layout layout();

		@Override
		default int length() {
			return this.layout().length();
		}
	}

	/**
	 * A <i>layout</i> is a descriptor for the structure of a compound object such as an image or vertex.
	 * <p>
	 * A layout is comprised of:
	 * <ul>
	 * <li>the {@link #type} of each element, e.g. {@link Float}</li>
	 * <li>the {@link #size} which specifies the number of <i>elements</i> that comprise the component, e.g. 3 for a vertex normal</li>
	 * <li>the number of {@link #bytes} per element, e.g. {@link Float#BYTES}</li>
	 * </ul>
	 * <p>
	 * The following are synonymous for a floating-point 3-tuple:
	 * <pre>
	 *     // Constructor
	 *     new Layout(3, Float.BYTES, Float.class);
	 *
	 *     // Factory
	 *     Layout.of(3, Float.class);
	 *     Layout.of(3, float.class);
	 *     Layout.of(3, Float.TYPE);
	 *
	 *     // Convenience floating-point factory
	 *     Layout.of(3);
	 * </pre>
	 * <p>
	 */
	public record Layout(int size, int bytes, Class<?> type) {
		/**
		 * Creates a layout for the given type.
		 * <p>
		 * Notes:
		 * <ul>
		 * <li>The {@link #type} parameter can be either a wrapper or primitive type</li>
		 * <li>The number of {@link #bytes} is specified by the corresponding constant, e.g. {@link Float#BYTES}</li>
		 * <li>The type returned by {@link #type()} is always the wrapper type, e.g. {@link Float}</li>
		 * </ul>
		 * <p>
		 * The following types are supported:
		 * <ul>
		 * <li>float</li>
		 * <li>integer</li>
		 * <li>short</li>
		 * <li>byte</li>
		 * </ul>
		 * <p>
		 * @param size			Size of this layout (number of components)
		 * @param type			Component type
		 * @return New layout
		 * @throws IllegalArgumentException for an unsupported component type
		 */
		public static Layout of(int size, Class<?> type) {
			return switch(type.getSimpleName().toLowerCase()) {
				case "float" 			-> new Layout(size, Float.BYTES, Float.class);
				case "int", "integer" 	-> new Layout(size, Integer.BYTES, Integer.class);
				case "short" 			-> new Layout(size, Short.BYTES, Short.class);
				case "byte"				-> new Layout(size, Byte.BYTES, Byte.class);
				default -> throw new IllegalArgumentException("Unsupported component type: " + type.getSimpleName());
			};
		}

		/**
		 * Helper - Creates a layout with {@link #size} floating-point components.
		 * @param size Size of this layout (number of components)
		 * @return New floating-point layout
		 */
		public static Layout of(int size) {
			return new Layout(size, Float.BYTES, Float.class);
		}

		/**
		 * Helper - Calculates the total <i>stride</i> of the given vertex layout, i.e. the sum of the {@link Layout#length()}
		 * @param layout Vertex layout
		 * @return Stride
		 */
		public static int stride(Collection<Layout> layout) {
			return layout.stream().mapToInt(Vertex.Layout::length).sum();
		}

		/**
		 * Constructor.
		 * @param size			Size of this layout (number of components)
		 * @param bytes			Number of bytes per component
		 * @param type			Component type
		 */
		public Layout {
			Check.oneOrMore(size);
			Check.oneOrMore(bytes);
			Check.notNull(type);
		}

		/**
		 * @return Length of a components of this layout (bytes)
		 */
		public int length() {
			return size * bytes;
		}
	}

	/**
	 * Creates a vertex from the given array of components.
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

	/**
	 * Convenience helper.
	 * @return Layout of this vertex
	 */
	public List<Layout> layout() {
		return components.stream().map(Component::layout).collect(toList());
	}

	/**
	 * Retrieves a vertex component by index.
	 * @param <T> Component type
	 * @param index Index
	 * @return Vertex component
	 * @throws ArrayIndexOutOfBoundsException for an invalid index
	 */
	@SuppressWarnings("unchecked")
	public <T extends Bufferable> T get(int index) {
		return (T) components.get(index);
	}

	@Override
	public int length() {
		return components.stream().mapToInt(Component::length).sum();
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(Component obj : components) {
			obj.buffer(buffer);
		}
	}

	@Override
	public int hashCode() {
		return components.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return (obj instanceof Vertex that) && this.components.equals(that.components);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append(components).build();
	}

	/**
	 * Builder for a vertex.
	 */
	public static class Builder {
		private Point pos;
		private Vector normal;
		private Coordinate coord;
		private Colour col;

		/**
		 * Sets the vertex position.
		 * @param pos Vertex position
		 */
		public Builder position(Point pos) {
			this.pos = notNull(pos);
			return this;
		}

		/**
		 * Sets the vertex normal.
		 * @param normal Vertex normal
		 */
		public Builder normal(Vector normal) {
			this.normal = notNull(normal);
			return this;
		}

		/**
		 * Sets the vertex texture coordinate.
		 * @param coord Vertex coordinate
		 */
		public Builder coordinate(Coordinate coord) {
			this.coord = notNull(coord);
			return this;
		}

		/**
		 * Sets the vertex colour.
		 * @param col Vertex colour
		 */
		public Builder colour(Colour col) {
			this.col = notNull(col);
			return this;
		}

		/**
		 * Constructs this vertex.
		 * @return New vertex
		 */
		public Vertex build() {
			final var components = Stream.of(pos, normal, coord, col).filter(Objects::nonNull).collect(toList());
			return new Vertex(components);
		}
	}
}
