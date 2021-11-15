---
title: Skybox
---

## Overview

In this chapter we will add a _skybox_ to the demo application.

The skybox will be implemented as a cube centred on the camera rendered with a cubemap texture.

Initially each image will be loaded and copied separately to the cubemap texture, we will then implement a compound _image array_ to improve the performance of the loading process.

TODO

---

## Skybox

### Pipeline Configuration

The skybox requires new shaders and a custom pipeline configuration for rendering.

We start with the pipeline builder for the rasterizer stage which is essentially a wrapper for the underlying descriptor:

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

Next we create a new configuration class for the skybox pipeline:

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

* Front faces are culled since the camera is _inside_ the skybox.

* The new pipeline shares the existing pipeline layout.

Next we create a cube model for the skybox:

```java
@Bean
public static Model skybox() {
    return new CubeBuilder()
        .build()
        .transform(List.of(Point.LAYOUT));
}
```

Note that we only need the vertex positions in the skybox shader (see below).

Finally a cubemap sampler is created which clamps texture coordinates:

```java
@Bean
public Sampler cubeSampler() {
    return new Sampler.Builder(dev)
        .wrap(Wrap.EDGE, false)
        .build();
}
```

### Shaders

The vertex shader for the skybox is as follows:

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

* The uniform buffer is now comprised of the three matrices as separate elements.

* The vertex position is first transformed by the rotation component of the view matrix (extracted using the built in `mat3` GLSL method) since the skybox is centred on the camera.

* The projection transformation sets the last two components of the position to be same ensuring that the vertex lies on the far clipping plane.

* The texture coordinate is set to the vertex position (we could have used the same technique in the rotating cube demo).

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

### Uniform Buffer

To support the new uniform buffer structure we modify the update method in the camera configuration:

```java
public Task matrix(VulkanBuffer uniform) {
    ...
    ByteBuffer bb = uniform.memory().map().buffer();

    return () -> {
        bb.rewind();
        model.buffer(bb);
        cam.matrix().buffer(bb);
        projection.buffer(bb);
    };
}
```

Notes:

* The uniform buffer is mapped once and the resultant byte buffer is rewound before updates.

* The projection matrix is updated on every frame but could be done just once (since it never changes).

* The existing vertex shader for the chalet model is refactored accordingly.

### Loader

The code to load the cubemap texture roughly follows the same pattern as the chalet texture with the following differences.

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

For some reason configuring the image as a cubemap is specified by a creation flag:

```java
public Builder cubemap() {
    return flag(VkImageCreateFlag.CUBE_COMPATIBLE);
}
```

We next iterate over the array of filenames to load each image:

```java
var loader = new ResourceLoaderAdapter<>(src, new NativeImageLoader());
String[] filenames = {"posx", "negx", ...};
for(int n = 0; n < 6; ++n) {
    ImageData image = loader.load(filenames[n] + ".jpg");
    ...
}
```

And perform a separate copy for each image:

```java
new ImageCopyCommand.Builder()
    .image(texture)
    .buffer(staging)
    .layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
    .subresource(res)
    .build()
    .submitAndWait(graphics);
```

Where the sub-resource specifies the array layer for each image:

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

* The descriptor pool size is doubled accordingly.

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

There are a couple of issues with the demo as it stands which we will address next:

1. The process of loading the cubemap texture is quite slow since we load and copy each image separately. 

2. The code to build the render sequence is a mess due to the large number of dependencies (pipelines, descriptor sets, vertex buffers, etc).

---

## Compound Image

### Image Arrays

Rather than loading each image separately via the `NativeImageLoader` we would prefer a single object and buffer offsets to employ more efficient bulk transfer operations.  We will introduce an _image array_ comprised of a list of JOVE images.

To support an image array we first refactor the existing image abstraction:

```java
public record ImageData(Dimensions size, int count, int mip, Layout layout, Bufferable data)
```

The _count_ member is the number of images in the array (or one for a single image).

We also add a placeholder for MIP levels which will be implemented in a future chapter (for now we assume one MIP level).

To create an image array we add a factory method:

```java
public static ImageData array(List<ImageData> images) {
    int count = images.size();
    if(count == 0) throw new IllegalArgumentException(...);
    ...
}
```

