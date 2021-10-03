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
3. end the render pass

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

### Command Interface

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

### Command Pool

We start with the command pool which is created using a static factory:

```java
class Pool extends AbstractVulkanObject {
    /**
     * Creates a command pool for the given queue.
     * @param queue     Work queue
     * @param flags     Flags
     */
    public static Pool create(Queue queue, VkCommandPoolCreate... flags) {
        // Init pool descriptor
        final VkCommandPoolCreateInfo info = new VkCommandPoolCreateInfo();
        info.queueFamilyIndex = queue.family().index();
        info.flags = IntegerEnumeration.mask(flags);

        // Create pool
        final LogicalDevice dev = queue.device();
        final VulkanLibrary lib = dev.library();
        final PointerByReference pool = lib.factory().pointer();
        check(lib.vkCreateCommandPool(dev.handle(), info, null, pool));

        // Create pool
        return new Pool(pool.getValue(), queue);
    }

    private final Queue queue;

    /**
     * Constructor.
     * @param handle        Command pool handle
     * @param queue         Work queue
     */
    private Pool(Pointer handle, Queue queue) {
        super(handle, queue.device(), queue.device().library()::vkDestroyCommandPool);
        this.queue = notNull(queue);
    }
}
```

To allocate one-or-more command buffers we add the following factory:

```java
/**
 * Allocates a number of command buffers from this pool.
 * @param num           Number of buffers to allocate
 * @param primary       Whether primary or secondary
 * @return Allocated buffers
 */
public List<Buffer> allocate(int num, boolean primary) {
    // Init descriptor
    final var info = new VkCommandBufferAllocateInfo();
    info.level = primary ? VkCommandBufferLevel.PRIMARY : VkCommandBufferLevel.SECONDARY;
    info.commandBufferCount = num;
    info.commandPool = this.handle();

    // Allocate buffers
    final LogicalDevice dev = queue.device();
    final VulkanLibrary lib = dev.library();
    final Pointer[] handles = lib.factory().pointers(num);
    check(lib.vkAllocateCommandBuffers(dev.handle(), info, handles));

    // Create buffers
    final var list = Arrays
            .stream(handles)
            .map(ptr -> new Buffer(ptr, this))
            .collect(toList());

    // Register buffers
    buffers.addAll(list);

    return list;
}
```

Note that the pool tracks the buffers that have been allocated:

```java
class Pool {
    private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

    ...

    @Override
    public synchronized void destroy() {
        buffers.clear();
        super.destroy();
    }
}
```

We also add the following methods to release some or all of the allocated buffers:

```java
/**
 * Frees <b>all</b> command buffers in this pool.
 */
public synchronized void free() {
    free(buffers);
    buffers.clear();
}

/**
 * Releases a set of command buffers back to this pool.
 * @param buffers Buffers to release
 */
private void free(Collection<Buffer> buffers) {
    final LogicalDevice dev = super.device();
    dev.library().vkFreeCommandBuffers(dev.handle(), this.handle(), buffers.size(), Handle.toArray(buffers));
}
```

### Command Buffer

A command buffer is used to **record** a sequence of commands, i.e. the commands are not executed immediately but are submitted to a work queue for execution by the hardware.

We define a command buffer as follows:

```java
class Buffer implements NativeObject {
    /**
     * Buffer state.
     */
    private enum State {
        INITIAL,
        RECORDING,
        EXECUTABLE,
    }

    private final Handle handle;
    private final Pool pool;

    private State state = State.INITIAL;

    /**
     * Constructor.
     * @param handle        Command buffer handle
     * @param pool          Parent pool
     */
    private Buffer(Pointer handle, Pool pool) {
        this.handle = new Handle(handle);
        this.pool = notNull(pool);
    }
}
```

The _state_ is used to track whether the buffer has been recorded (with the enumeration names being based on the Vulkan documentation).

The `begin` method is used to start recording:

```java
/**
 * Starts command buffer recording.
 * @param flags Flags
 * @throws IllegalStateException if this buffer is not ready for recording
 */
public Buffer begin(VkCommandBufferUsage... flags) {
    // Check buffer can be recorded
    if(state != State.INITIAL) throw new IllegalStateException(...);

    // Start buffer
    final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
    info.flags = IntegerEnumeration.mask(flags);
    info.pInheritanceInfo = null;
    check(lib.vkBeginCommandBuffer(handle, info));

    // Start recording
    state = State.RECORDING;
    return this;
}
```

