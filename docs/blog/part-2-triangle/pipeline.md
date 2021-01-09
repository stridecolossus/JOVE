---
title: The Graphics Pipeline
---

## Overview

The _graphics pipeline_ configures the various _stages_ executed by the hardware to render a fragment.

A pipeline stage is either a configurable _fixed function_ or a programmable stage implemented by a _shader_ module.

Configuring the pipeline requires a large amount of information for even the simplest case (though much of this is often default information or empty structures).

In this chapter we will implement the mandatory fixed-function pipeline stages and the _vertex shader_ stage required for the triangle demo:

---

## Building a Pipeline

### Pipeline Class

The pipeline domain class itself is relatively trivial:

```java
public class Pipeline extends AbstractVulkanObject {
    /**
     * Constructor.
     * @param handle        Pipeline handle
     * @param dev           Device
     */
    Pipeline(Pointer handle, LogicalDevice dev) {
        super(handle, dev, dev.library()::vkDestroyPipeline);
    }

    public static class Builder {
    }

    /**
     * A <i>pipeline layout</i> specifies the resources used by a pipeline.
     */
    public static class Layout extends AbstractVulkanObject {
    }
}
```

The API methods to create and manage the pipeline are defined in the `VulkanLibraryPipeline` JNA interface.

### Pipeline Layout

The _pipeline layout_ specifies the _resources_ (texture samplers, uniform buffers, etc) and _push constants_ used by the pipeline.

None of these are needed for the triangle demo but we are still required to specify a layout for the pipeline.

We create a bare-bones implementation this this stage of development:

```java
public static class Layout extends AbstractVulkanObject {
    private Layout(Pointer handle, LogicalDevice dev) {
        super(handle, dev, dev.library()::vkDestroyPipelineLayout);
    }

    public static class Builder {
        private final LogicalDevice dev;

        public Builder(LogicalDevice dev) {
            this.dev = notNull(dev);
        }

        public Layout build() {
            final VkPipelineLayoutCreateInfo info = new VkPipelineLayoutCreateInfo();
            // TODO
            final VulkanLibrary lib = dev.library();
            final PointerByReference layout = lib.factory().pointer();
            check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));
            return new Layout(layout.getValue(), dev);
        }
    }
}
```

### Pipeline Builder

Configuration of the pipeline is probably the largest and most complex aspect of creating a Vulkan application (in terms of the amount of supporting functionality that is required).

Our goals for configuration of the pipeline are:

1. Apply sensible defaults for the optional pipeline stages to reduce the amount of boiler-plate for a given application.

2. Implement a fluid interface for pipeline construction.

Obviously we start with a builder:

```java
public static class Builder {
    private final LogicalDevice dev;
    private Layout layout;
    private RenderPass pass;

    public Builder(LogicalDevice dev) {
        this.dev = notNull(dev);
        init();
    }

    public Builder layout(Layout layout) {
        this.layout = notNull(layout);
        return this;
    }

    public Builder pass(RenderPass pass) {
        this.pass = notNull(pass);
        return this;
    }
    
    public Pipeline build() {
    }
}
```

We create outline nested builders for the mandatory fixed function stages:

```java
private final VertexInputStageBuilder input = new VertexInputStageBuilder();
private final InputAssemblyStageBuilder assembly = new InputAssemblyStageBuilder();
// TODO - tessellation
private final ViewportStageBuilder viewport = new ViewportStageBuilder();
private final RasterizerStageBuilder raster = new RasterizerStageBuilder();
// TODO - depth/stencil
// TODO - multi sample
private final ColourBlendStageBuilder blend = new ColourBlendStageBuilder();
// TODO - dynamic

/**
 * @return Builder for the vertex input stage
 */
public VertexInputStageBuilder input() {
    return input;
}

// etc...
```

And another that builds the **array** of shader stages:

```java
private final ShaderStageBuilder shader = new ShaderStageBuilder();
```

We next implement a unit-test for the parent builder shown here to illustrate the fluid configuration approach:

```java
class BuilderTest {
    private Pipeline.Builder builder;
    private Rectangle rect;
    private Pipeline.Layout layout;
    private RenderPass pass;

    @BeforeEach
    void before() {
        builder = new Pipeline.Builder(dev);
        rect = new Rectangle(new Dimensions(3, 4));
        layout = mock(Pipeline.Layout.class);
        pass = mock(RenderPass.class);
    }

    @Test
    void build() {
        pipeline = builder
            .layout(layout)
            .pass(pass)
            .viewport()
                .viewport(rect)
                .scissor(rect)
                .build()
            .shader()
                .stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
                .shader(mock(Shader.class))
                .build()
            .build();

        ...
    }
}
```

### Nested Builders

