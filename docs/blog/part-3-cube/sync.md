---
title: The Render Loop
---

## Overview

Before we wrap up this phase of development we will finally implement a proper _render loop_.

Vulkan is designed to be multi-threading friendly:
- Work submitted to a queue is executed asynchronously, e.g. rendering a frame.
- Most Vulkan API methods can be invoked on multiple threads, e.g. recording a command buffer.

To ensure that operations occur in the correct order Vulkan provides several synchronisation mechanisms:
- a _semaphore_ that can synchronise operations within or across work queues.
- a _fence_ synchronises between the host and the GPU, i.e. between the host and rendering operations.
- pipeline barriers

We will start with a temporary solution to gracefully terminate the application via the keyboard (rather than having to bodge a loop counter or use the IDE).

Note that during this chapter the demo application will start generating validation errors (e.g. attempting to re-use command buffers that are not in the correct state) which we will address as they arise.

---

## Graceful Exit

To stop the render loop we create a simple GLFW keyboard listener that toggles a flag when the ESCAPE key is pressed:

```java
final AtomicBoolean running = new AtomicBoolean(true);

final KeyListener listener = (ptr, key, scancode, action, mods) -> {
    if(key == 256) {
        running.set(false);
    }
};
```

The listener is then attached to the desktop window:

```java
dev.library().glfwSetKeyCallback(window.handle(), listener);
```

We can now replace the nasty loop we implemented in the previous chapter with the following:

```java
while(running.get()) {
    // Update rotation
    ...
}
```

Note this is temporary code that will be replaced with a more comprehensive input-event handling solution in a future chapter.

---

## Render Loop

