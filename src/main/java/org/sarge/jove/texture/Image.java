package org.sarge.jove.texture;

import static org.sarge.lib.util.Check.notNull;

import java.awt.Graphics2D;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.BufferFactory;

/**
 * Platform-independant image.
 * @author Sarge
 * TODO - factor out AWT and/or use library
 */
public class Image {
	/**
	 * Image formats.
	 */
	public enum Format {
		RGB,
		RGBA,
		GRAY_SCALE;

		/**
		 * @return Whether this format is translucent (has an alpha channel)
		 */
		public boolean isTranslucent() {
			return this == RGBA;
		}
	}

	/**
	 * Determines the number of mipmap levels for the given image dimensions.
	 * @param dim Image dimensions
	 * @return Number of mipmap levels
	 */
	public static int levels(Dimensions dim) {
		final float max = Math.max(dim.width(), dim.height());
		return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
	}

	private final Format format;
	private final Dimensions size;
	private final BufferedImage image;

	/**
	 * Constructor.
	 * @param image Image
	 */
	public Image(Format format, Dimensions size, BufferedImage image) {
		this.format = notNull(format);
		this.size = notNull(size);
		this.image = notNull(image);
	}

	/**
	 * Maps the given colour model to the image format.
	 * @param model Colour model
	 * @return Image format
	 */
	private static Format map(ColorModel model) {
		final int size = model.getColorSpace().getNumComponents();
		if(size == 1) {
			return Format.GRAY_SCALE;
		}
		else
		if(size == 3) {
			if(model.hasAlpha()) {
				return Format.RGBA;
			}
			else {
				return Format.RGB;
			}
		}
		else {
			throw new IllegalArgumentException("Unsupported colour model: " + model);
		}
	}

	/**
	 * @return Image format
	 */
	public Format format() {
		return format;
	}

	/**
	 * @return Image dimensions
	 */
	public Dimensions size() {
		return size;
	}

	/**
	 * Looks up a pixel from this image.
	 * @param x
	 * @param y
	 * @return Packed pixel colour
	 */
	public int pixel(int x, int y) {
		return image.getRGB(x, y);
	}

	/**
	 * @return Image data buffer
	 */
	public ByteBuffer buffer() {
		final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] bytes = data.getData();
		final ByteBuffer buffer = BufferFactory.byteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;
	}

	private static final ColorModel TRANSLUCENT = createColourModel(true);
//	private static final ColorModel OPAQUE = createColourModel(false);

	/**
	 * Creates OpenGL format colour models.
	 */
	private static ColorModel createColourModel(boolean alpha) {
		return new ComponentColorModel(
			ColorSpace.getInstance(ColorSpace.CS_sRGB),
			new int[]{8, 8, 8, alpha ? 8 : 0},
			alpha,
			false,
			alpha ? ComponentColorModel.TRANSLUCENT : ComponentColorModel.OPAQUE,
			DataBuffer.TYPE_BYTE);
	}

	/**
	 * Loader for a default image.
	 * @see ImageIO
	 * TODO - this should be a platform-specific service
	 */
	public static class Loader {
		/**
		 * Loads an image.
		 * @param in Input stream
		 * @return Image
		 * @throws IOException if the image cannot be loaded
		 */
		public Image load(InputStream in) throws IOException {
			// Load image
			final BufferedImage image = ImageIO.read(in);

//			// Select colour model
//			final boolean alpha = image.getColorModel().hasAlpha();
//			final ColorModel model = alpha ? TRANSLUCENT : OPAQUE;

			// TODO - only need to do this is image does not already have alpha?

			// Create ARGB image
			final Dimensions dim = new Dimensions(image.getWidth(), image.getHeight());
			final WritableRaster raster = Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE, dim.width, dim.height, 4, null);
			final BufferedImage texture = new BufferedImage(TRANSLUCENT, raster, false, new Hashtable<>());

			// Draw image
			final Graphics2D g = texture.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();

			// Create Vulkan image
			final Format format = map(TRANSLUCENT);
			return new Image(format, dim, texture);
		}
	}
}