First the array of images is validated (not shown) to ensure each image has the same properties (dimensions, layout, etc):

```java
// Init header
ImageData header = images.get(0);
int len = header.data().length() * count;

// Validates images
images
    .stream()
    .skip(1)
    .forEach(e -> validate(header, e));
```

Next the images are transformed to a compound list of bufferable objects:

```java
List<Bufferable> compound = images
    .stream()
    .map(ImageData::data)
    .collect(toList());
```

And wrapped by a local implementation:

```java
Bufferable data = new Bufferable() {
    @Override
    public int length() {
        return len;
    }

    @Override
    public void buffer(ByteBuffer bb) {
        for(Bufferable b : compound) {
            b.buffer(bb);
        }
    }
};
```

Finally the image array is created from the compound data via a private copy constructor:

```java
return new ImageData(header, count, data);
```

We also add a helper that calculates the buffer offset for a given image array element:

```java
public int offset(int index) {
    return index * size.area() * layout.count() * layout.bytes();
}
```

### Loader

To persist an image array we will create a second image loader implementation using a custom data format (based on the model loader).

We first define a new resource loader abstraction for a custom file format based on data streams:

```java
public interface DataResourceLoader<T> extends ResourceLoader<DataInputStream, T> {
    @Override
    default DataInputStream map(InputStream in) throws IOException {
        return new DataInputStream(in);
    }

    /**
     * Writes the given data object.
     * @param data      Data to write
     * @param out       Output stream
     * @throws IOException if the data cannot be written
     */
    void save(T data, DataOutputStream out) throws IOException;
}
```

Next we factor out the various helper methods from the model loader into a new `DataHelper` utility class as we will be reusing them for persisting the image array.

The new loader is relatively straight forward:

```java
public class ImageLoader implements DataResourceLoader<ImageData> {
    private final DataHelper helper = new DataHelper();

    @Override
    public ImageData load(DataInputStream in) throws IOException {
        ...
    }

    @Override
    public void save(ImageData image, DataOutputStream out) throws IOException {
        // Write image image header
        helper.writeVersion(out);
        out.writeInt(image.count());
        out.writeInt(image.mip());

        // Write dimensions
        final Dimensions size = image.size();
        out.writeInt(size.width());
        out.writeInt(size.height());

        // Write layout
        helper.write(image.layout(), out);

        // Write image data
        helper.write(image.data(), out);
    }
}
```

### Copy Region

In the previous implementation we used a staging buffer and a copy command for _each_ image.  To support the cubemap image we will extend the copy command to support _copy regions_ such that each element of the image array can be transferred to the corresponding array layer in the cubemap texture.

First a copy region is defined as a simple transient record with a companion builder:

```java
public class ImageCopyCommand implements Command {
    ...
    public record CopyRegion(long offset, int length, int height, SubResource res, VkOffset3D imageOffset, ImageExtents extents)
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

We first temporarily bodge the cubemap loader method to create and output the image array:

```java
ImageData[] images = new ImageData[6];
var loader = new ResourceLoaderAdapter<>(src, new NativeImageLoader());
for(int n = 0; n < filenames.length; ++n) {
    images[n] = loader.load(filenames[n] + ".jpg");
}
ImageLoader loader = new ImageLoader();
loader.save(array, new DataOutputStream(...));
```

This code is then removed and instead we load the persisted image array:

```java
public View cubemap(...) {
    var loader = new ResourceLoaderAdapter<>(src, new ImageLoader());
    ImageData image = loader.load("skybox.image");
    if(image.count() != Image.CUBEMAP_ARRAY_LAYERS) throw new IllegalArgumentException(...);
    ...
}
```

Which is then copied to the staging buffer:

```
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, image.data());
```

The copy command builder is moved outside the `for` loop:

```java
var copy = new ImageCopyCommand.Builder()
    .image(texture)
    .buffer(staging)
    .layout(VkImageLayout.TRANSFER_DST_OPTIMAL);
```

And is refactored to specify a copy region for each image in the cubemap:

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

## Scene Graph

TODO

---

## Summary

In this chapter we added a skybox which involved implementing the following:

* Expanding the image class to support array layers.

* Implementation of a new custom file format and loader to persist an image array.

* The addition of regions to the image copy command.

* The rasterizer pipeline stage.

