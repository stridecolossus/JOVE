---
title: Terrain Tesselation
---

---

## Contents

- [Overview](#overview)
- [Terrain Grid](#terrain-grid)
- [Tesselation](#tesselation)
- [Enhancements](#enhancements)

---

## Overview

During the course of this blog we have implemented the majority of the basic Vulkan components that would be used by most applications, and have reached the end of the Vulkan tutorial.  From here on we will be implementing more advanced features such as lighting, shadows, etc.

Later we will also address the complexity caused by the number of objects that are currently required to render each element of a scene (descriptor sets, render sequences, pools, etc) by the introduction of a _scene graph_ and supporting framework.

In this first chapter we will start a new demo to render a terrain model derived from a height-map image, and then implement level of detail (LOD) functionality for the grid mesh using a _tesselation_ shader.

This will require the following new components:

* A model builder for a terrain grid.

* The tesselation pipeline stage.

* A new method to lookup pixel data from a height-map image.

We will also introduce the following supporting features and improvements:

* A builder for _specialisation constants_ to parameterise shader configuration.

* Enhancements to the pipeline builder to support _pipeline derivation_ for similar rendering use-cases.

* A _pipeline cache_ to improve the time taken to create pipelines.

This shader code in this chapter is heavily based on the Vulkan Cookbook and the [Vulkan Samples](https://github.com/KhronosGroup/Vulkan-Samples/blob/master/samples/api/terrain_tessellation) example.

---

## Terrain Grid

### Grid Builder

Before we introduce tesselation we will first render a static terrain model as a baseline for the demo application.

We start with a new model builder that constructs a _grid_ of _quads_ in the X-Z plane:

```java
public class GridBuilder {
    private Dimensions size = new Dimensions(4, 4);
    private Dimensions tile = new Dimensions(1, 1);
    private float scale = 1;
    private HeightFunction height = HeightFunction.literal(0);
    private Primitive primitive = Primitive.TRIANGLES;
    private IndexFactory index;

    public MutableModel build() {
        ...
    }
}
```

Where:

* _size_ is the dimensions of the grid, i.e. the number of vertices in each direction.

* _tile_ is the dimensions of each quad comprising the grid scaled by the _scale_ parameter.

* And _height_ generates the height (Y coordinate) of each vertex.

* The purpose of the `IndexFactory` is covered below.

The _height function_ is a simple interface that determines the height of a grid vertex:

```java
@FunctionalInterface
public interface HeightFunction {
    /**
     * Calculates the height at the given coordinates.
     * @param row Row
     * @param col Column
     * @return Height
     */
    float height(int x, int y);
}
```

We provide a default implementation for a constant value:

```java
static HeightFunction literal(float height) {
    return (x, y) -> height;
}
```

Note that the builder uses this implementation to set the height of all vertices to zero by default.

The grid is centred on the origin of the model so we first calculate the _half distances_ of the vertices relative to the origin:

```java
public MutableModel build() {
    int w = size.width();
    int h = size.height();
    float dx = tile.width() * scale * (w - 1) / 2;
    float dz = tile.height() * scale * (h - 1) / 2;

    ...

    return model;
}
```

Next the builder iterates through the grid to generate the vertices (in row major order):

```java
List<Vertex> vertices = new ArrayList<>();
for(int row = 0; row < h; ++row) {
    for(int col = 0; col < w; ++col) {
        ...
    }
}
```

The position and height of each vertex is determined as follows:

```java
float x = col * tile.width() * scale - dx;
float z = row * tile.height() * scale - dz;
float y = height.height(col, row);
Point pos = new Point(x, y, z);
```

And the texture coordinate is simply mapped from the row and column indices:

```java
Coordinate coord = new Coordinate2D((float) col / w, (float) row / h);
```

Finally we create the vertex:

```java
Vertex vertex = new Vertex(pos, coord);
vertices.add(vertex);
```

### Index Factory

To generate an index for the model the following factory abstraction is introduced:

```java
@FunctionalInterface
public interface IndexFactory {
    /**
     * Generates indices for a primitives in this strip.
     * @param index Primitive index
     * @param count Number of quads in the strip
     * @return Indices
     */
    IntStream indices(int index, int count);

    /**
     * Generates indices for a strip.
     * @param start Starting index
     * @param count Number of quads in this strip
     * @return Strip indices
     */
    default IntStream strip(int start, int count) {
        return IntStream
            .range(0, count)
            .flatMap(n -> indices(start + n, count));
    }
}
```

This interface is implemented in a new helper class to generate an index for a _list_ of triangles:

```java
public final class Triangle {
    public static final IndexFactory INDEX_TRIANGLES = (index, count) -> {
        int next = index + count + 1;
        return IntStream.of(
            index, next, index + 1,
            next, next + 1, index + 1
        );
    };
}
```

Note that the generated index is comprised of two counter-clockwise triangles per quad (similar to the cube demo).

A triangle _strip_ has a slightly different implementation:

```java
public static final IndexFactory INDEX_STRIP = new IndexFactory() {
    @Override
    public IntStream indices(int index, int count) {
        return IntStream.of(index, index + count + 1);
    }

    @Override
    public IntStream strip(int start, int count) {
        return IntStream
            .rangeClosed(0, count)
            .flatMap(n -> indices(start + n, count));
    }
};
```

Here the default `strip` method is overridden to append the final two indices of the triangle strip in the same manner as we did way back in the [Cube Demo](/JOVE/blog/part-3-cube/textures) chapter.  

Finally the primitive enumeration is modified to provide an index factory where appropriate:

```java
public enum Primitive {
    TRIANGLES(3, VkPrimitiveTopology.TRIANGLE_LIST, Triangle.INDEX_TRIANGLES),
    TRIANGLE_STRIP(3, VkPrimitiveTopology.TRIANGLE_STRIP, Triangle.INDEX_STRIP),
    ...

    public Optional<IndexFactory> index() {
        return Optional.ofNullable(index);
    }
}
```

The remaining primitives return an empty index factory, the purpose of this addition should become clear shortly.

### Grid Index

The grid builder will support the following use-cases for the index:

1. The general case for an indexed model where the index is generated by the configured factory.

2. A more specialised case for an unindexed grid containing duplicate vertices that are _looked up_ via the index factory of the model primitive (where the configured index is `null`).

3. And the degenerate case for a grid of points (where both the index is `null` and the primitive does not provide an index factory).

This is slightly convoluted but provides the flexibility to configure the behaviour of the index generation used later.

Note that the setter for the _index_ property of the builder accept a `null` value.

The builder first initialises the grid model:

```java
MutableModel model = new MutableModel(primitive, List.of(Point.LAYOUT, Coordinate2D.LAYOUT));
```

The index is then generated for the above use cases as follows:

```java
if(index == null) {
    Optional<IndexFactory> factory = primitive.index();
    if(factory.isPresent()) {
        // Build unindexed model with duplicate vertices
        buildIndex(factory.get()).mapToObj(vertices::get).forEach(model::add);
    }
    else {
        // Otherwise assume point or patch grid
        vertices.forEach(model::add);
    }
}
else {
    // Build indexed model with the configured factory
    vertices.forEach(model::add);
    buildIndex(index).forEach(model::add);
}
```

The `buildIndex` helper invokes the factory to generate a strip for each row of the grid:

```java
private IntStream buildIndex(IndexFactory factory) {
    int w = size.width() - 1;
    return IntStream
        .range(0, size.height() - 1)
        .map(row -> row * size.height())
        .flatMap(start -> factory.strip(start, w));
}
```

### Height Maps

To generate height data for the terrain grid we will load a _height map_ image.  Although the height of a given vertex can easily be sampled from the image there are several cases where an application will need to programatically 'sample' the height-map, e.g. to generate surface normals.

Therefore we essentially emulate a texture sampler by implementing a height function that looks up a pixel from an image.  In any case the image class should support the ability to retrieve pixel data for other use cases.

The height-map function is created using a new factory method:

```java
/**
 * Creates a height function based on a height-map image (i.e. equivalent to a texture sampler).
 * @param size          Grid dimensions
 * @param image         Image
 * @param component     Component channel index for height values
 * @param scale         Height scalar
 * @return Image height function
 * @throws IllegalArgumentException if the component index is invalid for the given image
 */
static HeightFunction heightmap(Dimensions size, ImageData image, int component, float scale) {
}
```

This function first maps the grid dimensions to those of the image:

```java
Dimensions dim = image.extents().size();
float w = dim.width() / size.width();
float h = dim.height() / size.height();
```

Next a scalar is calculated to normalise and scale the pixel values:

```java
float normalise = scale / MathsUtil.unsignedMaximum(Byte.SIZE * image.layout().bytes());
```

Where `unsignedMaximum` calculates the maximum unsigned integer value for a given number of bits:

```java
public static long unsignedMaximum(int bits) {
    return (1L << bits) - 1;
}
```

Finally the pixel coordinate is calculated and the method delegates to a new `pixel` method on the image class:

```java
return (row, col) -> {
    int x = (int) (col * w);
    int y = (int) (row * h);
    return image.pixel(x, y, component) * normalise;
};
```

The implementation of `pixel` for a KTX image is as follows:

```java
public int pixel(int x, int y, int component) {
    int offset = levels.get(0).offset;
    int start = (x + y * extents.size.width()) * layout.length();
    int index = offset + start + (component * layout.bytes());
    return LittleEndianDataInputStream.convert(image, index, layout.bytes());
}
```

Note that the pixel is retrieved from the _first_ MIP level (i.e. the 'full' image) and ignores any array layers.

Since KTX images are little-endian by default the pixel value is transformed using a new utility method:

```java
public static int convert(byte[] bytes, int offset, int len) {
    int value = bytes[offset] & MASK;
    for(int n = 1; n < len; ++n) {
        value = value | (bytes[offset + n] & MASK) << (n * 8);
    }
    return value;
}
```

Finally the KTX loader is improved to handle height-map images with one channel and/or multiple bytes per channel:

* The number of bytes in the image layout is derived from the samples section of the DFD (previously was assumed to be one byte).

* This value is validated against the `typeSize` from the header, which also implicitly ensures that __all__ channels have the same size.

* The format hint is fiddled to a normalised type since some of the images have an integral image format which is not supported by Vulkan samplers, e.g. `R16_UINT`

### Integration

In the new application we retain the following from the previous skybox demo:

* The essential Vulkan components (devices, swapchain, etc).

* The orbital camera controller.

* The uniform buffer for the three matrices.

* The rendering pipeline and render sequence for the model.

* The descriptor sets for the texture sampler.

First we create the terrain grid:

```java
@Bean
public static Model model() {
    return new GridBuilder()
        .size(new Dimensions(64, 64))
        .scale(0.25f)
        .build();
}
```

The vertex shader is the same as the previous demos and the fragment shader simply generates a constant colour for all fragments.  This should render a flat plane since the height of each vertex is zero.

Next we load the height-map image and use it to generate the grid:

```java
@Bean
public static Model model(ImageData heightmap) {
    Dimensions size = new Dimensions(64, 64);
    return new GridBuilder()
        .size(size)
        .scale(0.25f)
        .height(GridBuilder.HeightFunction.heightmap(size, heightmap, 0))
        .build();
}
```

Finally we change the fragment shader to also sample the height-map image:

```glsl
#version 450

layout(binding=0) uniform sampler2D heightMap;
layout(location=0) in vec2 inCoord;
layout(location=0) out vec4 outColour;

void main() {
    outColour = texture(heightMap, inCoord).r;
}
```

The sampler is modified to clamp texture coordinates:

```java
public Sampler sampler() {
    return new Sampler.Builder()
        .wrap(VkSamplerAddressMode.CLAMP_TO_EDGE)
        .build(dev);
}
```

The demo should produce something along the lines of the following:

![Static Terrain](terrain.grid.png)

Note that height-map image is either a gray-scale (i.e. single colour channel) or an RGBA image, in either case the height function and sampler select the __first__ channel of the image (which is why the terrain is red in the image above).

---

## Tesselation

### Overview

Tesselation is the process of generating drawing primitives from an arbitrary geometric object known as a _patch_ which is comprised of a number of _control points_.

In the pipeline this is comprised of three stages:

1. A _tesselation control shader_ that is responsible for transforming patch control points and determining the _tesselation levels_ of the geometry.

2. The fixed-function _tesselator_ which generates multiple primitives by sub-dividing the patch according to the tesselation levels.

3. A _tesselation evaluation shader_ that transforms the vertices of the resultant tesselated geometry.

Note that tesselation is an optional pipeline stage, i.e. the tesselator is enabled when the pipeline contains both shaders.

In the demo we will generate a low-polygon terrain model and employ LOD tesselation to increase the number of vertices dependant on the distance to the camera.

### Terrain Model

The terrain model is a grid of _patches_ with an index specifying each quad of the terrain.

The existing grid builder does not require any modifications, the only changes needed are the addition of the `PATCH` primitive and an index factory that generates a quad-strip:

```java
public final class Quad {
    public static final IndexFactory STRIP = (n, count) -> {
        int next = n + count + 1;
        return IntStream.of(n, next, next + 1, n + 1);
    };
}
```

The model configuration class is modified to generate a grid of patches with quad control points:

```java
public static Model model() {
    return new GridBuilder()
        .size(new Dimensions(64, 64))
        .scale(0.25f)
        .primitive(Primitive.PATCH)
        .index(Quad.STRIP)
        .build();
}
```

Note that the height function is removed so patch vertices will have a Y (or height value) of zero.

The vertex shader is now largely relegated to passing through the geometry to the tesselation stages:

```glsl
#version 450

layout(location=0) in vec3 inPosition;
layout(location=1) in vec2 inCoord;

layout(location=0) out vec2 outCoord;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    outCoord = inCoord;
}
```

### Control Shader

The control shader determines the amount of tesselation to be performed by setting the built-in `gl_TessLevelInner` and `gl_TessLevelOuter` variables for _each_ control point.

Initially we configure the shader as a simple 'pass-through' implementation that leaves the tesselation levels as the default values:

```glsl
#version 450

layout(location=0) in vec2 inCoord[];

layout(vertices=4) out;

layout(location=0) out vec2 outCoord[4];

void main() {
    if(gl_InvocationID == 0) {
        gl_TessLevelInner[0] = 1.0;
        gl_TessLevelInner[1] = 1.0;
        gl_TessLevelOuter[0] = 1.0;
        gl_TessLevelOuter[1] = 1.0;
        gl_TessLevelOuter[2] = 1.0;
        gl_TessLevelOuter[3] = 1.0;
    }
    
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
    outCoord[gl_InvocationID] = inCoord[gl_InvocationID];
}
```

Notes:

* The `layout(vertices=4) out` declaration specifies the number of control points to be processed by the shader (a quad in this case).

* Note that this shader is executed for each vertex but the tesselation levels are generally only calculated once per primitive.  It is common practice to the wrap the calculation of the tesselation levels in the conditional statement using the built-in GLSL `gl_InvocationID` variable.

Configuration of the tesselation stage in the pipeline is very trivial:

```java
public class TesselationPipelineStageBuilder ... {
    private int points;

    /**
     * Sets the number of patch control points.
     * @param points Number of control points
     */
    public TesselationPipelineStageBuilder points(int points) {
        this.points = points;
        return this;
    }

    @Override
    VkPipelineTessellationStateCreateInfo get() {
        if(points == 0) {
            return null;
        }

        var info = new VkPipelineTessellationStateCreateInfo();
        info.patchControlPoints = points;
        return info;
    }
}
```

### Evaluation Shader

After the fixed function tesselator stage has generated the geometry the evaluation shader is executed to transform each vertex of the tesselated primitives.

For the terrain demo the shader implementation will perform the following:

1. Interpolate the position and texture coordinate of the tesselated vertex.

2. Sample the height-map to determine the height of the vertex.

3. Apply perspective projection.

Note that obviously for this first attempt (with tesselation essentially disabled) the results of the interpolation should simply map one-to-one to the grid vertices.

The shader starts with a layout declaration that specifies the structure of the incoming geometry generated by the previous stages:

```glsl
layout(quads, equal_spacing, ccw) in;
```

This layout specifies that each patch is a quad with a counter-clockwise winding order (matching the index factory used above).  The purpose of the spacing argument is documented in [Tesselator Spacing](https://www.khronos.org/registry/vulkan/specs/1.0-wsi_extensions/html/chap22.html#tessellation-tessellator-spacing).

Next the shader declares the required resources and the input/output data:

```glsl
layout(location=0) in vec2 inCoords[];

layout(set=0, binding=0) uniform sampler2D heightMap;

layout(set=0, binding=1) uniform UniformBuffer {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(location=0) out vec3 outPosition;
layout(location=1) out vec2 outCoord;
```

Note that the incoming texture coordinates are an _array_ of the four vertices of each quad.

The shader first interpolates the texture coordinate along the left and right-hand edges of each quad.  The indices in the terrain model have a counter-clockwise winding order, so for a quad with the following vertices and index:

```
A - B   0 - 3   
| / |   | / |   
C - D   1 - 2   
```

The left edge is A-C and the right-hand is B-D when viewed from above the terrain (i.e. the negative Y direction).  To ensure the resultant vertices have the correct winding order the interpolation is performed in the negative Z direction.  The two edges therefore map to indices 1,0 and 2,3 respectively.

The `mix` function performs this interpolation parameterised by the built-in `gl_TessCoord` texture coordinate generated by the tesselator:

```glsl
void main() {
vec2 coords1 = mix(inCoords[1], inCoords[0], gl_TessCoord.x);
vec2 coords2 = mix(inCoords[2], inCoords[3], gl_TessCoord.x);
```

These values are then interpolated again in the other direction to calculate the final texture coordinate:

```glsl
vec2 coord = mix(coords1, coords2, gl_TessCoord.y);
```

The vertex position is interpolated similarly but in this case using the built-in `gl_in` positions array generated by the tesselator.

The height of each vertex is then sampled from the red channel of the height-map (as we did previously):

```glsl
pos.y +=texture(heightMap, coord).r * 2.5;
```

And finally the shader applies perspective projection and outputs the resultant vertex:

```glsl
gl_Position = projection * view * model * pos;
outPosition = pos.xyz;
outCoord = coord;
```

The complete evaluation shader is as follows:

```glsl
#version 450

layout(quads, equal_spacing, ccw) in;

layout(location=0) in vec2 inCoord[];

layout(set=0, binding=0) uniform sampler2D heightMap;

layout(set=0, binding=1) uniform UniformBuffer {
    mat4 model;
    mat4 view;
    mat4 projection;
};

layout(location=0) out vec2 outCoord;

void main() {
    // Interpolate texture coordinate
    vec2 coords1 = mix(inCoord[1], inCoord[0], gl_TessCoord.x);
    vec2 coords2 = mix(inCoord[2], inCoord[3], gl_TessCoord.x);
    vec2 coord = mix(coords1, coords2, gl_TessCoord.y);

    // Interpolate position
    vec4 pos1 = mix(gl_in[1].gl_Position, gl_in[0].gl_Position, gl_TessCoord.x);
    vec4 pos2 = mix(gl_in[2].gl_Position, gl_in[3].gl_Position, gl_TessCoord.x);
    vec4 pos = mix(pos1, pos2, gl_TessCoord.y);

    // Lookup vertex height
    pos.y += texture(heightMap, coord).r * 2.5;

    // Output vertex
    gl_Position = projection * view * model * pos;
    outCoord = coord;
}
```

### Integration

The pipeline configuration is modified to include the two new shaders and the tesselation stage:

```java
public Pipeline pipeline(...) {
    return new Pipeline.Builder()
        ...
        .shader(VkShaderStage.TESSELLATION_CONTROL)
            .shader(control)
            .build()
        .shader(VkShaderStage.TESSELLATION_EVALUATION)
            .shader(evaluation)
            .build()
        .tesselation()
            .points(4)
            .build()
        .rasterizer()
            .polygon(VkPolygonMode.LINE)
            .build()
        ...
        .build();
```

The rasterizer stage is also configured to render the geometry as a wire-frame which should help to diagnose how well (or not) the tesselation shaders are working.  Note that this requires the `fillModeNonSolid` device feature to be enabled.

The binding for the sampler is configured to be available to the relevant pipelines stages:

```java
public class DescriptorConfiguration {
    private final Binding samplerBinding = new Binding.Builder()
        .binding(0)
        .type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
        .stage(VkShaderStage.TESSELLATION_CONTROL)
        .stage(VkShaderStage.TESSELLATION_EVALUATION)
        .stage(VkShaderStage.FRAGMENT)
        .build();
}
```

Finally the pipeline layout is configured to make the uniform buffer available to the tesselation evaluation stage only.

The fragment shader is the same as the previous iteration.

If all goes well the demo should render the same terrain as the previous iteration but as a wire-frame:

![Wireframe Terrain](terrain.wireframe.png)

However there is plenty going on behind the scenes when using tesselation shaders with several failure cases:

The configuration of the grid is spread across several locations which must all correspond:
1. The number of control points is specified in the tesselation pipeline stage (4 for quads).
2. Which must obviously match the drawing primitive and index factory.
3. And the `quads` layout declaration in the evaluation shader.

Similarly the primitive winding order is:
1. Implicit in the index factory for the quad strip (counter-clockwise).
2. Explicitly declared by the `ccw` property of the layout for the evaluation shader.
3. And dictates the interpolation logic for the vertices (as illustrated above).

The RenderDoc debugger (see below) can be very useful here to inspect the vertex data generated by the tesselator.  We also used the fake texture coordinate trick used in the [Textures](JOVE/blog/part-3-cube/textures) chapter to test the generated vertex data.

### Level of Detail

The final step is to calculate the LOD tesselation levels for each quad to increase the number of vertices as the model nears the camera.

Within the conditional statement the control shader first samples the height for each vertex and then calculates the __squared__ distance from the camera:

```glsl
float distance[4];
for(int n = 0; n < 4; ++n) {
    float h = texture(heightMap, inCoord[n]).r;
    vec4 pos = view * model * (gl_in[gl_InvocationID].gl_Position + vec4(0.0, h, 0.0, 0.0));
    distance[n] = dot(pos, pos);
}
```

Note this requires the shader to have access to the sampler and the uniform buffer.

Next the _outer_ tesselation levels for each _edge_ of the quad are calculated as follows:

```glsl
gl_TessLevelOuter[0] = factor(distance[3], distance[0]);
gl_TessLevelOuter[1] = factor(distance[0], distance[1]);
gl_TessLevelOuter[2] = factor(distance[1], distance[2]);
gl_TessLevelOuter[3] = factor(distance[2], distance[3]);
```

Where `factor` is a simple helper method:

```glsl
float factor(float a, float b) {
    float dist = min(a, b);
    return max(1.0, 20.0 - dist);
}
```

Since we want the tesselation factor to increase with decreasing distance we need to invert the calculated factor.  The hard-coded value of 20 exaggerates the tesselation for the demo, a real-world terrain shader would use a much more subtle formula.

The _inner_ tesselation levels are then interpolated from the outer levels:

```glsl
gl_TessLevelInner[0] = mix(gl_TessLevelOuter[0], gl_TessLevelOuter[3], 0.5);
gl_TessLevelInner[1] = mix(gl_TessLevelOuter[2], gl_TessLevelOuter[1], 0.5);
```

With the wireframe pipeline still in place the demo should now render an increasing polygon count for the parts of the terrain that are closer to the camera:

![Tesselated Terrain](terrain.tesselated.png)

As a final aesthetic change the fragment shader is modified to generate a colour dependant on the height of each vertex:

```glsl
void main() {
    const vec4 green = vec4(0.2, 0.5, 0.1, 1.0);
    const vec4 brown = vec4(0.6, 0.5, 0.2, 1.0);
    const vec4 white = vec4(1.0);

    float height = texture(heightMap, inCoord).r;
    vec4 col = mix(green, brown, smoothstep(0.0, 0.4, height));
    outColour = mix(col, white, smoothstep(0.6, 0.9, height));
}
```

This should render lower vertices as green, progressing to brown as the height increases, and white for the highest values (based on the example in the Vulkan Cookbook).

Although the effect of the tesselation is less visible without the wireframe the filled terrain (with an exaggerated height scale) looks something like this:

![Tesselated Terrain Filled](terrain.filled.png)

Note that the shader code presented in this chapter is just about the simplest implementation to illustrate tesselation, there are many improvements that could be made to improve rendering quality and performance.  In particular the filled terrain model will exhibit gaps due to adjacent triangles having different tesselation levels.

---

## Enhancements

### Derived Pipelines

The wireframe terrain model is useful for visually testing the tesselation shader but we would like to be able to toggle between filled and wireframe modes.  This implies the following new requirements:

1. The _polygon mode_ is a property of the rasterizer pipeline stage which means the demo needs _two_ pipelines to switch between modes.

2. However both pipelines will be identical except for the polygon mode, ideally we would prefer to _override_ this property in the builder to avoid having to repeat all the common configuration.

3. We note that Vulkan supports the creation of multiple pipelines in one operation but the current implementation is limited to a single pipeline.

4. Additionally Vulkan supports _derivative_ pipelines which provide a hint to the hardware that a derived (or child) pipeline shares common properties with its parent, potentially improving performance when pipelines are instantiated and when switching pipeline bindings in the render sequence.

To support these requirements we will add functionality to the pipeline builder to support derivative pipelines and refactor the existing code to allow configuration to be overridden.

A pipeline that allows derivatives (i.e. the parent) is identified by a flag at instantiation-time:

```java
public static class Builder {
    private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
    private Handle baseHandle;

    ...

    public Builder flag(VkPipelineCreateFlag flag) {
        this.flags.add(flag);
        return this;
    }
    
    public Builder allowDerivatives() {
        return flag(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
    }
}
```

Note that the set of `flags` is also added to the pipeline domain object.

A pipeline derived from an _existing_ parent is specified by the following new method:

```java
public Builder derive(Pipeline base) {
    if(!base.flags().contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) {
        throw new IllegalArgumentException(...);
    }
    baseHandle = base.handle();
    derivative(this);
    return this;
}
```

Where `derivative` is a trivial helper:

```java
private static void derivative(Builder builder) {
    builder.flags.add(VkPipelineCreateFlag.DERIVATIVE);
}
```

The base pipeline is populated in the `build` method:

```java
info.basePipelineHandle = baseHandle;
```

Vulkan provides a second method to derive a pipeline by _index_ within an array of pipelines created in a single operation.  We first implement a new static factory method to create multiple pipelines:

```java
public static List<Pipeline> build(List<Builder> builders, DeviceContext dev) {
    // Build array of descriptors
    VkGraphicsPipelineCreateInfo[] array = StructureHelper.array(builders, VkGraphicsPipelineCreateInfo::new, Builder::populate);

    // Allocate pipelines
    VulkanLibrary lib = dev.library();
    Pointer[] handles = new Pointer[array.length];
    check(lib.vkCreateGraphicsPipelines(dev, null, array.length, array, null, handles));

    // Create pipelines
    return IntStream
        .range(0, array.length)
        .mapToObj(n -> create(handles[n], list.get(n), dev))
        .collect(toList());
}
```

Where `create` is a local helper to instantiate each pipeline:

```java
private static Pipeline create(Pointer handle, Builder builder, DeviceContext dev) {
    return new Pipeline(handle, dev, builder.layout, builder.flags);
}
```

The code in the existing `build` method that constructs a pipeline descriptor is wrapped up into the `populate` helper, and the method is refactored using the new factory as a convenience for constructing a single pipeline.

Next the pipeline builder is extended to support derivation of a pipeline during construction:

```java
public static class Builder {
    private Builder base;
    private int baseIndex = -1;

    public Builder derive() {
        // Validate
        if(!flags.contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) throw new IllegalStateException(...);
    
        // Create derived builder
        Builder builder = new Builder();
        derivative(builder);
        builder.base = this;
        
        // Clone pipeline properties
        ...
        
        return builder;
    }
}
```

Note that this method creates a __new__ builder for the derivative pipeline with a reference to the its parent.

In the new build method we populate the `basePipelineIndex` in the pipeline descriptor by looking up the index from the array.  As a convenience we first ensure that the given list of builders also contains the parents:

```java
public static List<Pipeline> build(List<Builder> builders, PipelineCache cache, DeviceContext dev) {
    List<Builder> list = new ArrayList<>(builders);
    builders
        .stream()
        .map(b -> b.base)
        .filter(Objects::nonNull)
        .filter(Predicate.not(list::contains))
        .forEach(list::add);
            
    ...
}
```

Next the index of the parent is determined for derived pipelines:

```java
for(Builder b : list) {
    if(b.base != null) {
        b.baseIndex = list.indexOf(b.base);
        assert b.baseIndex >= 0;
    }
}
```

And finally the index is populated in the descriptor:

```java
if(base != null) {
    if(baseHandle != null) throw new IllegalArgumentException(...);
    assert baseIndex >= 0;
}
info.basePipelineHandle = baseHandle;
info.basePipelineIndex = baseIndex;
```

Note that the two mechanisms for deriving a pipeline are mutually exclusive.

To allow the properties of a pipeline to be overridden the `derive` method clones from the parent builder:

```java
public Builder derive() {
    ...
    
    // Clone pipeline properties
    builder.layout = layout;
    builder.pass = pass;
    builder.flags.addAll(flags);

    // Clone pipeline stages
    for(ShaderStageBuilder b : shaders.values()) {
        builder.shaders.put(b.stage, new ShaderStageBuilder(b));
    }
    builder.input.init(input);
    builder.assembly.init(assembly);
    ...

    return builder;
}
```

The `init` method is added to the nested pipeline stage builders to clone the configuration.  We are also forced to refactor some of the nested builders that previously operated directly on the underlying Vulkan descriptor to support cloning (since JNA structures cannot easily be copied).

For the demo we now have several alternatives available to create the pipelines.  We opt to derive the wireframe pipeline and exercise the code that instantiates multiple pipelines:

```java
@Bean
public List<Pipeline> pipelines(...) {
    // Init main pipeline
    var pipeline = new Pipeline.Builder()
    ...

    // Derive wireframe pipeline
    var wireframe = pipeline
        .derive()
        .rasterizer()
            .polygon(VkPolygonMode.LINE)
            .build();

    // Build pipelines
    return Pipeline.Builder.build(List.of(pipeline, wireframe), dev);
}
```

The configuration class for the render sequence is next modified as follows:
* Inject the _list_ of pipelines.
* Add an index member.
* Add a toggle handler that flips the index.
* Expose the handler as a bean.
* Bind it to the space bar.

Since the render sequence is recorded per frame we can now switch the pipeline at runtime.  Cool.

### Pipeline Cache

A _pipeline cache_ stores the results of pipeline construction and can be reused between pipelines and between runs of an application, allowing the hardware to possibly optimise pipeline construction.  

The domain object is relatively trivial:

```java
public class PipelineCache extends AbstractVulkanObject {
    @Override
    protected Destructor<PipelineCache> destructor(VulkanLibrary lib) {
        return lib::vkDestroyPipelineCache;
    }
}
```

A cache object is instantiated using a factory method:

```java
public static PipelineCache create(DeviceContext dev, byte[] data) {
    // Build create descriptor
    VkPipelineCacheCreateInfo info = new VkPipelineCacheCreateInfo();
    if(data != null) {
        info.initialDataSize = data.length;
        info.pInitialData = BufferHelper.buffer(data);
    }

    // Create cache
    VulkanLibrary lib = dev.library();
    PointerByReference ref = dev.factory().pointer();
    check(lib.vkCreatePipelineCache(dev, info, null, ref));

    // Create domain object
    return new PipelineCache(ref.getValue(), dev);
}
```

Where `data` is the previously persisted cache (the data itself is platform specific).

The following method retrieves the cache data after construction of the pipeline (generally before application termination):

```java
public ByteBuffer data() {
    DeviceContext dev = super.device();
    VulkanFunction<ByteBuffer> func = (count, data) -> dev.library().vkGetPipelineCacheData(dev, this, count, data);
    IntByReference count = dev.factory().integer();
    return VulkanFunction.invoke(func, count, BufferHelper::allocate);
}
```

Caches can also be merged such that a single instance can be reused across multiple pipelines:

```java
public void merge(Collection<PipelineCache> caches) {
    DeviceContext dev = super.device();
    VulkanLibrary lib = dev.library();
    check(lib.vkMergePipelineCaches(dev, this, caches.size(), NativeObject.array(caches)));
}
```

Finally we add a new API library for the cache:

```java
int  vkCreatePipelineCache(DeviceContext device, VkPipelineCacheCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineCache);
int  vkMergePipelineCaches(DeviceContext device, PipelineCache dstCache, int srcCacheCount, Pointer pSrcCaches);
int  vkGetPipelineCacheData(DeviceContext device, PipelineCache cache, IntByReference pDataSize, ByteBuffer pData);
void vkDestroyPipelineCache(DeviceContext device, PipelineCache cache, Pointer pAllocator);
```

To persist a cache we implement a simple loader:

```java
public static class Loader implements ResourceLoader<InputStream, PipelineCache> {
    private final DeviceContext dev;

    @Override
    public InputStream map(InputStream in) throws IOException {
        return in;
    }

    @Override
    public PipelineCache load(InputStream in) throws IOException {
        byte[] data = in.readAllBytes();
        return create(dev, data);
    }

    public void write(PipelineCache cache, OutputStream out) throws IOException {
        byte[] array = BufferHelper.array(cache.data());
        out.write(array);
    }
}
```

TODO - cache manager, file source helper, integration

### Specialisation Constants

The new shaders contain a number of hard coded parameters (such as the tesselation factor and height scalar) which would ideally be programatically configured (possibly from a properties file).

Additionally in general we would like to centralise common or shared parameters to avoid hard-coding the same information in multiple locations or having to replicate shaders with different parameters.

Vulkan provides _specialisation constants_ for these requirements which are be used to parameterise a shader when it is instantiated.

The descriptor for a set of specialisation constants is constructed using a new factory method on the shader class:

```java
public static VkSpecializationInfo constants(Map<Integer, Object> constants) {
    // Skip if empty
    if(constants.isEmpty()) {
        return null;
    }

    var info = new VkSpecializationInfo();
    ...

    return info;
}
```

Where _constants_ is a map of arbitrary values indexed by identifier.

The method first transforms the map to a list:

```java
List<Constant> table = constants.entrySet().stream().map(Constant::new).collect(toList());
```

Where each constant is an instance of a local class constructed from each map entry:

```java
class Constant {
    private final int id;
    private final Object value;
    private final int size;
    private int offset;

    private Constant(Entry<Integer, Object> entry) {
        this.id = entry.getKey();
        this.value = entry.getValue();
        this.size = size();
    }
}
```

The _size_ of each constant is determined as follows:

```java
private int size() {
    if(value instanceof Integer) {
        return Integer.BYTES;
    }
    else
    if(value instanceof Float) {
        return Float.BYTES;
    }
    else
    if(value instanceof Boolean) {
        return Integer.BYTES;
    }
    else {
        throw new IllegalArgumentException(...);
    }
}
```

Notes:

* Only scalar and boolean values are supported.

* Booleans are represented as integer values.

The _offset_ of each constant within the data buffer is determined by the following simple loop (which also calculates the total length of the buffer):

```java
int size = 0;
for(Constant e : table) {
    e.offset = size;
    size += e.size;
}
```

Each constant has a descriptor:

```java
var info = new VkSpecializationInfo();
info.mapEntryCount = constants.size();
info.pMapEntries = StructureHelper.pointer(table, VkSpecializationMapEntry::new, Constant::populate);
```

Which is populated from the local class:

```java
private void populate(VkSpecializationMapEntry entry) {
    entry.constantID = id;
    entry.offset = offset;
    entry.size = size;
}
```

Finally the data buffer for the specialisation constants is allocated and filled:

```java
info.dataSize = size;
info.pData = BufferHelper.allocate(size);
for(Constant entry : table) {
    entry.append(info.pData);
}
```

Where `append` is another helper on the local class:

```java
private void append(ByteBuffer buffer) {
    if(value instanceof Integer n) {
        buffer.putInt(n);
    }
    else
    if(value instanceof Float f) {
        buffer.putFloat(f);
    }
    else
    if(value instanceof Boolean b) {
        final VulkanBoolean bool = VulkanBoolean.of(b);
        buffer.putInt(bool.toInteger());
    }
    else {
        assert false;
    }
}
```

The specialisation constants are applied to a shader during pipeline configuration:

```java
public class ShaderStageBuilder {
    private VkSpecializationInfo constants;
    
    ...
    
    public ShaderStageBuilder constants(VkSpecializationInfo constants) {
        this.constants = notNull(constants);
        return this;
    }
 
    void populate(VkPipelineShaderStageCreateInfo info) {
        ...
        info.pSpecializationInfo = constants;
    }
}
```

For example in the evaluation shader we can replace the hard-coded height scale with the following constant declaration:

```glsl
layout(constant_id=1) const float HeightScale = 2.5;
```

Note that the constant also has a default value if it is not explicitly configured by the application.

The set of constants used in both tesselation shaders is initialised in the pipeline configuration class:

```java
class PipelineConfiguration {
    private final VkSpecializationInfo constants = Shader.constants(Map.of(0, 20f, 1, 2.5f));
}
```

Finally the relevant shaders are parameterised when the pipeline is constructed, for example:

```java
shader(VkShaderStage.TESSELLATION_EVALUATION)
    .shader(evaluation)
    .constants(constants)
    .build()
```

### Render Doc

Testing the tesselation shader and diagnosing any problems can be quite difficult since there is a lot of magic going on behind the scenes.  For this reason we took the time to integrate with the excellent (and free) [RenderDoc](https://renderdoc.org/) graphics debugger for Vulkan, OpenGL, etc.

For a Java application the simplest way to use this tool is to load the native library:

```java
System.loadLibrary("renderdoc");
```

The application is attached to the debugger via `File > Running Instance` and frames can then be captured and inspected.  In particular the _mesh viewer_ is useful for checking the vertex data generated by the tesselator.

Notes:

* The library must be present on the Java library path.

* Alternatively RenderDoc can be configured to launch the application but this requires specifying fiddly Java commands, classpath, arguments, etc. and generally implies that the application is also packaged (as a JAR file) which is less convenient.

* The native library is essentially a validation layer which also displays a useful overlay on the rendering surface.

---

## Summary

In this chapter we rendered a terrain grid using tesselation shaders by implementation of the following:

* A model builder for the terrain grid.

* The tesselation pipeline stage.

* Derived pipelines.

* A persistent pipeline cache.

* Support for specialisation constants.

