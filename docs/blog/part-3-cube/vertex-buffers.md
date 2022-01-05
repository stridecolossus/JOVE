---
title: Vertex Buffers
---

---

## Contents

- [Overview](#overview)
- [Vertex Data](#vertex-data)
- [Vertex Configuration](#vertex-configuration)
- [Integration](#integration)

---

## Overview

In this chapter we will replace the hard-coded triangle data in the shader with a _vertex buffer_ (also known as a _vertex buffer object_ or VBO).

We will then transfer the vertex data to a _device local_ buffer (i.e. memory on the GPU) for optimal performance.

This process involves the following steps:

1. Build the vertex data.

2. Convert it to an interleaved NIO buffer.

3. Allocate a _staging_ buffer (visible to the host).

4. Copy the interleaved data to this staging buffer.

5. Allocate a _device local_ buffer (visible only to the hardware).

6. Copy the staged data to this buffer.

7. Release the staging buffer.

We will need to implement the following:

* New domain classes to specify the vertex data.

* The vertex buffer object itself.

* A command to copy between buffers.

* The _vertex input stage_ of the pipeline to configure the structure of the vertex data.

---

## Vertex Data

### Vertex

We start with a definition for an object that can be written to an NIO buffer:

```java
public interface Bufferable {
    /**
     * Writes this object to the given buffer.
     * @param buffer Buffer
     */
    void buffer(ByteBuffer buffer);
    
    /**
     * @return Length of this object (bytes)
     */
    long length();
}
```

A _vertex_ is a compound bufferable comprised of an arbitrary array of _components_ such as positions and texture coordinates:

```java
public class Vertex {
    private Bufferable[] components;

    public Vertex(Bufferable... components) {
        this.components = notNull(components);
    }

    public void buffer(ByteBuffer buffer) {
        for(Bufferable obj : components) {
            obj.buffer(buffer);
        }
    }
}
```

Notes:

* The vertex class does not implement `Bufferable` but does provide the `buffer` method to output the vertex components to an NIO buffer.

* This implementation assumes vertex data is _interleaved_.

We can now implement the _vertex components_ needed to deliver the triangle demo.

In general a vertex is comprised of the following components:
- position
- normal
- texture coordinate
- colour

Positions and normal vectors are both floating-point tuples with common functionality, we implement a small hierarchy with the following base-class:

```java
public sealed class Tuple implements Bufferable permits Point, Vector {
    public static final int SIZE = 3;

    public final float x, y, z;

    protected Tuple(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public final long length() {
        return SIZE * Float.BYTES;
    }

    @Override
    public void buffer(ByteBuffer buffer) {
        buffer.putFloat(x).putFloat(y).putFloat(z);
    }
}
```

A vertex position is a point in 3D space:

```java
public final class Point extends Tuple {
    /**
     * Origin point.
     */
    public static final Point ORIGIN = new Point(0, 0, 0);

    public Point(float x, float y, float z) {
        super(x, y, z);
    }
}
```

A colour is defined as an RGBA record:

```java
public record Colour(float red, float green, float blue, float alpha) implements Bufferable {
    public static final Colour WHITE = new Colour(1, 1, 1, 1);
    public static final Colour BLACK = new Colour(0, 0, 0, 1);

    @Override
    public final long length() {
        return 4 * Float.BYTES;
    }

    @Override
    public void buffer(ByteBuffer buffer) {
        buffer.putFloat(red).putFloat(green).putFloat(blue).putFloat(alpha);
    }
}
```

Vectors and texture coordinates are not required for the triangle demo so we will gloss over these objects until later.

### Vertex Buffer

The outline class for the vertex buffer is as follows:

```java
public class VulkanBuffer extends AbstractVulkanObject {
    private final Set<VkBufferUsage> usage;
    private final DeviceMemory mem;
    private final long len;

    @Override
    protected Destructor<VulkanBuffer> destructor(VulkanLibrary lib) {
        return lib::vkDestroyBuffer;
    }
}
```

The buffer is bound to a pipeline using the following command factory:

```java
public Command bind() {
    require(VkBufferUsage.VERTEX_BUFFER);
    VulkanBuffer[] array = new VulkanBuffer[]{this};
    return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
}
```

The local `require` helper checks that the buffer supports a given operation (in this case that it is a vertex buffer that can be bound to a pipeline):

```java
public void require(VkBufferUsage... flags) {
    Collection<VkBufferUsage> required = Arrays.asList(flags);
    if(Collections.disjoint(required, usage)) {
        throw new IllegalStateException(...);
    }
}
```

Copying the vertex data from staging to a device-local buffer is also implemented as a command:

```java
public Command copy(VulkanBuffer dest) {
    // Validate
    if(len > dest.len) throw new IllegalStateException(...);
    require(VkBufferUsage.TRANSFER_SRC);
    dest.require(VkBufferUsage.TRANSFER_DST);

    // Build copy descriptor
    VkBufferCopy region = new VkBufferCopy();
    region.size = len;

    // Create copy command
    return (api, buffer) -> api.vkCmdCopyBuffer(buffer, this, dest, 1, new VkBufferCopy[]{region});
}
```

Finally the memory is released when the buffer is destroyed:

```java
@Override
protected void release() {
    if(!mem.isDestroyed()) {
        mem.destroy();
    }
}
```

### Buffer Creation

Creating a Vulkan buffer is comprised of the following steps:

1. Instantiate the buffer.

2. Retrieve the _memory requirements_ for the new buffer.

3. Allocate device memory given these requirements.

4. Bind the allocated memory to the buffer.

Instantiating the buffer follows the usual pattern of populating a descriptor and invoking the API:

```java
public static VulkanBuffer create(LogicalDevice dev, AllocationService allocator, long len, MemoryProperties<VkBufferUsage> props) {
    // Build buffer descriptor
    var info = new VkBufferCreateInfo();
    info.usage = IntegerEnumeration.mask(props.usage());
    info.sharingMode = props.mode();
    info.size = oneOrMore(len);

    // Allocate buffer
    VulkanLibrary lib = dev.library();
    PointerByReference handle = dev.factory().pointer();
    check(lib.vkCreateBuffer(dev, info, null, handle));
    
    ...
}
```

Next we retrieve the memory requirements for the vertex buffer:

```java
var reqs = new VkMemoryRequirements();
lib.vkGetBufferMemoryRequirements(dev, handle.getValue(), reqs);
```

Which are passed to the allocation service with the specified memory properties:

```java
DeviceMemory mem = allocator.allocate(reqs, props);
```

The allocated memory is then bound to the buffer:

```java
check(lib.vkBindBufferMemory(dev, handle.getValue(), mem, 0L));
```

And finally we create the vertex buffer domain object:

```java
return new VulkanBuffer(handle.getValue(), dev, props.usage(), mem, len);
```

The API for vertex buffers consists of the following methods:

```java
interface Library {
    int  vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);
    void vkDestroyBuffer(DeviceContext device, VulkanBuffer buffer, Pointer pAllocator);

    void vkGetBufferMemoryRequirements(LogicalDevice device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);
    int  vkBindBufferMemory(LogicalDevice device, Pointer buffer, Handle memory, long memoryOffset);

    void vkCmdBindVertexBuffers(Command.Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets);
    void vkCmdBindIndexBuffer(Command.Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType);
    void vkCmdCopyBuffer(Command.Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions);
}
```

---

## Vertex Configuration

### Overview

To use the vertex buffer in the shader we need to configure the structure of the data in the pipeline which consists of two pieces of information:

1. A _binding_ that specifies the vertex data to be passed to the shader.

2. A number of _vertex attributes_ that define the structure of the data (corresponding to layouts of the vertex components).

We introduce a new pipeline stage with nested builders for the bindings and attributes:

```java
public class VertexInputStageBuilder extends AbstractPipelineBuilder<VkPipelineVertexInputStateCreateInfo> {
    private final Map<Integer, BindingBuilder> bindings = new HashMap<>();
    private final List<AttributeBuilder> attributes = new ArrayList<>();

    public BindingBuilder binding() {
        return new BindingBuilder();
    }
}
```

The descriptor for the vertex configuration is generated as follows:

```java
VkPipelineVertexInputStateCreateInfo get() {
    // Create descriptor
    var info = new VkPipelineVertexInputStateCreateInfo();

    // Add binding descriptions
    info.vertexBindingDescriptionCount = bindings.size();
    info.pVertexBindingDescriptions = StructureHelper.pointer(bindings.values(), VkVertexInputBindingDescription::new, BindingBuilder::populate);

    // Add attributes
    info.vertexAttributeDescriptionCount = attributes.size();
    info.pVertexAttributeDescriptions = StructureHelper.pointer(attributes, VkVertexInputAttributeDescription::new, AttributeBuilder::populate);

    return info;
}
```

Note that the nested builders are added to the parent rather than using any intermediate data objects.

### Bindings

The nested builder for the bindings is relatively simple:

```java
public class BindingBuilder {
    private int index = bindings.size();
    private int stride;
    private VkVertexInputRate rate = VkVertexInputRate.VERTEX;
}
```

Note that the binding _index_ is initialised to the next available slot but can also be explicitly overridden.

The _stride_ is the number of bytes per vertex (normally the `length` of a vertex).

The `populate` method fills an instance of the corresponding Vulkan descriptor for the binding:

```java
void populate(VkVertexInputBindingDescription desc) {
    desc.binding = binding;
    desc.stride = stride;
    desc.inputRate = rate;
}
```

The `build` method validates the data and returns to the parent builder:

```java
public VertexInputStageBuilder build() {
    // Validate binding description
    if(bindings.containsKey(index)) throw new IllegalArgumentException(...);
    if(locations.isEmpty()) throw new IllegalArgumentException(...);

    // Add binding
    bindings.put(index, this);

    return VertexInputStageBuilder.this;
}
```

### Attributes

Vertex attributes are configured via the following factory method:

```java
public AttributeBuilder attribute() {
    return new AttributeBuilder(this);
}
```

The nested builder for the vertex attributes follows a similar pattern to the bindings:

```java
public class AttributeBuilder {
    private final BindingBuilder binding;
    private int loc;
    private VkFormat format;
    private int offset;

    private AttributeBuilder(BindingBuilder binding) {
        this.binding = binding;
        this.loc = binding.locations.size();
    }

    private void populate(VkVertexInputAttributeDescription attr) {
        attr.binding = binding.index;
        attr.location = loc;
        attr.format = format;
        attr.offset = offset;
    }
}
```

Where:

* _loc_ corresponds to the _layout_ directives in the GLSL shader (see below).

* _offset_ specifies the starting byte of the attribute.

Note that the attribute location is initialised to the next available slot which is tracked in the parent binding builder:

```java
public class BindingBuilder {
    ...
    private final Set<Integer> locations = new HashSet<>();
}
```

The build method validates the attribute and returns to the parent builder:

```java
public BindingBuilder build() {
    // Validate attribute
    if(offset >= binding.stride) throw new IllegalArgumentException(...);
    if(format == null) throw new IllegalArgumentException(...);

    // Check location
    if(binding.locations.contains(loc)) throw new IllegalArgumentException(...);
    binding.locations.add(loc);

    // Add attribute
    attributes.add(this);

    return binding;
}
```

---

## Integration 

### Vertex Data

We bring all this new functionality together to programatically create the triangle vertices and copy the vertex data to the hardware.

The triangle vertices are specified as a simple array:

```java
Vertex[] vertices = {
    new Vertex(new Point(0, -0.5f, 0), new Colour(1, 0, 0, 1)),
    new Vertex(new Point(0.5f, 0.5f, 0), new Colour(0, 0, 1, 1)),
    new Vertex(new Point(-0.5f, 0.5f, 0), new Colour(0, 1, 0, 1)),
};
```

Which we wrap with a temporary compound bufferable object:

```java
private static final Bufferable TRIANGLE = new Bufferable() {
    private final Vertex[] vertices = ...

    @Override
    public int length() {
        return 3 * vertices[0].length();
    }

    @Override
    public void buffer(ByteBuffer buffer) {
        for(Vertex v : vertices) {
            v.buffer(buffer);
        }
    }
};
```

### Staging Buffer

We next implement a helper on the VBO class to create a staging buffer.

The memory properties define a buffer that is used as the source of a copy operation and is visible to the host (i.e. the application):

```java
public static VulkanBuffer staging(LogicalDevice dev, Bufferable data) {
    // Init memory properties
    var props = new MemoryProperties.Builder<VkBufferUsage>()
        .usage(VkBufferUsage.TRANSFER_SRC)
        .required(VkMemoryProperty.HOST_VISIBLE)
        .required(VkMemoryProperty.HOST_COHERENT)
        .build();

    ...
}
```

Next we create the staging VBO:

```java
int len = data.length();
VulkanBuffer buffer = create(dev, len, props);
```

And write the buffered triangle data:

```java
ByteBuffer bb = buffer.memory().map().buffer();
data.buffer(bb);
```

### Vertex Buffers

We start a new configuration class for the vertex buffer and triangle data:

```java
@Configuration
public class VertexBufferConfiguration {
    private static final Bufferable TRIANGLE = ...

    @Bean
    public static VulkanBuffer vbo(LogicalDevice dev, AllocationService allocator, Pool pool) {
        ...
    }
}
```

In the bean method we use the new helper to create the staging buffer:

```java
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, TRIANGLE);
```

And similarly for the vertex buffer:

```java
MemoryProperties<VkBufferUsage> props = new MemoryProperties.Builder<VkBufferUsage>()
    .usage(VkBufferUsage.TRANSFER_DST)
    .usage(VkBufferUsage.VERTEX_BUFFER)
    .required(VkMemoryProperty.DEVICE_LOCAL)
    .build();

VulkanBuffer vbo = VulkanBuffer.create(dev, allocator, staging.length(), props);
```

We next submit the copy command and wait for it to complete:

```java
staging
    .copy(vbo)
    .submitAndWait(pool);
```

And finally we release the staging buffer:

```java
staging.destroy();
```

The `submitAndWait` method is a new helper on the command class:

```java
default void submitAndWait(Pool pool) {
    Buffer buffer = Work.submit(this, pool);
    pool.waitIdle(); // TODO - synchronise using fence
    buffer.free();
    return buffer;
}
```

Which delegates to another factory that creates and submits a _one-time_ command:

```java
public static Buffer submit(Command cmd, Pool pool) {
    // Allocate and record one-time command
    Buffer buffer = pool
        .allocate()
        .begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)
        .add(cmd)
        .end();

    // Submit work
    Work work = Work.of(buffer);
    work.submit();

    return buffer;
}
```

Note that currently this approach blocks the entire device, this will be replaced later with proper synchronisation.

### Configuration

The VBO is bound to the render sequence:

```java
public Buffer sequence(...) {
    Command draw = ...
    return graphics
        .allocate()
        .begin()
            .add(frame.begin())
            .add(pipeline.bind())
            .add(vbo.bindVertexBuffer())
            .add(draw)
            .add(FrameBuffer.END)
        .end();
}
```

And we configure the structure of the vertex data using the new pipeline stage builder:

```java
public Pipeline pipeline(...) {
    return new Pipeline.Builder()
        ...
        .input()
            .binding()
                .index(0)
                .stride(Point.LAYOUT.length() + Colour.LAYOUT.length())
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
                .format(VkFormat.R32G32B32A32_SFLOAT)
                .offset(Point.LAYOUT.length())
                .build()
            .build()
```

There is a lot of mucking about and hard-coded data here that we will address in the next chapter.

### Vertex Shader

The final change to the demo is to remove the hard coded vertex data in the shader and configure the incoming VBO, which involves:

- The addition of two `layout` directives to specify the incoming data from the vertex buffer, corresponding to the locations specified in the vertex attributes.

- Set `gl_Position` to the position of each vertex.

- Pass through the colour of each vertex onto the next stage.

The resultant vertex shader is:

```glsl
#version 450

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec4 inColour;

layout(location = 0) out vec4 fragColour;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    fragColor = inColour;
}
```

(The fragment shader is unchanged).

If all goes well we should see the same triangle.

A good test is to change the vertex positions and/or colours to make sure we are actually using the new functionality and shader.

---

## Summary

In this chapter we:

- Created new domain objects and supporting framework code to define vertex data.

- Implemented the VBO class to transfer the data from the application to the hardware.

- Integrated the vertex input pipeline stage builder.

