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

* Similarly the `pImageIndices` array therefore consists of a single image index.

* The presentation task descriptor is created on every invocation of the `present` method which we may well want to cache later.

---

## Render Pass

### Overview

A _render pass_ is comprised of the following:

* A group of _attachments_ that define the structure and format of the frame buffers.

* One-or-more _sub passes_ that operate sequentially on the attachments (multiple sub passes are used to implement post processing effects).

* A number of _dependencies_ that specify how the sub-passes are linked (to allow the hardware to synchronise the process).

We will creates builders for each of these and comprise the new components into a render pass domain object.

### Attachments

We start with a new domain object and builder for an attachment which is essentially a wrapper for the code-generated structure:

```java
public class Attachment {
    private final VkAttachmentDescription desc;

    private Attachment(VkAttachmentDescription desc) {
        this.desc = notNull(desc);
    }
}
```

When creating the render pass the contiguous array of attachments is populated by the following method:

```java
public void populate(VkAttachmentDescription attachment) {
    attachment.format = desc.format;
    attachment.samples = desc.samples;
    attachment.loadOp = desc.loadOp;
    attachment.storeOp = desc.storeOp;
    attachment.stencilLoadOp = desc.stencilLoadOp;
    attachment.stencilStoreOp = desc.stencilStoreOp;
    attachment.initialLayout = desc.initialLayout;
    attachment.finalLayout = desc.finalLayout;
}
```

Note that the attachment class wraps an instance of the underlying Vulkan descriptor, which avoids having to replicate the fields and implement a large constructor.  However JNA does not support cloning of a structure out-of-the-box so the `populate` method is performs a field-by-field copy.

The builder initialises most of the attachment properties to sensible defaults:

```java
public static class Builder {
    private VkAttachmentDescription desc = new VkAttachmentDescription();

    public Builder() {
        samples(VkSampleCountFlag.COUNT_1);
        load(VkAttachmentLoadOp.DONT_CARE);
        store(VkAttachmentStoreOp.DONT_CARE);
        stencilLoad(VkAttachmentLoadOp.DONT_CARE);
        stencilStore(VkAttachmentStoreOp.DONT_CARE);
        initialLayout(VkImageLayout.UNDEFINED);
    }
}
```

Creating the attachment is straight-forward:

```java
public Attachment build() {
    // Validate
    if(desc.format == null) throw new IllegalArgumentException("No format specified for attachment");
    if(desc.finalLayout == null) throw new IllegalArgumentException("No final layout specified");

    // Create attachment
    final Attachment attachment = new Attachment(desc);

    // Prevent fiddling
    desc = null;

    return attachment;
}
```

### Render Pass

The render pass domain object is relatively simple:

```java
public class RenderPass extends AbstractVulkanObject {
    private final List<Attachment> attachments;

    private RenderPass(Pointer handle, LogicalDevice dev, List<Attachment> attachments) {
        super(handle, dev);
        this.attachments = List.copyOf(attachments);
    }

    public List<Attachment> attachments() {
        return attachments;
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyRenderPass;
    }
}
```

Next we implement a builder for the render pass that constructs the dependant components:

```java
public static class Builder {
    private final List<Attachment> attachments = new ArrayList<>();
    private final List<SubPassBuilder> subpasses = new ArrayList<>();
    private final List<DependencyBuilder> dependencies = new ArrayList<>();

    /**
     * Adds an attachment to this render pass.
     * @param attachment Attachment to add
     * @return Attachment index
     */
    private int add(Attachment attachment) {
        Check.notNull(attachment);
        if(!attachments.contains(attachment)) {
            attachments.add(attachment);
        }
        return attachments.indexOf(attachment);
    }
}
```

Notes:

* Sub-passes and dependencies are _nested_ builders implemented as local classes.

* Attachments are not explicitly added to the render pass builder (the `add` method is private) but are instead registered as a side-effect of configuring the sub-passes (see below).

