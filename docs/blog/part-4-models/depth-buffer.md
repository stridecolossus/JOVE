---
title: Depth Buffers
---

## Overview

In this chapter we will render the OBJ model and resolve various visual problems that arise including the implementation of a depth buffer attachment.

---

## Integration #1

After cloning the code from the previous demo we first load the buffered model and transfer the vertex and index buffers to the hardware:

```java
// Load model
final var loader = DataSource.loader(src, new ModelLoader());
final Model model = loader.load("chalet.model");

// Load VBO
final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
final VulkanBuffer vbo = loadBuffer(dev, model.vertices(), VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT, copyPool);

// Load index buffer
final VulkanBuffer index = loadBuffer(dev, model.index().get(), VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT, copyPool);
```

The `loadBuffer` helper wraps up the code to copy data to the hardware via a staging buffer.

We also implement factory methods for the draw command:

```java
public interface DrawCommand extends Command {
    static Command draw(int count) {
        return (api, handle) -> api.vkCmdDraw(handle, count, 1, 0, 0);
    }

    static Command indexed(int count) {
        return (api, handle) -> api.vkCmdDrawIndexed(handle, count, 1, 0, 0, 0);
    }
}
```

With a convenience helper to create a command for the model:

```java
static Command of(Model model) {
    if(model.index().isPresent()) {
        return indexed(model.count());
    }
    else {
        return draw(model.count());
    }
}
```

The rendering sequence now looks like this:

```java
final Command draw = DrawCommand.of(model);

...

.begin()
    .add(pass.begin(buffer))
    .add(pipeline.bind())
    .add(vbo.bindVertexBuffer())
    .add(index.bindIndexBuffer())
    .add(descriptor.bind(pipelineLayout))
    .add(draw)
    .add(RenderPass.END_COMMAND)
.end();
```

We strip the code that applied the rotation and see what happens - and what we get is a mess:

- the model is rendered but it looks sort of *inside out*

- the grass is obviously on the roof and vice-versa

- we are seeing bits of the model on top of each other

There are several issues here but the most pressing is the fact that we now need a *depth buffer* so that fragments are not rendered arbitrarily overlapping each other.

---

## Depth Buffer

### Attachment

We first add a second attachment to the render pass for a depth buffer:

```java
final RenderPass pass = new RenderPass.Builder(dev)
   ...
    .attachment()
        .format(VkFormat.VK_FORMAT_D32_SFLOAT)
        .load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
        .finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
        .build()
    .subpass()
        .colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
        .depth(1)
        .build()
    .build();
```

The format of the depth buffer attachment is hard-coded to one that is commonly available on most Vulkan implementations, but we make a note to come back and add code to properly select a format appropriate to the hardware.

Next we add a new method to the sub-pass builder to register the depth-buffer attachment:

```java
public SubpassBuilder depth(int index) {
    info.pDepthStencilAttachment = reference(index, VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
    return this;
}
```

We factor out the code that is common to both types of attachment and refactor accordingly:

```java
private VkAttachmentReference reference(int index, VkImageLayout layout) {
    Check.zeroOrMore(index);
    Check.notNull(layout);
    if(index >= attachments.size()) throw new IllegalArgumentException(...);
    if(layout == VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED) throw new IllegalArgumentException(...);

    // Create reference
    final var ref = new VkAttachmentReference();
    ref.attachment = index;
    ref.layout = layout;
    return ref;
}
```

### Pipeline Stage

Up until now we have not needed to specify the depth-stencil pipeline stage and the relevant field in the create descriptor was set to `null`.

We implement a new nested builder to configure this stage:

```java
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
    private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

    public DepthStencilStageBuilder() {
        enable(false);
        write(true);
        compare(VkCompareOp.VK_COMPARE_OP_LESS);
    }

    public DepthStencilStageBuilder enable(boolean enabled) {
        info.depthTestEnable = VulkanBoolean.of(enabled);
        return this;
    }

    public DepthStencilStageBuilder write(boolean write) {
        info.depthWriteEnable = VulkanBoolean.of(write);
        return this;
    }

    public DepthStencilStageBuilder compare(VkCompareOp op) {
        info.depthCompareOp = notNull(op);
        return this;
    }
    
    ...

    @Override
    protected VkPipelineDepthStencilStateCreateInfo result() {
        return info;
    }
}
```

---

## Clearing Attachments

### Clear Values

The final change we need to make for the depth buffer is to specify how it is cleared before each render pass.

We previously hard-coded a clear value for the single colour attachment in the `begin` method of the render pass.
We will refactor this code to support an arbitrary number of both colour and depth attachments.

We start with a new abstraction for a clear value:

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
    VkImageAspectFlag aspect();
}
```

And add a constant for an empty clear value (the default for attachments that are not cleared):

```java
ClearValue NONE = new ClearValue() {
    @Override
    public VkImageAspectFlag aspect() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void populate(VkClearValue value) {
    }
};
```

The clear value can now be a property of the image view (or attachment):

```java
public class View extends AbstractVulkanObject {
    ...
    private ClearValue clear = ClearValue.NONE;

    public ClearValue clear() {
        return clear;
    }
}
```

This is a mutable value that the setter validates against the attachment:

```java
public void clear(ClearValue clear) {
    if(clear != ClearValue.NONE) {
        final var aspects = image.descriptor().aspects();
        if(!aspects.contains(clear.aspect())) {
            throw new IllegalArgumentException(...);
        }
    }
    this.clear = notNull(clear);
}
```

We refactor the view builder accordingly which nicely centralises the clear values. 
In particular we can also refactor the swapchain code to conveniently initialise the clear colour when we configure the views:

```java
final SwapChain chain = new SwapChain.Builder(surface)
    .count(2)
    .format(format)
    .space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
    .clear(new Colour(0.3f, 0.3f, 0.3f, 1))
    .build();
```

Finally we refactor the `begin` method of the render pass and remove the hard-coded clear colour:

```java
final Collection<ClearValue> values = buffer.attachments().stream().map(View::clear).collect(toList());
info.clearValueCount = values.size();
info.pClearValues = StructureCollector.toPointer(VkClearValue::new, values, ClearValue::populate);
```

### Implementation

The next step is to implement clear values for both types of attachment.

Introducing this functionality should have relatively straight-forward but we had a nasty surprise when we introduced the second attachment with JNA throwing the infamous `Invalid memory access` error.  Eventually we realised that `VkClearValue` and `VkClearColorValue` are in fact **unions** and not structures!  Presumably the original code (with a clear value for the single attachment) only worked because JNA ignored the 'extra' data in the code-generated structures.

Thankfully JNA supports unions out-of-the-box so we manually modified the generated code and used `setType` to 'select' the relevant properties. 
As far as we can tell this is the **only** instance in the whole Vulkan API that uses unions!

The clear value for a colour attachment is implemented as follows:

```java
class ColourClearValue implements ClearValue {
    private final Colour col;

    public ColourClearValue(Colour col) {
        this.col = notNull(col);
    }

    @Override
    public VkImageAspectFlag aspect() {
        return VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT;
    }

    @Override
    public void populate(VkClearValue value) {
        value.setType("color");
        value.color.setType("float32");
        value.color.float32 = col.toArray();
    }
}
```

Similarly for a depth attachment:

```java
class DepthClearValue implements ClearValue {
    private final float depth;

    public DepthClearValue(Percentile depth) {
        this.depth = depth.floatValue();
    }

    @Override
    public VkImageAspectFlag aspect() {
        return VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT;
    }

