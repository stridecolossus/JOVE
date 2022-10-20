package org.sarge.jove.platform.vulkan.image;

import static java.util.stream.Collectors.joining;

import java.io.*;
import java.util.*;
import java.util.stream.IntStream;

import org.sarge.jove.common.*;
import org.sarge.jove.io.*;
import org.sarge.jove.io.ImageData.Level;

/**
 * Loader for a KTX image.
 * <p>
 * TODO - skipped
 * <p>
 * @see <a href="https://www.khronos.org/registry/KTX/specs/2.0/ktxspec_v2.html">KTX2 specification</a>
 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#formats-compatibility">Formats compatibility</a>
 * @see <a href="https://github.com/KhronosGroup/3D-Formats-Guidelines/blob/main/KTXDeveloperGuide.md">Developer Guide</a>
 * @author Sarge
 */
public class VulkanImageLoader implements ResourceLoader<DataInput, ImageData> {
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
		final Dimensions size = new Dimensions(in.readInt(), in.readInt());
		final int depth = Math.max(1, in.readInt());

		// Load image data size
		final int layerCount = Math.max(1, in.readInt());
		final int faceCount = in.readInt();
		final int levelCount = in.readInt();

		// Validate
		validate(1, layerCount, "LayerCount");
		if((faceCount == Image.CUBEMAP_ARRAY_LAYERS) && !size.isSquare()) {
			throw new IllegalArgumentException("Cubemap images must be square");
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
		in.readLong();
		final long compressionByteLength = in.readLong();

		// Load MIP level index
		final List<Level> index = loadIndex(in, levelCount);

		// Load DFD
		final Sample[] samples = loadFormatDescriptor(in, formatByteLength, typeSize);

		// Load key-values
		loadKeyValues(in, keyValuesByteLength);

		// Load compression
		// TODO - compression?
		if(compressionByteLength != 0) throw new UnsupportedOperationException();

		// Load image data
		final int len = index.stream().mapToInt(Level::length).sum();
		final byte[] data = new byte[len];
		in.readFully(data, 0, len);

		// Build image layout
		// TODO - should be INTEGER? but VK format needs to be UNORM
		final Component layout = new Component(samples.length, Component.Type.NORMALIZED, false, samples[0].bytes());
		final String components = Arrays.stream(samples).map(Sample::channel).collect(joining());

		// Create KTX image
		return new ImageData(size, components, layout, data) {
			@Override
			public int depth() {
				return depth;
			}

			@Override
			public int format() {
				return format;
			}

			@Override
			public List<Level> levels() {
				return index;
			}

			@Override
			public int layers() {
				return faceCount;
			}

			@Override
			protected int pixel(int index) {
				return LittleEndianDataInputStream.convert(data, index, layout.bytes());
			}
		};
	}

	/**
	 * Loads and validates the KTX header.
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
	 * Loads the MIP index.
	 * @param in
	 * @param count Number of MIP levels
	 * @return MIP index
	 */
	private static List<Level> loadIndex(DataInput in, int count) throws IOException {
		// Load MIP level index
		final int[] offset = new int[count];
		final int[] length = new int[count];
		for(int n = 0; n < count; ++n) {
			offset[n] = (int) in.readLong();
			length[n] = (int) in.readLong();
			in.readLong();
		}

		// Truncate offsets relative to start of image and convert to MIP index
		final int start = offset[count - 1];
		return IntStream
				.range(0, count)
				.mapToObj(n -> new Level(offset[n] - start, length[n]))
				.toList();
	}

	/**
	 * Image sample descriptor.
	 */
	private record Sample(String channel, int bits) {
		private int bytes() {
			return (bits + 1) / Byte.SIZE;
		}
	}

