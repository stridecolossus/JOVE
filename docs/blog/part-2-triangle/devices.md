---
title: Vulkan Devices
---

## Overview

Now we have a Vulkan instance we can use it to enumerate the _physical devices_ available on the local hardware and select one that satisfies the requirements of the application.

This will be partially dependant on having a Vulkan rendering surface so first we will extend the _desktop_ service to create a native GLFW window.

Finally we will create a _logical device_ from the selected physical device and specify the _work queues_ we will use in subsequent chapters.

---

## The Rendering Surface

### Application Window

The obvious starting point is a _window_ class that encapsulates a GLFW window handle:

```java
public class Window {
    private final Handle handle;
    private final DesktopLibrary lib;
    private final Descriptor descriptor;

    /**
     * Constructor.
     * @param window            Window handle
     * @param lib               GLFW API
     * @param descriptor        Window descriptor
     */
    private Window(Pointer window, DesktopLibrary lib, Descriptor descriptor) {
        this.handle = new Handle(window);
        this.lib = notNull(lib);
        this.descriptor = notNull(descriptor);
    }

    public void destroy() {
        lib.glfwDestroyWindow(handle);
    }
}
```

The _window descriptor_ wraps up the details of the window in a simple class with a convenience builder:

```java
public record Descriptor(String title, Dimensions size, Set<Property> properties) {
    public static class Builder {
        private String title;
        private Dimensions size;
        private final Set<Property> props = new HashSet<>();
        
        ...
        
        public Builder property(Property p) {
            props.add(p);
            return this;
        }

        public Descriptor build() {
            return new Descriptor(title, size, props);
        }
    }
}
```

The properties is an enumeration that maps to the various GLFW window hints:

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

    /**
     * Applies this property.
     * @param lib Desktop library
     */
    void apply(DesktopLibrary lib) {
        final int value = this == DISABLE_OPENGL ? 0 : 1; // TODO
        lib.glfwWindowHint(hint, value);
    }
}
```

This is just the bare-bones we need for the forseeable future but we are likely to need to refactor the descriptor and properties to support other functionality, e.g. full-screen windows.

Finally we add a factory method to create a window:

```java
/**
 * Creates a GLFW window.
 * @param lib                GLFW library
 * @param descriptor        Window descriptor
 * @return New window
 * @throws RuntimeException if the window cannot be created
 */
public static Window create(DesktopLibrary lib, Descriptor descriptor) {
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
    return new Window(window, lib, descriptor);
}
```

### Vulkan Surface

To create a Vulkan surface for a given window we add the following to the new class:

```java
public Handle surface(Handle instance) {
    final PointerByReference ref = new PointerByReference();
    final int result = lib.glfwCreateWindowSurface(instance, this.handle(), null, ref);
    if(result != 0) {
        throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
    }
    return new Handle(ref.getValue());
}
```

We can now create a window and retrieve the surface in the demo:

```java
// Create instance
Instance instance = ...

// Create window
Window window = new Window.Builder(desktop)
    .title("demo")
    .size(new Dimensions(1280, 760))
    .property(Window.Property.DISABLE_OPENGL)
    .build();

// Retrieve rendering surface
Handle surface = window.surface(instance.handle());
```

The `DISABLE_OPENGL` property specifies that the new window should **not** create an OpenGL context (which GLFW does by default).

---

## Physical Devices

### Domain Classes

A _physical device_ represents a hardware component that supports Vulkan, i.e. the GPU.

The first step is an outline domain class for the physical device:

```java
public class PhysicalDevice {
    private final Pointer handle;
    private final Instance instance;
    private final List<Queue.Family> families;

    /**
     * Constructor.
     * @param handle        Device handle
     * @param instance        Parent instance
     * @param families        Queue family descriptors
     */
    PhysicalDevice(Pointer handle, Instance instance, VkQueueFamilyProperties[] families) {
        this.handle = notNull(handle);
        this.instance = notNull(instance);
        this.families = ...
    }

    public Pointer handle() {
        return handle;
    }