If the number and complexity of the nested builders was relatively small we would implement them as _local classes_ of the pipeline (as we did for the render pass in the previous chapter).
This is clearly not viable for the pipeline - the parent class would become unwieldy, error-prone and difficult to maintain or test.

Therefore we need to factor out the nested builders into their own source files whilst maintaining the fluid interface.
After some research we failed to find a decent strategy or pattern for this approach (though we did find some absolutely hideous solutions using reflection and other shenanigans).

Our solution is that each nested builder will have a reference to the parent pipeline builder (returned in its `build()` method) and a protected `result()` method that constructs the resultant object.  We wrap this up in an abstract base-class:

```java
abstract class AbstractPipelineBuilder<T> {
    private Pipeline.Builder parent;

    /**
     * Sets the parent builder.
     * @param parent Parent builder
     */
    protected void parent(Pipeline.Builder parent) {
        this.parent = notNull(parent);
    }

    /**
     * @return Result of this builder
     */
    protected abstract T result();

    /**
     * Completes construction.
     * @return Parent builder
     */
    public Pipeline.Builder build() {
        return parent;
    }
}
```

For example here is the viewport stage builder (which we will complete below):

```java
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
    ...

    @Override
    protected VkPipelineViewportStateCreateInfo result() {
        final var info = new VkPipelineViewportStateCreateInfo();
        ...
        return info;
    }
}
```

Finally the pipeline builder initialises the `parent()` of each nested builder in its constructor.

This is a slightly shonky implementation but it is relatively simple and achieves our goal of a fluid nested builder. 
The resultant classes are relatively self-contained and are therefore more manageable and testable (and the nastier details are at least package-private).

### Conclusion

We can now finish the pipeline builder:

```java
public Pipeline build() {
    // Create descriptor
    final VkGraphicsPipelineCreateInfo pipeline = new VkGraphicsPipelineCreateInfo();

    // Init layout
    if(layout == null) throw new IllegalArgumentException("No pipeline layout specified");
    pipeline.layout = layout.handle();

    // Init render pass
    if(pass == null) throw new IllegalArgumentException("No render pass specified");
    pipeline.renderPass = pass.handle();
    pipeline.subpass = 0;       // TODO
    
    ...
}
```

The configuration for the fixed-function stages is populated by retrieving the result() of each nested builder:

```java
// Init fixed function pipeline stages
pipeline.pVertexInputState = input.result();
pipeline.pInputAssemblyState = assembly.result();
pipeline.pViewportState = viewport.result();
pipeline.pRasterizationState = raster.result();
pipeline.pColorBlendState = blend.result();
```

Next we populate the shader stages:

```java
// Init shader pipeline stages
pipeline.stageCount = shaders.size();
pipeline.pStages = shaders.result();
```

Finally we invoke the API to instantiate the pipeline:

```java
// Allocate pipeline
final VulkanLibrary lib = dev.library();
final Pointer[] pipelines = lib.factory().pointers(1);
check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

// Create pipeline
return new Pipeline(pipelines[0], dev);
```

### Viewport Pipeline Stage

Rather than clog this chapter up by covering the design and development of every nested builder we will introduce them as they are needed.

The only fixed-function stage builder that we **must** configure for any application is the viewport which specifies:
- The viewport rectangle(s) that define the regions of the framebuffer that will be rendered to.
- The scissor rectangles that define the drawing regions for the rasterizer.

The builder for the viewport stage is relatively simple:

```java
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
    /**
     * Transient viewport descriptor.
     */
    private record Viewport(Rectangle rect, Percentile min, Percentile max) {
    }

    private final List<Viewport> viewports = new ArrayList<>();
    private final List<Rectangle> scissors = new ArrayList<>();
    
    public ViewportStageBuilder viewport(Rectangle rect, Percentile min, Percentile max) {
        viewports.add(new Viewport(rect, min, max));
        return this;
    }

    public ViewportStageBuilder viewport(Rectangle viewport) {
        return viewport(viewport, 0, 1);
    }

    public ViewportStageBuilder scissor(Rectangle rect) {
        scissors.add(notNull(rect));
        return this;
    }
}
```

The percentile type is described at the end of this chapter.

The builder creates the viewport and scissor rectangle arrays:

```java
protected VkPipelineViewportStateCreateInfo result() {
    // Validate
    final int count = viewports.size();
    if(count == 0) throw new IllegalArgumentException("No viewports specified");
    if(scissors.size() != count) throw new IllegalArgumentException("Number of scissors must be the same as the number of viewports");

    // Add viewports
    final VkPipelineViewportStateCreateInfo info = new VkPipelineViewportStateCreateInfo();
    info.viewportCount = count;
    info.pViewports = StructureCollector.toPointer(viewports, VkViewport::new, Viewport::populate);

    // Add scissors
    if(scissors.isEmpty()) throw new IllegalArgumentException("No scissor rectangles specified");
    info.scissorCount = count;
    info.pScissors = StructureCollector.toPointer(scissors, VkRect2D.ByReference::new, ViewportStageBuilder::rectangle);

    return info;
}
```

