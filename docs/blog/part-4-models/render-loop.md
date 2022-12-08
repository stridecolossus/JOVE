---
title: The Render Loop and Synchronisation
---

---

## Contents

- [Overview](#overview)
- [Render Loop](#render-loop)
- [Synchronisation](#synchronisation)
- [Rotation and Animation](#rotation-and-animation)

---

## Overview

Before we progress the demo there are several issues with the existing, crude render loop implemented in the previous chapter:

* The rendering code is completely single-threaded.

* The rendering and presentation tasks are 'synchronised' by blocking on the work queues.

* The window event queue is not being polled, meaning the window is inoperable.

* There is no mechanism to terminate the application other than the dodgy timer or force-quitting the process.

* The existing render loop is cumbersome and mixes unrelated activities (rendering, animation, event polling).

In this chapter these issues are addressed by the introduction of the following:

* New JOVE components to separate the various activities into reusable, coherent components.

* Synchronisation to fully utilise the multi-threaded nature of the Vulkan pipeline.

* A GLFW keyboard handler to gracefully exit the application.

* An animation framework.

---

## Render Loop

### Refactoring

We start by factoring out the various aspects of the render loop into reusable, collaborating components that can more easily be unit-tested.

First the array of frame buffers is wrapped into the following compound object:

```java
public class FrameSet implements TransientObject {
    private final Swapchain swapchain;
    private final List<FrameBuffer> buffers;

    public FrameBuffer buffer(int index) {
        return buffers.get(index);
    }
}
```

In the constructor a frame buffer is created for each swapchain image:

```java
public FrameSet(Swapchain swapchain, RenderPass pass, List<View> additional) {
    List<View> images = swapchain.attachments();
    Dimensions extents = swapchain.extents();
    for(View image : images) {
        // Enumerate attachments
        var attachments = new ArrayList<View>();
        attachments.add(image);
        attachments.addAll(additional);

        // Create buffer
        FrameBuffer buffer = FrameBuffer.create(pass, extents, attachments);
        buffers.add(buffer);
    }
}
```

The `additional` attachments are not required for the rotating cube demo but will come into play in later chapters, e.g. depth buffers.

Finally the set of frame buffers can now be properly released on application shutdown:

```java
public void destroy() {
    for(FrameBuffer b : buffers) {
        b.destroy();
    }
}
```

Next the `FrameBuilder` is introduced which is responsible for constructing the command buffer for the rendering work:

```java
public class FrameBuilder {
    private final IntFunction<FrameBuffer> frames;
    private final Supplier<Buffer> factory;
    private final VkCommandBufferUsage[] flags;

    public Buffer build(int index, RenderSequence seq) {
        ...
    }
}
```

The `build` method first allocates a command buffer:

```java
public Buffer build(int index, RenderSequence seq) {
    Buffer buffer = factory.get();
    buffer.begin(flags);
    ...
    buffer.end();
    return buffer;
}
```

Next a render pass is started for the next framebuffer:

```java
FrameBuffer fb = frames.apply(index);
buffer.add(fb.begin());
...
buffer.add(FrameBuffer.END);
```

And finally the render sequence is recorded.  The `build` method now looks like this (indentation added for clarity):

```java
public Buffer build(int index, RenderSequence seq) {
    Buffer buffer = factory.get();
    FrameBuffer fb = frames.apply(index);
    buffer.begin(flags);
        buffer.add(fb.begin());
            seq.record(buffer);
        buffer.add(FrameBuffer.END);
    buffer.end();
    return buffer;
}
```

The _render sequence_ is a convenience abstraction for recording a sequence of rendering commands:

```java
public interface RenderSequence {
    /**
     * Records this render sequence to the given command buffer.
     * @param buffer Render task
     * @see Buffer#add(Command)
     */
    void record(Buffer buffer);
}
```

For the moment this simply wraps an arbitrary collection of commands:

```java
static RenderSequence of(List<Command> commands) {
    return buffer -> commands.forEach(buffer::add);
}
```

### Controller

Next the acquire-render-present process is factored out to a new controller component:

```java
public class FrameProcessor {
    private final Swapchain swapchain;
    private final FrameBuilder builder;
}
```

The existing rendering process is moved and refactored accordingly:

```java
public void render(RenderSequence seq) {
    // Acquire next frame buffer
    int index = swapchain.acquire(null, null);

    // Render frame
    Buffer buffer = builder.build(index, seq);
    
    // Submit render task
    Pool pool = buffer.pool();
    new Work.Builder(pool)
        .add(buffer)
        .build()
        .submit();
        
    // Wait for frame to be rendered
    pool.waitIdle();

    // Present rendered frame
    swapchain.present(pool.queue(), index, null);
    pool.waitIdle();
}
```

Next a new listener is introduced for frame completion events:

```java
@FunctionalInterface
public interface FrameListener {
    /**
     * Notifies a completed frame.
     */
    void frame();
}
```

Which is registered with the controller:

```java
public class FrameProcessor {
    private final Set<Listener> listeners = new HashSet<>();

    public void add(Listener listener) {
        listeners.add(listener);
    }
}
```

The `render` method is wrapped with an elapsed duration and broadcasts frame completion events:

```java
public void render(RenderSequence seq) {
    // Render frame
    Instant start = Instant.now();
    ...

    // Notify frame completion
    Instant end = Instant.now();
    for(Listener listener : listeners) {
        listener.frame(start, end);
    }
}
```

### Render Loop

The final piece of the new framework is a render loop that takes advantage of the scheduling functionality in the executor framework:

```java
public class RenderLoop {
    private final ScheduledExecutorService executor;
    private ScheduledFuture<?> future;
    private long rate;
}
```

Once started this component executes render tasks with a target frame-rate:

```java
public void start(Runnable task) {
    if(future != null) throw new IllegalStateException(...);
    future = executor.scheduleAtFixedRate(task, 0, rate, TimeUnit.MILLISECONDS);
}
```

The frame-rate is a configurable property:

```java
public void rate(int fps) {
    this.rate = TimeUnit.SECONDS.toMillis(1) / fps;
}
```

The render loop can also be terminated:

```java
public void stop() {
    if(future == null) throw new IllegalStateException(...);
    future.cancel(true);
    future = null;
}
```

This new component should allow applications to configure a target frame-rate without having to implement fiddly timing and thread yielding logic.  Note that the executor automatically handles tasks that take longer than the configured period, i.e. if a frame takes longer to render than the specified frame-rate, the next frame starts later, and is not run concurrently.

Finally we implement the following simple FPS counter to verify the render loop:

```java
public class FrameCounter implements FrameListener {
    private Instant next = Instant.EPOCH;
    private int count;
    private int fps;

    @Override
    public void frame() {
        Instant now = Instant.now();
        if(now.isAfter(next)) {
            count = 1;
            next = now.plusSeconds(1);
        }
        else {
            ++count;
        }
    }
}
```

### Integration

The new framework is used to break up the existing demo into simpler components.

First the presentation configuration is modified to create the set of frame buffers:

```java
@Bean
public static FrameSet frames(Swapchain swapchain, RenderPass pass) {
    return new FrameSet(swapchain, pass, List.of());
}
```

And the presentation controller:

```java
@Bean
public FrameProcessor processor(FrameSet frames, @Qualifier("graphics") Command.Pool pool) {
    var builder = new FrameBuilder(frames::buffer, pool::allocate, VkCommandBufferUsage.ONE_TIME_SUBMIT);
    return new FrameProcessor(frames.swapchain(), builder, 2);
}
```

Next the rendering command sequence is wrapped into the new class:

```java
@Bean
public static RenderSequence sequence(List<Command> commands) {
    return RenderSequence.of(...);
}
```

Where the `commands` have been factored out into separate beans and are automatically aggregated by the container.

A new executor service is added to the main class of the demo:

```java
@Bean
public static ScheduledExecutorService executor() {
    return Executors.newSingleThreadScheduledExecutor();
}
```

Which is composed with the frame processor and render sequence to initialise and start the render loop:

```java
@Bean
public RenderLoop loop(ScheduledExecutorService executor, FrameProcessor proc, RenderSequence seq) {
    Runnable task = () -> proc.render(seq);
    RenderLoop loop = new RenderLoop(executor);
    loop.rate(60);
    loop.start(task);
    return loop;
}
```

Finally the animation logic is factored out into a frame listener:

```java
@Bean
public static FrameListener animation(Matrix projection, Matrix view, ResourceBuffer uniform) {
    long period = 2500;
    ByteBuffer bb = uniform.buffer();
    return () -> {
        // Build rotation matrix
        long time = System.currentTimeMillis();
        float angle = (time % period) * MathsUtil.TWO_PI / period;
        Matrix h = Rotation.matrix(Vector.Y, angle);
        Matrix v = Rotation.matrix(Vector.X, MathsUtil.toRadians(30));
        Matrix model = h.multiply(v);

        // Update matrix
        Matrix matrix = projection.multiply(view).multiply(model);
        matrix.buffer(bb);
        bb.rewind();
    };
}
```

Which is registered with the controller via the following method:

```java
@Autowired
void listeners(FrameProcessor proc, Collection<FrameProcessor.Listener> listeners) {
    listeners.forEach(proc::add);
}
```

Notes:

* Spring auto-magically creates the set of listeners from those registered with the container.

* To avoid having to implement messy synchronisation the animation is updated once per frame.

### Graceful Exit

To gracefully exit the application a GLFW keyboard listener is added to the main configuration class:

```java
public class RotatingCubeDemo {
    @Bean
    KeyListener exit(Window window) {
        KeyListener listener = (ptr, key, scancode, action, mods) -> System.exit(0));
        window.desktop().library().glfwSetKeyCallback(window.handle(), listener);
        return listener;
    }
}
```

The keyboard listener is a GLFW callback defined as follows:

```java
interface KeyListener extends Callback {
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
```

A complication of particular importance is that GLFW event processing __must__ be performed on the main application thread, unfortunately it cannot be implemented as a separate thread or using the executor framework.  Therefore the polling logic has to remain as an activity on the main thread:

```java
@Bean
static CommandLineRunner runner(Desktop desktop) {
    return args -> {
        while(true) {
            desktop.poll();
        }
    };
}
```

See the [GLFW thread documentation](https://www.glfw.org/docs/latest/intro.html#thread_safety) for more details.

> Apparently GLFW version 4 will deprecate callbacks in favour of query methods which would remove this problem.

Finally a cleanup method is added to the main class to ensure the Vulkan device is shutdown correctly.

```java
@PreDestroy
void destroy() {
    dev.waitIdle();
}
```

### Application Configuration

Since the demo is moving towards a proper double-buffered swapchain and render loop, now is a good point to externalise some of the application configuration to simplify testing and data modifications.

A new `ConfigurationProperties` component is added to the demo:

```java
@Configuration
@ConfigurationProperties
public class ApplicationConfiguration {
    private String title;
    private int frameCount;
    private int frameRate;
    private Colour col;
    private long period;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    ...

    public void setBackground(float[] col) {
        this.col = Colour.of(col);
    }
}
```

The properties are configured in the `application.properties` file:

```java
title: Rotating Cube Demo
frameCount: 2
frameRate: 60
background: 0.3, 0.3, 0.3
period: 2500
```

Notes:

* The properties class __must__ be a simple POJO (with old-school getters and setters) to be auto-magically populated by the Spring framework.

* The `background` property (the clear colour for for the swapchain views) is a comma-separated list which Spring injects as an array.

The demo application is refactored by injecting the properties into the relevant beans replacing any hard-coded values and injected `@Value` parameters used previously.

For example the swapchain can now be refactored as follows:

```java
class PresentationConfiguration {
    @Autowired private LogicalDevice dev;
    @Autowired private ApplicationConfiguration cfg;

    @Bean
    public Swapchain swapchain(Surface surface) {
        return new Swapchain.Builder(dev, surface)
            .count(cfg.getFrameCount())
            .clear(cfg.getBackground())
            .build();
    }
}
```

---

## Synchronisation

### Semaphores

So far we have avoided synchronisation by simply blocking the work queues after rendering and presentation of a frame.  However Vulkan is designed to be multi-threaded from the ground up, in particular the following methods are asynchronous operations:

* Acquiring the next swapchain image

* Submitting a render task to the work queue

* Presentation of a rendered frame

All of these methods return immediately with the actual work queued for execution in the background.

The Vulkan API provides several synchronisation mechanisms that can be used by the application, a _semaphore_ is the simplest of these and is used to synchronise operations within or across work queues.

The class itself is trivial since semaphores do not have any public functionality:

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
    PointerByReference handle = dev.factory().pointer();
    VulkanLibrary.check(lib.vkCreateSemaphore(dev, info, null, handle));
    return new Semaphore(handle.getValue(), dev);
}
```

Two semaphores are created in the constructor of the controller to signal the following conditions:

1. An acquired swapchain image is `available` for rendering.

2. A frame has been rendered and is `ready` for presentation.

The _available_ semaphore is passed to the acquire method:

```java
int index = swapchain.acquire(available, null);
```

And _ready_ is used when presenting the frame:

```java
swapchain.present(queue, index, ready);
```

The `present` method is modified to populate the relevant member of the descriptor:

```java
public void present(Queue queue, int index, Semaphore semaphore) {
    ...
    info.waitSemaphoreCount = semaphores.size();
    info.pWaitSemaphores = NativeObject.toArray(List.of(semaphore));
}
```

Finally the semaphores are released when the controller is destroyed:

```java
public class FrameProcessor implements TransientObject {
    @Override
    public void destroy() {
        available.destroy();
        ready.destroy();
    }
}
```

Note that if the demo is run as it stands (with the work queue blocking still present) there will be additional Vulkan errors because the semaphores are never actually signalled.

### Work Submission

To integrate the semaphores the work class is extended by adding two new members:

```java
public class Work {
    ...
    private final Map<Semaphore, Set<VkPipelineStage>> wait = new LinkedHashMap<>();
    private final Set<Semaphore> signal = new HashSet<>();
}
```

Each entry in the `wait` table consists of a semaphore that must be signalled before the work can be performed and the stages(s) of the pipeline to wait on.
The `signal` member is the set of semaphores to be signalled when the work has completed.

The builder is modified to configure the semaphores:

```java
public Builder wait(Semaphore semaphore, Collection<VkPipelineStage> stages) {
    wait.put(semaphore, Set.copyOf(stages));
    return this;
}

public Builder signal(Semaphore semaphore) {
    signal.add(semaphore);
    return this;
}
```

And the populate method of the work class is updated to include the signals:

```java
info.signalSemaphoreCount = signal.size();
info.pSignalSemaphores = NativeObject.toArray(signal);
```

Population of the wait semaphores is slightly more complicated because the two components are separate fields, rather than an array of some child structure.  Therefore the table is a linked map to ensure that both fields are iterated in the same order.

First the array of semaphores is populated:

```java
info.waitSemaphoreCount = wait.size();
info.pWaitSemaphores = NativeObject.toArray(wait.keySet());
```

And then the list of pipeline stages for each semaphore:

```java
int[] stages = wait.values().stream().map(BitMask::reduce).mapToInt(BitMask::bits).toArray();
info.pWaitDstStageMask = new PointerToIntArray(stages);
```

Note that although `pWaitDstStageMask` implies this is a bit-field it is in fact a pointer to an integer array.

Here the `PointerToIntArray` helper class is introduced to transform the array of pipeline stages to a contiguous memory block:

```java
public class PointerToIntArray extends Memory {
    public PointerToIntArray(int[] array) {
        super(Integer.BYTES * array.length);
        for(int n = 0; n < array.length; ++n) {
            setInt(n * Integer.BYTES, array[n]);
        }
    }
}
```

Additional helpers are also implemented for arrays of floating-point values and pointers.  Surprisingly JNA does not provide these helpers (or where it does they are hidden for some reason).

The step to submit the render task is factored out to a local helper method and integrated with the semaphores:

```java
protected void submit(Buffer buffer) {
    new Work.Builder(buffer.pool())
        .add(buffer)
        .wait(available, VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
        .signal(ready)
        .build()
        .submit();
}
```

This should resolve the validation errors due to the semaphores never being signalled.

### Fence

However if one were to remove the `waitIdle` calls in the existing render loop the validation layer will again flood with errors, the command buffers are incorrectly being used concurrently for multiple frames.  Additionally the application is continually queueing up rendering work without checking whether it actually completes, which can be seen if one watches the memory usage.

To resolve both of these issues the second synchronisation mechanism is introduced which synchronises Vulkan and application code:

```java
public class Fence extends AbstractVulkanObject {
    @Override
    protected Destructor<Fence> destructor(VulkanLibrary lib) {
        return lib::vkDestroyFence;
    }
}
```

Again fences are created via a factory:

```java
public static Fence create(DeviceContext dev, VkFenceCreateFlag... flags) {
    // Init descriptor
    var info = new VkFenceCreateInfo();
    info.flags = BitMask.reduce(flags);

    // Create fence
    VulkanLibrary lib = dev.library();
    PointerByReference handle = dev.factory().pointer();
    check(lib.vkCreateFence(dev, info, null, handle));

    // Create domain object
    return new Fence(handle.getValue(), dev);
}
```

Fences are signalled in the same manner as semaphores but can also be explicitly waited on by the application:

```java
public static void wait(DeviceContext dev, Collection<Fence> fences, boolean all, long timeout) {
    Pointer array = NativeObject.toArray(fences);
    VulkanLibrary lib = dev.library();
    check(lib.vkWaitForFences(dev, fences.size(), array, all, timeout));
}
```

Where _all_ specifies whether to wait for any or all of the supplied fences and _timeout_ is expressed in nanoseconds.

A signalled fence can also be reset:

```java
public static void reset(DeviceContext dev, Collection<Fence> fences) {
    Pointer array = NativeObject.toArray(fences);
    VulkanLibrary lib = dev.library();
    check(lib.vkResetFences(dev, fences.size(), array));
}
```

Convenience over-loads are also implemented for these methods:

```java
public void reset() {
    reset(device(), Set.of(this));
}

public void waitReady() {
    wait(device(), Set.of(this), true, Long.MAX_VALUE);
}
```

The state of the fence can also be programatically queried:

```java
public boolean signalled() {
    DeviceContext dev = this.device();
    VulkanLibrary lib = dev.library();
    VkResult result = lib.vkGetFenceStatus(dev, this);
    return switch(result) {
        case SUCCESS -> true;
        case NOT_READY -> false;
        default -> throw new VulkanException(result);
    };
}
```

A fence is created in the constructor of the controller:

```java
public class FrameProcessor implements TransientObject {
    ...
    private final Fence fence;

    public FrameProcessor(...) {
        ...
        this.fence = Fence.create(dev, VkFenceCreateFlag.SIGNALED);
    }
}
```

And the existing code is replaced with a `waitReady` call on the fence to block until the frame has been rendered.

A further blocking call is introduced at the _start_ of the render process to ensure that the _previous_ frame has been completed, hence the fence is initialised to the `SIGNALED` state.

The final `render` method now looks like this:

```java
public void render(RenderSequence seq) {
    // Wait for previous frame to complete
    fence.waitReady();
    fence.reset();

    // Acquire next frame buffer
    int index = swapchain.acquire(available, null);

    // Render frame
    Buffer buffer = builder.build(index, seq);
    submit(buffer);

    // Block until frame rendered
    fence.waitReady();

    // Present rendered frame
    WorkQueue queue = buffer.pool().queue();
    swapchain.present(queue, index, ready);
}
```

The demo should now run without validation errors (for the render loop anyway), however there are still further improvements that can be implemented.

### Subpass Dependencies

The final synchronisation mechanism is a _subpass dependency_ which specifies memory and execution dependencies between the stages of a render pass.

A subpass dependency is configured via two new mutable inner classes:

```java
public class Dependency {
    private Subpass dependency;
    private final Set<VkDependencyFlag> flags = new HashSet<>();
    private final Properties src = new Properties();
    private final Properties dest = new Properties();
}
```

Where _source_ and _destination_ specify the properties of the dependency:

```java
public class Properties {
    private final Set<VkPipelineStage> stages = new HashSet<>();
    private final Set<VkAccess> access = new HashSet<>();
}
```

Note that the _destination_ of the dependency is implicitly the enclosing subpass.

The subpass domain type is modified to include the list of dependencies:

```java
public class Subpass {
    private final List<Dependency> dependencies = new ArrayList<>();

    public Dependency dependency() {
        var dependency = new Dependency();
        dependencies.add(dependency);
        return dependency;
    }
}
```

The source and destination subpass instances are referenced by _index_ in the same manner as the attachment references.
A hidden mutable `index` is added to the subpass class which is initialised in the `create` method of the render pass:

```java
int index = 0;
for(Subpass subpass : subpasses) {
    subpass.init(index++);
}
```

Next the subpass dependencies are added to the render pass:

```java
List<Dependency> dependencies = subpasses.stream().flatMap(Subpass::dependencies).toList();
info.dependencyCount = dependencies.size();
info.pDependencies = StructureCollector.pointer(dependencies, new VkSubpassDependency(), Dependency::populate);
```

Which are populated as follows:

```java
void populate(VkSubpassDependency info) {
    info.srcSubpass = index;
    info.dstSubpass = Subpass.this.index;
    info.srcStageMask = BitMask.reduce(src.stages);
    info.srcAccessMask = BitMask.reduce(src.access);
    info.dstStageMask = BitMask.reduce(dest.stages);
    info.dstAccessMask = BitMask.reduce(dest.access);
}
```

A subpass can also be dependant on the implicit subpass before or after the render pass:

```java
public Dependency external() {
    this.dependency = VK_SUBPASS_EXTERNAL;
    return this;
}
```

Where `VK_SUBPASS_EXTERNAL` is a synthetic subpass with a special case index (copied from the Vulkan header file):

```java
public class Dependency {
    private static final Subpass VK_SUBPASS_EXTERNAL = new Subpass();

    static {
        VK_SUBPASS_EXTERNAL.index = ~0;
    }
}
```

In the demo a dependency can now be configured on the implicit `external` subpass:

```java
Subpass subpass = new Subpass()
    .colour(attachment)
    .dependency()
        .external()
        .source()
            .stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
            .build()
        .destination()
            .stage(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT)
            .access(VkAccess.COLOR_ATTACHMENT_WRITE)
            .build()
        .build();
```

The _destination_ clause specifies that the subpass should wait for the colour attachment to be ready for writing, i.e. when the swapchain has finished using the image.

This should allow Vulkan to more efficiently use the multi-threaded nature of the pipeline.

### Frames In-Flight

The render loop is still likely not fully utilising the pipeline since the code for a frame is essentially single-threaded, whereas Vulkan is designed to allow completed pipeline stages to be used to render the next frame in parallel.  Multiple _in flight_ frames are introduced to take advantage of this feature.

First the existing render loop and synchronisation primitives are wrapped into an inner class which tracks the in-flight progress of each frame:

```java
public class FrameProcessor {
    public class Frame {
        private final Semaphore available, ready;
        private final Fence fence;
    
        public void render(RenderSequence seq) {
            ...
        }
    }
}
```

And the constructor is modified to create an _array_ of frames:

```java
public class FrameProcessor implements TransientObject {
    ...
    private final Frame[] frames;
    private int next;

    public FrameProcessor(Swapchain swapchain, FrameBuilder builder, int frames) {
        ...
        this.frames = new Frame[frames];
        init();
    }

    private void init() {
        DeviceContext dev = swapchain.device();
        Arrays.setAll(frames, n -> new Frame(dev));
    }
}
```

Notes:

* The number of in-flight frames does not necessarily have to be the same as the number of swapchains images (though in practice this is generally the case).

* The controller is now a transient object and releases the synchronisation primitives for each frame on destruction.

Finally the `render` method is modified to delegate to the next in-flight frame:

```java
public void render(RenderSequence seq) {
    int index = next++ % frames.length;
    Frame frame = frames[index];
    ...
}
```

Multiple frames can now be executed in parallel, the introduced synchronisation better utilises the pipeline, and the array of in-flight frames bounds the overall work.

---

## Rotation and Animation

### Normals

To support rotations the `Vector` class is expanded by the introduction of:

* A specific sub-class for _normal_ vectors.

* A further specialisation for the _cardinal_ axes (replacing the existing constants in the vector class).

* A `sealed` hierarchy for the various `Tuple` implementations.

A _normal_ is a unit-vector:

```java
public sealed class Normal extends Vector implements Component permits Axis {
    public static final Layout LAYOUT = Layout.floats(3);

    public Normal(Vector vec) {
        super(normalize(vec));
    }

    @Override
    public final float magnitude() {
        return 1;
    }

    @Override
    public final Normal normalize() {
        return this;
    }
}
```

The `normalize` code is moved from the `Vector` class and the existing method is refactored accordingly:

```java
public Normal normalize() {
    return new Normal(this);
}
```

Next this new type is sub-classed for the cardinal axes:

```java
public final class Axis extends Normal {
    public static final Axis
            X = new Axis(0),
            Y = new Axis(1),
            Z = new Axis(2);
}
```

The vector of each axis is initialised in the constructor:

```java
private final int index;

private Axis(int index) {
    super(axis(index));
    this.index = index;
}

private static Vector axis(int index) {
    final float[] axis = new float[SIZE];
    axis[index] = 1;
    return new Vector(axis);
}
```

The frequently used inverse axes are also pre-calculated and cached:

```java
private final Normal inv = super.invert();

@Override
public Normal invert() {
    return inv;
}
```

And finally the code to construct a rotation matrix about one of the cardinal axes is moved from the matrix class:

```java
public Matrix rotation(float angle) {
    var matrix = new Matrix.Builder().identity();
    float sin = MathsUtil.sin(angle);
    float cos = MathsUtil.cos(angle);
    switch(index) {
        case 0 -> matrix.set(...);
        ...
    }
    return matrix.build();
}
```

Note that this code switches on the `index` of the axis, this works fine for now but may be replaced later by an internal helper enumeration.

The purpose of these changes are:

1. The intent of code using the new hierarchy can now be made more expressive and type-safe, e.g. classes that _require_ a unit-vector can now enforce the `Normal` type explicitly.

2. Reduces the reliance on documented assumptions and defensive checks to ensure vectors are normalised when required, e.g. when generating rotation matrices.

3. The overhead of re-normalising is trivial where an already normalized vector is referenced as the base `Vector` type.

4. The hierarchy now supports extension points for further optimisations, e.g. caching of the inverse cardinal axes.

### Rotations

Next a new abstraction is introduced for a general transformation implemented by a matrix:

```java
@FunctionalInterface
public interface Transform {
    /**
     * @return Transformation matrix
     */
    Matrix matrix();
}
```

With the following intermediate specialisation for rotation transforms (a marker interface for the time being):

```java
public interface Rotation extends Transform {
}
```

Note that a matrix is itself a transform:

```java
public Matrix {
    @Override
    public final Matrix matrix() {
        return this;
    }
}
```

The simplest rotation implementation is an axis-angle which specifies a counter-clockwise rotation about a given normal:

```java
public class AxisAngle implements Rotation {
    private final Normal axis;
    private final float angle;
    
    @Override
    public Matrix matrix() {
        ...
    }
}
```

Note that this implementation enforces the axis to be a unit-vector, the results for an arbitrary vector would be interesting!

The new `Axis` class generates a rotation matrix for the cardinal axes, however the new framework needs to support rotations about an arbitrary axis (which was explicitly disallowed in the original code), rather than composing multiple matrices as in the previous demo.  Implementing code to generate a rotation matrix about an arbitrary axis is relatively straight-forward (if somewhat messy) but a better alternative is to introduce _quaternions_ which also offer additional functionality that will be required in later chapters.

### Quaternions

A _quaternion_ is a more compact and efficient representation of a rotation often used when multiple rotations are frequently composed (e.g. skeletal animation), but is generally less intuitive to use and comprehend:

```java
public final class Quaternion implements Rotation {
    public final float w, x, y, z;

    /**
     * @return Magnitude <b>squared</b> of this quaternion
     */
    public float magnitude() {
        return w * w + x * x + y * y + z * z;
    }

    public Matrix matrix() {
        ...
    }
}
```

A quaternion can be constructed from an axis-angle using the following factory:

```java
public static Quaternion of(AxisAngle rot) {
    float half = rot.angle() * MathsUtil.HALF;
    Vector vec = rot.axis().multiply(MathsUtil.sin(half));
    return new Quaternion(MathsUtil.cos(half), vec.x, vec.y, vec.z);
}
```

And converted back to an axis-angle in the inverse operation:

```java
@Override
public AxisAngle toAxisAngle() {
    float scale = MathsUtil.inverseRoot(1 - w * w);
    float angle = 2 * MathsUtil.acos(w);
    Vector axis = new Vector(x, y, z).multiply(scale);
    return new AxisAngle(axis, angle);
}
```

The implementation of the `matrix` method for a quaternion (not shown) is less performant than the code for the cardinal axes.  For an immutable, one-off rotation this probably would not be a concern, but for rotations about the cardinal axes that are frequently re-calculated the faster solution is obviously preferable.  Therefore the axis-angle class selects the most appropriate implementation:

```java
public Matrix matrix() {
    if(axis instanceof Axis cardinal) {
        return cardinal.rotation(angle);
    }
    else {
        return Quaternion.of(this).matrix();
    }
}
```

Finally the following mutable sub-class supports animation of a rotation:

```java
public class MutableRotation extends AxisAngle {
    @Override
    public void set(float angle) {
        super.set(angle);
    }
}
```

Where the `set` method modifies the rotation `angle` (which becomes a mutable but hidden property) and is exposed in this implementation.

### Player

The final enhancement to the existing code is the implementation of a new framework to support playable media and animations.

First the following new abstraction defines a media resource or animation that can be played:

```java
public interface Playable {
    enum State {
        STOP,
        PLAY,
        PAUSE
    }

    default boolean isPlaying() {
        return state() == State.PLAY;
    }

    State state();

    void apply(State state);
}
```

Which is partially implemented by the following skeleton:

```java
public abstract class AbstractPlayable implements Playable {
    private State state = State.STOP;

    @Override
    public void apply(State state) {
        this.state = notNull(state);
    }
}
```

A playable resource is managed by the _player_ controller:

```java
public class Player extends AbstractPlayable {
    @FunctionalInterface
    public interface Listener {
        void update(Player player);
    }

    private Playable playable;
    private final Collection<Listener> listeners = new HashSet<>();
}
```

Note that a player is itself a `Playable` and delegates state changes to the underlying playable resource:

```java
public void apply(State state) {
    super.apply(state);
    playable.apply(state);
    update();
}
```

Where `update` notifies interested observers of any state changes:

```java
private void update() {
    for(Listener listener : listeners) {
        listener.update(this);
    }
}
```

A playable resource may stop playing in the background, for example:

* OpenAL audio generally runs on a separate thread and stops playing at the end of a non-looping clip.

* A non-repeating animation terminates when the animation duration expires.

This case is handled by testing the underlying playable when querying the player state:

```java
public State state() {
    State state = super.state();
    if((state == State.PLAY) && !playable.isPlaying()) {
        super.apply(State.STOP);
        update();
        return State.STOP;
    }
    return state;
}
```

### Animation

Next a simple stopwatch utility is implemented that records the elapsed duration of a frame:

```java
public class Frame {
    @FunctionalInterface
    public interface Listener {
        void update(Frame frame);
    }

    private Instant start = Instant.EPOCH;
    private Instant end = Instant.EPOCH;
}
```

Note that the existing frame listener interface is moved to this new class and modified to accept a `Frame` instance.

The frame provides `start` and `stop` methods to wrap some activity (i.e. rendering) and the elapsed duration can then be queried:

```java
public Duration elapsed() {
    return Duration.between(start, end);
}
```

The frame processor introduced earlier is modified to include a `Frame` instance and the `render` method is instrumented to record the elapsed duration.

Finally the `Animator` is introduced which interpolates an _animation_ position over a given duration:

```java
public class Animator extends AbstractPlayable implements Frame.Listener {
    @FunctionalInterface
    public interface Animation {
        void update(float pos);
    }

    private final Animation animation;
    private final long duration;
    private long time;
    private float speed = 1;
    private boolean repeat = true;

    @Override
    public void update(Frame frame) {
        ...
    }
}
```

The animator is a playable object, therefore the `update` method ignores frame events if the animation is not running:

```java
public void update() {
    if(!isPlaying()) {
        return;
    }
    ...
}
```

Next the _time position_ of the animation is calculated:

```java
time += frame.elapsed().toMillis() * speed;
```

At the end of the duration the `time` is quantised or the animation stops if it is not repeating:

```java
if(time > duration) {
    if(repeat) {
        time = time % duration;
    }
    else {
        time = duration;
        apply(Playable.State.STOP);
    }
}
```

Finally the updated position is then applied to the animation:

```java
animation.update(time / (float) duration);
```

The final piece of the jigsaw is the mutable rotation implementation is modified to support animation about the unit-circle:

```java
public class MutableRotation extends AxisAngle implements Animation {
    @Override
    public void update(float pos) {
        set(pos * TWO_PI);
    }
}
```

Alternatively a separate adapter could have been implemented but that hardly seemed worthwhile for such a relatively trivial case.

### Integration

In the cube demo the existing hand-crafted matrix code is replaced by a rotation animation:

```java
@Bean
static MutableRotation rotation() {
    Normal axis = new Vector(MathsUtil.HALF, 1, 0).normalize();
    return new MutableRotation(axis);
}
```

The animation and timing logic is replaced by an animator:

```java
@Bean
static Animator animator(Animation rot, ApplicationConfiguration cfg) {
    return new Animator(rot, cfg.getPeriod());
}
```

Which is controlled by a player:

```java
@Bean
public static Player player(Animator animator) {
    Player player = new Player(animator);
    player.apply(Playable.State.PLAY);
    return player;
}
```

And finally the code to update the uniform buffer is refactored accordingly:

```java
public static FrameListener update(ResourceBuffer uniform, Matrix projection, Matrix view, MutableRotation rot) {
    ByteBuffer bb = uniform.buffer();
    return frame -> {
        Matrix model = rot.rotation().matrix();
        Matrix matrix = projection.multiply(view).multiply(model);
        matrix.buffer(bb);
        bb.rewind();
    };
}
```

---

## Summary

In this chapter the render loop was improved by:

- Factoring out the various aspects of the application and render loops into separate, reusable components.

- Integration of the GLFW window event queue and a means of gracefully terminating the demo.

- Implementation of Vulkan synchronisation to safely utilise the multi-threaded rendering pipeline.

- The addition of a new framework for playable media and animations.

This might appear a lot of work for little benefit, however we now have a framework which can be more easily extended in future chapters, in particular when input devices and scene graphs are introduced in later chapters.
