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

## Compound Image

### Image Arrays

The process of loading the cubemap texture is quite slow since we load and copy each image separately.

Therefore we introduce an _image array_ which is a single chunk of data employing more efficient bulk transfer operations.

To support an image array we first refactor the existing image abstraction:

```java
public record ImageData(Dimensions size, int count, int mip, Layout layout, Bufferable data)
```

The _count_ member is the number of images in the array (or one for a single image).

We also add a placeholder for MIP levels which will be implemented in a future chapter (for now we assume one MIP level per image).

To create an image array we add a factory method:

```java
public static ImageData array(List<ImageData> images) {
    int count = images.size();
    if(count == 0) throw new IllegalArgumentException(...);
    
    ...
    
    return new ImageData(header, count, data);
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
        ...
    }
}
```

The custom file format is illustrated by the output method:

```java
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

To generate the persistent image array we first temporarily bodge the cubemap loader method to create and output the image array from the separate images:

```java
ImageData[] images = new ImageData[6];
var loader = new ResourceLoaderAdapter<>(src, new NativeImageLoader());
for(int n = 0; n < filenames.length; ++n) {
    images[n] = loader.load(filenames[n] + ".jpg");
}
ImageLoader loader = new ImageLoader();
loader.save(array, new DataOutputStream(...));
```

This code is then removed and instead we load the persisted compound image:

```java
public View cubemap(...) {
    var loader = new ResourceLoaderAdapter<>(src, new ImageLoader());
    ImageData image = loader.load("skybox.image");
    if(image.count() != Image.CUBEMAP_ARRAY_LAYERS) throw new IllegalArgumentException(...);
    ...
}
```

Which is copied to the staging buffer:

```java
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, image.data());
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

## Push Constants

### Overview

We will next introduce _push constants_ as an alternative (and more efficient) means of updating the view matrices.

Push constants are used to send data to shaders with the following constraints:

* The amount of data is usually relatively small (specified by the `maxPushConstantsSize` of the `VkPhysicalDeviceLimits` structure).

* Push constants are updated and stored within the command buffer itself.

* Push constants have alignment restrictions, see [vkCmdPushConstants](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/vkCmdPushConstants.html).

### Push Constant Range

We start with a _push constant range_ which specifies a portion of the push constants and the shaders stages where that data is used:

```java
public record PushConstantRange(int offset, int size, Set<VkShaderStage> stages) {
    public int length() {
        return offset + size;
    }

    void populate(VkPushConstantRange range) {
        range.stageFlags = IntegerEnumeration.mask(stages);
        range.size = size;
        range.offset = offset;
    }
}
```

The _offset_ and _size_ of a push constant range must be a multiply of four bytes which we validate in the constructor:

```java
public PushConstantRange {
    Check.zeroOrMore(offset);
    Check.oneOrMore(size);
    Check.notEmpty(stages);
    stages = Set.copyOf(stages);
    validate(offset);
    validate(size);
}

static void validate(int size) {
    if((size % 4) != 0) throw new IllegalArgumentException(...);
}
```

Note that the constructor copies the set of pipeline stages and rewrites the constructor argument.  This is the standard approach for ensuring that a record class is immutable (although unfortunately this is currently flagged as a warning in our IDE).

The builder for the pipeline layout is modified to include a list of push constant ranges:

```java
public static class Builder {
    ...
    private final List<PushConstantRange> ranges = new ArrayList<>();

    public Builder add(PushConstantRange range) {
        ranges.add(range);
        return this;
    }
}
```

The ranges are populated in the usual manner:

```java
public PipelineLayout build(DeviceContext dev) {
    ...
    info.pushConstantRangeCount = ranges.size();
    info.pPushConstantRanges = StructureHelper.first(ranges, VkPushConstantRange::new, PushConstantRange::populate);
    ...
}
```

Note that multiple ranges can be specified:

1. This enabled the application to update some or all of the push constants at different shader stages.

2. And also allows the hardware to perform optimisations.

In the `build` method we also determine the overall size of the push constants and associated shaders stages (which are added to the pipeline layout constructor):

