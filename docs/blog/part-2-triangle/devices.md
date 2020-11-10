---
title: Vulkan Devices
---

# Overview

Now we have a Vulkan instance we can use it to enumerate the _physical devices_ available on the local hardware and select one that satisfies the requirements of the application.

This will partially be dependant on having a Vulkan rendering surface so we will also implement GLFW to create a window and surface.

Finally we will create a _logical device_ from the selected physical device and specify the _work queues_ we will require for future chapters.

---

# Let's get Physical

## The Physical Device

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
PhysicalDevice(Pointer handle, Instance instance, VkQueueFamilyProperties[] families) {
    this.handle = notNull(handle);
    this.instance = notNull(instance);
    this.families = IntStream.range(0, families.length).mapToObj(n -> family(n, families[n])).collect(toList());
}

private Queue.Family family(int index, VkQueueFamilyProperties props) {
    final var flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
    return new Queue.Family(this, index, props.queueCount, flags);
}
```

## Enumerating the Physical Devices

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

which delegates to a helper that retrieves the queue families for a device and creates the domain object:

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

Note that again we invoke the same API method twice to retrieve the queue families.  However in this case we use `toArray()` on an instance of a `VkQueueFamilyProperties` structure to allocate the array and pass the _first_ element to the API method (i.e. our array is equivalent to a native pointer-to-structure).  This is the standard approach for an array of JNA structures, we will abstract this common pattern in the next chapter.

We can now add some temporary code to the demo to output the physical devices:

```java
PhysicalDevice
    .devices(instance)
    .map(PhysicalDevice::properties)
    .map(props -> props.deviceName)
    .map(String::new)
    .forEach(System.out::println);
```

On a normal PC there will generally just be the one device (the GPU).

## Selecting a Device

The final step for dealing with the physical device(s) is to select one that supports the requirements of an application.

There are several properties that we can select on:
1. The properties of a queue family provided by the device.
2. Whether a queue family supports rendering to a Vulkan surface.
3. The supported features of the device.

### Queue Families

The properties of a queue family are defined by the `VkQueueFlag` enumeration and specify whether the queue supports graphics rendering, data transfer, etc.

We add the following helper factory to the queue family class to create a predicate for a set of flags:

```java
/**
 * Helper - Creates a queue family predicate for the given flags.
 * @param flags Queue flags
 * @return Queue flags predicate
 */
public static Predicate<Family> predicate(VkQueueFlag... flags) {
    return family -> family.flags().containsAll(Arrays.asList(flags));
}
```

### Presentation Support

Later we will create a Vulkan surface for our application window which can then be tested against a queue family:

```java
/**
 * @param surface Rendering surface
 * @return Whether this family supports presentation to the given surface
 */
public boolean isPresentationSupported(Pointer surface) {
    final VulkanLibrary lib = dev.instance().library();
    final IntByReference supported = lib.factory().integer();
    check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(dev.handle(), index, surface, supported));
    return VulkanBoolean.of(supported.getValue()) == VulkanBoolean.TRUE;
}
```

We add another convenience helper for presentation testing:

```java
/**
 * Helper - Create a predicate for a queue family that supports presentation.
 * @param surface Vulkan surface
 * @return Presentation predicate
 * @see #isPresentationSupported(org.sarge.jove.common.NativeObject.Handle)
 */
public static Predicate<Family> predicate(Pointer surface) {
    return family -> family.isPresentationSupported(surface);
}
```

### Supported Device Features

Finally we could select based on the _device features_ required by our application:

```java
public VkPhysicalDeviceProperties properties() {
    final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
    instance.library().vkGetPhysicalDeviceProperties(handle, props);
    return props;
}

public VkPhysicalDeviceFeatures features() {
    final VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
    instance.library().vkGetPhysicalDeviceFeatures(handle, features);
    return features;
}
```

We will not be using any of these properties or features for some time so we ignore this option until it is needed.

---

# The Rendering Surface

Before we can progress with selection of the physical device we need to create a window and a Vulkan rendering surface.

## Application Window

The obvious starting point is a window class that encapsulates a GLFW window handle:

```java
public class Window {
    private final Handle handle;
    private final DesktopLibrary lib;
    private final Descriptor descriptor;

