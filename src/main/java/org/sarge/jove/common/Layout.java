package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.lib.util.Check;

/**
 * A <i>layout</i> is a descriptor for the structure of compound data such as an image or vertex.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>{@link #components} as a string representation of the <i>component</i> mapping of the data, e.g. {@code RGB} for a point or vector</li>
 * <li>the {@link #type} of each component, e.g. {@link Float}</li>
 * <li>the number of {@link #bytes} per component, e.g. {@link Float#BYTES}</li>
 * <li>whether the data is {@link #signed}</li>
 * </ul>
 * <p>
 * The {@link #bytes(Class)} method can be used to determine the number of bytes per component for common numeric types such as {@link Float}.
 * <p>
 * Example: The following are synonymous for a floating-point 3-tuple:
 * <pre>
 *     new Layout(3, Float.class, Float.BYTES, true);
 *     new Layout(3, Float.class, true);
 *     Layout.of(3);
 * </pre>
 * <p>
 * Note that the {@link #equals(Object)} compares by <i>identity</i> in order to prevent components with the same structure being considered equal, i.e. points and vectors.
 * <p>
 * @author Sarge
 */
public record Layout(int size, Class<?> type, int bytes, boolean signed) {
	/**
	 * Creates a layout with a number of components of the given numeric type.
	 * @param size			Size of this layout (number of components)
	 * @param type			Component type
	 * @param signed		Whether components are signed or unsigned
	 * @throws IllegalArgumentException if the number of components is larger than {@link #MAPPING}
	 * @throws IllegalArgumentException for an unsupported type
	 * @see #bytes(Class)
	 */
	public static Layout of(int size, Class<?> type, boolean signed) {
		final int bytes = bytes(type);
		return new Layout(size, type, bytes, signed);
	}

	/**
	 * Creates a layout with {@link #size} signed floating-point components.
	 * @param size Size of this layout (number of components)
	 * @return New floating-point layout
	 */
	public static Layout of(int size) {
		return of(size, Float.class, true);
	}

	/**
	 * Determines the number of bytes for the given numeric type.
	 * <p>
	 * The {@link #type} parameter can be either a wrapper or primitive type.
	 * <p>
	 * For example: {@link Float} or {@code float} is mapped to {@link Float#BYTES}.
	 * <p>
	 * The following types are supported:
	 * <ul>
	 * <li>float</li>
	 * <li>integer</li>
	 * <li>short</li>
	 * <li>byte</li>
	 * </ul>
	 * <p>
	 * @param type Type
	 * @return Number of bytes
	 * @throws IllegalArgumentException for an unsupported type
	 */
	public static int bytes(Class<?> type) {
		// TODO - JDK17 switch
		return switch(type.getSimpleName().toLowerCase()) {
			case "float" -> Float.BYTES;
			case "int", "integer" -> Integer.BYTES;
			case "short" -> Short.BYTES;
			case "byte" -> Byte.BYTES;
			default -> throw new IllegalArgumentException("Unsupported layout component type: " + type);
		};
	}

	/**
	 * Calculates the total <i>stride</i> of the given layouts.
	 * @param layouts Layouts
	 * @return Stride
	 */
	public static int stride(Collection<Layout> layouts) {
		return layouts.stream().mapToInt(Layout::length).sum();
	}

	/**
	 * Constructor.
	 * @param size			Number of components
	 * @param type			Component type
	 * @param bytes			Number of bytes per component
	 * @param signed		Whether components are signed or unsigned
	 */
	public Layout {
		Check.oneOrMore(size);
		Check.notNull(type);
		Check.oneOrMore(bytes);
	}

	/**
	 * @return Length of this layout (bytes)
	 */
	public int length() {
		return size * bytes;
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}
}
