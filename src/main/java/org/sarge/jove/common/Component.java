package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * A <i>component</i> describes the structure and format of common data tuples such as image pixels or vertex components.
 * <p>
 * A component is comprised of:
 * <ul>
 * <li>The {@link #size} number of elements in the component, e.g. 3 for a vertex normal</li>
 * <li>The {@link #type} of the component, e.g. {@link ByteSized.Type#FLOAT}</li>
 * <li>Whether the data is {@link #signed}</li>
 * <li>The number of {@link #bytes} per element, e.g. {@link Float#BYTES}</li>
 * </ul>
 * <p>
 * Example component layout for a floating-point 3-tuple normal: <pre>new Component(3, Type.FLOAT, true, Float.BYTES)</pre>
 * <p>
 * The {@link #toString()} representation of a component is a compacted string with a {@code U} suffix for unsigned types.
 * For example the above layout is represented as {@code 3-FLOAT4}.
 * <p>
 */
public record Component(int size, Component.Type type, boolean signed, int bytes) implements ByteSized {
	/**
	 * Component types.
	 */
	public enum Type {
		INTEGER,
		FLOAT,
		NORMALIZED
	}

	/**
	 * Creates a signed {@link Type#FLOAT} component layout with {@link #size} elements.
	 * @param size Number of elements
	 * @return New floating-point component layout
	 */
	public static Component floats(int size) {
		return new Component(size, Type.FLOAT, true, Float.BYTES);
	}

	/**
	 * Creates a component layout for the given Java type.
	 * <p>
	 * The following types are supported:
	 * <ul>
	 * <li>floating-point</li>
	 * <li>integral numbers: long, integer, short, byte</li>
	 * <li>boolean (represented as an unsigned byte)</li>
	 * </ul>
	 * Both primitive and wrappers types are valid.
	 * <p>
	 * @param type Type
	 * @return Component layout
	 * @throws IllegalArgumentException for an unsupported type
	 */
	public static Component of(Class<?> type) {
		return switch(type.getSimpleName().toLowerCase()) {
			case "float" 			-> new Component(1, Type.FLOAT, true, Float.BYTES);
			case "integer", "int"	-> new Component(1, Type.INTEGER, true, Integer.BYTES);
			case "long" 			-> new Component(1, Type.INTEGER, true, Long.BYTES);
			case "short" 			-> new Component(1, Type.INTEGER, true, Short.BYTES);
			case "byte"	 			-> new Component(1, Type.INTEGER, true, 1);
			case "boolean" 			-> new Component(1, Type.INTEGER, false, 1);
			default -> throw new IllegalArgumentException("Unsupported component type: " + type);
		};
	}

	/**
	 * Constructor.
	 * @param size			Number of elements
	 * @param type			Component type
	 * @param signed		Whether components are signed or unsigned
	 * @param bytes			Number of bytes per element
	 */
	public Component {
		Check.oneOrMore(size);
		Check.notNull(type);
		Check.oneOrMore(bytes);
	}

	@Override
	public int stride() {
		return size * bytes;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append(size);
		str.append('-');
		str.append(type);
		str.append(bytes);
		if(!signed) {
			str.append("U");
		}
		return str.toString();
	}
}
