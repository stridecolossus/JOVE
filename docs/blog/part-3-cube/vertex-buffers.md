---
title: Vertex Buffers
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

### Framework

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

A _vertex_ is a compound object comprised of an arbitrary collection of bufferable _components_:

```java
public class Vertex implements Bufferable {
    /**
     * A <i>vertex component</i> is a bufferable object that can be comprised in a vertex.
     */
    public interface Component extends Bufferable {
        ...
    }

    private final List<Component> components;
}
```

The structure of a _vertex component_ is specified by a _layout_ declaration:

```java
public interface Component extends Bufferable {
    /**
     * @return Layout of this component
     */
    Layout layout();
}
```

Which is a simple record type specifying the type and number of the elements that make up that component:

```java
public record Layout(int size, int bytes, Class<?> type) {
    public int length() {
        return size * bytes;
    }
}
```

For example a point or vector is comprised of three floating-point values:

```java
new Layout(3, Float.BYTES, Float.class);
```

Finally a compound vertex can be written to an NIO buffer:

```java
public class Vertex implements Bufferable {
    ...
    
    @Override
    public int length() {
        return components.stream().mapToInt(Component::length).sum();
    }

    @Override
    public void buffer(ByteBuffer buffer) {
        for(Component obj : components) {
            obj.buffer(buffer);
        }
    }
}
```

Notes:

* The component layout is used to configure the structure of the vertex data to be passed to the shader (see below).

* This implementation assumes that vertex data is _interleaved_.

* The design may be slightly over-engineered at the expense of supporting flexibility and a framework for custom implementations.

### Implementation

We can now implement vertex components to support the triangle demo.

In general a vertex is comprised of the following components:
- position
- normal
- texture coordinate
- colour

Positions and normal vectors are both floating-point tuples with common functionality, we implement a small hierarchy with the following base-class:

```java
public sealed class Tuple implements Bufferable, Component permits Point, Vector {
    public static final int SIZE = 3;
    public static final Layout LAYOUT = Layout.of(SIZE);

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

    @Override
    public final Layout layout() {
        return LAYOUT;
    }
}
```

A vertex position is implemented as a point in 3D space:

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

A colour is defined as an RGBA tuple:

```java
public record Colour(float red, float green, float blue, float alpha) implements Bufferable, Component {
    public static final Layout LAYOUT = Layout.of(4);
    public static final Colour WHITE = new Colour(1, 1, 1, 1);
    public static final Colour BLACK = new Colour(0, 0, 0, 1);

    @Override
    public void buffer(ByteBuffer buffer) {
        buffer.putFloat(red).putFloat(green).putFloat(blue).putFloat(alpha);
    }

    @Override
    public final Layout layout() {
        return LAYOUT;
    }
}
```

Vectors and texture coordinates are not required for the triangle demo so we will gloss over these objects until later.

Finally we implement a builder for a vertex with the above components:

```java
public static class Builder {
    private Point pos;
    private Vector normal;
    private Coordinate coord;
    private Colour col;

    public Vertex build() {
        final var components = Stream
            .of(pos, normal, coord, col)
            .filter(Objects::nonNull)
            .collect(toList());

        return new Vertex(components);
    }
}
```

