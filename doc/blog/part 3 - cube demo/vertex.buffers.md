# Overview

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


# Vertex Data

## Defining a Vertex

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
- The colour class is modified to be bufferable.
- Generally we should model optional properties properly using an `Optional` but we have intentionally broken that rule here for the sake of simplicity.
- For the moment we will gloss over the vector and texture coordinate classes as they are not needed until later in development.

## Buffering Vertices

To buffer an entire vertex we add the following enumeration to the vertex class:

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

This is used in the new _vertex layout_ object that specifies the order of the vertex components in the interleaved buffer:

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
- The purpose of the `map()` method in the enumeration is to allow us to specify arbitrary vertex layouts.
- The _size_ attribute is the total number of floating-point values in a vertex which is used later when we allocate the interleaved buffer.
- This implementation assumes that the output data buffer will be interleaved (the most common approach) but other strategies should be simple to implement using these entities.

## Integration #1

We can now start the demo application for this phase (based on the triangle demo) and add the vertex data and an interleaved buffer:

```java
// Build triangle vertices
final Vertex[] vertices = {
		new Vertex.Builder().position(new Point(0, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
		new Vertex.Builder().position(new Point(0.5f, 0.5f, 0)).colour(new Colour(0, 1,  0, 1)).build(),
		new Vertex.Builder().position(new Point(-0.5f, 0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
};

// Define vertex layout
final Vertex.Layout layout = new Vertex.Layout(List.of(Vertex.Component.POSITION, Vertex.Component.COLOUR));

// Create interleaved buffer
final ByteBuffer bb = ByteBuffer.allocate(vertices.length * layout.size() * Float.BYTES).order(ByteOrder.nativeOrder());

// Buffer vertices
for(Vertex v : vertices) {
	layout.buffer(v, bb);
}
bb.rewind();
```


# Vertex Buffers

## Vertex Buffer Creation

Next we implement the vertex buffer domain class that will be used for both the staging and device-local buffers:

```java
public class VertexBuffer extends AbstractVulkanObject {
	private final long len;
	private final Pointer mem;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param len			Length (bytes)
	 * @param mem			Memory handle
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

As normal this is created via a builder:

```java
public static class Builder {
	private final Set<VkBufferUsageFlag> usage = new HashSet<>();
	private final Set<VkMemoryPropertyFlag> props = new HashSet<>();
	private VkSharingMode mode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
	private long len;

	public Builder usage(VkBufferUsageFlag usage) {
	}

	public Builder property(VkMemoryPropertyFlag prop) {
	}

	public Builder mode(VkSharingMode mode) {
	}

	public Builder length(long len) {
	}
		
	public VertexBuffer build() {
		// Validate
		if(usage.isEmpty()) throw new IllegalArgumentException("No buffer usage flags specified");
		if(len == 0) throw new IllegalArgumentException("Cannot create an empty buffer");
	
		// Build buffer descriptor
		final VkBufferCreateInfo info = new VkBufferCreateInfo();
		info.usage = IntegerEnumeration.mask(usage);
		info.sharingMode = mode;
		info.size = len;
	
		// Allocate buffer
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = lib.factory().pointer();
		check(lib.vkCreateBuffer(dev.handle(), info, null, handle));
	
		// TODO - allocate memory	
		...
	
		// Create buffer
		return new VertexBuffer(handle.getValue(), dev, len, mem);
	}
}
```

The _mem_ class member is a pointer to the internal memory of the buffer (whether host or device local).
After the buffer has been created we query Vulkan for its memory requirements, which is based on the memory properties we specified during construction:

```java
// Query memory requirements
final VkMemoryRequirements reqs = new VkMemoryRequirements();
lib.vkGetBufferMemoryRequirements(dev.handle(), handle.getValue(), reqs);
```

The memory requirements are passed to a helper (detailed below) that allocates memory of the appropriate type which is then bound to the buffer:

```java
public VertexBuffer build() {
	...
	
	// Allocate buffer memory
	final Pointer mem = dev.allocate(reqs, props);
	
	// Bind memory
	check(lib.vkBindBufferMemory(logical, handle, mem, 0L));
	
	// Create buffer
	return new VertexBuffer(handle.getValue(), dev, len, mem);
}
```

## Memory Allocation

The memory allocation helper is implemented by the logical device:

```java
/**
 * Allocates device memory.
 * @param reqs		Memory requirements
 * @param flags		Flags
 * @return Memory handle
 * @throws RuntimeException if the memory cannot be allocated
 */
