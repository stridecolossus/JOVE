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
public static class Loader {
	public Loader(DataSource src) {
		this.src = notNull(src);
		init();
	}
	
	...
}
```

# Loading

```java
public ImageData load(String name) throws IOException {
	// Load raw image
	final BufferedImage image;
	try(final InputStream in = src.apply(name)) {
		image = ImageIO.read(in);
	}

	// Lookup image converter
	final var entry = converters.get(image.getType());
	if(entry == null) throw new IOException("Unsupported image type: " + image);

	// Add alpha channel as required
	final BufferedImage result = addAlpha(image, entry.alpha);

	// Apply transforms
	// TODO - handle other buffer types (int, etc)?
	final DataBufferByte buffer = (DataBufferByte) result.getRaster().getDataBuffer();
	final byte[] bytes = buffer.getData();
	for(Transform t : entry.transforms) {
		t.transform(bytes, entry.components.length);
	}

	// Convert to buffer
	final ByteBuffer bb = ByteBuffer.wrap(bytes);

	// Create image wrapper
	final Dimensions dim = new Dimensions(image.getWidth(), image.getHeight());
	return new DefaultImageData(dim, entry.components, bb);
}
```

# Image Transforms

```java
public interface Transform {
	/**
	 * Applies this transform to the given image data.
	 * @param bytes		Image data
	 * @param step		Pixel step size
	 */
	void transform(byte[] bytes, int step);
}

/**
 * An <i>image swizzle</i> is used to swap components of an image byte array.
 * <p>
 * For example, to transform a BGR image to RGB:
 * <pre>
 *  byte[] bytes = ...
 *  Swizzle swizzle = new Swizzle(0, 2);		// Swap the R and G components
 *  swizzle.transform(bytes, 3);				// Apply to 3-component sized image
 * </pre>
 */
public record Swizzle(int src, int dest) implements Transform {
	@Override
	public void transform(byte[] bytes, int step) {
		for(int n = 0; n < bytes.length; n += step) {
			ArrayUtils.swap(bytes, n + src, n + dest);
		}
	}
}
```

# Supported Formats

```java
private static record Entry(Integer alpha, int[] components, Transform[] transforms) {
}

private final Map<Integer, Entry> converters = new HashMap<>();

private void init() {
	add(BufferedImage.TYPE_BYTE_INDEXED, 	1, null);
	add(BufferedImage.TYPE_3BYTE_BGR, 		4, BufferedImage.TYPE_4BYTE_ABGR, new Swizzle(0, 2));
	add(BufferedImage.TYPE_4BYTE_ABGR, 	4, null, new Swizzle(0, 3), new Swizzle(1, 2));
}
```

# Alpha Channel

```java
private static BufferedImage addAlpha(BufferedImage image, Integer type) {
	final BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), type);
	final Graphics g = result.getGraphics();
	g.drawImage(image, 0, 0, null);
	g.dispose();
	return result;
}
```

# Image Descriptor

```java
public static record Descriptor(Handle handle, VkImageType type, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspects) { ... }
```
