---
title: Textures
---

## Overview

In this chapter we will load and apply a _texture_ to the demo.

This involves uploading the image data to the hardware in a similar fashion to the vertex buffers in the previous chapter.  However for texture images we also make use of _pipeline barriers_ to transform the image to the appropriate layout during the loading process.

The process of loading a texture consists of the following steps:

1. Load the native image.

2. Transform the image to a format and structure supported by Vulkan.

3. Copy the image data to a staging buffer.

4. Create a texture on the hardware.

5. Transition the texture so it is ready for the image.

6. Copy the staged image data to the texture.

7. Transition the texture to a layout ready for sampling.

Note that unlike the swapchain images the application is responsible for allocating and managing texture images, therefore we will also refactor the existing image accordingly.

We will require the following components:

* A new domain object representing a platform-independant image and an associated loader.

* The refactored image class.

* A command to copy the image data to the texture.

* Pipeline barriers.

* The texture sampler.

* Texture coordinates.

We will first modify the demo to render a quad and implement texture coordinates before moving on to loading the image to the texture.

---

## Texture Coordinates

### Quad

First we change the vertex data to render a coloured quad with white in the bottom-right corner:

```java
new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
new Vertex.Builder().position(new Point(-0.5f, +0.5f, 0)).colour(new Colour(0, 1, 0, 1)).build(),
new Vertex.Builder().position(new Point(+0.5f, -0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
new Vertex.Builder().position(new Point(+0.5f, +0.5f, 0)).colour(new Colour(1, 1, 1, 1)).build(),
```

The default drawing primitive is a _triangle strip_ with vertices ordered as follows:

```
0---2---4-- etc
|   |   |
1---3---5
```

The first triangle is comprised of the first three vertices with each subsequent triangle incrementing the 'index' by one resulting in 012, 123, 234, etc. 

Notes:

* By default the Y direction is inverted in the Vulkan coordinate system.

* The triangles in a strip have an _alternating_ winding order.

After changing the number of vertices to 4 in the drawing command we should see something like the following:

![Textured Quad](quad.png)

### Coordinates

Next we implement the texture coordinate domain object:

```java
public interface Coordinate extends Vertex.Component {
    record Coordinate1D(float u) implements Coordinate {
        ...
    }

    record Coordinate2D(float u, float v) implements Coordinate {
        public static final Layout LAYOUT = Layout.of(2);

        @Override
        public final void buffer(ByteBuffer buffer) {
            buffer.putFloat(u).putFloat(v);
        }

        @Override
        public Layout layout() {
            return LAYOUT;
        }
    }

    record Coordinate3D(float u, float v, float w) implements Coordinate {
        ...
    }
}
```

For convenience we define the texture coordinates for a quad in the 2D implementation:

```java
public static final Coordinate2D
    TOP_LEFT        = new Coordinate2D(0, 0),
    BOTTOM_LEFT     = new Coordinate2D(0, 1),
    TOP_RIGHT       = new Coordinate2D(1, 0),
    BOTTOM_RIGHT    = new Coordinate2D(1, 1);
```

We can now replace the colour data in the quad with texture coordinates as shown here for the top-left vertex:

```java
new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).coordinates(Coordinate2D.TOP_LEFT).build()
```

Finally we modify the second attribute of the vertex input pipeline stage to configure the structure of the texture coordinates:

```java
.binding()
    .index(0)
    .stride(Point.LAYOUT.length() + Coordinate2D.LAYOUT.length())
    .build()
.attribute()
    .binding(0)
    .location(0)
    .format(VkFormat.R32G32B32_SFLOAT)
    .offset(0)
    .build()
.attribute()
    .binding(0)
    .location(1)
    .format(VkFormat.R32G32_SFLOAT)
    .offset(Point.LAYOUT.length())
    .build()
```

### Vertex Shader

We modify the vertex shader to replace the colour with a texture coordinate which is passed through to the fragment shader:

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

In the fragment shader we fake a red-green colour based on the texture coordinates:

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

We can now be fairly confident that the texture coordinates are being handled correctly before we move onto texture sampling.

