---
title: Vulkan Devices
---

## Overview

Hardware components that support Vulkan are represented by a _physical device_ which defines the capabilities of that component (rendering, data transfer, etc).  In general there will be a single physical device (i.e. the GPU) or perhaps also an on-board graphics device for a laptop.

A _logical device_ is an instance of a physical device and is central component for all subsequent Vulkan functionality.

The process of creating the logical device is:

1. Enumerate the physical devices available on the hardware.

2. Select one that satisfies the requirements of the application.

3. Create a _logical device_ for the selected physical device.

4. Retrieve the necessary _work queues_ used to perform rendering and other graphics tasks.

For our demo we need to choose a device that supports rendering so we will also extend the _desktop_ library to create a window with a Vulkan rendering surface.

---

## Physical Devices

### Device and Queues

We first implement new domain objects for the physical device and the supported work queues:

```java
public class PhysicalDevice {
    private final Pointer handle;
    private final Instance instance;
    private final List<Family> families;

    /**
     * Constructor.
     * @param handle        Device handle
     * @param instance      Parent instance
     * @param families      Queue family descriptors
     */
    PhysicalDevice(Pointer handle, Instance instance, List<Family> families) {
        this.handle = notNull(handle);
        this.instance = notNull(instance);
        this.families = List.copyOf(families);
    }

    public Pointer handle() {
        return handle;
    }

    public Instance instance() {
        return instance;
    }

    public List<Family> families() {
        return families;
    }
}
```

The physical device specifies a number of _queue families_ that define the capabilities of work that can be performed by that device:

```java
public record Queue(Pointer handle, Family family) {
    /**
     * A <i>queue family</i> defines the properties of a group of queues.
     */
    public record Family(int index, int count, Set<VkQueueFlag> flags) {
    }
}
```

The queue class itself will be fleshed out when we implement the logical device.

### Enumerating the Physical Devices

To enumerate the available physical devices we invoke the `vkEnumeratePhysicalDevices` API method _twice_:

1. Once to retrieve the number of available devices via an integer-by-reference value (the array parameter is set to `null`).

2. Again to retrieve the actual array of device handles.

We wrap this code in the following factory method:

```java
public static Stream<PhysicalDevice> devices(Instance instance) {
    // Determine array length
    final IntByReference count = lib.factory().integer();
    check(api.vkEnumeratePhysicalDevices(instance.handle(), count, null));

    // Allocate array
    final Pointer[] array = new Pointer[count.getValue()];

    // Retrieve array
    if(array.length > 0) {
        check(api.vkEnumeratePhysicalDevices(instance.handle(), count, array));
    }

    // Create devices
    return Arrays.stream(array).map(ptr -> create(ptr, instance));
}
```

The `create` method is a helper that retrieves the array of queue families for each device:

```java
private static PhysicalDevice create(Pointer handle, Instance instance) {
    // Count number of families
    final VulkanLibrary lib = instance.library();
    final IntByReference count = lib.factory().integer();
    lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, null);

    // Retrieve families
    final VkQueueFamilyProperties[] array;
    if(count.getValue() > 0) {
        array = (VkQueueFamilyProperties[]) new VkQueueFamilyProperties().toArray(count.getValue());
        lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array[0]);
    }
    else {
        array = Array.newInstance(VkQueueFamilyProperties.class, 0);
    }
    
    ...
}
```

Note that again we invoke the same API method twice to retrieve the queue families.  However in this case we use the JNA `toArray` factory method on an instance of a `VkQueueFamilyProperties` structure to allocate the array and pass the _first_ element to the API method (i.e. our array is equivalent to a native pointer-to-structure).  We will abstract this common pattern at the end of the chapter.

Finally we add another helper to create a queue family domain object:

```java
private static Family family(int index, VkQueueFamilyProperties props) {
    final Set<VkQueueFlag> flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
    return new Family(index, props.queueCount, flags);
}
```

