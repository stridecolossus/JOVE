---
title: Presentation
---

## Overview

In the next two chapters we will implement the various components required for _presentation_ to the Vulkan surface:

* The _swapchain_ is a controller that manages the process of presenting a _frame buffer_ to the display.

* Each _frame buffer_ is comprised of a number of _attachments_ which are the target images for rendering, i.e. colour attachments, depth buffers, etc.

* Attachments are instances of an _image view_ which provides functionality for managing Vulkan images.

* A _render pass_ specifies how attachments are managed during the rendering process.

Generally an application will employ a double or triple-buffer strategy where a completed frame is presented while the next is being rendered, the buffers are swapped, and the process repeats for the next frame.  In addition Vulkan is designed to allow these activities to be executed in parallel if required.

During this chapter we will also introduce several new framework components and supporting functionality (covered in detail at the end of the chapter).

---

## Swapchain

### Rendering Surface

The swapchain is dependant on the physical capabilities of the graphics hardware such as supported image formats, presentation modes, etc.

We start with a new domain class that wraps the rendering surface previously retrieved from GLFW:

```java
public class Surface extends AbstractTransientNativeObject {
    private final Instance instance;

    public Surface(Handle handle, Instance instance) {
        super(handle);
        this.instance = notNull(instance);
    }

    @Override
    protected void release() {
        VulkanLibrarySurface lib = instance.library();
        lib.vkDestroySurfaceKHR(instance, this, null);
    }
}
```

Notes:

* A `Handle` is an opaque and immutable wrapper for a JNA pointer.

* The `AbstractTransientNativeObject` is the template base-class for Vulkan domain objects managed by the application.

* Both of these new framework components are detailed towards the end of the chapter.

The new surface class provides a factory for the surface properties for the selected physical device:

```java
public Properties properties(PhysicalDevice dev) {
    return new Properties(dev);
}
```

The surface properties provides a number of accessors that are used to configure the swapchain.  The _surface capabilities_ specify minimum and maximum constraints on various aspects of the hardware, such as the number of frame buffers, the maximum dimensions of the image views, etc:

```java
public VkSurfaceCapabilitiesKHR capabilities() {
    VulkanLibrary lib = dev.library();
    VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
    check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, Surface.this, caps));
    return caps;
}
```

The supported _image formats_ are retrieved using the two-stage approach:

```java
public Collection<VkSurfaceFormatKHR> formats() {
    VulkanLibrary lib = instance.library();
    VulkanFunction<VkSurfaceFormatKHR> func = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, surface.this, count, array);
    IntByReference count = instance.factory().integer();
    var formats = VulkanFunction.enumerate(func, count, VkSurfaceFormatKHR::new);
    return Arrays.stream(formats).collect(toList());
}
```

Finally the swapchain will support a number of available _presentation modes_ (at least one) which can be configured by the application:

```java
public Set<VkPresentModeKHR> loadModes() {
    VulkanLibrary lib = instance.library();
    VulkanFunction<int[]> func = (count, array) -> lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, Surface.this, count, array);
    IntByReference count = instance.factory().integer();
    int[] array = VulkanFunction.invoke(func, count, int[]::new);
    ...
}
```

The presentation modes are returned as an integer array which is mapped to the corresponding enumeration:

```java
IntegerEnumeration.ReverseMapping<VkPresentModeKHR> mapping = IntegerEnumeration.mapping(VkPresentModeKHR.class);
return Arrays
    .stream(array)
    .mapToObj(mapping::map)
    .collect(toSet());
```

The new API methods are added to the surface library:

```java
public interface VulkanLibrarySurface {
    ...
    int  vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, Surface surface, VkSurfaceCapabilitiesKHR caps);
    int  vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, Surface surface, IntByReference count, VkSurfaceFormatKHR formats);
    int  vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, Surface surface, IntByReference count, int[] modes);
    void vkDestroySurfaceKHR(Instance instance, Surface surface, Pointer allocator);
}
```

### Images

Vulkan creates the images for the colour attachments for us when the swapchain is instantiated.

We first create a new domain object for a Vulkan image:

```java
public class Image implements NativeObject {
    private final Handle handle;
    private final LogicalDevice dev;
    private final ImageDescriptor descriptor;
}
```

The _image descriptor_ comprises the static properties of the image:

```java
public record ImageDescriptor(VkImageType type, VkFormat format, ImageExtents extents, Set<VkImageAspect> aspects, int levels, int layers) {
}
```

Where _extents_ specifies the dimensions of the image:

```java
public record ImageExtents(Dimensions dimensions, int depth) {
}
```

And `Dimensions` is a simple record for the size of an arbitrary 2D rectangle:

```java
public record Dimensions(int width, int height) {
}
```

We also add a builder to construct an image descriptor.

### Image Views

An _image view_ is a reference to an image and is the entry-point for operations such as layout transforms, sampling, etc:

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