public Pointer allocate(VkMemoryRequirements reqs, Set<VkMemoryPropertyFlag> flags) {
	// Find memory type
	final int type = parent.findMemoryType(reqs.memoryTypeBits, flags);

	// Init memory descriptor
	final VkMemoryAllocateInfo info = new VkMemoryAllocateInfo();
   info.allocationSize = reqs.size;
   info.memoryTypeIndex = type;

   // Allocate memory
   final VulkanLibrary lib = library();
   final PointerByReference mem = lib.factory().pointer();
   check(lib.vkAllocateMemory(this.handle(), info, null, mem));

   // Get memory handle
   return mem.getValue();
}
```

To determine the appropriate memory type this method delegates to another new helper method on the physical device:

```java
/**
 * Finds a memory type for the given memory properties.
 * @param props Memory properties
 * @return Memory type index
 * @throws ServiceException if no suitable memory type is available
 */
public int findMemoryType(int filter, Set<VkMemoryPropertyFlag> props) {
	// Retrieve memory properties
	final var mem = this.memory();

	// Find matching memory type index
	final int mask = IntegerEnumeration.mask(props);
	for(int n = 0; n < mem.memoryTypeCount; ++n) {
		if(MathsUtil.isBit(filter, n) && MathsUtil.isMask(mem.memoryTypes[n].propertyFlags, mask)) {
			return n;
		}
	}

	// Otherwise memory not available for this device
	throw new RuntimeException("No memory type available for specified memory properties:" + props);
}
```

We re-factor the memory properties accessor so that the data is cached locally:

```java
private VkPhysicalDeviceMemoryProperties mem;

...

/**
 * @return Memory properties of this device
 */
public VkPhysicalDeviceMemoryProperties memory() {
	if(mem == null) {
		mem = new VkPhysicalDeviceMemoryProperties();
		instance.library().vkGetPhysicalDeviceMemoryProperties(handle, mem);
	}
	return mem;
}
// TODO - mutable return value!
```

This change means we are essentially exposing a mutable property of the physical device.
Eventually we will wrap the JNA structure with some sort of helper class but at the moment we don't really know enough to make it worthwhile so we just make it `TODO`.

Finally the allocated memory is released in an over-ridden destroy method in the vertex buffer:

```java
@Override
public synchronized void destroy() {
	final LogicalDevice dev = super.device();
	dev.library().vkFreeMemory(dev.handle(), mem, null);
	super.destroy();
}
```

## Populating a Buffer

Populating a vertex buffer consists of the following steps:
1. Map the internal memory to an NIO buffer.
2. Copy the source data to this buffer.
3. Release the memory mapping.

We add a couple of overloaded methods to the vertex buffer class to load the interleaved data:

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
		throw new IllegalStateException(String.format("Buffer exceeds size of this VBO: length=%d offset=%d this=%s", len, offset, this));
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

public void load(ByteBuffer buffer) {
	load(Bufferable.of(buffer), buffer.remaining(), 0);
}
```

## Copying Buffers

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
final VertexBuffer staging = VertexBuffer.staging(dev, bb.capacity());

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
final Queue queue = dev.queue(transfer);
final Command.Pool copyPool = Command.Pool.create(queue);
final Command copyCommand = staging.copy(dest);
final Command.Buffer copyBuffer = copyPool.allocate(copyCommand);
new Work.Builder().add(copyBuffer).build().submit();
queue.waitIdle();
copyBuffer.free();
```

We also add a convenience factory method to create a staging vertex buffer.

The code for the copy operation is a bit messy and long-winded - we will come back and simplify this later.


# Vertex Input Configuration

## Vertex Input Pipeline Stage

We next need to configure the structure of the vertex data in the pipeline.

This consists of two pieces of information:
1. A _binding_ description that specifies the data to be passed to the shader (essentially corresponding to a vertex layout).
2. A number of _attribute_ descriptors that define the format of each component of a vertex (i.e. each component of the layout).

We add two new nested builders to the `VertexInputStageBuilder` to construct these new objects.

## Vertex Input Bindings

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

We add a private method that populates an instance of the Vulkan descriptor from this data:

```java
void populate(VkVertexInputBindingDescription desc) {
	desc.binding = binding;
	desc.stride = stride;
	desc.inputRate = rate;
}
```

The build method validates the data and returns to the parent builder:

```java
/**
 * Constructs this input binding.
 * @throws IllegalArgumentException for a duplicate binding index or an invalid vertex stride
 */
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

