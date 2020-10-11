# Image Descriptor

```java
public static record Descriptor(Handle handle, VkImageType type, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspects) { ... }
```

# Image Builder

```java
public static class Builder {
	private final LogicalDevice dev;
	private final VkImageCreateInfo info = new VkImageCreateInfo();
	private final Set<VkImageUsageFlag> usage = new HashSet<>();
	private final Set<VkMemoryPropertyFlag> props = new HashSet<>();
	private final Set<VkImageAspectFlag> aspects = new HashSet<>();
	private Extents extents;

	private void init() {
		type(VkImageType.VK_IMAGE_TYPE_2D);
		mipLevels(1);
		arrayLayers(1);
		samples(VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT);
		tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL);
		mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
		initialLayout(VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED);
	}

	...

	public Image build() {
		// Validate image
		if(info.format == null) throw new IllegalArgumentException("Image format not specified");
		if(extents == null) throw new IllegalArgumentException("Image extents not specified");

		// Complete create descriptor
		info.extent = this.extents.create();
		info.usage = IntegerEnumeration.mask(usage);

		// Allocate image
		final VulkanLibrary lib = dev.library();
		final PointerByReference ref = lib.factory().pointer();
		check(lib.vkCreateImage(dev.handle(), info, null, ref));

		// Create image descriptor
		final Handle handle = new Handle(ref.getValue());
		final Descriptor descriptor = new Descriptor(handle, info.imageType, info.format, extents, aspects);

		// Retrieve image memory requirements
		final var reqs = new VkMemoryRequirements();
		lib.vkGetImageMemoryRequirements(dev.handle(), handle, reqs);

		// Allocate image memory
		final Pointer mem = dev.allocate(reqs, props);
		check(lib.vkBindImageMemory(dev.handle(), handle, mem, 0));

		// Create image
		return new Image(descriptor, mem, dev);
	}
}
```

# Demo

```java
// Load image
final File dir = ...
final ImageData.Loader loader = new ImageData.Loader(DataSource.file(dir));
final ImageData image = loader.load("thiswayup.jpg");

// Copy image to staging buffer
final ByteBuffer bb = image.buffer();
final VertexBuffer staging = VertexBuffer.staging(dev, bb.capacity());
staging.load(bb);

// TODO - helper on image builder
final VkFormat format = new FormatBuilder()
	.components(image.components().size())
	.bytes(1)
	.signed(false)
	.type(Type.NORMALIZED)
	.build();

// Create texture
final Image texture = new Image.Builder(dev)
	.extents(Image.Extents.of(image.size()))
	.format(format)
	.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
	.usage(VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT)
	.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
	.build();
```

# Transition Barrier 

```java
public class Barrier implements ImmediateCommand {
	private final int src, dest;
	private final VkImageMemoryBarrier[] images;

	@Override
	public void execute(VulkanLibrary lib, Handle handle) {
		lib.vkCmdPipelineBarrier(handle, src, dest, 0, 0, null, 0, null, images.length, images);
	}

	public static class Builder {
		...
	}
	
	public class ImageBarrierBuilder {
		...
	}
}
```

# Immediate Command

```java
/**
 * Adapter for a command that can be submitted immediately.
 */
abstract class ImmediateCommand implements Command {
	/**
	 * Submits this command to the given pool.
	 * @param pool Command pool
	 * @param wait Whether to wait for completion
	 */
	public void submit(Command.Pool pool, boolean wait) {
		// Allocate one-off buffer
		final Command.Buffer buffer = pool.allocate();
		buffer.once(this);

		try {
			// Perform work
			final Work work = new Builder().add(buffer).build();
			work.submit();

			// Wait for work to complete
			if(wait) {
				buffer.pool().queue().waitIdle();
			}
		}
		finally {
			// Release
			buffer.free();
		}
	}
}
```

# Copy Command

```java
public class ImageCopyCommand extends ImmediateCommand {
	private final Image image;
	private final VertexBuffer buffer;
	private final VkBufferImageCopy[] regions;
	private final VkImageLayout layout;

	...
	
	@Override
	public void execute(VulkanLibrary lib, Handle handle) {
		lib.vkCmdCopyBufferToImage(handle, buffer.handle(), image.handle(), layout, regions.length, regions);
	}

	/**
	 * Inverts this command to copy <b>from</b> the image to the buffer.
	 * @return Inverted copy command
	 */
	public Command invert() {
		return (api, handle) -> api.vkCmdCopyImageToBuffer(handle, image.handle(), layout, buffer.handle(), regions.length, regions);
	}

	public static class Builder {
		...
	}
}
```

# Image sub-resource builder (factor)

```java
public class ImageSubResourceBuilder<T> {
	private final T parent;
	private final Set<VkImageAspectFlag> aspects = new HashSet<>();
	private int mipLevel;
	private int levelCount = 1;
	private int baseArrayLayer;
	private int layerCount = 1;

	/**
	 * Constructor.
	 * @param parent Parent builder
	 */
	public ImageSubResourceBuilder(T parent) {
		this.parent = notNull(parent);
	}

	...

	/**
	 * @return Image sub-resource range descriptor
	 */
	public VkImageSubresourceRange range() {
		final VkImageSubresourceRange range = new VkImageSubresourceRange();
		...
		return range;
	}

	/**
	 * @return Image sub-resource layers descriptor
	 */
	public VkImageSubresourceLayers layers() {
		final VkImageSubresourceLayers layers = new VkImageSubresourceLayers();
		...
		return layers;
	}

	/**
	 * Constructs this image sub-resource.
	 * @return Parent builder
	 */
	public T build() {
		return parent;
	}
}
```

# Sampler

```java
public class Sampler extends AbstractVulkanObject {
	/**
	 * The <i>wrapping policy</i> specifies how coordinates outside of the texture are handled.
	 */
	public enum Wrap {
		REPEAT,
		EDGE,
		BORDER
	}

	...

	public static class Builder {
		
		...
		
		public Sampler build() {
			// Create descriptor
			final VkSamplerCreateInfo info = new VkSamplerCreateInfo();

			...

			// Allocate sampler
			final VulkanLibrary lib = dev.library();
			final PointerByReference handle = lib.factory().pointer();
			check(lib.vkCreateSampler(dev.handle(), info, null, handle));

			// Create sampler
			return new Sampler(handle.getValue(), dev);
		}
	}
}
```

# Integration #2

```java
// Copy staging to texture
new ImageCopyCommand.Builder()
		.buffer(staging)
		.image(texture)
		.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
		.subresource()
			.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
			.build()
		.build()
		.submit(pool, true);

// Release staging
staging.destroy();

// Transition texture ready for sampling
new Barrier.Builder()
		.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
		.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
		.barrier(texture)
			.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
			.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
			.source(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
			.destination(VkAccessFlag.VK_ACCESS_SHADER_READ_BIT)
			.build()
		.build()
		.submit(pool, true);

// Create sampler
final Sampler sampler = new Sampler.Builder(dev).build();
```
