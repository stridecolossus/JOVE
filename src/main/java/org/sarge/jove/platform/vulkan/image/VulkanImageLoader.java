package org.sarge.jove.platform.vulkan.image;

import static java.util.stream.Collectors.toList;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.collections4.iterators.ReverseListIterator;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.Bufferable;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ImageData.AbstractImageData;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.io.ImageData.Level;
import org.sarge.jove.io.ResourceLoader;
import org.sarge.jove.util.LittleEndianDataInputStream;

/**
 *
 * @author Sarge
 */
public class VulkanImageLoader implements ResourceLoader<DataInput, ImageData> {
	/**
	 *
	 */
	private static class Index {
		private int offset;
		private final int length;
		private final int uncompressed;

		/**
		 * Constructor.
		 * @param offset				Byte offset into file
		 * @param length				Length
		 * @param uncompressed			Uncompressed length
		 */
		private Index(int offset, int length, int uncompressed) {
			this.offset = offset;
			this.length = length;
			this.uncompressed = uncompressed;
		}

		private Level level() {
			return new Level(offset, length);
		}

		@Override
		public String toString() {
			return offset + " " + length;
		}
	}

	@Override
	public DataInput map(InputStream in) throws IOException {
		return new LittleEndianDataInputStream(in);
	}

	@Override
	public ImageData load(DataInput in) throws IOException {
		// Load and validate header
		loadHeader(in);

		// Load image format
		final int format = in.readInt();
		final int typeSize = in.readInt();

		// Load image extents
		final Extents extents = new Extents(
				new Dimensions(in.readInt(), in.readInt()),
				Math.max(1, in.readInt())
		);

		// Load image
		final int layerCount = Math.max(1, in.readInt());
		final int faceCount = in.readInt();
		final int levelCount = in.readInt();

		// Validate
		if(layerCount > 1) throw new UnsupportedOperationException("Image layers must be one");
		switch(faceCount) {
			case 1:
				break;

			case 6:
				if(!extents.size().isSquare()) throw new IllegalArgumentException("");
				break;

			default:
				throw new UnsupportedOperationException("");
		}

		// Load compression scheme
		final int scheme = in.readInt();
		if(scheme > 3) throw new UnsupportedOperationException("Unsupported compression scheme: " + scheme);

		// Load format descriptor offsets
		in.readInt();
		final int formatByteLength = in.readInt();

		// Load key-value offsets
		in.readInt();
		final int keyValuesByteLength = in.readInt();

		// Load compression offsets
		final long compressionByteOffset = in.readLong();
		final long compressionByteLength = in.readLong();

		// Load MIP level index
		final Index[] index = new Index[levelCount];
		for(int n = 0; n < levelCount; ++n) {
			final int fileOffset = (int) in.readLong();
			final int len = (int) in.readLong();
			final int uncompressed = (int) in.readLong();
			index[n] = new Index(fileOffset, len, uncompressed);
		}

		// Re-calculate MIP level offsets relative to start of image array
		final int offset = index[index.length - 1].offset;
		for(int n = 0; n < levelCount; ++n) {
			index[n].offset -= offset;
		}

		// Load DFD
		loadFormatDescriptor(in, formatByteLength);

		// Load key-values
		in.skipBytes(keyValuesByteLength);

		// Load compression
		// TODO
		if(compressionByteLength != 0) throw new UnsupportedOperationException();
		assert compressionByteOffset >= 0;

		// Build MIP level index
		final List<Level> levels = Arrays.stream(index).map(Index::level).collect(toList());

		// Allocate image data
		final int len = levels.stream().mapToInt(Level::length).sum();
		final byte[][] data = new byte[1][len];

		// Load image data (smallest MIP level first)
		final Iterator<Level> itr = new ReverseListIterator<>(levels);
		while(itr.hasNext()) {
			final Level level = itr.next();
			in.readFully(data[0], level.offset(), level.length());
// TODO - calc this in Index
//			final int padding = 3 - ((level.len + 3) % 4);
//			in.skipBytes(padding);
		}

		// Create image
		final String components = "RGBA";			// TODO
		final Layout layout = Layout.bytes(4); 		// TODO
		return new AbstractImageData(extents, components, layout, levels) {
			@Override
			public Bufferable data(int layer) {
				return Bufferable.of(data[layer]);
			}
		};
	}

	/**
	 * Validates the KTX header.
	 */
	private static void loadHeader(DataInput in) throws IOException {
		// Load header
		final byte[] header = new byte[12];
		in.readFully(header);

		// Validate header
		final String str = new String(header);
		if(!str.contains("KTX 20")) throw new UnsupportedOperationException("Invalid KTX file");
	}

	/**
	 *
	 * @param in
	 * @param total
	 * @throws IOException
	 */
	private static void loadFormatDescriptor(DataInput in, int total) throws IOException {
		if(in.readInt() != total) throw new IllegalArgumentException("Invalid DFD length");

		if(total == 0) {
			return;
		}

		final short vendor = in.readShort();
		final short type = in.readShort();

		final short version = in.readShort();
		final short blockSize = in.readShort();

		final byte colourModel = in.readByte();
		final byte primaries = in.readByte();
		final byte transferFunction = in.readByte();
		final byte flags = in.readByte();

		final byte[] texelDimensions = new byte[4];
		in.readFully(texelDimensions);
		in.readFully(texelDimensions); // bytesPlane 0-3
		in.readFully(texelDimensions); // bytesPlane 4-7

		for(int n = 0; n < 4; ++n) {
			final byte bitOffset = in.readByte();
			final byte qualifiers = in.readByte();
			final byte bitLength = in.readByte();
			final byte channelType = in.readByte();

			final int samplePosition = in.readInt();
			final int lower = in.readInt();
			final int upper = in.readInt();
		}
	}

	/**
	 *
	 * @param in
	 * @param size
	 * @throws IOException
	 */
	private static void loadKeyValues(DataInput in, int size) throws IOException {
		if(size == 0) {
			return;
		}

		int count = 0;
		while(true) {
			final int len = in.readInt();
			final byte[] entry = new byte[len];
			in.readFully(entry);

			final int padding = 3 - ((len + 3) % 4);
			in.skipBytes(padding);

			count += len + padding + 4;

			if(count >= size) {
				break;
			}
		}
	}
}

// http://www.peterfranza.com/2008/09/26/little-endian-input-stream/
// https://github.com/KhronosGroup/3D-Formats-Guidelines/blob/main/KTXDeveloperGuide.md
// https://community.khronos.org/t/implementing-a-ktx-loader/107981/3
// https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#formats-compatibility
// https://satellitnorden.wordpress.com/2018/03/13/vulkan-adventures-part-4-the-mipmap-menace-mipmapping-tutorial/
