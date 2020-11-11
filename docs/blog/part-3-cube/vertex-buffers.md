---
title: Vertex Buffers
---

## Overview

In this chapter we will replace the hard-coded triangle data in the vertex shader with a _vertex buffer_.

A vertex buffer (sometimes referred to as a _vertex buffer object_ or VBO) is used to transfer vertex data from the application to the hardware.

This process involves the following steps:

1. build the vertex data
2. convert it to an interleaved NIO buffer
3. allocate a _staging_ vertex buffer (visible to the host)
4. copy the interleaved data to this staging buffer
5. allocate a _device local_ vertex buffer (visible to the hardware)
6. copy the staged data to this buffer

We will need:

- domain classes for the vertex data
- a mechanism to transform the data to an interleaved NIO buffer
- the vertex buffer class
- a new command to copy from the staging buffer to the device
- modifications to the vertex input stage of the pipeline to refer to the vertex buffer
- an updated shader that uses the vertex buffer

---

## Vertex Data

### Defining a Vertex

A _vertex_ can consist of some or all of the following components:

- position
- normal
- texture coordinates
- colour

We already have a colour class but we will need new domain objects to represent the other vertex components.

Points and normals are roughly the same so we will create a small class hierarchy with the following base-class:

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
}
```

The vertex position is trivial at this stage:

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

Finally we compose these domain objects into the new vertex class:

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

Notes:
- The colour class is modified to be `Bufferable`.
- Generally we should model optional properties properly using a Java `Optional` but we have intentionally broken that rule here for the sake of simplicity.
- For the moment we will gloss over the vector and texture coordinate classes as they are not needed until later in development.

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
     * @param handle        Buffer handle
     * @param dev            Logical device
     * @param len            Length (bytes)
     * @param mem            Memory handle
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

    public Builder usage(VkBufferUsageFlag usage) {
        this.usage.add(usage);
    }

    public Builder property(VkMemoryPropertyFlag prop) {
        this.allocation.property(prop);
    }

    public Builder mode(VkSharingMode mode) {
        this.mode = notNull(mode);
    }

    public Builder length(long len) {
        this.allocation.size(len);
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

### Memory Allocation

In the `build()` method, after the buffer has been created we query Vulkan for its memory requirements:

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

// Copy
final Queue queue = dev.queue(transferFamily);
final Command.Pool copyPool = Command.Pool.create(queue);
final Command.Buffer copyBuffer = Command.once(copyPool, staging.copy(dest));
new Work.Builder().add(copyBuffer).build().submit();
queue.waitIdle();
copyBuffer.free();

// Release staging
staging.destroy();
```

The copy operation uses a _one-time command_ that is allocated by a new helper in the command class:

```java
static Buffer once(Pool pool, Command cmd) {
    return pool
        .allocate()
        .begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
        .add(cmd)
        .end();
}
```

The copy code is still a bit messy and long-winded - we will come back and simplify this later.

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

The _filter_ is a bit-mask of the available memory types corresponding to the `memoryTypeBits` field of the `VkMemoryRequirements` structure returned by Vulkan.

We also provide a convenience method to initialise the allocation from a memory requirements structure:

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

## Vertex Input Configuration

### Vertex Input Pipeline Stage

We next need to configure the structure of the vertex data in the pipeline.

This consists of two pieces of information:

1. A _binding_ description that specifies the data to be passed to the shader (essentially corresponding to a vertex layout).

2. A number of _attribute_ descriptors that define the format of each component of a vertex (i.e. each component of the layout).

We add two new nested builders to the `VertexInputStageBuilder` to construct these new objects.

### Vertex Input Bindings

The builder for a binding is relatively simple:

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

### Vertex Attributes

The builder for a vertex attribute follows the same pattern:

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

### Revised Stage Builder

Next we integrate these child builders into the vertex input stage builder:

```java
public class VertexInputStageBuilder extends AbstractPipelineBuilder<VkPipelineVertexInputStateCreateInfo> {
    private final Map<Integer, BindingBuilder> bindings = new HashMap<>();
    private final List<AttributeBuilder> attributes = new ArrayList<>();

    ...
    
    @Override
    protected VkPipelineVertexInputStateCreateInfo result() {
        // Validate bindings
        ...

        // Create descriptor
        final var info = new VkPipelineVertexInputStateCreateInfo();

        // Add binding descriptions
        if(!bindings.isEmpty()) {
            info.vertexBindingDescriptionCount = bindings.size();
            info.pVertexBindingDescriptions = VulkanStructure.populate(VkVertexInputBindingDescription::new, bindings.values(), BindingBuilder::populate);

            // Add attributes
            info.vertexAttributeDescriptionCount = attributes.size();
            info.pVertexAttributeDescriptions = VulkanStructure.populate(VkVertexInputAttributeDescription::new, attributes, AttributeBuilder::populate);
        }

        return info;
    }
}
```

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
