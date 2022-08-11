package org.sarge.jove.common;

import java.util.Collection;

import org.sarge.lib.util.Check;

/**
 * A <i>layout</i> is a descriptor for the structure of compound data such as an image or vertex.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>{@link #size} number of components, e.g. 3 for a vertex normal</li>
 * <li>the {@link #type} of each component, e.g. {@link Layout.Type#FLOAT}</li>
 * <li>the number of {@link #bytes} per component, e.g. {@link Float#BYTES}</li>
 * <li>whether the data is {@link #signed}</li>
 * </ul>
 * <p>
 * Example for the layout of a floating-point 3-tuple such as a vector: <pre>new Layout(3, Type.FLOAT, Float.BYTES, true)</pre>
 * <p>
 * The {@link #toString()} representation of a layout is a compacted string with a {@code U} suffix for unsigned types, for example the above layout is represented as {@code 3-FLOAT4}.
 * <p>
 * @author Sarge
 */
public record Layout(int size, Layout.Type type, int bytes, boolean signed) {
	/**
	 * Component types.
	 */
	public enum Type {
		INTEGER,
		FLOAT,
		NORMALIZED
	}

	/**
	 * Creates a signed {@link Type#FLOAT} layout with {@link #size} components.
	 * @param size Number of components
	 * @return New floating-point layout
	 */
	public static Layout floats(int size) {
		return new Layout(size, Type.FLOAT, Float.BYTES, true);
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
	 * Helper.
	 * @return Length of a component with this layout (bytes)
	 */
	public int length() {
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
