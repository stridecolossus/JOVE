---
title: Rendering Commands
---

## Overview

We are on the last lap for the goal of this phase of development - rendering a simple triangle.

The final components we need to complete the demo are the command sequence for drawing the triangle and a simple render loop that acquires and presents a frame.

---

## Commands

### Introduction

Vulkan implements work to performed on a queue by recording a sequence of _commands_ to a _command buffer_, which is allocated and managed by a _command pool_.

For the triangle demo the command sequence is:

1. start the render pass

2. bind the pipeline

3. draw the triangle

4. end the render pass

To specify this chain of commands we would again like to design a fluid API as illustrated by the following pseudo-code:

```java
Command draw = ...

buffer
    .begin()
    .add(pass.begin())
    .add(pipeline.bind())
    .add(draw)
    .add(pass.end());
    .end();
```

We start with an outline class for commands, buffers and pools:

```java
@FunctionalInterface
public interface Command {
    /**
     * Executes this command.
     * @param lib       Vulkan library
     * @param buffer    Command buffer handle
     */
    void execute(VulkanLibrary lib, Handle buffer);

    /**
     * A <i>command buffer</i> is allocated by a {@link Pool} and used to record commands.
     */
    class Buffer implements NativeObject {
    }

    /**
     * A <i>command pool</i> allocates and maintains command buffers that are used to perform work on a given {@link Queue}.
     */
    class Pool extends AbstractVulkanObject {
    }
}
```

The `Command` interface abstracts the signature of a Vulkan command whose arguments are always comprised of the API and a handle to the command buffer.

We also create a new API to support commands:

```java
interface VulkanLibraryCommandBuffer {
    int vkCreateCommandPool(Handle device, VkCommandPoolCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pCommandPool);
    int vkResetCommandPool(Handle device, Handle commandPool, int flags);
    void vkDestroyCommandPool(Handle device, Handle commandPool, Handle pAllocator);

    int vkAllocateCommandBuffers(Handle device, VkCommandBufferAllocateInfo pAllocateInfo, Pointer[] pCommandBuffers);
    int vkResetCommandBuffer(Handle commandBuffer, int flags);
    void vkFreeCommandBuffers(Handle device, Handle commandPool, int commandBufferCount, Handle pCommandBuffers);

    int vkBeginCommandBuffer(Handle commandBuffer, VkCommandBufferBeginInfo pBeginInfo);
    int vkEndCommandBuffer(Handle commandBuffer);
}
```

### Command Pool

We start with a domain object for the command pool:

```java
class Pool extends AbstractVulkanObject {
    private final Queue queue;

    private Pool(Pointer handle, LogicalDevice dev, Queue queue) {
        super(handle, dev);
        this.queue = notNull(queue);
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyCommandPool;
    }
}
```

A pool is created via a factory method:

```java
public static Pool create(LogicalDevice dev, Queue queue, VkCommandPoolCreateFlag... flags) {
    // Init pool descriptor
    final var info = new VkCommandPoolCreateInfo();
    info.queueFamilyIndex = queue.family().index();
    info.flags = IntegerEnumeration.mask(flags);

    // Create pool
    final VulkanLibrary lib = dev.library();
    final PointerByReference pool = lib.factory().pointer();
    check(lib.vkCreateCommandPool(dev.handle(), info, null, pool));

    // Create pool
    return new Pool(pool.getValue(), dev, queue);
}
```

To allocate command buffers we add the following factory method to the pool:

```java
public List<Buffer> allocate(int num, boolean primary) {
    // Init descriptor
    final VkCommandBufferAllocateInfo info = new VkCommandBufferAllocateInfo();
    info.level = primary ? VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
    info.commandBufferCount = oneOrMore(num);
    info.commandPool = this.handle();

    // Allocate buffers
    final DeviceContext dev = super.device();
    final VulkanLibrary lib = dev.library();
    final Pointer[] handles = lib.factory().array(num);
    check(lib.vkAllocateCommandBuffers(dev.handle(), info, handles));
    
    ...
}
```

The handles of the newly allocated buffers are then transformed to domain objects:

