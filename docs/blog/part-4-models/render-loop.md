---
title: The Render Loop and Synchronisation
---

## Overview

Before we start the next demo application there are several issues with the existing, crude render loop we have implemented so far.

In no particular order:

* The rendering code is completely single-threaded (by blocking the work queues).

* The code mixes up the Vulkan rendering process, the application logic (updating the rotation), and the code to control the loop (the dodgy timer).

* The window event queue is blocked.

* There is no mechanism to terminate the application (other than the timer code or force-quitting the process).

In this chapter we will address these issues by implementing the following:

* New reusable components to separate the rendering process and the application logic.

* Synchronisation to fully utilise the multi-threaded nature of the Vulkan pipeline.

* A GLFW keyboard handler to gracefully exit the application.

A complication of particular importance is that GLFW event processing __must__ be performed on the main application thread.  We cannot (for example) wrap up the application loop as a separate thread or use the task executor framework.  Note that GLFW does not return any errors or exceptions if a thread-safe method is not invoked on the main thread.  See the [GLFW thread documentation](https://www.glfw.org/docs/latest/intro.html#thread_safety) for more details.

---

## Refactoring

### Render Loop

We start by refactoring the existing render loop:
* Remove the `while` loop and the timer logic.
* Factor out the Vulkan code to a new component.
* Factor out the rotation update logic to a separate component.

We create a new component for the remaining rendering code (the acquire-render-present steps):

```java
public class RenderLoop implements Runnable {
    private final Swapchain swapchain;
    private final List<Buffer> buffers;
    private final Queue presentation;

    @Override
    public void run() {
        ...
    }
}
```

And we refactor the existing render loop bean accordingly:

```java
return new RenderLoop(swapchain, buffers, presentation.queue());
```

Next we create a new component in the main class that encapsulates the rotation logic:

```java
@Bean
public static Runnable update(Matrix matrix, VulkanBuffer uniform, ApplicationConfiguration cfg) {
    long period = cfg.getPeriod();
    long start = System.currentTimeMillis();
    return () -> {
        ...
        uniform.load(m);
    };
}
```

### Application 

Note that the newly refactored components are `Runnable` objects, we compose these _tasks_ into the following new class that encapsulates the application thread:

```java
public class Application {
    private final List<Runnable> tasks;
    private final AtomicBoolean running = new AtomicBoolean();

    public boolean isRunning() {
        return running.get();
    }
}
```

The application loop iterates through the tasks:

```java
public void run() {
    running.set(true);
    while(isRunning()) {
        tasks.forEach(Runnable::run);
    }
}
```

The loop is terminated by the following method:

```java
public void stop() {
    if(!isRunning()) throw new IllegalStateException(...);
    running.set(false);
}
```

### Graceful Exit

To gracefully exit the application loop we add a GLFW key listener in the main class that stops the application when the ESCAPE key is pressed:

```java
@Bean
public static KeyListener listener(Window window, Application app) {
    KeyListener listener = (ptr, key, scancode, action, mods) -> {
        if(key == 256) {
            app.stop();
        }
    };
    
    ...
    
    return listener;
}
```

The listener is a GLFW callback defined as follows:

```java
interface DesktopLibraryDevice {
    public interface KeyListener extends Callback {
        /**
         * Notifies a key event.
         * @param window            Window
         * @param key               Key index
         * @param scancode          Key scan code
         * @param action            Key action
         * @param mods              Modifiers
         */
        void key(Pointer window, int key, int scancode, int action, int mods);
    }
}
```

Finally the listener is registered on the window:

```
Desktop desktop = window.desktop();
desktop.library().glfwSetKeyCallback(window.handle(), listener);
```

Notes:

* We will replace this code with a more comprehensive event handling framework later.

* For the moment we hard-code the ESCAPE key.

* Although not used elsewhere in the application we add the listener to the container since GLFW seems to unregister a garbage-collected listener.

### Integration

We first instantiate the application in the main class:

```java
@Bean
public static Application application(List<Runnable> tasks) {
    return new Application(tasks);
}
```

Spring handily creates a list of the tasks for us from __all__ the instances of `Runnable` registered in the container.

We add a new local component to start the loop:

```java
@Component
static class ApplicationLoop implements CommandLineRunner {
    private final Application app;
    private final LogicalDevice dev;

    public ApplicationLoop(Application app, LogicalDevice dev) {
        this.app = app;
        this.dev = dev;
    }

    @Override
    public void run(String... args) throws Exception {
        app.run();
        dev.waitIdle();
    }
}
```

Notes:

* Here we use `@Component` which defines a class to be instantiated by the container.

* We also move the `waitIdle` call after the loop has finished to correctly cleanup the Vulkan resources.

Finally we add a further task to process the GLFW window event queue:

```java
class DesktopConfiguration {
    @Bean
    public static Runnable poll(Desktop desktop) {
        return desktop::poll;
    }
}
```

Again note that the GLFW listener and polling must be performed on the main application thread.

When we now run the demo we should finally be able to move the window and close the application gracefully.

---

## Synchronisation

### Semaphores

So far we have avoided synchronisation by blocking the device after rendering and presentation of a frame.  However Vulkan is designed to be multi-threaded from the ground up and provides several synchronisation mechanisms that can be used by the application.

A _semaphore_ is the simplest of these used to synchronise operations within or across work queues.

The semaphore class is trivial since there is no public functionality:

```java
public class Semaphore extends AbstractVulkanObject {
    private Semaphore(Pointer handle, DeviceContext dev) {
        super(handle, dev);
    }

    @Override
    protected Destructor<Semaphore> destructor(VulkanLibrary lib) {
        return lib::vkDestroySemaphore;
    }
}
```

A semaphore is created using a factory method:

```java
public static Semaphore create(DeviceContext dev) {
    VkSemaphoreCreateInfo info = new VkSemaphoreCreateInfo();
    VulkanLibrary lib = dev.library();
    PointerByReference handle = lib.factory().pointer();
    VulkanLibrary.check(lib.vkCreateSemaphore(dev, info, null, handle));
    return new Semaphore(handle.getValue(), dev);
}
```

We create two semaphores in the render loop to signal the following conditions:

1. The acquired swapchain image is `available` for rendering.

2. The frame has been rendered and is `ready` for presentation.

The semaphores are instantiated in the constructor:

```java
private final Semaphore available, ready;

public RenderLoop(Swapchain swapchain, ...) {
    DeviceContext dev = swapchain.device();
    available = Semaphore.create(dev);
    ready = Semaphore.create(dev);
}
```

We pass the _available_ semaphore to the acquire method which is refactored to pass it to the API method:

```java
int index = swapchain.acquire(available);
```

The _ready_ semaphore is passed to the presentation method:

```java
swapchain.present(presentation, Set.of(ready));
```

And the method is modified to populate the relevant member of the descriptor for the presentation operation:

```java
public void present(Queue queue, Set<Semaphore> semaphores) {
    ...
    info.waitSemaphoreCount = semaphores.size();
    info.pWaitSemaphores = NativeObject.toArray(semaphores);
    ...
}
```

Finally we release the semaphores when the render loop object is destroyed:

```java
public void close() {
    available.close();
    ready.close();
}
```

If we run the demo as it now stands (with the work queue blocking still present) we will get additional errors because the semaphores are never signalled.

Also note that the `submit` and `present` methods are _asynchronous_ operations, i.e. a task is queued for execution and the method returns immediately.

### Work Submission

To use the semaphores we extend the work class by adding two new members:

```java
public class Work {
    ...
    private final Map<Semaphore, Integer> wait = new HashMap<>();
    private final Set<Semaphore> signal = new HashSet<>();
}
```

The `signal` member is the set of semaphores to be signalled when the work has completed.

Each entry in the `wait` table is a semaphore that must be in the signalled state before the work can be performed and the stages(s) of the pipeline to wait on (represented as an integer mask).

We modify the builder to configure the semaphores for a work submission:

```java
public Builder wait(Semaphore semaphore, Collection<VkPipelineStage> stages) {
    wait.put(semaphore, IntegerEnumeration.mask(stages));
}

public Builder signal(Semaphore semaphore) {
    signal.add(semaphore);
}
```

And the populate method of the work class is updated to include the signals:

```java
info.signalSemaphoreCount = signal.size();
info.pSignalSemaphores = NativeObject.toArray(signal);
```

Population of the wait semaphores is slightly more complicated because the two components are separate fields (rather than an array of some child structure):

We first transform the table to a list of its entries:

```java
var list = new ArrayList<>(wait.entrySet());
```

Next we construct the pointer-array for the semaphores:

```java
var semaphores = list.stream().map(Entry::getKey).collect(toList());
info.waitSemaphoreCount = wait.size();
info.pWaitSemaphores = NativeObject.toArray(semaphores);
```

Finally we construct the stage masks which for some reason is a pointer-to-integer array:

```java
int[] stages = list.stream().map(Entry::getValue).mapToInt(Integer::intValue).toArray();
Memory mem = new Memory(stages.length * Integer.BYTES);
mem.write(0, stages, 0, stages.length);
info.pWaitDstStageMask = mem;
```

We can now configure the work submission for the render task to use the semaphores:

```java
new Work.Builder(buffer.pool())
    .add(buffer)
    .wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
    .signal(ready)
    .build()
    .submit();
```

This resolves the validation errors that were due to the semaphores never being signalled.

### Fence

However if one were to remove the `waitIdle` calls in the existing code the validation layer will again flood with errors, we are trying to use the command buffers concurrently for multiple frames.  Additionally the application is continually queueing up rendering work without checking whether it actually completes (which can be seen if one watches the memory usage).

The second synchronisation mechanism is the _fence_ which can be used to synchronise Vulkan and application code:

```java
public class Fence extends AbstractVulkanObject {
    @Override
    protected Destructor<Fence> destructor(VulkanLibrary lib) {
        return lib::vkDestroyFence;
    }
}
```

Again a fence is created using a factory:

```java
public static Fence create(DeviceContext dev, VkFenceCreateFlag... flags) {
    // Init descriptor
    VkFenceCreateInfo info = new VkFenceCreateInfo();
    info.flags = IntegerEnumeration.mask(flags);

    // Create fence
    VulkanLibrary lib = dev.library();
    PointerByReference handle = lib.factory().pointer();
    check(lib.vkCreateFence(dev, info, null, handle));

    // Create domain object
    return new Fence(handle.getValue(), dev);
}
```

A fence can be signalled in the same manner as a semaphore but it can also be explicitly waited on by the application:

```java
public static void wait(DeviceContext dev, Collection<Fence> fences, boolean all, long timeout) {
    Pointer array = NativeObject.toArray(fences);
    VulkanLibrary lib = dev.library();
    check(lib.vkWaitForFences(dev, fences.size(), array, VulkanBoolean.of(all), timeout));
}
```

Where _all_ specifies whether to wait for any or all of the supplied fences and _timeout_ is expressed in milliseconds.

Signalled fences can also be reset:

```java
public static void reset(DeviceContext dev, Collection<Fence> fences) {
    Pointer array = NativeObject.toArray(fences);
    VulkanLibrary lib = dev.library();
    check(lib.vkResetFences(dev, fences.size(), array));
}
```

We also provide convenience equivalents of these two methods for the fence instance itself:

```java
public void reset() {
    reset(device(), Set.of(this));
}

public void waitReady() {
    wait(device(), Set.of(this), true, Long.MAX_VALUE);
}
```

The state of the fence can also be queried:

```java
public boolean signalled() {
    DeviceContext dev = this.device();
    VulkanLibrary lib = dev.library();
    int result = lib.vkGetFenceStatus(dev, this);
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

Where the state codes are constants:

```java
private static final int SIGNALLED = VkResult.SUCCESS.value();
private static final int NOT_SIGNALLED = VkResult.NOT_READY.value();
```

We can now add a fence to the render loop component:

```java
fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
```

At the start of the render loop we block on the fence to ensure the previous frame has completed, the fence is initialised to the signalled state for this reason.

After resetting the fence we then modify the work submission code to accept the fence, which just involves adding the parameter and passing it to the API:

```java
public static void submit(List<Work> work, Fence fence) {
    ...
    check(lib.vkQueueSubmit(pool.queue(), array.length, array, fence));
}
```

The refactored render loop now looks like this:

```java
// Wait for previous work to complete
fence.waitReady();

// Retrieve next swapchain image index
final int index = swapchain.acquire(available, null);

// Init frame sync
fence.reset();

// Render frame
final Buffer buffer = factory.apply(index);
new Work.Builder(buffer.pool())
    .add(buffer)
    .wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
    .signal(ready)
    .build()
    .submit(fence);

// Present frame
swapchain.present(presentation, index, Set.of(ready));
```

We also release the fence in the `close` method.

The demo should now run without validation errors (for the render loop anyway), however there are still further improvements we can implement.

### Sub-Pass Dependencies

TODO

### Frames In-Flight

The render loop is still likely not fully utilising the pipeline since the code for a frame is essentially single-threaded, completed pipeline stages could be used to render the next frame.

Here we introduce multiple _in-flight_ frames to make better use of the pipeline whilst still bounding the overall amount of work.

We first wrap up the existing render code and the synchronisation primitives into a separate local class representing an instance of an in-flight frame:

```java
private class Frame {
    private final Semaphore available, ready;
    private final Fence fence;

    private void render() {
        ...
    }
    
    private void close() {
        ...
    }
}
```

Next we add a new parameter to the constructor and instantiate an array of in-flight frames:

```java
public class RenderLoop {
    private final Frame[] frames;
    private int current;

    public RenderLoop(Swapchain swapchain, int frames, ...) {
        this.frames = new Frame[frames];
        Arrays.setAll(this.frames, ignored -> new Frame());
    }
}
```

The frames are also released on destruction:

```java
public void close() {
    for(Frame f : frames) {
        f.close();
    }
}
```

The main render loop is refactored to iterate through the array of in-flight frames on each invocation:

```java
public void run() {
    // Render next frame
    final Frame frame = frames[current];
    frame.render();

    // Move to next in-flight frame
    if(++current >= frames.length) {
        current = 0;
    }
}
```

Since the majority of the Vulkan methods are asynchronous we should now be able to process multiple in-flight frames in parallel, with the synchronisation implemented above bounding the work (we should no longer be seeing increasing memory usage).

There is one further potential failure case: if the number of in-flight frames is larger than the number of swapchain images and/or an image is acquired out-of-order we may end up rendering an image that is already in-flight.

To avoid this scenario we track the swapchain images that are actively in-flight:

```java
public class RenderLoop {
    ...
    private final Frame[] active;

    public RenderLoop(...) {
        ...
        active = new Frame[swapchain.views().size()];
        Arrays.fill(active, frames[0]);
    }
}
```

After we acquire the image index we additionally wait on the frame currently using the image and then update the mapping:

```java
active[index].fence.waitReady();
active[index] = this;
```

In the demo application we set the number of in-flight frames to be the same as the number of swapchain images.

---

## Summary

In this chapter we:

- Factored out a reusable render loop component.

- Factored out the application loop.

- Implemented synchronisation to ensure that the frame rendering is correctly synchronised within the pipeline and across the work queues.

- Integrated the GLFW window event queue and implemented a means of gracefully terminating the demo.
