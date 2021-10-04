---
title: The Graphics Pipeline
---

## Overview

The _graphics pipeline_ configures the various _stages_ executed by the hardware to render a fragment.

A pipeline stage is either a configurable _fixed function_ or a _programmable stage_ implemented by a _shader module_.

Configuring the pipeline requires a large amount of information for even the simplest case (though much of this is often default data or empty structures).

In this chapter we will implement the mandatory fixed-function pipeline stages and the _vertex shader_ stage required for the triangle demo:

---

## Building the Pipeline

### Pipeline

The pipeline domain class itself is relatively trivial:

```java
public class Pipeline extends AbstractVulkanObject {
    private final PipelineLayout layout;

    Pipeline(Pointer handle, LogicalDevice dev, PipelineLayout layout) {
        super(handle, dev);
        this.layout = notNull(layout);
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyPipeline;
    }
}
```

The _pipeline layout_ specifies the _resources_ (texture samplers, uniform buffers and _push constants_) used by the pipeline.  None of these are needed for the triangle demo but we are still required to specify a layout for the pipeline.

We create a bare-bones implementation for this stage of development:

```java
public class PipelineLayout extends AbstractVulkanObject {
    private PipelineLayout(Pointer handle, LogicalDevice dev) {
        super(handle, dev);
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyPipelineLayout;
    }
}
```

And a builder to create the layout:

```java
public PipelineLayout build() {
    final var info = new VkPipelineLayoutCreateInfo();
    final VulkanLibrary lib = dev.library();
    final PointerByReference layout = lib.factory().pointer();
    check(lib.vkCreatePipelineLayout(dev.handle(), info, null, layout));
    return new PipelineLayout(layout.getValue(), dev);
}
```

### Builder

Configuration of the pipeline is probably the largest and most complex aspect of creating a Vulkan application (in terms of the amount of supporting functionality that is required).

Our goals for configuration of the pipeline are:

1. Implement a fluid interface for pipeline construction.

2. Apply sensible defaults for the optional pipeline stages to reduce the amount of boiler-plate for a given application.

3. Factor out the code for configuration of the pipeline stages into separate classes (for maintainability and testability reasons).

We start with an outline builder for the pipeline:

```java
public static class Builder {
    private PipelineLayout layout;
    private RenderPass pass;

    public Builder layout(PipelineLayout layout) {
        this.layout = notNull(layout);
        return this;
    }

    public Builder pass(RenderPass pass) {
        this.pass = notNull(pass);
        return this;
    }
}
```

Next we create outline builders for the mandatory fixed-function pipeline stages:

```java
private final VertexInputStageBuilder input = new VertexInputStageBuilder();
private final InputAssemblyStageBuilder assembly = new InputAssemblyStageBuilder();
private final ViewportStageBuilder viewport = new ViewportStageBuilder();
private final RasterizerStageBuilder raster = new RasterizerStageBuilder();
private final ColourBlendStageBuilder blend = new ColourBlendStageBuilder();
```

Which can be requested from the parent builder via an accessor, for example:

```java
/**
 * @return Builder for the vertex input stage
 */
public VertexInputStageBuilder input() {
    return input;
}
```

If the number and complexity of these nested builders was relatively small we would implement them as _local classes_ of the pipeline (as we did for the render pass in the previous chapter).  This is clearly not viable for the pipeline - the parent class would become unwieldy and difficult to maintain or test.

Therefore we need to factor out the nested builders into their own source files whilst maintaining the fluid interface.  After some research we failed to find a decent strategy or pattern for this approach (though we did find some absolutely hideous solutions using reflection and other shenanigans).

Our solution is that each nested builder will have a reference to the parent pipeline builder (returned in its `build` method) and a package-private `get` method that constructs the resultant object.  We wrap this up in the following template class:

```java
abstract class AbstractPipelineBuilder<T> {
    private Builder parent;

    /**
     * Sets the parent builder.
     * @param parent Parent
     */
    void parent(Builder parent) {
        this.parent = notNull(parent);
    }

    /**
     * @return Result of this builder
     */
    abstract T get();

    /**
     * Constructs this object.
     * @return Parent pipeline builder
     */
    public Builder build() {
        return parent;
    }
}
```

For example the viewport stage builder (which we complete below) is declared as follows:

```java
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
    @Override
    VkPipelineViewportStateCreateInfo get() {
        ...
    }
}
```

The pipeline builder initialises the parent of each nested pipeline stage in the constructor:

```java
public Builder() {
    viewport.parent(this);
    ...
}
```

This is a slightly ropey implementation but it is relatively simple and achieves our goal of a fluid nested builder.  The resultant classes are relatively self-contained in terms of maintenance and testing and the nastier details are at least package-private.