Which is used to transform the array of structures when we create the device domain object:

```java
    // Create queue families
    final List<Family> families = IntStream
        .range(0, props.length)
        .mapToObj(n -> family(n, props[n]))
        .collect(toList());
    
    // Create device
    return new PhysicalDevice(handle, instance, families);
}
```

### Device Properties

A physical device exposes properties that we wrap into the following lazily-instantiated accessor:

```java
private final Supplier<Properties> props = new LazySupplier<>(Properties::new);

public Properties properties() {
    return props.get();
}

public class Properties {
    private final VkPhysicalDeviceProperties struct = new VkPhysicalDeviceProperties();

    private Properties() {
        instance.library().vkGetPhysicalDeviceProperties(handle, struct);
    }

    public String name() {
        return new String(struct.deviceName);
    }

    public VkPhysicalDeviceType type() {
        return struct.deviceType;
    }
}
```

We can now add the following temporary code to the demo to dump the available physical devices:

```java
PhysicalDevice
    .devices(instance)
    .map(PhysicalDevice::properties)
    .map(Properties::name)
    .forEach(System.out::println);
```

---

## Rendering Surface

### Application Window

To select the physical device that supports rendering we first need a suitable Vulkan surface.  Again we take advantage of the Vulkan support provided by the GLFW window library.

We start with a _window_ class that encapsulates a GLFW window handle:

```java
public class Window {
    private final Desktop desktop;
    private final Pointer handle;
    private final Descriptor descriptor;

    Window(Desktop desktop, Pointer window, Descriptor descriptor) {
        this.desktop = notNull(desktop);
        this.handle = notNull(window);
        this.descriptor = notNull(descriptor);
    }

    public void destroy() {
        lib.glfwDestroyWindow(handle);
    }
}
```

The _window descriptor_ wraps up the details of the window in a simple record:

```java
public record Descriptor(String title, Dimensions size, Set<Property> properties) {
}
```

The _properties_ is an enumeration of the various visual capabilities of a window (_hints_ in GLFW parlance):

```java
public enum Property {
    RESIZABLE(0x00020003),
    DECORATED(0x00020005),
    AUTO_ICONIFY(0x00020006),
    MAXIMISED(0x00020008),
    DISABLE_OPENGL(0x00022001),

    private final int hint;

    private Property(int hint) {
        this.hint = hint;
    }

    void apply(DesktopLibrary lib) {
        final int value = this == DISABLE_OPENGL ? 0 : 1;
        lib.glfwWindowHint(hint, value);
    }
}
```

(The constants are copied from the header file).

Finally we add the following factory method to create a window given its descriptor:

```java
public class Desktop {
    public Window window(Window.Descriptor descriptor) {
        // Apply window hints
        lib.glfwDefaultWindowHints();
        descriptor.properties().forEach(p -> p.apply(lib));
    
        // Create window
        final Dimensions size = descriptor.size();
        final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);
        if(window == null) {
            throw new RuntimeException(...);
        }
    
        // Create window wrapper
        return new Window(this, window, descriptor);
    }
}
```

Notes:

* This is a bare-bones implementation sufficient for the triangle demo, however we will almost certainly need to refactor this code to support richer functionality, e.g. full-screen windows.

* We add a convenience builder for a window.

* By default GLFW creates an OpenGL surface for a new window which is disabled using the `DISABLE_OPENGL` window hint.  (Also note the argument for this hint is annoyingly the opposite of what one would expect).

* The above implementation ignores display monitors for the moment.

### Vulkan Surface

To create a Vulkan surface we add the following factory to the new class:

```java
public Pointer surface(Pointer instance) {
    final DesktopLibrary lib = desktop.library();
    final PointerByReference ref = new PointerByReference();
    final int result = lib.glfwCreateWindowSurface(instance, this.handle(), null, ref);
    if(result != 0) {
        throw new RuntimeException(...);
    }
    return ref.getValue();
}
```

