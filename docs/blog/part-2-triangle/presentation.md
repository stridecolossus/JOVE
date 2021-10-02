---
title: Presentation
---

## Overview

In this chapter we will implement the various components required for _presentation_ to the Vulkan surface:

* The _swapchain_ is a controller that manages the process of presenting a _frame buffer_ to the display.

* Each _frame buffer_ is comprised of a number of _attachments_ which are the target images for rendering, i.e. colour attachments, depth buffers, etc.

* Attachments are instances of an _image view_ which provides functionality for managing Vulkan images.

* A _render pass_ specifies how attachments are managed during the rendering process.

Generally an application will employ a double or triple-buffer strategy where a completed frame is presented while the next is being rendered (in parallel), the buffers are swapped, and the process repeats for the next frame.

During this chapter we introduce several new framework components and supporting functionality that are covered in detail at the end of the chapter.

---

## Swapchain

### Rendering Surface

The swapchain is dependant on the physical capabilities of the graphics hardware such as supported image formats, presentation modes, etc.

We start with a new domain class that wraps the rendering surface previously retrieved from GLFW:

```java
public class Surface extends AbstractTransientNativeObject {
    private final PhysicalDevice dev;

    public Surface(Handle handle, PhysicalDevice dev) {
        super(handle);
        this.dev = notNull(dev);
    }

    @Override
    protected void release() {
        final Instance instance = dev.instance();
        final VulkanLibrarySurface lib = instance.library();
        lib.vkDestroySurfaceKHR(instance.handle(), handle, null);
    }
}
```

Notes:

* A `Handle` is an opaque and immutable wrapper for a JNA pointer.

* The `AbstractTransientNativeObject` is the base-class for Vulkan domain objects managed by the application.

* Both of these new framework components are detailed towards the end of the chapter.

The surface provides a number of accessors that will be used to configure the swapchain.

The _surface capabilities_ specify minimum and maximum constraints on various aspects of the hardware, such as the number of frame buffers, the maximum dimensions of the image views, etc:

```java
public VkSurfaceCapabilitiesKHR capabilities() {
    final VulkanLibrary lib = dev.library();
    final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
    check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.handle(), handle, caps));
    return caps;
}
```

The supported _image formats_ are retrieved using the two-stage approach:

```java
public Collection<VkSurfaceFormatKHR> formats() {
    final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.handle(), handle, count, array);
    final var formats = VulkanFunction.enumerate(func, dev.library(), VkSurfaceFormatKHR::new);
    return Arrays.stream(formats).collect(toList());
}
```

Finally the swapchain will support a number of available _presentation modes_ (at least one) which can be configured by the application:

```java
public Set<VkPresentModeKHR> modes() {
    // Count number of supported modes
    // TODO - API method returns the modes as an int[] and we cannot use VulkanFunction::enumerate for a primitive array
    final VulkanLibrary lib = dev.library();
    final IntByReference count = lib.factory().integer();
    check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), this.handle, count, null));

    // Retrieve modes
    final int[] array = new int[count.getValue()];
    check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), this.handle, count, array));

    // Convert to enumeration
    return Arrays
            .stream(array)
            .mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n))
            .collect(toSet());
}
```

The new API methods are added to the surface library:

```java
public interface VulkanLibrarySurface {
    ...
    int vkGetPhysicalDeviceSurfaceCapabilitiesKHR(Handle device, Handle surface, VkSurfaceCapabilitiesKHR caps);
    int vkGetPhysicalDeviceSurfaceFormatsKHR(Handle device, Handle surface, IntByReference count, VkSurfaceFormatKHR formats);
    int vkGetPhysicalDeviceSurfacePresentModesKHR(Handle device, Handle surface, IntByReference count, int[] modes);
    void vkDestroySurfaceKHR(Handle instance, Handle surface, Handle allocator);
}
```

### Images and Views

Vulkan creates the images for the colour attachments when the swapchain is instantiated.

We first create new domain objects for a Vulkan image:

