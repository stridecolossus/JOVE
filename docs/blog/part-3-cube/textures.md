---
title: Textures and Descriptor Sets
---

## Overview

In this chapter we will load and apply a _texture_ to our demo.

This will involve uploading the image data to the hardware in a similar fashion to the vertex buffers we implemented in the previous step. 
However for texture images we also make use of _pipeline barriers_ to transform the image to the appropriate layout at various stages.

The process of loading a texture is as follows:

1. Load the platform-specific image.

2. Transform the image data to a format and structure appropriate for Vulkan.

3. Copy the image data to a staging buffer.

4. Create a texture on the hardware.

5. Transition the texture so it is ready for the image.

6. Copy the staged image data to the texture.

7. Transition the texture to a layout ready for sampling.

Finally we will then create a _texture sampler_ to apply this image to the geometry and implement a _descriptor set_ that configures the texture image in the pipeline.

---

## Textures

### Image Loader

In the short-term we will use the built-in AWT support for images - we are likely to want to replace this with a more flexible (and frankly better) implementation at some point in the future, e.g. to support a wider choice of image formats or to use the Android platform (where the AWT package is unavailable).

Therefore we abstract over the underlying image implementation as follows:

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

    /**
     * Default implementation.
     */
    class DefaultImageData implements ImageData {
        private final Dimensions size;
        private final List<Integer> components;
        private final ByteBuffer data;
        
        ...
    }
}
```

Notes:

- We may need more information down the line but this is sufficient for this stage of development.

- Images as treated as static data, i.e. we are not planning to implement any image processing and assume all image data is pre-processed externally.

- We assume all images are represented as bytes.

- ImageData is not a very good name for this class but we are running out of synonyms.

The process of loading an image is:
1. Load the AWT buffered image using `ImageIO`.
2. Add an alpha channel if necessary.
3. Fiddle the colour channels as required, e.g. BGR to RGB.
4. Convert the resultant image data to an NIO buffer.

We add a static method to load the image:

```java
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
}
```

Next we transform the loaded image depending on its type:

```java
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
```

Finally we create the image domain object:

```java
// Buffer image data
final DataBufferByte data = (DataBufferByte) result.getRaster().getDataBuffer();
final ByteBuffer buffer = Bufferable.allocate(data.getData());

// Enumerate image components
final int[] components = result.getColorModel().getComponentSize();
final var list = Arrays.stream(components).boxed().collect(toList());

// Create image wrapper
final Dimensions dim = new Dimensions(result.getWidth(), result.getHeight());
return new DefaultImageData(dim, list, buffer);
```

Which uses the following helper on the bufferable class to create a _direct_ NIO buffer:

```java
ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

