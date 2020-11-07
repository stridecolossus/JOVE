# Overview

The _graphics pipeline_ specifies the various _stages_ executed by the hardware to render a graphics fragment.

Pipeline stages are either a configurable _fixed function_ or a programmable stage implemented by a _shader_ module.

Configuring the pipeline requires a large amount of information for even the simplest case (though much of this is often default information or empty structures).

In this chapter we will implement the mandatory fixed-function pipeline stages and the _vertex shader_ stage required for the triangle demo:

---

# Building a Pipeline

## Pipeline Class

The pipeline domain class itself is trivial:

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

    /**
     * Creates a command to bind this pipeline.
     * @return New bind pipeline command
     */
    public Command bind() {
        return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, handle());
    }

    /**
     * A <i>pipeline layout</i> specifies the resources used by a pipeline.
     */
    public static class Layout extends AbstractVulkanObject {
    }
}
```

The _pipeline layout_ specifies the resources (texture samplers, uniform buffers, etc) used by the pipeline.
Although none of these are needed for the triangle demo we are still required to specify a layout for the pipeline.
We will gloss over construction of the layout until a future chapter.

## Pipeline Builder

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

    @BeforeEach
    void before() {
        builder = new Pipeline.Builder(dev);
        rect = new Rectangle(new Dimensions(3, 4));
    }

    @Test
    void build() {
        pipeline = builder
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

For the triangle demo the only mandatory information is the viewport pipeline stage and the vertex shader.

## Nested Builders

If the number and complexity of the nested builders was relatively small we would implement them as local classes of the pipeline (as we did for the render pass in the previous chapter).
This is clearly not viable for the pipeline - the parent class would become unwieldy, error-prone and difficult to maintain or test.

Therefore we need to factor out the nested builders into their own source files whilst maintaining the fluid interface.
After some research we failed to find a decent strategy or pattern for this approach (though we did find some absolutely horrible solutions using reflection and other shenanigans).

Our solution is that each nested builder will have a reference to the parent pipeline builder (returned in its build() method) and a protected result() method that constructs the resultant object.  We wrap this up in an abstract base-class:

```
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

For example here is the re-factored viewport stage builder (which we will complete below):

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

Finally the pipeline builder initialises the parent() of each nested builder in its constructor.

This is a slightly shonky implementation but it is relatively simple and achieves our goal of a fluid nested builder. 
The resultant classes are relatively self-contained and are therefore more manageable and testable (and the nastier details are at least package-private).

## Conclusion

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

---

# Viewport Pipeline Stage

Rather than clog this chapter up by covering the design and development of every nested builder we introduce them as they are used.

The only fixed-function stage builder that we **must** configure for any application is the viewport which specifies:
- The viewport rectangle(s) that define the regions of the framebuffer that will be rendered to.
- The scissor rectangles that define the drawing regions for the rasterizer.

```java
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
    /**
     * Transient viewport descriptor.
     */
    private record Viewport(Rectangle rect, float min, float max) {
    }

    private final List<Viewport> viewports = new ArrayList<>();
    private final List<Rectangle> scissors = new ArrayList<>();
    
    private boolean copy = true;

    public ViewportStageBuilder setCopyScissor(boolean copy) {
        this.copy = copy;
        return this;
    }

    public ViewportStageBuilder viewport(Rectangle rect, float min, float max) {
        // Add viewport
        viewports.add(new Viewport(rect, min, max, flip));

        // Add scissor
        if(copy) {
            scissor(rect);
        }

        return this;
    }

    public ViewportStageBuilder viewport(Rectangle viewport) {
        return viewport(viewport, 0, 1);
    }

    public ViewportStageBuilder scissor(Rectangle rect) {
        scissors.add(rect);
        return this;
    }
}
```

The `setCopyScissor` setting is used to automatically create a scissor rectangle for each viewport (the most common case).

The build method creates the viewport and scissor rectangle arrays:

```java
@Override
protected VkPipelineViewportStateCreateInfo result() {
    // Add viewports
    final VkPipelineViewportStateCreateInfo info = new VkPipelineViewportStateCreateInfo();
    if(viewports.isEmpty()) throw new IllegalArgumentException("No viewports specified");
    info.viewportCount = viewports.size();
    info.pViewports = VulkanStructure.array(VkViewport::new, viewports, Viewport::populate);

    // Add scissors
    if(scissors.isEmpty()) throw new IllegalArgumentException("No scissor rectangles specified");
    info.scissorCount = scissors.size();
    info.pScissors = VulkanStructure.array(VkRect2D.ByReference::new, scissors, this::rectangle);

    return info;
}
```

---

# Shader Pipeline Stage

The final element of the pipeline that we **must** configure is the vertex shader.

## Shaders

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
public static Shader loader(LogicalDevice dev, InputStream in) throws IOException {
    final byte[] code = in.readAllBytes();
    return create(dev, code);
}
```

This is a fairly crude approach that we will replace with a more elegant and flexible solution in subsequent chapters.

## Builder

The shader stage pipeline builder creates an **array** of `VkPipelineShaderStageCreateInfo` descriptors:

```java
public class ShaderStageBuilder extends AbstractPipelineBuilder<VkPipelineShaderStageCreateInfo> {
    /**
     * Entry for a shader stage.
     */
    private static class Entry {
        private VkShaderStageFlag stage;
        private Shader shader;
        private String name = "main";

        /**
         * Validates this shader stage.
         */
        private void validate() {
            if(stage == null) throw new IllegalArgumentException("Shader stage not specified");
            if(shader == null) throw new IllegalArgumentException("No shader specified: " + stage);
        }

        /**
         * Populates the shader stage descriptor.
         * @param info Shader stage descriptor
         */
        private void populate(VkPipelineShaderStageCreateInfo info) {
            info.stage = stage;
            info.module = shader.handle();
            info.pName = name;
        }
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

An `Entry` is a transient record specifying a shader stage.

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

# Integration

We can now load the shaders and configure the rendering pipeline for the triangle demo.

## Shaders

The vertex shader hard-codes the triangle vertices and passes the colour for each vertex through to the fragment shader:

```C
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) out vec4 fragColour;

vec2 positions[3] = vec2[](
    vec2(0.0, -0.5),
    vec2(0.5, 0.5),
    vec2(-0.5, 0.5)
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

which is simply passed through to the next stage by the fragment shader:

```C
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec4 fragColour;

layout(location = 0) out vec4 outColour;

void main() {
    outColour = fragColour;
}
```

We will also need to compile the shaders to SPIV using the GLSLC utility application provided as part of the JDK:

```
cd JOVE/src/test/resources/demo/triangle
glslc triangle.frag -o spv.triangle.frag
glslc triangle.vert -o spv.triangle.vert
```

## Demo

We can now load the shader modules:

```java
// Load shaders
final Shader vert = loader.load(dev, new FileInputStream("./src/test/resources/demo/triangle/spv.triangle.vert"));
final Shader frag = loader.load(dev, new FileInputStream("./src/test/resources/demo/triangle/spv.triangle.frag"));
```

And finally configure the pipeline:

```java
final Rectangle extent = new Rectangle(chain.extents());

// Create pipeline layout
final Pipeline.Layout layout = new Pipeline.Layout.Builder().build();

// Create pipeline
final Pipeline pipeline = new Pipeline.Builder(dev)
    .layout(layout)
    .pass(pass)
    .viewport(extent)
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

# Summary

In this chapter we implemented a number of builders to configure the graphics pipeline and implemented shader modules.

