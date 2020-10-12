# Vertex Buffer

```java
public class VertexBuffer extends AbstractVulkanObject {
	private final long len;
	private final Pointer mem;

	/**
	 * Constructor.
	 * @param handle		Buffer handle
	 * @param dev			Logical device
	 * @param len			Length (bytes)
	 */
	private VertexBuffer(Pointer handle, LogicalDevice dev, long len, Pointer mem) {
		super(handle, dev, dev.library()::vkDestroyBuffer);
		this.len = oneOrMore(len);
		this.mem = notNull(mem);
	}

	/**
	 * Loads the given byte-buffer to this vertex buffer.
	 * @param bytes Byte-buffer
	 */
	public void load(ByteBuffer bytes) {
	}

	/**
	 * @return Command to bind this buffer
	 */
	public Command bind() {
		return (api, buffer) -> api.vkCmdBindVertexBuffers(buffer, 0, 1, new Handle[]{this.handle()}, new long[]{0});
	}
}
```

# Builder

```java
public VertexBuffer build() {
	// Validate
	if(usage.isEmpty()) throw new IllegalArgumentException("No buffer usage flags specified");
	if(len == 0) throw new IllegalArgumentException("Cannot create an empty buffer");

	// Build buffer descriptor
	final VkBufferCreateInfo info = new VkBufferCreateInfo();
	info.usage = IntegerEnumeration.mask(usage);
	info.sharingMode = mode;
	info.size = len;
	// TODO - queue families

	// Allocate buffer
	final VulkanLibrary lib = dev.library();
	final PointerByReference handle = lib.factory().pointer();
	check(lib.vkCreateBuffer(dev.handle(), info, null, handle));

	// Query memory requirements
	final VkMemoryRequirements reqs = new VkMemoryRequirements();
	lib.vkGetBufferMemoryRequirements(dev.handle(), handle.getValue(), reqs);

	// Allocate buffer memory
	final Pointer mem = dev.allocate(reqs, props);

	// Bind memory
	check(lib.vkBindBufferMemory(logical, handle, mem, 0L));

	// Create buffer
	return new VertexBuffer(handle.getValue(), dev, len);
}
```

# Memory Allocation - Physical Device

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
	throw new ServiceException("No memory type available for specified memory properties:" + props);
}
```

# Memory Allocation - Logical Device

```java
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

# Destroy

```java
@Override
public synchronized void destroy() {
	final LogicalDevice dev = super.device();
	dev.library().vkFreeMemory(dev.handle(), mem, null);
	super.destroy();
}
```

# Load Buffer

```java
/**
 * Loads the given source buffer to this vertex buffer.
 * @param src Source buffer
 * @throws IllegalStateException if the given buffer exceeds the size of this vertex buffer
 */
public void load(ByteBuffer src) {
	// Check buffer
	final int actual = src.remaining();
	if(actual > len) throw new IllegalStateException(String.format("Buffer exceeds length of this data buffer: len=%d max=%d", actual, len));

	// Map buffer memory
	final LogicalDevice dev = this.device();
	final VulkanLibrary lib = dev.library();
	final PointerByReference data = lib.factory().pointer();
	check(lib.vkMapMemory(dev.handle(), mem, 0, actual, 0, data));

	// Copy to memory
	final ByteBuffer bb = data.getValue().getByteBuffer(0, actual);
	bb.put(src);

	// Cleanup
	lib.vkUnmapMemory(dev.handle(), mem);
}
```

# Copy Command

```java
public Command copy(VertexBuffer dest) {
	final VkBufferCopy region = new VkBufferCopy();
	region.size = len;
	return (api, cb) -> api.vkCmdCopyBuffer(cb, this.handle(), dest.handle(), 1, new VkBufferCopy[]{region});
}
```

# Demo

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
final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
final Command copyCommand = staging.copy(dest);
final Command.Buffer copyBuffer = copyPool.allocate(copyCommand);
Work.submit(copyBuffer, true);
```

# Vertex Input Stage

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

# Vertex Shader

```java
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec4 inColor;

layout(location = 0) out vec4 fragColor;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    fragColor = inColor;
}
```
