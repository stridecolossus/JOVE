package org.sarge.jove.platform.vulkan.image;

import java.io.DataInput;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.lang3.ArrayUtils;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Layout;
import org.sarge.jove.io.Bufferable;
import org.sarge.jove.io.ImageData;
import org.sarge.jove.io.ImageData.AbstractImageData;
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
	private record LevelIndex(int offset, int len, int uncompressed) {
		// Empty
	}

	/**
	 *
	 */
	private class Level {
		private final byte[] bytes;

		public Level(int len) {
			this.bytes = new byte[len];
		}
	}

	/**
	 *
	 */
	private static class VulkanImageData extends AbstractImageData {
		private final Level[] data;

		private VulkanImageData(Dimensions size, String components, Layout layout, Level[] data) {
			super(size, components, layout);
			this.data = data;
		}

		@Override
		public int levels() {
			return data.length;
		}

		// TODO
		@Override
		public int layers() {
			return 1;
		}

		@Override
		public Bufferable data(int layer, int level) {
			if(layer != 0) throw new UnsupportedOperationException("TODO");
			return Bufferable.of(data[level].bytes);
		}
	}

	@Override
	public LittleEndianDataInputStream map(InputStream in) throws IOException {
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
		final LevelIndex[] index = new LevelIndex[levelCount];
		for(int n = 0; n < levelCount; ++n) {
			final int offset = (int) in.readLong();
			final int len = (int) in.readLong();
			final int uncompressed = (int) in.readLong();
			index[n] = new LevelIndex(offset, len, uncompressed);
		}

		// Skip format descriptor
		final int formatTotalSize = in.readInt();
		in.skipBytes(formatTotalSize);
		assert formatByteLength == formatTotalSize;
		assert formatByteOffset > 0;
		// TODO

		// Skip key-values
		in.skipBytes(keyValuesByteLength);
		assert keyValuesByteOffset > 0;
		// TODO

		// Skip compression
		if(compressionByteLength != 0) throw new UnsupportedOperationException();
		assert compressionByteOffset >= 0;
		// TODO

		// Load image data
		final Level[] levels = new Level[index.length];
		ArrayUtils.reverse(index);
		for(int n = 0; n < index.length; ++n) {
			// Skip to start of next level
			final LevelIndex level = index[n];
			if(n > 0) {
				final int skip = index[n - 1].offset - level.offset;
				assert skip >= 0;
				in.skipBytes(skip);
			}

			// Load level
			final Level mip = new Level(level.len);
			in.readFully(mip.bytes);
			levels[n] = mip;
		}

		// Create image
		final Dimensions size = new Dimensions(width, height);
		final Layout layout = Layout.bytes(4); // TODO
		return new VulkanImageData(size, "RGBA", layout, levels);
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

	////////////

	public static void main2(String[] args) throws Exception {
		final VulkanImageLoader loader = new VulkanImageLoader();
		final var result = loader.load(new LittleEndianDataInputStream(new FileInputStream("../Demo/Data/chalet.ktx2")));
		System.out.println(result);
	}
}

// http://www.peterfranza.com/2008/09/26/little-endian-input-stream/
// https://github.com/KhronosGroup/3D-Formats-Guidelines/blob/main/KTXDeveloperGuide.md
