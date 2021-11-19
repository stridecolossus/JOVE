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
    void execute(VulkanLibrary lib, Buffer buffer);

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

The `Command` interface abstracts the signature of a Vulkan command where the arguments are always comprised of the Vulkan API and the command buffer.

We also create a new API to support the new domain objects:

```java
interface VulkanLibraryCommandBuffer {
    int vkCreateCommandPool(LogicalDevice device, VkCommandPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pCommandPool);
    int vkResetCommandPool(LogicalDevice device, Pool commandPool, int flags);
    void vkDestroyCommandPool(LogicalDevice device, Pool commandPool, Pointer pAllocator);

    int vkAllocateCommandBuffers(LogicalDevice device, VkCommandBufferAllocateInfo pAllocateInfo, Pointer[] pCommandBuffers);
    int vkResetCommandBuffer(Buffer commandBuffer, int flags);
    void vkFreeCommandBuffers(LogicalDevice device, Pool commandPool, int commandBufferCount, Buffer[] pCommandBuffers);

    int vkBeginCommandBuffer(Buffer commandBuffer, VkCommandBufferBeginInfo pBeginInfo);
    int vkEndCommandBuffer(Buffer commandBuffer);
}
```

### Command Pool

We start with the command pool:

```java
class Pool extends AbstractVulkanObject {
    private final Queue queue;

    private Pool(Pointer handle, LogicalDevice dev, Queue queue) {
        super(handle, dev);
        this.queue = notNull(queue);
    }

    @Override
    protected Destructor<Pool> destructor(VulkanLibrary lib) {
        return lib::vkDestroyCommandPool;
    }
}
```

A pool is created via a factory method:

```java
public static Pool create(LogicalDevice dev, Queue queue, VkCommandPoolCreateFlag... flags) {
    // Init pool descriptor
    var info = new VkCommandPoolCreateInfo();
    info.queueFamilyIndex = queue.family().index();
    info.flags = IntegerEnumeration.mask(flags);

    // Create pool
    VulkanLibrary lib = dev.library();
    PointerByReference pool = dev.factory().pointer();
    check(lib.vkCreateCommandPool(dev, info, null, pool));

    // Create pool
    return new Pool(pool.getValue(), dev, queue);
}
```

To allocate command buffers we add the following factory method:

```java
public List<Buffer> allocate(int num, boolean primary) {
    // Init descriptor
    var info = new VkCommandBufferAllocateInfo();
    info.level = primary ? VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
    info.commandBufferCount = oneOrMore(num);
    info.commandPool = this.handle();

    // Allocate buffers
    DeviceContext dev = super.device();
    VulkanLibrary lib = dev.library();
    Pointer[] handles = new Pointer[num];
    check(lib.vkAllocateCommandBuffers(dev, info, handles));
    
    ...
}
```

The handles of the newly allocated buffers are then transformed to the domain object:

```java
// Create buffers
List<Buffer> list = Arrays
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
    DeviceContext dev = super.device();
    dev.library().vkFreeCommandBuffers(dev, this, buffers.size(), NativeObject.toArray(buffers));
}
```

Finally the pool can also be reset which recycles resources and restores all allocated buffers to their initial state:

```java
public void reset(VkCommandPoolResetFlag... flags) {
    int mask = IntegerEnumeration.mask(flags);
    DeviceContext dev = super.device();
    check(dev.library().vkResetCommandPool(dev, this, mask));
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

The _state_ member track whether the buffer has been recorded or is ready for execution (the enumeration names are based on the Vulkan documentation).

The `begin` method starts the recording of a command sequence:

```java
public Buffer begin(VkCommandBufferUsageFlag... flags) {
    // Check buffer can be recorded
    if(state != State.INITIAL) throw new IllegalStateException(...);

    // Init descriptor
    var info = new VkCommandBufferBeginInfo();
    info.flags = IntegerEnumeration.mask(flags);

    // Start buffer recording
    VulkanLibrary lib = pool.device().library();
    check(lib.vkBeginCommandBuffer(this, info));

    // Start recording
    state = State.RECORDING;
    return this;
}
```

The sequence is then recorded by adding commands:

```java
public Buffer add(Command cmd) {
    if(state != State.RECORDING) throw new IllegalStateException(...);
    cmd.execute(pool.device().library(), this);
    return this;
}
```

Finally the `end` method completes recording:

```java
public Buffer end() {
    if(state != State.RECORDING) throw new IllegalStateException(...);
    VulkanLibrary lib = pool.device().library();
    check(lib.vkEndCommandBuffer(this));
    state = State.EXECUTABLE;
    return this;
}
```

We also add convenience mutators to reset or release a buffer back to the pool.

### Submission

Submitting tasks to a work queue involves populating a Vulkan descriptor comprising one-or-more command buffers with a synchronisation specification.  We defer synchronisation until later as it is not needed for the triangle demo.

We implement a new `Work` class that composes a work submission:

```java
public class Work {
    private final Pool pool;
    private final List<Buffer> buffers = new ArrayList<>();
}
```

And the obligatory builder to add command buffers:

```java
public Builder add(Buffer buffer) {
    // Check buffer has been recorded
    if(!buffer.isReady()) throw new IllegalStateException(...);

    // Check all work is submitted to the same queue family
    if(!matches(work, buffer.pool())) throw new IllegalArgumentException(...);

    // Add buffer to this work
    work.buffers.add(buffer);

    return this;
}
```

Although the work domain object is not necessarily dependant on the command pool it is more convenient for the code to have access to the pool (and the logical device).

All command buffers __must__ be submitted to the same queue family which is validated by the `matches` helper:

```java
private static boolean matches(Work work, Pool pool) {
    Family left = work.pool.queue().family();
    Family right = pool.queue().family();
    return left.equals(right);
}
```

We also add a convenience factory to create a work submission for a single command buffer:

```java
public static Work of(Buffer buffer) {
    Pool pool = buffer.pool();
    return new Builder(pool).add(buffer).build();
}
```

Work is submitted to the queue as a _batch_ of submissions (which again must all use the same queue family):

```java
public static void submit(List<Work> work) {
    // Validate
    Check.notEmpty(work);
    Pool pool = work.get(0).pool;
    if(!work.stream().allMatch(e -> matches(e, pool))) {
        throw new IllegalArgumentException(...);
    }

    // Populate array of submission descriptors
    VkSubmitInfo[] array = StructureHelper.array(work, VkSubmitInfo::new, Work::populate);

    // Submit work
    VulkanLibrary lib = pool.device().library();
    check(lib.vkQueueSubmit(pool.queue(), array.length, array, null));
}
```

Where the Vulkan descriptor for each work submission in the batch is populated by the following helper on the `Work` class:

```java
private void populate(VkSubmitInfo info) {
    info.commandBufferCount = buffers.size();
    info.pCommandBuffers = NativeObject.toArray(buffers);
}
```

Finally we add the API method to the existing device library:

```java
interface VulkanLibraryLogicalDevice {
    ...
    int vkQueueSubmit(Queue queue, int submitCount, VkSubmitInfo[] pSubmits, Handle fence);
}
```

---

## Integration

### Commands

We can now implement the specific commands required for the triangle demo.

We add the following factory method on the `FrameBuffer` class to begin rendering to that buffer:

```java
public Command begin() {
    // Create descriptor
    var info = new VkRenderPassBeginInfo();
    info.renderPass = pass.handle();
    info.framebuffer = this.handle();

    // Populate rendering area
    VkExtent2D ext = info.renderArea.extent;
    ext.width = extents.width();
    ext.height = extents.height();

    // Init clear values
    ...

    // Create command
    return (lib, buffer) -> lib.vkCmdBeginRenderPass(buffer, info, VkSubpassContents.INLINE);
}
```

For the moment we temporarily bodge a grey clear value for the colour attachment:

```java
VkClearValue clear = new VkClearValue();
clear.color = new VkClearColorValue();
clear.color.float32 = new float[]{0.3f, 0.3f, 0.3f, 1};
info.clearValueCount = 1;
info.pClearValues = clear;
```

Clear values will be more fully implemented in the [models](/JOVE/blog/part-4-models/model-loader) chapter.

Ending the render pass is defined as a constant since the command does not require any additional arguments:

```java
public static final Command END = (api, buffer) -> api.vkCmdEndRenderPass(buffer);
```

To bind the pipeline in the render sequence we add the following factory to the `Pipeline` class:

```java
public Command bind() {
    return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.GRAPHICS, this);
}
```

Finally we hard-code the draw command to render the triangle vertices:

```java
Command draw = (api, handle) -> api.vkCmdDraw(handle, 3, 1, 0, 0);
```

This specifies the three triangles vertices, in a single instance, both starting at index zero.  We will implement a proper builder for draw commands in a later chapter.

Finally we add the API methods for the new commands:

```java
interface VulkanLibraryRenderPass {
    ...
    void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents);
    void vkCmdEndRenderPass(Buffer commandBuffer);
    void vkCmdDraw(Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance);
}
```

### Rendering Sequence

In the demo application we modify the devices configuration class by replacing the two queues with command pools:

```java
class DeviceConfiguration {
    private static Pool pool(LogicalDevice dev, Selector selector) {
        Queue queue = dev.queue(selector.family());
        return Pool.create(dev, queue);
    }
    
