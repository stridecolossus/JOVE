---
title: Depth Buffers
---

## Overview

In this chapter we will render the OBJ model constructed previously and resolve various visual problems that arise.

This will include the introduction of a _depth test_ requiring the following new functionality:

* Implementation of the depth-stencil pipeline stage.

* Attachment clear values.

We will also implement several improvements to the existing code including the introduction of a _camera_ model.

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
        var loader = new ResourceLoaderAdapter<>(src, new ModelLoader());
        return loader.load("chalet.model");
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

We provide convenience factory methods to create simple draw commands:

```java
static DrawCommand draw(int count) {
    return new Builder().count(count).build();
}

static DrawCommand indexed(int count) {
    return new Builder().indexed(0).count(count).build();
}
```

Finally we add a further helper to create a draw command for a given model:

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
    return new Coordinate2D(array[0], -array[1]);
}
```

And modify the texture coordinate parser in the OBJ loader:

```java
add("vt", new VertexComponentParser<>(2, ObjectModelLoader::flip, ObjectModel::coordinate));
```

We assume that this will apply to all OBJ models, we can always make it an optional feature if that assumption turns out to be incorrect.

The model now looks to be textured correctly, in particular the signs on the front of the chalet are the right way round (so we are not rendering the model inside-out for example).

---

## Depth Buffer

### Pipeline Stage

To resolve the issue of overlapping fragments we either need to order the geometry by distance from the camera or use a _depth test_ to ensure that obscured fragments are not rendered.  A depth test is implemented using a _depth buffer_ which is a frame buffer attachment that stores the depth of each rendered fragment, discarding subsequent fragments that are closer to the camera.

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

We can now enable the depth test in the pipeline configuration:

```java
.depth()
    .enable(true)
    .build()
```

### Clear Values

In the previous demos we hard-coded a clear value for the colour attachments, with the addition of the depth buffer we will now properly implement this functionality.

Introducing clear values should have been easy, however we had a nasty surprise when we added the depth-stencil to the demo, with JNA throwing the infamous `Invalid memory access` error.  Eventually we realised that `VkClearValue` and `VkClearColorValue` are in fact __unions__ and not structures.  Presumably the original code with a single clear value only worked by luck because the properties for a colour attachment happen to be the first field in each object, i.e. the `color` and `float32` properties.

Thankfully JNA supports unions out-of-the-box.  We manually modified the generated code and used the `setType` method of the JNA union class to 'select' the relevant properties.  As far as we can tell this is the __only__ instance in the whole Vulkan API that uses unions!

A clear value is defined by the following abstraction:

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

Finally we also create an empty implementation for the default value:

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

The clear value now becomes a mutable property of the image view:

```java
public class View extends AbstractVulkanObject {
    private ClearValue clear = ClearValue.NONE;

    public View clear(ClearValue clear) {
        validate(clear);
        this.clear = notNull(clear);
        return this;
    }
}
```

Where `validate` checks that the `aspect` of the clear value matches the image view.

We also refactor the builder for the swapchain class to conveniently initialise a clear colour for all swapchain images.

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

* The format of the depth buffer attachment is temporarily hard-coded to one that is commonly available on most Vulkan implementations.

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

* We add a convenience constant for the default `DEPTH` clear value for a depth-stencil attachment.

With the depth buffer enabled we should finally be able to see the chalet model:

![Chalet Model](chalet.png)

Ta-da!

---

## Improvements

### Format Selector

Rather than hard-coding the format of the depth buffer image and attachment we create a helper class that selects the most suitable format from a list of candidates:

```java
public class FormatSelector {
    private final Function<VkFormat, VkFormatProperties> mapper;
    private final Predicate<VkFormatProperties> predicate;

    public Optional<VkFormat> select(List<VkFormat> candidates) {
        return candidates
            .stream()
            .filter(this::matches)
            .findAny();
    }

