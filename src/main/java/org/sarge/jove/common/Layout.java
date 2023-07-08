package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * A <i>layout</i> describes the structure and format of common data tuples such as image pixels or vertex components.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>The {@link #size} number of data elements, e.g. 3 for a vertex normal</li>
 * <li>The {@link #type} of the data, e.g. {@link ByteSized.Type#FLOAT}</li>
 * <li>Whether the data is {@link #signed}</li>
 * <li>The number of {@link #bytes} per element, e.g. {@link Float#BYTES}</li>
 * </ul>
 * <p>
 * Example layout for a floating-point 3-tuple normal: <pre>new Component(3, Type.FLOAT, true, Float.BYTES)</pre>
 * <p>
 * The {@link #toString()} representation of a layout is a compacted string with a {@code U} suffix for unsigned types.
 * For example the above layout is represented as {@code 3-FLOAT4}.
 * <p>
 */
public record Layout(int count, Layout.Type type, boolean signed, int bytes) {
	/**
	 * A <i>component</i> defines a data type with a layout.
	 */
	public interface Component {
		/**
		 * @return Layout of this component
		 */
		Layout layout();
	}

	/**
	 * Component types.
	 */
	public enum Type {
		INTEGER,
		FLOAT,
		NORMALIZED
	}

	/**
	 * Creates a signed {@link Type#FLOAT} layout with {@link #size} elements.
	 * @param size Number of elements
	 * @return New floating-point layout
	 */
	public static Layout floats(int size) {
		return new Layout(size, Type.FLOAT, true, Float.BYTES);
	}

	/**
	 * Constructor.
	 * @param count			Number of elements
	 * @param type			Data type
	 * @param signed		Whether elements are signed or unsigned
	 * @param bytes			Number of bytes per element
	 */
	public Layout {
		Check.oneOrMore(count);
		Check.notNull(type);
		Check.oneOrMore(bytes);
	}

	/**
	 * @return Stride of this layout (bytes)
	 */
	public int stride() {
		return count * bytes;
	}

	@Override
	public String toString() {
		final StringBuilder str = new StringBuilder();
		str.append(count);
		str.append('-');
		str.append(type);
		str.append(bytes);
		if(!signed) {
			str.append("U");
		}
		return str.toString();
	}
}