In the demo we can now create a window and retrieve the handle to the rendering surface:

```java
// Create window
Window window = new Window.Builder()
    .title("demo")
    .size(new Dimensions(1280, 760))
    .property(Window.Property.DISABLE_OPENGL)
    .build(desktop);

// Retrieve rendering surface
Pointer surface = window.surface(instance.handle());
```

---

## Device Selection

### Overview

As previously mentioned the process of selecting the physical device is slightly messy due to the inter-dependencies between the Vulkan instance, the rendering surface and the presentation queue.
This is exacerbated by the mechanism Vulkan uses to select the work queues - this essentially requires the same logic to be applied twice: once to select the physical device with the required queue capabilities, and again to retrieve each queue from the resultant logical device.

After trying several approaches we settled on the design described below which determines each queue family as a _side-effect_ of selecting the physical device in one operation.
Generally we would avoid a design that could lead to unpleasant surprises, but at least the nastier aspects are self-contained and the resultant API is at least relatively simple from the perspective of the user.

### Selector

The `Selector` combines a device predicate with an accessor for the selected queue family (the side effect):

```java
public static class Selector implements Predicate<PhysicalDevice> {
    private Optional<Family> family = Optional.empty();

    /**
     * @return Selected queue family
     * @throws NoSuchElementException if no queue was selected
     */
    public Family family() {
        return family.orElseThrow();
    }

    @Override
    public boolean test(PhysicalDevice dev) {
        ...
    }
}
```

Since the API method to test presentation is also dependant on the physical device the selector uses a bi-predicate that combines the device and family tests:

```java
public static class Selector implements Predicate<PhysicalDevice> {
    private final BiPredicate<PhysicalDevice, Family> predicate;

    public Selector(BiPredicate<PhysicalDevice, Family> predicate) {
        this.predicate = notNull(predicate);
    }
}
```

The `test` method determines the queue predicate for the device and finds the matching family if present:

```java
public boolean test(PhysicalDevice dev) {
    // Build filter for this device
    final Predicate<Family> filter = family -> predicate.test(dev, family);

    // Retrieve matching queue family
    family = dev.families.stream().filter(filter).findAny();

    // Selector passes if the queue is found
    return family.isPresent();
}
```

For the presentation case we implement a factory method that matches a given rendering surface:

```java
public static Selector of(Handle surface) {
    final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> dev.isPresentationSupported(surface, family);
    return new Selector(predicate);
}
```

This delegates to the following helper on the physical device:

```java
public boolean isPresentationSupported(Handle surface, Family family) {
    final VulkanLibrary lib = this.library();
    final IntByReference supported = lib.factory().integer();
    check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(this.handle(), family.index(), surface, supported));
    return supported.getValue() == 1; // TODO
}
```

Note that the API uses an `IntByReference` for the test result which maps __one__ to boolean _true_ (there is no explicit boolean by-reference type).  We will introduce proper boolean support in a later chapter.

Finally we add a second factory to create a selector based on general queue capabilities (the device aspect of the predicate is unused in this case):

```java
public static Selector of(VkQueueFlag... flags) {
    final var list = Arrays.asList(flags);
    final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> family.flags().containsAll(list);
    return new Selector(predicate);
}
```

An application may also be required to select the device based on its properties or available features but we defer these cases until later.

### Integration

We can now enumerate the available physical devices and use selectors to find one that supports the requirements of the demo:

```java
// Select a device that supports rendering
Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);

// Select a device that supports presentation
Selector presentation = Selector.of(surface);

// Find matching device
PhysicalDevice physical = PhysicalDevice
    .devices(instance)
    .filter(graphics)
    .filter(presentation)
    .findAny()
    .orElseThrow(() -> new RuntimeException("No suitable physical device available"));
```

Note that the selected families could actually be the same object depending on the hardware implementation but we allow for multiple return results.

---












## Logical Device

### Domain Classes

