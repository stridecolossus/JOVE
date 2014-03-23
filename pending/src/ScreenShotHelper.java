package org.sarge.jove.scene;

import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.nio.ByteBuffer;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.sarge.jove.util.BufferUtils;

/**
 * 
 * TODO
 * - move all image stuff to new image helper/loader
 * - ditto FLOAT_SIZE
 * - factor out read-pixels -> rendering system, rest if generic (?)
 * - over-ridden helpers to return as ByteBuffer, BufferedImage, save to location (data-source?)
 * 
 * @author Sarge
 */
public class ScreenShotHelper {
	private static final ColorModel OPAQUE = new ComponentColorModel(
		ColorSpace.getInstance( ColorSpace.CS_sRGB ),
		new int[]{ 8, 8, 8 },
		false,
		false,
		Transparency.OPAQUE,
		DataBuffer.TYPE_BYTE
	);

	private static final int FLOAT_SIZE = 3;
	
	private static final int[] OFFSETS = new int[]{ 0, 1, 2 };

//	public ByteBuffer capture( Rectangle rect ) {
//		// Read back buffer
//		final ByteBuffer buffer = BufferUtils.createByteBuffer( rect.width * rect.height * FLOAT_SIZE );
//		GL11.glReadBuffer( GL11.GL_BACK );
//		GL11.glReadPixels( rect.x, rect.y, rect.width, rect.height, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, buffer );
//	}

	/**
	 * Captures the specified portion of the viewport.
	 */
	public BufferedImage capture( Rectangle rect ) {
		// Read back buffer
		final ByteBuffer buffer = BufferUtils.createByteBuffer( rect.width * rect.height * FLOAT_SIZE );
		GL11.glReadBuffer( GL11.GL_BACK );
		GL11.glReadPixels( rect.x, rect.y, rect.width, rect.height, GL12.GL_BGR, GL11.GL_UNSIGNED_BYTE, buffer );

		// Convert to image buffer
		final DataBuffer data = new DataBufferByte( buffer.array(), buffer.limit() );

		// Create image raster
		final WritableRaster raster = Raster.createInterleavedRaster(
			data,
			rect.width,
			rect.height,
			rect.width * FLOAT_SIZE,		// Scan-line stride
			FLOAT_SIZE,						// Pixel stride
			OFFSETS,
			null							// Origin
		);
		
		// Create image
		return new BufferedImage( OPAQUE, raster, false, null );
	}
}