    /**
     * Constructor.
     * @param window            Window handle
     * @param lib                GLFW API
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
    final Pointer window = lib.glfwCreateWindow(size.width(), size.height(), descriptor.title(), null, null);    // TODO - monitors
    if(window == null) {
        throw new RuntimeException(...);
    }

    // Create window wrapper
    return new Window(window, lib, descriptor);
}
```

## Vulkan Surface

To create a Vulkan surface for a given window we add the following to the new class:

```java
/**
 * Creates a Vulkan rendering surface for this window.
 * @param vulkan Vulkan instance handle
 * @return Vulkan surface
 */
public Pointer surface(Pointer vulkan) {
    final PointerByReference ref = new PointerByReference();
    final int result = lib.glfwCreateWindowSurface(vulkan, handle, null, ref);
    if(result != 0) {
        throw new RuntimeException("Cannot create Vulkan surface: result=" + result);
    }
    return ref.getValue();
}
```

We can now create a window and surface in the demo:

```java
// Create instance
final Instance instance = ...

// Create window
final var descriptor = new Window.Descriptor.Builder()
    .title("demo")
    .size(new Dimensions(1280, 760))
    .property(Window.Property.DISABLE_OPENGL)
    .build();
final Window window = Window.create(descriptor);

// Create rendering surface
final Pointer surface = window.surface(instance);
```

The `DISABLE_OPENGL` property specifies that the new window should **not** create an OpenGL context (which GLFW does by default) which will otherwise cause our Vulkan implementation to fail.

## Presentation Support Redux

With a handle to the surface we can now return to selecting the physical device.

First we define predicates for our device requirements:

```java
// Create queue family predicates
final var graphicsPredicate = Queue.Family.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
final var presentationPredicate = Queue.Family.predicate(surface);
```

We can then walk the available devices and find one that matches these requirements:

```java
final PhysicalDevice gpu = PhysicalDevice
    .devices(instance)
    .filter(PhysicalDevice.predicate(graphicsPredicate))
    .filter(PhysicalDevice.predicate(presentationPredicate))
    .findAny()
    .orElseThrow(() -> new RuntimeException("No GPU available"));
```

Finally we add the following convenience helper to the physical device to select a queue family:

```java
public Queue.Family family(Predicate<Queue.Family> test) {
    return families.stream().filter(test).findAny().orElseThrow();
}
```

And extract the relevant families from the selected device:

```java
final Queue.Family transferFamily = gpu.family(transferPredicate);
final Queue.Family presentationFamily = gpu.family(presentationPredicate);
```

Note that these could actually be the same object depending on how the GPU implements its queues.

---

# That is Logical Captain

The _logical device_ is an instance of the physical device we have selected from those available on the hardware.

We will create the logical device domain class and complete the queue class we defined at the very start of this chapter.

## The Logical Device

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
    private LogicalDevice(Pointer handle, PhysicalDevice parent, List<RequiredQueue> queues) {
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
private record RequiredQueue(Queue.Family family, float[] priorities) {
    /**
     * Populates a descriptor for a queue required by this device.
     */
    private void populate(VkDeviceQueueCreateInfo info) {
    }
}
```

Notes:

- The _family_ is a queue family selected from the parent physical device.

- The _priorities_ is an array of percentile values that specifies the priority of each queue (expressed as a 0..1 floating-point).

- The constructor (not shown) validates the queue specification.

The logical device is highly configurable so we create a builder:

```java
public static class Builder {
    private PhysicalDevice parent;
    private VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
    private final Set<String> extensions = new HashSet<>();
    private final Set<String> layers = new HashSet<>();
    private final List<RequiredQueue> queues = new ArrayList<>();

    ...

    public LogicalDevice build() {
        // Create descriptor
        final VkDeviceCreateInfo info = new VkDeviceCreateInfo();
        info.pEnabledFeatures = features;

        // Add required extensions
        info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
        info.enabledExtensionCount = extensions.size();

        // Add validation layers
        info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
        info.enabledLayerCount = layers.size();

        // Add queue descriptors
        // TODO

        // Allocate device
        final VulkanLibrary lib = parent.instance().library();
        final PointerByReference logical = lib.factory().pointer();
        check(lib.vkCreateDevice(parent.handle(), info, null, logical));

        // Create logical device
        return new LogicalDevice(logical.getValue(), parent, queues);
    }
}
```

Note that we can also specify extensions and validation layers at the device level as well as for the instance.
This distinction is ignored by more up-to-date Vulkan implementation but we retain both for backwards compatibility.

The builder provides several over-loaded methods to specify one or more required work queues:

```java
public Builder queue(Queue.Family family) {
    return queues(family, 1);
}

public Builder queues(Queue.Family family, int num) {
    final float[] priorities = new float[num];
    Arrays.fill(priorities, 1);
    return queues(family, priorities);
}

public Builder queues(Queue.Family family, float[] priorities) {
    if(!parent.families().contains(family)) throw new IllegalArgumentException(...);
    queues.add(new RequiredQueue(family, priorities));
    return this;
}
```

The resultant list of required queues has two purposes:

1. Specify the required queues for the device when we invoke the API.

2. Construct the queue instances for the logical device

We populate the array of required queues in the descriptor for the logical device:

```java
// Add queue descriptors
info.queueCreateInfoCount = queues.size();
info.pQueueCreateInfos = VulkanStructure.array(VkDeviceQueueCreateInfo::new, queues, RequiredQueue::populate)[0];
```

Notes:

- The `VulkanStructure::array` construct is detailed in the next chapter.

- JNA requires a contiguous memory block for the array (which is actually a native pointer-to-array type).

The `populate()` method generates the descriptor for each queue:

```java
private void populate(VkDeviceQueueCreateInfo info) {
    // Allocate contiguous memory block for the priorities array
    final Memory mem = new Memory(priorities.length * Float.BYTES);
    mem.write(0, priorities, 0, priorities.length);
    
    // Populate queue descriptor
    info.queueCount = priorities.length;
    info.queueFamilyIndex = family.index();
    info.pQueuePriorities = mem;
}
```

## Work Queues

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

## Unit-Testing

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

## Integration

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

# Summary

In this chapter we:

- Created a GLFW window and Vulkan surface

- Enumerated the physical devices available on the local hardware and selected one appropriate to our application

- Created a logical device and the work queues we will need in subsequent chapters