### Vertex Configuration Redux

As noted above the configuration of the vertex input pipeline stage is currently quite laborious and requires hard-coded of the vertex attribute format.  However we already have the necessary information represented by the layout of the vertex data, hence we add a new convenience helper to the pipeline stage builder.

The new method first allocates the next available binding index:

```java
public VertexInputStageBuilder add(List<Layout> layout) {
    // Allocate next binding
    int index = bindings.size();
    
    ...
}
```

We use the nested builder to configure the binding for the layout:

```java
// Calculate vertex stride for this layout
int stride = Layout.stride(layout);

// Add binding
new BindingBuilder()
    .index(index)
    .stride(stride)
    .build();
```

Where `stride` is another new helper on the layout class:

```java
public static int stride(Collection<Layout> layout) {
    return layout.stream().mapToInt(Vertex.Layout::length).sum();
}
```

Next we iterate over the layout to construct a vertex attribute for each entry:

```java
// Add attribute for each layout component
int offset = 0;
int loc = 0;
for(Layout attr : layout) {
    // Determine component format
    final VkFormat format = FormatBuilder.format(attr);

    // Add attribute for component
    new AttributeBuilder()
        .binding(index)
        .location(loc)
        .format(format)
        .offset(offset)
        .build();
        
    // Increment offset to the start of the next attribute
    ++loc;
    offset += entry.length();
}
```

Notes:

* The _location_ of each attribute is assumed to begin at index zero.

* The loop calculates the cumulative _offset_ of each attribute within a vertex.

* We utilise the `FormatBuilder` to determine the attribute formats.

We can now replace the configuration for the vertex data in the pipeline with the following considerably simpler code:

```
.input()
    .add(List.of(Point.LAYOUT, Coordinate2D.LAYOUT))
    .build()
```

---

## Images

### Image Data

In the short-term we will use the built-in AWT support for images - we are likely to want to replace this with a more flexible (and frankly better) implementation at some point in the future, e.g. to support a wider choice of image formats or to use the Android platform (where the AWT package is unavailable).

Our requirements for images are fairly straight-forward so we introduce the following abstraction rather than using (for example) Java images directly:

```java
public interface ImageData {
    /**
     * @return Image dimensions
     */
    Dimensions size();

    /**
     * @return Image layout
     */
    Layout layout();

    /**
     * @return Image data
     */
    byte[] bytes();
}
```

Notes:

* We assume that image data is stored as bytes.

* A vertex layout is reused to represent the structure of the image data.

### Image Loader

The process of loading a texture image consists of the following steps:

1. Load the native image using the `ImageIO` helper class.

2. Add an alpha channel as required.

3. Fiddle the colour channels as necessary (native images are BGR whereas Vulkan is generally RGBA).

4. Wrap the resultant image with the new domain class.

Loading the native image is straight-forward:

```java
public ImageData load(InputStream in) throws IOException {
    BufferedImage image = ImageIO.read(in);
    if(image == null) throw new IOException(...);
    ...
}
```

We transform the data to a format supported by Vulkan depending on the native image type:

```java
final BufferedImage result = switch(image.getType()) {
    // Gray-scale
    case BufferedImage.TYPE_BYTE_GRAY -> image;

    // RGB
    case BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_BYTE_INDEXED -> swizzle(alpha(image));

    // RGBA
    case BufferedImage.TYPE_4BYTE_ABGR -> swizzle(image);

    // Unknown
    default -> throw new RuntimeException(...);
};
```

Transforming the image to a format suitable for Vulkan turned out to be much harder than anticipated using the built-in Java libraries - in particular adding an alpha component either requires fiddly array manipulation or re-drawing the image, neither of which are very palatable.  In the end we opted for the simpler (if slower and uglier) approach of simply re-drawing the image into a buffered image with an alpha channel:

```java
private static BufferedImage alpha(BufferedImage image) {
    BufferedImage alpha = new BufferedImage(
        image.getWidth(),
        image.getHeight(),
        BufferedImage.TYPE_4BYTE_ABGR
    );
    Graphics g = alpha.getGraphics();
    try {
        g.drawImage(image, 0, 0, null);
    }
    finally {
        g.dispose();
    }
    return alpha;
}
```

