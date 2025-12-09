package org.sarge.jove.common;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.util.Validation.requireOneOrMore;

import java.util.List;

/**
 * A <i>layout</i> describes the structure and format of common data tuples such as image pixels or vertex components.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>The {@link #size} number of data elements, e.g. 3 for a vertex normal</li>
 * <li>The {@link #type} of the data, e.g. {@link Type#FLOAT}</li>
 * <li>Whether the data is {@link #signed}</li>
 * <li>The number of {@link #bytes} per element, e.g. {@link Float#BYTES}</li>
 * </ul>
 * <p>
 * For example the layout for a vertex position could be <pre>Layout(3, Type.FLOAT, true, Float.BYTES)</pre>
 * <p>
 * @author Sarge
 */
public record Layout(int count, Type type, boolean signed, int bytes) {
	/**
	 * Component types.
	 */
	public enum Type {
		INTEGER,
		FLOAT,
		NORMALIZED
	}

	/**
	 * Creates a signed {@link Type#FLOAT} layout with {@link #count} elements.
	 * @param count Number of elements
	 * @return New floating-point layout
	 */
	public static Layout floats(int count) {
		return new Layout(count, Type.FLOAT, true, Float.BYTES);
	}

	/**
	 * Constructor.
	 * @param count			Number of elements
	 * @param type			Data type
	 * @param signed		Whether elements are signed or unsigned
	 * @param bytes			Number of bytes per element
	 */
	public Layout {
		requireOneOrMore(count);
		requireNonNull(type);
		requireOneOrMore(bytes);
	}

	/**
	 * @return Stride of this layout (bytes)
	 */
	public int stride() {
		return count * bytes;
	}

	/**
	 * @param layouts Layouts
	 * @return Total stride of the given layouts (bytes)
	 */
	public static int stride(List<Layout> layouts) {
		return layouts
				.stream()
				.mapToInt(Layout::stride)
				.sum();
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