Up until now we have basically cut-and-pasted the render loop for each demo (always a sign that we're doing it wrong) so we will factor out the current code into a reusable class.

### Runner Class

The new _runner_ class composes the various components that collaborate in the render loop:

```java
public class Runner {
    private final Swapchain swapchain;
    private final Queue queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final Frame[] frames;

    public Runner(Swapchain swapchain, IntFunction<Frame> factory, Queue queue) {
        this.swapchain = notNull(swapchain);
        this.queue = notNull(queue);
        this.frames = IntStream.range(0, swapchain().views().size()).mapToObj(factory::apply).toArray(Frame[]::new);
    }
}
```

The `Frame` class factors out the logic for submitting render command and updating the application state:

```java
public interface Frame {
    /**
     * Renders the next frame.
     * @param view Swapchain image
     */
    void render(View view);

    /**
     * Updates state after a frame has been rendered.
     * @return Whether to continue execution of this runner
     */
    void update();
}
```

The constructor of the runner invokes the _factory_ to create a frame instance for each swapchain image.

We next add the render loop itself:

```java
public final void run() {
    while(running.get()) {
        frame();
    }
}
```

The `frame()` method essentially 'inverts' the existing render loop code using the new _frame_ class:

```java
protected void frame() {
    // Acquire next swapchain image
    final int index = swapchain.acquire();

    // Render frame
    final Frame frame = frames[index];
    final View view = swapchain.views().get(index);
    frame.render(view);

    // Present frame
    swapchain.present(queue);

    // Update application logic
    frame.update();
}
```

The final piece of functionality is a method to exit the loop:

```java
public final void stop() {
    running.set(false);
}
```

### Integration

We can now remove the existing render loop and add a _frame_ implementation for the demo:

```java
final IntFunction<Frame> factory = idx -> new Frame() {
    @Override
    public void render(View view) {
        new Work.Builder()
            .add(commands.get(idx))
            .build()
            .submit();
    }

    @Override
    public void update() {
        // Handle input events
        desktop.poll();

        // Update view matrix
        ...

        // TODO
        dev.waitIdle();
    }
};
```

Next we instantiate the render loop:

```java
final Runner runner = new Runner(swapchain, factory, dev.queue(present));
```

We will also need to re-implement the keyboard handler and remove the superfluous flag (which is now a property of the runner):

```java
final KeyListener listener = (ptr, key, scancode, action, mods) -> {
    if(key == 256) {
        runner.stop();
    }
};
```

Finally we start the render loop:

```java
// Start render loop
runner.run();

// Wait for pending work to complete
presentQueue.waitIdle();
runner.destroy();
```

All this work does not change the functionality of the demo but we now have a render loop implementation that we can re-use or extend in future applications.

---

## Semaphores

We will first synchronise the process of acquiring and rendering a frame using two semaphores:
1. one to signal when a swapchain image has been acquired and is ready for rendering.
2. another to signal when a frame has been rendered and is ready for presentation.

### Semaphore Class

A semaphore has no functionality so we implement it as a factory method on the logical device:

```java
public class Semaphore extends AbstractVulkanObject {
    private Semaphore(Pointer handle) {
        super(handle, LogicalDevice.this, lib::vkDestroySemaphore);
    }
}

public Semaphore semaphore() {
    final VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
    final PointerByReference handle = lib.factory().pointer();
    VulkanLibrary.check(lib.vkCreateSemaphore(this.handle(), info, null, handle));
    return new Semaphore(handle.getValue());
}
```

Next we refactor the swapchain to signal a semaphore when the next frame has been acquired:

```java
public int acquire(Semaphore semaphore, Fence fence) {
    check(device().library().vkAcquireNextImageKHR(device().handle(), this.handle(), Long.MAX_VALUE, handle(semaphore), handle(fence), index));
    return index.getValue();
}
```

The semaphore and fence (which we will cover later in this chapter) are both optional so we add the `handle()` helper to extract the handle if it is not `null`.  However note that the API expects **either** a semaphore, or a fence, or both, so we also add an invariant test in the `acquire()` method (this is one of the validation errors that we are receiving).

Finally the presentation method is refactored to wait on a semaphore that signals when the frame is ready to be presented:

```java
public void present(Queue queue, Set<Semaphore> semaphores) {
    ...
    // Populate wait semaphores
    info.waitSemaphoreCount = semaphores.size();
    info.pWaitSemaphores = Handle.toPointerArray(semaphores);
    ...
}
```

### Signalling

Semaphores synchronise operations across queues so we next refactor the `Work` class by adding two new data structures for _wait_ and _signal_ semaphores:

```java
public static class Builder {
    private final Collection<Pair<Semaphore, Integer>> wait = new ArrayList<>();
    private final Set<Semaphore> signal = new HashSet<>();
}
```

Note that a _wait_ semaphore also has an associated `VkPipelineStageFlag` bit-mask which we compose using an Apache commons `Pair` object (we could have created a simple record but this is easier).

We add a new builder method to register a _wait_ semaphore:

```java
public Builder wait(Semaphore semaphore, Set<VkPipelineStageFlag> stages) {
    final var entry = ImmutablePair.of(semaphore, IntegerEnumeration.mask(stages));
    wait.add(entry);
    return this;
}
```

And another for a _signal_ semaphore:

```java
public Builder signal(Semaphore semaphore) {
    signal.add(semaphore);
    return this;
}
```

Finally we modify the build method to add the wait semaphores in the work descriptor:

```java
if(!wait.isEmpty()) {
    // Populate wait semaphores
    final var semaphores = wait.stream().map(Pair::getLeft).collect(toList());
    info.waitSemaphoreCount = wait.size();
    info.pWaitSemaphores = Handle.toPointerArray(semaphores);
    ...
```

The pipeline stage bit-mask for each wait semaphore is actually a _separate_ field in the descriptor:

```java
    ...
    // Populate pipeline stage flags (which for some reason is a pointer to an integer array)
    final int[] stages = wait.stream().map(Pair::getRight).mapToInt(Integer::intValue).toArray();
    final Memory mem = new Memory(stages.length * Integer.BYTES);
    mem.write(0, stages, 0, stages.length);
    info.pWaitDstStageMask = mem;
}
```

The `pWaitDstStageMask` field **must** have the same length and order as the wait semaphores, hence the semaphore-mask pairs.

We also add the signal semaphores to the `pSignalSemaphores` field in the descriptor in the usual manner.

### Frames In-Flight

There are still several issues with our implementation even if we add semaphores to the render loop:
- The `update()` method waits on the device to complete all pending work before proceeding to the next frame.
- The render loop is essentially single-threaded for one frame.

To fully utilise the multi-threaded nature of the pipeline we will:
1. introduce the notion of multiple _in-flight frames_ executed in parallel.
2. implement sub-pass dependencies (which we skipped back in the swapchain chapter).

To track the state of an in-flight frame during rendering we add the following local class to the `Runner` that composes the synchronisation state of that frame:

```java
public static final class FrameState {
    private final Semaphore ready, finished;

    FrameState(LogicalDevice dev) {
        this.ready = dev.semaphore();
        this.finished = dev.semaphore();
    }
}
```

We create an array of in-flight frames in the constructor:

```java
public class Runner {
    private final FrameState[] states;
    private int current;
    ...

    public Runner(Swapchain swapchain, int size, IntFunction<Frame> factory, Queue queue) {
        ...
        this.states = Stream.generate(() -> new FrameState(swapchain.device())).limit(size).toArray(FrameState[]::new);
    }
}
```

The _size_ parameter specifies the number of in-flight frames (which does not necessarily have to be the same as the number of swapchain images).

The render loop now iterates through the array for each frame to be rendered:

```java
protected void frame() {
    // Start next in-flight frame
    final FrameState state = states[current];

    // Acquire next swapchain image
    final int index = swapchain.acquire(state.ready());

    // Render frame
    final Frame frame = frames[index];
    final View view = swapchain.views().get(index);
    frame.render(state, view);

    // Present frame
    swapchain.present(queue, Set.of(state.finished()));

    // Update application logic
    frame.update();

    // Select next in-flight frame
    if(++current >= frames.length) {
        current = 0;
    }
}
```

Note that we now have **two** arrays:
1. The array of `frames` indexed by the `index` of the swapchain image (returned by the `acquire()` method).
2. The `states` array indexed by the `current` in-flight frame (which is cycled at the end of the loop).

> The names are slightly over-loaded but they will have to do.

Lastly we modify `Frame` to accept the in-flight frame state so that the demo can synchronise the render operation:

```java
public void render(FrameState state, View view) {
    new Work.Builder()
        .add(commands.get(idx))
        .wait(state.ready(), VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
        .signal(state.finished())
        .build();
}
```

The `VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT` is a special-case pipeline stage that indicates that the semaphore should wait until the **entire** pipeline has completed execution.

The semaphores are released after use:

```java
public class Runner {
    public final class FrameState {
        private void destroy() {
            ready.destroy();
            finished.destroy();
        }
    }

    public void destroy() {
        for(FrameState f : frames) {
            f.destroy();
        }
    }
}
```

### Sub-Pass Dependencies

The render pass automatically manages image layout transitions, however until now we have forced the demo to wait before rendering and presenting a frame.
To effectively utilise the pipeline we need to configure the _dependencies_ between the sub-passes.
Note that although we currently only have a single sub-pass there is an _implicit_ sub-pass at the start and end of the overall render pass.

To configure the sub-pass dependencies we add a new nested builder to the render pass class:

```java
public class RenderPass {
    public static class Builder {
        private final List<DependencyBuilder> dependencies = new ArrayList<>();
    }
    
    ...

    public DependencyBuilder dependency(int src, int dest) {
        return new DependencyBuilder(src, dest);
    }
}
```

A sub-pass _dependency_ is comprised of configuration for a _source_ and a _destination_ sub-pass.

```java
public class DependencyBuilder {
    public class Dependency {
        ...
    }
    
    private final Dependency src, dest;

    private DependencyBuilder(int src, int dest) {
        this.src = new Dependency(src);
        this.dest = new Dependency(dest);
    }

    public Dependency source() {
        return src;
    }

    public Dependency destination() {
        return dest;
    }

    public Builder build() {
        dependencies.add(this);
        return Builder.this;
    }
}
```

The source and destination are essentially the same data so we create a single object that represents both (modifier methods omitted for brevity):

```java
public class Dependency {
    private final int index;
    private final Set<VkPipelineStageFlag> stages = new HashSet<>();
    private final Set<VkAccessFlag> access = new HashSet<>();
}
```

As usual we implement a `populate()` method to configure a dependency:

```java
private void populate(VkSubpassDependency dep) {
    // Populate source
    dep.srcSubpass = src.index;
    dep.srcStageMask = src.stages();
    dep.srcAccessMask = src.access();

    // Populate destination
    dep.dstSubpass = dest.index;
    dep.dstStageMask = dest.stages();
    dep.dstAccessMask = dest.access();
}
```

And we modify the parent build method to add the array of dependencies to the descriptor for the render pass:

```java
public RenderPass build() {
    ...

    // Add dependencies
    info.dependencyCount = dependencies.size();
    info.pDependencies = StructureCollector.toPointer(VkSubpassDependency::new, dependencies, DependencyBuilder::populate);
    
    ...
}
```

Finally we add a constant for the special case index of the implicit sub-passes:

```java
public class RenderPass {
    /**
     * Index of the implicit sub-pass before or after the render pass.
     */
    public static final int VK_SUBPASS_EXTERNAL = (~0);
}
```

> The weird looking `(~0)` value is copied from the Vulkan header.  The value is actually `-1` but we retain the above for consistency (apparently this is legal Java!)

### Integration

With all the above in place we can configure the demo to register a sub-pass dependency:

```java
final RenderPass pass = new RenderPass.Builder(dev)
    .attachment()
        ....
    .subpass()
        .colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
        .build()
    .dependency(RenderPass.VK_SUBPASS_EXTERNAL, 0)
        .source().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
        .destination().stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
        .destination().access(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
        .build()
    .build();
```

Here we create a dependency between the implicit starting sub-pass and our single sub-pass (index zero) that specifies the following:
1. The `source` clause tells the render pass to wait for the colour attachment of the swapchain image to be available.
2. Our sub-pass (the `destination`) waits until the colour attachment is ready for writing (rendering) before it is executed.

We also change the pipeline stage of the _wait_ semaphore in the `update()` method to `VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT` rather than `VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT` meaning the render task waits on the swapchain image rather than the entire pipeline.

---

## Fences

Although all this new functionality optimises use of the pipeline and render pass, if we remove the `waitIdle()` call in the `update()` method we still get validation errors - the application is constantly queueing up more render tasks that are trying to reuse the same command buffers.

### Fence Class

Vulkan provides another synchronisation mechanism called a _fence_ that allows application code to wait for a signalled state (whereas a semaphore only synchronises between queues).

The _fence_ domain object is relatively simple:

```java
public class Fence extends AbstractVulkanObject {
    public static Fence create(LogicalDevice dev, VkFenceCreateFlag... flags) {
        // Init descriptor
        final VkFenceCreateInfo info = new VkFenceCreateInfo();
        info.flags = IntegerEnumeration.mask(flags);

        // Create fence
        final VulkanLibrary lib = dev.library();
        final PointerByReference handle = lib.factory().pointer();
        check(lib.vkCreateFence(dev.handle(), info, null, handle));

        // Create domain object
        return new Fence(handle.getValue(), dev);
    }

    Fence(Pointer handle, LogicalDevice dev) {
        super(handle, dev, dev.library()::vkDestroyFence);
    }
}
```

The following method is used to synchronise the application with the graphics hardware:

```java
/**
 * Waits for a group of fences.
 * @param dev           Logical device
 * @param fences        Fences
 * @param all           Whether to wait for all or any fence
 * @param timeout       Timeout (ms)
 * @throws VulkanException if the API method fails
 */
public static void wait(LogicalDevice dev, Collection<Fence> fences, boolean all, long timeout) {
    final Pointer[] array = Handle.toArray(fences);
    final VulkanLibrary lib = dev.library();
    check(lib.vkWaitForFences(dev.handle(), array.length, array, VulkanBoolean.of(all), timeout));
}
```

After use a fence can be reset back to the unsignalled state:

```java
/**
 * Resets a group of fences.
 * @param dev           Logical device
 * @param fences        Fences to reset
 * @throws VulkanException if the fences cannot be reset
 */
public static void reset(LogicalDevice dev, Collection<Fence> fences) {
    final Pointer[] array = Handle.toArray(fences);
    final VulkanLibrary lib = dev.library();
    check(lib.vkResetFences(dev.handle(), array.length, array));
}
```

We also provide convenience methods to reset or wait on a single fence:

```java
public void reset() {
    reset(device(), Set.of(this));
}

public void waitReady() {
    wait(device(), Set.of(this), true, Long.MAX_VALUE);
}
```

The application can also programatically query the state of a fence:

```java
private static final int SIGNALLED = VkResult.VK_SUCCESS.value();
private static final int NOT_SIGNALLED = VkResult.VK_NOT_READY.value();

public boolean signalled() {
    final LogicalDevice dev = this.device();
    final int result = dev.library().vkGetFenceStatus(dev.handle(), this.handle());
    if(result == SIGNALLED) {
        return true;
    }
    else
    if(result == NOT_SIGNALLED) {
        return false;
    }
    else {
        throw new VulkanException(result);
    }
}
```

### Integration

First we add a fence to the in-flight frame state object:

```java
public final class FrameState {
    ...
    private final Fence fence;

    FrameState(Frame frame) {
        ...
        this.fence = Fence.create(dev, VkFenceCreateFlag.VK_FENCE_CREATE_SIGNALED_BIT);
    }
}
```

Note that the fence is initialised to the signalled state, the reason for this will become apparent shortly.

The render loop is modified to wait for the fence to be signalled before:
1. acquiring the next swapchain image.
2. and rendering the frame.

We also pass the `fence` to the swapchain `acquire()` method resulting in the following:

```java
protected void frame() {
    // Start next frame
    final FrameState state = frames[current];
    state.waitFence();

    // Acquire next swapchain image
    // TODO - using same fence for both cases, does this work? need 1. for frame itself and 2. for image being rendered (pass null to acquire)?
    final int index = swapchain.acquire(state.ready(), state.fence());
    state.waitFence();
    
    ...
}
```

The `waitFence()` helper method combines waiting for the fence to be signalled and then resets it:

```java
private void waitFence() {
    fence.waitReady();
    fence.reset();
}
```

The fence is passed to the `submit()` method when we render a frame:

```java
public static void submit(List<Work> work, Fence fence) {
    ...
    final VulkanLibrary lib = queue.device().library();
    check(lib.vkQueueSubmit(queue.handle(), array.length, array, handle(fence)));
}
```

We can finally remove the `waitIdle()` call in the `update()` method and should no longer see validation errors when we run the demo.

### ???

issue
GLFW listeners being garbage collected
separate thread with weak ref to listener -> is released!!!
TODO - solution

---

## XXX

*** REFACTOR TO FRAME PER SWAPCHAIN IMAGE - BUFFER, DESCRIPTOR SETS, FRAME BUFFER, ETC ***

---

## Summary

In this chapter we:

- Factored out the render loop so that it can be reused in future demos and applications.

- Added synchronisation to ensure that the frame rendering is correctly synchronised within the pipeline and the work queues.

- Implemented a temporary means of gracefully terminating the demo.