    public Instance instance() {
        return instance;
    }

    public List<Queue.Family> families() {
        return families;
    }
}
```

The device specifies a number of _queue families_ that define the capabilities of work that can be performed by that device (such as rendering, data transfer, etc).

We create a new domain object for a _queue_ and its family:

```java
public class Queue {
    /**
     * A <i>queue family</i> defines the properties of a group of queues.
     */
    public static class Family {
        private final PhysicalDevice dev;
        private final int count;
        private final int index;
        private final Set<VkQueueFlag> flags;

        /**
         * Constructor.
         * @param dev        Physical device
         * @param index        Family index
         * @param count        Number of queues
         * @param flags        Queue flags
         */
        Family(PhysicalDevice dev, int index, int count, Set<VkQueueFlag> flags) {
            this.dev = notNull(dev);
            this.index = zeroOrMore(index);
            this.count = oneOrMore(count);
            this.flags = Set.copyOf(flags);
        }
    }
}
```

The queue class itself will be fleshed out when we implement the logical device.

The queue families can now be created in the constructor from a set of Vulkan descriptors:

```java
{
    ...
    this.families = IntStream.range(0, families.length).mapToObj(n -> family(n, families[n])).collect(toList());
}

private Queue.Family family(int index, VkQueueFamilyProperties props) {
    final var flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
    return new Queue.Family(this, index, props.queueCount, flags);
}
```

We also implement a lazily instantiated accessor for the properties of the device:

```java
private final Supplier<Properties> props = new LazySupplier<>(Properties::new);

public Properties properties() {
    return props.get();
}

public class Properties {
    @SuppressWarnings("hiding")
    private final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();

    private Properties() {
        instance.library().vkGetPhysicalDeviceProperties(handle, props);
    }

    public String name() {
        return new String(props.deviceName);
    }

    public VkPhysicalDeviceType type() {
        return props.deviceType;
    }

    public VkPhysicalDeviceLimits limits() {
        return props.limits;
    }

    @Override
    public String toString() {
        return props.toString();
    }
}
```

### Enumerating the Physical Devices

To enumerate the available physical devices we invoke the `vkEnumeratePhysicalDevices()` API method _twice_:

1. Once to retrieve the number of devices via an integer-by-reference value (the array parameter is set to null).

2. And then again to retrieve the array of device handles given this value.

We wrap this code in a static factory:

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

Which delegates to a local helper that retrieves the queue families for each device and creates the domain object:

```java
private static PhysicalDevice create(Pointer handle, Instance instance) {
    // Count number of families
    final VulkanLibrary lib = instance.library();
    final IntByReference count = lib.factory().integer();
    lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, null);

    // Retrieve families
    final VkQueueFamilyProperties[] array;
    if(count.getValue() > 0) {
        array = (T[]) new VkQueueFamilyProperties().toArray(count.getValue());
        lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array[0]);
    }
    else {
        array = (T[]) Array.newInstance(VkQueueFamilyProperties.class, 0);
    }
    
    // Create device
    return new PhysicalDevice(handle, instance, families);
}
```

Note that again we invoke the same API method twice to retrieve the queue families.
However in this case we use `toArray()` on an instance of a `VkQueueFamilyProperties` structure to allocate the array and pass the _first_ element to the API method
(i.e. our array is equivalent to a native pointer-to-structure).
This is the standard approach for an array of JNA structures, we will abstract this common pattern at the end of the chapter.

We can now add some temporary code to the demo to dump the available physical devices:

```java
PhysicalDevice
    .devices(instance)
    .map(PhysicalDevice::properties)
    .map(Properties::name)
    .forEach(System.out::println);