static ByteBuffer allocate(int len) {
    return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
}
```

Transforming the image to a format suitable for Vulkan turned out to be much harder than anticipated using the built-in Java libraries - in particular adding an alpha component either requires fiddly array manipulation or re-drawing the image, neither of which are very palatable.

In the end we opted for the simpler (if slower and uglier) approach of simply re-drawing the image into a buffered image with an alpha channel:

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

The native image channels are in BGR order so we apply the following low-level byte swizzle for colour images:

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

This loader is somewhat crude and brute-force, but it does the business for the images we are interested in for the forseeable future.  We add the following unit-test to check the texture images we will be using in the next few chapters:

```java
@ParameterizedTest
@CsvSource({
    "duke.jpg, 375, 375, 4",
    "duke-translucent.png, 375, 375, 4",
    "heightmap.jpg, 256, 256, 1",
    ...
})
void load(String filename, int w, int h, int components) throws IOException {
    // Load image from file-system
    final ImageData image;
    final Path path = Paths.get("./src/test/resources", filename);
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

### Texture Images

Up until now we have not needed to create an image explicitly (the swapchain images were created for us).  However texture images will need to be managed by the application, i.e. a native object with an explicit `destroy()` method.  Therefore we refactor the existing image class to an interface and create separate implementations for the two cases.

The image class now looks like this:

```java
public interface Image extends NativeObject {
    /**
     * @return Descriptor for this image
     */
    Descriptor descriptor();

    /**
     * Image extents.
     */
    record Extents(int width, int height, int depth) {
        ...
    }
    
    /**
     * Descriptor for an image.
     */
    record Descriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspects) {
    }
    
    /**
     * Default implementation.
     */
    class DefaultImage extends AbstractVulkanObject implements Image {
        private final Descriptor descriptor;
        private final Pointer mem;

        /**
         * Constructor.
         * @param handle        Handle
         * @param descriptor    Image descriptor
         * @param mem           Internal memory
         * @param dev           Logical device
         */
        protected DefaultImage(Handle handle, Descriptor descriptor, Pointer mem, LogicalDevice dev) {
            super(handle, dev, dev.library()::vkDestroyImage);
            this.descriptor = notNull(descriptor);
            this.mem = notNull(mem);
        }

        @Override
        public Descriptor descriptor() {
            return descriptor;
        }

        @Override
        public synchronized void destroy() {
            final LogicalDevice dev = this.device();
            dev.library().vkFreeMemory(dev.handle(), mem, null);
            super.destroy();
        }
    }
}
```

We introduce an _image descriptor_ and refactor the swapchain accordingly (with a custom image implementation that cannot be destroyed).
The view class is also refactor to destroy the underlying image as required:

```java
@Override
public synchronized void destroy() {
    if(image instanceof TransientNativeObject obj) {
        obj.destroy();
    }
    super.destroy();
}
```

Now we can add a builder to the image class to support textures:

```java
public static class Builder {
    private final LogicalDevice dev;
    private final VkImageCreateInfo info = new VkImageCreateInfo();
    private final Set<VkImageUsageFlag> usage = new HashSet<>();
    private final Set<VkImageAspectFlag> aspects = new HashSet<>();
    private final MemoryAllocator.Allocation allocation;
    private Extents extents;

    public Builder(LogicalDevice dev) {
        this.dev = notNull(dev);
        this.allocation = dev.allocator().allocation();
        init();
    }

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
}
```

The build method creates the image in roughly the same fashion as creating a vertex buffer (also using the new memory allocator):

```java
public Image build() {
    // Validate image
    if(info.format == null) throw new IllegalArgumentException("Image format not specified");
    if(extents == null) throw new IllegalArgumentException("Image extents not specified");

    // Complete descriptor
    info.extent = this.extents.toExtent3D();
    info.usage = IntegerEnumeration.mask(usage);

    // Allocate image
    final VulkanLibrary lib = dev.library();
    final PointerByReference ref = lib.factory().pointer();
    check(lib.vkCreateImage(dev.handle(), info, null, ref));

    // Create image descriptor
    final Handle handle = new Handle(ref.getValue());
    final Descriptor descriptor = new Descriptor(info.imageType, info.format, extents, aspects);

    // Retrieve image memory requirements
    final var reqs = new VkMemoryRequirements();
    lib.vkGetImageMemoryRequirements(dev.handle(), handle, reqs);

    // Allocate image memory
    final Pointer mem = allocation.init(reqs).allocate();
    check(lib.vkBindImageMemory(dev.handle(), handle, mem, 0));

    // Create image
    return new DefaultImage(handle, descriptor, mem, dev);
}
```

### Integration #1

We can now use these new components to load a texture image in the demo:

```java
// Load image
final ImageData image = ImageData.load(new FileInputStream("./src/test/resources/thiswayup.jpg"));

// Copy image to staging buffer
final ByteBuffer bb = image.buffer();
final VulkanBuffer staging = VulkanBuffer.staging(dev, bb.capacity());
staging.load(bb);

// Determine image format
final VkFormat format = FormatBuilder.format(image);

// Create texture
final Image texture = new Image.Builder(dev)
    .extents(Image.Extents.of(image.size()))
    .format(format)
    .usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
    .usage(VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
    .build();
```

Notes:

- We add a convenience factory for staging buffers.

- We also add a helper method to the format builder to determine the image format.

- The image we are using is a `TYPE_3BYTE_BGR` buffered image which requires an alpha channel to be added and results in the `VK_FORMAT_R8G8B8A8_UNORM` Vulkan format.

---

## Barrier Transitions

### Barrier Class

Next we implement a new domain class and builder for the pipeline barriers:

```java
public class Barrier implements ImmediateCommand {
    private final int src, dest;
    private final VkImageMemoryBarrier[] images;

    @Override
    public void execute(VulkanLibrary lib, Handle handle) {
        lib.vkCmdPipelineBarrier(handle, src, dest, 0, 0, null, 0, null, images.length, images);
    }

    public static class Builder {
        private final Set<VkPipelineStageFlag> srcStages = new HashSet<>();
        private final Set<VkPipelineStageFlag> destStages = new HashSet<>();
        private final List<VkImageMemoryBarrier> images = new ArrayList<>();

        ...

        public Barrier build() {
            if(images.isEmpty()) throw new IllegalArgumentException("Barrier is empty");
            return new Barrier(srcStages, destStages, images);
        }
    }
}
```

For our demo we will be executing this command immediately and waiting for it to complete.  For convenience we introduce the `ImmediateCommand` adapter to run one-time commands:

```java
public static abstract class ImmediateCommand implements Command {
    /**
     * Submits this as a <i>one-time</i> command to the given pool and waits for completion.
     * @param pool Command pool
     * @see Work#submit(Command, Command.Pool)
     */
    public void submit(Command.Pool pool) {
        Work.submit(this, pool);
    }
}
```

This delegates to a new helper on the `Work` class we implemented previously:

```java
public static void submit(Command cmd, Command.Pool pool) {
    // Allocate and record command
    final Command.Buffer buffer = pool
        .allocate()
        .begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
        .add(cmd)
        .end();

    try {
        // Submit work
        final Queue queue = pool.queue();
        final Work work = new Builder().add(buffer).build();
        work.submit();

        // Wait for completion
        queue.waitIdle();
    }
    finally {
        // Cleanup
        buffer.free();
    }
}
```

A _barrier_ is used to synchronise access to images, buffers and memory objects.

For the current demo we only need the image barrier part which is implemented as a nested local class:

```java
public class ImageBarrierBuilder {
    private final Image image;
    private final Set<VkAccessFlag> src = new HashSet<>();
    private final Set<VkAccessFlag> dest = new HashSet<>();
    private VkImageLayout oldLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
    private VkImageLayout newLayout;
    private final ImageSubResourceBuilder<ImageBarrierBuilder> subresource = new ImageSubResourceBuilder<>(this);

    ...

    private void populate(VkImageMemoryBarrier barrier) {
        // Populate image barrier descriptor
        barrier.image = image.handle();
        barrier.srcAccessMask = IntegerEnumeration.mask(src);
        barrier.dstAccessMask = IntegerEnumeration.mask(dest);
        barrier.oldLayout = oldLayout;
        barrier.newLayout = newLayout;

        // Populate sub-resource range descriptor
        subresource.populate(barrier.subresourceRange);
    }

    public Builder build() {
        // Validate
        if(newLayout == null) throw new IllegalArgumentException("New layout not specified");
        if(newLayout == oldLayout) throw new IllegalArgumentException("Previous and next layouts cannot be the same");

        // Add barrier
        images.add(this);

        return Builder.this;
    }
}
```

The _src_ and _dest_ fields specify how the image is used before and after the barrier transition.

### Image sub-resources

The barrier also configures the _sub-resource range_ which specifies a _view_ of the image, e.g. the number of accessible mipmap levels.

The sub-resource range is implemented as another nested builder:

```java
public class ImageSubResourceBuilder<T> {
    private final T parent;
    private final Set<VkImageAspectFlag> aspects = new HashSet<>();
    private int mipLevel;
    private int levelCount = 1;
    private int baseArrayLayer;
    private int layerCount = 1;

    public ImageSubResourceBuilder(T parent) {
        this.parent = notNull(parent);
    }
    
    ...
    
    public void populate(VkImageSubresourceRange range) {
        range.aspectMask = IntegerEnumeration.mask(aspects);
        range.baseMipLevel = mipLevel;
        range.levelCount = levelCount;
        range.baseArrayLayer = baseArrayLayer;
        range.layerCount = layerCount;
    }

    public T build() {
        return parent;
    }
}
```

Image sub-resources are used in several areas so this nested builder has a generic _parent_ returned in its `build()` method (similar to the approach we took when constructing the pipeline).

A slight irritation that only came to light during development of the copy command (below) is that there are two slightly different Vulkan descriptors for image sub-resources.  We bodge the sub-resource builder to support both (i.e. overloaded `populate()` methods) rather than creating two separate builders or some overly complex class-hierarchy.

### Image Copying

After preparing the texture the next step is to copy the image from the staging buffer:

```java
public class ImageCopyCommand extends ImmediateCommand {
    private final Image image;
    private final VulkanBuffer buffer;
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

The `invert()` method converts this command to copy from the texture to the buffer, we won't be using this for some time but it's trivial to implement while we're at it.

### Integration #2

We can now copy the image to the texture on the hardware, transition it to a layout suitable for sampling and delete the intermediate staging buffer:

```java
// Copy staging to texture
new ImageCopyCommand.Builder()
    .buffer(staging)
    .image(texture)
    .layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
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
```

---

## Texture Sampling



### Texture Coordinates

Before we progress any further we will modify the demo to include texture coordinates and render a quad rather than a triangle.

First we change the vertex data to render a coloured quad with white in the bottom-right corner:

```java
final Vertex[] vertices = {
    new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
    new Vertex.Builder().position(new Point(-0.5f, +0.5f, 0)).colour(new Colour(0, 1, 0, 1)).build(),
    new Vertex.Builder().position(new Point(+0.5f, -0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
    new Vertex.Builder().position(new Point(+0.5f, +0.5f, 0)).colour(new Colour(1, 1, 1, 1)).build(),
};
```

The default drawing primitive is a _triangle strip_ which assumes vertices are ordered as follows:

```
0---2---4-- etc
|   |   |
1---3---5
```

The first triangle is comprised of the first three vertices with each subsequent triangle incrementing the 'index' by one resulting in 012, 123, 234, etc. 
Additionally the triangles in the strip have an _alternating_ winding order.

After changing the number of vertices to 4 in the drawing command we should see something like the following:

![Textured Quad](quad.png)

Next we implement the texture coordinate domain class that we glossed over earlier:

```java
public interface Coordinate extends Bufferable {
    class Coordinate1D implements Coordinate {
        public final float u;

        public Coordinate1D(float u) {
            this.u = u;
        }

        @Override
        public final void buffer(ByteBuffer buffer) {
            buffer.putFloat(u);
        }
    }

    class Coordinate2D extends Coordinate1D {
        /**
         * Size of 2D coordinates.
         */
        public static final int SIZE = 2;

        /**
         * Quad coordinates.
         */
        public static final Coordinate2D
            TOP_LEFT        = new Coordinate2D(0, 0),
            BOTTOM_LEFT     = new Coordinate2D(0, 1),
            TOP_RIGHT       = new Coordinate2D(1, 0),
            BOTTOM_RIGHT    = new Coordinate2D(1, 1);

        public final float v;

        @Override
        public final void buffer(ByteBuffer buffer) {
            buffer.putFloat(u).putFloat(v);
        }
    }

    class Coordinate3D extends Coordinate2D {
        public final float w;
        ...
    }
}
```

We can now replace the colour data in the quad with texture coordinates:

```java
new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).coordinates(Coordinate2D.TOP_LEFT).build(),
new Vertex.Builder().position(new Point(-0.5f, +0.5f, 0)).coordinates(Coordinate2D.BOTTOM_LEFT).build(),
new Vertex.Builder().position(new Point(+0.5f, -0.5f, 0)).coordinates(Coordinate2D.TOP_RIGHT).build(),
new Vertex.Builder().position(new Point(+0.5f, +0.5f, 0)).coordinates(Coordinate2D.BOTTOM_RIGHT).build(),
```

and modify the vertex layout accordingly:

```java
Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.COORDINATE);
```

### Shader Modifications

We modify the vertex shader by replacing the colour with a texture coordinate which is passed through to the fragment shader:

```glsl
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
```

Finally we modify the fragment shader to fake a colour based on the texture coordinates:

```glsl
#version 450 core

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 outColor;

void main(void) {
    outColor = vec4(texCoord.x, texCoord.y, 0, 1);
}
```

This should render the quad with black in the top-left corner (corresponding to the origin texture coordinate) and yellow in the bottom-right (full red and green):

![Textured Quad](faked-quad.png)

We can now be fairly confident that the texture coordinates are being handled correctly before we apply the texture.

### Texture Sampler

With the image uploaded to the hardware we next implement the _texture sampler_ class:

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

This is relatively simple domain object used to configure the various sampling properties.  The `Wrap` enumeration is used to simplify specification of the texture addressing mode for coordinates that are outside of the texture.

TODO

We also integrate the supported features functionality implemented back in the chapter on devices, for example:

```java
/**
 * Sets the number of texel samples for anisotropy filtering.
 * @param maxAnisotropy Number of texel samples
 * @throws IllegalStateException if anisotropy filtering is not enabled
 */
public Builder anisotropy(float anisotropy) {
    dev.features().check("samplerAnisotropy");
    this.anisotropy = oneOrMore(anisotropy);
    return this;
}
```

---

## Summary

In this chapter we:

- Created a rough-and-ready image loader.

- Implemented commands and pipeline barriers to upload a texture image to the hardware.

- Created the sampler and texture coordinate classes.

- Implemented descriptor sets to configure the sampler in the pipeline.

The API methods to manage images, views and sampler are defined in the `VulkanLibraryImage` JNA interface.
