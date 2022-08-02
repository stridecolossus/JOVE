---
title: Presentation
---

---

## Contents

- [Overview](#overview)
- [Framework](#framework-enhancements)
- [Swapchain](#swapchain)
- [Improvements](#improvements)

---

## Overview

In the next two chapters we will implement the various components required for _presentation_ to the Vulkan surface:

* The _swapchain_ is the controller that manages the process of presenting a _frame buffer_ to the display.

* Each _frame buffer_ is comprised of a number of _attachments_ which are the target images for rendering, i.e. colour attachments, depth buffers, etc.

* Attachments are instances of an _image view_ which provides functionality for managing Vulkan images.

* A _render pass_ specifies how attachments are managed during the rendering process.

Generally an application will employ a double or triple-buffer strategy where a completed frame is presented while the next is being rendered, the buffers are swapped, and the process repeats for the next frame.  In addition Vulkan is designed to allow these activities to be executed in parallel if required.

First several new framework components are introduced that are used in the new presentation functionality.

---

## Framework Enhancements

### Native Handles

Most of the Vulkan domain objects developed thus far have a _handle_ which is the JNA native pointer for that object.

There are a couple of issues with this approach:

* A JNA pointer is mutable which potentially breaks the class if the underlying pointer is exposed.

* Vulkan API methods are defined in terms of pointers which is not type-safe.

* Additional code is required to extract the handle from the domain object.

Therefore we introduce several new framework classes to resolve (or at least mitigate) these problems.

The `Handle` class is an immutable, opaque wrapper for a JNA pointer:

```java
public final class Handle {
    private final Pointer ptr;

    public Handle(Pointer ptr) {
        this.ptr = new Pointer(Pointer.nativeValue(ptr));
    }

    @Override
    public int hashCode() {
        return ptr.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return (obj == this) || ((obj instanceof Handle that) && this.ptr.equals(that.ptr));
    }

    @Override
    public String toString() {
        return ptr.toString();
    }
}
```

Domain objects that contain a handle are refactored to implement the following new abstraction:

```java
public interface NativeObject {
    Handle handle();
}
```

And a new JNA type converter is implemented to transform a native object to the underlying JNA pointer:

```java
TypeConverter CONVERTER = new TypeConverter() {
    @Override
    public Class<?> nativeType() {
        return Pointer.class;
    }

    @Override
    public Object toNative(Object value, ToNativeContext context) {
        if(value instanceof NativeObject obj) {
            return obj.handle().toPointer();
        }
        else {
            return null;
        }
    }

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        throw new UnsupportedOperationException();
    }
};
```

Registering this converter allows all domain objects to be used directly in API methods which is more type-safe, better documented, and requires less code.

Notes:

* The `fromNative` method is disallowed since domain objects will never be instantiated via a native method.

* A further type converter is registered for the `Handle` class which is more convenient for Vulkan structure fields and some API methods.

* The `toArray` helper (not shown) on the new interface converts a collection of objects to a native pointer-to-array type as a contiguous memory block.

### Transient Objects

The new abstraction is extended for domain objects that are managed by the application:

```java
public interface TransientNativeObject extends NativeObject {
    /**
     * Destroys this object.
     * @throws IllegalStateException if this object has already been destroyed
     */
    void destroy();

    /**
     * @return Whether this object has been destroyed
     */
    boolean isDestroyed();
}
```

The following template implementation encapsulates a handle and manages the process of releasing resources on destruction:

```java
public abstract class AbstractTransientNativeObject implements TransientNativeObject {
    protected final Handle handle;
    private boolean destroyed;

    protected AbstractTransientNativeObject(Handle handle) {
        this.handle = notNull(handle);
    }

    @Override
    public Handle handle() {
        return handle;
    }

    @Override
    public final boolean isDestroyed() {
        return destroyed;
    }

    @Override
    public synchronized void destroy() {
        if(destroyed) throw new IllegalStateException(...);
        release();
        destroyed = true;
    }

    /**
     * Releases this object.
     */
    protected abstract void release();
}
```

### Abstract Vulkan Object

We also note that the majority of the Vulkan domain objects are derived from the logical device and share the following properties:

* A handle.

* A reference to the logical device.

* A _destructor_ method, e.g. `vkDestroyRenderPass`

This seems a valid case for a further intermediate base-class that abstracts this common pattern:

```java
public abstract class AbstractVulkanObject extends AbstractTransientNativeObject {
    private final LogicalDevice dev;

    protected AbstractVulkanObject(Pointer handle, LogicalDevice dev) {
        super(new Handle(handle));
        this.dev = notNull(dev);
    }
}
```

To destroy this object the following abstraction for a _destructor_ is introduced:

```java
@FunctionalInterface
public interface Destructor<T extends AbstractVulkanObject> {
    /**
     * Destroys this object.
     * @param dev           Logical device
     * @param obj           Native object to destroy
     * @param allocator     Vulkan memory allocator (always {@code null})
     */
    void destroy(DeviceContext dev, T obj, Pointer allocator);
}
```

The new class provides the API method used to destroy the object:

```java
protected abstract Destructor<?> destructor(VulkanLibrary lib);
```

Which is used in the overridden `destroy` method:

```java
@Override
public synchronized void destroy() {
    // Destroy this object
    Destructor destructor = destructor(dev.library());
    destructor.destroy(dev, this, null);

    // Delegate
    super.destroy();
}
```

---

## Swapchain

### Rendering Surface

The swapchain is dependant on the physical capabilities of the graphics hardware such as supported image formats, presentation modes, etc.

We start with a new domain class that wraps the rendering surface previously retrieved from GLFW:

```java
public class Surface extends AbstractTransientNativeObject {
    private final PhysicalDevice dev;

    public Surface(Handle surface, PhysicalDevice dev) {
        super(surface);
        this.dev = notNull(dev);
    }

    @Override
    protected void release() {
        Instance instance = dev.instance();
        VulkanLibrary lib = instance.library();
        lib.vkDestroySurfaceKHR(instance, this, null);
    }
}
```

The surface class provides a number of accessors that are used to configure the swapchain.  The _surface capabilities_ specify minimum and maximum constraints on various aspects of the hardware, such as the number of frame buffers, the maximum dimensions of the image views, etc:

```java
public VkSurfaceCapabilitiesKHR capabilities() {
    VulkanLibrary lib = dev.library();
    VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
    check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, this, caps));
    return caps;
}
```

The supported _image formats_ are retrieved using the two-stage approach:

```java
public Collection<VkSurfaceFormatKHR> formats() {
    VulkanLibrary lib = instance.library();
    VulkanFunction<VkSurfaceFormatKHR> func = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, this, count, array);
    IntByReference count = instance.factory().integer();
    var formats = VulkanFunction.enumerate(func, count, VkSurfaceFormatKHR::new);
    return Arrays.asList(formats);
}
```

Finally the swapchain supports a number of available _presentation modes_ (at least one) which can be configured by the application:

```java
public Set<VkPresentModeKHR> modes() {
    VulkanLibrary lib = instance.library();
    VulkanFunction<int[]> func = (count, array) -> lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, this, count, array);
    IntByReference count = instance.factory().integer();
    int[] array = VulkanFunction.invoke(func, count, int[]::new);
    ...
}
```

The presentation modes are returned as an integer array which is mapped to the corresponding enumeration:

```java
var mapping = IntegerEnumeration.mapping(VkPresentModeKHR.class);
return Arrays
    .stream(array)
    .mapToObj(mapping::map)
    .collect(toSet());
```

A new JNA library is created for the various surface related API methods:

```java
public interface Library {
    int  vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, Surface surface, VkSurfaceCapabilitiesKHR caps);
    int  vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, Surface surface, IntByReference count, VkSurfaceFormatKHR formats);
    int  vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, Surface surface, IntByReference count, int[] modes);
    void vkDestroySurfaceKHR(Instance instance, Surface surface, Pointer allocator);
}
```

### Images

Vulkan creates the images for the colour attachments when the swapchain is instantiated:

```java
public class Image implements NativeObject {
    private final Handle handle;
    private final LogicalDevice dev;
    private final ImageDescriptor descriptor;
}
```

The _image descriptor_ comprises the static properties of an image:

```java
public record ImageDescriptor(VkImageType type, VkFormat format, Extents extents, Set<VkImageAspect> aspects, int levels, int layers) {
}
```

Where _extents_ specifies the dimensions of the image:

```java
public record Extents(Dimensions dimensions, int depth) {
}
```

And `Dimensions` is a simple record for the size of an arbitrary 2D rectangle:

```java
public record Dimensions(int width, int height) {
}
```

A convenience builder is also implemented to construct an image descriptor.

### Image Views

An _image view_ is the entry-point for operations on an image such as layout transforms, sampling, etc:

```java
public class View extends AbstractVulkanObject {
    private final Image image;
    
    View(Pointer handle, LogicalDevice dev, Image image) {
        super(handle, dev);
        this.image = notNull(image);
    }

    @Override
    protected Destructor<View> destructor(VulkanLibrary lib) {
        return lib::vkDestroyImageView;
    }
}
```

Notes:

* The swapchain images are created and managed by Vulkan and are not explicitly destroyed by the application.

* However the application is responsible for creating (and destroying) the image views.

* The view class initially has no functionality but will be expanded as we progress, in particular when addressing clearing attachments.

An image view is constructed by a builder as usual:

```java
public static class Builder {
    private final Image image;
    private VkImageViewType type;
    private VkComponentMapping mapping = DEFAULT_COMPONENT_MAPPING;
}
```

The `build` method populates a Vulkan descriptor for the view and invokes the API method:

```java
public View build() {
    // Build view descriptor
    var info = new VkImageViewCreateInfo();
    info.viewType = type;
    info.format = image.descriptor().format();
    info.image = image.handle();
    info.components = mapping;
    info.subresourceRange = ...

    // Allocate image view
    LogicalDevice dev = image.device();
    VulkanLibrary lib = dev.library();
    PointerByReference handle = dev.factory().pointer();
    check(lib.vkCreateImageView(dev, info, null, handle));

    // Create image view
    return new View(handle.getValue(), dev, image);
}
```

The `subresourceRange` field of the create descriptor specifies a subset of the mipmap levels and array layers accessible to the view.  For this demo we will use the whole image so the descriptor is hard-coded for the moment:

```java
var range = new VkImageSubresourceRange();
range.aspectMask = IntegerEnumeration.reduce(image.descriptor().aspects());
range.baseMipLevel = 0;
range.levelCount = 1;
range.baseArrayLayer = 0;
range.layerCount = 1;
info.subresourceRange = range;
```

The component mapping specifies the swizzle for the RGBA colour components of the view:

```java
private static final VkComponentMapping DEFAULT_COMPONENT_MAPPING = identity();

private static VkComponentMapping identity() {
    var swizzle = VkComponentSwizzle.IDENTITY;
    var identity = new VkComponentMapping();
    identity.r = swizzle;
    identity.g = swizzle;
    identity.b = swizzle;
    identity.a = swizzle;
    return identity;
}
```

Finally a new JNA library is added for image views:

```java
interface Library {
    int  vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);
    void vkDestroyImageView(LogicalDevice device, View imageView, Pointer pAllocator);
}
```

### Swapchain

With the above in place the swapchain itself can now be implemented:

```java
public class Swapchain extends AbstractVulkanObject {
    private final VkFormat format;
    private final Dimensions extents;
    private final List<View> attachments;

    @Override
    protected Destructor<Swapchain> destructor(VulkanLibrary lib) {
        return lib::vkDestroySwapchainKHR;
    }

    @Override
    protected void release() {
        views.forEach(View::destroy);
    }
}
```

The swapchain is another highly configurable domain object created via a builder:

```java
public static class Builder {
    private final LogicalDevice dev;
    private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
    private final Surface surface;
    private final VkSurfaceCapabilitiesKHR caps;
}
```

The surface capabilities are queried from the surface in the constructor:

```java
public Builder(LogicalDevice dev, Surface surface) {
    this.dev = notNull(dev);
    this.surface = notNull(surface);
    this.caps = surface.capabilities();
    init();
}
```

The constructor also initialises the swapchain properties to sensible defaults via the setters based on the capabilities of the surface:

```java
private void init() {
    extent(caps.currentExtent.width, caps.currentExtent.height);
    count(caps.minImageCount);
    transform(caps.currentTransform);
    format(DEFAULT_FORMAT);
    space(DEFAULT_COLOUR_SPACE);
    arrays(1);
    mode(VkSharingMode.EXCLUSIVE);
    usage(VkImageUsage.COLOR_ATTACHMENT);
    alpha(VkCompositeAlphaFlagKHR.OPAQUE);
    mode(DEFAULT_PRESENTATION_MODE);
    clipped(true);
}
```

Constructing the swapchain comprises three steps:

1. Create the swapchain.

2. Retrieve the image handles.

3. Create a view for each image.

Instantiating the swapchain is relatively trivial:

```java
public Swapchain build() {
    VulkanLibrary lib = dev.library();
    ReferenceFactory factory = dev.factory();
    PointerByReference chain = factory.pointer();
    check(lib.vkCreateSwapchainKHR(dev, info, null, chain));
    ...
}
```

Next the handles to the swapchain images are retrieved:

```java
VulkanFunction<Pointer[]> func = (count, array) -> lib.vkGetSwapchainImagesKHR(dev, chain.getValue(), count, array);
IntByReference count = factory.integer();
Pointer[] handles = VulkanFunction.invoke(func, count, Pointer[]::new);
```

The images all share the same descriptor:

```java
Dimensions extents = new Dimensions(info.imageExtent.width, info.imageExtent.height);
ImageDescriptor descriptor = new ImageDescriptor.Builder()
    .format(info.imageFormat)
    .extents(extents)
    .aspect(VkImageAspect.COLOR)
    .build();
```

Which is used when creating the view for each image:

```java
var views = Arrays
    .stream(handles)
    .map(Handle::new)
    .map(image -> new Image(image, dev, descriptor))
    .map(View::of)
    .collect(toList());
```

Where the convenience `of` factory method constructs a default view for a given image:

```java
public class View {
    public static View of(Image image) {
        return new Builder(image).build();
    }
}    
```

Finally the swapchain domain object is instantiated:

```java
return new Swapchain(chain.getValue(), dev, info.imageFormat, extents, views);
```

A new JNA library is added for the swapchain:

```java
interface Library {
    int  vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);
    void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Pointer pAllocator);
    int  vkGetSwapchainImagesKHR(LogicalDevice device, Swapchain swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);
    int  vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, Semaphore semaphore, Fence fence, IntByReference pImageIndex);
    int  vkQueuePresentKHR(Queue queue, VkPresentInfoKHR pPresentInfo);
}
```

### Presentation

Presentation is comprised of a pair of new methods on the swapchain.

The index of the next swapchain image to be rendered is retrieved as follows:

```java
public int acquire(Semaphore semaphore, Fence fence) throws SwapchainInvalidated {
    DeviceContext dev = super.device();
    VulkanLibrary lib = dev.library();
    IntByReference index = dev.factory().integer();
    int result = lib.vkAcquireNextImageKHR(dev, this, Long.MAX_VALUE, semaphore, fence, index);
    ...
}
```

The _semaphore_ and _fence_ are synchronisation primitives that are covered in a later chapter, for the moment these values are `null` in the demo.

Acquiring the swapchain image is one of the few API methods that can return _multiple_ success codes (see [vkAcquireNextImageKHR](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/vkAcquireNextImageKHR.html)).  Therefore the following code replaces the usual `check` on the API result code:

```java
if((result == VulkanLibrary.SUCCESS) || (result == SUB_OPTIMAL)) {
    return index.getValue();
}
else
if(result == OUT_OF_DATE) {
    throw new SwapchainInvalidated(result);
}
else {
    throw new VulkanException(result);
}
```

Where the new result codes are constants looked up from the Vulkan enumeration:

```java
private static final int OUT_OF_DATE = VkResult.ERROR_OUT_OF_DATE_KHR.value();
private static final int SUB_OPTIMAL = VkResult.SUBOPTIMAL_KHR.value();
```

The `SwapchainInvalidated` exception indicates that the swapchain has become invalid, e.g. the surface has been resized or minimised.  Note that a _suboptimal_ return value is considered as valid in this case.

When an image has been rendered it can then be presented to the surface, which requires population of a Vulkan descriptor for the presentation task:

```java
public void present(Queue queue, int index, Semaphore semaphore) {
    // Create presentation descriptor
    var info = new VkPresentInfoKHR();

    // Populate swap-chain
    info.swapchainCount = 1;
    info.pSwapchains = NativeObject.toArray(List.of(this));
    
    ...
}
```

The image(s) to be presented are stored as a contiguous memory block which maps to a native pointer-to-integer-array:

```java
int[] array = new int[]{index.getValue()};
Memory mem = new Memory(array.length * Integer.BYTES);
mem.write(0, array, 0, array.length);
info.pImageIndices = mem;
```

Finally the presentation task is added to the relevant work queue:

```java
VulkanLibrary lib = dev.library();
int result = lib.vkQueuePresentKHR(queue, info);
if((result == OUT_OF_DATE) || (result == SUB_OPTIMAL)) {
    throw new SwapchainInvalidated(result);
}
else
if(result != VulkanLibrary.SUCCESS) {
    throw new VulkanException(result);
}
```

Notes:

* The API supports presentation of multiple swapchains in one operation but for the moment the code is restricted to a single instance.

* Similarly the `pImageIndices` array consists of a single image index.

* The presentation task descriptor is created on every invocation of the `present` method which will probably be cached later.

* In this case a sub-optimal swapchain is __not__ considered valid.

---

## Improvements

### Format Builder

When developing the swapchain we realised that finding an image `VkFormat` can be difficult given the size of the enumeration.
However the naming convention is highly consistent and it is therefore possible to specify the format programatically.

The following helper class allows an application to specify the various components of a required format:

```java
public class FormatBuilder {
    public static final String RGBA = "RGBA";

    private String components = RGBA;
    private int count = 4;
    private int bytes = 4;
    private Type type = Type.FLOAT;
    private boolean signed = true;
}
```

The `Type` field is an enumeration of the Vulkan component types:

```java
public enum Type {
    INT,
    FLOAT,
    NORM,
    SCALED,
    RGB
}
```

The builder constructs the format name and looks up the resultant enumeration constant:

```java
public VkFormat build() {
    // Build component layout
    StringBuilder layout = new StringBuilder();
    int size = bytes * Byte.SIZE;
    for(int n = 0; n < count; ++n) {
        layout.append(template.charAt(n));
        layout.append(size);
    }

    // Build format string
    char ch = signed ? 'S' : 'U';
    String format = String.format("%s_%c%s", layout, ch, type.name());

    // Lookup format
    return VkFormat.valueOf(format);
}
```

The demo can now be refactored to build the image format rather than having to find it manually in the enumeration:

```java
VkFormat format = new FormatBuilder()
    .template("BGRA")
    .bytes(1)
    .signed(true)
    .type(FormatBuilder.Type.RGB)
    .build();
```

Which maps to the `B8G8R8A8_SRGB` format used by the colour attachment.

### Vulkan Booleans

Development of the swapchain was the first time that a boolean value (whether the swapchain images are clipped) was used in a JNA based library.  We encountered a curious problem that stumped us for some time.  See [this](https://stackoverflow.com/questions/55225896/jna-maps-java-boolean-to-1-integer) stack-overflow question.

In summary: a Vulkan boolean is represented as zero (for false) or one (for true) - so far so logical.
But by default JNA maps a Java boolean to zero for false but __minus one__ for true!  WTF!

There are a lot of boolean values used across Vulkan so we needed some global solution to over-ride the default JNA mapping.

The [VulkanBoolean](https://github.com/stridecolossus/JOVE/blob/master/src/main/java/org/sarge/jove/common/VulkanBoolean.java) class was implemented to marshal a Java boolean to/from a native integer represented as zero or one:

```java
public final class VulkanBoolean {
    public static final VulkanBoolean TRUE = new VulkanBoolean(1);
    public static final VulkanBoolean FALSE = new VulkanBoolean(0);

    private final int value;

    private VulkanBoolean(int value) {
        this.value = value;
    }

    public boolean toBoolean() {
        return this == TRUE;
    }

    @Override
    public int hashCode() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
```

Convenience converters are provided to transform native values and Java booleans to the new type:

```java
public static VulkanBoolean of(int value) {
    return value == TRUE.value ? TRUE : FALSE;
}

public static VulkanBoolean of(boolean bool) {
    return bool ? TRUE : FALSE;
}
```

Again a JNA type converter is used to map the new type to/from its native representation in API methods and structures:

```java
public static final TypeConverter CONVERTER = new TypeConverter() {
    @Override
    public Class<?> nativeType() {
        return Integer.class;
    }

    @Override
    public Object toNative(Object value, ToNativeContext context) {
        if(value == null) {
            return FALSE.value;
        }
        else {
            VulkanBoolean bool = (VulkanBoolean) value;
            return bool.value;
        }
    }

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        if(nativeValue == null) {
            return FALSE;
        }
        else {
            return of((int) nativeValue);
        }
    }
};
```

This solves the mapping problem in API methods and JNA structures that contain booleans and also has the side-benefit of being more type-safe and self-documenting.

> As it turns out the JNA `W32APITypeMapper` helper class probably already solves this issue but by this point we had already code-generated the structures.

---

## Summary

In this chapter we:

* Implemented the swapchain and image/views to support presentation to the rendering surface.

* Introduced a number of framework improvements to abstract common patterns.