## Vertex Attributes

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

The build method checks its binding exists and also checks for duplicate attribute locations.

## Revised Stage Builder

Next we integrate these child builders into the vertex input stage builder:

```java
public class VertexInputStageBuilder extends AbstractPipelineBuilder<VkPipelineVertexInputStateCreateInfo> {
	private final Map<Integer, BindingBuilder> bindings = new HashMap<>();
	private final List<AttributeBuilder> attributes = new ArrayList<>();

	...
	
	@Override
	protected VkPipelineVertexInputStateCreateInfo result() {
		// Validate bindings
		for(final var b : bindings.values()) {
			if(b.locations.isEmpty()) {
				throw new IllegalArgumentException(String.format("No attributes specified for binding: ", b.binding));
			}
		}

		// Create descriptor
		final var info = new VkPipelineVertexInputStateCreateInfo();

		// Add binding descriptions
		if(!bindings.isEmpty()) {
			info.vertexBindingDescriptionCount = bindings.size();
			info.pVertexBindingDescriptions = VulkanStructure.array(VkVertexInputBindingDescription::new, bindings.values(), BindingBuilder::populate)[0];

			// Add attributes
			info.vertexAttributeDescriptionCount = attributes.size();
			info.pVertexAttributeDescriptions = VulkanStructure.array(VkVertexInputAttributeDescription::new, attributes, AttributeBuilder::populate)[0];
		}

		return info;
	}
}
```

Finally we add a convenience method to the builder that creates a binding and associated attributes for a given vertex layout:

```java
public VertexInputStageBuilder binding(Vertex.Layout layout) {
	// Allocate next binding
	final int index = bindings.size();

	// Add binding
	new BindingBuilder()
			.binding(index)
			.stride(layout.size() * Float.BYTES)
			.build();

	// Add attribute for each component
	int offset = 0;
	int loc = 0;
	for(Vertex.Component c : layout.components()) {
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

		// Increment offset to the start of the next attribute
		++loc;
		offset += c.size() * Float.BYTES;
	}
	assert offset == layout.size() * Float.BYTES;

	return this;
}
```

# Integration #2

## Vertex Input Stage

Using the above helper we can configure the vertex input stage of the pipeline to match the vertex layout of the triangle:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
	.input()
		.binding(layout)
		.build()
	...
```

This is equivalent to:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
	.input()
		.binding()
			.binding(0)
			.stride(layout.size() * Float.BYTES)
			.build()
		.attribute()				// Position
			.binding(0)
			.location(0)
			.format(VkFormat.VK_FORMAT_R32G32B32_SFLOAT)
			.offset(0)
		.attribute()				// Colour
			.binding(0)
			.location(1)
			.format(VkFormat.VK_FORMAT_R32G32B32A32_SFLOAT)			
			.offset(Point.SIZE * Float.BYTES)
		.build()
	...
```

Finally we add a new command to bind the vertex buffer in the rendering sequence (before the draw command):

```
public Command bind() {
	final PointerArray array = Handle.toPointerArray(List.of(this));
	return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, array, new long[]{0});
}
```

## Vertex Shader

The above should work but doesn't achieve anything since we are not actually using the vertex buffer.

We need to make the following changes to the vertex shader:
- Remove the hard-coded vertices.
- Add two `layout` directives to specify the incoming vertex data (the locations corresponds to the vertex layout).
- Change `gl_Position` to be the position of each vertex.
- Set the output `fragColour` to the colour of each vertex.

The resultant vertex shader is:

```C
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

If all goes well we should see the same triangle.  A good test is to change one of the vertex positions and/or colours to make sure we are using the new functionality and shader.


# Summary

In this chapter we implemented vertex domain objects and a vertex buffer to transfer the triangle 'model' from the application to the hardware.