```java
// Create buffers
final var list = Arrays
    .stream(handles)
    .map(ptr -> new Buffer(ptr, this))
    .collect(toList());

// Register buffers
buffers.addAll(list);

return list;
```

Note that command buffers are automatically released by the pool when it is destroyed, however we also track the allocated buffers:

```java
class Pool {
    private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();
    
    ...

    @Override
    protected void release() {
        buffers.clear();
    }
}
```

Buffers can also be explicitly released back to the pool by the application:

```java
public synchronized void free() {
    free(buffers);
    buffers.clear();
}

private void free(Collection<Buffer> buffers) {
    final LogicalDevice dev = super.device();
    dev.library().vkFreeCommandBuffers(dev.handle(), this.handle(), buffers.size(), Handle.toArray(buffers));
}
```

Finally the pool can also be reset which recycles resources and restores all allocated buffers to their initial state:

```java
public void reset(VkCommandPoolResetFlag... flags) {
    final int mask = IntegerEnumeration.mask(flags);
    final LogicalDevice dev = super.device();
    check(dev.library().vkResetCommandPool(dev.handle(), this.handle(), mask));
}
```

### Command Buffers

A command buffer is used to __record__ a sequence of commands which can then be submitted to a work queue for execution:

```java
class Buffer implements NativeObject {
    private enum State {
        INITIAL,
        RECORDING,
        EXECUTABLE,
    }

    private final Handle handle;
    private final Pool pool;
    private State state = State.INITIAL;

    private Buffer(Pointer handle, Pool pool) {
        this.handle = new Handle(handle);
        this.pool = notNull(pool);
    }

    public boolean isReady() {
        return state == State.EXECUTABLE;
    }
}
```

The _state_ member track whether the buffer has been recorded and is ready for execution (the enumeration names are based on the Vulkan documentation).

The `begin` method starts recording of a command sequence:

```java
public Buffer begin(VkCommandBufferUsageFlag... flags) {
    // Check buffer can be recorded
    if(state != State.INITIAL) throw new IllegalStateException(...);

    // Init descriptor
    final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
    info.flags = IntegerEnumeration.mask(flags);

    // Start buffer recording
    final VulkanLibrary lib = pool.device().library();
    check(lib.vkBeginCommandBuffer(handle, info));

    // Start recording
    state = State.RECORDING;
    return this;
}
```

The sequence is then recorded by adding commands:

```java
public Buffer add(Command cmd) {
    if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording: " + this);
    cmd.execute(pool.device().library(), handle);
    return this;
}
```

Finally the `end` method completes recording:

```java
public Buffer end() {
    if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording: " + this);
    final VulkanLibrary lib = pool.device().library();
    check(lib.vkEndCommandBuffer(handle));
    state = State.EXECUTABLE;
    return this;
}
```

We also convenience mutators to reset or release a buffer back to the pool.

### Work Submission

Executing a command buffer involves populating a Vulkan descriptor comprising a _batch_ of buffers to be submitted to a work queue along with relevant synchronisation declarations.  We will ignore synchronisation until later as it is not needed for the triangle demo.

We create the `Work` class that wraps the submission descriptor and work queue:

```java
public class Work {
    private final VkSubmitInfo info;
    private final Queue queue;
}
```

To create a work instance we add the obligatory builder:

```java
public static class Builder {
    private final List<Buffer> buffers = new ArrayList<>();
    private Queue queue;
}
```

The `add` method adds a command buffer to the work submission:

```java
public Builder add(Buffer buffer) {
    // Check buffer has been recorded
    if(!buffer.isReady()) throw new IllegalStateException(...);
    
    ...
    
    // Add buffer to this work
    buffers.add(buffer);

    return this;
}
```

Note that all command buffers __must__ be submitted to the same queue family, which is validated in the `add` method:

```
Queue q = buffer.pool().queue();
if(queue == null) {
    this.queue = q;
}
else {
    if(!queue.family().equals(q.family())) {
        throw new IllegalArgumentException(...);
    }
}
```

The build method populates the descriptor for the work submission and allocates the new domain object instance:

```java
public Work build() {
    // Init batch descriptor
    final VkSubmitInfo info = new VkSubmitInfo();

    // Populate command buffers
    info.commandBufferCount = buffers.size();
    info.pCommandBuffers = Handle.toArray(buffers);

    // Create work
    return new Work(info, queue);
}
```