The _logical device_ is an instance of the physical device we have selected from those available on the hardware.

Again we start with an outline for the logical device:

```java
public class LogicalDevice {
    private final Pointer handle;
    private final PhysicalDevice parent;
    private final VulkanLibrary lib;
    private final Map<Family, List<Queue>> queues;

    /**
     * Constructor.
     * @param handle Device handle
     * @param parent Parent physical device
     * @param queues Work queues
     */
    private LogicalDevice(Pointer handle, PhysicalDevice parent, Set<RequiredQueue> queues) {
        this.handle = notNull(handle);
        this.parent = notNull(parent);
        this.lib = parent.instance().library();
        this.queues = ...
    }

    public Map<Queue.Family, List<Queue>> queues() {
        return queues;
    }

    public void destroy() {
        lib.vkDestroyDevice(handle, null);
    }
}
```

The local `RequiredQueue` class is a transient descriptor for a work queue that is required by the application:

```java
private record RequiredQueue(Family family, List<Percentile> priorities) {
    /**
     * Populates a descriptor for a queue required by this device.
     */
    private void populate(VkDeviceQueueCreateInfo info) {
    }
}
```

Notes:

- The _family_ is a queue family selected from the parent physical device.

- The _priorities_ is a list of percentile values that specifies the priority of each queue.

- The `Percentile` class is a custom wrapper for a percentile represented as a 0..1 floating-point value (covered in the next chapter).

- The constructor (not shown) validates the queue specification.

The logical device is highly configurable so we create a builder:

```java
public static class Builder {
    private PhysicalDevice parent;
    private final Set<String> extensions = new HashSet<>();
    private final Set<String> layers = new HashSet<>();
    private final Set<RequiredQueue> queues = new ArrayList<>();

    ...

    public LogicalDevice build() {
        // Create descriptor
        final VkDeviceCreateInfo info = new VkDeviceCreateInfo();

        // Add required extensions
        info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
        info.enabledExtensionCount = extensions.size();

        // Add validation layers
        info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
        info.enabledLayerCount = layers.size();

        // Add queue descriptors
        ...

        // Allocate device
        final VulkanLibrary lib = parent.instance().library();
        final PointerByReference logical = lib.factory().pointer();
        check(lib.vkCreateDevice(parent.handle(), info, null, logical));

        // Create logical device
        return new LogicalDevice(logical.getValue(), parent, queues);
    }
}
```

We can specify extensions and validation layers at both the instance and device level, e.g. for the swap-chain.
Note more recent Vulkan implementations will ignore validation layers specified at the device level (we retain both for backwards compatibility).

The builder provides several over-loaded methods to specify one or more required work queues:

```java
public Builder queue(Family family) {
    return queues(family, 1);
}

public Builder queues(Family family, int num) {
    return queues(family, Collections.nCopies(num, 1f));
}

public Builder queues(Family family, List<Float> priorities) {
    if(!parent.families().contains(family)) throw new IllegalArgumentException(...);
    queues.add(new RequiredQueue(family, priorities));
    return this;
}
```

Note that the required queues is a _set_ to avoid duplicates since the physical device may return the same family for different queue specifications (depending on the hardware implementation).

The resultant list of required queues has two purposes:

1. Specify the required queues for the device when we invoke the API.

2. Construct the queue instances for the logical device

We populate the array of required queues in the descriptor for the logical device:

```java
// Add queue descriptors
info.queueCreateInfoCount = queues.size();
info.pQueueCreateInfos = StructureCollector.toPointer(VkDeviceQueueCreateInfo::new, queues, RequiredQueue::populate);
```

Notes:

- JNA requires a contiguous memory block for the array (which is actually a native pointer-to-array type).

- The `StructureCollector` is detailed at the end of this chapter.

The `populate` method generates the descriptor for each `RequiredQueue`:

```java
private void populate(VkDeviceQueueCreateInfo info) {
    // Convert percentile priorities to array
    final float[] array = ArrayUtils.toPrimitive(priorities.stream().map(Percentile::floatValue).toArray(Float[]::new));

    // Allocate contiguous memory block for the priorities array
    final Memory mem = new Memory(priorities.size() * Float.BYTES);
    mem.write(0, array, 0, array.length);

    // Populate queue descriptor
    info.queueCount = array.length;
    info.queueFamilyIndex = family.index();
    info.pQueuePriorities = mem;
}
```

Note that the `pQueuePriorities` field is a contiguous block of floating-point values mapped to a `const float*` type.

### Work Queues

When the logical device domain object is instantiated the list of `RequiredQueue` is also passed as a constructor argument:

```java
private LogicalDevice(Pointer handle, PhysicalDevice parent, List<RequiredQueue> queues) {
    ...
    this.queues = queues.stream().flatMap(this::create).collect(groupingBy(Queue::family));
}
```

Each entry can generate one-or-more queue instances:

```java
private Stream<Queue> create(RequiredQueue queue) {
    return IntStream.range(0, queue.priorities.length).mapToObj(n -> create(n, queue.family));
}
```

Finally the handle for each queue is retrieved from Vulkan:

```java
private Queue create(int index, Family family) {
    final PointerByReference queue = lib.factory().pointer();
    lib.vkGetDeviceQueue(handle, family.index(), index, queue);
    return new Queue(queue.getValue(), this, family);
}
```

The builder for the logical device is quite complex so the unit-test has a large number of failure cases (a selection are shown here):

```java
@Nested
class BuilderTests {
    private LogicalDevice.Builder builder;

    @BeforeEach
    void before() {
        builder = new LogicalDevice.Builder().parent(parent);
    }

    @Test
    void invalidPriority() {
        assertThrows(IllegalArgumentException.class, () -> builder.queues(family, new float[]{2}).build());
    }

    @Test
    void invalidQueueCount() {
        assertThrows(IllegalArgumentException.class, () -> builder.queues(family, 3).build());
    }

    @Test
    void invalidQueueFamily() {
        assertThrows(IllegalArgumentException.class, () -> builder.queue(mock(QueueFamily.class)).build());
    }
    
    ...
}
```

We can now create a logical device in the demo and lookup the specified work queues:

```java
// Create device
final LogicalDevice dev = new LogicalDevice.Builder(gpu)
    .extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
    .layer(ValidationLayer.STANDARD_VALIDATION)
    .queue(graphicsFamily)
    .queue(presentationFamily)
    .build();
    
// Lookup work queues
final Queue graphicsQueue = dev.queue().get(graphicsFamily).get(0);
final Queue presentationQueue = dev.queues().get(presentationFamily).get(0);
```

### Queue Selector

When reviewing the demo code as it stands we see that the process of specifying and retrieving the work queues is rather awkward.

For each queue we currently have three objects:

- A predicate used to select the physical device.

- The queue family specifying the required queues when creating the logical device.

- And finally the work queue itself.

In addition the logic to retrieve the queues from the logical device is pretty ugly.

Ideally we would like to compose these concerns into a single object that supports all the use-cases.

We create a _selector_ that composes the queue predicate and a _reference_ to the family:

```java
public static class Selector extends Reference implements Predicate<PhysicalDevice> {
    private final Predicate<Family> predicate;

    public Selector(Predicate<Family> predicate) {
        this.predicate = notNull(predicate);
    }

    @Override
    public boolean test(PhysicalDevice dev) {
        ...
    }
}
```

The `Reference` is basically a hidden accessor for the family:

```java
static abstract class Reference {
    /**
     * @return Queue family
     */
    abstract Family family();
}
```

Which is implemented by the existing queue family:

```java
public static class Family extends Reference {
    @Override
    final Family family() {
        return this;
    }
}
```

We can now refactor the various `queue` methods in the builder for the logical device to accept this new type (i.e. so we can pass either a queue family or a selector):