```java
public class Image implements NativeObject {
    private final Handle handle;
    private final LogicalDevice dev;
    private final Descriptor descriptor;
}
```

The _image descriptor_ comprises the static properties of the image:

```java
public record Descriptor(VkImageType type, VkFormat format, ImageExtents extents, Set<VkImageAspect> aspects, int levels, int layers) {
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

An _image view_ is a reference to an image and is the entry-point for operations such as layout transforms, sampling, etc:

```java
public class View extends AbstractVulkanObject {
    private final Image image;
    
    View(Pointer handle, LogicalDevice dev, Image image) {
        super(handle, dev);
        this.image = notNull(image);
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyImageView;
    }
}
```

Notes:

* The swapchain images are managed by Vulkan and are not explicitly destroyed by the application.

* However the application is responsible for creating (and destroying) the image views.

* The `destructor` provides the API method to release a managed native object (which is used in the `destroy` method of the base-class).

* The view class initially has no functionality but will be expanded as we progress (in particular when addressing clearing attachments).

* We also add a convenience builder to construct an image descriptor.

To construct a view for a given image we implement a builder:

```java
public static class Builder {
    private VkImageViewType type;
    private VkComponentMapping mapping = new VkComponentMapping();
    private SubResource subresource;
}
```

The build method populates a Vulkan descriptor for the view and invokes the API method:

```java
public View build(Image image) {
    // Build view descriptor
    final VkImageViewCreateInfo info = new VkImageViewCreateInfo();
    info.viewType = type;
    info.format = image.descriptor().format();
    info.image = image.handle();
    info.components = mapping;
    info.subresourceRange = subresource.toRange();

    // Allocate image view
    final LogicalDevice dev = image.device();
    final VulkanLibrary lib = dev.library();
    final PointerByReference handle = lib.factory().pointer();
    check(lib.vkCreateImageView(dev.handle(), info, null, handle));

    // Create image view
    return new View(handle.getValue(), dev, image);
}
```

The _sub-resource_ specifies the purpose of the image and which regions should be accessed:

```java
public record SubResource(Set<VkImageAspect> mask, int mipLevel, int levelCount, int baseArrayLayer, int layerCount) {
}
```

The following helper creates the Vulkan descriptor for the sub-resource which is used in the image view builder:

```java
public VkImageSubresourceRange toRange() {
    final var range = new VkImageSubresourceRange();
    range.aspectMask = IntegerEnumeration.mask(mask);
    range.baseMipLevel = mipLevel;
    range.levelCount = levelCount;
    range.baseArrayLayer = baseArrayLayer;
    range.layerCount = layerCount;
    return range;
}
```

We also implement a builder for a sub-resource (not shown) with convenience methods to initialise the data from an image or another sub-resource.

Finally we add a new JNA library for images and views:

```java
interface VulkanLibraryImage {
    int vkCreateImage(Handle device, VkImageCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pImage);
    void vkDestroyImage(Handle device, Handle image, Handle pAllocator);