```java
// Determine overall size of the push constants data
int max = ranges
    .stream()
    .mapToInt(PushConstantRange::length)
    .max()
    .orElse(0);

// Enumerate pipeline stages
Set<VkShaderStage> stages = ranges
    .stream()
    .map(PushConstantRange::stages)
    .flatMap(Set::stream)
    .collect(toSet());

// Create layout
return new PipelineLayout(layout.getValue(), dev, max, stages);
```

These new properties are used to validate push constant update commands which are addressed next.

### Update Command

Push constants are backed by a data buffer and are updated using a new command:

```java
public class PushUpdateCommand implements Command {
    private final PipelineLayout layout;
    private final int offset;
    private final ByteBuffer data;
    private final int stages;

    @Override
    public void execute(VulkanLibrary lib, Buffer buffer) {
        data.rewind();
        lib.vkCmdPushConstants(buffer, layout, stages, offset, data.limit(), data);
    }
}
```

Notes:

* The data buffer is rewound before updates are applied, generally Vulkan seems to automatically rewind buffers as required (e.g. for updating the uniform buffer) but not in this case.

* The constructor applies validation (not shown) to verify alignments, buffer sizes, etc.

The new API method is added to the library for the pipeline layout:

```java
void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, int stageFlags, int offset, int size, ByteBuffer pValues);
```

The constructor is public but we also provide a builder:

```java
public static class Builder {
    private int offset;
    private ByteBuffer data;
    private final Set<VkShaderStage> stages = new HashSet<>();

    ...
    
    public PushUpdateCommand build(PipelineLayout layout) {
        return new PushUpdateCommand(layout, offset, data, stages);
    }
}
```

We provide builder methods to update all the push constants or an arbitrary _slice_ of the backing data buffer:

```java
public Builder data(ByteBuffer data, int offset, int size) {
    this.data = data.slice(offset, size);
    return this;
}
```

And the following convenience method to update a slice specified by a corresponding range:

```java
public Builder data(ByteBuffer data, PushConstantRange range) {
    return data(data, range.offset(), range.size());
}
```

Finally we add a convenience factory method to create a backing buffer appropriate to the pipeline layout:

```java
public static ByteBuffer data(PipelineLayout layout) {
    return BufferWrapper.allocate(layout.max());
}
```

And a second helper to update the entire push constants buffer:

```java
public static PushUpdateCommand of(PipelineLayout layout) {
    final ByteBuffer data = data(layout);
    return new PushUpdateCommand(layout, 0, data, layout.stages());
}
```

### Buffer Wrapper

As a further convenience for applying updates to push constants (or to uniform buffers) we introduce a wrapper for a data buffer:

```java
public class BufferWrapper {
    private final ByteBuffer bb;

    public BufferWrapper rewind() {
        bb.rewind();
        return this;
    }

    public BufferWrapper append(Bufferable data) {
        data.buffer(bb);
        return this;
    }
}
```

The following method provides a random access approach to insert data into the buffer:

```java
public BufferWrapper insert(int index, Bufferable data) {
    int pos = index * data.length();
    bb.position(pos);
    data.buffer(bb);
    return this;
}
```

This can be used to populate push constants or a uniform buffer that is essentially an array of some data type (used below).

In the same vein we add a new factory method to the bufferable class to wrap a JNA structure:

```java
static Bufferable of(Structure struct) {
    return new Bufferable() {
        @Override
        public int length() {
            return struct.size();
        }

        @Override
        public void buffer(ByteBuffer bb) {
            byte[] array = struct.getPointer().getByteArray(0, struct.size());
            BufferWrapper.write(array, bb);
        }
    };
}
```

This allows arbitrary JNA structures to be used to populate push constants or a uniform buffer which will become useful in later chapters.

The `write` method is a static helper on the new class:

```java
public static void write(byte[] array, ByteBuffer bb) {
    if(bb.isDirect()) {
        for(byte b : array) {
            bb.put(b);
        }
    }
    else {
        bb.put(array);
    }
}
```

Note that direct NIO buffers generally do not support the optional bulk methods.

Finally we also implement helpers to allocate and populate direct buffers:

```java
public class BufferWrapper {
    public static final ByteOrder ORDER = ByteOrder.nativeOrder();

    public static ByteBuffer allocate(int len) {
        return ByteBuffer.allocateDirect(len).order(ORDER);
    }

    public static ByteBuffer buffer(byte[] array) {
        ByteBuffer bb = allocate(array.length);
        write(array, bb);
        return bb;
    }
}
```