	/**
	 * Loads the data format descriptor (DFD).
	 * @param in			Data
	 * @param total 		Expected DFD size
	 * @param typeSize		Expected number of bytes per sample
	 * @return Image samples
	 * @see <a href="https://www.khronos.org/registry/DataFormat/specs/1.0/chunked/index.html">Khronos DFD documentation</a>
	 * @see <a href="https://www.khronos.org/registry/DataFormat/api/1.1/khr_df.h">Enumerations header</a>
	 */
	@SuppressWarnings("unused")
	static Sample[] loadFormatDescriptor(DataInput in, int total, int typeSize) throws IOException {
		// Check DFD size
		if(in.readInt() != total) throw new IllegalArgumentException("Invalid DFD length");

		// Skip if empty
		if(total == 0) {
			return new Sample[]{};
		}

		// Load header
		in.readShort();									// Vendor
		final short type = in.readShort();				// DFD Type
		final short ver = in.readShort();				// Version number
		final short blockSize = in.readShort();			// 24 + 16 per sample (bytes)
		final byte model = in.readByte();				// KHR_DF_MODEL_RGBSDA (1)
		final byte primaries = in.readByte();			// KHR_DF_PRIMARIES_BT709 (1)
		final byte transferFunction = in.readByte();	// KHR_DF_TRANSFER_LINEAR (1) or KHR_DF_TRANSFER_SRGB (2)
		final byte flags = in.readByte();				// KHR_DF_FLAG_ALPHA_STRAIGHT (0) or KHR_DF_FLAG_ALPHA_PREMULTIPLIED (1)

		// Validate
		validate(type, 0, "DescriptorType");
		validate(ver, 2, "Version");

		// Skip texel dimensions
		final byte[] array = new byte[4];
		in.readFully(array);

		// Skip planes
		in.readFully(array);							// 0-3
		in.readFully(array);							// 4-7

		// Load samples
		final int num = (blockSize - 24) /  16;
		final Sample[] samples = new Sample[num];
		for(int n = 0; n < num; ++n) {
			// Load sample information
			in.readShort();								// Bit offset (assumes compacted)
			final byte bits = in.readByte();
			final char channel = channel(in.readByte());
			samples[n] = new Sample(String.valueOf(channel), bits);

			// Validate type size
			if(samples[n].bytes() != typeSize) {
				throw new UnsupportedOperationException(String.format("Invalid bit length for channel [%c]: typeSize=%d bits=%d", channel, typeSize, bits));
			}

			// Skip sample position and lower/upper bounds
			in.readInt();
			in.readInt();
			in.readInt();
		}

		return samples;
	}

	/**
	 * Maps the given channel to the corresponding component character.
	 * Top 4 bits is the data qualifier (unused).
	 * @param channel Channel
	 * @return Component character
	 */
	private static char channel(byte channel) {
		return switch(channel & 0x0f) {
			case 0 -> 'R';
			case 1 -> 'G';
			case 2 -> 'B';
			// TODO - depth/stencil
			// 13 -> stencil
			// 14 -> depth
			case 15 -> 'A';
			default -> throw new IllegalArgumentException("Unsupported or invalid channel: " + channel);
		};
	}

	/**
	 * Loads the key-values.
	 * @param in
	 * @param size		Expected size
	 * @return Key-values entries
	 */
	static List<byte[]> loadKeyValues(DataInput in, int size) throws IOException {
		// Skip if empty
		if(size == 0) {
			return List.of();
		}

		// Load key-values
		final List<byte[]> entries = new ArrayList<>();
		int count = 0;
		while(true) {
			// Load key-value length
			final int len = in.readInt();

			// Load key-value entry
			final byte[] entry = new byte[len];
			in.readFully(entry);
			entries.add(entry);

			// Calculate padding
			final int padding = padding(len);
			in.skipBytes(padding);

			// Calculate position
			count += len + padding + 4;
			assert count <= size;

			// Stop at end of key-values
			if(count >= size) {
				break;
			}
		}

		return entries;
	}

	/**
	 * Calculates the number of padding bytes required to align the given length on a 4-byte boundary.
	 * @param len Length
	 * @return Padding
	 */
	private static int padding(int len) {
		return 3 - ((len + 3) % 4);
	}

	/**
	 * Validates a KTX field.
	 * @param expected		Expected value
	 * @param actual		Actual loaded value
	 * @param name			Field name
	 * @throws UnsupportedOperationException if the values do not match
	 */
	private static void validate(int expected, int actual, String name) {
		// TODO - optional
		if(expected != actual) {
			throw new UnsupportedOperationException(String.format("Unsupported or unexpected value: %s: expected=%d actual=%d", name, expected, actual));
		}
	}
}