Note that all the components of the vertex are optional.

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
    final VulkanBuffer[] array = new VulkanBuffer[]{this};
    return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
}
```

The local `require` helper checks that the buffer supports a given operation (in this case that it is a vertex buffer that can be bound to a pipeline).

Copying the vertex data from staging to a device-local buffer is also implemented as a command:

```java
public Command copy(VulkanBuffer dest) {
    // Validate
    if(len > dest.len) throw new IllegalStateException(...);
    require(VkBufferUsage.TRANSFER_SRC);
    dest.require(VkBufferUsage.TRANSFER_DST);

    // Build copy descriptor
    final VkBufferCopy region = new VkBufferCopy();
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
        mem.close();
    }
}
```

Creating a Vulkan buffer is comprised of the following steps:

1. Instantiate the buffer.

2. Retrieve the _memory requirements_ for the new buffer.

3. Allocate device memory given these requirements.

4. Bind the allocated memory to the buffer.

Instantiating the buffer follows the usual pattern of populating a descriptor and invoking the API:

```java
public static VulkanBuffer create(LogicalDevice dev, long len, MemoryProperties<VkBufferUsage> props) {
    // Build buffer descriptor
    final var info = new VkBufferCreateInfo();
    info.usage = IntegerEnumeration.mask(props.usage());
    info.sharingMode = props.mode();
    info.size = oneOrMore(len);

    // Allocate buffer
    final VulkanLibrary lib = dev.library();
    final PointerByReference handle = lib.factory().pointer();
    check(lib.vkCreateBuffer(dev, info, null, handle));
    
    ...
}
```

Then the underlying device memory is allocated and bound to the new buffer:

```java
// Query memory requirements
final var reqs = new VkMemoryRequirements();
lib.vkGetBufferMemoryRequirements(dev, handle.getValue(), reqs);

// Allocate buffer memory
final DeviceMemory mem = dev.allocate(reqs, props);

// Bind memory
check(lib.vkBindBufferMemory(dev, handle.getValue(), mem.handle(), 0L));
```

And finally we create the vertex buffer domain object:

```java
// Create buffer
return new VulkanBuffer(handle.getValue(), dev, props.usage(), mem, len);
```

The API for vertex buffers consists of the following methods:

```java
interface VulkanLibraryBuffer {
    int vkCreateBuffer(LogicalDevice device, VkBufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pBuffer);
    void vkDestroyBuffer(DeviceContext device, VulkanBuffer buffer, Pointer pAllocator);

    void vkGetBufferMemoryRequirements(LogicalDevice device, Pointer buffer, VkMemoryRequirements pMemoryRequirements);
    int vkBindBufferMemory(LogicalDevice device, Pointer buffer, Handle memory, long memoryOffset);

    void vkCmdBindVertexBuffers(Command.Buffer commandBuffer, int firstBinding, int bindingCount, VulkanBuffer[] pBuffers, long[] pOffsets);
    void vkCmdBindIndexBuffer(Command.Buffer commandBuffer, VulkanBuffer buffer, long offset, VkIndexType indexType);

    void vkCmdCopyBuffer(Command.Buffer commandBuffer, VulkanBuffer srcBuffer, VulkanBuffer dstBuffer, int regionCount, VkBufferCopy[] pRegions);
}
```

---

## Vertex Configuration

### Overview

To use the vertex buffer in the shader we need to configure the structure of the data in the pipeline, which is where the vertex layout comes into play.

This configuration consists of two pieces of information:

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

    public AttributeBuilder attribute() {
        return new AttributeBuilder();
    }
}
```

The descriptor for the vertex configuration is generated as follows:

```java
VkPipelineVertexInputStateCreateInfo get() {
    // Create descriptor
    final var info = new VkPipelineVertexInputStateCreateInfo();

    // Add binding descriptions
    info.vertexBindingDescriptionCount = bindings.size();
    info.pVertexBindingDescriptions = StructureHelper.first(bindings.values(), VkVertexInputBindingDescription::new, BindingBuilder::populate);

    // Add attributes
    info.vertexAttributeDescriptionCount = attributes.size();
    info.pVertexAttributeDescriptions = StructureHelper.first(attributes, VkVertexInputAttributeDescription::new, AttributeBuilder::populate);

    return info;
}
```

Note that the nested builders are added to the parent rather than using any intermediate data objects.

### Bindings

The nested builder for the bindings is relatively simple:

```java
public class BindingBuilder {
    private int binding;
    private int stride;
    private VkVertexInputRate rate = VkVertexInputRate.VERTEX;
}
```

