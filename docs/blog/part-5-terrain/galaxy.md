---
title: Galaxy Demo
---

---

## Contents

TODO

- [Overview](#overview)
- [Galaxy Model](#galaxy-model)
- [Push Constants](#push-constants)
- [Rendering](#rendering)

---

## Overview

In this chapter we will create a new project to render a model of a galaxy.

Rather than attempting to generate a convincing galaxy from first principles (which would be a project in itself) the model will be derived from an image.  The general approach is:

1. Load a galaxy image.

1. Scale the image down to a manageable size.

1. Create a flat grid of points corresponding to the scaled image.

1. Select the vertex colour of each point from the image.

1. Discard those where the colour is lower than some threshold, i.e. ignore black or very faint 'stars'.

1. Randomly set the vertical position of each vertex to simulate the _thin disc_ and central _bulge_ of the galaxy.

The approach for rendering the resultant model is:

* Transform each point to a _billboard_ (i.e. a flat quad facing the camera).

* Render each 'star' as a translucent disc with the colour fading with distance from the centre of the quad.

Therefore the following new components will be required for this demo:

* Support for the _geometry_ shader.

* Implementation of the _colour blending_ pipeline stage to mix the colour of overlapping vertices.

Several further improvements will also be introduced during the course of this chapter.

---

## Galaxy Model

summary of project? orbital controller
galaxy builder
show image


```java
public Model build(InputStream in) throws IOException {
    var builder = new DefaultModel.Builder()
        .primitive(Primitive.POINTS)
        .layout(Point.LAYOUT)
        .layout(Colour.LAYOUT);

    ...
    
    return builder.build();
}
```

For the moment the galaxy image is loaded and scaled down using the AWT image library:

```java
BufferedImage image = ImageIO.read(in);
BufferedImage scaled = new BufferedImage(size, size, image.getType());
Graphics2D g = scaled.createGraphics();
g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
g.drawImage(image, 0, 0, size, size, null);
g.dispose();
```

Next the image is iterated to lookup the colour of each pixel:

```java
Raster raster = scaled.getData();
float[] pixel = new float[3];
for(int x = 0; x < size; ++x) {
    for(int y = 0; y < size; ++y) {
        raster.getPixel(x, y, pixel);
        ...
    }
}
```

Pixels that are darker than a configured `threshold` are discarded:

```java
if(pixel[0] + pixel[1] + pixel[2] < threshold) {
    continue;
}
```

Next the vertex position is determine from the pixel coordinates:

```java
float px = scale(x);
float py = scale(y);
```

Where `scale` calculates a scaled coordinate about the model origin:

```java
private float scale(float pos) {
    return (pos / size - MathsUtil.HALF) * scale;
}
```

To generate a 3D galaxy model the distance __squared__ from the centre of the galaxy is calculated and the Z coordinate is randomised:

```java
float dist = px * px + py * py;
float range = dist < bulge ? bulge - dist : disc;
float z = random.nextFloat(-range, +range);
Point pos = new Point(px, py, z);
```

Where `bulge` is the radius of the central bulge, such that `bulge - dist` creates a poor mans spherical distribution.

Next the pixel colour is scaled to percentile values:

```java
for(int n = 0; n < 3; ++n) {
    pixel[n] = pixel[n] / 0xFF;
}
Colour col = Colour.of(pixel);
```

And finally the vertex is added to the model:

```java
Vertex vertex = Vertex.of(pos, col);
builder.add(vertex);
```

---

## Push Constants

### Introduction

In the vertex shader the vertices are transformed by a matrix implemented as a _push constant_ rather than a uniform buffer:

```glsl
#version 450

layout(location=0) in vec3 inPosition;
layout(location=1) in vec4 inColour;

layout(push_constant) uniform ModelViewMatrix {
    mat4 modelview;
};

layout(location=0) out vec4 outColour;

void main() {
    gl_PointSize = 1;
    gl_Position = modelview * vec4(pos, 1.0);
    outColour = inColour;
}
```

Push constants are an alternative and more efficient mechanism for transferring arbitrary data to shaders with some constraints:

* The maximum amount of data is usually relatively small.

* Push constants are updated and stored within the command buffer.

* Alignment restrictions on the size and offset of each element.

See [vkCmdPushConstants](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/vkCmdPushConstants.html).

Note that the vertex shader does not use the projection matrix as this will now be a function of the geometry shader (see below).

### Push Constant Range

We start with a _push constant range_ which specifies a portion of the push constants and the shader stages where that data can be used:

```java
public record PushConstantRange(int offset, int size, Set<VkShaderStage> stages) {
    public int length() {
        return offset + size;
    }

    void populate(VkPushConstantRange range) {
        range.stageFlags = IntegerEnumeration.reduce(stages);
        range.size = size;
        range.offset = offset;
    }
}
```

The _offset_ and _size_ of a push constant range must be a multiple of four bytes which is validated in the constructor:

```java
public PushConstantRange {
    ...
    validate(offset);
    validate(size);
}

static void validate(int size) {
    if((size % 4) != 0) throw new IllegalArgumentException(...);
}
```

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
    info.pPushConstantRanges = StructureHelper.pointer(ranges, VkPushConstantRange::new, PushConstantRange::populate);
    ...
}
```

Note that multiple ranges can be specified which allows the application to update some or all of the push constants at different shader stages and also enables the hardware to perform optimisations.

In the `build` method for the pipeline layout we determine the _maximum_ length of the push ranges:

```java
int max = ranges
    .stream()
    .mapToInt(PushConstantRange::length)
    .max()
    .orElse(0);
```

This is validated (not shown) against the hardware limit specified by the `maxPushConstantsSize` of the `VkPhysicalDeviceLimits` structure, this value is usually quite small (256 bytes on our development environment).

Finally the set of shader stages is aggregated and added to the pipeline layout:

```java
Set<VkShaderStage> stages = ranges
    .stream()
    .map(PushConstantRange::stages)
    .flatMap(Set::stream)
    .collect(toSet());

return new PipelineLayout(layout.getValue(), dev, max, stages);
```

### Update Command

Push constants are backed by a data buffer which is updated using a new command:

```java
public class PushConstantUpdateCommand implements Command {
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
    
    public PushConstantUpdateCommand build(PipelineLayout layout) {
        return new PushConstantUpdateCommand(layout, offset, data, stages);
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

Finally a convenience factory method is added to create a backing buffer appropriate to the pipeline layout:

```java
public static ByteBuffer data(PipelineLayout layout) {
    return BufferHelper.allocate(layout.max());
}
```

And a second command factory to update the entire buffer:

```java
public static PushConstantUpdateCommand of(PipelineLayout layout) {
    ByteBuffer data = data(layout);
    return new PushConstantUpdateCommand(layout, 0, data, layout.stages());
}
```

### Buffer Helper

The `BufferHelper` is a new utility class for NIO buffers which is introduced first.
Most Vulkan functionality that is dependant on a data buffer assumes a __direct__ NIO buffer, which is wrapped into the following convenience factory method:

```java
public final class BufferHelper {
    /**
     * Native byte order for a bufferable object.
     */
    public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

    private BufferHelper() {
    }

    public static ByteBuffer allocate(int len) {
        return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
    }
}
```

The utility class also supports conversion of an NIO buffer to a byte array:

```java
public static byte[] array(ByteBuffer bb) {
    if(bb.isDirect()) {
        bb.rewind();
        int len = bb.limit();
        byte[] bytes = new byte[len];
        for(int n = 0; n < len; ++n) {
            bytes[n] = bb.get();
        }
        return bytes;
    }
    else {
        return bb.array();
    }
}
```

And the reverse operation to wrap an array with a buffer:

```java
public static ByteBuffer buffer(byte[] array) {
    ByteBuffer bb = allocate(array.length);
    write(array, bb);
    return bb;
}
```

Where `write` is a local helper method:

```java
private static void write(byte[] array, ByteBuffer bb) {
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

Note that direct NIO buffers generally do not support the optional bulk methods, hence the `isDirect` test in the `write` method.

Existing code that converts arrays to/from byte buffers is refactored using the new utility methods, e.g. shader SPIV code.

### Integration

To use the push constants in the demo application the uniform buffer is first replaced with the following layout declaration in the vertex shader:

```glsl
layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};
```

In the pipeline configuration we remove the uniform buffer and replace it with a single push constant range sized to the three matrices:

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

In the camera configuration the push constants are updated once per frame:

```java
@Bean
public Task matrix(PushUpdateCommand update) {
    ByteBuffer data = update.data();
    return () -> {
        data.rewind();
        Matrix.IDENTITY.buffer(data);
        cam.matrix().buffer(data);
        projection.buffer(data);
    };
}
```

Finally the update command is added to the render sequence before starting the render pass.

---

## Rendering

### Geometry Shader

The role of the geometry shader in this demo is to transform each point in the model to a billboard quad.

This is declared in the shader layout:

```glsl
layout(points) in;
layout(triangle_strip, max_vertices=4) out;
```

The shader retrieves the vertex position and generates the billboard vertices:

```glsl
void main() {
    vec4 pos = gl_in[0].gl_Position;
    vertex(pos, -1, +1);
    vertex(pos, -1, -1);
    vertex(pos, +1, +1);
    vertex(pos, +1, -1);
    EndPrimitive();
}
```

Which uses the following helper method:

```glsl
layout(binding=0) uniform Projection {
    mat4 projection;
};

const float SIZE = 0.025;

void vertex(vec4 pos, float x, float y) {
    gl_Position = projection * (pos + vec4(x * SIZE, y * SIZE, 0, 0));
    outCoords = vec2(x, y);
    outColour = inColour[0];
    EmitVertex();
}
```

Note:

* The incoming colour has to be defined as an array, in this case with a single element used for all generated vertices.

* The geometry shader only applies perspective projection since the vertex shader has already transformed the model into view space, i.e. the billboard faces the screen.

* The projection matrix remains as a uniform buffer since it is not a volatile object, and push constants are a limited resource.

The final geometry shader is as follows:

```glsl
#version 450

layout(binding=0) uniform Projection {
    mat4 projection;
};

layout(points) in;

layout(triangle_strip, max_vertices=4) out;

layout(location=0) in vec4[] inColour;

layout(location=0) out vec2 outCoords;
layout(location=1) out vec4 outColour;

const float SIZE = 0.025;

void vertex(vec4 pos, float x, float y) {
    gl_Position = projection * (pos + vec4(x * SIZE, y * SIZE, 0, 0));
    outCoords = vec2(x, y);
    outColour = inColour[0];
    EmitVertex();
}

void main() {
    vec4 pos = gl_in[0].gl_Position;
    vertex(pos, -1, +1);
    vertex(pos, -1, -1);
    vertex(pos, +1, +1);
    vertex(pos, +1, -1);
    EndPrimitive();
}
```

### Specialisation Constants

The `layout(constant_id=1) const float SIZE = 0.025`


The terrain shaders contain a number of hard coded parameters (such as the tesselation factor and height scalar) which would ideally be programatically configured (possibly from a properties file).

Additionally in general we prefer to centralise common or shared parameters to avoid hard-coding the same information in multiple locations or having to replicate shaders for different parameters.

Vulkan provides _specialisation constants_ for these requirements which parameterise a shader when it is instantiated.

For example in the evaluation shader the hard-coded height scale is replaced with the following constant declaration:

```glsl
layout(constant_id=1) const float HeightScale = 2.5;
```

Note that the constant also has a default value if it is not explicitly configured by the application.

The descriptor for a set of specialisation constants is constructed via a new factory method:

```java
public static VkSpecializationInfo constants(Map<Integer, Object> constants) {
    // Skip if empty
    if(constants.isEmpty()) {
        return null;
    }
    ...
}
```

Each constant generates a separate child descriptor:

```java
var info = new VkSpecializationInfo();
info.mapEntryCount = constants.size();
info.pMapEntries = StructureHelper.pointer(constants.entrySet(), VkSpecializationMapEntry::new, populate);
```

Where `populate` is factored out to the following function:

```java
var populate = new BiConsumer<Entry<Integer, Object>, VkSpecializationMapEntry>() {
    private int len = 0;

    @Override
    public void accept(...) {
        ...
    }
};
```

This function populates the descriptor for each entry and calculates the buffer offset and total length as a side-effect:

```java
// Init constant
int size = size(entry.getValue());
out.size = size;
out.constantID = entry.getKey();

// Update buffer offset
out.offset = len;
len += size;
```

Where `size` is a local helper:

```java
int size() {
    return switch(value) {
        case Integer n -> Integer.BYTES;
        case Float f -> Float.BYTES;
        case Boolean b -> Integer.BYTES;
        default -> throw new UnsupportedOperationException();
    };
}
```

The constants are then written to a data buffer:

```java
ByteBuffer buffer = BufferHelper.allocate(populate.len);
for(Object value : constants.values()) {
    switch(value) {
        case Integer n -> buffer.putInt(n);
        case Float f -> buffer.putFloat(f);
        case Boolean b -> {
            int bool = VulkanBoolean.of(b).toInteger();
            buffer.putInt(bool);
        }
        default -> throw new RuntimeException();
    }
}
```

And finally this data is added to the descriptor:

```java
info.dataSize = populate.len;
info.pData = buffer;
```

Notes:

* Only scalar and boolean values are supported.

* Booleans are represented as integer values.

Specialisation constants are configured in the pipeline:

```java
public class ShaderStageBuilder {
    private VkSpecializationInfo constants;
    
    ...
    
    public ShaderStageBuilder constants(VkSpecializationInfo constants) {
        this.constants = notNull(constants);
        return this;
    }
 
    void populate(VkPipelineShaderStageCreateInfo info) {
        ...
        info.pSpecializationInfo = constants;
    }
}
```

The set of constants used in both tesselation shaders is initialised in the pipeline configuration class:

```java
class PipelineConfiguration {
    private final VkSpecializationInfo constants = Shader.constants(Map.of(0, 20f, 1, 2.5f));
}
```

Finally the relevant shaders are parameterised when the pipeline is constructed, for example:

```java
shader(VkShaderStage.TESSELLATION_EVALUATION)
    .shader(evaluation)
    .constants(constants)
    .build()
```

### Colour Blending

The fragment shader generates a circle from the quad:

```glsl
#version 450

layout(location=0) in vec2 inCoord;
layout(location=1) in vec4 inColour;

layout(location=0) out vec4 outColour;

layout(constant_id=2) const float RADIUS = 0.2;

void main() {
    float alpha = 1 - dot(inCoord, inCoord);
    if(alpha < RADIUS) {
        discard;
    }
    outColour = vec4(alpha);
}
```

To enable colour blending in the demo a new pipeline stage is implemented:

```java
public class ColourBlendPipelineStageBuilder extends AbstractPipelineStageBuilder<VkPipelineColorBlendStateCreateInfo> {
    private final VkPipelineColorBlendStateCreateInfo info = new VkPipelineColorBlendStateCreateInfo();

    public ColourBlendPipelineStageBuilder() {
        info.logicOpEnable = VulkanBoolean.FALSE;
        info.logicOp = VkLogicOp.COPY;
        Arrays.fill(info.blendConstants, 1);
    }
}
```

The global blending properties are configured as follows:

```java
public ColourBlendPipelineStageBuilder enable(boolean enabled) {
    info.logicOpEnable = VulkanBoolean.of(enabled);
    return this;
}

public ColourBlendPipelineStageBuilder operation(VkLogicOp op) {
    info.logicOp = notNull(op);
    return this;
}

public ColourBlendPipelineStageBuilder constants(float[] constants) {
    System.arraycopy(constants, 0, info.blendConstants, 0, constants.length);
    return this;
}
```

The blending configuration for each colour attachment is implemented as a nested builder:

```java
public AttachmentBuilder attachment() {
    return new AttachmentBuilder();
}
```

Which specifies the logical operation between a fragment and the existing colour in the framebuffer attachment(s).

```java
public class AttachmentBuilder {
    private boolean enabled = true;
    private int mask = IntegerEnumeration.reduce(VkColorComponent.values());
    private final BlendOperationBuilder colour = new BlendOperationBuilder();
    private final BlendOperationBuilder alpha = new BlendOperationBuilder();

    private AttachmentBuilder() {
        colour.source(VkBlendFactor.SRC_ALPHA);
        colour.destination(VkBlendFactor.ONE_MINUS_SRC_ALPHA);
        alpha.source(VkBlendFactor.ONE);
        alpha.destination(VkBlendFactor.ZERO);
        attachments.add(this);
    }
}
```

The colour `mask` is used to specify which channels are subject to the blend operation, expressed as a simple string:

```java
public AttachmentBuilder mask(String mask) {
    this.mask = mask
        .chars()
        .mapToObj(Character::toString)
        .map(VkColorComponent::valueOf)
        .collect(collectingAndThen(toList(), IntegerEnumeration::reduce));

    return this;
}
```

The properties for the colour and alpha components are identical so a further nested builder configures both values:

```java
public class BlendOperationBuilder {
    private VkBlendFactor src;
    private VkBlendFactor dest;
    private VkBlendOp blend = VkBlendOp.ADD;

    private BlendOperationBuilder() {
    }

    public AttachmentBuilder build() {
        return AttachmentBuilder.this;
    }
}
```

The descriptor for the attachment is generated in the nested builder as follows:

```java
private void populate(VkPipelineColorBlendAttachmentState info) {
    // Init descriptor
    info.blendEnable = VulkanBoolean.of(enabled);
    info.colorWriteMask = mask;

    // Init colour blending operation
    info.srcColorBlendFactor = colour.src;
    info.dstColorBlendFactor = colour.dest;
    info.colorBlendOp = colour.blend;

    // Init alpha blending operation
    info.srcAlphaBlendFactor = alpha.src;
    info.dstAlphaBlendFactor = alpha.dest;
    info.alphaBlendOp = alpha.blend;
}
```

And finally the descriptor for the pipeline stage can be constructed:

```java
@Override
VkPipelineColorBlendStateCreateInfo get() {
    // Init default attachment if none specified
    if(attachments.isEmpty()) {
        new AttachmentBuilder().build();
    }

    // Add attachment descriptors
    info.attachmentCount = attachments.size();
    info.pAttachments = StructureHelper.pointer(attachments, VkPipelineColorBlendAttachmentState::new, AttachmentBuilder::populate);

    return info;
}
```

Note that by convenience a single, default attachment is added if none are configured (at least one must be specified).

### Integration

The pipeline configuration can now be updated to apply an additive blending operation in the demo:

```java
.blend()
    .enable(true)
    .operation(VkLogicOp.COPY)
    .attachment()
        .colour()
            .destination(VkBlendFactor.ONE)
            .build()
        .build()
    .get();
```

---

## Further Improvements

### Device Limits

The `VkPhysicalDeviceLimits` structure specifies various limits supported by the hardware, this is wrapped by a new helper class:

```java
public class DeviceLimits {
    private final VkPhysicalDeviceLimits limits;
    private final DeviceFeatures features;
}
```

For convenience the supported device features are also incorporated into this new class and can be enforced as required:

```java
public void require(String name) {
    if(!features.features().contains(name)) {
        throw new IllegalStateException("Feature not supported: " + name);
    }
}
```

A device limit can be queried from the structure by name:

```java
public <T> T value(String name) {
    return (T) limits.readField(name);
}
```

The reason for implementing limits by name is two-fold:

1. The `readField` approach avoids the problem of the underlying JNA structure being totally mutable.

2. It is assumed that applications will prefer to query by name rather than coding for specific structure fields.

Some device limits are a _quantised_ range of permissible values which can be retrieved by the following helper:

```java
public float[] range(String name, String granularity) {
    // Lookup range bounds
    float[] bounds = value(name);
    float min = bounds[0];
    float max = bounds[1];

    // Lookup granularity step
    float step = value(granularity);

    // Determine number of values
    int num = (int) ((max - min) / step);

    // Build quantised range
    float[] range = new float[num + 1];
    for(int n = 0; n < num; ++n) {
        range[n] = min + n * step;
    }
    range[num] = max;

    return range;
}
```

Where _name_ is the limit and _granularity_ specifies the step size, e.g. `pointSizeRange` and `pointSizeGranularity` for the range of valid point primitives.

The device limits are lazily retrieved from the logical device:

```java
public class LogicalDevice ... {
    ...
    private final Supplier<DeviceLimits> limits = new LazySupplier<>(this::loadLimits);

    private DeviceLimits loadLimits() {
        VkPhysicalDeviceProperties props = parent.properties();
        return new DeviceLimits(props.limits, features);
    }

    @Override
    public DeviceLimits limits() {
        return limits.get();
    }
}
```

For example, the builder for an indirect draw command can now validate that the command configuration is supported by the hardware:

```java
DeviceLimits limits = buffer.device().limits();
int max = limits.value("maxDrawIndirectCount");
limits.require("multiDrawIndirect");
if(count > max) throw new IllegalArgumentException(...);
```

Although the validation layer would also trap this problem when the command is _executed_ the above code applies the validation at _instantiation_ time (which may be earlier).

---

## Summary

In this chapter we rendered a galaxy model and introduced:

* The geometry shader.

* The colour blending pipeline stage.

* Push constants.

* Shader specialisation constants.

* Support for device limits.