### Integration

First the uniform buffer is replaced with a push constants layout declaration in the vertex shaders:

```glsl
#version 450

layout(location = 0) in vec3 inPosition;

layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(location = 0) out vec3 outCoords;

void main() {
    vec3 pos = mat3(view) * inPosition;
    gl_Position = (projection * vec4(pos, 0.0)).xyzz;
    outCoords = inPosition;
}
```

In the pipeline configuration we remove the uniform buffer and replace it with a single push constants range sized to the three matrices:

```java
@Bean
PipelineLayout layout(DescriptorLayout layout) {
    int len = 3 * Matrix.IDENTITY.length();

    return new PipelineLayout.Builder()
        .add(layout)
        .add(new PushConstantRange(0, len, Set.of(VkShaderStage.VERTEX)))
        .build(dev);
}
```

Next we create a command to update the whole of the push constants buffer:

```java
@Bean
public static PushUpdateCommand update(PipelineLayout layout) {
    return PushUpdateCommand.of(layout);
}
```

In the camera configuration we use the new helper class to update the matrix data in the push constants:

```java
@Bean
public Task matrix(PushUpdateCommand update) {
    // Init model rotation
    Matrix model = ...

    // Add projection matrix
    BufferWrapper buffer = new BufferWrapper(update.data());
    buffer.insert(2, projection);

    // Update modelview matrix
    return () -> {
        buffer.rewind();
        buffer.append(model);
        buffer.append(cam.matrix());
    };
}
```

In the render sequence we perform the update before starting the render pass:

```java
begin()
add(update)
add(fb.begin())
    ...
add(FrameBuffer.END)
end();
```

Up to this point we have created and recorded the command buffers _once_ since both the scene and the render sequence are static.  However if one were to run the demo application as it stands nothing will be rendered because the push constants are updated in the command buffer itself.  We therefore need to re-record the command sequence for _every_ frame to apply the updates.

In any case a real-world application would generally be rendering a dynamic scene (i.e. adding and removing geometry) requiring command buffers to be recorded prior to each frame (often as a separate multi-threaded activity).

For the moment we will record the render sequence on demand.  We first factor out the code that allocates and record the command buffer to a separate, temporary component:

```java
@Component
class Sequence {
    @Autowired private Command.Pool graphics;
    @Autowired private List<FrameBuffer> buffers;
    @Autowired private PushUpdateCommand update;
    ...

    private List<Command.Buffer> commands;

    @PostConstruct
    void init() {
        commands = graphics.allocate(2);
    }
}
```

The `@PostConstruct` annotation specifies that the method is invoked by the container after the object has been instantiated, which we use to allocate the command buffers.

Next we add a factory method that selects and records a command buffer given the swapchain image index:

```java
public Command.Buffer get(int index) {
    Command.Buffer cmd = commands.get(index);
    if(cmd.isReady()) {
        cmd.reset();
    }
    record(cmd, index);
    return cmd;
}
```

Where `record` wraps up the existing render sequence code.

Note that for the purpose of progressing the demo application we are reusing the command buffers (as opposed to allocating new instances for example).  This requires a command buffer to be `reset` before it can be re-recorded, which entails the following temporary change to the command pool:

```java
return Pool.create(dev, queue, VkCommandPoolCreateFlag.RESET_COMMAND_BUFFER);
```

In general using this approach is discouraged in favour of resetting the entire command pool and/or implementing some sort of caching strategy.

The demo application should now run as before using push constants to update the view matrices.

However during the course of this chapter we have introduced several new problems:

* The temporary code to manage the command buffers is very ugly and requires individual buffers to be programatically reset.

* The code to record the command buffers is a mess due to the large number of dependencies.

* We still have multiple arrays of frame buffers, descriptor sets and command buffers.

These issues are all related and will be addressed in the next chapter before we add any further objects to the scene.

---

## Summary

In this chapter we added a skybox which involved implementation of the following:

* The rasterizer pipeline stage.

* Extension of the image class to support array layers.

* Implementation of a new custom file format and loader to persist an image array.

* The addition of regions to the image copy command.

* Push constants

* The buffer wrapper helper class