### Viewport

To avoid making this chapter overly long we only cover the implementation of the mandatory pipeline stages required for the triangle demo and introduce other stages as they are needed.

The only fixed-function that we __must__ configure is the _viewport pipeline stage_ that defines the drawing regions of the frame buffers and rasterizer:

```java
public class ViewportStageBuilder extends AbstractPipelineBuilder<VkPipelineViewportStateCreateInfo> {
    private final List<Viewport> viewports = new ArrayList<>();
    private final List<Rectangle> scissors = new ArrayList<>();
}
```

A `Viewport` is a local type that aggregates the viewport rectangle and near/far rendering depths:

```java
private record Viewport(Rectangle rect, Percentile min, Percentile max) {
    private Viewport {
        Check.notNull(rect);
        Check.notNull(min);
        Check.notNull(max);
    }
}
```

Where `Rectangle` is another trivial record type:

```java
public record Rectangle(int x, int y, int width, int height) { ... }
```

We add the following setters to specify the viewport(s) for the pipeline:

```java
public ViewportStageBuilder viewport(Rectangle rect, Percentile min, Percentile max) {
    viewports.add(new Viewport(rect, min, max));
    return this;
}

public ViewportStageBuilder viewport(Rectangle viewport) {
    return viewport(viewport, Percentile.ZERO, Percentile.ONE);
}

public ViewportStageBuilder scissor(Rectangle rect) {
    scissors.add(notNull(rect));
    return this;
}
```

Finally the builder populates the Vulkan descriptor for the viewport stage:

```java
@Override
VkPipelineViewportStateCreateInfo get() {
    // Validate
    final int count = viewports.size();
    if(count == 0) throw new IllegalArgumentException("No viewports specified");
    if(scissors.size() != count) throw new IllegalArgumentException("Number of scissors must be the same as the number of viewports");

    // Add viewports
    final var info = new VkPipelineViewportStateCreateInfo();
    info.viewportCount = count;
    info.pViewports = StructureHelper.first(viewports, VkViewport::new, Viewport::populate);

    // Add scissors
    info.scissorCount = count;
    info.pScissors = StructureHelper.first(scissors, VkRect2D.ByReference::new, VulkanHelper::populate);

    return info;
}
```

Which uses the following population method on the `Viewport` type:

```java
private void populate(VkViewport viewport) {
    viewport.x = rect.x();
    viewport.y = rect.y();
    viewport.width = rect.width();
    viewport.height = rect.height();
    viewport.minDepth = min.floatValue();
    viewport.maxDepth = max.floatValue();
}
```

---

## Shaders

The other component that we must implement is the programmable pipeline stage to support a _vertex shader_ to render the triangle.

### Shader Module

We first create the a _shader module_ domain object:

```java
public class Shader extends AbstractVulkanObject {
    private Shader(Pointer handle, LogicalDevice dev) {
        super(handle, dev);
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyShaderModule;
    }
}
```

Shaders are created via a factory that first converts the SPIV code to a byte-buffer:

```java
public static Shader create(LogicalDevice dev, byte[] code) {
    // Convert code to buffer
    ByteBuffer bb = ByteBuffer
        .allocateDirect(code.length)
        .order(ByteOrder.nativeOrder())
        .put(code)
        .flip();
}
```

And as usual we populate a Vulkan descriptor for the shader and invoke the relevant API method:

```java
// Create descriptor
var info = new VkShaderModuleCreateInfo();
info.codeSize = code.length;
info.pCode = bb;

// Allocate shader
VulkanLibrary lib = dev.library();
PointerByReference shader = lib.factory().pointer();
check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

// Create shader
return new Shader(shader.getValue(), dev);
```

The API for shader modules consists of two methods:

```java
interface VulkanLibraryShader {
    int vkCreateShaderModule(Handle device, VkShaderModuleCreateInfo info, Handle pAllocator, PointerByReference shader);
    void vkDestroyShaderModule(Handle device, Handle shader, Handle pAllocator);
}
```

We also add the following helper to load a shader from the file system:

```java
public static Shader load(LogicalDevice dev, InputStream in) throws IOException {
    byte[] code = in.readAllBytes();
    return create(dev, code);
}
```

### Shader Pipeline Stage

A pipeline can be comprised of multiple programmable shader stages, therefore in this case we implement the nested builder for shaders as a local class of the pipeline builder:

```java
public static class Builder {
    public class ShaderStageBuilder {
        private final VkShaderStage stage;
        private Shader shader;
        private String name = "main";
    }
}
```

The build method returns control to the parent pipeline builder:

```java
public Builder build() {
    validate();
    return Builder.this;
}
```

We add a map of shader stages to the pipeline builder and a factory method to start a new shader stage (checking for duplicates):