* Therefore attachment indices are automatically allocated rather than being explicitly configured by the user (a constraint that seems valid at this stage).

* We will address the `DependencyBuilder` later as sub-pass dependencies are not needed at this stage of development.

The `build` method follows the usual pattern to construct the render pass:

```java
public RenderPass build(LogicalDevice dev) {
    // Validate
    if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
    assert !attachments.isEmpty();

    // Create render pass descriptor
    final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

    // Add attachments
    info.attachmentCount = attachments.size();
    info.pAttachments = StructureHelper.first(attachments, VkAttachmentDescription::new, Attachment::populate);

    // Add sub-passes
    info.subpassCount = subpasses.size();
    info.pSubpasses = StructureHelper.first(subpasses, VkSubpassDescription::new, SubPassBuilder::populate);

    // Add dependencies
    info.dependencyCount = dependencies.size();
    info.pDependencies = StructureHelper.first(dependencies, VkSubpassDependency::new, DependencyBuilder::populate);

    // Allocate render pass
    final VulkanLibrary lib = dev.library();
    final PointerByReference pass = lib.factory().pointer();
    check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

    // Create render pass
    return new RenderPass(pass.getValue(), dev, attachments);
}
```

### Sub-Pass

A sub-pass is configured by invoking the following factory on the render pass builder:

```java
public static class Builder {
    /**
     * Starts a sub-pass.
     * @return New sub-pass builder
     */
    public SubPassBuilder subpass() {
        return new SubPassBuilder();
    }
}
```

The builder constructs a list of _references_ to the colour and optional depth-buffer attachments comprising the sub-pass:

```java
public class SubPassBuilder {
    private final List<Reference> colours = new ArrayList<>();
    private Reference depth;
}
```

The `Reference` is a simple transient record:

```java
private record Reference(int index, VkImageLayout layout) {
    private void populate(VkAttachmentReference ref) {
        ref.attachment = index;
        ref.layout = layout;
    }
}
```

Adding an attachment to the sub-pass also registers the attachment in the render pass using the `add` method:

```java
public SubPassBuilder colour(Attachment attachment, VkImageLayout layout) {
    final int index = add(attachment);
    colours.add(new Reference(index, layout));
    return this;
}

public SubPassBuilder depth(Attachment depth, VkImageLayout layout) {
    if(this.depth != null) throw new IllegalArgumentException("Depth attachment already configured");
    final int index = add(depth);
    this.depth = new Reference(index, layout);
    return this;
}
```

The nested builder adds _itself_ to the list of sub-passes and returns control to the _parent_ builder:

```java
public Builder build() {
    if((depth == null) && colours.isEmpty()) throw new IllegalArgumentException("No attachments specified in sub-pass");
    subpasses.add(this);
    return Builder.this;
}
```

Finally we implement the following to populate a sub-pass descriptor (used in the parent builder):

```java
void populate(VkSubpassDescription desc) {
    // Init descriptor
    desc.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

    // Populate colour attachments
    desc.colorAttachmentCount = colours.size();
    desc.pColorAttachments = StructureHelper.first(colours, VkAttachmentReference::new, Reference::populate);

    // Populate depth attachment
    if(depth != null) {
        desc.pDepthStencilAttachment = new VkAttachmentReference();
        depth.populate(desc.pDepthStencilAttachment);
    }
}
```

### Frame Buffers

The final component we require for rendering is the _frame buffer_ that composes the attachments used in the render pass:

```java
public class FrameBuffer extends AbstractVulkanObject {
    private final RenderPass pass;
    private final List<View> attachments;
    private final Dimensions extents;

    /**
     * Constructor.
     * @param handle            Handle
     * @param dev               Logical device
     * @param pass              Render pass
     * @param attachments       Image attachments
     * @param extents           Image extents
     */
    private FrameBuffer(Pointer handle, LogicalDevice dev, RenderPass pass, List<View> attachments, Dimensions extents) {
        super(handle, dev);
        this.extents = notNull(extents);
        this.attachments = List.copyOf(notEmpty(attachments));
        this.pass = notNull(pass);
    }

    /**
     * @return Image attachments
     */
    public List<View> attachments() {
        return attachments;
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyFramebuffer;
    }
}
```

