---
title: Vertex Buffers
---

## Overview

In this chapter we will replace the hard-coded triangle data in the shader with a _vertex buffer_ (sometimes referred to as a _vertex buffer object_ or VBO).

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

- New domain classes to specify the vertex data.

- The vertex buffer object itself.

- A command to copy between buffers.

- The _vertex input stage_ of the pipeline to configure the structure of the vertex data.

---

## Vertex Data

### Vertex Components

We will define a _vertex_ as a compound object consisting of the following components:

- position
- normal
- texture coordinates
- colour

Positions and normals are roughly the same so we will create a small class hierarchy with the following base-class:

```java
public class Tuple implements Bufferable {
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

The bufferable interface is implemented by domain classes that can be written to an NIO buffer:

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

The vertex position can now be represented by the following simple domain object:

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

Finally we add an RGBA colour:

```java
public record Colour(float red, float green, float blue, float alpha) implements Bufferable {
    public static final Colour WHITE = new Colour(1, 1, 1, 1);
    public static final Colour BLACK = new Colour(0, 0, 0, 1);

    public Colour {
        Check.isPercentile(red);
        Check.isPercentile(green);
        Check.isPercentile(blue);
        Check.isPercentile(alpha);
    }

    @Override
    public void buffer(ByteBuffer buffer) {
        buffer.putFloat(red).putFloat(green).putFloat(blue).putFloat(alpha);
    }

    @Override
    public long length() {
        return SIZE * Float.BYTES;
    }
}
```

We will not be using normals or the texture coordinates for the triangle demo so we will gloss over these objects until they are required.

We can now compose these domain objects into the new vertex class:

```java
public interface Vertex {
    Point position();
    Vector normal();
    TextureCoordinate coords();
    Colour colour();

    /**
     * Default implementation.
     */
    record DefaultVertex(Point position, Vector normal, TextureCoordinate coords, Colour colour) implements Vertex {
    }

    class Builder {
        ...
        public Vertex build() {
            return new DefaultVertex(pos, normal, coords, col);
        }
    }
}
```

Note that all the components of the vertex are optional.
Generally we would model optional properties properly using a Java `Optional` but we have intentionally broken that rule here for the sake of simplicity.

### Buffering Vertices

To buffer an entire vertex we add the following enumeration that extracts a component from a vertex:

```java
enum Component {
    POSITION(Point.SIZE, Vertex::position),
    NORMAL(Vector.SIZE, Vertex::normal),
    TEXTURE_COORDINATE(TextureCoordinate.Coordinate2D.SIZE, Vertex::coords),
    COLOUR(Colour.SIZE, Vertex::colour);

    ...

    /**
     * Extracts this component from the given vertex.
     * @param vertex Vertex
     * @return Vertex component
     */
    protected Bufferable map(Vertex vertex) {
        return mapper.apply(vertex);
    }
}
```

This is used in the new _vertex layout_ object that specifies the _order_ of the vertex components in the interleaved buffer:

```java
class Layout {
    private final List<Component> layout;
    private final int size;

    public Layout(List<Component> layout) {
        this.layout = List.copyOf(layout);
        this.size = layout.stream().mapToInt(Component::size).sum();
    }
}