```java
private final Map<VkShaderStage, ShaderStageBuilder> shaders = new HashMap<>();

public ShaderStageBuilder shader(VkShaderStage stage) {
    final var shader = new ShaderStageBuilder(stage);
    if(shaders.containsKey(stage)) throw new IllegalArgumentException(...);
    shaders.put(stage, shader);
    return shader;
}
```

And the following helper to populate the resultant JNA descriptor array:

```java
void populate(VkPipelineShaderStageCreateInfo info) {
    validate();
    info.stage = stage;
    info.module = shader.handle();
    info.pName = name;
}
```

### Conclusion

We can now complete the parent builder which first populates the Vulkan descriptor for the pipeline:

```java
public Pipeline build(LogicalDevice dev) {
    // Create descriptor
    final var pipeline = new VkGraphicsPipelineCreateInfo();

    // Init layout
    if(layout == null) throw new IllegalArgumentException("No pipeline layout specified");
    pipeline.layout = layout.handle();

    // Init render pass
    if(pass == null) throw new IllegalArgumentException("No render pass specified");
    pipeline.renderPass = pass.handle();
    ...
}
```

Next the fixed-function stages are retrieved from the various nested builders and added to the descriptor:

```java
pipeline.pVertexInputState = input.get();
pipeline.pInputAssemblyState = assembly.get();
pipeline.pViewportState = viewport.get();
pipeline.pRasterizationState = raster.get();
pipeline.pDepthStencilState = depth.get();
pipeline.pColorBlendState = blend.get();
```

We then populate the array of programmable shader stages:

```java
if(!shaders.containsKey(VkShaderStage.VERTEX)) throw new IllegalStateException("No vertex shader specified");
pipeline.stageCount = shaders.size();
pipeline.pStages = StructureHelper.first(shaders.values(), VkPipelineShaderStageCreateInfo::new, ShaderStageBuilder::populate);
```

Finally we invoke the API to instantiate the pipeline:

```java
// Allocate pipeline
final VulkanLibrary lib = dev.library();
final Pointer[] pipelines = lib.factory().array(1);
check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

// Create pipeline
return new Pipeline(pipelines[0], dev, layout);
```

The pipeline API looks like this:

```java
interface VulkanLibraryPipeline {
    int vkCreatePipelineLayout(Handle device, VkPipelineLayoutCreateInfo pCreateInfo, Handle pAllocator, PointerByReference pPipelineLayout);
    void vkDestroyPipelineLayout(Handle device, Handle pipelineLayout, Handle pAllocator);

    int vkCreateGraphicsPipelines(Handle device, Handle pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Handle pAllocator, Pointer[] pPipelines);
    void vkDestroyPipeline(Handle device, Handle pipeline, Handle pAllocator);
}
```

Note that Vulkan supports creation of multiple pipelines in one operation but we restrict the code to a single instance for the moment.

---

## Integration

### Vertex Shader

The vertex shader hard-codes the triangle vertices and passes the colour for each vertex through to the fragment shader:

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

We also need to compile the shaders to SPIV using the `GLSLC` utility application provided as part of the Vulkan SDK:

```
cd JOVE/src/test/resources/demo/triangle
glslc triangle.frag -o spv.triangle.frag
glslc triangle.vert -o spv.triangle.vert
```

### Configuration

We implement a new configuration class to load the shaders:

```java
@Configuration
class PipelineConfiguration {
    @AutoWired private LogicalDevice dev;

    @Bean
    public Shader vertex() throws IOException {
        return Shader.load(dev, new FileInputStream("./src/test/resources/demo/triangle/spv.triangle.vert"));
    }

    @Bean
    public Shader fragment() throws IOException {
        return Shader.load(dev, new FileInputStream("./src/test/resources/demo/triangle/spv.triangle.frag"));
    }
}
```

And we can finally configure the graphics pipeline:

```java
@Bean
PipelineLayout pipelineLayout() {
    return new PipelineLayout.Builder(dev).build();
}

@Bean
public Pipeline pipeline(RenderPass pass, Swapchain swapchain, Shader vertex, Shader fragment, PipelineLayout layout) {
    return new Pipeline.Builder()
        .layout(layout)
        .pass(pass)
        .viewport(swapchain.extents())
        .shader(VkShaderStage.VERTEX)
            .shader(vertex)
            .build()
        .shader(VkShaderStage.FRAGMENT)
            .shader(fragment)
            .build()
        .build(dev);
}
```

We also add a convenience factory method to the pipeline that creates a single viewport and scissor with the same rectangle (the most common case).

Phew!

---

## Summary

In this chapter we implemented a nested builder to configure the graphics pipeline and shader modules.