    @Override
    public void populate(VkClearValue value) {
        value.setType("depthStencil");
        value.depthStencil.depth = depth;
        value.depthStencil.stencil = 0;
    }
}
```

We also add convenience clear value constants:

```java
ClearValue COLOUR = new ColourClearValue(Colour.BLACK);
ClearValue DEPTH = new DepthClearValue(Percentile.ONE);
```

---

## Integration #2

### Depth Buffer

Unlike the swapchain images we need to create the depth buffer image ourselves:

```java
final Image depthImage = new Image.Builder(dev)
    .aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
    .extents(extents)
    .format(VkFormat.VK_FORMAT_D32_SFLOAT)
    .tiling(VkImageTiling.VK_IMAGE_TILING_OPTIMAL)
    .usage(VkImageUsageFlag.VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
    .build();

final View depth = new View.Builder(dev)
    .image(depthImage)
    .subresource()
        .aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)
        .build()
    .build();
```

Notes:

- The extents of the image should be same as the swapchain.

- The format is again hard-coded for the moment.

- The _store_ operation is set to ignore since we don't use the buffer after rendering.

- We leave the _old layout_ as undefined since we don't care about the previous contents.

- We do not need to transition the depth buffer image as this is handled by Vulkan during the render pass.

The depth buffer view is then added to each frame buffer object we create. 

> The same depth buffer can safely be used in each frame because only a single sub-pass will be running at any one time in our bodged render loop.

Finally we modify the pipeline configuration to include the depth test:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .depth()
        .enable(true)
        .build()
    ...
    .build();
```

When we run our demo after this painful refactoring exercise things look somewhat better, the geometry is no longer overlapping thanks to the depth buffer.

However we still need to solve the other problems.

### Rasterizer Pipeline Stage

The inside-out problem is due to the fact that the OBJ model has a right-handed coordinate system, which results in triangles with the opposite winding order to the default.

We take a small detour to fully implement the builder for the rasterizer pipeline stage (that configures the winding order):

```java
public class RasterizerStageBuilder extends AbstractPipelineBuilder<VkPipelineRasterizationStateCreateInfo> {
    private final VkPipelineRasterizationStateCreateInfo info = new VkPipelineRasterizationStateCreateInfo();

    public RasterizerStageBuilder() {
        depthClamp(false);
        discard(false);
        polygon(VkPolygonMode.VK_POLYGON_MODE_FILL);
        cull(VkCullModeFlag.VK_CULL_MODE_BACK_BIT);
        winding(VkFrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE);
        lineWidth(1);
    }
    
    ...

    @Override
    protected VkPipelineRasterizationStateCreateInfo result() {
        return info;
    }
}
```

Next we add a new `winding` property to the model class which is initialised to _clockwise_ in the OBJ loader.

Finally we apply the winding order when configuring the pipeline:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .rasterizer()
        .winding(model.winding())
        .build()
    ...
```

Alternatively we could have configured the `cullMode` of the rasterizer to cull front-facing polygons - either works.

### Inverting Texture Coordinates

For the problem of the upside-down texture coordinates we *could* simply flip the texture image or fiddle the texture coordinates in the shader - but neither of these solves the actual root problem (and inverting the image would only make loading slower).  

Instead we add a further custom list implementation to the OBJ model to flip the texture coordinates:

```java
public static class ObjectModel {
    private static class FlipTextureComponentList extends VertexComponentList<Coordinate2D> {
        private boolean flip;

        @Override
        public boolean add(Coordinate2D coords) {
            if(flip) {
                return super.add(new Coordinate2D(coords.u, -coords.v));
            }
            else {
                return super.add(coords);
            }
        }
    }

    private final FlipTextureComponentList coords = new FlipTextureComponentList();

    public void setFlipTextureCoordinates(boolean flip) {
        coords.flip = flip;
    }
}
```

### Conclusion

We are also viewing the model from above so we add a temporary rotation so we see it from the side and finally we get the following:

![Chalet Model](chalet.png)

Ta-da!

---

## Summary

In this chapter we implemented:

- A depth buffer attachment.

- Clear Values for colour and depth attachments.

- And the depth-stencil and rasterizer pipeline stages.
