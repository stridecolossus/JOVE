---
title: Depth Buffers
---

## Overview

In this fairly short chapter we will render the OBJ model constructed previously and resolve various visual problems that arise.

This will include the introduction of a _depth test_ requiring the following new functionality:

* Implementation of the depth-stencil pipeline stage.

* Attachment clear values.

---

## Model Render

### Configuration

We start a new `ModelDemo` project based on the previous rotating cube demo, minus the rotation animation.

Next we replace the previous VBO configuration with a new class that loads the buffered model:

```java
@Configuration
public class ModelConfiguration {
    @Autowired private LogicalDevice dev;
    @Autowired private AllocationService allocator;
    @Autowired private Pool graphics;

    @Bean
    public static Model model(DataSource src) {
        final var loader = ResourceLoader.of(src, new ModelLoader());
        return loader.apply("chalet.model");
    }
}
```

We then create the VBO and index buffer objects for the model:

```java
@Bean
public VulkanBuffer vbo(Model model) {
    return buffer(model.vertices(), VkBufferUsage.VERTEX_BUFFER);
}

@Bean
public VulkanBuffer index(Model model) {
    return buffer(model.index().get(), VkBufferUsage.INDEX_BUFFER);
}
```

Which both delegate to the following helper:

```java
private VulkanBuffer buffer(Bufferable data, VkBufferUsage usage) {
    // Create staging buffer
    final VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, data);

    // Init buffer memory properties
    final MemoryProperties<VkBufferUsage> props = new MemoryProperties.Builder<VkBufferUsage>()
            .usage(VkBufferUsage.TRANSFER_DST)
            .usage(usage)
            .required(VkMemoryProperty.DEVICE_LOCAL)
            .build();

    // Create buffer
    final VulkanBuffer buffer = VulkanBuffer.create(dev, allocator, staging.length(), props);

    // Copy staging to buffer
    staging.copy(buffer).submitAndWait(graphics);

    // Release staging
    staging.close();

    return buffer;
}
```

In the render configuration we inject and bind the index buffer:

```java
.add(index.bindIndexBuffer())
```

To initialise the projection matrix (now that we have removed the animation) we add the following temporary code:

```java
@Component
static class ApplicationLoop implements CommandLineRunner {
    @Autowired
    public void init(Matrix matrix, VulkanBuffer uniform) {
        uniform.load(matrix);
    }
}
```

The `@Autowired` annotation is used to instruct the container to invoke the method once the component has been instantiated.

Finally we need to update the drawing command for the indexed model, we take the opportunity to implement a convenience builder on the `DrawCommand` class:

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

We add factory methods implemented using the builder and add a further convenience method to create a drawing command for a given model:

```java
static DrawCommand draw(int count) {
    return new Builder().count(count).build();
}

static DrawCommand indexed(int count) {
    return new Builder().indexed(0).count(count).build();
}

static DrawCommand of(Model model) {
    final int count = model.header().count();
    if(model.isIndexed()) {
        return indexed(count);
    }
    else {
        return draw(count);
    }
}
```

We can now replace the hard-coded command in the render sequence:

```java
Command draw = DrawCommand.of(model);
```

Finally, the previous view transform means we are looking at the model from above - we add a temporary static rotation to get a better viewing angle:

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

We run the demo and see what we get - which is a bit of a mess!

![Broken Chalet Model](mess.png)

There are a couple of issues here:

* The texture looks upside down - the grass is on the roof and vice-versa.

* We are seeing bits of the model that should be obscured - fragments are being rendered arbitrarily overlapping each other.

### Texture Coordinate Invert

The upside-down texture is due to the fact that OBJ texture coordinates (and OpenGL) assume an origin at the bottom-left of the image, whereas for Vulkan the 'start' of the texture image is the top-left corner.

We _could_ fiddle the texture coordinates in the shader or simply flip the texture image, but neither of these resolves the actual root problem (and inverting the image would only make loading slower).  Instead we will flip the vertical coordinate _once_ when the model is loaded.

We introduce a sort of adapter method that flips the vertical component of the texture coordinates before it is instantiated:

```java
private static Coordinate2D flip(float[] array) {
    assert array.length == 2;
    array[1] = -array[1];
    return new Coordinate2D(array[0], array[1]);
}
```

This is used in the parser configuration of the OBJ loader:

```java
add("vt", new VertexComponentParser<>(2, ObjectModelLoader::flip, ObjectModel::coordinate));
```

We assume that this will apply to all OBJ models, we can always make it an optional feature if that assumption turns out to be incorrect.

The model now looks to be textured correctly, in particular the signs on the front of the chalet are the right way round (so we are not rendering the model inside-out for example).

We did head down a blind alley for some time believing that some of the rendering problems were due to errors in culling or the triangle winding order.  This proved to be unfounded but we did fully implement the _rasterizer pipeline stage_ which we introduce at this point for the sake of posterity:

```java
public class RasterizerStageBuilder extends AbstractPipelineBuilder<VkPipelineRasterizationStateCreateInfo> {
    private boolean depthClampEnable;
    private boolean rasterizerDiscardEnable;
    private VkPolygonMode polygonMode = VkPolygonMode.FILL;
    private VkCullMode cullMode = VkCullMode.BACK;
    private VkFrontFace frontFace = VkFrontFace.COUNTER_CLOCKWISE;
    private float lineWidth = 1;
    private boolean depthBiasEnable;
}
```

---

## Depth Buffer

### Pipeline Stage

To resolve the issue of overlapping fragments we either need to order the geometry by distance from the camera or use a _depth test_ to ensure that obscured fragments are not rendered.  A depth test is implemented using a _depth buffer_ which is a frame buffer attachment that stores the depth of each rendered fragment, discarding subsequent fragments that are closer.

