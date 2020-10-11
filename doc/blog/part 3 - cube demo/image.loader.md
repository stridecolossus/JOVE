# Image Wrapper

```java
public interface ImageData {
	/**
	 * @return Image dimensions
	 */
	Dimensions size();

	/**
	 * @return Component sizes
	 */
	List<Integer> components();

	/**
	 * @return Image data
	 */
	ByteBuffer buffer();
}
```

# Image Loader

```java
class Loader {
	public ImageData load(InputStream in) {
		// Load image
		final BufferedImage image;
		try {
			image = ImageIO.read(in);
		}
		catch(IOException e) {
			throw new RuntimeException(e);
		}
		if(image == null) {
			throw new RuntimeException("Invalid image");
		}

		// Convert image
		final BufferedImage result = switch(image.getType()) {
			// Gray-scale
			case BufferedImage.TYPE_BYTE_GRAY -> {
				yield image;
			}

			// RGB
			case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_INDEXED -> {
				if(add) {
					yield swizzle(alpha(image));
				}
				else {
					yield swizzle(image);
				}
			}

			// RGBA
			case BufferedImage.TYPE_4BYTE_ABGR -> swizzle(alpha(image));

			// Unknown
			default -> throw new RuntimeException("Unsupported image format: " + image);
		};

		// Create image wrapper
		final Dimensions dim = new Dimensions(result.getWidth(), result.getHeight());
		final int[] components = result.getColorModel().getComponentSize();
		final DataBufferByte data = (DataBufferByte) result.getRaster().getDataBuffer();
		return new DefaultImageData(dim, components, ByteBuffer.wrap(data.getData()));
	}
}
```

# Alpha Channel

```java
private static BufferedImage alpha(BufferedImage image) {
	final BufferedImage alpha = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
	final Graphics g = alpha.getGraphics();
	try {
		g.drawImage(image, 0, 0, null);
	}
	finally {
		g.dispose();
	}
	return alpha;
}
```

# Swizzle

```java
private static BufferedImage swizzle(BufferedImage image) {
	final DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
	final byte[] bytes = data.getData();
	for(int n = 0; n < bytes.length; n += 4) {
		swap(bytes, n, 0, 3);
		swap(bytes, n, 1, 2);
	}
	return image;
}

private static void swap(byte[] bytes, int index, int src, int dest) {
	final int a = index + src;
	final int b = index + dest;
	final byte temp = bytes[a];
	bytes[a] = bytes[b];
	bytes[b] = temp;
}
```

# Test

```java
@ParameterizedTest
@CsvSource({
	"duke.jpg, 375, 375, 4",
	"duke.png, 375, 375, 4",
	"heightmap.jpg, 256, 256, 1",
})
void load(String filename, int w, int h, int components) throws IOException {
	// Load image from file-system
	final ImageData image;
	final Path path = Paths.get("./src/test/resources", filename);
	System.out.println(path);
	try(final InputStream in = Files.newInputStream(path)) {
		image = loader.load(in);
	}

	// Check image
	assertNotNull(image);
	assertEquals(new Dimensions(w, h), image.size());
	assertNotNull(image.components());
	assertEquals(components, image.components().size());

	// Check buffer
	assertNotNull(image.buffer());
	assertTrue(image.buffer().isReadOnly());
	assertEquals(w * h * components, image.buffer().capacity());
}
```
