package org.sarge.jove.common;

import java.util.*;

import org.sarge.lib.util.Check;

/**
 * A <i>layout</i> is a descriptor for the structure of of data tuples such as image pixels or vertex components.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>{@link #size} number of components, e.g. 3 for a vertex normal</li>
 * <li>the {@link #type} of each component, e.g. {@link Layout.Type#FLOAT}</li>
 * <li>whether the data is {@link #signed}</li>
 * <li>the number of {@link #bytes} per component, e.g. {@link Float#BYTES}</li>
 * </ul>
 * <p>
 * Example for the layout of a floating-point 3-tuple normal: <pre>new Layout(3, Type.FLOAT, true, Float.BYTES)</pre>
 * <p>
 * The {@link #toString()} representation of a layout is a compacted string with a {@code U} suffix for unsigned types, for example the above layout is represented as {@code 3-FLOAT4}.
 * <p>
 * @author Sarge
 */
public record Layout(int size, Layout.Type type, boolean signed, int bytes) {
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
		return new Layout(size, Type.FLOAT, true, Float.BYTES);
	}

	/**
	 * Constructor.
	 * @param size			Number of components
	 * @param type			Component type
	 * @param signed		Whether components are signed or unsigned
	 * @param bytes			Number of bytes per component
	 */
	public Layout {
		Check.oneOrMore(size);
		Check.notNull(type);
		Check.oneOrMore(bytes);
	}

	/**
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

	/**
	 * Convenience wrapper for the layouts of a compound object such as a vertex.
	 */
	public record CompoundLayout(List<Layout> layouts) {
		/**
		 * Creates a new compound layout.
		 * @param layouts Layouts
		 * @return New compound layout
		 */
		public static CompoundLayout of(Layout... layouts) {
			return new CompoundLayout(Arrays.asList(layouts));
		}

		/**
		 * Constructor.
		 * @param layouts Layouts
		 */
		public CompoundLayout {
			layouts = List.copyOf(layouts);
		}

		/**
		 * Calculates the total <i>stride</i> of this compound layout.
		 * @return Layout stride
		 */
		public int stride() {
			return layouts.stream().mapToInt(Layout::length).sum();
		}
	}
}
