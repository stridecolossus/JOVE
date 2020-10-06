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
public class Barrier implements Command {
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
