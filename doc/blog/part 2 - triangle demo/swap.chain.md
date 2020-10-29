# Overview

In this chapter we will implement the various components required for rendering to the Vulkan surface.

A _swap chain_ is comprised of one-or-more _frame buffers_ that are presented to the display in turn.  An application will generally employ a double-buffer (or even triple-buffer) strategy whereby one frame is presented while another is being rendered, then they are swapped, and the process repeats for the next frame.

A _frame buffer_ is comprised of a number of _attachments_ that are used during rendering such as colour attachments, the depth buffer, etc.

We will also implement the _render pass_ that specifies how the attachments are managed during the rendering process.


# The Swapchain

## Overview

Before we can actually start on the swapchain we will need new domain objects for the following:
1. A rendering surface with accessors for the supported capabilities and image formats.
2. New domain classes for the swapchain images and views.

## Rendering Surface

We start with a new domain class for the _rendering surface_ that wraps the handle we previously retrieved from GLFW:

```java
public class Surface implements TransientNativeObject {
    private final Handle handle;
    private final LogicalDevice dev;

    public Surface(Handle handle, LogicalDevice dev) {
        this.handle = notNull(handle);
        this.dev = notNull(dev);
    }

    @Override
    public Handle handle() {
        return handle;
    }

    @Override
    public synchronized void destroy() {
        final Instance instance = dev.parent().instance();
        final VulkanLibrarySurface lib = instance.library();
        lib.vkDestroySurfaceKHR(instance.handle(), handle, null);
    }
}
```

The surface provides the following accessors that will be used when we implement the swapchain:

```java
/**
 * @return Capabilities of this surface
 */
public VkSurfaceCapabilitiesKHR capabilities() {
    final VulkanLibrary lib = dev.library();
    final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
    check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.parent().handle(), handle, caps));
    return caps;
}

/**
 * @return Formats supported by this surface
 */
public Collection<VkSurfaceFormatKHR> formats() {
    final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.parent().handle(), handle, count, array);
    final var formats = VulkanFunction.enumerate(func, dev.library(), VkSurfaceFormatKHR::new);
    return Arrays.stream(formats).collect(toList());
}

/**
 * @return Presentation modes supported by this surface
 */
public Set<VkPresentModeKHR> modes() {
    // Count number of supported modes
    final VulkanLibrary lib = dev.library();
    final Handle handle = dev.parent().handle();
    final IntByReference count = lib.factory().integer();
    check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(handle, handle, count, null));

    // Retrieve modes
    final int[] array = new int[count.getValue()];
    check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(handle, handle, count, array));

    // Convert to enumeration
    return Arrays
            .stream(array)
            .mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n))
            .collect(toSet());
}
```

While this object does not contain much functionality it abstracts over the various API methods that use a combination the logical device, physical device and instance.

## Images and Views

A Vulkan _image_ is essentially a descriptor for an image _object_ managed by Vulkan (i.e. we are not actually dealing with image _data_ here).

The new class is relatively trivial at this stage:

```java
public class Image {
    public record Extents(int width, int height, int depth) {
    }

    private final Pointer handle;
    private final LogicalDevice dev;
    private final VkFormat format;
    private final Extents extents;
    private final Set<VkImageAspectFlag> aspect;

    private VkImageLayout layout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;

    public Image(Pointer handle, LogicalDevice dev, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspect) {
    }
}
```

An _image view_ is a reference to an image and is the entry-point for operations on images such as transforms, sampling, etc.

This class initially has no functionality but will be expanded when we address textures:

```java
public class View {
    private final Pointer handle;
    private final Image image;
}
```

Note that both of these new domain objects are managed by Vulkan and therefore do not need to be explicitly destroyed by the application.

## The Swap Chain

Now we can finally create the swapchain itself:

```java
public class SwapChain extends AbstractVulkanObject {
    private final VkFormat format;
    private final Dimensions extents;
    private final List<View> views;

    /**
     * Constructor.
     * @param handle        Swap-chain handle
     * @param dev           Logical device
     * @param format        Image format
     * @param views         Image views
     */
    SwapChain(Pointer handle, LogicalDevice dev, VkFormat format, Dimensions extents, List<View> views) {
        super(handle, dev, dev.library()::vkDestroySwapchainKHR);
        ...
    }

    /**
     * Acquires the next image in this swap-chain.
     * @return Image index
     */
    public int acquire() {
    }

    /**
     * Presents the next frame.
     * @param queue Presentation queue
     */
    public void present(Queue queue) {
    }
}
```