To configure the depth test we first introduce a new pipeline stage:

```java
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
    private boolean enable;
    private boolean write = true;
    private boolean bounds;
    private VkCompareOp op = VkCompareOp.LESS;
    
    @Override
    VkPipelineDepthStencilStateCreateInfo get() {
        var info = new VkPipelineDepthStencilStateCreateInfo();
        info.depthTestEnable = VulkanBoolean.of(enable);
        info.depthWriteEnable = VulkanBoolean.of(write);
        info.depthCompareOp = op;
        return info;
    }
}
```

We can now enable the depth test in the pipeline configuration:

```java
.depth()
    .enable(true)
    .build()
```

### Clear Values

In the previous demos we hard-coded a clear value for the colour attachments, with the addition of the depth buffer we will now properly implement this functionality.

We first define the following abstraction for a general clear value:

```java
public interface ClearValue {
    /**
     * Populates the given clear value descriptor.
     * @param value Descriptor
     */
    void populate(VkClearValue value);

    /**
     * @return Expected image aspect for this clear value
     */
    VkImageAspect aspect();
}
```

The temporary code can now be moved to a new implementation for colour attachments:

```java
record ColourClearValue(Colour col) implements ClearValue {
    @Override
    public VkImageAspect aspect() {
        return VkImageAspect.COLOR;
    }

    @Override
    public void populate(VkClearValue value) {
        value.setType("color");
        value.color.setType("float32");
        value.color.float32 = col.toArray();
    }
}
```

For a depth-stencil attachment we add a second implementation:

```java
record DepthClearValue(Percentile depth) implements ClearValue {
    /**
     * Default clear value for a depth attachment.
     */
    public static final ClearValue DEFAULT = new DepthClearValue(Percentile.ONE);

    @Override
    public VkImageAspect aspect() {
        return VkImageAspect.DEPTH;
    }

    @Override
    public void populate(VkClearValue value) {
        value.setType("depthStencil");
        value.depthStencil.depth = depth.floatValue();
        value.depthStencil.stencil = 0;
    }
}
```

Finally we also add an empty constant implementation as the default value:

```java
ClearValue NONE = new ClearValue() {
    @Override
    public VkImageAspect aspect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void populate(VkClearValue value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean equals(Object obj) {
        return obj == this;
    }

    @Override
    public String toString() {
        return "None";
    }
};
```

The clear value now becomes a mutable property of an image view:

```java
public class View extends AbstractVulkanObject {
    private ClearValue clear = ClearValue.NONE;

    public void clear(ClearValue clear) {
        if(clear != ClearValue.NONE) {
            var aspects = image.descriptor().aspects();
            if(!aspects.contains(clear.aspect())) {
                throw new IllegalArgumentException(...);
            }
        }
        this.clear = notNull(clear);
    }
}
```

We modify the image builder accordingly and also refactor the swapchain class to conveniently initialise a clear colour for all swapchain images.

Finally we refactor the `begin` method of the frame buffer to populate the clear values at the start of the render-pass:

```java
// Map attachments to clear values
Collection<ClearValue> clear = attachments
    .stream()
    .map(View::clear)
    .filter(Predicate.not(ClearValue.NONE::equals))
    .collect(toList());

// Init clear values
info.clearValueCount = clear.size();
info.pClearValues = StructureHelper.first(clear, VkClearValue::new, ClearValue::populate);
```

Introducing this functionality should have been easy, however we had a nasty surprise when we introduced the depth-stencil with JNA throwing the infamous `Invalid memory access` error.  Eventually we realised that `VkClearValue` and `VkClearColorValue` are in fact __unions__ and not structures.  Presumably the original code with a single clear value only worked by luck because the JNA union defaulted to using the first field (i.e. the `color` property).

Thankfully JNA supports unions out-of-the-box.  We manually modified the generated code and used the `setType` method of the JNA union class to 'select' the relevant properties.  As far as we can tell this is the __only__ instance in the whole Vulkan API that uses unions!

### Integration

To use the depth test we first add a new depth-stencil attachment to the render-pass configuration:

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

* The format of the depth buffer attachment is hard-coded to one that is commonly available on most Vulkan implementations, we make a note to properly select a format appropriate to the hardware later on.

* The _store_ operation is left as the default (don't care).

* Similarly the _old layout_ property is left as undefined since we do not use the previous contents.

Unlike the swapchain images we are required to create the image for the depth buffer - we start with the image descriptor:

```java
@Bean
public View depth(Swapchain swapchain, AllocationService allocator) {
    ImageDescriptor descriptor = new ImageDescriptor.Builder()
        .aspect(VkImageAspect.DEPTH)
        .extents(new ImageExtents(swapchain.extents()))
        .format(VkFormat.D32_SFLOAT)
        .build();
}
```

The _extents_ of the depth buffer should be same as the swapchain images.

Next we create the image:

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

Finally we create a view of the image configured with the new clear value:

```java
new View.Builder(image)
    .clear(DepthClearValue.DEFAULT)
    .build();
```

The depth buffer is then added to each frame buffer along with the colour attachment:

```java
FrameBuffer.create(pass, extents, List.of(view, depth))
```

Notes:

* Depth buffer images do not need to be programatically transitioned as Vulkan automatically manages the image during the render pass.

* The same depth buffer can safely be used in each frame since only a single sub-pass will be executing at any one time in the current render loop.

With the depth buffer enabled we should finally be able to see the chalet model:

![Chalet Model](chalet.png)

Ta-da!

---

## Summary

In this chapter we implemented:

- The depth-stencil and rasterizer pipeline stages.

- A mechanism to clear colour and depth-stencil attachments.

- A builder for draw commands.

- Texture coordinate inversion for an OBJ model.