public void buffer(Vertex vertex, ByteBuffer buffer) {
    for(final var c : layout) {
        c.map(vertex).buffer(buffer);
    }
}
```

Notes:

- The enumeration allows us to specify arbitrary vertex layouts using the same underlying object.

- The _size_ attribute is the total number of floating-point values in a vertex (used later when we allocate the interleaved buffer).

- This implementation assumes that the output data buffer will be interleaved (the most common approach) but other strategies should be simple to implement using these entities.

### Integration #1

We can now start the demo application for this phase (based on the triangle demo) and add the vertex data and an interleaved buffer:

```java
// Build triangle vertices
final Vertex[] vertices = {
    new Vertex.Builder().position(new Point(0, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
    new Vertex.Builder().position(new Point(-0.5f, 0.5f, 0)).colour(new Colour(0, 1, 0, 1)).build(),
    new Vertex.Builder().position(new Point(0.5f, 0.5f, 0)).colour(new Colour(0, 0,  1, 1)).build(),
};

// Define vertex layout
final Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.COLOUR);

// Create interleaved buffer
// TODO - helper
final ByteBuffer bb = ByteBuffer
    .allocate(vertices.length * layout.size() * Float.BYTES)
    .order(ByteOrder.nativeOrder());

// Buffer vertices
for(Vertex v : vertices) {
    layout.buffer(v, bb);
}
bb.rewind();
```

---

## Vertex Buffers

### Vertex Buffer Creation

Next we implement the vertex buffer domain class that will be used for both the staging and device-local buffers:

```java
public class VertexBuffer extends AbstractVulkanObject {
    private final long len;
    private final Pointer mem;

    /**
     * Constructor.
     * @param handle    Buffer handle
     * @param dev       Logical device
     * @param len       Length (bytes)
     * @param mem       Memory handle
     */
    VertexBuffer(Pointer handle, LogicalDevice dev, long len, Pointer mem) {
        super(handle, dev, dev.library()::vkDestroyBuffer);
        this.len = oneOrMore(len);
        this.mem = notNull(mem);
    }

    /**
     * @return Length of this buffer (bytes)
     */
    public long length() {
        return len;
    }
}
```

The _mem_ class member is a pointer to the internal memory of the buffer (whether host or device local).

As normal a vertex buffer is created via a builder:

```java
public static class Builder {
    private final Set<VkBufferUsageFlag> usage = new HashSet<>();
    private final MemoryAllocator.Allocation allocation;
    private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
    private long len;
    
    ...

    public Builder length(long len) {
        this.allocation.size(len);
        return this;
    }
        
    public VertexBuffer build() {
        // Validate
        if(usage.isEmpty()) throw new IllegalArgumentException("No buffer usage flags specified");
        if(allocation.size() == 0) throw new IllegalArgumentException("Cannot create an empty buffer");
    
        // Build buffer descriptor
        final VkBufferCreateInfo info = new VkBufferCreateInfo();
        info.usage = IntegerEnumeration.mask(usage);
        info.sharingMode = mode;
        info.size = allocation.size();
    
        // Allocate buffer
        final VulkanLibrary lib = dev.library();
        final PointerByReference handle = lib.factory().pointer();
        check(lib.vkCreateBuffer(dev.handle(), info, null, handle));
    
        // TODO - allocate memory    
        ...
    
        // Create buffer
        return new VertexBuffer(handle.getValue(), dev, allocation.size(), mem);
    }
}
```

The various _usage_ flags specify the purpose(s) of the buffer, e.g. whether it is a VBO, a destination for a copy operation, etc.

### Memory Allocation

In the `build()` method we query Vulkan for its memory requirements after the buffer has been created:

```java
// Query memory requirements
final VkMemoryRequirements reqs = new VkMemoryRequirements();
lib.vkGetBufferMemoryRequirements(dev.handle(), handle.getValue(), reqs);
```

These requirements are passed to the memory allocator (covered in the following section) which allocates a memory block of the appropriate type:

```java
// Allocate buffer memory
final Pointer mem = allocation.init(reqs).allocate();
```

And the memory pointer is bound to the buffer:

```java
// Bind memory
check(lib.vkBindBufferMemory(logical, handle, mem, 0L));
```

The internal memory is released in the over-ridden destroy method of the vertex buffer:

```java
@Override
public synchronized void destroy() {
    final LogicalDevice dev = super.device();
    dev.library().vkFreeMemory(dev.handle(), mem, null);
    super.destroy();
}
```

### Populating a Buffer

Populating a vertex buffer consists of the following steps:
1. Map the internal memory to an NIO buffer.
2. Copy the source data to this buffer.
3. Release the memory mapping.

We add the following method to the vertex buffer class to load the interleaved data:

```java
/**
 * Loads the given buffer to this vertex buffer.
 * @param buffer Data buffer
 * @throws IllegalStateException if the size of the given buffer exceeds the length of this vertex buffer
 */
public void load(ByteBuffer obj, long len, long offset) {
    // Check buffer
    Check.zeroOrMore(offset);
    if(offset + len > this.len) {
        throw new IllegalStateException(...);
    }

    // Map buffer memory
    final LogicalDevice dev = this.device();
    final VulkanLibrary lib = dev.library();
    final PointerByReference data = lib.factory().pointer();
    check(lib.vkMapMemory(dev.handle(), mem, offset, len, 0, data));

    try {
        // Copy to memory
        final ByteBuffer bb = data.getValue().getByteBuffer(0, len);
        obj.buffer(bb);
    }
    finally {
        // Cleanup
        lib.vkUnmapMemory(dev.handle(), mem);
    }
}
```

### Integration #2

Finally we add a factory method that creates a command to copy between vertex buffers, this will be used to move the vertex data from the staging buffer to the hardware.

```java
public Command copy(VertexBuffer dest) {
    final VkBufferCopy region = new VkBufferCopy();
    region.size = len;
    return (api, cb) -> api.vkCmdCopyBuffer(cb, this.handle(), dest.handle(), 1, new VkBufferCopy[]{region});
}
```

We bring all this together in the demo to copy the triangle vertex data to the hardware:

```java
// Create staging VBO
final VertexBuffer staging = new VertexBuffer.Builder(dev)
    .length(bb.capacity())
    .usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
    .build();

// Load to staging
staging.load(bb);

// Create device VBO
final VertexBuffer dest = new VertexBuffer.Builder(dev)
    .length(bb.capacity())
    .usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
    .usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
    .build();

// Copy from staging to device
final Queue queue = dev.queue(transferFamily);
final Command.Pool pool = Command.Pool.create(queue);
final Command.Buffer copy = pool
    .allocate()
    .begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
    .add(staging.copy(dest))
    .end();
new Work.Builder()
    .add(copyBuffer)
    .build()
    .submit();
queue.waitIdle();
copy.free();

// Release staging
staging.destroy();
```

The copy portion of the code is messy and long-winded - we will come back and simplify this later.

---

## Memory Allocator

### Allocator

The process of allocating the memory for the buffer will be largely replicated when we address textures in the next chapter - so we encapsulate memory allocation into the following helper:

```java
public class MemoryAllocator {
    /**
     * Creates a memory allocator for the given logical device.
     * @param dev Logical device
     * @return New memory allocator
     */
    static MemoryAllocator create(LogicalDevice dev) {
        // Retrieve memory properties for this device
        final var props = new VkPhysicalDeviceMemoryProperties();
        dev.library().vkGetPhysicalDeviceMemoryProperties(dev.parent().handle(), props);

        // Create allocator
        return new MemoryAllocator(dev, props);
    }

    private final LogicalDevice dev;
    private final VkPhysicalDeviceMemoryProperties props;

    /**
     * Constructor.
     * @param dev           Logical device
     * @param props         Memory properties
     */
    private MemoryAllocator(LogicalDevice dev, VkPhysicalDeviceMemoryProperties props) {
        this.dev = notNull(dev);
        this.props = notNull(props);
    }
}
```

Originally the `VkPhysicalDeviceMemoryProperties` was retrieved via an accessor on the physical device but we now incorporate this in the new class and remove the accessor.

A _memory allocation_ is specified by a local class that provides a builder-like interface:

```java
/**
 * @return New memory allocation
 */
public Allocation allocation() {
    return new Allocation();
}

/**
 * An <i>allocation</i> specifies memory requirements.
 */
public class Allocation {
    private long size;
    private int filter = Integer.MAX_VALUE;
    private final Set<VkMemoryPropertyFlag> flags = new HashSet<>();

    /**
     * Sets the required size of the memory.
     * @param size Required memory size (bytes)
     */
    public Allocation size(long size) {
        this.size = oneOrMore(size);
        return this;
    }

    /**
     * Sets the memory type filter bit-mask.
     * @param filter Memory type filter mask
     */
    public Allocation filter(int filter) {
        this.filter = filter;
        return this;
    }

    /**
     * Adds a memory property.
     * @param flag Memory property
     */
    public Allocation property(VkMemoryPropertyFlag flag) {
        flags.add(notNull(flag));
        return this;
    }
}
```

The _filter_ is a bit-mask of the available memory types specified by the `memoryTypeBits` field of the `VkMemoryRequirements` structure returned by Vulkan.
We also provide a convenience method to initialise the allocation from this structure:

```java
public Allocation init(VkMemoryRequirements reqs) {
    size(reqs.size);
    filter(reqs.memoryTypeBits);
    return this;
}
```

### Memory Allocation

The memory block is allocated as follows:

```java
public Pointer allocate() {
    // Validate
    if(size == 0) throw new IllegalArgumentException("Memory size not specified");

    // Find memory type
    final int mask = IntegerEnumeration.mask(flags);
    final int type = findMemoryType(filter, mask);

    // Init memory descriptor
    final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
    info.allocationSize = size;
    info.memoryTypeIndex = type;

    // Allocate memory
    final VulkanLibrary lib = dev.library();
    final PointerByReference mem = lib.factory().pointer();
    check(lib.vkAllocateMemory(dev.handle(), info, null, mem));

    // Get memory handle
    return mem.getValue();
}
```

The process of selecting the appropriate memory type is:
1. Walk the available memory types array.
2. Filter (by index) against the given `filter` bit-mask.
3. Filter by the required memory types.

This is implemented by the following helper:

```java
/**
 * Finds a memory type for the given memory properties.
 * @param filter        Memory types filter mask
 * @param mask          Memory properties bit-field
 * @return Memory type index
 * @throws RuntimeException if no suitable memory type is available
 */
private int findMemoryType(int filter, int mask) {
    // Find matching memory type index
    for(int n = 0; n < props.memoryTypeCount; ++n) {
        if(MathsUtil.isBit(filter, n) && MathsUtil.isMask(props.memoryTypes[n].propertyFlags, mask)) {
            return n;
        }
    }

    // Otherwise memory not available for this device
    throw new RuntimeException("No memory type available for specified memory properties:" + props);
}
```

### Future Enhancements

This works fine for our simple demos at this stage of development but we will need several enhancements for a production-ready allocator:

- The above creates a new memory block for every allocation but the total number of active allocations is capped by the hardware.  In general the allocator should maintain a memory _pool_ from which requested memory blocks are allocated (i.e. pointer offsets into a larger block of memory), with the pool growing as required within the constraints of the hardware.

- Vulkan also supports various memory _heaps_ that offer more optimal properties and performance for different memory scenarios, which are completely ignored in this implementation.

- Ideally the allocation would specify _required_ and _optimal_ memory requirements with the allocator falling back to the minimum requirements if the optimal strategy is not available.

- The allocator should also provide statistics to the application for diagnostics purposes (number of allocations, heap sizes, memory constraints, etc).

---

## Vertex Configuration

### Vertex Input Pipeline Stage

We next need to configure the structure of the vertex data in the pipeline.

This consists of two pieces of information:

1. A _binding_ description that specifies the data to be passed to the shader (essentially corresponding to a vertex layout).

2. A number of _attribute_ descriptors that define the format of each component of a vertex (i.e. each component of the layout).

We add two new nested builders to the pipeline stage builder:

```java
public class VertexInputStageBuilder extends AbstractPipelineBuilder<VkPipelineVertexInputStateCreateInfo> {
    private final Map<Integer, BindingBuilder> bindings = new HashMap<>();
    private final List<AttributeBuilder> attributes = new ArrayList<>();

    public class BindingBuilder {
    }
    
    public class AttributeBuilder {
    }   
}
```

The nested builder for the bindings is relatively simple:

```java
public class BindingBuilder {
    private int binding;
    private int stride;
    private VkVertexInputRate rate = VkVertexInputRate.VK_VERTEX_INPUT_RATE_VERTEX; // TODO - instancing

    private BindingBuilder() {
    }

    /**
     * Sets the binding index.
     * @param binding Binding index
     */
    public BindingBuilder binding(int binding) {
        this.binding = zeroOrMore(binding);
        return this;
    }

    /**
     * Sets the vertex stride.
     * @param stride Vertex stride (bytes)
     */
    public BindingBuilder stride(int stride) {
        this.stride = oneOrMore(stride);
        return this;
    }

    /**
     * Sets the input rate.
     * @param rate Input rate (default is {@link VkVertexInputRate#VK_VERTEX_INPUT_RATE_VERTEX})
     */
    public BindingBuilder rate(VkVertexInputRate rate) {
        this.rate = notNull(rate);
        return this;
    }
}
```

The _stride_ is the number of bytes per vertex which normally is the size() attribute of the vertex layout.

Again we add a `populate()` method that fills an instance of the Vulkan descriptor from this data:

```java
void populate(VkVertexInputBindingDescription desc) {
    desc.binding = binding;
    desc.stride = stride;
    desc.inputRate = rate;
}
```

The build method validates the data and returns to the parent builder:

```java
public VertexInputStageBuilder build() {
    // Validate binding description
    if(bindings.containsKey(binding)) throw new IllegalArgumentException("Duplicate binding index: " + binding);
    if(stride == 0) throw new IllegalArgumentException("Invalid vertex stride");

    // Add binding
    bindings.add(this);

    return VertexInputStageBuilder.this;
}
```

Note that we add the builder itself to the parent rather than creating any intermediate POJO for the binding data.

The builder for the vertex attributes follows the same pattern:

```java
public class AttributeBuilder {
    private int binding;
    private int loc;
    private VkFormat format;
    private int offset;

    ...

    private void populate(VkVertexInputAttributeDescription desc) {
        attr.binding = binding;
        attr.location = loc;
        attr.format = format;
        attr.offset = offset;
    }

    public VertexInputStageBuilder build() {
        // Validate attribute
        final BindingBuilder desc = bindings.get(binding);
        if(desc == null) throw new IllegalArgumentException("Invalid binding index for attribute: " + binding);
        if(offset >= desc.stride) throw new IllegalArgumentException("Offset exceeds vertex stride");
        if(format == null) throw new IllegalArgumentException("No format specified for attribute");

        // Check location
        if(desc.locations.contains(loc)) throw new IllegalArgumentException("Duplicate location: " + loc);
        desc.locations.add(loc);

        // Add attribute
        attributes.add(this);

        return VertexInputStageBuilder.this;
    }
}
```

The _loc_ field corresponds to the _layout_ directives in the GLSL shader (see below).

The _offset_ specifies the offset (in bytes) of the attribute within the vertex.

Finally we can complete the parent builder for the vertex input stage:

```java
protected VkPipelineVertexInputStateCreateInfo result() {
    // Validate bindings
    ...

    // Create descriptor
    final var info = new VkPipelineVertexInputStateCreateInfo();

    // Add binding descriptions
    if(!bindings.isEmpty()) {
        info.vertexBindingDescriptionCount = bindings.size();
        info.pVertexBindingDescriptions = StructureCollector.toPointer(VkVertexInputBindingDescription::new, bindings.values(), BindingBuilder::populate);

        // Add attributes
        info.vertexAttributeDescriptionCount = attributes.size();
        info.pVertexAttributeDescriptions = StructureCollector.toPointer(VkVertexInputAttributeDescription::new, attributes, AttributeBuilder::populate);
    }

    return info;
}
```

### Convenience

We also take the opportunity to implement a convenience method to create the binding and attributes for a given vertex layout.

First we select the next available binding index:

```java
public VertexInputStageBuilder binding(Vertex.Layout layout) {
    // Allocate next binding
    final int index = bindings.size();

    ...

    return this;
}
```

Then we use the nested builder to create the binding:

```java
    // Add binding
    new BindingBuilder()
        .binding(index)
        .stride(layout.size() * Float.BYTES)
        .build();
```

Next we iterate over the components of the layout:

```java
    // Add attribute for each component
    int offset = 0;
    int loc = 0;
    for(Vertex.Component c : layout.components()) {
    }
```

And create an attribute for each component:

```java
    // Determine component format
    final VkFormat format = new FormatBuilder()
        .components(c.size())
        .type(FormatBuilder.Type.FLOAT)
        .bytes(Float.BYTES)
        .build();

    // Add attribute for component
    new AttributeBuilder()
        .binding(index)
        .location(loc)
        .format(format)
        .offset(offset)
        .build();
```

The byte _offset_ within the vertex is incremented at the end of the loop:

```java
        // Increment offset to the start of the next attribute
        ++loc;
        offset += c.size() * Float.BYTES;
    }
    assert offset == layout.size() * Float.BYTES;
