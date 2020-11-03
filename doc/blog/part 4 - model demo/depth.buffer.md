# Depth Buffers

## Integration #1

After cloning the code from the previous demo we first load the buffered model and transfer the vertex and index buffers to the hardware:

```java
// Load model
final ModelLoader loader = new ModelLoader();
final Model model = loader.load(new FileInputStream(...));

// Load VBO
final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
final VertexBuffer vbo = loadBuffer(dev, model.vertices(), copyPool);

// Load index buffer
final VertexBuffer index = loadBuffer(dev, model.index().get(), copyPool);
```

Next we need to add a new command to `VertexBuffer` to bind an index buffer:

```java
public Command bindIndexBuffer() {
	return (api, buffer) -> api.vkCmdBindIndexBuffer(buffer, this.handle(), 0, VkIndexType.VK_INDEX_TYPE_UINT32);
}
```

And we rename the existing bind() method to bindVertexBuffer() to differentiate the commands.

We also get around to renaming the class to `VulkanBuffer` since it is not really just for vertex data.

Finally we encapsulate the drawing command:

```java
public interface DrawCommand extends Command {
    /**
     * Creates a drawing command.
     * @param model Model to draw
     * @return Drawing command
     */
    static DrawCommand of(Model model) {
        if(model.index().isPresent()) {
            return (api, handle) -> api.vkCmdDrawIndexed(handle, model.count(), 1, 0, 0, 0);
        }
        else {
            return (api, handle) -> api.vkCmdDraw(handle, model.count(), 1, 0, 0);
        }
    }
    // TODO - instancing, offset, etc
}
```

The rendering sequence now looks like this:

```java
.begin()
	.add(pass.begin(buffer))
	.add(pipeline.bind())
	.add(vbo.bindVertexBuffer())
	.add(index.bindIndexBuffer())
	.add(descriptor.bind(pipelineLayout))
	.add(DrawCommand.of(model))
	.add(RenderPass.END_COMMAND)
.end();
```

We strip the code that applied the rotation and see what happens - and what we get is a mess:

- the model is rendered but it looks sort of *inside out*
- the grass is obviously on the roof and vice-versa
- we are seeing bits of the model on top of each other

There are several issues here but the most pressing is the fact that we now need a *depth buffer* so that fragments are not rendered arbitrarily overlapping each other.

## Adding a Depth Buffer

### Depth Buffer Attachment

We first add a second attachment to the render pass for the depth buffer:

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

Next we add a new method to the sub-pass builder to register a depth-buffer attachment:

```java
public SubpassBuilder depth(int index) {
	info.pDepthStencilAttachment = reference(index, VkImageLayout.VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
	return this;
}
```

We factor out the code that is common to both types of attachment we have implemented so far and refactor accordingly:

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

### Depth-Stencil Pipeline Stage

Up until now we have not needed to specify the optional depth-stencil pipeline stage and the relevant field in the create descriptor was set to `null`.

We implement a new nested builder to configure this stage (which turns out to be fairly trivial):

```java
public class DepthStencilStageBuilder extends AbstractPipelineBuilder<VkPipelineDepthStencilStateCreateInfo> {
	private final VkPipelineDepthStencilStateCreateInfo info = new VkPipelineDepthStencilStateCreateInfo();

	public DepthStencilStageBuilder() {
		enable(false);
		write(true);
		compare(VkCompareOp.VK_COMPARE_OP_LESS);

		// TODO
		info.depthBoundsTestEnable = VulkanBoolean.FALSE;
		info.minDepthBounds = 0;
		info.maxDepthBounds = 1;

		// TODO - stencil
		info.stencilTestEnable = VulkanBoolean.FALSE;
	}

	/**
	 * Sets whether depth-testing is enabled (default is {@code false}).
	 * @param enabled Whether depth-test is enabled
	 */
	public DepthStencilStageBuilder enable(boolean enabled) {
		info.depthTestEnable = VulkanBoolean.of(enabled);
		return this;
	}

	/**
	 * Sets whether to write to the depth buffer (default is {@code true}).
	 * @param write Whether to write to the depth buffer
	 */
	public DepthStencilStageBuilder write(boolean write) {
		info.depthWriteEnable = VulkanBoolean.of(write);
		return this;
	}

	/**
	 * Sets the depth-test comparison function (default is {@link VkCompareOp#VK_COMPARE_OP_LESS}).
	 * @param op Depth-test function
	 */
	public DepthStencilStageBuilder compare(VkCompareOp op) {
		info.depthCompareOp = notNull(op);
		return this;
	}

	@Override
	protected VkPipelineDepthStencilStateCreateInfo result() {
		return info;
	}
}
```

