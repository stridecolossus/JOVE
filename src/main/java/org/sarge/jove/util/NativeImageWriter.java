package org.sarge.jove.util;

import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;

import javax.imageio.ImageIO;

import org.sarge.jove.common.Dimensions;

/**
 * TODO
 * @author Sarge
 */
public class NativeImageWriter {
	/**
	 * Creates a Java image from the given image data.
	 * @param bytes		Image data
	 * @param size		Dimensions
	 * @return Image
	 */
	public static BufferedImage image(byte[] bytes, Dimensions size) {
		// Create image data
		final var data = new DataBufferByte(bytes, bytes.length);

		// Create raster
		final var raster = Raster.createInterleavedRaster(
				data,
				size.width(), size.height(),
				size.width() * 4,					// Line stride
				4,									// Pixel stride
				new int[]{2, 1, 0},					// Colour band offsets (BGR -> RGB)
				null								// Top-left location
		);

		// Init colour space model
		final var model = new ComponentColorModel(
				ColorSpace.getInstance(ColorSpace.CS_sRGB),
				new int[]{8, 8, 8},			// Bits per colour channel
				false, false,				// No alpha channel
				Transparency.OPAQUE,
				DataBuffer.TYPE_BYTE
		);

		// Create image
		return new BufferedImage(model, raster, false, null);
	}

	/**
	 * Writes the given image to disk.
	 * @param image			Image to write
	 * @param format		Image format
	 * @param file			Output file
	 * @throws RuntimeException if the image cannot be written
	 */
	public static void write(BufferedImage image, String format, File file) {
		try {
			ImageIO.write(image, format, file);
		}
		catch(IOException e) {
			throw new RuntimeException("Error writing image: " + file, e);
		}
	}
}