* The `destructor` provides the API method to release a managed native object (which is used in the `destroy` method of the base-class).

* The view class initially has no functionality but will be expanded as we progress (in particular when addressing clearing attachments).

To construct a view for a given image we implement a builder:

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
    VkImageViewCreateInfo info = new VkImageViewCreateInfo();
    info.viewType = type;
    info.format = image.descriptor().format();
    info.image = image.handle();
    info.components = new VkComponentMapping();
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
range.aspectMask = IntegerEnumeration.mask(image.descriptor().aspects());
range.baseMipLevel = 0;
range.levelCount = 1;
range.baseArrayLayer = 0;
range.layerCount = 1;
info.subresourceRange = range;
```

The component mapping specifies the swizzle for the RGBA colour components of the view:

```java
private static final VkComponentMapping DEFAULT_COMPONENT_MAPPING = create();

private static VkComponentMapping create() {
    VkComponentSwizzle identity = VkComponentSwizzle.IDENTITY;
    var mapping = new VkComponentMapping();
    mapping.r = identity;
    mapping.g = identity;
    mapping.b = identity;
    mapping.a = identity;
    return mapping;
}
```

Finally we add a new JNA library for images and views:

```java
interface VulkanLibraryImage {
    int  vkCreateImageView(LogicalDevice device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);
    void vkDestroyImageView(LogicalDevice device, View imageView, Pointer pAllocator);
```

### Swapchain

With the above in place we can now implement the swapchain itself:

```java
public class Swapchain extends AbstractVulkanObject {
    public static final VkColorSpaceKHR DEFAULT_COLOUR_SPACE = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;
    public static final VkPresentModeKHR DEFAULT_PRESENTATION_MODE = VkPresentModeKHR.FIFO_KHR;
    public static final VkFormat DEFAULT_FORMAT = VkFormat.B8G8R8A8_SRGB;

    private final VkFormat format;
    private final Dimensions extents;
    private final List<View> views;

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
    private final VkSurfaceCapabilitiesKHR caps;
    private final Collection<VkSurfaceFormatKHR> formats;
    private final Set<VkPresentModeKHR> modes;
}
```

The available capabilities, formats and presentation modes are queried (once) from the surface:

```java
public Builder(LogicalDevice dev, Surface surface) {
    Surface.Properties props = surface.properties(dev.parent());
    this.caps = props.capabilities();
    this.formats = props.formats();
    this.modes = props.modes();
    this.dev = notNull(dev);
    info.surface = surface.handle();
    init();
}
```

The constructor also initialises the swapchain properties to sensible defaults via the setters (based on the capabilities of the surface):

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

Constructing the swapchain is comprised of three steps:
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

Next we retrieve the handles to the swapchain images created by Vulkan:

```java
VulkanFunction<Pointer[]> func = (count, array) -> lib.vkGetSwapchainImagesKHR(dev, chain.getValue(), count, array);
IntByReference count = factory.integer();
Pointer[] handles = VulkanFunction.invoke(func, count, Pointer[]::new);
```

The images share the same descriptor:

```java
Dimensions extents = new Dimensions(
    info.imageExtent.width,
    info.imageExtent.height
);
ImageDescriptor descriptor = new ImageDescriptor.Builder()
    .format(info.imageFormat)
    .extents(new ImageExtents(extents))
    .aspect(VkImageAspect.COLOR)
    .build();
```

Which is used when we create the view for each swapchain image:

```java
var views = Arrays
    .stream(handles)
    .map(Handle::new)
    .map(image -> new Image(image, dev, descriptor))
    .map(Image::view)
    .collect(toList());
```

We add the convenience `view` factory method on the image class to build a default view for a given image:

```java
public class Image {
    public static View view() {
        return new View.Builder().build(this);
    }
}    
```

Finally we create the swapchain domain object itself:

```java
return new Swapchain(chain.getValue(), dev, info.imageFormat, extents, views);
```

And add a new API:

```java
interface VulkanLibrarySwapchain {
    int  vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);
    void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Pointer pAllocator);
    int  vkGetSwapchainImagesKHR(LogicalDevice device, Swapchain swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);
}
```

### Presentation

To support presentation we extend the new API with the following methods:

```java
interface VulkanLibrarySwapchain {
    ...
    int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, Semaphore semaphore, Fence fence, IntByReference pImageIndex);
    int vkQueuePresentKHR(Queue queue, VkPresentInfoKHR pPresentInfo);
}
```

We add the following to acquire the index of the next image to be rendered:

```java
public int acquire() {
    final DeviceContext dev = super.device();
    final VulkanLibrary lib = dev.library();
    check(lib.vkAcquireNextImageKHR(dev, this, Long.MAX_VALUE, null, null, index));
    return index.getValue();
}
```

The `index` is a new class member created from the reference factory in the swapchain constructor.

The _semaphore_ and _fence_ are synchronisation primitives that are covered in a later chapter when we fully implement the render loop.  For the moment we will leave these values as `null` in the acquire method.

When an image has been rendered it can be presented to the surface, which requires population of a Vulkan descriptor for the presentation task:

```java
public void present(Queue queue, int index) {
    // Create presentation descriptor
    VkPresentInfoKHR info = new VkPresentInfoKHR();

    // Populate swap-chain
    info.swapchainCount = 1;
    info.pSwapchains = NativeObject.toArray(List.of(this));
    
    ...
}
```

Next we specify the array of images to be presented as a contiguous memory block:

```java
int[] array = new int[]{index.getValue()};
Memory mem = new Memory(array.length * Integer.BYTES);
mem.write(0, array, 0, array.length);
info.pImageIndices = mem;
```

And finally we invoke the API method that adds the presentation task to the relevant work queue:

```java
check(lib.vkQueuePresentKHR(queue, info));
```

Notes:

* The API supports presentation of multiple swapchains in one operation but we restrict ourselves to a single instance for the moment.

* Similarly the `pImageIndices` array therefore consists of a single image index.

* The presentation task descriptor is created on every invocation of the `present` method which we may well want to cache later.

### Format Builder

When developing the above code we realised that finding an image format in the `VkFormat` enumeration can be quite tricky - there are an awful lot of them.  However the enumeration names are logical and consistent which allows us to programatically select an image format.

We introduce the following helper class that is used to specify the various component of a required format:

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

The builder constructs the format name and looks up the enumeration constant:

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

We can now refactor the demo to build the format rather than having to find it in the enumeration:

```java
VkFormat format = new FormatBuilder()
    .template("BGRA")
    .bytes(1)
    .signed(true)
    .type(FormatBuilder.Type.RGB)
    .build();
