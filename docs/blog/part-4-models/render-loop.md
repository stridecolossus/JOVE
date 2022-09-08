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
     * @param start     Start time
     * @param end       Completion time
     */
    void frame(Instant start, Instant end);
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
    public void frame(Instant start, Instant end) {
        ++count;
        if(start.isAfter(next)) {
            fps = count;
            count = 1;
            next = start.plusSeconds(1);
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
    return (time, elapsed) -> {
        // Build rotation matrix
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
    private final Map<Semaphore, Integer> wait = new LinkedHashMap<>();
    private final Set<Semaphore> signal = new HashSet<>();
}
```

Each entry in the `wait` table consists of:

* A semaphore that must be signalled before the work can be performed.

* The stages(s) of the pipeline to wait on stored as an integer mask.

The `signal` member is the set of semaphores to be signalled when the work has completed.

The builder is modified to configure the semaphores:

```java
public Builder wait(Semaphore semaphore, Collection<VkPipelineStage> stages) {
    wait.put(semaphore, IntegerEnumeration.reduce(stages));
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
int[] stages = wait.values().stream().mapToInt(Integer::intValue).toArray();
info.pWaitDstStageMask = new IntegerArray(stages);
```

Note that although `pWaitDstStageMask` implies this is a bit-field it is in fact a pointer to an integer array.

Here the `IntegerArray` helper class is introduced to transform the array of pipeline stages to a contiguous memory block:

```java
public class IntegerArray extends Memory {
    public IntegerArray(int[] array) {
        super(Integer.BYTES * array.length);
        for(int n = 0; n < array.length; ++n) {
            setInt(n * Integer.BYTES, array[n]);
        }
    }
}
```

Additional helpers are also implemented for arrays of floating-point values and pointers.  Surprisingly JNA does not provide these helpers (or it does but they are hidden for some reason).

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
    VkFenceCreateInfo info = new VkFenceCreateInfo();
    info.flags = IntegerEnumeration.reduce(flags);

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
    check(lib.vkWaitForFences(dev, fences.size(), array, VulkanBoolean.of(all), timeout));
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
    Queue queue = buffer.pool().queue();
    swapchain.present(queue, index, ready);
}
```

The demo should now run without validation errors (for the render loop anyway), however there are still further improvements that can be implemented.

### Subpass Dependencies

The final synchronisation mechanism is a _subpass dependency_ which specifies memory and execution dependencies between the stages of a render-pass.

A subpass dependency is configured via two new mutable types:

```java
public class Dependency {
    private Integer index;
    private final Properties src = new Properties(this);
    private final Properties dest = new Properties(this);
}
```

Where `index` refers to the dependant subpass by _index_ (see below).

And _source_ and _destination_ specify the properties of the dependency:

```java
public class Properties {
    private final Dependency dependency;
    private final Set<VkPipelineStage> stages = new HashSet<>();
    private final Set<VkAccess> access = new HashSet<>();
}
```

The subpass domain class is modified to include an index and the list of dependencies:

```java
public class Subpass {
    ...
    private int index;
    private final List<Dependency> dependencies = new ArrayList<>();

    public Dependency dependency() {
        return new Dependency();
    }
}
```

The subpass _index_ is allocated by the builder of the parent render pass:

```java
public Subpass subpass() {
    Subpass subpass = new Subpass(subpasses.size());
    ...
}
```

The dependant subpass can be specified in the dependency:

```java
public Dependency dependency(Subpass subpass) {
    index = subpass.index;
    return this;
}
```

A subpass can also be dependant on the implicit subpass before or after the render pass:

```java
public Dependency external() {
    index = VK_SUBPASS_EXTERNAL;
    return this;
}
```

The descriptor for the subpass dependency is populated as follows:

```java
void populate(VkSubpassDependency info) {
    info.srcSubpass = index;
    info.dstSubpass = Subpass.this.index;
    info.srcStageMask = IntegerEnumeration.reduce(src.stages);
    info.srcAccessMask = IntegerEnumeration.reduce(src.access);
    info.dstStageMask = IntegerEnumeration.reduce(dest.stages);
    info.dstAccessMask = IntegerEnumeration.reduce(dest.access);
}
```

Note that the _destination_ of the dependency is implicitly the enclosing subpass.

The final change is to modify the render pass builder to populate the aggregated subpass dependencies:

```java
List<Dependency> dependencies = subpasses.stream().flatMap(Subpass::dependencies).toList();
info.dependencyCount = dependencies.size();
info.pDependencies = StructureHelper.pointer(dependencies, VkSubpassDependency::new, Dependency::populate);
```

In the demo a dependency can now be configured on the implicit `external` subpass:

```java
RenderPass pass = new RenderPass.Builder()
    .subpass()
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
            .build()
        .build()
    .build(dev);
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

### Animator

The final enhancement to the existing code is the implementation of a new framework to support rotation animations.

First the following new abstraction defines something that can be played:

```java
public interface Playable {
    boolean isPlaying();
    void play();
    void pause();
    void stop();
}
```

Which is partially implemented in the following template class:

```java
public abstract class AbstractPlayable implements Playable {
    private boolean playing;

    @Override
    public boolean isPlaying() {
        return playing;
    }

    @Override
    public void play() {
        if(playing) throw new IllegalStateException();
        playing = true;
    }

    @Override
    public void pause() {
        update();
    }

    @Override
    public void stop() {
        update();
    }

    private void update() {
        if(!playing) throw new IllegalStateException();
        playing = false;
    }
}
```

A playable resource is managed by the _player_ controller:

```java
public class Player extends AbstractPlayable {
    /**
     * Listener for player state changes.
     */
    @FunctionalInterface
    public interface Listener {
        /**
         * Notifies a player state change.
         * @param player Player
         */
        void update(Player player);
    }

    private final Collection<Playable> playing = new ArrayList<>();
    private final Collection<Listener> listeners = new HashSet<>();
}
```

The player delegates state changes and notifies interested listeners, for example:

```java
@Override
public void stop() {
    super.stop();
    delegate(Playable::stop);
}

private void delegate(Consumer<Playable> state) {
    playing.forEach(state);
    listeners.forEach(e -> e.update(this));
}
```

Next a simple timer utility is added that records an elapsed duration:

```java
public class Frame {
    /**
     * A <i>frame listener</li> notifies completion of a rendered frame.
     */
    @FunctionalInterface
    public interface Listener {
        /**
         * Notifies a completed frame.
         */
        void update();
    }

    private Instant start;
    private Instant end = Instant.EPOCH;
    private boolean running;

    public Duration elapsed() {
        return Duration.between(start, end);
    }
}
```

Note that the frame listener interface is moved to this new class.

The _frame_ can be started:

```java
public void start() {
    if(running) throw new IllegalStateException();
    start = Instant.now();
    running = true;
}
```

And stopped on completion of some activity:

```java
public void end() {
    if(!running) throw new IllegalStateException();
    this.end = Instant.now();
    this.running = false;
}
```

This timer is used by an _animator_ which interpolates an _animation_ over a given duration:

```java
public class Animator extends AbstractPlayable implements Frame.Listener {
    /**
     * An <i>animation</i> is updated by this animator.
     */
    @FunctionalInterface
    public interface Animation {
        /**
         * Updates this animation.
         * @param animator Animator
         */
        void update(Animator animator);
    }

    // Configuration
    private final Animation animation;
    private final long duration;
    private float speed = 1;
    private boolean repeat = true;

    // Animation state
    private final Frame frame = new Frame();
    private long time;
    private float pos;
}
```

The animator ignores frame events if it has been stopped or paused:

```java
public void update() {
    if(!isPlaying()) {
        return;
    }
    ...
}
```

Next the elapsed duration since the previous frame is calculated:

```
frame.end();
time += frame.elapsed().toMillis() * speed;
```

At the end of the duration the current time is quantised or the animation stops if it not repeating:

```java
if(time > duration) {
    if(repeat) {
        time = time % duration;
    }
    else {
        super.stop();
        time = duration;
    }
}
```

The updated position is then applied to the animation:

```java
pos = time / (float) duration;
animation.update(this);
```

And finally the timer is restarted for the next frame:

```java
if(isPlaying()) {
    frame.start();
}
```

### Rotation

Next a second new abstraction is introduced for a general matrix transformation:

```java
@FunctionalInterface
public interface Transform {
    /**
     * @return Transformation matrix
     */
    Matrix matrix();

    /**
     * @return Whether this transform has changed (default is {@code false})
     */
    default boolean isDirty() {
        return false;
    }
}
```

Which is sub-classed by a specialised rotation transform:

```java
public interface Rotation extends Transform {
    /**
     * @return This rotation as an axis-angle
     */
    AxisAngle rotation();
}
```

Where an axis-angle is a simple tuple type:

```java
record AxisAngle(Vector axis, float angle) implements Rotation {
    @Override
    public AxisAngle rotation() {
        return this;
    }

    @Override
    public Matrix matrix() {
        ...
    }
}
```


The implementation of the `matrix` method for the new types is covered below.

### Quaternions

A _quaternion_ is a more compact and efficient representation of a rotation but is less intuitive to use and comprehend.

See [Wikipedia](https://en.wikipedia.org/wiki/Quaternions_and_spatial_rotation).

Generally quaternions are used to represent a rotation about an _arbitrary_ axis or where multiple rotations are frequently composed, e.g. skeletal animation.

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
public AxisAngle rotation() {
    float scale = MathsUtil.inverseRoot(1 - w * w);
    float angle = 2 * MathsUtil.acos(w);
    Vector axis = new Vector(x, y, z).multiply(scale);
    return new AxisAngle(axis, angle);
}
```

The aim here is to use the existing code to construct a rotation matrix for the cardinal axes, but use a quaternion for the more general case of an arbitrary axis.  This is based on the following assumptions:

* Constructing a rotation matrix for a cardinal axis is more efficient than generating it from a quaternion.

* Many use-cases will rotate about a cardinal axis.

* The existing matrix code disallows the case of an arbitrary axis forcing the application to decide which implementation to use (or requires an awkward test to differentiate between cardinal and arbitrary axes).

Therefore a new vector implementation is introduced for cardinal axes which can then generate a rotation matrix.

First we take the opportunity to introduce an intermediate type for a normalised vector:

```java
public class NormalizedVector extends Vector {
    /**
     * @param len Vector length
     * @return Whether the given vector length is normalised
     */
    protected static boolean isNormalized(float len) {
        return MathsUtil.isEqual(1, len);
    }

    @Override
    public float magnitude() {
        return 1;
    }

    @Override
    public NormalizedVector normalize() {
        return this;
    }
}
```

The `normalize` method of the vector class is refactored accordingly.

Next the cardinal `Axis` is implemented and the existing constants are moved from the vector class:

```java
public abstract class Axis extends NormalizedVector {
    public static final Axis X = ...
    public static final Axis Y = ...
    public static final Axis Z = ...
    
    /**
     * Constructs the rotation matrix for this axis.
     */
    protected abstract void rotation(float sin, float cos, Matrix.Builder matrix);
}
```

The existing matrix building code is moved to this new type:

```java
public Matrix rotation(float angle) {
    var matrix = new Matrix.Builder().identity();
    float sin = MathsUtil.sin(angle);
    float cos = MathsUtil.cos(angle);
    rotation(sin, cos, matrix);
    return matrix.build();
}
```

Which delegates to the specific implementation for each axis, for example:

```java
public static final Axis X = new Axis(new Vector(1, 0, 0)) {
    @Override
    protected void rotation(float sin, float cos, Builder matrix) {
        matrix.set(1, 1, cos);
        matrix.set(1, 2, -sin);
        matrix.set(2, 1, sin);
        matrix.set(2, 2, cos);
    }
};
```

Finally the `matrix` method in the axis-angle can now select the appropriate mechanism to build a rotation matrix based on type rather than the awkward switching logic used previously:

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

Note that a quaternion also implements `Rotation` so an application now has the option of explicitly choosing an implementation or leaving the responsibility to the framework.

### Integration

The final new type is an implementation for a mutable rotation:

```java
public class MutableRotation implements Rotation {
    private final Vector axis;
    private float angle;

    public void angle(float angle) {
        this.angle = angle;
    }

    @Override
    public Matrix matrix() {
        var rot = new AxisAngle(axis, angle);
        return rot.matrix();
    }
}
```

Which is then adapted into a rotation animation:

```java
public class RotationAnimation implements Animation {
    private final MutableRotation rot;

    public RotationAnimation(Vector axis) {
        this.rot = new MutableRotation(axis.normalize());
    }

    @Override
    public void update(Animator animator) {
        float angle = animator.position() * MathsUtil.TWO_PI;
        rot.angle(angle);
    }
}
```

Note that the rotation axis is normalised in the constructor of the mutable rotation, otherwise the results will be interesting!

In the cube demo the existing hand-crafted matrix code is replaced by a rotation animation:

```java
@Bean
static RotationAnimation rotation() {
    return new RotationAnimation(new Vector(MathsUtil.HALF, 1, 0));
}
```

The animation and timing logic is replaced by an animator:

```java
@Bean
static Animator animator(ApplicationConfiguration cfg, RotationAnimation rot) {
    return new Animator(cfg.getPeriod(), rot);
}
```

Which is controlled by a player:

```java
@Bean
public static Player player(Animator animator) {
    Player player = new Player();
    player.add(animator);
    player.state(Playable.State.PLAY);
    player.repeat(true);
    return player;
}
```

And finally the code to update the uniform buffer is refactored accordingly:

```java
public static FrameListener update(ResourceBuffer uniform, Matrix projection, Matrix view, RotationAnimation rot) {
    ByteBuffer bb = uniform.buffer();
    return (time, elapsed) -> {
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

- The addition of a new framework for animations.

This might appear a lot of work for little benefit, however we now have a framework which can be more easily extended in future chapters, in particular when input devices and scene graphs are introduced later.

