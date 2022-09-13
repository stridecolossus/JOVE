package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * A <i>component</i> describes the structure and format of common data tuples such as image pixels or vertex components.
 * <p>
 * A component is comprised of:
 * <ul>
 * <li>The {@link #size} number of elements in the component, e.g. 3 for a vertex normal</li>
 * <li>The {@link #type} of the component, e.g. {@link Stride.Type#FLOAT}</li>
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
public record Component(int size, Component.Type type, boolean signed, int bytes) implements Stride {
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