    int vkCreateImageView(Handle device, VkImageViewCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pView);
    void vkDestroyImageView(Handle device, Handle imageView, Handle pAllocator);
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
    protected Destructor destructor(VulkanLibrary lib) {
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

The available capabilities, formats and presentation modes are queried from the surface:

```java
public Builder(LogicalDevice dev, Surface surface) {
    this.dev = notNull(dev);
    this.caps = surface.capabilities();
    this.formats = surface.formats();
    this.modes = surface.modes();
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
    // Create swapchain
    VulkanLibrary lib = dev.library();
    ReferenceFactory factory = lib.factory();
    PointerByReference chain = factory.pointer();
    check(lib.vkCreateSwapchainKHR(dev.handle(), info, null, chain));
    ...
}
```

Next we retrieve the handles to the swapchain images created by Vulkan:

```java
// Retrieve swapchain images
VulkanFunction<Pointer[]> func = (api, count, array) -> api.vkGetSwapchainImagesKHR(dev.handle(), chain.getValue(), count, array);
Pointer[] handles = VulkanFunction.enumerate(func, lib, factory::array);
```

The images share the same descriptor:

```java
// Init swapchain image descriptor
Dimensions extents = new Dimensions(
    info.imageExtent.width,
    info.imageExtent.height
);
Descriptor descriptor = new Descriptor.Builder()
    .format(info.imageFormat)
    .extents(new ImageExtents(extents))
    .aspect(VkImageAspect.COLOR)
    .build();
```

Which is used when we create the view for each swapchain image:

```java
// Create image views
var views = Arrays
    .stream(handles)
    .map(Handle::new)
    .map(image -> new Image(image, dev, descriptor))
    .map(Image::view)
    .collect(toList());
```

And finally we create the swapchain domain object itself:

```java
return new Swapchain(chain.getValue(), dev, info.imageFormat, extents, views);
```

And add a new API:

```java
interface VulkanLibrarySwapchain {
    int vkCreateSwapchainKHR(Handle device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, PointerByReference pSwapchain);
    void vkDestroySwapchainKHR(Handle device, Handle swapchain, Handle pAllocator);
    int vkGetSwapchainImagesKHR(Handle device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);
}
```

### Presentation

To support presentation we extend the new API with the following methods:

```java
interface VulkanLibrarySwapchain {
    ...
    int vkAcquireNextImageKHR(Handle device, Handle swapchain, long timeout, Handle semaphore, Handle fence, IntByReference pImageIndex);
    int vkQueuePresentKHR(Handle queue, VkPresentInfoKHR pPresentInfo);
}
```

We add the following to acquire the index of the next image to be rendered:

```java
public class Swapchain ... {
    private final IntByReference index = new IntByReference();
    
    public int acquire(Semaphore semaphore, Fence fence) {
        if((semaphore == null) && (fence == null)) throw new IllegalArgumentException("Either semaphore or fence must be provided");
        final DeviceContext dev = device();
        final VulkanLibrary lib = dev.library();
        check(lib.vkAcquireNextImageKHR(dev.handle(), this.handle(), Long.MAX_VALUE, NativeObject.ofNullable(semaphore), NativeObject.ofNullable(fence), index));
        return index.getValue();
    }
}
```

The _semaphore_ and _fence_ are synchronisation primitives that are covered in a later chapter when we fully implement the render loop.

When an image has been rendered it can be presented to the surface, which requires population of a Vulkan descriptor for the presentation task:

```java
public void present(Queue queue, Set<Semaphore> semaphores) {
    // Create presentation descriptor
    VkPresentInfoKHR info = new VkPresentInfoKHR();

    // Populate wait semaphores
    info.waitSemaphoreCount = semaphores.size();
    info.pWaitSemaphores = Handle.toArray(semaphores);

    // Populate swap-chain
    info.swapchainCount = 1;
    info.pSwapchains = Handle.toArray(List.of(this));
    
    ...
}
```

Next we specify the array of images to be presented as a contiguous memory block:

```java
// Set image indices
int[] array = new int[]{index.getValue()};
Memory mem = new Memory(array.length * Integer.BYTES);
mem.write(0, array, 0, array.length);
info.pImageIndices = mem;
```

And finally we invoke the API method that adds the presentation task to the relevant work queue:

```java
// Present frame
check(lib.vkQueuePresentKHR(queue.handle(), info));
```

Notes:

* The API supports presentation of multiple swapchains in one operation but we restrict ourselves to a single instance for the moment.

* Similarly the `pImageIndices` array is limited to a single image.

* The presentation task descriptor is created on every invocation of the `present` method which we may well want to cache later.

---

TODO

TODO

TODO

## The Render Pass

### Parent Builder

A render pass consists of a number of _sub-passes_ (at least one) that operate on the results of the previous passes to implement (for example) post processing effects.

For now the render pass domain object is nothing more than a handle though we will be adding more functionality later:

```java
public class RenderPass extends AbstractVulkanObject {
    RenderPass(Pointer handle, LogicalDevice dev) {
        super(handle, dev, dev.library()::vkDestroyRenderPass);
    }
}
```

However constructing this object requires three pieces of information:
1. One or more attachment descriptions, e.g. a colour attachment and the depth buffer.
2. At least one sub-pass description.
3. A number of sub-pass _dependencies_ that specify how the sub-passes are linked.

Therefore the builder for this class is itself comprised of a number of _nested_ builders for each of the above:

```java
public static class Builder {
    private final LogicalDevice dev;
    private final List<AttachmentBuilder> attachments = new ArrayList<>();
    private final List<SubPassBuilder> subpasses = new ArrayList<>();
    //private final List<VkSubpassDependency> dependencies = new ArrayList<>();         // TODO

    public Builder(LogicalDevice dev) {
        this.dev = notNull(dev);
    }

    /**
     * @return New attachment builder
     */
    public AttachmentBuilder attachment() {
        return new AttachmentBuilder();
    }

    /**
     * @return New sub-pass builder
     */
    public SubPassBuilder subpass() {
        return new SubPassBuilder();
    }

    /**
     * Constructs this render pass.
     * @return New render pass
     */
    public RenderPass build() {
    }
}
```

Notes:
- The 'parent' builder maintains a list of the nested builders.
- We omit dependencies for the moment as they are not needed for this stage of development.

### Attachments

The nested builder for an attachment is relatively straight-forward:

```java
public class AttachmentBuilder {
    private VkFormat format;
    private VkSampleCountFlag samples = VkSampleCountFlag.COUNT_1;
    private VkAttachmentLoadOp loadOp = VkAttachmentLoadOp.DONT_CARE;
    private VkAttachmentStoreOp storeOp = VkAttachmentStoreOp.DONT_CARE;
    private VkAttachmentLoadOp stencilLoadOp = VkAttachmentLoadOp.DONT_CARE;
    private VkAttachmentStoreOp stencilStoreOp = VkAttachmentStoreOp.DONT_CARE;
    private VkImageLayout initialLayout = VkImageLayout.UNDEFINED;;
    private VkImageLayout finalLayout;

    ...

    public Builder build() {
        attachments.add(this);
        return Builder.this;
    }

    private void populate(VkAttachmentDescription desc) {
        final var desc = new VkAttachmentDescription();
        desc.format = format;
        desc.samples = samples;
        desc.loadOp = loadOp;
        desc.storeOp = storeOp;
        desc.stencilLoadOp = stencilLoadOp;
        desc.stencilStoreOp = stencilStoreOp;
        desc.initialLayout = initialLayout;
        desc.finalLayout = finalLayout;
        return desc;
    }
}
```

The parent builder will allocate the attachment and sub-pass descriptors that are initialised by the `populate` method.

### Sub-Passes

A sub-pass specifies the attachments that are used in that stage of rendering:

```java
public class SubPassBuilder {
    /**
     * Attachment reference.
     */
    private class Reference {
        ...
    }
    
    private VkPipelineBindPoint bind = VkPipelineBindPoint.GRAPHICS;
    private final List<Reference> colour = new ArrayList<>();
    private Reference depth;

    /**
     * Sets the bind point of this sub-pass.
     * @param bind Bind point (default is {@link VkPipelineBindPoint#GRAPHICS})
     */
    public SubPassBuilder bind(VkPipelineBindPoint bind) {
        this.bind = notNull(bind);
        return this;
    }

    /**
     * Adds a colour attachment.
     * @param index         Attachment index
     * @param layout        Attachment layout
     */
    public SubPassBuilder colour(int index, VkImageLayout layout) {
        colour.add(new Reference(index, layout));
        return this;
    }

    /**
     * Adds the depth-buffer attachment.
     * @param index Attachment index
     */
    public SubPassBuilder depth(int index) {
        this.depth = new Reference(index, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
        return this;
    }

    /**
     * Populates the descriptor for this sub-pass.
     * @param desc Sub-pass descriptor
     */
    private void populate(VkSubpassDescription desc) {
        // Init descriptor
        desc.pipelineBindPoint = bind;

        // Populate colour attachments
        desc.colorAttachmentCount = colour.size();
        desc.pColorAttachments = VulkanStructure.array(VkAttachmentReference::new, colour, Reference::populate);

        // Populate depth attachment
        if(depth != null) {
            desc.pDepthStencilAttachment = new VkAttachmentReference();
            depth.populate(desc.pDepthStencilAttachment);
        }
    }

    public Builder build() {
        subpasses.add(this);
        return Builder.this;
    }
}
```

The attachment reference is a simple transient local class:

```java
private class Reference {
    private final int index;
    private final VkImageLayout layout;

    /**
     * Constructor.
     * @param index         Attachment index
     * @param layout        Image layout
     * @throws IllegalArgumentException if the index is invalid for this render pass or the layout is undefined
     */
    private Reference(int index, VkImageLayout layout) {
        this.index = zeroOrMore(index);
        this.layout = notNull(layout);
        if(index >= attachments.size()) throw new IllegalArgumentException(...);
        if(layout == VkImageLayout.UNDEFINED) throw new IllegalArgumentException(...);
    }

    /**
     * Populates the descriptor for this attachment reference.
     * @param ref Attachment reference descriptor
     */
    private void populate(VkAttachmentReference ref) {
        ref.attachment = index;
        ref.layout = layout;
    }
}
```

We essentially follow the underlying Vulkan structures in this design but in future we may decide to introduce an actual attachment domain object and remove the need for indexes to refer to the attachments in the sub-passes and dependencies.

### Creating the Render Pass

Finally the build method for the render pass aggregates the descriptors from the nested builders, invokes the API and creates the domain object:

```java
public RenderPass build() {
    // Create render pass descriptor
    final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

    // Add attachments
    if(attachments.isEmpty()) throw new IllegalArgumentException("At least one attachment must be specified");
    info.attachmentCount = attachments.size();
    info.pAttachments = StructureCollector.toPointer(VkAttachmentDescription::new, attachments, AttachmentBuilder::populate);

    // Add sub-passes
    if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
    info.subpassCount = subpasses.size();
    info.pSubpasses = StructureCollector.toPointer(VkSubpassDescription::new, subpasses, SubPassBuilder::populate);

    // Allocate render pass
    final VulkanLibrary lib = dev.library();
    final PointerByReference pass = lib.factory().pointer();
    check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

    // Create render pass
    return new RenderPass(pass.getValue(), dev);
}
```

### Integration

We can now add the render pass to our triangle demo which consists of a single colour attachment:

```java
// Create render pass
final RenderPass pass = new RenderPass.Builder(dev)
    .attachment()
        .format(format)
        .load(VkAttachmentLoadOp.CLEAR)
        .store(VkAttachmentStoreOp.STORE)
        .finalLayout(VkImageLayout.PRESENT_SRC)
        .build()
    .subpass()
        .colour(0, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
        .build()
    .build();
```

The colour attachment is cleared before rendering and transitioned to a layout ready for presentation to the surface.

---

## Frame Buffers

The last component we require for rendering is the _frame buffer_ which contains the attachments to be rendered:

```java
public class FrameBuffer extends AbstractVulkanObject {
    /**
     * Creates a frame buffer for the given views.
     * @param views Swapchain image views
     * @param pass Render pass
     * @return New frame buffer
     */
    public static FrameBuffer create(List<View> views, RenderPass pass) {
        // Use extents of first attachment
        Check.notEmpty(views);
        final Image.Extents extents = views.get(0).image().descriptor().extents();

        // Build descriptor
        final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
        info.renderPass = pass.handle();
        info.attachmentCount = views.size();
        info.pAttachments = Handle.toArray(views);
        info.width = extents.width();
        info.height = extents.height();
        info.layers = 1; // TODO

        // Allocate frame buffer
        final LogicalDevice dev = pass.device();
        final VulkanLibrary lib = dev.library();
        final PointerByReference buffer = lib.factory().pointer();
        check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

        // Create frame buffer
        return new FrameBuffer(buffer.getValue(), dev, extents, views);
    }

    private final List<View> attachments;

    private FrameBuffer(Pointer handle, LogicalDevice dev, List<View> attachments) {
        super(handle, dev, dev.library()::vkDestroyFramebuffer);
        this.extents = notNull(extents);
        this.attachments = List.copyOf(notEmpty(attachments));
    }

    public List<View> attachments() {
        return attachments;
    }
}
```

The buffers are added to the demo as follows:

```java
// Create frame buffers
final var buffers = chain
    .views()
    .stream()
    .map(view -> FrameBuffer.create(List.of(view), pass))
    .collect(toList());
```

This class is little more than a handle and a list of views - we will probably wrap this as a member of the swapchain class at some point since it seems unlikely we will need to add any specific functionality and the class is already coupled to the swapchain views.

---

## Framework Enhancements

In this section we cover the new supporting classes used in this chapter.

### Native Handles

Most of the Vulkan domain objects developed thus far have a _handle_ which is the JNA pointer returned by the various API methods.  However a JNA pointer is mutable which essentially breaks the class if we expose the handle.

To resolve this mutability issue we first define a native object that contains a handle:

```java
public interface NativeObject {
    Handle handle();
}
```

The `Handle` is an opaque wrapper for a JNA pointer:

```java
public final class Handle {
    private final Pointer ptr;

    public Handle(Pointer ptr) {
        this.ptr = notNull(ptr);
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

To use this new type in Vulkan API methods and structures we implement a custom JNA type converter:

```java
public static final TypeConverter CONVERTER = new TypeConverter() {
    @Override
    public Class<?> nativeType() {
        return Pointer.class;
    }

    @Override
    public Object toNative(Object value, ToNativeContext context) {
        if(value == null) {
            return null;
        }
        else {
            final Handle handle = (Handle) value;
            return handle.ptr;
        }
    }

    @Override
    public Object fromNative(Object value, FromNativeContext context) {
        if(value == null) {
            return null;
        }
        else {
            return new Handle((Pointer) value);
        }
    }
};
```

The new converter is registered with the type mapper of the Vulkan JNA library and we refactor all the existing domain classes accordingly.

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

    /**
     * Constructor.
     * @param handle        Object handle
     * @param dev           Logical device
     */
    protected AbstractVulkanObject(Pointer handle, LogicalDevice dev) {
        super(new Handle(handle));
        this.dev = notNull(dev);
    }
}
```

To destroy this object we introduce the following abstraction for a _destructor_ method:

```java
@FunctionalInterface
public interface Destructor {
    /**
     * Destroys this object.
     * @param dev           Logical device
     * @param handle        Handle
     * @param allocator     Vulkan memory allocator (always {@code null})
     */
    void destroy(Handle dev, Handle handle, Handle allocator);
}
```

We add the following to return the API method used to destroy the object:

```java
/**
 * Provides the <i>destructor</i> API method for this object.
 * @param lib Vulkan API
 * @return Destructor method
 */
protected abstract Destructor destructor(VulkanLibrary lib);
```

Which is used in the overridden `destroy` method:

```java
@Override
public synchronized void destroy() {
    // Destroy this object
    final Destructor destructor = destructor(dev.library());
    destructor.destroy(dev.handle(), this.handle(), null);

    // Delegate
    super.destroy();
}
```

Ideally `destroy` would also be `final` but this would make testing trickier (since base Mockito cannot mock a final method).

---

## Improvements








### Vulkan Booleans

Development of the swapchain was the first time we came across JNA code using boolean values and we encountered a curious problem that stumped us for some time.
See [this](https://stackoverflow.com/questions/55225896/jna-maps-java-boolean-to-1-integer) stack-overflow question.

In summary: a Vulkan boolean is represented as zero (for false) or one (for true) - so far so logical. 
But by default JNA maps a Java boolean to zero for false but **minus one** for true!  WTF!

There are a lot of boolean values used across Vulkan so we needed some global solution to over-ride the default JNA mapping.

We created the [VulkanBoolean](https://github.com/stridecolossus/JOVE/blob/master/src/main/java/org/sarge/jove/common/VulkanBoolean.java) class to map a Java boolean to/from a native integer represented as zero or one:

```java
public final class VulkanBoolean {
    public static final VulkanBoolean TRUE = new VulkanBoolean(true);
    public static final VulkanBoolean FALSE = new VulkanBoolean(false);
    
    /**
     * Converts a native integer value to a Vulkan boolean (non-zero is {@code true}).
     * @param value Native value
     * @return Vulkan boolean
     */
    public static VulkanBoolean of(int value) {
        return value == 0 ? VulkanBoolean.FALSE : VulkanBoolean.TRUE;
    }

    public static VulkanBoolean of(boolean bool) {
        return bool ? VulkanBoolean.TRUE : VulkanBoolean.FALSE;
    }

    private final boolean value;

    private VulkanBoolean(boolean value) {
        this.value = value;
    }

    /**
     * @return Native integer representation of this boolean (1 for {@code true} or 0 for {@code false})
     */
    private int toInteger() {
        return value ? 1 : 0;
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }
}
```

Again we used a JNA type converter to map the new type to/from its native representation:

```java
static final TypeConverter CONVERTER = new TypeConverter() {
    @Override
    public Class<?> nativeType() {
        return Integer.class;
    }

    @Override
    public Object toNative(Object value, ToNativeContext context) {
        if(value == null) {
            return 0;
        }
        else {
            final VulkanBoolean bool = (VulkanBoolean) value;
            return bool.toInteger();
        }
    }

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        if(nativeValue == null) {
            return VulkanBoolean.FALSE;
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

In this chapter we implemented the swapchain, render pass and frame-buffer domain objects.

The API methods introduced in this chapter are defined in the `VulkanLibrarySwapChain`, `VulkanLibraryRenderPass` and `VulkanLibraryFrameBuffer` JNA interfaces.







### Format Builder

When testing the new swapchain we realised that finding an image format in the `VkFormat` enumeration can be quite tricky - there are an awful lot of them.
However the names are logical and consistent so we implement a helper that allows us to programatically define an image format:

```java
public class FormatBuilder {
    public static final String RGBA = "RGBA";
    public static final String ARGB = "ARGB";
    public static final String BGRA = "BGRA";

    /**
     * Component data-type.
     */
    public enum Type {
        INTEGER("INT"),
        FLOAT("FLOAT"),
        NORMALIZED("NORM"),
        SCALED("SCALED"),
        RGB("RGB");

        private final String token;

        private Type(String token) {
            this.token = token;
        }
    }

    private String components = RGBA;
    private int num = 4;
    private int bytes = 4;
    private Type type = Type.FLOAT;
    private boolean signed = true;
}
```

The build method constructs the format name from the various fields and looks up the enumeration constant using `valueOf`:

```java
public VkFormat build() {
    // Validate format
    if(num > components.length()) throw new IllegalArgumentException(...);

    // Build component layout
    final StringBuilder layout = new StringBuilder();
    for(int n = 0; n < num; ++n) {
        layout.append(components.charAt(n));
        layout.append(bytes * Byte.SIZE);
    }

    // Build format string
    final String format = new StringJoiner("_")
        .add("VK_FORMAT")
        .add(layout.toString())
        .add((signed ? "S" : "U") + type.token)
        .toString();

    // Lookup format
    return VkFormat.valueOf(format);
}
```



### Integration

We now have all the elements we need to add the swapchain to our demo application:

```java
// Specify required image format
final VkFormat format = new FormatBuilder()
    .components(FormatBuilder.BGRA)
    .bytes(1)
    .signed(false)
    .type(Vertex.Component.Type.NORMALIZED)
    .build();

// Create double-buffer swap-chain
final SwapChain chain = new SwapChain.Builder(surface)
    .format(format)
    .count(2)
    .space(VkColorSpaceKHR.SRGB_NONLINEAR)
    .build();
```

In this example the image format is: `B8G8R8A8_UNORM`