The _stride_ is the number of bytes per vertex (normally the `length` attribute of a vertex).

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
    if(bindings.containsKey(binding)) throw new IllegalArgumentException(...);
    if(stride == 0) throw new IllegalArgumentException(...);

    // Add binding
    bindings.add(this);

    return VertexInputStageBuilder.this;
}
```

### Attributes

The nested builder for the vertex attributes follows the same pattern:

```java
public class AttributeBuilder {
    private int binding;
    private int loc;
    private VkFormat format;
    private int offset;

    private void populate(VkVertexInputAttributeDescription attr) {
        attr.binding = binding;
        attr.location = loc;
        attr.format = format;
        attr.offset = offset;
    }

    public VertexInputStageBuilder build() {
        ...
        attributes.add(this);
        return VertexInputStageBuilder.this;
    }
}
```

Where:

* _loc_ corresponds to the _layout_ directives in the GLSL shader (see below).

* _offset_ specifies the starting byte of the attribute.

We also implement validation (not shown) to ensure the attribute offsets and vertex stride are logical and that attribute locations are not duplicated.

---

## Integration 

### Vertex Data

We bring all this new functionality together to programatically create the triangle vertices and copy the vertex data to the hardware.

The triangle vertices are specified as a simple array:

```java
Vertex[] vertices = {
    new Vertex.Builder().position(new Point(0, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
    new Vertex.Builder().position(new Point(-0.5f, 0.5f, 0)).colour(new Colour(0, 1, 0, 1)).build(),
    new Vertex.Builder().position(new Point(0.5f, 0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
};
```

Which we wrap as a bufferable object:

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

And add to a new configuration class:

```java
@Configuration
public class VertexBufferConfiguration {
    private static final Bufferable TRIANGLE = ... {
    };
    
    @Autowired private LogicalDevice dev;
}
```

### Vertex Buffers

We next implement a helper on the VBO class to create the staging buffer.

The memory properties define a buffer that is used as the source of a copy operation and is visible to the host (i.e. the application):

```java
public static VulkanBuffer staging(LogicalDevice dev, Bufferable data) {
    // Init memory properties
    final var props = new MemoryProperties.Builder<VkBufferUsage>()
        .usage(VkBufferUsage.TRANSFER_SRC)
        .required(VkMemoryPropertyFlag.HOST_VISIBLE)
        .required(VkMemoryPropertyFlag.HOST_COHERENT)
        .build();

    ...
}
```

Next we create the staging VBO:

```java
// Create staging buffer
final int len = data.length();
final VulkanBuffer buffer = create(dev, len, props);
```

And write the buffered triangle data:

```java
// Write data to buffer
final ByteBuffer bb = buffer.memory().map().buffer();
data.buffer(bb);
```

The new helper is used to create the staging buffer bean:

```java
@Bean
public VulkanBuffer staging() {
    return VulkanBuffer.staging(dev, TRIANGLE);
}
```

The device-local VBO is created similarly:

```java
@Bean
public VulkanBuffer vbo() {
    final MemoryProperties<VkBufferUsage> props = new MemoryProperties.Builder<VkBufferUsage>()
            .usage(VkBufferUsage.TRANSFER_DST)
            .usage(VkBufferUsage.VERTEX_BUFFER)
            .required(VkMemoryPropertyFlag.DEVICE_LOCAL)
            .build();

    return VulkanBuffer.create(dev, TRIANGLE.length(), props);
}
```

### Copy Operation

TODO


### Vertex Shader

The final change to the demo is to remove the hard coded vertex data in the shader and configure the incoming VBO, which involves:

- The addition of two `layout` directives to specify the incoming data from the vertex buffer, corresponding to the locations specified in the vertex attributes.

- Set `gl_Position` to the position of each vertex.

- Pass through the colour of each vertex onto the next stage.

The resultant vertex shader is:

```glsl
#version 450
#extension GL_ARB_separate_shader_objects : enable

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

- Implemented the vertex buffer object to transfer the data from the application to the hardware.

- Integrated the vertex input pipeline stage builder.