---

## Integration

### Command Implementation

We can now implement the specific commands required for the triangle demo (based on the pseudo-code above).

We add the following factory method to the `RenderPass` class to start rendering:

```java
public Command begin(FrameBuffer buffer) {
    // Create descriptor
    final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
    info.renderPass = this.handle();
    info.framebuffer = buffer.handle();
    info.renderArea = buffer.extents().toRect2D();

    // Init clear values
    // TODO...
    final VkClearValue clear = new VkClearValue();
    clear.setType("color");
    clear.color.setType("float32");
    clear.color.float32 = new float[]{0.3f, 0.3f, 0.3f, 1};
    info.clearValueCount = 1;
    info.pClearValues = clear;
    // ...TODO

    // Create command
    return (lib, handle) -> lib.vkCmdBeginRenderPass(handle, info, VkSubpassContents.INLINE);
}
```

The command also initialises the clear values for the frame buffer attachments - we have hard-coded a grey colour for our single colour attachment.  
In a future chapter we will replace this temporary code with a proper implementation for both colour and depth attachments.

> We explain the purpose of the various `setType` calls when we address depth buffers in the [models](/JOVE/blog/part-4-models/model-loader) chapter.

Ending the render pass can be defined as a constant since there are no additional arguments:

```java
public static final Command END_COMMAND = (api, buffer) -> api.vkCmdEndRenderPass(buffer);
```

To bind a pipeline in the render sequence we add the following factory method to the `Pipeline` class:

```java
public Command bind() {
    return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.GRAPHICS, this.handle());
}
```

Finally for the moment we hard-code the drawing command in the demo:

```java
Command draw = (api, handle) -> api.vkCmdDraw(handle, 3, 1, 0, 0);
```

This specifies the three triangles vertices, in a single instance, both starting at index zero.
Later we will factor this out to a factory when we address vertex buffers and models.

### Rendering Sequence

We can now integrate all of the above to create and record the rendering sequence.

First we create a command buffer for each swapchain image:

```java
final Command.Pool pool = Command.Pool.create(presentationQueue);
final List<Command.Buffer> commands = pool.allocate(buffers.size());
```

Next we record the rendering sequence for each frame-buffer:

```java
for(int n = 0; n < buffers.size(); ++n) {
    final Command.Buffer cmd = commands.get(n);
    final FrameBuffer fb = buffers.get(n);
    cmd.begin()
        .add(pass.begin(fb))
        .add(pipeline.bind())
        .add(draw)
        .add(RenderPass.END_COMMAND)
    .end();
}
```

We have several parallel lists here sized by the swapchain buffering strategy (the swapchain image/views, the frame buffers and the commands) which we will eventually want to compose into an actual object but the above is fine for the moment.

### Rendering

To render the triangle we emulate a single frame:

```java
// Start next frame
final int index = chain.acquire();

// Render frame
new Work.Builder()
    .add(commands.get(index))
    .stage(VkPipelineStageFlag.TOP_OF_PIPE)
    .build()
    .submit();

// TODO
present.waitIdle();

// Present frame
chain.present(present);

// TODO
Thread.sleep(1000);
```

Obviously this is temporary code just sufficient to test this first demo - we will be implementing a proper render loop in future chapters.






### All that for a triangle?

If all goes well when we run the demo we should see the following:

![Triangle](triangle.png)

Viola!

There are a few gotchas that could result in staring at a blank screen:

- Although not covered in this demo the rasterizer pipeline stage specifies culling of back-facing polygons by default.  The triangle vertices are counter-clockwise (which _should_ be the default winding order) but changing the culling mode (or disabling culling altogether) is worth checking.

- Double-check that the format of the swapchain images is as expected.

- Walk through the configuration of the render pass and ensure that the correct load/store operations and image layouts are specified.

- Check the arguments for the hard-coded drawing command (they are all integers and could easily be accidentally transposed).

---

## Summary

In this final chapter for this phase of development we implemented commands and a crude render loop to display a triangle.

The API methods introduced in this chapter are defined in the `VulkanLibraryCommandBuffer` JNA interface.