The selector itself is implemented as follows:

```java
private Optional<Family> family = Optional.empty();

@Override
public boolean test(PhysicalDevice dev) {
    family = dev.families().stream().filter(predicate).findAny();
    return family.isPresent();
}

@Override
public Family family() {
    return family.orElseThrow();
}
```

Note that we initialise the family as part of the predicate test (see below).

We add convenience factory methods to create selectors:

```java
public static Selector of(VkQueueFlag... flags) {
    final var set = Arrays.asList(flags);
    return new Selector(family -> family.flags.containsAll(set));
}

public static Selector of(Handle surface) {
    return new Selector(family -> family.isPresentationSupported(surface));
}
```

And helper methods to retrieve the queue(s) from the logical device:

```java
public List<Queue> list(LogicalDevice dev) {
    final var list = dev.queues().get(family());
    if(list == null) throw new NoSuchElementException("Queue family is not available");
    return list;
}

public Queue queue(LogicalDevice dev) {
    final var list = list(dev);
    return list.get(0);
}
```

We can now refactor the demo as follows:

```java
// Create queue selectors
var graphics = Queue.Selector.of(VkQueueFlag.GRAPHICS);
var present = Queue.Selector.of(surfaceHandle);

// Find GPU
PhysicalDevice gpu = PhysicalDevice
    .devices(instance)
    .filter(graphics)
    .filter(present)
    .findAny()
    .orElseThrow(() -> new RuntimeException("No GPU available"));

// Create logical device
LogicalDevice dev = new LogicalDevice.Builder(gpu)
    ...
    .queue(graphics)
    .queue(present)
    .build();

// Retrieve work queues
Queue graphicsQueue = graphics.queue();
Queue presentationQueue = present.queue();
```

This refactoring reduces the number of objects we have to deal with and simplifies the code, with minimal impact on the existing classes (the previous approach can still be used if required).

> A valid criticism of the selector is that fact that the queue family is initialised as a side-effect of the predicate. 
Obviously we would prefer to avoid this approach (particularly if the code was intended to be thread-safe) but for this use-case it does the job.

---

## Improvements

### Two-Stage Invocation

When enumerating the physical devices we first came across API methods that are invoked **twice** to retrieve an array from Vulkan.

The process is generally:
1. Invoke an API method with an integer-by-reference value to determine the length of the array (the array itself is `null`).
2. Allocate the array accordingly.
3. Invoke again to populate the array (passing the length value and the empty array).

This is a common pattern across the Vulkan API which we refer to as _two-stage invocation_.

We define the following interface to abstract a Vulkan API method that employs two-stage invocation:

```java
public interface VulkanFunction<T> {
    /**
     * Vulkan API method that retrieves an array of the given type.
     * @param lib       Vulkan library
     * @param count     Return-by-reference count of the number of array elements
     * @param array     Array instance or <code>null</code> to retrieve size of the array
     * @return Vulkan result code
     */
    int enumerate(VulkanLibrary lib, IntByReference count, T array);
}
```

The API method can now be defined as follows:

```java
VulkanFunction<Pointer[]> func = (api, count, devices) -> api.vkEnumeratePhysicalDevices(instance.handle(), count, devices);
```

We next add a helper method encapsulating the process described above:

```java
static <T> T[] enumerate(VulkanFunction<T[]> func, VulkanLibrary lib, IntFunction<T[]> factory) {
    // Determine array length
    final IntByReference count = lib.factory().integer();
    check(func.enumerate(lib, count, null));

    // Allocate array
    final T[] array = factory.apply(count.getValue());

    // Retrieve array
    if(array.length > 0) {
        check(func.enumerate(lib, count, array));
    }

    return array;
}
```

and refactor the code to enumerate the devices:

