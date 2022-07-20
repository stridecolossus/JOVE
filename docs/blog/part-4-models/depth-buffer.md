---
title: Depth Buffers
---

---

## Contents

- [Overview](#overview)
- [Rendering](#rendering-the-model)
- [Depth Buffer](#depth-buffer)
- [Improvements](#improvements)

---

## Overview

In this chapter we will render the OBJ model constructed previously and resolve various visual problems that arise.

This will include the introduction of a _depth test_ requiring the following new functionality:

* Implementation of the depth-stencil pipeline stage.

* Attachment clear values.

Finally we will introduce various improvements to the existing code to simplify configuration of the presentation logic.

---

## Rendering the Model

### Configuration

A new `ModelDemo` project is started based on the previous rotating cube demo, minus the rotation animation.

Next the previous VBO configuration is replaced with a new class that loads the buffered model:

```java
@Configuration
public class ModelConfiguration {
    @Autowired private LogicalDevice dev;
    @Autowired private AllocationService allocator;
    @Autowired private Pool graphics;

    @Bean
    public static Model model(DataSource src) {
        var loader = new ResourceLoaderAdapter<>(src, new ModelLoader());
        return loader.load("chalet.model");
    }
}
```

Then the VBO and index buffer objects are created for the model:

```java
@Bean
public VertexBuffer vbo(Model model) {
    VulkanBuffer buffer = buffer(model.vertices(), VkBufferUsage.VERTEX_BUFFER);
    return new VertexBuffer(buffer);
}

@Bean
public IndexBuffer index(Model model) {
    VulkanBuffer buffer = buffer(model.index().get(), VkBufferUsage.INDEX_BUFFER);
    return new IndexBuffer(buffer);
}
```

Which both delegate to the following helper:

```java
private VulkanBuffer buffer(Bufferable data, VkBufferUsage usage) {
    // Create staging buffer
    VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, data);

    // Init buffer memory properties
    MemoryProperties<VkBufferUsage> props = new MemoryProperties.Builder<VkBufferUsage>()
        .usage(VkBufferUsage.TRANSFER_DST)
        .usage(usage)
        .required(VkMemoryProperty.DEVICE_LOCAL)
        .build();

    // Create buffer
    VulkanBuffer buffer = VulkanBuffer.create(dev, allocator, staging.length(), props);

    // Copy staging to buffer
    staging.copy(buffer).submitAndWait(graphics);

    // Release staging
    staging.destroy();

    return buffer;
}
```

In the render configuration the index buffer is injected and bound to the pipeline:

```java
add(index.bind())
```

The following temporary code is added to initialise the projection matrix:

```java
@Component
static class ApplicationLoop implements CommandLineRunner {
    @Autowired
    public void init(Matrix matrix, ResourceBuffer uniform) {
        uniform.load(matrix);
    }
}
```

The `@Autowired` annotation instructs the container to invoke the method once the component has been instantiated.

Finally the drawing command must be updated for the indexed model, we take the opportunity to implement a convenience builder on the `DrawCommand` class:

```java
public static class Builder {
    private boolean indexed;
    private int count;
    private int firstVertex;
    private int firstIndex;
    private int instanceCount = 1;
    private int firstInstance;
}
```

The build method selects the appropriate command variant depending on the supplied arguments:

```java
public DrawCommand build() {
    if(indexed) {
        return (api, buffer) -> api.vkCmdDrawIndexed(buffer, count, instanceCount, firstIndex, firstVertex, firstInstance);
    }
    else {
        return (api, buffer) -> api.vkCmdDraw(buffer, count, instanceCount, firstVertex, firstInstance);
    }
}
```

Convenience factory methods are added for common use-cases:

```java
static DrawCommand draw(int count) {
    return new Builder().count(count).build();
}

static DrawCommand indexed(int count) {
    return new Builder().indexed(0).count(count).build();
}
```

Finally a further helper is implemented to create a draw command for a given model:

```java
static DrawCommand of(Model model) {
    int count = model.header().count();
    if(model.isIndexed()) {
        return indexed(count);
    }
    else {
        return draw(count);
    }
}
```

The hard-coded draw command can now be replaced in the render sequence:

```java
Command draw = DrawCommand.of(model);
```

Finally, the previous view transform means we are looking at the model from above, a temporary static rotation is added to get a better viewing angle:

```java
public static Matrix matrix(Swapchain swapchain) {
    ...
    
    // Construct view transform
    Matrix trans = new Matrix.Builder()
        .identity()
        .column(3, new Point(0, -0.5f, -2))
        .build();

    ...

    // TODO - temporary
    Matrix x = Matrix.rotation(Vector.X, MathsUtil.toRadians(90));
    Matrix y = Matrix.rotation(Vector.Y, MathsUtil.toRadians(-120));
    Matrix model = y.multiply(x);

    // Create matrix
    return projection.multiply(view).multiply(model);
}
```

Where:

* The translation component of the view transform moves the camera back a bit and slightly above 'ground' level.

* The _x_ rotation tilts the model so we are looking at it from the side.

* And _y_ rotates vertically so the camera is facing the corner of the chalet with the door.

We run the demo and see what we get, which is a bit of a mess!

![Broken Chalet Model](mess.png)

There are a couple of issues here:

* The texture looks upside down, the grass is on the roof and vice-versa.

* Fragments are being rendered arbitrarily overlapping each other.

### Texture Coordinate Invert

The upside-down texture is due to the fact that OBJ texture coordinates (and OpenGL) assume an origin at the bottom-left of the image, whereas for Vulkan the 'start' of the texture image is the top-left corner.

We _could_ fiddle the texture coordinates in the shader, or flip the texture using an editor application, or invert it programatically at load time.  However none of these resolve the actual root problem, flipping the image would just add extra effort, and inverting at load-time would only make loading slower.  Instead the vertical texture coordinate is flipped _once_ when the OBJ model is first loaded.

The following adapter method flips the vertical component of each texture coordinate:

```java
private static Coordinate2D flip(float[] array) {
    assert array.length == 2;
    return new Coordinate2D(array[0], -array[1]);
}
```

And the parser in the OBJ loader is updated accordingly:

```java
add("vt", new VertexComponentParser<>(2, ObjectModelLoader::flip, ObjectModel::coordinate));
```

We assume that this will apply to all OBJ models, it can always be made an optional feature if that assumption turns out to be incorrect.

The model now looks to be textured correctly, in particular the signs on the front of the chalet are the right way round (so the model is not being rendered inside-out for example).

---

## Depth Buffer

### Pipeline Stage

To resolve the issue of overlapping fragments, either the geometry needs to be ordered by distance from the camera, or the _depth test_ is enabled to ensure that obscured fragments are not rendered.  The depth test is uses a _depth buffer_ which is an attachment that stores the depth of each rendered fragment, discarding subsequent fragments that are closer to the camera.

The depth test is configured by a new pipeline stage:

```java
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
    private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

    public DepthStencilStageBuilder() {
        enable(false);
        write(true);
        compare(VkCompareOp.LESS_OR_EQUAL);
    }
}
```

In the previous demos the clear value for the colour attachments was hard-coded, with the addition of the depth buffer this functionality will now be properly implemented.

Introducing clear values should have been easy, however there was a nasty surprise when adding the depth-stencil to the demo, with JNA throwing the infamous `Invalid memory access` error.  Eventually we realised that `VkClearValue` and `VkClearColorValue` are in fact __unions__ and not structures.  Presumably the original code with a single clear value only worked by luck because the properties for a colour attachment happen to be the first field in each object, i.e. the `color` and `float32` properties.

Thankfully JNA supports unions out-of-the-box, the generated code was manually modified as unions.  As far as we can tell this is the __only__ instance in the whole Vulkan API that uses unions!

A clear value is defined by the following abstraction:

```java
public sealed interface ClearValue permits ColourClearValue, DepthClearValue {
    /**
     * Populates the given clear value descriptor.
     * @param value Descriptor
     */
    void populate(VkClearValue value);
}
```

The temporary code can now be moved to a new implementation for colour attachments:

```java
record ColourClearValue(Colour col) implements ClearValue {
    @Override
    public void populate(VkClearValue value) {
        value.setType("color");
        value.color.setType("float32");
        value.color.float32 = col.toArray();
    }
}
```

The `setType` method of a JNA union is used to 'select' the relevant properties.  

And similarly a second implementation for the depth-stencil attachment:

```java
record DepthClearValue(Percentile depth) implements ClearValue {
    /**
     * Default clear value for a depth attachment.
     */
    public static final DepthClearValue DEFAULT = new DepthClearValue(Percentile.ONE);

    @Override
    public void populate(VkClearValue value) {
        value.setType("depthStencil");
        value.depthStencil.depth = depth.floatValue();
        value.depthStencil.stencil = 0;
    }
}
```

The clear value now becomes a mutable property of the image view:

```java
public class View extends AbstractVulkanObject {
    private ClearValue clear;

    public Optional<ClearValue> clear() {
        return Optional.ofNullable(clear);
    }

    public View clear(ClearValue clear) {
        this.clear = clear;
        return this;
    }
}
```

The builder for the swapchain class is also refactored to conveniently initialise a clear colour for all swapchain images.

Finally the `begin` method of the frame buffer is updated to populate the clear values at the start of the render-pass:

```java
Collection<ClearValue> clear = attachments
    .stream()
    .map(View::clear)
    .flatMap(Optional::stream)
    .collect(toList());

// Init clear values
info.clearValueCount = clear.size();
info.pClearValues = StructureHelper.pointer(clear, VkClearValue::new, ClearValue::populate);
```

### Integration

To use the depth test in the demo a new depth-stencil attachment is added to the render-pass configuration:

```java
Attachment colour = ...

Attachment depth = new Attachment.Builder()
    .format(VkFormat.D32_SFLOAT)
    .load(VkAttachmentLoadOp.CLEAR)
    .finalLayout(VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
    .build();

Subpass subpass = new Subpass.Builder()
    .colour(new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
    .depth(new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL))
    ...
```

Notes:

* The format of the depth buffer attachment is temporarily hard-coded to one that is commonly available on most Vulkan implementations.

* The _store_ operation is left as the default (don't care).

* Similarly the _old layout_ property is left as undefined since the previous contents are unused.

Unlike the swapchain images we are required to create and manage the image for the depth buffer attachment:

```java
@Bean
public View depth(Swapchain swapchain, AllocationService allocator) {
    ImageDescriptor descriptor = new ImageDescriptor.Builder()
        .aspect(VkImageAspect.DEPTH)
        .extents(new ImageExtents(swapchain.extents()))
        .format(VkFormat.D32_SFLOAT)
        .build();
        
    ...
}
```

Next the image for the depth attachment is instantiated:

```java
MemoryProperties<VkImageUsage> props = new MemoryProperties.Builder<VkImageUsage>()
    .usage(VkImageUsage.DEPTH_STENCIL_ATTACHMENT)
    .required(VkMemoryProperty.DEVICE_LOCAL)
    .build();

Image image = new Image.Builder()
    .descriptor(descriptor)
    .tiling(VkImageTiling.OPTIMAL)
    .properties(props)
    .build(dev, allocator);
```

And a view of the image configured with a clear value:

```java
new View.Builder(image)
    .clear(DepthClearValue.DEFAULT)
    .build();
```

The depth buffer is then added to each frame buffer along with the colour attachment:

```java
FrameBuffer.create(pass, extents, List.of(view, depth))
```

Finally the depth test is enabled in the pipeline configuration:

```java
.depth()
    .enable(true)
    .build()
```

Notes:

* Depth buffer images do not need to be programatically transitioned as Vulkan automatically manages the image during the render pass.

* The same depth buffer can safely be used in each frame since only a single sub-pass will be executing at any one time in the current render loop.

With the depth buffer enabled we should finally be able to see the chalet model:

![Chalet Model](chalet.png)

Ta-da!

---

## Improvements

### Format Selector

Rather than hard-coding the format of the depth buffer the following helper is implemented to select a suitable format from a list of candidates:

```java
public class FormatSelector {
    private final PhysicalDevice dev;
    private final Predicate<VkFormatProperties> filter;

    public Optional<VkFormat> select(List<VkFormat> candidates) {
        return candidates.stream().filter(this::matches).findAny();
    }
}
```

The `matches` method looks up the format properties from the device and applies the filter test:

```java
private boolean matches(VkFormat format) {
    VkFormatProperties props = dev.properties(format);
    return filter.test(props);
}
```

The following factory method creates a format predicate for optimal or linear tiling features:

```java
public static Predicate<VkFormatProperties> filter(boolean optimal, Set<VkFormatFeature> features) {
    Mask mask = new Mask(IntegerEnumeration.reduce(features));
    return props -> {
        int bits = optimal ? props.optimalTilingFeatures : props.linearTilingFeatures;
        return mask.matches(bits);
    };
}
```

Where `Mask` is a new utility class for common bit-level operations:

```java
public record Mask(int mask) {
    public boolean matches(int bits) {
        return (mask & bits) == mask;
    }
}
```

A convenience constructor (not shown) is also added to create a selector using the `filter` factory method.

The optimal format can now be selected in the demo when configuring the depth buffer:

```java
@Bean
public View depth(Swapchain swapchain, AllocationService allocator) {
    FormatSelector selector = new FormatSelector(dev.parent(), true, VkFormatFeature.DEPTH_STENCIL_ATTACHMENT);
    VkFormat format = selector.select(VkFormat.D32_SFLOAT, VkFormat.D32_SFLOAT_S8_UINT, VkFormat.D24_UNORM_S8_UINT).orElseThrow();
    ...
}
```

### Swapchain Configuration

We also make some modifications to the configuration of the swapchain to select various properties rather than hard-coding.

First the following helper is added to the surface class to select an image format, falling back to a default format if the requested configuration is not available:

```java
public VkSurfaceFormatKHR format(VkFormat format, VkColorSpaceKHR space, VkSurfaceFormatKHR def) {
    return this
        .formats()
        .stream()
        .filter(f -> f.format == format)
        .filter(f -> f.colorSpace == space)
        .findAny()
        .or(() -> Optional.ofNullable(def))
        .orElseGet(Surface::defaultSurfaceFormat);
}
```

Where the following factory creates the commonly supported default format:

```java
public static VkSurfaceFormatKHR defaultSurfaceFormat() {
    // Create surface format
    var format = new VkSurfaceFormatKHR();
    format.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;

    // Init default swapchain image format
    format.format = new FormatBuilder()
            .components("BGRA")
            .bytes(1)
            .signed(false)
            .type(FormatBuilder.Type.NORM)
            .build();

    return format;
}
```

A second helper is added to select a preferred presentation mode:

```java
public VkPresentModeKHR mode(VkPresentModeKHR... modes) {
    Set<VkPresentModeKHR> available = this.modes();
    return Arrays
            .stream(modes)
            .filter(available::contains)
            .findAny()
            .orElse(DEFAULT_PRESENTATION_MODE);
}
```

The configuration of the swapchain is now more robust:

```java
public Swapchain swapchain(Surface surface, ApplicationConfiguration cfg) {
    // Select presentation mode
    VkPresentModeKHR mode = surface.mode(VkPresentModeKHR.MAILBOX_KHR);

    // Select SRGB surface format
    VkSurfaceFormatKHR format = surface.format(VkFormat.B8G8R8_UNORM, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);

    // Create swapchain
    return new Swapchain.Builder(dev, surface)
        .count(cfg.getFrameCount())
        .clear(cfg.getBackground())
        .format(format)
        .presentation(mode)
        .build();
}
```

Finally the surface property accessors can be cached to minimise API calls:

```java
public Surface cached() {
    return new Surface(handle, dev) {
        private final Supplier<VkSurfaceCapabilitiesKHR> caps = new LazySupplier<>(super::capabilities);
        private final Supplier<List<VkSurfaceFormatKHR>> formats = new LazySupplier<>(super::formats);
        private final Supplier<Set<VkPresentModeKHR>> modes = new LazySupplier<>(super::modes);

        @Override
        public VkSurfaceCapabilitiesKHR capabilities() {
            return caps.get();
        }
        
        ...
    };
}
```

Where `LazySupplier` returns a singleton instance using a relatively cheap thread-safe implementation.

### Global Flip

By default the Vulkan Y axis points __down__ which is the opposite direction to OpenGL (and just about every other 3D library).

However we came across a global solution that handily flips the axis by specifying a 'negative' viewport rectangle: [Flipping the Vulkan viewport](https://www.saschawillems.de/blog/2019/03/29/flipping-the-vulkan-viewport/)

The implementation is relatively trivial - we add a _flip_ setting to the `ViewportStageBuilder` which is applied when the viewport descriptor is populated:

```java
private void populate(Viewport viewport, VkViewport info) {
    Rectangle rect = viewport.rect;
    info.x = rect.x();
    info.width = rect.width();
    if(flip) {
        info.y = rect.y() + rect.height();
        info.height = -rect.height();
    }
    else {
        info.y = rect.y();
        info.height = rect.height();
    }
    ...
}
```

Notes:

* This solution is only supported in Vulkan version 1.1.x or above.

* The Y coordinate of the viewport origin is also shifted to the bottom of the viewport.

However we eventually decided against using the global flip as the default setting for a couple of reasons:

1. This is a breaking change for existing code since flipping the Y axis also essentially flips the triangle winding order.

2. The implementation actually flips the _coordinate space_ rather than simply flipping the frame buffer.  This means that __all__ code still needs to be aware that the global flip is enabled (e.g. for the camera model and view transform later on) which seems to defeat the object.

Therefore the _flip_ setting is disabled by default and we just accept that the Y direction is __down__ from this point forwards.

---

## Summary

In this chapter we implemented:

- The depth-stencil pipeline stage.

- A mechanism to clear colour and depth-stencil attachments.

- A builder for draw commands.

- Texture coordinate inversion for an OBJ model.

