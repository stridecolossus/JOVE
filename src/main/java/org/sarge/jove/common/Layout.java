package org.sarge.jove.common;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.sarge.lib.util.Check;

/**
 * A <i>layout</i> is a descriptor for the structure of compound data such as an image or vertex.
 * <p>
 * A layout is comprised of:
 * <ul>
 * <li>the {@link #size} which specifies the number of <i>components</i> that comprise the data, e.g. 3 for a point or vector</li>
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
 */
public record Layout(int size, Class<?> type, int bytes, boolean signed) {
	/**
	 * Creates a layout with {@link #size} signed floating-point components.
	 * @param size Size of this layout (number of components)
	 * @return New floating-point layout
	 */
	public static Layout of(int size) {
		return new Layout(size, Float.class, true);
	}

	/**
	 * Determines the number of bytes for the given numeric type.
	 * <p>
	 * The {@link #type} parameter can be either the wrapper or primitive type.
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
		return switch(type.getSimpleName().toLowerCase()) {
			case "float" -> Float.BYTES;
			case "int", "integer" -> Integer.BYTES;
			case "short" -> Short.BYTES;
			case "byte" -> Byte.BYTES;
			default -> throw new IllegalArgumentException("Unsupported layout component type: " + type);
		};
	}

	/**
	 * Constructor.
	 * @param size			Size of this layout (number of components)
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
	 * Constructor for a layout comprised of a numeric type.
	 * @param size			Size of this layout (number of components)
	 * @param type			Component type
	 * @param signed		Whether components are signed or unsigned
	 * @throws IllegalArgumentException for an unsupported type
	 * @see #bytes(Class)
	 */
	public Layout(int size, Class<?> type, boolean signed) {
		this(size, type, bytes(type), signed);
	}

	/**
	 * @return Length of this layout (bytes)
	 */
	public int length() {
		return size * bytes;
	}

	/**
	 * A <i>compound layout</i> is a convenience wrapper for a list of layouts.
	 * <p>
	 * Note that the component layouts are compared by <i>identity</i> rather than equality in the {@link #contains(Layout)} and {@link #equals(List)} methods.
	 * This prevents objects with the same layout structure being accidentally considered equal, e.g. points and vectors.
	 * <p>
	 * Example:
	 * <p>
	 * <pre>
	 * 	CompoundLayout one = new CompoundLayout(List.of(Point.LAYOUT);
	 * 	CompoundLayout two = new CompoundLayout(List.of(Vector.NORMALS);
	 * 	Point.LAYOUT.equals(Vector.NORMALS); // true
	 * 	one.equals(two); // false
	 * </pre>
	 */
	public static class CompoundLayout implements Iterable<Layout> {
		/**
		 * Creates a compound layout.
		 * @param layouts Layouts
		 * @return New compound layout
		 */
		public static CompoundLayout of(Layout... layouts) {
			final CompoundLayout compound = new CompoundLayout();
			for(Layout e : layouts) {
				compound.add(e);
			}
			return compound;
		}

		private final List<Layout> layouts = new ArrayList<>();

		protected CompoundLayout() {
		}

		/**
		 * Adds a layout to this list.
		 * @param layout Layout to add
		 * @throws IllegalArgumentException for a duplicate layout
		 */
		protected void add(Layout layout) {
			if(contains(layout)) throw new IllegalArgumentException("Duplicate layout entry: " + layout);
			layouts.add(notNull(layout));
		}

		/**
		 * @return Number of components in this layout
		 */
		public int size() {
			return layouts.size();
		}

		/**
		 * Calculates the total <i>stride</i> of this compound layout, i.e. the sum of {@link Layout#length()}
		 * @return Total stride
		 */
		public int stride() {
			return layouts.stream().mapToInt(Layout::length).sum();
		}

		/**
		 * @param layout Layout
		 * @return Whether this list contains the given layout
		 */
		public boolean contains(Layout layout) {
			return layouts.stream().anyMatch(e -> e == layout);
		}

		@Override
		public Iterator<Layout> iterator() {
			return layouts.iterator();
		}

		@Override
		public int hashCode() {
			return layouts.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return (obj == this) || (obj instanceof CompoundLayout that) && equals(that.layouts);
		}

		/**
		 * Compares layouts by identity.
		 */
		private boolean equals(List<Layout> layouts) {
			// Check same length
			if(this.layouts.size() != layouts.size()) {
				return false;
			}

			// Compare layout by identity
			final Iterator<Layout> left = this.iterator();
			final Iterator<Layout> right = layouts.iterator();
			while(left.hasNext()) {
				if(left.next() != right.next()) {
					return false;
				}
			}

			// Matching layouts
			assert !right.hasNext();
			return true;
		}

		@Override
		public String toString() {
			return layouts.toString();
		}
	}

	/**
	 * Mutable implementation.
	 */
	public static class MutableCompoundLayout extends CompoundLayout {
		@Override
		public void add(Layout layout) {
			super.add(layout);
		}
	}
}