```java
public static Stream<PhysicalDevice> devices(Instance instance) {
    final VulkanFunction<Pointer[]> func = (api, count, devices) -> api.vkEnumeratePhysicalDevices(instance.handle(), count, devices);
    final Pointer[] handles = VulkanFunction.enumerate(func, instance.library(), Pointer[]::new);
    return Arrays.stream(handles).map(ptr -> create(ptr, instance));
}
```

This implementation is suitable for an array where the component type is automatically marshalled by JNA (such as the device pointers in the above example).

However for an array of JNA structures we need a second, slightly different implementation since the result **must** be a contiguous block of memory allocated using the `toArray` helper:

```java
static <T extends Structure> T[] enumerate(VulkanFunction<T> func, VulkanLibrary lib, Supplier<T> identity) {
    // Count number of values
    final IntByReference count = lib.factory().integer();
    check(func.enumerate(lib, count, null));

    // Retrieve values
    final T[] array = (T[]) identity.get().toArray(count.getValue());
    if(array.length > 0) {
        check(func.enumerate(lib, count, array[0]));
    }

    return array;
}
```

The _identity_ supplies an instance of the structure used to allocate the resultant array.
Note that in this case the API method accepts a pointer-to-structure which maps to the **first** element of the allocated array.

The code to retrieve the queue families now becomes:

```java
VkQueueFamilyProperties[] families = VulkanFunction.enumerate(func, instance.library(), VkQueueFamilyProperties::new);
```

This abstraction centralises the process of two-stage invocation reducing code complexity, duplication and testing.

### Supported Extensions and Layers

Although our demos simply assume that the expected extensions and validation layers are available (e.g. the swapchain extension and diagnostics layers)
a well-behaved application would adapt to those supported by the local Vulkan implementation.

With the `VulkanFunction` in place we can take the opportunity now to implement functionality to retrieve the supported extensions and validation layers.

The following new helper class encapsulated the logic for retrieval of the supported extensions and layers at either the device or instance level:

```java
public class Supported {
    private final Set<String> extensions;
    private final Set<ValidationLayer> layers;

    public Supported(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> extensionsFunction, VulkanFunction<VkLayerProperties> layersFunction) {
        this.extensions = extensions(lib, extensionsFunction);
        this.layers = layers(lib, layersFunction);
    }

    public Set<String> extensions() {
        return extensions;
    }

    public Set<ValidationLayer> layers() {
        return layers;
    }
}
```

The extensions are retrieved and converted to a set of strings using a local helper:

```java
private static Set<String> extensions(VulkanLibrary lib, VulkanFunction<VkExtensionProperties> extensions) {
    return Arrays
        .stream(VulkanFunction.enumerate(extensions, lib, VkExtensionProperties::new))
        .map(e -> e.extensionName)
        .map(String::new)
        .collect(toSet());
}
```

And similarly for the validation layers:

```java
private static Set<ValidationLayer> layers(VulkanLibrary lib, VulkanFunction<VkLayerProperties> layers) {
    return Arrays
        .stream(VulkanFunction.enumerate(layers, lib, VkLayerProperties::new))
        .map(layer -> new ValidationLayer(new String(layer.layerName), layer.implementationVersion))
        .collect(toSet());
}
```

This new class is used to retrieve the extensions and layers for a `PhysicalDevice` via a new on-demand accessor:

```java
public Supported supported() {
    VulkanFunction<VkExtensionProperties> extensions = (api, count, array) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, array);
    VulkanFunction<VkLayerProperties> layers = (api, count, array) -> api.vkEnumerateDeviceLayerProperties(handle, count, array);
    return new Supported(instance.library(), extensions, layers);
}
```

And at the implementation level via the following static accessor method on the `VulkanLibrary` interface itself:

```java
static Supported supported(VulkanLibrary lib) {
    VulkanFunction<VkExtensionProperties> extensions = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
    VulkanFunction<VkLayerProperties> layers = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
    return new Supported(lib, extensions, layers);
}
```

### Structure Collector