The descriptors are populated by the following helper methods:

```java
private record Viewport ... {
    private void populate(VkViewport viewport) {
        rectangle(rect, viewport);
        viewport.minDepth = min.floatValue();
        viewport.maxDepth = max.floatValue();
    }
}

private static void rectangle(Rectangle rect, VkRect2D out) {
    out.offset.x = rect.x();
    out.offset.y = rect.y();
    out.extent.width = rect.width();
    out.extent.height = rect.height();
}
```

The most common case is a single viewport with a corresponding scissor rectangle, so we add a helper that copies the most recent viewport rectangle:

```java
public ViewportStageBuilder copyScissor() {
    if(viewports.isEmpty()) throw new IllegalStateException("No viewports have been specified");
    final Viewport prev = viewports.get(viewports.size() - 1);
    scissor(prev.rect);
    return this;
}
```

---

## Shader Pipeline Stage

The final element of the pipeline that we **must** configure is the vertex shader.

### Shaders

First we create a new domain object for a shader module:

```java
public class Shader extends AbstractVulkanObject {
    /**
     * Creates a shader module.
     * @param dev       Parent device
     * @param code      Shader SPIV code
     * @return New shader
     */
    public static Shader create(LogicalDevice dev, byte[] code) {
        // Create descriptor
        final VkShaderModuleCreateInfo info = new VkShaderModuleCreateInfo();
        info.codeSize = code.length;
        info.pCode = Bufferable.allocate(code);

        // Allocate shader
        final VulkanLibrary lib = dev.library();
        final PointerByReference shader = lib.factory().pointer();
        check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

        // Create shader
        return new Shader(shader.getValue(), dev);
    }

    /**
     * Constructor.
     * @param handle        Shader module handle
     * @param dev           Device
     */
    private Shader(Pointer handle, LogicalDevice dev) {
        super(handle, dev, dev.library()::vkDestroyShaderModule);
    }
}
```

A shader is loaded from an input stream using a static factory method:

```java
public static Shader load(LogicalDevice dev, InputStream in) throws IOException {
    final byte[] code = in.readAllBytes();
    return create(dev, code);
}
```

The API methods to create and destroy the shader are defined in the `VulkanLibraryShader` JNA interface.

### Builder

The shader stage pipeline builder creates an **array** of `VkPipelineShaderStageCreateInfo` descriptors:

```java
public class ShaderStageBuilder extends AbstractPipelineBuilder<VkPipelineShaderStageCreateInfo> {
    /**
     * Entry for a shader stage.
     */
    private static class Entry {
        ...
    }

    private final Map<VkShaderStageFlag, Entry> shaders = new HashMap<>();
    
    ...

    /**
     * {@inheritDoc}
     * @throws IllegalStateException if no vertex shader has been configured
     */
    @Override
    protected VkPipelineShaderStageCreateInfo result() {
        if(!shaders.containsKey(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)) throw new IllegalStateException("No vertex shader specified");
        return VulkanStructure.array(VkPipelineShaderStageCreateInfo::new, shaders.values(), Entry::populate);
    }
}
```

An `Entry` is a transient record that specifies a configured shader stage:

```java
private static class Entry {
    private VkShaderStageFlag stage;
    private Shader shader;
    private String name = "main";

    private void validate() {
        if(stage == null) throw new IllegalArgumentException("Shader stage not specified");
        if(shader == null) throw new IllegalArgumentException("No shader specified: " + stage);
    }

    private void populate(VkPipelineShaderStageCreateInfo info) {
        info.stage = stage;
        info.module = shader.handle();
        info.pName = name;
    }
}
```

The builder has a reference to the _current_ entry which is populated in the various setter methods:

```java
private Entry entry;

/**
 * Starts a new shader stage.
 */
void init() {
    if(entry != null) throw new IllegalStateException("Previous shader stage has not been completed");
    entry = new Entry();
}

/**
 * Sets the shader stage.
 * @param stage Shader stage
 */
public ShaderStageBuilder stage(VkShaderStageFlag stage) {
    entry.stage = notNull(stage);
    return this;
}
```

We over-ride the public build method to validate the current entry and check for duplicates:

```java
@Override
public Builder build() {
    entry.validate();
    if(shaders.containsKey(entry.stage)) throw new IllegalArgumentException("Duplicate shader stage: " + entry.stage);
    shaders.put(entry.stage, entry);
    entry = null;
    return super.build();
}
```

Finally we modify the pipeline builder to initialise a new shader stage:

```java
public ShaderStageBuilder shader() {
    shaders.init();
    return shaders;
}
```

---

## Integration

