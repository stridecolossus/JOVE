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

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.util.BufferFactory;

/**
 * Default image implemented using AWT.
 * @author Sarge
 * TODO - Windows/AWT only, no point in base interface
 */
public class DefaultImage implements Image {
	private final Header header;
	private final BufferedImage image;

	/**
	 * Constructor.
	 * @param image Image
	 */
	public DefaultImage(Header header, BufferedImage image) {
//		final Dimensions size = new Dimensions(image.getWidth(), image.getHeight());
//		final Format format = map(image.getColorModel());
		this.header = notNull(header); // new Header(format, size);
		this.image = notNull(image);
	}

//	/**
//	 * Constructor.
//	 * @param header	Image header
//	 * @param image		Image
//	 */
//	private DefaultImage(Header header, BufferedImage image) {
//		this.header = notNull(header);
//		this.image = notNull(image);
//	}

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

	@Override
	public Header header() {
		return header;
	}

	@Override
	public int pixel(int x, int y) {
		return image.getRGB(x, y);
	}

	@Override
	public ByteBuffer buffer() {
		final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
		final byte[] bytes = data.getData();
		final ByteBuffer buffer = BufferFactory.byteBuffer(bytes.length);
		buffer.put(bytes);
		buffer.flip();
		return buffer;

//		// TODO - helper?
//		// TODO - do all image types automatically do this or do we need to convert first?
//		final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
//
//		// Convert ABGR to RGBA
//		// TODO - this sucks? is probably hopelessly inefficient? only works for ABGR images?
//		final byte[] bytes = data.getData();
//		final byte[] rgba = new byte[bytes.length];
//		for(int n = 0; n < rgba.length; n += 4) {
//			rgba[n] = bytes[n + 3];
//			rgba[n + 1] = bytes[n + 2];
//			rgba[n + 2] = bytes[n + 1];
//			rgba[n + 3] = bytes[n];
//		}
//
//		// Buffer
//		final ByteBuffer buffer = BufferFactory.byteBuffer(rgba.length);
//		buffer.put(rgba);
//		buffer.flip();
//		return buffer;
	}

	@Override
	public Image convert(Header header) {
		throw new UnsupportedOperationException();
//		final Dimensions size = header.size();
//		return convert(header, size.width(), size.height());
	}

//	public Image convert(Header descriptor, int w, int h) {
//		// Create target image
//		final int type = map(descriptor.format());
//		final Dimensions size = descriptor.size();
//		final BufferedImage result = new BufferedImage(size.width(), size.height(), type);
//
//		// Draw image to target
//		final Graphics g = result.getGraphics();
//		g.drawImage(image, 0, 0, size.width(), size.height(), 0, 0, w, h, null);
//		g.dispose();
//
//		// Create new image wrapper
//		return new DefaultImage(descriptor, result);
//	}
//
//	private static int map(Format format) {
//		switch(format) {
//		case RGB:			return BufferedImage.TYPE_3BYTE_BGR;
//		case RGBA:			return BufferedImage.TYPE_4BYTE_ABGR;
//		case GRAY_SCALE:	return BufferedImage.TYPE_BYTE_GRAY;
//		default:			throw new RuntimeException();
//		}
//	}
//
//	BufferedImage createFloatBufferedImage(int w, int h, int bands) {
//	    // Define dimensions and layout of the image
//	    //int bands = 4; // 4 bands for ARGB, 3 for RGB etc
//	    int[] bandOffsets = {0, 1, 2, 3}; // length == bands, 0 == R, 1 == G, 2 == B and 3 == A
//
//	    // Create a TYPE_FLOAT sample model (specifying how the pixels are stored)
//	    SampleModel sampleModel = new PixelInterleavedSampleModel(DataBuffer.TYPE_FLOAT, w, h, bands, w  * bands, bandOffsets);
//	    // ...and data buffer (where the pixels are stored)
//	    DataBuffer buffer = new DataBufferFloat(w * h * bands);
//
//	    // Wrap it in a writable raster
//	    WritableRaster raster = Raster.createWritableRaster(sampleModel, buffer, null);
//
//	    // Create a color model compatible with this sample model/raster (TYPE_FLOAT)
//	    // Note that the number of bands must equal the number of color components in the
//	    // color space (3 for RGB) + 1 extra band if the color model contains alpha
//	    ColorSpace colorSpace = ColorSpace.getInstance(ColorSpace.CS_sRGB);
//	    ColorModel colorModel = new ComponentColorModel(colorSpace, true, false, Transparency.TRANSLUCENT, DataBuffer.TYPE_FLOAT);
//
//	    // And finally create an image with this raster
//	    return new BufferedImage(colorModel, raster, colorModel.isAlphaPremultiplied(), null);
//	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}

	private static final ColorModel TRANSLUCENT = createColourModel(true);
	private static final ColorModel OPAQUE = createColourModel(false);

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
		private boolean flip; // = true;

		/**
		 * Sets whether to flip images in the Y direction.
		 * @param flip Whether to flip
		 */
		public Loader flip(boolean flip) {
			this.flip = flip;
			return this;
		}

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
			if(flip) {
				g.drawImage(image, 0, 0, dim.width, dim.height, 0, dim.height, dim.width, 0, null);
			}
			else {
				g.drawImage(image, 0, 0, null);
			}
			g.dispose();

			// Create Vulkan image
			final Format format = map(TRANSLUCENT);
			final Header header = new Header(format, dim);
			return new DefaultImage(header, texture);
		}
	}
}