The swapchain is highly configurable so again we will create a builder to specify its properties, but in this case we also expose a package-private constructor to simplify testing:

```java
public static class Builder {
    // Dependencies
    private final Surface surface;
    private final LogicalDevice dev;

    // Properties
    private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
    private ClearValue clear = ClearValue.COLOUR;

    // Surface constraints
    private final VkSurfaceCapabilitiesKHR caps;
    private final Collection<VkSurfaceFormatKHR> formats;

    /**
     * Constructor.
     */
    public Builder(Surface surface, LogicalDevice dev) {
        this.surface = notNull(surface);
        this.dev = notNull(dev);
        this.caps = surface.capabilities();
        this.formats = surface.formats();
    }
}
```

Note that this builder directly populates an instance of the descriptor rather than replicating the fields or introducing an intermediate POJO.

The capabilities and presentation formats supported by the surface are used to validate the setters in the builder, for example:

```java
/**
 * Sets the image usage flag.
 * @param usage Image usage
 * @throws IllegalArgumentException if the usage flag is not supported by the surface
 */
public Builder usage(VkImageUsageFlag usage) {
    if(!IntegerEnumeration.contains(caps.supportedUsageFlags, usage)) {
        throw new IllegalArgumentException("Usage not supported: " + usage);
    }
    info.imageUsage = notNull(usage);
    return this;
}
```

The builder also initialises the descriptor in its constructor to sensible defaults using the various setters:

```java
count(caps.minImageCount);
transform(caps.currentTransform);
space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
arrays(1);
mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE); // or concurrent?
usage(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
alpha(VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
present(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR);
clipped(true);
```

Constructing the swapchain is comprised of three steps:
1. Create the swapchain.
2. Retrieve the image handles.
3. Create a view for each image.

```java
public SwapChain build() {
    // Allocate swap-chain
    final LogicalDevice dev = surface.device();
    final VulkanLibrary lib = dev.library();
    final ReferenceFactory factory = lib.factory();
    final PointerByReference chain = factory.pointer();
    check(lib.vkCreateSwapchainKHR(dev.handle(), info, null, chain));

    // Get swap-chain image views
    final VulkanFunction<Pointer[]> func = (api, count, array) -> api.vkGetSwapchainImagesKHR(dev.handle(), chain.getValue(), count, array);
    final var handles = VulkanFunction.enumerate(func, lib, factory::pointers);
    final var views = Arrays.stream(handles).map(this::view).collect(toList());

    // Create swap-chain
    final Dimensions extent = new Dimensions(info.imageExtent.width, info.imageExtent.height);
    return new SwapChain(chain.getValue(), dev, info.imageFormat, extent, views);
}
```

The image views are created by the following helper:

```java
private View view(Pointer handle) {
    final Image.Extents extents = new Image.Extents(info.imageExtent.width, info.imageExtent.height);
    final Image image = new Image(handle, surface.device(), info.imageFormat, extents, Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));
    return new View(image);
}
```

Finally we extend the API for these new types:

```java
interface VulkanLibrarySwapChain {
    int vkCreateSwapchainKHR(Pointer device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);
    void vkDestroySwapchainKHR(Pointer device, Pointer swapchain, Pointer pAllocator);
    int vkGetSwapchainImagesKHR(Pointer device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);
    int vkAcquireNextImageKHR(Pointer device, Pointer swapchain, long timeout, Pointer semaphore, Pointer fence, IntByReference pImageIndex);
}

interface VulkanLibraryImage {
    int vkCreateImage(Pointer device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pImage);
    void vkDestroyImage(Pointer device, Pointer image, Pointer pAllocator);
    int vkCreateImageView(Pointer device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);
    void vkDestroyImageView(Pointer device, Pointer imageView, Pointer pAllocator);
}
```

## Presentation

We can now turn to presentation of the swapchain images.

We first add an image index:

```java
public class SwapChain extends AbstractVulkanObject {
    ...
    private final IntByReference index = new IntByReference();
}
```

This is used to acquire the next image to be rendered at the start of a frame:

```java
public int acquire() {
    final VulkanLibrarySwapChain lib = dev.library();
    check(lib.vkAcquireNextImageKHR(dev.handle(), handle, Long.MAX_VALUE, null, null, index));
    return index.getValue();
}
```

When an image has been completed it can be presented to the surface:

```java
public void present(Queue queue) {
    // Create presentation descriptor
    final VkPresentInfoKHR info = new VkPresentInfoKHR();

    // Add swap-chains
    info.swapchainCount = 1;
    info.pSwapchains = Handle.toPointerArray(List.of(this));

    // Set image indices
    final int[] array = new int[]{index.getValue()};
    final Memory mem = new Memory(array.length * Integer.BYTES);
    mem.write(0, array, 0, array.length);
    info.pImageIndices = mem;

    // Present frame
    final VulkanLibrary lib = device().library();
    check(lib.vkQueuePresentKHR(queue.handle(), new VkPresentInfoKHR[]{info}));
}
```

Notes:
- The API is designed to present multiple swapchains but we limit ourselves to one for the moment.
- We create the descriptor for presentation on every invocation of the presentation method - we may want to cache these later.
- We will address synchronisation in a future chapter.

## Format Builder

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

The build method constructs the format name from the various fields and looks up the enumeration constant using `valueOf()`:

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
    return VkFormat.valueOf(format.toString());
}
```

## Integration

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
    .space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
    .build();
```

In this example the image format is: `VK_FORMAT_B8G8R8A8_UNORM`


# The Render Pass

## Parent Builder

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

## Attachments

The nested builder for an attachment is relatively straight-forward:

```java
public class AttachmentBuilder {
    private VkFormat format;
    private VkSampleCountFlag samples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT;
    private VkAttachmentLoadOp loadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
    private VkAttachmentStoreOp storeOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
    private VkAttachmentLoadOp stencilLoadOp = VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_DONT_CARE;
    private VkAttachmentStoreOp stencilStoreOp = VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_DONT_CARE;
    private VkImageLayout initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;;
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

The populate() method fills the descriptor for an attachment that is allocated by the parent builder (shown below).

## Sub-Passes

A sub-pass specifies the attachments that are used in that stage of rendering:

```java
public class SubPassBuilder {
    /**
     * Attachment reference.
     */
    private class Reference {
        ...
    }
    
    private VkPipelineBindPoint bind = VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS;
    private final List<Reference> colour = new ArrayList<>();
    private Reference depth;

    /**
     * Sets the bind point of this sub-pass.
     * @param bind Bind point (default is {@link VkPipelineBindPoint#VK_PIPELINE_BIND_POINT_GRAPHICS})
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
     * Adds a depth-buffer attachment.
     * @param index Attachment index
     */
    public SubPassBuilder depth(int index) {
        this.depth = new Reference(index, VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
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
        if(layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) throw new IllegalArgumentException(...);
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

## Creating the Render Pass

Finally the build method for the render pass aggregates the descriptors from the nested builders, invokes the API and creates the domain object:

```java
public RenderPass build() {
    // Create render pass descriptor
    final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

    // Add attachments
    if(attachments.isEmpty()) throw new IllegalArgumentException("At least one attachment must be specified");
    info.attachmentCount = attachments.size();
    info.pAttachments = VulkanStructure.array(VkAttachmentDescription::new, attachments, AttachmentBuilder::populate);

    // Add sub-passes
    if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
    info.subpassCount = subpasses.size();
    info.pSubpasses = VulkanStructure.array(VkSubpassDescription::new, subpasses, SubPassBuilder::populate);

    // Allocate render pass
    final VulkanLibrary lib = dev.library();
    final PointerByReference pass = lib.factory().pointer();
    check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

    // Create render pass
    return new RenderPass(pass.getValue(), dev);
}
```

## Integration

We can now add the render pass to our triangle demo which consists of a single colour attachment:

```java
// Create render pass
final RenderPass pass = new RenderPass.Builder(dev)
    .attachment()
        .format(format)
        .load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
        .store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
        .finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
        .build()
    .subpass()
        .colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
        .build()
    .build();
```

The colour attachment is cleared before rendering and transitioned to a layout ready for presentation to the surface.


# Frame Buffers

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
        info.pAttachments = Handle.toPointerArray(views);
        info.width = extents.width();
        info.height = extents.height();
        info.layers = 1; // TODO

        // Allocate frame buffer
        final LogicalDevice dev = pass.device();
        final VulkanLibrary lib = dev.library();
        final PointerByReference buffer = lib.factory().pointer();
        check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

        // Create frame buffer
        return new FrameBuffer(buffer.getValue(), dev, views);
    }

    private final List<View> attachments;

    /**
     * Constructor.
     * @param handle            Handle
     * @param dev               Logical device
     * @param attachments       Image attachments
     */
    private FrameBuffer(Pointer handle, LogicalDevice dev, List<View> attachments) {
        super(handle, dev, dev.library()::vkDestroyFramebuffer);
        this.attachments = List.copyOf(notEmpty(attachments));
    }

    /**
     * @return Image attachments
     */
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

