package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * A <i>component</i> is a bufferable type comprised of a number of Java primitives.
 * @author Sarge
 */
public interface Component extends Bufferable {
	/**
	 * A <i>component layout</i> is a descriptor for the structure of this component.
	 */
	record Layout(int size, int bytes, Class<?> type) {
		/**
		 * Creates a component layout of the given type.
		 * @param size			Size of this component
		 * @param type			Component type
		 * @return New layout
		 */
		public static Layout of(int size, Class<?> type) {
			return switch(type.getSimpleName().toLowerCase()) {
				case "float" 			-> new Layout(size, Float.BYTES, Float.class);
				case "int", "integer" 	-> new Layout(size, Integer.BYTES, Integer.class);
				case "short" 			-> new Layout(size, Short.BYTES, Short.class);
				default -> throw new IllegalArgumentException("Unsupported component type: " + type.getSimpleName());
			};
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
		 * @return Length of this component (bytes)
		 */
		public int length() {
			return size * bytes;
		}
	}

	/**
	 * @return Layout of this component
	 */
	Layout layout();

	@Override
	default int length() {
		return this.layout().length();
	}
}