```

Which maps to the `B8G8R8A8_SRGB` format used by the colour attachment.

---

## Framework Enhancements

In this section we cover the new supporting classes introduced in this chapter.

### Native Handles

Most of the Vulkan domain objects developed thus far have a _handle_ which is the JNA native pointer for that object.

There are a couple of issues with this approach:

* A JNA pointer is mutable which essentially breaks the class if we expose the handle.

* Currently our API methods are defined in terms of pointers which is not type-safe and requires additional code to convert to/from the handle.

We will introduce another abstraction for native objects to resolve (or at least mitigate) these problems.

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

We next introduce a new base-class for domain objects that contain a handle:

```java
public interface NativeObject {
    Handle handle();
}
```

And a JNA type converter to convert a native object to its underlying JNA pointer:

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

* The `fromNative` method is disallowed since we will never be creating a domain object via a native method.

* We also register another type converter for the `Handle` class which is more convenient for Vulkan structure fields (and some API methods).

* The `toArray` helper on the new interface converts a collection of objects to a native pointer-to-array type as a contiguous memory block.

### Transient Objects

For domain objects that are managed by the application we extend the above:

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

We can now create a template implementation that encapsulates a handle and the process of releasing resources on destruction:

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

We also note that the majority of our Vulkan domain objects are derived from the logical device and share the following properties:

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

To destroy this object we introduce the following abstraction for a _destructor_ method:

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

We add the following provider for the API method used to destroy the object:

```java
/**
 * Provides the <i>destructor</i> API method for this object.
 * @param lib Vulkan API
 * @return Destructor method
 */
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

For example the swapchain can now be refactored as follows:

```java
public class Swapchain extends AbstractVulkanObject {
    Swapchain(Pointer handle, DeviceContext dev, ...) {
        super(handle, dev);
        ...
    }

    @Override
    protected Destructor<Swapchain> destructor(VulkanLibrary lib) {
        return lib::vkDestroySwapchainKHR;
    }

    @Override
    protected void release() {
        attachments.forEach(View::destroy);
    }
}
```

### Vulkan Booleans

Development of the swapchain was the first time we came across JNA code using boolean values and we encountered a curious problem that stumped us for some time.
See [this](https://stackoverflow.com/questions/55225896/jna-maps-java-boolean-to-1-integer) stack-overflow question.

In summary: a Vulkan boolean is represented as zero (for false) or one (for true) - so far so logical. 
But by default JNA maps a Java boolean to zero for false but **minus one** for true!  WTF!

There are a lot of boolean values used across Vulkan so we needed some global solution to over-ride the default JNA mapping.

We created the [VulkanBoolean](https://github.com/stridecolossus/JOVE/blob/master/src/main/java/org/sarge/jove/common/VulkanBoolean.java) class to map a Java boolean to/from a native integer represented as zero or one:

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

We also add convenience converters for a native value or a Java boolean:

```java
public static VulkanBoolean of(int value) {
    return value == TRUE.value ? TRUE : FALSE;
}

public static VulkanBoolean of(boolean bool) {
    return bool ? TRUE : FALSE;
}
```

Again we used a JNA type converter to map the new type to/from its native representation in API methods and structures:

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

