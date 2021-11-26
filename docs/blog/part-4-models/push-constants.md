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