The method to swizzle the channels from BGRA to RGBA iterates the underlying data and swaps at the byte level:

```java
private static BufferedImage swizzle(BufferedImage image) {
    DataBufferByte data = (DataBufferByte) image.getRaster().getDataBuffer();
    byte[] bytes = data.getData();
    for(int n = 0; n < bytes.length; n += 4) {
        swap(bytes, n, 0, 3);
        swap(bytes, n, 1, 2);
    }
    return image;
}
```

Which uses the following helper:

```java
private static void swap(byte[] bytes, int index, int src, int dest) {
    int a = index + src;
    int b = index + dest;
    byte temp = bytes[a];
    bytes[a] = bytes[b];
    bytes[b] = temp;
}
```

> There are probably much simpler and better solutions for adding the alpha channel and channel swizzles.

Finally we wrap the resultant buffered image:

```java
return new ImageData() {
    @Override
    public Dimensions size() {
        return new Dimensions(result.getWidth(), result.getHeight());
    }

    @Override
    public Layout layout() {
        int num = result.getColorModel().getNumComponents();
        return new Layout(num, 1, Byte.class);
    }

    @Override
    public byte[] bytes() {
        DataBufferByte buffer = (DataBufferByte) result.getRaster().getDataBuffer();
        return buffer.getData();
    }
};
```

Note that layout of the image assumes one byte per channel.

This loader is somewhat crude and brute-force, but it does the business for the images we are interested in for the forseeable future.  We add the following unit-test to check the texture images we will be using in the next few chapters:

```java
@ParameterizedTest
@CsvSource({
    "duke.jpg, 375, 375, 4",
    "duke.png, 375, 375, 4",
    "heightmap.jpg, 256, 256, 1",
})
void load(String filename, int w, int h, int components) throws IOException {
    // Load image from file-system
    Path path = Paths.get("./src/test/resources", filename);
    BufferedImage buffered = ImageIO.read(Files.newInputStream(path));

    // Load image wrapper
    ImageData image = loader.load(buffered);
    assertNotNull(image);

    // Check image properties
    assertEquals(new Dimensions(w, h), image.size());
    assertEquals(Layout.of(components, Byte.class), image.layout());

    // Check image data
    assertNotNull(image.bytes());
    assertEquals(w * h * components, image.bytes().length);
}
```

---

## Textures

### Texture Image

To allow us to create Vulkan images programatically we first change the existing implementation to an interface:

```java
public interface Image extends NativeObject {
    /**
     * @return Descriptor for this image
     */
    ImageDescriptor descriptor();

    /**
     * @return Device context for this image
     */
    DeviceContext device();
}
```

And we refactor the swapchain builder to instantiate a new local implementation:

```java
private static class SwapChainImage implements Image {
    private final Handle handle;
    private final LogicalDevice dev;
    private final ImageDescriptor descriptor;
}
```

We next add a second image implementation to support textures:

```java
class DefaultImage extends AbstractVulkanObject implements Image {
    private final ImageDescriptor descriptor;
    private final DeviceMemory mem;

    @Override
    protected Destructor<DefaultImage> destructor(VulkanLibrary lib) {
        return lib::vkDestroyImage;
    }

    @Override
    protected void release() {
        if(!mem.isDestroyed()) {
            mem.close();
        }
    }
}
```

With a builder to configure the various image properties:

```java
class Builder {
    private ImageDescriptor descriptor;
    private MemoryProperties<VkImageUsage> props;
    private VkSampleCountFlag samples = VkSampleCountFlag.COUNT_1;
    private VkImageTiling tiling = VkImageTiling.OPTIMAL;
    private VkImageLayout layout = VkImageLayout.UNDEFINED;
}
```

To instantiate an image we first populate the relevant descriptor:

```java
public DefaultImage build(LogicalDevice dev, AllocationService allocator) {
    VkImageCreateInfo info = new VkImageCreateInfo();
    info.imageType = descriptor.type();
    info.format = descriptor.format();
    info.extent = descriptor.extents().toExtent3D();
    info.mipLevels = descriptor.levels();
    info.arrayLayers = descriptor.layers();
    info.samples = samples;
    info.tiling = tiling;
    info.initialLayout = layout;
    info.usage = IntegerEnumeration.mask(props.usage());
    info.sharingMode = props.mode();
    ...
}
```

Next the API is invoked to create the image:

```java
VulkanLibrary lib = dev.library();
PointerByReference handle = lib.factory().pointer();
check(lib.vkCreateImage(dev, info, null, handle));
```

Allocating and binding the image memory follows the same pattern as the vertex buffer in the previous chapter:

```java
// Retrieve image memory requirements
var reqs = new VkMemoryRequirements();
lib.vkGetImageMemoryRequirements(dev, handle.getValue(), reqs);

// Allocate image memory
DeviceMemory mem = allocator.allocate(reqs, props);

// Bind memory to image
check(lib.vkBindImageMemory(dev, handle.getValue(), mem, 0));
```

And finally we create the domain object:

```java
return new DefaultImage(handle.getValue(), dev, descriptor, mem);
```

The image API is extended accordingly:

```java
interface VulkanLibraryImage {
    int  vkCreateImage(LogicalDevice device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pImage);
    void vkDestroyImage(DeviceContext device, Image image, Pointer pAllocator);
    void vkGetImageMemoryRequirements(LogicalDevice device, Pointer image, VkMemoryRequirements pMemoryRequirements);
    int  vkBindImageMemory(LogicalDevice device, Pointer image, DeviceMemory memory, long memoryOffset);
    ...
}
```

### Image Copy

To copy the image data from the staging buffer to the texture (or vice-versa) we implement a new command:

```java
public class ImageCopyCommand implements Command {
    private final Image image;
    private final VulkanBuffer buffer;
    private final VkBufferImageCopy[] regions;
    private final VkImageLayout layout;
    private final boolean bufferToImage;

    @Override
    public void execute(VulkanLibrary lib, Command.Buffer cb) {
        if(bufferToImage) {
            lib.vkCmdCopyBufferToImage(cb, buffer, image, layout, regions.length, regions);
        }
        else {
            lib.vkCmdCopyImageToBuffer(cb, image, layout, buffer, regions.length, regions);
        }
    }
}
```

We implement a builder to construct the copy command:

```java
public ImageCopyCommand build() {
    // Populate descriptor
    var copy = new VkBufferImageCopy();
    copy.imageSubresource = SubResource.of(image.descriptor(), subresource).toLayers();
    copy.imageExtent = image.descriptor().extents().toExtent3D();
    copy.imageOffset = offset;
    
    // Create copy command
    return new ImageCopyCommand(image, buffer, new VkBufferImageCopy[]{copy}, layout, bufferToImage);
}
```

A slight irritation that only came to light during development of the copy command is that there are two slightly different Vulkan descriptors for image sub-resources - `VkImageSubresourceRange` which is used when created an image view, and `VkImageSubresourceLayers` which is used above.  We bodge the sub-resource builder to support both cases rather than creating two separate builders or some overly complex class-hierarchy.

TODO - sub-resource builder from image descriptor or another sub-resource???

```java
public static SubResource of(ImageDescriptor descriptor, SubResource subresource) {
    if(subresource == null) {
        return of(descriptor);
    }
    else {
        return subresource;
    }
}
```

Finally we add a helper to create the extents descriptor for an image:

```java
public VkExtent3D toExtent3D() {
    VkExtent3D extent = new VkExtent3D();
    extent.width = dimensions.width();
    extent.height = dimensions.height();
    extent.depth = depth;
    return extent;
}
```

### Texture Sampler

A _texture sampler_ is used in the fragment shader to sample a fragment colour from the texture image:

```java
public class Sampler extends AbstractVulkanObject {
    ...
    
    @Override
    protected Destructor<Sampler> destructor(VulkanLibrary lib) {
        return lib::vkDestroySampler;
    }
}
```