    @Bean
    public Pool graphics(LogicalDevice dev) {
        return pool(dev, graphics);
    }
    
    @Bean
    public Pool presentation(LogicalDevice dev) {
        return pool(dev, presentation);
    }
}
```

Next we add a new configuration class for the rendering sequence based on the pseudo-code above:

```java
@Configuration
public class RenderConfiguration {
    @Bean
    public static Buffer sequence(FrameBuffer frame, Pipeline pipeline, Pool graphics) {
        Command draw = ...
        return graphics
            .allocate()
            .begin()
                .add(frame.begin())
                .add(pipeline.bind())
                .add(draw)
                .add(FrameBuffer.END)
            .end();
    }
}
```

Note that the bean for the injected command pool is disambiguated by _name_ (alternatively we could use an explicit `@Qualifier` annotation).

To finally display the triangle we need to invoke presentation and rendering.

We add another bean which starts a Spring `ApplicationRunner` once the container has been initialised:

```java
@Bean
public static ApplicationRunner render(Swapchain swapchain, Buffer render) {
    return args -> { ... }
}
```

Although we have a double-buffer swapchain and many of the components required to implement a fully threaded render loop, for the moment we bodge a single frame:

```java
// Start next frame
swapchain.acquire();

// Render frame
Work.of(render).submit();

// Wait for frame
Pool pool = render.pool();
pool.waitIdle();

// Present frame
swapchain.present(pool.queue());

// Wait...
Thread.sleep(1000);
```

Notes:

* The `acquire` method will generate a Vulkan error since we are not providing any synchronisation parameters.

* We briefly block execution at the end of the 'loop' so we have a chance of seeing the results (if there are any).
 
* Obviously this is temporary code just sufficient to test this first demo - we will be implementing a proper render loop in future chapters.

* In particular the window will be non-functional, i.e. cannot be moved or closed.

### Conclusion

If all goes well when we run the demo we should see the following:

![Triangle](triangle.png)

Viola!

All that for a triangle?

There are a couple of gotchas that could result in staring at a blank screen:

* The triangle vertices in the vertex shader are ordered counter-clockwise which _should_ be the default winding order - although not covered in this part of the demo the _rasterizer_ pipeline stage may need to be configured explicitly (or culling switched off altogether).

* The arguments for the hard-coded drawing command are all integers and can easily be accidentally transposed.

---

## Summary

In the final chapter for this phase of development we implemented:

* The command pool and buffer.

* A mechanism for submitting work to the hardware.

* Specific commands to support rendering.

* A crude render 'loop' to display the triangle.

