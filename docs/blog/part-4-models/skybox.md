---
title: Skybox
---

## Overview

In this chapter we will add a _skybox_ to the demo application.

The skybox is implemented as a cube centred on the camera rendered with a cubemap texture.

We will also make changes to the image framework to improve the performance of the texture loading process.

Finally we replace the existing uniform buffer with _push constants_ for the modelview and projection matrices.

---

## Skybox

### Pipeline Configuration

We start with a new configuration class for the skybox which requires new shaders and a custom pipeline configuration:

```java
@Configuration
public class SkyBoxConfiguration {
    @Bean
    public Pipeline skyboxPipeline(...) {
        return new Pipeline.Builder()
            ...
            .depth()
                .enable(true)
                .write(false)
                .build()
            .rasterizer()
                .cull(VkCullMode.FRONT)
                .build()
            .build(dev);
    }
}
```

Notes:

* The depth test is enabled but does not update the depth buffer (specified by the `write` property) which ensures the skybox is not rendered over existing geometry.

* Front faces are culled (see below) since the camera is _inside_ the skybox.

* The new pipeline shares the existing pipeline layout.

The builder for the _rasterizer_ pipeline stage is essentially a wrapper for the underlying descriptor:

```java
public class RasterizerStageBuilder extends AbstractPipelineBuilder<VkPipelineRasterizationStateCreateInfo> {
    private final VkPipelineRasterizationStateCreateInfo info = new VkPipelineRasterizationStateCreateInfo();

    public RasterizerStageBuilder cull(VkCullMode cullMode) {
        info.cullMode = notNull(cullMode);
        return this;
    }

    ...

    @Override
    VkPipelineRasterizationStateCreateInfo get() {
        return info;
    }
}
```

The rasterizer properties are initialised in the constructor:

```java
public RasterizerStageBuilder() {
    depthClamp(false);
    discard(false);
    polygon(VkPolygonMode.FILL);
    cull(VkCullMode.BACK);
    winding(VkFrontFace.COUNTER_CLOCKWISE);
    lineWidth(1);
}
```

Next we create a cube model for the skybox:

```java
@Bean
public static Model skybox() {
    return new CubeBuilder()
        .build()
        .transform(List.of(Point.LAYOUT));
}
```

Note that we only use the vertex positions in the skybox shader (see below).

Finally a cubemap sampler is created which also clamps texture coordinates:

```java
@Bean
public Sampler cubeSampler() {
    return new Sampler.Builder(dev)
        .wrap(Wrap.EDGE, false)
        .build();
}
```

### Shaders

In the vertex shader for the skybox we first split the matrices which were previously multiplied together by the application:

```glsl
#version 450

layout(set = 0, binding = 1) uniform UniformBuffer {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(location = 0) in vec3 inPosition;
layout(location = 0) out vec3 outCoords;
```

The reason for this change is to allow the vertex positions of the skybox cube to be transformed by the view without being affected by the other components:

```glsl
void main() {
    vec3 pos = mat3(view) * inPosition;
}
```

Note that we use the build-in `mat3` operator to extract the rotation component of the view matrix.

The projection transformation sets the last two components to be the same value such that the resultant vertex lies on the far clipping plane, i.e. the skybox is drawn _behind_ the rest of the geometry.

```glsl
gl_Position = (projection * vec4(pos, 0.0)).xyzz;
```

Finally the texture coordinate is simply set to the incoming vertex position (hence the reason for not including texture coordinates in the cube model).

```glsl
outCoords = inPosition;
```

> We could have used a similar technique in the rotating cube demo.

The full vertex shader is as follows:

```glsl
#version 450

layout(set = 0, binding = 1) uniform UniformBuffer {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(location = 0) in vec3 inPosition;

layout(location = 0) out vec3 outCoords;

void main() {
    vec3 pos = mat3(view) * inPosition;
    gl_Position = (projection * vec4(pos, 0.0)).xyzz;
    outCoords = inPosition;
}
```

Notes:

* The `model` matrix is not used in the skybox shader.

* The existing vertex shader for the chalet model is refactored accordingly (both pipelines share the same layout).

The fragment shader is the same as the previous demo except the sampler has a `samplerCube` declaration:

```glsl
#version 450

layout(location = 0) in vec3 inCoords;
layout(set = 0, binding = 0) uniform samplerCube cubemap;
layout(location = 0) out vec4 outColour;

void main() {
    outColour = texture(cubemap, inCoords);
}
```

### Loader

Initially we will load separate images for each 'face' of the cubemap texture.  The code roughly follows the same pattern as the chalet texture with the following differences:

The texture is configured with six array layers (for each face of the cube):

```java
ImageDescriptor descriptor = new ImageDescriptor.Builder()
    .type(VkImageType.IMAGE_TYPE_2D)
    .aspect(VkImageAspect.COLOR)
    .extents(new ImageExtents(...))
    .format(format)
    .arrayLayers(6)
    .build();
```

We add a new `cubemap` method to the image builder:

```java
Image texture = new Image.Builder()
    .descriptor(descriptor)
    .properties(props)
    .cubemap()
    .build(dev, allocator);
```

Configuring the texture as a cubemap is specified as a creation flag:

```java
public Builder cubemap() {
    return flag(VkImageCreateFlag.CUBE_COMPATIBLE);
}
```

We next iterate over an array of filenames to load each image:

```java
var loader = new ResourceLoaderAdapter<>(src, new NativeImageLoader());
String[] filenames = {"posx", "negx", ...};
for(int n = 0; n < 6; ++n) {
    ImageData image = loader.load(filenames[n] + ".jpg");
    ...
}
```

And perform a separate copy operation:

```java
new ImageCopyCommand.Builder()
    .image(texture)
    .buffer(staging)
    .layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
    .subresource(res)
    .build()
    .submitAndWait(graphics);
```

Where the sub-resource specifies the array layer for each face of the cubemap texture:

```java
SubResource res = new SubResource.Builder(descriptor)
    .baseArrayLayer(n)
    .build();
```

Finally we create a cubemap view for the image:

```java
SubResource subresource = new SubResource.Builder(descriptor)
    .layerCount(6)
    .build();

return new View.Builder(texture)
    .type(VkImageViewType.VIEW_TYPE_CUBE)
    .subresource(subresource)
    .mapping(image)
    .build();
```

### Integration

Integration of the skybox requires:

* Creation of a separate vertex buffer and draw command for the skybox model.

* The addition of a second group of descriptor sets for the new pipeline.

* Doubling the descriptor pool size.

The final change is the addition of the following commands to the render sequence:

```java
skyboxPipeline.bind()
set.bind(skyboxPipeline.layout())
skyboxVertexBuffer.bindVertexBuffer()
DrawCommand.of(skybox)
```

Note that the skybox is drawn _after_ the rest of the geometry reducing the number of fragments being rendered, i.e. the cubemap is only rendered to 'empty' parts of the scene.

If all goes well we should now see the skybox rendered behind the chalet model:

TODO

---

## KTX Images

### Overview

The process of loading the cubemap texture is quite slow and cumbersome:

1. Each cubemap texture is loaded and copied separately.

2. There is also the overhead of using the native image loader (including injection of the alpha channel).

Ideally we would prefer the following:

* A compound file format that reduces the file management overhead and allows the use of more efficient bulk transfer operations.

* An image format that more closely matches the target Vulkan texture with minimal (or ideally zero) transformation.

* Additionally we would also like to support MIP levels and eventually compressed image formats.