Finally we modify the specification of the pipeline to include the depth test:

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
	...
	.depth()
		.enable(true)
		.build()
	...
	.build();
```

### Creating the Depth Buffer

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

- The extents of the image should be same as the swapchain images.
- The format is again hard-coded for the moment.
- The store operation is set to ignore since we don't use the buffer after rendering.
- We leave the old layout as undefined since we don't care about the previous contents.
- We do not need to transition the depth buffer image as this is handled by Vulkan during the render pass.

The depth buffer view is then added to each frame buffer object we create.
The same depth buffer can safely be used in each frame because only a single sub-pass will be running at any one time in our bodged render loop.

## Clearing the Attachments

The final step for the depth buffer is to specify how it is cleared before each render pass.

Prior to this stage we had hard-coded a clear value for a single colour attachment in the command for starting the render pass.
We need to make this more general so we can support an arbitrary number of colour and depth attachments.

This involves the following:
1. factoring out the code that actually populates a clear value.
2. make the clear value a property of the attachment view (rather than being hard-coded in the render command).
3. re-factor the render command accordingly.

### Clear Value

Firstly we implement a new domain class to represent a clear value:

```java
public abstract class ClearValue {
	private final VkImageAspectFlag aspect;
	private final Object arg;

	/**
	 * Constructor.
	 * @param aspect		Expected image aspect
	 * @param arg			Clear argument
	 */
	private ClearValue(VkImageAspectFlag aspect, Object arg) {
		this.aspect = notNull(aspect);
		this.arg = notNull(arg);
	}

	/**
	 * @return Expected image aspect
	 */
	public VkImageAspectFlag aspect() {
		return aspect;
	}

	/**
	 * @return Population function
	 */
	public abstract void populate(VkClearValue value);
}
```

This is a skeleton implementation where the populate() method fills the relevant field(s) in the `VkClearValue` array when we create the render command.
The aspect() method allows us to check that we are applying the correct type of clear value for a given attachment.

We add factory methods to create a clear value for the two cases - colour attachments:

```java
/**
 * Creates a clear value for a colour attachment.
 * @param col Colour
 * @return New colour attachment clear value
 */
public static ClearValue of(Colour col) {
	return new ClearValue(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT, col) {
		@Override
		public void populate(VkClearValue value) {
			value.setType("color");
			value.color.setType("float32");
			value.color.float32 = col.toArray();
		}
	};
}
```

and the depth attachment:

```java
/**
 * Creates a clear value for a depth buffer attachment.
 * @param depth Depth value 0..1
 * @return New depth attachment clear value
 * @throws IllegalArgumentException if the depth is not a valid 0..1 value
 */
public static ClearValue depth(float depth) {
	Check.isPercentile(depth);
	return new ClearValue(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT, depth) {
		@Override
		public void populate(VkClearValue value) {
			value.setType("depthStencil");
			value.depthStencil.depth = depth;
			value.depthStencil.stencil = 0;
		}
	};
}
```

Introducing this new functionality should have been easy but we had a lot of head-scratching when we first tested this code with JNA throwing the infamous "Invalid memory access" error.  Eventually we realised that we are actually dealing with **unions** and not structures here!  Presumably our original code only worked because we were dealing with a single attachment and Vulkan simply ignored the 'extra' data of the structures.  

We manually modified the relevant Vulkan types to unions and used the JNA setType() method to 'select' the relevant property.  As far as we can tell this is the only instance in the whole Vulkan API that uses a union!

Finally we add default constants for both cases and a static helper that determines the default clear value for a given attachment:

```java
public static final ClearValue COLOUR = of(Colour.BLACK);
public static final ClearValue DEPTH = depth(1);

/**
 * Determines the default clear value for the given image aspects.
 * @param aspects Image aspects
 * @return Default clear value or {@code null} if not applicable
 */
public static ClearValue of(Set<VkImageAspectFlag> aspects) {
	if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)) {
		return COLOUR;
	}
	else
	if(aspects.contains(VkImageAspectFlag.VK_IMAGE_ASPECT_DEPTH_BIT)) {
		return DEPTH;
	}
	else {
		return null;
	}
}
```

### Attachment Clear Value

The clear value for a given attachment is now a property of the image view:

```java
public class View {
	private ClearValue clear;
	