```

Using this helper we can configure the vertex input stage of the pipeline to match the vertex layout of the triangle:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    .input()
        .binding(layout)
        .build()
    ...
```

Which is equivalent to:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    .input()
        .binding()
            .binding(0)
            .stride(layout.size() * Float.BYTES)
            .build()
        .attribute()            // Position
            .binding(0)
            .location(0)
            .format(VkFormat.VK_FORMAT_R32G32B32_SFLOAT)
            .offset(0)
        .attribute()            // Colour
            .binding(0)
            .location(1)
            .format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT)            
            .offset(Point.SIZE * Float.BYTES)
        .build()
    ...
```

### Integration #3

The last change we need to make is a new command to bind the vertex buffer in the rendering sequence (before the draw command):

```java
public Command bind() {
    final PointerArray array = Handle.toPointerArray(List.of(this));
    return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
}
```

The above should work but doesn't achieve anything since we are not yet using the vertex buffer in the shader.

We need to make the following changes to the vertex shader code:

- Remove the hard-coded vertex data.

- Add two `layout` directives to specify the incoming data from the vertex buffer (matching the locations specified in the vertex attributes).

- Set `gl_Position` to the position of each vertex.

- Pass through the colour of each vertex.

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

A good test is to change one of the vertex positions and/or colours to make sure we are actually using the new functionality and shader.

---

## Summary

In this chapter we:

- Created new vertex domain objects and the vertex buffer object to transfer the triangle 'model' from the application to the hardware.

- Completed the builder for the vertex input pipeline stage.

- Implemented a first-cut memory allocator.

The API methods in this chapter are defined in the `VulkanLibraryBuffer` JNA interface.
