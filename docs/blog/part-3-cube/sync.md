---
title: The Render Loop
---

## Overview

Before we wrap up this phase of development we will finally implement a proper _render loop_ with synchronisation across work queues and between the host and the GPU.

We will also implement a temporary solution to allow us to gracefully terminate the application via the keyboard (rather than having to bodge a loop counter or use the IDE).

---

## Graceful Exit

We will start with a simple GLFW keyboard listener that toggles a boolean flag when the ESCAPE key is pressed:

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

Up until now we have basically cut-and-pasted the render loop (which is always a sign that we're doing it wrong) so we will factor out the current code into a reusable class.

### Runner Class

The new _runner_ composes the various collaborating components that are used during rendering:

```java
public class Runner {
    private final Swapchain swapchain;
    private final Queue queue;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private final IntFunction<Frame> factory;

    public Runner(Swapchain swapchain, IntFunction<Frame> factory, Queue queue) {
        this.swapchain = notNull(swapchain);
        this.factory = notNull(factory);
        this.queue = notNull(queue);
    }
}
```

The _factory_ returns a _frame_ instance for a given swapchain image.

This new domain object encapsulates the steps of the loop that submit the render commands and update the application logic (polling the keyboard and updating the view matrix):

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

We next add the loop itself:

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
    final View view = swapchain.views().get(index);
    final Frame frame = factory.apply(index);
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
final Runner runner = new Runner(swapchain, 2, factory, dev.queue(present));
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
runner.run();
```

All this work does not change the functionality of the demo but we now have a render loop implementation that we can re-use or extend in future applications.

---

## Synchronisation

### Overview

Vulkan is designed to be multi-threading friendly:
- Work submitted to a queue is executed asynchronously, e.g. rendering a frame.
- Most Vulkan API methods can be invoked on multiple threads, e.g. recording a command buffer.

To ensure that operations occur in the correct order Vulkan provides several synchronisation mechanisms:
- a _semaphore_ that can synchronise operations within or across work queues.
- a _fence_ synchronises between the host and the GPU, i.e. between the host and rendering operations.
- pipeline barriers

### Semaphores

We will first synchronise the process of acquiring and rendering a frame using semaphores.

This new domain object has no functionality so we implement it as a factory method on the logical device:

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

Next we refactor the swapchain to use the new class when we acquire the next image:

```java
public int acquire(Semaphore semaphore, Fence fence) {
    check(device().library().vkAcquireNextImageKHR(device().handle(), this.handle(), Long.MAX_VALUE, handle(semaphore), handle(fence), index));
    return index.getValue();
}
```

The semaphore and fence (which we will cover later in this chapter) are both optional so we add the `handle()` helper to extract the handle if it is not `null`.  However note that the API expects **either** a semaphore or a fence, or both, so we also added an invariant test in the `acquire()` method (this is one of the validation errors that we are receiving).

The presentation method is also refactored to wait on one-or-more semaphores:

```java
public void present(Queue queue, Set<Semaphore> semaphores) {
    ...
    // Populate wait semaphores
    info.waitSemaphoreCount = semaphores.size();
    info.pWaitSemaphores = Handle.toPointerArray(semaphores);
    ...
}
```


SEMAPHORE CLASS
SWAPCHAIN REFACTOR
WORK REFACTOR
2 X SEMAPHORE -> RUNNER
INTEGRATION
TOP OF PIPE

SUBPASS DEPENDENCIES
INTEGRATION

FRAME STATE
FENCE
INTEGRATION

### 

---

## Summary

In this chapter we:

- Factored out the render loop so that it can be reused in future demos and applications.

- Added synchronisation to ensure that the frame rendering is correctly synchronised within the pipeline and the work queues.

- Implemented a temporary means of gracefully terminating the demo.