We can now load the shaders and configure the rendering pipeline for the triangle demo.

### Shaders

The vertex shader hard-codes the triangle vertices and passes a colour for each vertex through to the fragment shader:

```glsl
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) out vec4 fragColour;

vec2 positions[3] = vec2[](
    vec2(0.0, -0.5),
    vec2(-0.5, 0.5),
    vec2(0.5, 0.5)
);

vec3 colours[3] = vec3[](
    vec3(1.0, 0.0, 0.0),
    vec3(0.0, 1.0, 0.0),
    vec3(0.0, 0.0, 1.0)
);

void main() {
    gl_Position = vec4(positions[gl_VertexIndex], 0.0, 1.0);
    fragColour = vec4(colours[gl_VertexIndex], 1.0);
}
```

Note the use of the built-in `gl_VertexIndex` variable which is used to index into the two arrays.

The colour for each vertex is simply passed through to the next stage by the fragment shader:

```glsl
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec4 fragColour;

layout(location = 0) out vec4 outColour;

void main() {
    outColour = fragColour;
}
```

We will also need to compile the shaders to SPIV using the `GLSLC` utility application provided as part of the JDK:

```
cd JOVE/src/test/resources/demo/triangle
glslc triangle.frag -o spv.triangle.frag
glslc triangle.vert -o spv.triangle.vert
```

### Demo

We can now load the shader modules:

```java
// Load shaders
final Shader vert = Shader.load(dev, new FileInputStream("./src/test/resources/demo/triangle/spv.triangle.vert"));
final Shader frag = Shader.load(dev, new FileInputStream("./src/test/resources/demo/triangle/spv.triangle.frag"));
```

And finally configure the pipeline:

```java
// Create pipeline layout
final Pipeline.Layout layout = new Pipeline.Layout.Builder().build();

// Create pipeline
final Pipeline pipeline = new Pipeline.Builder(dev)
    .layout(layout)
    .pass(pass)
    .viewport()
        .viewport(new Rectangle(chain.extents()))
        .build()
    .shader()
        .stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
        .shader(vert)
        .build()
    .shader()
        .stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
        .shader(frag)
        .build()
    .build();
```

Phew!

---

## Improvements

### Percentile Values

For the min/max depth of the viewport we introduced the `Percentile` class that represents a percentile value as a 0..1 floating-point number:

```java
public final class Percentile extends Number implements Comparable<Percentile> {
    private final float value;

    /**
     * Constructor.
     * @param value Percentile as a 0..1 floating-point value
     */
    public Percentile(float value) {
        Check.isPercentile(value);
        this.value = value;
    }

    @Override
    public float floatValue() {
        return value;
    }

    ...
}
```

We also add convenience constants:

```java
/**
 * Maximum value of a percentile expressed as an integer.
 */
public static final int MAX = 100;

/**
 * Zero percentile.
 */
public static final Percentile ZERO = new Percentile(0);

/**
 * 50% percentile.
 */
public static final Percentile HALF = new Percentile(0.5f);

/**
 * 100% percentile.
 */
public static final Percentile ONE = new Percentile(1);
```

Finally we add support for percentiles specified by a 0..100 integer:

```java
private static final Percentile[] INTEGERS = new Percentile[MAX + 1];

static {
    for(int n = 0; n <= MAX; ++n) {
        INTEGERS[n] = new Percentile(n / (float) MAX);
    }
}

/**
 * Creates an integer percentile.
 * @param value Percentile as a 0..100 integer
 * @return Percentile
 * @throws ArrayIndexOutOfBoundsException if the given value is not in the range 0...100
 */
public static Percentile of(int value) {
    return INTEGERS[value];
}

...

@Override
public String toString() {
    return (int) (value * MAX) + "%";
}
```

We can now refactor the queue priorities in the `RequiredQueue` class using this new type.

### Testing Support

Any unit-test that is dependant on the logical device (which is the majority of them) generally require the same test setup.

We introduce the following test base-class and refactor all existing test cases accordingly:

```java
public abstract class AbstractVulkanTest {
    protected MockReferenceFactory factory;
    protected LogicalDevice dev;
    protected VulkanLibrary lib;

    @BeforeEach
    private final void beforeVulkanTest() {
        // Create API
        lib = mock(VulkanLibrary.class);

        // Init reference factory
        factory = new MockReferenceFactory();
        when(lib.factory()).thenReturn(factory);

        // Create logical device
        dev = mock(LogicalDevice.class);
        when(dev.handle()).thenReturn(new Handle(new Pointer(42)));
        when(dev.library()).thenReturn(lib);
    }
}
```

Note that we give the setup method a relatively unique name (and make it private and final) to avoid potentially conflicting with the sub-class.

---

## Summary

In this chapter we implemented a nested builder to configure the graphics pipeline and shader modules.

