package org.sarge.jove.common;

import org.sarge.lib.util.Check;

/**
 * A <i>component</i> is a compound bufferable type with a defined structure used to model inter-leaved vertex data.
 * @author Sarge
 */
public interface Component extends Bufferable {
	/**
	 * A <i>component layout</i> is a descriptor for the structure of a component.
	 */
	record Layout(int size, int bytes, Class<?> type) {
		/**
		 * Convenience layout for a 3-component floating-point tuple.
		 */
		public static final Layout TUPLE = Layout.of(3);

		/**
		 * Creates a component layout of the given type.
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