    private boolean matches(VkFormat format) {
        VkFormatProperties props = mapper.apply(format);
        return predicate.test(props);
    }
}
```

The _mapper_ retrieves the format properties for a given Vulkan format which will use a new accessor on the physical device:

```java
public VkFormatProperties properties(VkFormat format) {
    var props = new VkFormatProperties();
    instance.library().vkGetPhysicalDeviceFormatProperties(this, format, props);
    return props;
}
```

We also a helper factory method to create a filter that selects a format based on the _optimal_ or _linear_ tiling features:

```java
public static Predicate<VkFormatProperties> feature(Set<VkFormatFeature> features, boolean optimal) {
    int mask = IntegerEnumeration.mask(features);
    return props -> {
        int actual = optimal ? props.optimalTilingFeatures : props.linearTilingFeatures;
        return MathsUtil.isMask(mask, actual);
    };
}
```

In the configuration class we can now select the best depth buffer format:

```java
@Bean
public View depth(Swapchain swapchain, AllocationService allocator) {
    // Select depth format
    var filter = FormatSelector.feature(Set.of(VkFormatFeature.DEPTH_STENCIL_ATTACHMENT), true);
    PhysicalDevice parent = dev.parent();
    FormatSelector selector = new FormatSelector(parent::properties, filter);
    VkFormat format = selector.select(List.of(VkFormat.D32_SFLOAT, VkFormat.D32_SFLOAT_S8_UINT, VkFormat.D24_UNORM_S8_UINT)).orElseThrow();
    ...
}
```

Later on we may want to encapsulate this code into another helper method or class and possibly retrieve the candidates from a configuration file.

### Swapchain Configuration

We also make some modifications to the configuration of the swapchain to select various properties rather than hard-coding.

First the following helper is added to the surface properties class to select an image format, falling back to an arbitrary format if the optimal configuration is not available:

```java
public VkSurfaceFormatKHR format(VkFormat format, VkColorSpaceKHR space) {
    List<VkSurfaceFormatKHR> formats = this.formats();
    return formats
        .stream()
        .filter(f -> f.format == format)
        .filter(f -> f.colorSpace == space)
        .findAny()
        .orElse(formats.get(0));
}
```

Next we implement another helper on the swapchain class that selects a preferred presentation mode or falls back to the default:

```java
public static VkPresentModeKHR mode(Surface.Properties props, VkPresentModeKHR... modes) {
    Set<VkPresentModeKHR> available = props.modes();
    return Arrays
        .stream(modes)
        .filter(available::contains)
        .findAny()
        .orElse(DEFAULT_PRESENTATION_MODE);
}
```

The configuration of the swapchain is now more robust:

```java
public Swapchain swapchain(Surface.Properties props, ApplicationConfiguration cfg) {
    // Select presentation mode
    VkPresentModeKHR mode = Swapchain.mode(props, VkPresentModeKHR.MAILBOX_KHR);

    // Select SRGB surface format
    VkSurfaceFormatKHR format = props.format(VkFormat.B8G8R8_UNORM, VkColorSpaceKHR.SRGB_NONLINEAR_KHR);

    // Create swapchain
    return new Swapchain.Builder(dev, props)
        .count(cfg.getFrameCount())
        .clear(cfg.getBackground())
        .format(format)
        .presentation(mode)
        .build();
}
```

Notes:

* The `swapchain` bean now accepts the surface properties rather than the surface itself (since we now also use the properties in the selection code).

* The presentation modes and surface formats are lazily retrieved in the surface class to minimise API calls.

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

* The _flip_ setting is __on__ by default and can be assumed to be active from this point onwards.

However note this is a breaking change since flipping the Y axis also essentially flips the triangle winding order.  This entails the following modifications to existing code:

* The Y component of the vertex positions in the cube builder.

* The model rotation applied to the chalet model.

### Vector

Next we add some new functionality to the `Vector` class that will be used in the camera class below.

A vector has a _magnitude_ (or length) which is calculated using the _Pythagorean_ theorem as the square-root of the _hypotenuse_ of the vector.  Although square-root operations are generally delegated to the hardware and are therefore less expensive than in the past, we prefer to avoid having to perform roots where possible.  Additionally many algorithms work irrespective of whether the distance is squared or not.

Therefore we treat the magnitude as the __squared__ length of the vector (which is highlighted in the documentation):

```java
/**
 * @return Magnitude (or length) <b>squared</b> of this vector
 */
