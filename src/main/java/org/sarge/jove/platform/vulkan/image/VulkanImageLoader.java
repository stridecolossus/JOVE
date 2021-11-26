package org.sarge.jove.platform.vulkan.image;

import java.io.DataInput;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.Bufferable;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ImageData.AbstractImageData;
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
	private record Index(int offset, int length, int uncompressed) {
	}

	@Override
	public DataInput map(InputStream in) throws IOException {
		return new LittleEndianDataInputStream(in);
	}

	@Override
	public ImageData load(DataInput in) throws IOException {
		// Load and validate header
		loadHeader(in);

		// Load image descriptor
		final int format = in.readInt();						// 43 = R8G8B8A8_SRGB
		final int typeSize = in.readInt();						// 1 = size of the data type in bytes
		final int width = in.readInt();					// 4096 x 4096
		final int height = in.readInt();
		final int depth = Math.max(1, in.readInt());		// 0 -> 1
		final int layerCount = Math.max(1, in.readInt());		// 0 -> 1
		final int faceCount = in.readInt();						// 1
		final int levelCount = in.readInt();					// 13
		final int scheme = in.readInt();						// 0..3 (0 == none)

		// TODO
		// - validate vs image type
		// - layers == 1 (must)
		// - faces == 1 or 6
		// - depth
		// - w == h for cubes

		// Load format descriptor offsets
		final int formatByteOffset = in.readInt();
		final int formatByteLength = in.readInt();

		// Load key-value offsets
		final int keyValuesByteOffset = in.readInt();
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

		// Load DFD
		loadFormatDescriptor(in, formatByteLength);

		// Load key-values
		loadKeyValues(in, keyValuesByteLength);

		// Load compression
		// TODO
		if(compressionByteLength != 0) throw new UnsupportedOperationException();
		assert compressionByteOffset >= 0;

		// Build levels (smallest MIP level first)
		final List<Level> levels = new ArrayList<>();
		int len = 0;
		for(int n = index.length - 1; n >= 0; --n) {
			final Level level = new Level(len, index[n].length);
			levels.add(level);
			len += index[n].length;
		}

		// Load image data
		final byte[][] data = new byte[1][len];
		for(Level level : levels) {
			in.readFully(data[0], level.offset(), level.length());
// TODO - calc this in Index
//			final int padding = 3 - ((level.len + 3) % 4);
//			in.skipBytes(padding);
		}

		// Order by MIP level index
		Collections.reverse(levels);

		// Create image
		final Dimensions size = new Dimensions(width, height);
		final Layout layout = Layout.bytes(4); // TODO
		return new AbstractImageData(size, "RGBA", layout, levels) {
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
		if(!str.contains("KTX 20")) throw new IOException("Invalid KTX file");
	}

	/**
	 *
	 * @param in
	 * @param total
	 * @throws IOException
	 */
	private static void loadFormatDescriptor(DataInput in, int total) throws IOException {
		if(in.readInt() != total) throw new IOException("Invalid DFD length");

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
