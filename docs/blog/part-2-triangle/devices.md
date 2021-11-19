---
title: Vulkan Devices
---

## Overview

Hardware components that support Vulkan are represented by a _physical device_ which defines the capabilities of that component (rendering, data transfer, etc).  In general there will be a single physical device (i.e. the GPU) or perhaps also an on-board graphics device for a laptop.

A _logical device_ is an instance of a physical device and is the central component for all subsequent Vulkan functionality.

The logical device also exposes a number of asynchronous _work queues_ that are used for various tasks such as invoking rendering, transferring data to/from the hardware, etc.  The process of displaying a rendered frame is known as _presentation_ and is implemented by Vulkan as a task on the _presentation queue_.

Creating the logical device consists of the following steps:

1. Enumerate the physical devices available on the hardware.

2. Select one that satisfies the requirements of the application.

3. Create a _logical device_ for the selected physical device.

4. Retrieve the required work queues.

For our demo we need to choose a device that supports presentation so we will also extend the _desktop_ library to create a window and a Vulkan rendering surface.

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

### Enumerating the Physical Devices

To enumerate the available physical devices we invoke the `vkEnumeratePhysicalDevices` API method _twice_:

1. Once to retrieve the number of available devices via an integer-by-reference value (the array parameter is set to `null`).

2. Again to retrieve the actual array of device handles.

We wrap this code in the following factory method:

```java
public static Stream<PhysicalDevice> devices(Instance instance) {
    // Determine array length
    IntByReference count = instance.factory().integer();
    check(api.vkEnumeratePhysicalDevices(instance.handle(), count, null));

    // Allocate array
    Pointer[] array = new Pointer[count.getValue()];

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
    VulkanLibrary lib = instance.library();
    IntByReference count = instance.factory().integer();
    lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, null);

    // Retrieve families
    VkQueueFamilyProperties[] array;
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

Note that again we invoke the same API method twice to retrieve the queue families.  However in this case we use the JNA `toArray` factory method on an instance of a `VkQueueFamilyProperties` structure to allocate the array and pass the _first_ element to the API method (i.e. the array maps to a native pointer-to-structure).  We will abstract this common pattern at the end of the chapter.

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

Finally we add a new interface to the Vulkan library for the various API methods used above:

```java
interface VulkanLibraryPhysicalDevice {
    int vkEnumeratePhysicalDevices(Pointer instance, IntByReference count, Pointer[] devices);
    void vkGetPhysicalDeviceProperties(Pointer device, VkPhysicalDeviceProperties props);
    void vkGetPhysicalDeviceQueueFamilyProperties(Pointer device, IntByReference count, VkQueueFamilyProperties props);
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

The _properties_ is an enumeration of the various visual capabilities of a window:

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
        int value = this == DISABLE_OPENGL ? 0 : 1;
        lib.glfwWindowHint(hint, value);
    }
}
```

The enumeration values (window _hints_ in GLFW parlance) are copied from the header file.

A hint is applied to a new window by the following helper:

```java
void apply(DesktopLibrary lib) {
    int value = this == DISABLE_OPENGL ? 0 : 1;
    lib.glfwWindowHint(hint, value);
}
```

Finally we add a factory method to create a window given its descriptor, starting with the window hints:

```java
public static Window create(Desktop desktop, Descriptor descriptor) {
    DesktopLibrary lib = desktop.library();
    lib.glfwDefaultWindowHints();
    for(Property p : descriptor.properties) {
        p.apply(lib);
    }
    ...
}
```

Next we invoke the API to instantiate the window and retrieve the handle:

```java
Dimensions size = descriptor.size();
Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);
if(window == null) throw new RuntimeException(...);
```

And finally we create the window domain object:

```java
return new Window(desktop, window, descriptor);
```

Notes:

* This is a bare-bones implementation sufficient for the triangle demo, however we will almost certainly need to refactor this code to support richer functionality, e.g. full-screen windows.

* We add a convenience builder for a window descriptor.

* By default GLFW creates an OpenGL surface for a new window which is disabled using the `DISABLE_OPENGL` window hint.

* The argument for this hint is irritatingly the opposite of what one would expect (and all the other hints).

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

After trying several approaches we settled on the design described below which determines each queue family as a _side-effect_ of selecting the physical device in one operation (the same approach as that used in the tutorial).  Generally we would avoid such a design that could lead to unpleasant surprises, but at least the nastier aspects are self-contained and the resultant API is relatively simple from the perspective of the user.

### Selector

The `Selector` is a device predicate with an accessor for the selected queue family (the side effect):

```java
public static abstract class Selector implements Predicate<PhysicalDevice> {
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

This new helper class is a template implementation, the following method generates the queue family predicate for a device:

```java
/**
 * Constructs the queue family filter for the given physical device.
 * @param dev Physical device
 * @return Queue family predicate
 */
protected abstract Predicate<Family> predicate(PhysicalDevice dev);
```

The `test` method retrieves the queue predicate and finds the matching family if present:

```java
public boolean test(PhysicalDevice dev) {
    // Build filter for this device
    final Predicate<Family> predicate = predicate(dev);

    // Retrieve matching queue family
    family = dev.families.stream().filter(predicate).findAny();

    // Selector passes if the queue is found
    return family.isPresent();
}
```

For the presentation case we implement a factory method that matches the rendering surface:

```java
public static Selector of(Handle surface) {
    return new Selector() {
        @Override
        protected Predicate<Family> predicate(PhysicalDevice dev) {
            return family -> dev.isPresentationSupported(surface, family);
        }
    };
}
```

This delegates to the following helper on the physical device:

```java
public boolean isPresentationSupported(Handle surface, Family family) {
    final VulkanLibrary lib = this.library();
    final IntByReference supported = instance.factory().integer();
    check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(this.handle(), family.index(), surface, supported));
    return supported.getValue() == 1; // TODO
}
```

Which invokes a new API interface:

```java
public interface VulkanLibrarySurface {
    /**
     * Queries whether a queue family supports presentation for the given surface.
     * @param device                Physical device handle
     * @param queueFamilyIndex      Queue family
     * @param surface               Vulkan surface
     * @param supported             Returned boolean flag
     * @return Result
     */
    int vkGetPhysicalDeviceSurfaceSupportKHR(Pointer device, int queueFamilyIndex, Pointer surface, IntByReference supported);
}
```

Note that the API uses an `IntByReference` for the test result which maps __one__ to boolean _true_ (there is no explicit boolean by-reference type).  We will introduce proper boolean support in a later chapter.

Finally we add a second factory to create a selector based on general queue capabilities (the device is unused):

```java
public static Selector of(VkQueueFlag... flags) {
    final var list = Arrays.asList(flags);
    return new Selector() {
        @Override
        protected Predicate<Family> predicate(PhysicalDevice dev) {
            return family -> family.flags().containsAll(list);
        }
    };
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
PhysicalDevice gpu = PhysicalDevice
    .devices(instance)
    .filter(graphics)
    .filter(presentation)
    .findAny()
    .orElseThrow(() -> new RuntimeException("No suitable physical device available"));
```

Note that the selected queue families could actually refer to the same object (depending on the hardware implementation) but we allow for multiple return results.

---

## Logical Device

### Domain Object

The first cut domain object for the logical device is as follows:

```java
public class LogicalDevice {
    private final Pointer handle;
    private final PhysicalDevice parent;
    private final VulkanLibrary lib;
    private final Map<Family, List<Queue>> queues;

    LogicalDevice(Pointer handle, PhysicalDevice parent, Map<Family, List<Queue>> queues) {
        this.handle = notNull(handle);
        this.parent = notNull(parent);
        this.lib = parent.instance().library();
        this.queues = Map.copyOf(queues);
    }

    public void destroy() {
        lib.vkDestroyDevice(handle, null);
    }
}
```

We also provide accessors to retrieve the work queues:

```java
public Map<Family, List<Queue>> queues() {
    return queues;
}

/**
 * Helper - Retrieves the <i>first</i> queue of the given family.
 * @param family Queue family
 * @return Queue
 * @throws IllegalArgumentException if the queue is not present
 */
public Queue queue(Family family) {
    final var list = queues.get(family);
    if((list == null) || list.isEmpty()) throw new IllegalArgumentException(...);
    return list.get(0);
}
```

And a helper method to block execution until the device is idle:

```java
public void waitIdle() {
    check(lib.vkDeviceWaitIdle(handle));
}
```

Similarly for an individual queue:

```java
public record Queue {
    ...
    
    public void waitIdle(VulkanLibrary lib) {
        check(lib.vkQueueWaitIdle(handle));
    }
}
```

### Builder

The logical device is another object that is highly configurable and is therefore constructed via a builder (setters omitted for brevity):

```java
public static class Builder {
    private final PhysicalDevice parent;
    private final Set<String> extensions = new HashSet<>();
    private final Set<String> layers = new HashSet<>();
    private final Map<Family, List<Percentile>> queues = new HashMap<>();
}
```

The builder provides a number of over-loaded methods to specify _required_ work queues for the device (delegating to the final base implementation):

```java
public Builder queue(Family family) {
    return queues(family, 1);
}

public Builder queues(Family family, int num) {
    return queues(family, Collections.nCopies(num, Percentile.ONE));
}

public Builder queues(Family family, List<Percentile> priorities) {
    queues.put(family, List.copyOf(priorities));
    return this;
}
```

Notes:

- The _family_ is a queue family selected from the parent physical device.

- The _priorities_ is a list of percentile values that specifies the priority of each required queue.

- The `Percentile` class is a custom type for a percentile represented as a 0..1 floating-point value.

- We can specify extensions and validation layers at both the instance and device level, however more recent Vulkan implementations will ignore layers specified at the device level (we retain both for backwards compatibility).

The `build` method populates a Vulkan descriptor for the logical device and invokes the API:

```java
public LogicalDevice build() {
    // Create descriptor
    var info = new VkDeviceCreateInfo();

    // Add required extensions
    info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
    info.enabledExtensionCount = extensions.size();

    // Add validation layers
    info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
    info.enabledLayerCount = layers.size();

    // Add queue descriptors
    info.queueCreateInfoCount = queues.size();
    info.pQueueCreateInfos = StructureHelper.first(queues.entrySet(), VkDeviceQueueCreateInfo::new, Builder::populate);

    // Allocate device
    Instance instance = parent.instance();
    VulkanLibrary lib = instance.library();
    ReferenceFactory factory = instance.factory();
    PointerByReference handle = factory.pointer();
    check(lib.vkCreateDevice(parent, info, null, handle));
    
    ...
}
```

JNA requires a native array to be a contiguous memory block (as opposed to a Java array where the memory address of the elements is arbitrary).
Here we introduce the `StructureHelper` helper class (detailed at the end of the chapter) which handles the transformation of a Java collection to a JNA structure array.

The `populate` method is invoked by the helper to 'fill' a JNA structure from a domain object (in this case a map entry representing a group of required queues):

```java
public static void populate(Entry<Family, List<Percentile>> queue, VkDeviceQueueCreateInfo info) {
    // Convert priorities to array
    Float[] floats = queue.getValue().stream().map(Percentile::floatValue).toArray(Float[]::new);
    float[] array = ArrayUtils.toPrimitive(floats);

    // Allocate contiguous memory block for the priorities array
    Memory mem = new Memory(array.length * Float.BYTES);
    mem.write(0, array, 0, array.length);

    // Populate queue descriptor
    info.queueCount = array.length;
    info.queueFamilyIndex = queue.getKey().index();
    info.pQueuePriorities = mem;
}
```

Note that again we create a contiguous memory block for the array of queue priorities mapping to a `const float*` native type.

### Work Queues

To retrieve the work queues from the newly created device we transform the map of required queues to a number of instances of the `RequiredQueue` local class and finally create the logical device:

```java
public LogicalDevice build() {
    ...

    // Retrieve work queues
    class RequiredQueue {
        ...
    }
    Map<Family, List<Queue>> map = queues
        .entrySet()
        .stream()
        .map(RequiredQueue::new)
        .flatMap(RequiredQueue::stream)
        .collect(groupingBy(Queue::family));


    // Create logical device
    return new LogicalDevice(handle.getValue(), parent, map, types);
}
```

This local class is responsible for generating the requested number of work queues for each family:

```java
class RequiredQueue {
    private final Family family;
    private final int count;

    private RequiredQueue(Entry<Family, List<Percentile>> entry) {
        this.family = entry.getKey();
        this.count = entry.getValue().size();
    }

    private Stream<Queue> stream() {
        return IntStream.range(0, count).mapToObj(this::create);
    }

    private Queue create(int index) {
        PointerByReference ref = factory.pointer();
        lib.vkGetDeviceQueue(handle.getValue(), family.index(), index, ref);
        return new Queue(ref.getValue(), family);
    }
}
```

Notes:

* The `vkGetDeviceQueue` API method is invoked multiple times for each specified queue generated by the `stream` helper (the index is implicit).

* We use a local class to centralise the queue logic whilst retaining access to the API dependencies (the logical device handle and Vulkan library).

Finally we implement a new Vulkan library for the various API methods to manage the logical device and work queues:

```java
interface VulkanLibraryLogicalDevice {
    int vkCreateDevice(Pointer physicalDevice, VkDeviceCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference device);
    void vkDestroyDevice(Pointer device, Pointer pAllocator);
    void vkGetDeviceQueue(Pointer device, int queueFamilyIndex, int queueIndex, PointerByReference pQueue);
    int vkDeviceWaitIdle(Pointer device);
    int vkQueueWaitIdle(Pointer queue);
}
```

### Integration

We can now create the logical device and retrieve the work queues:

```java
// Create device
LogicalDevice dev = new LogicalDevice.Builder(gpu)
    .extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
    .layer(ValidationLayer.STANDARD_VALIDATION)
    .queue(graphics.family())
    .queue(presentation.family())
    .build();
    
// Lookup work queues
Queue graphicsQueue = dev.queue(graphics.family());
Queue presentationQueue = dev.queue(presentation.family());
```

---

## Improvements

### Two-Stage Invocation

When enumerating the physical devices we first came across API methods that are invoked __twice__ to retrieve an array from Vulkan.

The process is generally:

1. Invoke an API method with an integer-by-reference value to determine the length of the array (the array argument is `null`).

2. Allocate the array accordingly.

3. Invoke again to populate the empty array.

This is a common pattern across the Vulkan API which we refer to as _two-stage invocation_.

The following interface abstracts an API method that employs two-stage invocation:

```java
@FunctionalInterface
public interface VulkanFunction<T> {
    /**
     * Vulkan API method that retrieves data using the <i>two-stage invocation</i> approach.
     * @param count         Size of the data
     * @param data          Returned data or {@code null} to retrieve the size of the data
     * @return Vulkan result code
     */
    int enumerate(IntByReference count, T data);
}
```

The API method to enumerate the physical devices can now be defined thus:

```java
VulkanFunction<Pointer[]> func = (count, devices) -> lib.vkEnumeratePhysicalDevices(instance, count, devices);
```

Two-stage invocation of the function is encapsulated in the following helper:

```java
static <T> T invoke(VulkanFunction<T> func, IntByReference count, IntFunction<T> factory) {
    // Invoke to determine the size of the data
    check(func.enumerate(count, null));

    // Instantiate the data object
    int size = count.getValue();
    T data = factory.apply(size);

    // Invoke again to populate the data object
    if(size > 0) {
        check(func.enumerate(count, data));
    }

    return data;
}
```

The process of enumerating the physical devices can now be refactored to the following more concise code:

```java
public static Stream<PhysicalDevice> devices(Instance instance) {
    final VulkanFunction<Pointer[]> func = (count, devices) -> instance.library().vkEnumeratePhysicalDevices(instance, count, devices);
    final IntByReference count = instance.factory().integer();
    final Pointer[] handles = VulkanFunction.invoke(func, count, Pointer[]::new);
    return Arrays.stream(handles).map(ptr -> create(ptr, instance));
}
```

For an array of JNA structures we need a second, slightly different implementation since the array __must__ be a contiguous block of memory allocated using the `toArray` helper:

```java
static <T extends Structure> T[] invoke(VulkanFunction<T> func, IntByReference count, Supplier<T> identity) {
    // Invoke to determine the length of the array
    check(func.enumerate(count, null));

    // Instantiate the structure array
    T[] array = (T[]) identity.get().toArray(count.getValue());

    // Invoke again to populate the array (note passes first element)
    if(array.length > 0) {
        check(func.enumerate(count, array[0]));
    }

    return array;
}
```

Notes:

* The _identity_ generates an instance of the structure used to allocate the resultant array.

* Note that in this case the API method accepts a pointer-to-structure which maps to the __first__ element of the allocated array.

As an example, the code to retrieve the queue families for a physical device now becomes:

```java
VulkanFunction<VkQueueFamilyProperties> func = (count, array) -> instance.library().vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array);
IntByReference count = instance.factory().integer();
VkQueueFamilyProperties[] props = VulkanFunction.invoke(func, count, VkQueueFamilyProperties::new);
```

### Structure Collector

Vulkan makes heavy use of structures to configure a variety of objects.

However an array of JNA structures poses a number of problems:

- Unlike a standard POJO an array of JNA structures __must__ be allocated using the JNA `toArray` helper method to create a contiguous memory block.

- Obviously we must know the size of the data to allocate the array which imposes constraints on how we process data, in particular whether we can employ Java streams.

- Many API methods expect a pointer-to-array, i.e. the __first__ element of the array.

- Additionally there are edge cases where the data is empty or where `null` is a valid argument.

None of these are particularly difficult to overcome but the number of situations where this occurs makes the code tedious to develop, error-prone, and less testable.

To address these issues we implement the following helper that allocates and populates a JNA array from an arbitrary collection of domain objects:

```java
public final class StructureHelper {
    public static <T, R extends Structure> R[] array(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
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

- _T_ is the domain type, e.g. a map entry for a required queue in the logical device.

- _R_ is the array component type, e.g. `VkDeviceQueueCreateInfo`.

- _identity_ generates an instance of the structure used to allocate the array.

- the _populate_ method 'fills' a JNA structure from the corresponding domain object (note that JNA structures do not support copying or cloning).

For the case where the API method requires a pointer-to-array argument we provide the following alternative:

```java
public static <T, R extends Structure> R first(Collection<T> data, Supplier<R> identity, BiConsumer<T, R> populate) {
    final R[] array = array(data, identity, populate);
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
info.pQueueCreateInfos = StructureHelper.first(queues.entrySet(), VkDeviceQueueCreateInfo::new, Builder::populate);
```

Finally we also provide a generalised custom stream collector:

```java
/**
 * Helper - Creates a collector that constructs a contiguous array of JNA structures.
 * @param <T> Data type
 * @param <R> Resultant structure type
 * @param identity      Identity constructor
 * @param populate      Population function
 * @param chars         Collector characteristics
 * @return Structure collector
 * @see #array(Collection, Supplier, BiConsumer)
 */
public static <T, R extends Structure> Collector<T, ?, R[]> collector(Supplier<R> identity, BiConsumer<T, R> populate, Characteristics... chars) {
    final BinaryOperator<List<T>> combiner = (left, right) -> {
        left.addAll(right);
        return left;
    };
    final Function<List<T>, R[]> finisher = list -> array(list, identity, populate);
    return Collector.of(ArrayList::new, List::add, combiner, finisher, chars);
}
```

These helpers centralise the logic of allocating and populating a structure array using internal iteration.

---

## Summary

In this chapter we:

- Created a GLFW window and Vulkan surface

- Enumerated the physical devices available on the local hardware and selected one appropriate for the demo application.

- Created the logical device and work queues used in subsequent chapters

- Added supporting functionality for _two stage invocation_ and population of structure arrays.
