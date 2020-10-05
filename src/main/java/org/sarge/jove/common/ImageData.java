package org.sarge.jove.common;

import static org.sarge.jove.util.Check.notNull;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

import javax.imageio.ImageIO;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.util.DataSource;

/**
 * Wrapper for image data.
 * @author Sarge
 */
public interface ImageData {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Image data
	 */
	ByteBuffer buffer();

	/**
	 * Default implementation.
	 * Note that the underlying array is mutable.
	 */
	class DefaultImageData implements ImageData {
		private final Dimensions size;
		private final byte[] bytes;

		/**
		 * Constructor.
		 * @param size		Dimensions
		 * @param bytes		Image data
		 * @throws IllegalArgumentException if the size of the array does not match the dimensions
		 */
		DefaultImageData(Dimensions size, byte[] bytes) {
			if(size.width() * size.height() * 4 != bytes.length) throw new IllegalArgumentException("Array length does not match image dimensions");
			this.size = notNull(size);
			this.bytes = bytes;
		}

		@Override
		public Dimensions size() {
			return size;
		}

		@Override
		public ByteBuffer buffer() {
			return ByteBuffer.wrap(bytes);
		}

		@Override
		public String toString() {
			return new ToStringBuilder(this).append("size", size).build();
		}
	}

	/**
	 * Loader for images.
	 */
	public static class Loader {
		private final DataSource src;

		/**
		 * Constructor.
		 * @param src Data source
		 */
		public Loader(DataSource src) {
			this.src = notNull(src);
		}

//		/**
//		 * Determines the number of mipmap levels for the given image dimensions.
//		 * @param dim Image dimensions
//		 * @return Number of mipmap levels
//		 */
//		public static int levels(Dimensions dim) {
//			final float max = Math.max(dim.width(), dim.height());
//			return 1 + (int) Math.floor(Math.log(max) / Math.log(2));
//		}

/*
		// https://stackoverflow.com/questions/24639986/how-do-colormodels-and-writablerasters-work-in-java-bufferedimages

		interface Transform {
			//void apply(byte[] src, byte[] dest);
//			int map(int index);
			int transform(int index);
		}

		interface Mapper {
			byte map(int index, byte[] bytes);

			//Mapper DEFAULT
		}

		Transform abgr = index -> switch(index) {
			case 0 -> 3;
			case 3 -> 0;
			default -> index;
		};

		Transform bgr = index -> switch(index) {
			case 0 -> 2;
			case 2 -> 0;
			default -> index;
		};

		Mapper mapper = (index, bytes) -> switch(index) {
			default -> bytes[index];
			case 2 -> Byte.MAX_VALUE;
		};


		void copy(byte[] src, byte[] dest, int step, Transform trans, Mapper mapper) {
			for(int n = 0; n < dest.length; n += 4) {
				for(int c = 0; c < 4; ++c) {
					final int index = trans.transform(c);
					dest[n + c] = mapper.map(n * step + index, src);
				}
			}
		}
*/

		/**
		 * Loads an image.
		 * @param name Image name
		 * @return Image
		 * @throws IOException
		 * @throws ServiceException if the given image cannot be loaded
		 */
		public ImageData load(String name) throws IOException {
			// Load raw image
			final BufferedImage image;
			try(final InputStream in = src.apply(name)) {
				image = ImageIO.read(in);
			}

			// Allocate image data
			final Dimensions dim = new Dimensions(image.getWidth(), image.getHeight());
			final int len = dim.width() * dim.height();
			final byte[] bytes = new byte[len * 4];

			final DataBufferByte buffer = (DataBufferByte) image.getRaster().getDataBuffer();
			final byte[] src = buffer.getData();

			switch(image.getType()) {
			case BufferedImage.TYPE_4BYTE_ABGR:
				for(int n = 0; n < bytes.length; n += 4) {
					bytes[n] = src[n + 3];
					bytes[n + 1] = src[n + 2];
					bytes[n + 2] = src[n + 1];
					bytes[n + 3] = src[n];
				}
				break;

			case BufferedImage.TYPE_3BYTE_BGR:
				int index = 0;
				for(int n = 0; n < bytes.length; n += 4) {
					bytes[n] = src[index + 2];
					bytes[n + 1] = src[index + 1];
					bytes[n + 2] = src[index];
					bytes[n + 3] = Byte.MAX_VALUE;
					index += 3;
				}
				break;

			default:
				throw new IOException("");
			}

			// Create image wrapper
			return new DefaultImageData(dim, bytes);
		}
	}
}

//		private static BufferedImage addAlphaChannel(BufferedImage image) {
//
//
//
//			final int type = switch(image.getType()) {
//				case BufferedImage.TYPE_3BYTE_BGR -> BufferedImage.TYPE_4BYTE_ABGR;
//				default -> throw new UnsupportedOperationException("");
//			};
//
//			final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);
//
//			final Graphics2D g = result.createGraphics();
//			g.drawImage(image, 0, 0, null);
//			//g.setComposite(alpha);
//			g.dispose();
//			//g.drawImage(img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, observer)
//
//			return result;
//
//		}
//		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//		private BufferedImage ApplyTransparency(BufferedImage image, Image mask)
//		{
//		    BufferedImage dest = new BufferedImage(
//		            image.getWidth(), image.getHeight(),
//		            BufferedImage.TYPE_INT_ARGB);
//		    Graphics2D g2 = dest.createGraphics();
//		    g2.drawImage(image, 0, 0, null);
//		    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F);
//		    g2.setComposite(ac);
//		    g2.drawImage(mask, 0, 0, null);
//		    g2.dispose();
//		    return dest;
//		}
//	}
//}