```

On a normal PC there will generally just be one or two devices (the GPU and maybe an on-board graphics card).

### Selecting a Device

The final step for dealing with the physical devices is to select one that supports the requirements of an application.

There are several properties that we can select on:
1. The properties of a queue family provided by the device.
2. Whether the device supports rendering to the Vulkan surface.
3. The supported _features_ of the device (covered in later chapters).

The properties of a queue family are defined by the `VkQueueFlag` enumeration and specify whether the queue supports graphics rendering, data transfer, etc.

We add the following helper factory to the queue family class to create a predicate for a set of queue flags:

```java
public static Predicate<Family> predicate(VkQueueFlag... flags) {
    final var list = Arrays.asList(flags);
    return family -> family.flags().containsAll(list);
}
```

And another to test whether the family supports presentation to a given surface:

```java
public static Predicate<Family> predicate(Handle surface) {
    return family -> family.isPresentationSupported(surface);
}
```

Which delegates to the following method:

```java
public boolean isPresentationSupported(Handle surface) {
    final VulkanLibrary lib = dev.instance().library();
    final IntByReference supported = lib.factory().integer();
    check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(dev.handle(), index, surface, supported));
    return VulkanBoolean.of(supported.getValue()).toBoolean();
}
```

We can then walk the available devices and find one that matches our requirements:

```java
var graphicsPredicate = Queue.Family.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
var presentationPredicate = Queue.Family.predicate(surfaceHandle);

PhysicalDevice gpu = PhysicalDevice
    .devices(instance)
    .filter(PhysicalDevice.predicate(graphicsPredicate))
    .filter(PhysicalDevice.predicate(presentationPredicate))
    .findAny()
    .orElseThrow(() -> new RuntimeException("No GPU available"));
```

Finally we add the following helper to the physical device to select a queue family:

```java
public Queue.Family family(Predicate<Queue.Family> test) {
    return families.stream().filter(test).findAny().orElseThrow();
}
```

And in the demo we extract the families from the selected device:

```java
Queue.Family graphicsFamily = gpu.family(graphicsPredicate);
Queue.Family presentFamily = gpu.family(presentationPredicate);
```

Note that these could actually be the same object depending on how the GPU implements its queues.

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
    private final Map<Queue.Family, List<Queue>> queues;

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
        this.queues = ... // TODO
    }

    public void destroy() {
        lib.vkDestroyDevice(handle, null);
    }
}
```

The local `RequiredQueue` class is a transient descriptor for a work queue that is required by the application:

```java
private record RequiredQueue(Queue.Family family, List<Percentile> priorities) {
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
public Builder queue(Queue.Family family) {
    return queues(family, 1);
}

public Builder queues(Queue.Family family, int num) {
    return queues(family, Collections.nCopies(num, 1f));
}

public Builder queues(Queue.Family family, List<Float> priorities) {
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

The `populate()` method generates the descriptor for each `RequiredQueue`:

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
private Queue create(int index, Queue.Family family) {
    final PointerByReference queue = lib.factory().pointer();
    lib.vkGetDeviceQueue(handle, family.index(), index, queue);
    return new Queue(queue.getValue(), this, family);
}
```

We implement several over-loaded accessors to lookup a work queue from the device:

```java
/**
 * @return Work queues for this device ordered by family
 */
public Map<Queue.Family, List<Queue>> queues() {
    return queues;
}

/**
 * Helper - Looks up the work queue(s) for the given family.
 * @param family Queue family
 * @return Queue(s)
 * @throws IllegalArgumentException if this device does not contain queues with the given family
 */
public List<Queue> queues(Queue.Family family) {
    final var list = queues.get(family);
    if(list == null) throw new IllegalArgumentException("Queue family not present: " + family);
    return list;
}

/**
 * Helper - Looks up the <b>first</b> work queue for the given family.
 * @param family Queue family
 * @return Queue
 * @throws IllegalArgumentException if this device does not contain a queue with the given family
 */
public Queue queue(Queue.Family family) {
    return queues(family).get(0);
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

### Integration

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
final Queue graphicsQueue = dev.queue(graphicsFamily);    
final Queue presentationQueue = dev.queue(presentationFamily);
```

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
        .map(layer -> new ValidationLayer(Native.toString(layer.layerName), layer.implementationVersion))
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

- Unlike a standard POJO an array of JNA structures **must** be allocated using the JNA `toArray()` helper method to create a contiguous memory block.

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
