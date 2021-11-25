package org.sarge.jove.io;

import static org.sarge.lib.util.Check.notEmpty;
import static org.sarge.lib.util.Check.notNull;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;

/**
 * An <i>image data</i> is an abstraction for a general image comprising multiple array layers and MIP levels.
 * <p>
 * The image {@link #components} specifies the order of the channels comprising the image, e.g. {@code ABGR} for a transparent native image.
 * <p>
 * The {@link #layout} describes the number of channels comprising the image and the structure of each pixel.
 * For example a transparent image with one byte per channel would have the following layout: <code>new Layout(4, Byte.class, 1, false)</code>
 * <p>
 * @author Sarge
 */
public interface ImageData {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Components
	 */
	String components();

	/**
	 * @return Component layout
	 */
	Layout layout();

	/**
	 * @return Number of layers
	 */
	int layers();

	/**
	 * @return Number of MIP levels
	 */
	int levels();

	/**
	 * Retrieves the image data for the given layer and MIP level.
	 * @param layer		Layer
	 * @param level		MIP level
	 * @return Image data
	 * @throws IndexOutOfBoundsException for an invalid layer or level index
	 */
	Bufferable data(int layer, int level);

	/**
	 * Skeleton implementation.
	 */
	abstract class AbstractImageData implements ImageData {
		private final Dimensions size;
		private final String components;
		private final Layout layout;

		/**
		 * Constructor.
		 * @param size				Image dimensions
		 * @param components		Components
		 * @param layout			Layout
		 * @throws IllegalArgumentException if the size of the layout does not match the number of components
		 */
		protected AbstractImageData(Dimensions size, String components, Layout layout) {
			this.size = notNull(size);
			this.components = notEmpty(components);
			this.layout = notNull(layout);
			validate();
		}

		private void validate() {
			if(components.length() != layout.size()) {
				throw new IllegalArgumentException(String.format("Mismatched image components and layout: components=%s layout=%s", components, layout));
			}
		}

		@Override
		public Dimensions size() {
			return size;
		}

		@Override
		public String components() {
			return components;
		}

		@Override
		public Layout layout() {
			return layout;
		}

		@Override
		public int layers() {
			return 1;
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this)
					.append(size)
					.append(components)
					.append(layout)
					.append("levels", levels())
					.append("layers", layers())
					.build();
		}
	}
}
