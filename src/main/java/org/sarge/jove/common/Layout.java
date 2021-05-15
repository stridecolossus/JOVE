package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * A <i>layout</i> is a descriptor for the structure of a composite object such as an image or vertex.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>the {@link #type} of each element, e.g. {@link Float}</li>
 * <li>the {@link #size} which specifies the number of <i>elements</i> that comprise the component, e.g. 3 for a 3D point</li>
 * <li>the number of {@link #bytes} per element, e.g. {@link Float#BYTES}</li>
 * </ul>
 * Examples:
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
 * @see VertexComponent
 * @author Sarge
 */
public record Layout(int size, int bytes, Class<?> type) {
	/**
	 * Creates a layout for the given type.
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The {@link #type} parameter can be a wrapper or primitive type</li>
	 * <li>The number of {@link #bytes} is specified by the corresponding constant, e.g. {@link Float#BYTES}</li>
	 * <li>The type returned by {@link #type()} is the wrapper class, e.g. {@link Float}</li>
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
	 * @param size			Size of this component
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
	 * Helper - Creates a layout with floating-point components.
	 * @param size Size of this component
	 * @return New floating-point layout
	 */
	public static Layout of(int size) {
		return new Layout(size, Float.BYTES, Float.class);
	}

	/**
	 * Constructor.
	 * @param size			Size of this component (number of elements)
	 * @param bytes			Number of bytes per element
	 * @param type			Component type
	 */
	public Layout {
		Check.oneOrMore(size);
		Check.oneOrMore(bytes);
		Check.notNull(type);
	}

	/**
	 * @return Length of an element of this layout (bytes)
	 */
	public int length() {
		return size * bytes;
	}
}