A frame buffer is created using the following factory:

```java
public static FrameBuffer create(RenderPass pass, Dimensions extents, List<View> attachments) {
    // Build descriptor
    final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
    info.renderPass = pass.handle();
    info.attachmentCount = attachments.size();
    info.pAttachments = Handle.toArray(attachments);
    info.width = extents.width();
    info.height = extents.height();
    info.layers = 1;

    // Allocate frame buffer
    final DeviceContext dev = pass.device();
    final VulkanLibrary lib = dev.library();
    final PointerByReference buffer = lib.factory().pointer();
    check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

    // Create frame buffer
    return new FrameBuffer(buffer.getValue(), dev, pass, attachments, extents);
}
```

Notes:

* The `create` method performs a considerable amount of validation that is omitted for clarity.

* The frame buffer initially has no functionality but will be expanded when we actually address rendering.

TODO - Handle::toArray()

---

## Integration

### Presentation

We now have all the components we require to add the swapchain and render pass to the demo application.

```java
// Create double-buffer swapchain
Swapchain swapchain = new Swapchain.Builder(dev, surface)
    .count(2)
    .build();
```

The render pass consists of a single colour attachment which is cleared before rendering and transitioned to a layout ready for presentation:

```java
// Create colour attachment
Attachment attachment = new Attachment.Builder()
    .format(VkFormat.B8G8R8A8_UNORM)
    .load(VkAttachmentLoadOp.CLEAR)
    .store(VkAttachmentStoreOp.STORE)
    .finalLayout(VkImageLayout.PRESENT_SRC_KHR)
    .build();
```

We next create the render pass with a single sub-pass to render the colour attachment:

```java
// Create render pass
RenderPass pass = new RenderPass.Builder()
    .subpass()
        .colour(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
        .build()
    .build(dev);
```

Finally we create the frame buffers:

```java
```

TODO

### Format Builder

When developing the above code we realised that finding an image format in the `VkFormat` enumeration can be quite tricky - there are an awful lot of them.  However the enumeration names are logical and consistent which allows us to programatically select an image format.

We introduce the following helper class that is used to specify the various component of a required format:

```java
public class FormatBuilder {
    public static final String RGBA = "RGBA";
    public static final String ARGB = "ARGB";
    public static final String BGRA = "BGRA";

    private String template = RGBA;
    private int count = 4;
    private int bytes = 4;
    private Type type = Type.FLOAT;
    private boolean signed = true;
}
```

The `Type` field is an enumeration of the Vulkan component types:

```java
public enum Type {
    INTEGER("INT"),
    FLOAT("FLOAT"),
    NORMALIZED("NORM"),
    SCALED("SCALED"),
    RGB("RGB");
}
```

The builder constructs the format name and looks up the enumeration constant:

```java
public VkFormat build() {
    // Build component layout
    final StringBuilder layout = new StringBuilder();
    final int size = bytes * Byte.SIZE;
    for(int n = 0; n < count; ++n) {
        layout.append(template.charAt(n));
        layout.append(size);
    }

    // Build format string
    final char ch = signed ? 'S' : 'U';
    final String format = String.format("%s_%c%s", layout, ch, type.token);

    // Lookup format
    return VkFormat.valueOf(format);
}
```

We can now refactor the demo to build the format rather than having to find it in the enumeration:

```java
VkFormat format = new FormatBuilder()
    .template(FormatBuilder.BGRA)
    .bytes(1)
    .signed(false)
    .type(FormatBuilder.Type.NORMALIZED)
    .build();
```

Which maps to the `B8G8R8A8_UNORM` format used by the colour attachment.

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