Vulkan makes heavy use of structures to configure a variety of objects and we are also often required to allocate and populate arrays of these structures.

However an array of JNA structures poses a number of problems:

- Unlike a standard POJO an array of JNA structures **must** be allocated using the JNA `toArray` helper method to create a contiguous memory block.

- This implies we must know the size of the data to allocate the array which imposes constrains on how we build the array (in particular whether we can employ Java streams).

- Additionally there are edge cases where the data is empty (or even `null`).

- Finally many API methods expect a pointer-to-array value, i.e. the **first** element of the array.

Whilst none of this is particularly difficult to overcome it can be tedious, error-prone, and resulting in less testable code.

To address the above issues we implement the following method that allocates and populates a JNA array from an arbitrary collection of domain objects:

```java
public class StructureCollector {
    public static <T, R extends Structure> R[] toArray(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
        // Check for empty data
        if(data.isEmpty()) {
            return null;
        }
    
        // Allocate contiguous array
        @SuppressWarnings("unchecked")
        final R[] array = (R[]) identity.get().toArray(data.size());
    
        // Populate array
        final Iterator<T> itr = data.iterator();
        for(final R element : array) {
            populate.accept(itr.next(), element);
        }
        assert !itr.hasNext();
    
        return array;
    }
}
```

Notes:

- T is the domain type, e.g. `RequiredQueue`

- R is the component type of the resultant JNA structure array, e.g. `VkDeviceQueueCreateInfo`

- The _identity_ is an instance of the structure used to allocate the array.

- The `populate` method 'fills' an array element from the corresponding domain object.

The edge cases are handled by the following:

```java
public static <T, R extends Structure> R toPointer(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
    final R[] array = toArray(data, identity, populate);

    if(array == null) {
        return null;
    }
    else {
        return array[0];
    }
}
```

In the logical device we use this helper to build the array of required queue descriptors:

```java
info.pQueueCreateInfos = StructureCollector.toPointer(queues, VkDeviceQueueCreateInfo::new, RequiredQueue::populate);
```

We also provide a more generalised custom stream collector:

```java
public class StructureCollector <T, R extends Structure> implements Collector<T, List<T>, R[]> {
    private final Supplier<R> identity;
    private final BiConsumer<T, R> populate;
    private final Set<Characteristics> chars;

    /**
     * Constructor.
     * @param identity      Identity structure
     * @param populate      Population function
     * @param chars         Stream characteristics
     */
    public StructureCollector(Supplier<R> identity, BiConsumer<T, R> populate, Characteristics... chars) {
        this.identity = notNull(identity);
        this.populate = notNull(populate);
        this.chars = Set.copyOf(Arrays.asList(chars));
    }

    @Override
    public Supplier<List<T>> supplier() {
        return ArrayList::new;
    }

    @Override
    public BiConsumer<List<T>, T> accumulator() {
        return List::add;
    }

    @Override
    public BinaryOperator<List<T>> combiner() {
        return (left, right) -> {
            left.addAll(right);
            return left;
        };
    }

    @Override
    public Function<List<T>, R[]> finisher() {
        return list -> toArray(list, identity, populate);
    }
}
```

Conclusions:

- The helper methods encapsulates the logic for allocation and population of the resultant array (reducing code duplication).

- Separating the iteration from the population logic simplifies application code.

- We _could_ have used JNA structures directly (instead of custom domain objects) but we would still need to perform a field-by-field copy to the contiguous block.

---

## Summary

In this chapter we:

- Created a GLFW window and Vulkan surface

- Enumerated the physical devices available on the local hardware and selected one appropriate to our application

- Created a logical device and the work queues we will need in subsequent chapters

- Added supporting functionality for _two stage invocation_ and population of structure arrays.

The API methods in this chapter are defined in the following JNA interfaces:
- `VulkanLibraryPhysicalDevice`
- `VulkanLibraryLogicalDevice`
- `VulkanLibrarySurface`