public float magnitude() {
    return x * x + y * y + z * z;
}
```

Many operations assume that a vector has been _normalized_ to _unit length_ (with possibly undefined results if the assumption is invalid).  We leave this responsibility to the application which can use the following method to normalize a vector as required:

```java
public Vector normalize() {
    float len = magnitude();
    if(MathsUtil.isEqual(1, len)) {
        return this;
    }
    else {
        float f = MathsUtil.inverseRoot(len);
        return multiply(f);
    }
}
```

Where `multiply` scales a vector by a given value:

```java
public Vector multiply(float f) {
    return new Vector(x * f, y * f, z * f);
}
```

The camera class uses the _cross product_ to yield the vector perpendicular to two other vectors (using the right-hand rule):

```java
public Vector cross(Vector vec) {
    float x = this.y * vec.z - this.z * vec.y;
    float y = this.z * vec.x - this.x * vec.z;
    float z = this.x * vec.y - this.y * vec.x;
    return new Vector(x, y, z);
}
```

Finally we add a convenience method to generate the vector between two points:

```java
public static Vector between(Point start, Point end) {
    float dx = end.x - start.x;
    float dy = end.y - start.y;
    float dz = end.z - start.z;
    return new Vector(dx, dy, dz);
}
```

Note that the vector class is immutable and all 'mutator' methods create a new instance.

### Camera Model

The final improvement is to replace the view transform code in the demo with a _camera_ implementation, which we will be using heavily in the next chapter.

The camera is a model class (in MVC terms) representing the position and orientation of the viewer:

```java
public class Camera {
    private Point pos = Point.ORIGIN;
    private Vector dir = Vector.Z;
    private Vector up = Vector.Y;
}
```

Note that under the hood the camera direction is the inverse of the view direction:

We provide various mutator methods to move the camera position:

```java
public void move(Point pos) {
    this.pos = notNull(pos);
}

public void move(Vector vec) {
    pos = pos.add(vec);
}

public void move(float dist) {
    move(dir.multiply(dist));
}

public void strafe(float dist) {
    move(right.multiply(dist));
}
```

And a convenience setter to point the camera at a given target location:

```java
public void look(Point pt) {
    if(pos.equals(pt)) throw new IllegalArgumentException(...);
    dir = Vector.between(pt, pos).normalize();
}
```

We next add some transient members to the camera class to support the view transform:

```java
public class Camera {
    ...
    private Vector right;
    private Matrix matrix;
    private boolean dirty;
}
```

Where:

* The `dirty` flag is signalled in the various mutator methods (not shown) when any of the camera properties are modified.

* The `right` vector is the horizontal axis of the viewport (also used in the `strafe` method above).

The view transform matrix for the camera is then constructed on demand:

```java
public Matrix matrix() {
    if(dirty) {
        update();
        dirty = false;
    }
    return matrix;
}
```

The `update` method first determines the viewport axes based on the camera axes:

```java
private void update() {
    // Determine right axis
    right = up.cross(dir).normalize();

    // Determine up axis
    Vector y = right.cross(dir).normalize();
}
```

Note that the computed Y axis is inverted to account for the global flip.

From the three axes we can now build the view transform matrix as before:

```java
// Build translation component
Matrix trans = Matrix.translation(new Vector(pos));

// Build rotation component
Matrix rot = new Matrix.Builder()
    .identity()
    .row(0, right)
    .row(1, y)
    .row(2, dir)
    .build();

// Create camera matrix
matrix = rot.multiply(trans);
```

We add a new camera object to the configuration class:

```java
public class CameraConfiguration {
    @Bean
    public static Camera camera() {
        Camera cam = new Camera();
        cam.move(new Point(0, -0.5f, -2));
        return cam;
    }
}
```

And finally we remove the old view transform code and use the camera to calculate the final projection matrix:

```java
return projection.multiply(cam.matrix()).multiply(model);
```

---

## Summary

In this chapter we implemented:

- The depth-stencil pipeline stage.

- A mechanism to clear colour and depth-stencil attachments.

- A builder for draw commands.

- Texture coordinate inversion for an OBJ model.

- A camera model.