To satisfy all these requirements we will implement a loader for a [KTX](https://www.khronos.org/ktx/) image.  Texture images, cubemaps, MIP levels, etc. can then be prepared offline minimising the work that needs to be performed at runtime (and the amount of code required).

We will need the following new components:

* Modifications to the image class to support multiple layers and MIP levels.

* The KTX loader itself.

* The addition of copy regions to the command used to copy an image to the hardware.

### Loose Ends

However there is a small problem - a KTX image is a binary format with _little endian_ byte ordering whereas Java is big-endian by default.

Note that we _could_ load the entire file into an NIO byte buffer with little endian ordering which has a similar API to the data stream but we would prefer to stick with I/O streams for consistency with the existing loaders.  Additionally we anticipate that we will need to support other little endian file formats in the future.

The matter is further complicated when one considers the weird implementation of the `DataInputStream` class.  __All__ the methods are declared `final` (though not the class itself oddly enough) so essentially it is closed for extension.  The stream implements `DataInput` but there is no way to provide a custom implementation of this interface to the stream, so the abstraction is completely pointless.

Therefore we are forced to completely re-implement the whole data stream class rather than building on what is already available - great design!

We start with a custom data stream wrapper:

```java
public class LittleEndianDataInputStream extends InputStream implements DataInput {
    private final DataInputStream in;

    public LittleEndianDataInputStream(InputStream in) {
        this.in = new DataInputStream(in);
    }
}
```

Most of the methods defined in `DataInput` simply delegate to the underlying stream, for example:

```java
@Override
public int skipBytes(int n) throws IOException {
    return in.skipBytes(n);
}

@Override
public byte readByte() throws IOException {
    return in.readByte();
}
```

We can now provide overridden implementations for the cases that we _need_ to handle which boils down to three methods (out of fifteen) to support little endian data types, for example:

```java
public class LittleEndianDataInputStream extends InputStream implements DataInput {
    private static final int MASK = 0xff;

    private final byte[] buffer = new byte[8];

    public int readInt() throws IOException {
        in.readFully(buffer, 0, Integer.BYTES);
        return
                (buffer[3])        << 24 |
                (buffer[2] & MASK) << 16 |
                (buffer[1] & MASK) <<  8 |
                (buffer[0] & MASK);
    }
}
```

### Image Refactor

We next modify the general image abstraction to support multiple layers:

```java
public interface ImageData {
    int layers();
    Bufferable data(int layer);
}
```

And introduce a simple MIP level record:

```java
record Level(int offset, int length) {
}

List<Level> levels();
```

Where _offset_ indexes into the image data array.

Finally we refactor the native image accordingly with a single layer and MIP level.

### KTX Loader

We can now use the new stream implementation in the KTX loader:

```java
public class VulkanImageLoader implements ResourceLoader<DataInput, ImageData> {
    @Override
    public DataInput map(InputStream in) throws IOException {
        return new LittleEndianDataInputStream(in);
    }

    @Override
    public ImageData load(DataInput in) throws IOException {
        ...
    }
}
```

The [KTX file format](https://www.khronos.org/registry/KTX/specs/2.0/ktxspec_v2.html) consists of the following sections:

1. A header token.

2. Image details (dimensions, number of layers, etc).

3. A MIP level index.

4. A Data Format Descriptor (DFD)

5. Additional information represented as a set of key-value pairs.

6. Compression data.

7. Image data ordered by MIP level and layer.

Notes:

* The DFD, key-values and compression data are optional.

* For the moment we will assume image data is uncompressed and gloss over this section of the loader.

* The KTX file format has a large number of byte offsets/indices into the file itself which we largely ignore.

The header token is a fixed length byte array which we use to validate the file format:

```java
// Load header
byte[] header = new byte[12];
in.readFully(header);

// Validate header
String str = new String(header);
if(!str.contains("KTX 20")) throw new IOException(...);
```

Next the image header details are loaded (the comments illustrate the values for the chalet image):

```java
int format = in.readInt();                      // 43 = R8G8B8A8_SRGB
int typeSize = in.readInt();                    // 1 = size of the data type in bytes
int width = in.readInt();                       // 4096 x 4096
int height = in.readInt();
int depth = Math.max(1, in.readInt());
int layerCount = Math.max(1, in.readInt());
int faceCount = in.readInt();                   // 1 or 6 for a cubemap
int levelCount = in.readInt();                  // 13
int scheme = in.readInt();                      // 0..3 (0 = no compression)
```

Note that some values can be zero and need to be constrained accordingly, e.g. the image depth.

Next we load the MIP level index:

```java
Index[] index = new Index[levelCount];
for(int n = 0; n < levelCount; ++n) {
    int byteOffset = (int) in.readLong();
    int len = (int) in.readLong();
    int uncompressed = (int) in.readLong();
    index[n] = new Index(fileOffset, len, uncompressed);
}
```

Note:

* The `Index` is a simple local transient record type.

* The `byteOffset` field is the offset into the file itself.

* The index is in MIP level order (starting at zero for the largest image) whereas the actual data is the __reverse__ (smallest MIP image first).

TODO - DFD, key-values

From the index we can now build the list of MIP levels:

```java
final List<Level> levels = new ArrayList<>();
int len = 0;
for(int n = index.length - 1; n >= 0; --n) {
    final Level level = new Level(len, index[n].length);
    levels.add(level);
    len += index[n].length;
}
```

Note that this is done in __reverse__ order since we want to calculate the offset of each MIP level into the image data.

We can now load the rest of the file into the image data as a single byte array:

```java
byte[][] data = new byte[1][len];
for(Level level : levels) {
    in.readFully(data[0], level.offset(), level.length());
}
```

We assume that most consumers of the image will expect the MIP levels to be in ascending index order therefore we reverse the index:

```java
Collections.reverse(levels);
```

Finally we can create the resultant image:

```java
Dimensions size = new Dimensions(width, height);
Layout layout = Layout.bytes(4);
return new AbstractImageData(size, "RGBA", layout, levels) {
    @Override
    public Bufferable data(int layer) {
        return Bufferable.of(data[layer]);
    }
};
```





### Copy Region

In the previous implementation we used a staging buffer and a copy command for _each_ image.  To support the cubemap image we will extend the copy command to support _copy regions_ such that each element of the image array can be transferred to the corresponding array layer in the cubemap texture.

First a copy region is defined as a simple transient record with a companion builder:

```java
public class ImageCopyCommand implements Command {
    ...
    public record CopyRegion(long offset, int length, int height, SubResource res, VkOffset3D imageOffset, ImageExtents extents) {
    }
}
```

Each copy region populates the corresponding descriptor:

```java
private void populate(VkBufferImageCopy copy) {
    copy.bufferOffset = offset;
    copy.bufferRowLength = length;
    copy.bufferImageHeight = height;
    copy.imageSubresource = SubResource.toLayers(res);
    copy.imageOffset = imageOffset;
    copy.imageExtent = extents.toExtent3D();
}
```

The builder for the copy command is extended to support copy regions:

```java
public static class Builder {
    ...
    private final List<CopyRegion> regions = new ArrayList<>();

    public Builder region(CopyRegion region) {
        regions.add(region);
        return this;
    }
}
```

We also add a convenience variant to add a copy region for an entire image:

```java
public Builder region(Image image) {
    CopyRegion whole = CopyRegion.of(image.descriptor());
    return region(whole);
}
```

Which uses the following factory method:

```java
public static CopyRegion of(ImageDescriptor descriptor) {
    return new CopyRegion.Builder()
        .subresource(descriptor)
        .extents(descriptor.extents())
        .build();
}
```

### Integration

TODO - try with chalet image first
then mip maps
then cubemap layers
???

show KTX commands

show loading times comparisons





Which is copied to the staging buffer:

```java
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, image.data(0));
```

The copy command is moved outside the `for` loop:

```java
var copy = new ImageCopyCommand.Builder()
    .image(texture)
    .buffer(staging)
    .layout(VkImageLayout.TRANSFER_DST_OPTIMAL);
```

Which is refactored to specify a copy region for each image in the cubemap:

```java
for(int n = 0; n < Image.CUBEMAP_ARRAY_LAYERS; ++n) {
    SubResource res = new SubResource.Builder(descriptor)
        .baseArrayLayer(n)
        .build();

    CopyRegion region = new CopyRegion.Builder()
        .offset(image.offset(n))
        .extents(texture.descriptor().extents())
        .subresource(res)
        .build();

    copy.region(region);
}
```

Note that the copy region uses the `offset` helper we implemented earlier to offset into the compound image array.

Finally the copy command is invoked and we release the staged image:

```java
copy.build().submitAndWait(graphics);
staging.destroy();
```

The remainder of the method is the same as the previous version.

This implementation improves the loading times by about 200% on our development environment.  Not bad.

---

## Summary

In this chapter we added a skybox which involved implementation of the following:

* The rasterizer pipeline stage.

* Extension of the image class to support array layers and MIP levels.

* Implementation of the KTX loader (for uncompressed images).

* The addition of regions to the image copy command.