To configure the sampler we implement a builder:

```java
public static class Builder {
    private VkFilter magFilter = VkFilter.LINEAR;
    private VkFilter minFilter = VkFilter.LINEAR;
    private VkSamplerMipmapMode mipmapMode = VkSamplerMipmapMode.LINEAR;
    private float minLod;
    private float maxLod;
    private float mipLodBias;
    private final VkSamplerAddressMode[] addressMode = new VkSamplerAddressMode[3];
    private VkBorderColor border;
    private float anisotropy = 1f;
}
```

The `addressMode` array specifies the _wrapping policy_ in the three axes of a texture, which we represent by the following enumeration:

```java
public enum Wrap {
    REPEAT,
    EDGE,
    BORDER;
}
```

In the builder we configure the wrapping policy using the following helper that determines the appropriate addressing mode:

```java
public VkSamplerAddressMode mode(boolean mirror) {
    return switch(this) {
        case REPEAT -> mirror ? VkSamplerAddressMode.MIRRORED_REPEAT : VkSamplerAddressMode.REPEAT;
        case EDGE -> mirror ? VkSamplerAddressMode.MIRROR_CLAMP_TO_EDGE : VkSamplerAddressMode.CLAMP_TO_EDGE;
        case BORDER -> VkSamplerAddressMode.CLAMP_TO_BORDER;
    };
}
```

We initialise the wrapping policy in the constructor:

```java
public Builder() {
    Arrays.fill(addressMode, VkSamplerAddressMode.REPEAT);
}
```

The builder populates a descriptor for the sampler and invokes the API:

```java
public Sampler build() {
    // Populate descriptor
    VkSamplerCreateInfo info = new VkSamplerCreateInfo();
    ...

    // Allocate sampler
    VulkanLibrary lib = dev.library();
    PointerByReference handle = lib.factory().pointer();
    check(lib.vkCreateSampler(dev, info, null, handle));

    // Create sampler
    return new Sampler(handle.getValue(), dev);
}
```

Finally we add the new API methods to the image library:

```java
interface VulkanLibraryImage {
    int  vkCreateSampler(LogicalDevice device, VkSamplerCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSampler);
    void vkDestroySampler(DeviceContext device, Sampler sampler, Pointer pAllocator);
}
```

### Pipeline Barrier

A _pipeline barrier_ is a command used to synchronise access to images, buffers and memory objects within the pipeline:

```java
public class Barrier implements Command {
    private final int src, dest;
    private final VkImageMemoryBarrier[] images;

    /**
     * Constructor.
     * @param src           Source pipeline stages
     * @param dest          Destination pipeline stages
     * @param images        Image memory barriers
     */
    private Barrier(Set<VkPipelineStage> src, Set<VkPipelineStage> dest, VkImageMemoryBarrier[] images) {
        this.src = IntegerEnumeration.mask(src);
        this.dest = IntegerEnumeration.mask(dest);
        this.images = notNull(images);
    }

    @Override
    public void execute(VulkanLibrary lib, Buffer buffer) {
        lib.vkCmdPipelineBarrier(buffer, src, dest, 0, 0, null, 0, null, images.length, images);
    }
}
```

Notes:

* The _src_ and _dest_ members specify how the image is used before/after the barrier transition.

* For the demo we only require a partial implementation to support image transitions (the other arguments are initialised to zero or `null` in the API call).

We implement a builder to configure a barrier:

```java
public static class Builder {
    private final Set<VkPipelineStage> srcStages = new HashSet<>();
    private final Set<VkPipelineStage> destStages = new HashSet<>();
    private final List<ImageBarrierBuilder> images = new ArrayList<>();
    
    ...
    
    public Barrier build() {
        var array = StructureHelper.array(images, VkImageMemoryBarrier::new, ImageBarrierBuilder::populate);
        return new Barrier(srcStages, destStages, array);
    }
}
```

To configure an image memory transition as part of the barrier we implement a nested builder:

```java
public class ImageBarrierBuilder {
    private final Image image;
    private final Set<VkAccess> src = new HashSet<>();
    private final Set<VkAccess> dest = new HashSet<>();
    private VkImageLayout oldLayout = VkImageLayout.UNDEFINED;
    private VkImageLayout newLayout;
    private SubResource subresource;
}
```

The Vulkan descriptor for an image barrier is populated as follows:

```java
private void populate(VkImageMemoryBarrier barrier) {
    barrier.image = image.handle();
    barrier.srcAccessMask = IntegerEnumeration.mask(src);
    barrier.dstAccessMask = IntegerEnumeration.mask(dest);
    barrier.oldLayout = oldLayout;
    barrier.newLayout = newLayout;
    barrier.subresourceRange = SubResource.of(image.descriptor(), subresource).toRange();
}
```

---

## Integration

We now have all the new components we need to load an image to the hardware and create the texture sampler.

We start a new configuration class:

```java
@Configuration
public class TextureConfiguration {
    @Autowired private LogicalDevice dev;

    @Bean
    public View texture(AllocationService allocator, Pool graphics) throws IOException {
        ...
    }

    @Bean
    public Sampler sampler() {
        return new Sampler.Builder(dev).build();
    }
}
```

In the `texture` bean method we first load the image from the file-system:

```java
ImageData.Loader loader = new ImageData.Loader();
ImageData image = loader.load(new FileInputStream("./src/main/resources/thiswayup.jpg"));
```

From which we can determine the appropriate Vulkan format:

```java
VkFormat format = FormatBuilder

() // R8G8B8A8_UNORM
    .bytes(1)
    .type(FormatBuilder.Type.NORMALIZED)
    .signed(false)
    .build();
```

The image is a `TYPE_3BYTE_BGR` buffered image which maps to the `R8G8B8A8_UNORM` format.

Next we create a texture configured according to the image:

```java
// Create descriptor
ImageDescriptor descriptor = new ImageDescriptor.Builder()
    .type(VkImageType.IMAGE_TYPE_2D)
    .aspect(VkImageAspect.COLOR)
    .extents(new ImageExtents(image.size()))
    .format(format)
    .build();

// Init image memory properties
var props = new MemoryProperties.Builder<VkImageUsage>()
    .usage(VkImageUsage.TRANSFER_DST)
    .usage(VkImageUsage.SAMPLED)
    .required(VkMemoryProperty.DEVICE_LOCAL)
    .build();

// Create texture
Image texture = new Image.Builder()
    .descriptor(descriptor)
    .properties(props)
    .build(dev, allocator);
```

The new texture is prepared for writing by an image barrier transition:

```java
new Barrier.Builder()
    .source(VkPipelineStage.TOP_OF_PIPE)
    .destination(VkPipelineStage.TRANSFER)
    .barrier(texture)
        .newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
        .destination(VkAccess.TRANSFER_WRITE)
        .build()
    .build()
    .submitAndWait(graphics);
```

We load the image data to a staging buffer:

```java
Bufferable data = Bufferable.of(image.bytes());
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, data);
```

And invoke the copy command:

```java
new ImageCopyCommand.Builder()
    .image(texture)
    .buffer(staging)
    .layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
    .build()
    .submitAndWait(graphics);
```

The texture is then transitioned again ready for use by the sampler:

```java
new Barrier.Builder()
    .source(VkPipelineStage.TRANSFER)
    .destination(VkPipelineStage.FRAGMENT_SHADER)
    .barrier(texture)
        .oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
        .newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL)
        .source(VkAccess.TRANSFER_WRITE)
        .destination(VkAccess.SHADER_READ)
        .build()
    .build()
    .submitAndWait(graphics);
```

Finally we create a default image view for the texture:

```java
staging.close();
return View.of(texture);
```

This code is still quite imperative and long-winded but relatively straight-forward.  We could make more use of dependency injection but most of the objects we create are not required outside of this class.

---

## Summary

In this chapter we:

- Introduced texture coordinates.

- Created a basic image loader.

- Added support for texture images and samplers.

- Partially implemented pipeline barriers.