	public ClearValue clear() {
		return clear;
	}
	
	/**
	 * Sets the clear value for this attachment.
	 * @param clear Clear value
	 * @throws IllegalArgumentException if the clear value is incompatible with this view
	 */
	public void clear(ClearValue clear) {
		if(!image.descriptor().aspects().contains(clear.aspect())) {
			throw new IllegalArgumentException(...)
		}
		this.clear = notNull(clear);
	}
}
```

This allows us to set the clear value for an attachment in the view builder (or initialise it to an appropriate default):

```java
private ClearValue clear;

public View build() {
	...

	// Create image view
	final View view = new View(handle.getValue(), image, dev);

	// Init clear value
	if(clear == null) {
		clear = ClearValue.of(image.descriptor().aspects());
	}
	view.clear = clear;

	return view;
}
```

In particular we can now refactor the swapchain code to initialise the clear colour when we construct the views which is much more convenient and centralised:

```java
final SwapChain chain = new SwapChain.Builder(surface)
	.count(2)
	.format(format)
	.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
	.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
	.build();
```

For our demo we leave the depth buffer to use the default clear value.

### Populating the Clear Values

Finally we refactor the command factory for the render pass to use the above and remove the temporary code we used to clear the colour attachment:

```java
public Command begin(FrameBuffer buffer, Rectangle extent) {
    // Create descriptor
    final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
    info.renderPass = this.handle();
    info.framebuffer = buffer.handle();
    info.renderArea = buffer.extents().toRect2D();

    // Map attachments to clear values
    final Collection<ClearValue> values = buffer.attachments().stream().map(View::clear).collect(toList());

    // Init clear values
    info.clearValueCount = values.size();
    info.pClearValues = VulkanStructure.populate(VkClearValue::new, values, ClearValue::populate);

    // Create command
    return (lib, handle) -> lib.vkCmdBeginRenderPass(handle, info, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE);
}
```

## Integration #2

When we run our demo after this painful refactoring exercise things look somewhat better, the geometry is no longer overlapping thanks to the depth buffer.

However we still need to solve the other problems.

### Inverting Texture Coordinates

For the problem of the upside-down texture coordinates we *could* simply flip the texture image or fiddle the texture coordinates in the shader - but neither of these solves the actual root problem (and inverting the image would make loading slower).  

Therefore we add a new property to the transient OBJ model that flips the coordinates in the Y direction *once* during loading:

```java
public static class ObjectModel {
	private boolean flip = true;

	...

	/**
	 * Sets whether to vertically flip texture coordinates (default is {@code true}).
	 * @param flip Whether to vertically flip coordinates
	 */
	public void flip(boolean flip) {
		this.flip = flip;
	}

	protected void coord(Coordinate2D coords) {
		if(flip) {
			this.coords.add(new Coordinate2D(coords.u, -coords.v));
		}
		else {
			this.coords.add(coords);
		}
	}
}
```

### Rasterizer Pipeline Stage

The inside-out problem is due to the fact that the triangles in the OBJ model are opposite to the default winding order.

We take a small detour to fully implement the builder for the rasterizer pipeline stage (which again is pretty simple):

```java
public class RasterizerStageBuilder extends AbstractPipelineBuilder<VkPipelineRasterizationStateCreateInfo> {
    private boolean depthClampEnable;
    private boolean rasterizerDiscardEnable;
    private VkPolygonMode polygonMode = VkPolygonMode.VK_POLYGON_MODE_FILL;
    private VkCullModeFlag cullMode = VkCullModeFlag.VK_CULL_MODE_BACK_BIT;
    private VkFrontFace frontFace = VkFrontFace.VK_FRONT_FACE_COUNTER_CLOCKWISE;
    private boolean depthBiasEnable;
    private float depthBiasConstantFactor;
    private float depthBiasClamp;
    private float depthBiasSlopeFactor;
    private float lineWidth = 1;

    @Override
    protected VkPipelineRasterizationStateCreateInfo result() {
        final var info = new VkPipelineRasterizationStateCreateInfo();
        ...
        return info;
    }
}
```

And we swap the face-culling property (alternatively we could swap the winding order, either works):

```java
final Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .rasterizer()
        .cullMode(VkCullModeFlag.VK_CULL_MODE_FRONT_BIT)
        .build()
    ...
```

### Conclusion

We are also viewing the model from above so we add a temporary rotation so we see it from the side and finally we get the following:

PICTURE

Ta-da!