Specific commands can then be added to the sequence:

```java
/**
 * Adds a command.
 * @param cmd Command
 * @throws IllegalStateException if this buffer is not recording
 */
public Buffer add(Command cmd) {
    if(state != State.RECORDING) throw new IllegalStateException(...);
    cmd.execute(lib, handle);
    return this;
}
```

Finally the `end` method finishes recording:

```java
/**
 * Ends recording.
 * @throws IllegalStateException if this buffer is not recording
 * @throws IllegalArgumentException if no commands have been recorded
 */
public Buffer end() {
    if(state != State.RECORDING) throw new IllegalStateException(...);
    check(lib.vkEndCommandBuffer(handle));
    state = State.EXECUTABLE;
    return this;
}
```

Notes:
- We could have introduced a further recorder class but it hardly seems worthwhile.
- Command buffers are managed by the pool and are not explicitly destroyed.

---

## Command Implementation

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

## Submitting Work

Submitting a command buffer to a work queue involves populating a descriptor of the work to be performed comprising a _batch_ of command buffers and synchronisation declarations.
We will ignore synchronisation until a future chapter as it is not needed for the triangle demo.

We define a _work_ class with the obligatory builder:

```java
public class Work {
    private final VkSubmitInfo info;
    private final Queue queue;

    /**
     * Constructor.
     * @param info          Descriptor for this work batch
     * @param queue         Work queue
     */
    public Work(VkSubmitInfo info, Queue queue) {
        this.info = notNull(info);
        this.queue = notNull(queue);
    }

    /**
     * @return Work queue for this batch
     */
    public Queue queue() {
        return queue;
    }

    public static class Builder {
        private final List<Command.Buffer> buffers = new ArrayList<>();
        private Queue queue;
        
        ...

        public Work build() {
            if(buffers.isEmpty()) throw new IllegalArgumentException("No command buffers specified");
            final VkSubmitInfo info = new VkSubmitInfo();
            info.commandBufferCount = buffers.size();
            info.pCommandBuffers = Handle.toArray(buffers);
            return new Work(info, queue);
        }
    }
}
```

The target queue for the work is determined from the command buffer when it is added to the batch:

```java
public Builder add(Command.Buffer buffer) {
    // Check buffer has been recorded
    if(!buffer.isReady()) throw new IllegalStateException("Command buffer has not been recorded: " + buffer);

    // Initialise queue
    final Queue q = buffer.pool().queue();
    if(queue == null) {
        queue = q;
    }
    else
    if(queue.family() != q.family()) {
        throw new IllegalArgumentException("Command buffers must all have the same queue: " + buffer);
    }

    // Add buffer to this work
    buffers.add(buffer);

    return this;
}
```

Note that each work submission must be allocated from the same queue family.

Finally a collection of work batches is submitted for execution to the queue as follows:

```java
public static void submit(List<Work> work, Fence fence) {
    // Determine submission queue and check all batches have the same queue
    Check.notEmpty(work);
    final Queue queue = work.get(0).queue;
    if(!work.stream().map(e -> e.queue).allMatch(queue::equals)) {
        throw new IllegalArgumentException(...);
    }

    // Convert descriptors to array
    final var array = work.stream().map(e -> e.info).toArray(VkSubmitInfo[]::new);

    // Submit work
    final VulkanLibrary lib = queue.device().library();
    check(lib.vkQueueSubmit(queue.handle(), array.length, array, null));
}
```

---

## Integration

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

We also release the various Vulkan objects after the 'loop' has terminated:

```java
// Wait for pending work to complete
present.waitIdle();

// Release render pass
buffers.forEach(FrameBuffer::destroy);
pass.destroy();

// Release pipeline
vert.destroy();
frag.destroy();
pipeline.destroy();

// Release swapchain
chain.destroy();

// Destroy window
surface.destroy();
window.destroy();
desktop.destroy();

// Destroy device
pool.destroy();
dev.destroy();
instance.destroy();
```

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
