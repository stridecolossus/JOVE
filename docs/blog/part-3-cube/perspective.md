---
title: Perspective Projection
---

## Overview

In this chapter we will introduce _perspective projection_ so that fragments that are more distant in the scene appear correctly foreshortened.

We will implement a _matrix_ which is central to 3D graphics.  Of course we could utilise a third-party library but the point of this project is to build from first principles.

We will require the following new components:

* The _matrix_ class.

* A perspective projection matrix.

* A _uniform buffer_ to pass the matrix to the shader.

We will then build a _model_ for a textured cube and apply a rotation animation requiring:

* A builder for a general model.

* A specific model builder for a cube.

* A rotation matrix.

---

## Perspective Projection

### Enter The Matrix

We first establish some constraints on our design for the matrix class:

* Must be _square_ (same width and height).

* Arbitrary _order_ (or size).

* Immutable.

The first-cut outline for the matrix is as follows:

```java
public final class Matrix implements Bufferable {
    private final float[][] matrix;

    private Matrix(int order) {
        matrix = new float[order][order];
    }

    public int order() {
        return matrix.length;
    }

    public float get(int row, int col) {
        return matrix[row][col];
    }
}
```

The matrix data is implemented as a 2D floating-point array.  This is certainly not the most efficient implementation in terms of memory (since each row is itself an object) but it is the simplest.

A matrix is a bufferable object:

```java
public int length() {
    return matrix.length * matrix.length * Float.BYTES;
}

public void buffer(ByteBuffer buffer) {
    int order = order();
    for(int r = 0; r < order; ++r) {
        for(int c = 0; c < order; ++c) {
            buffer.putFloat(matrix[c][r]);
        }
    }
}
```

To construct a matrix we provide a builder:

```java
public static class Builder {
    private Matrix matrix;

    public Builder(int order) {
        matrix = new Matrix(order);
    }
    
    public Matrix build() {
        try {
            return matrix;
        }
        finally {
            matrix = null;
        }
    }
}
```

The builder is used to set a matrix element:

```java
public Builder set(int row, int col, float value) {
    matrix.matrix[row][col] = value;
    return this;
}
```

The matrix can also be initialised to identity:

```java
public Builder identity() {
    int order = matrix.order();
    for(int n = 0; n < order; ++n) {
        set(n, n, 1);
    }
    return this;
}
```

We will add further matrix functionality as we progress through this chapter.

### Perspective Projection

We can now use the new class to construct a projection matrix defined as follows:

```java
public interface Projection {
    /**
     * Builds the matrix for this projection.
     * @param near      Near plane
     * @param far       Far plane
     * @param dim       Viewport dimensions
     * @return Projection matrix
     */
    Matrix matrix(float near, float far, Dimensions dim);
}
```

A perspective projection is based on the field-of-view (FOV) which is analogous to the focus of a camera.

We add a factory method to create a perspective projection for a given FOV:

```java
static Projection perspective(float fov) {
    return new Projection() {
        private final float scale = 1 / MathsUtil.tan(fov * MathsUtil.HALF);

        @Override
        public Matrix matrix(float near, float far, Dimensions dim) {
            return new Matrix.Builder()
                .set(0, 0, scale / dim.ratio())
                .set(1, 1, -scale)
                .set(2, 2, far / (near - far))
                .set(2, 3, (near * far) / (near - far))
                .set(3, 2, -1)
                .build();
        }
    };
}
```

This code is based on the example from the Vulkan Cookbook.

We also add a convenience constant for a perspective projection with a default 60 degree field-of-view:

```java
Projection DEFAULT = perspective(MathsUtil.toRadians(60));
```

### Uniform Buffers

To pass the matrix to the shader we next implement a _uniform buffer_ which is a descriptor set resource implemented as a general Vulkan buffer.

We add a new factory method to the `VulkanBuffer` class to create a uniform buffer:

```java
public Resource uniform() {
    require(VkBufferUsage.UNIFORM_BUFFER);

    return new Resource() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.UNIFORM_BUFFER;
        }

        @Override
        public void populate(VkWriteDescriptorSet write) {
            var info = new VkDescriptorBufferInfo();
            info.buffer = handle();
            info.offset = 0;
            info.range = len;
            write.pBufferInfo = info;
        }
    };
}
```

The `require` method is an invariant test that throws an exception if the buffer does not support the required usage flag.

To make the process of loading the matrix into the buffer slightly more convenient we add the following helper:

```java
class VulkanBuffer {
    public void load(Bufferable data) {
        Region region = mem.region().orElseGet(mem::map);
        ByteBuffer bb = region.buffer();
        data.buffer(bb);
    }
}
```

### Integration #1

As a first step we will apply an identity matrix to the quad vertices in the demo to test the uniform buffer.

We create a new configuration class for the projection matrix:

```java
@Configuration
public class CameraConfiguration {
    @Bean
    public static Matrix matrix() {
        return Matrix.IDENTITY;
    }
}
```

The matrix is loaded into a uniform buffer:

```java
@Bean
public static VulkanBuffer uniform(LogicalDevice dev, AllocationService allocator, Matrix matrix) {
    MemoryProperties<VkBufferUsage> props = new MemoryProperties.Builder<VkBufferUsage>()
        .usage(VkBufferUsage.UNIFORM_BUFFER)
        .required(VkMemoryProperty.HOST_VISIBLE)
        .required(VkMemoryProperty.HOST_COHERENT)
        .build();

    VulkanBuffer uniform = VulkanBuffer.create(dev, allocator, matrix.length(), props);
    uniform.load(matrix);

    return uniform;
}
```

Note that we are using a buffer that is visible to the host (i.e. the application) which is less efficient than device-local memory.  However we will eventually be updating the matrix every frame to apply a rotation animation so this approach is more logical.

We add a second binding to the descriptor set layout for the uniform buffer:

```java
private final Binding samplerBinding = ...

private final Binding uniformBinding = new Binding.Builder()
    .binding(1)
    .type(VkDescriptorType.UNIFORM_BUFFER)
    .stage(VkShaderStage.VERTEX)
    .build();
```

The new resource is registered with the descriptor set pool:

```java
public Pool pool() {
    final int count = cfg.getFrameCount();
    return new Pool.Builder()
        .add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, count)
        .add(VkDescriptorType.UNIFORM_BUFFER, count)
        .max(count)
        .build(dev);
}
```

And finally we initialise the uniform buffer resource in the `descriptors` bean:

```java
DescriptorSet.set(descriptors, uniformBinding, uniform.uniform());
```

Note that for the moment we are using the same uniform buffer for all descriptor sets since we are not yet modifying the matrix between frames.

To use the uniform buffer we add the following layout declaration to the vertex shader:

```glsl
layout(binding = 1) uniform ubo {
    mat4 matrix;
};
```

The matrix is applied to each vertex:

```glsl
gl_Position = matrix * vec4(inPosition, 1.0);
```

Note that we create a _homogeneous_ vector (consisting of four components) to multiply the vertex position by the matrix.

The vertex shader should now look like this:

```glsl
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform ubo {
    mat4 matrix;
};

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

void main() {
    gl_Position = matrix * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
```

If all goes well we should still see the flat textured quad since the identity matrix essentially changes nothing.

### View Transform

Next we apply a _view transform_ to the demo representing the viewers position and orientation (i.e. the camera).

First we add the following methods to the matrix builder to populate a row or column of the matrix:

```java
public Builder row(int row, Tuple vec) {
    set(row, 0, vec.x);
    set(row, 1, vec.y);
    set(row, 2, vec.z);
    return this;
}

public Builder column(int col, Tuple vec) {
    set(0, col, vec.x);
    set(1, col, vec.y);
    set(2, col, vec.z);
    return this;
}
```

Next we introduce the _vector_ domain object:

```java
public final class Vector extends Tuple {
    public static final Vector X = new Vector(1, 0, 0);
    public static final Vector Y = new Vector(0, 1, 0);
    public static final Vector Z = new Vector(0, 0, 1);

    ...
    
    public Vector negate() {
        return new Vector(-x, -y, -z);
    }
}
```

We can now replace the identity matrix with a perspective projection:

```java
@Bean
public static Matrix matrix(Swapchain swapchain) {
    Matrix projection = Projection.DEFAULT.matrix(0.1f, 100, swapchain.extents());
}
```

Next we construct the view transform matrix which consists of translation and rotation components.

The translation matrix moves the eye position (or camera) one unit out of the screen (or moves the scene one unit into the screen, whichever way you look at it):

```java
Matrix trans = new Matrix.Builder()
    .identity()
    .column(3, new Point(0, 0, -1))
    .build();
```

The rotation matrix is initialised to the three camera axes:

```java
Matrix rot = new Matrix.Builder()
    .row(0, Vector.X)
    .row(1, Vector.Y.invert())
    .row(2, Vector.Z)
    .build();
```

Finally we compose the two view transform matrices to create the view transform which is then multiplied (see below) with the perspective projection:

```java
Matrix view = rot.multiply(trans);
return projection.multiply(view);
```

Notes:

* The Y axis is inverted for Vulkan.

* The Z axis points _out_ from the screen.

* Later we will wrap the above into a camera class.

### Matrix Multiplication

Matrix multiplication is implemented as follows:

```java
public Matrix multiply(Matrix m) {
    // Check same sized matrices
    int order = order();
    if(m.order() != order) throw new IllegalArgumentException(...);

    // Multiply matrices
    Matrix result = new Matrix(order);
    for(int r = 0; r < order; ++r) {
        for(int c = 0; c < order; ++c) {
            float total = 0;
            for(int n = 0; n < order; ++n) {
                total += this.matrix[r][n] * m.matrix[n][c];
            }
            result.matrix[r][c] = total;
        }
    }

    return result;
}
```

Each element of the resultant matrix is the _sum_ of its _row_ multiplied by the corresponding _column_ in the right-hand matrix.

For example, given the following matrices:

```
[a b]   [c .]
[. .]   [d .]
```

The result for element [0, 0] is `a * c + b * d` (and similarly for the rest of the matrix).

### Integration #2

To test the perspective projection we modify the vertex data to moved one edge of the quad into the screen by fiddling the Z coordinate of the right-hand vertex positions:

```java
new Point(-0.5f, +0.5f, 0)
new Point(-0.5f, -0.5f, 0)
new Point(+0.5f, +0.5f, -0.5f)
new Point(+0.5f, -0.5f, -0.5f)
```

If the transformation code is correct we should now see the quad in 3D with the right-hand edge sloping away from the viewer like an open door:

![Perspective Quad](perspective.png)

---

## Cube Model

### Model

We introduce the _model_ class which composes the vertex data and associated properties:

```java
public interface Model {
    Header header();
    Bufferable vertices();
}
```

The _header_ is a descriptor for the properties of the model:

```java
public record Header(List<Layout> layout, Primitive primitive, int count) {
}
```

The _layout_ field specifies the structure of the vertices and _count_ is the draw count of the model.

Rather than fiddling the code-generated `VkPrimitiveTopology` enumeration we implement a wrapper:

```java
public enum Primitive {
    TRIANGLES(3, VkPrimitiveTopology.TRIANGLE_LIST),
    TRIANGLE_STRIP(3, VkPrimitiveTopology.TRIANGLE_STRIP),
    ...

    private final int size;
    private final VkPrimitiveTopology topology;
}
```

The following methods on the new enumeration are used to validate the model in the constructor of the header (not shown):

```java
public boolean isStrip() {
    return switch(this) {
        case TRIANGLE_STRIP, TRIANGLE_FAN, LINE_STRIP -> true;
        default -> false;
    };
}

public boolean isNormalSupported() {
    return switch(this) {
        case TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> true;
        default -> false;
    };
}

public boolean isValidVertexCount(int count) {
    if(isStrip()) {
        return (count == 0) || (count >= size);
    }
    else {
        return (count % size) == 0;
    }
}
```

We create a template implementation which is extended by the following default model class:

```java
public class DefaultModel extends AbstractModel {
    private final List<Vertex> vertices;

    public DefaultModel(Header header, List<Vertex> vertices) {
        super(header);
        this.vertices = List.copyOf(vertices);
    }
}
```

The interleaved vertex buffer is generated from the vertex data as follows:

```java
public Bufferable vertexBuffer() {
    return new Bufferable() {
        private final int len = vertices.size() * Layout.stride(header.layout());

        @Override
        public int length() {
            return len;
        }

        @Override
        public void buffer(ByteBuffer buffer) {
            for(Vertex v : vertices) {
                v.buffer(buffer);
            }
        }
    };
}
```

To construct a model we provide the ubiquitous builder:

```java
public static class Builder {
    private Primitive primitive = Primitive.TRIANGLE_STRIP;
    private final List<Vertex> vertices = new ArrayList<>();

    ...

    public DefaultModel build() {
        Header header = new Header(layout, primitive, vertices.size());
        return new DefaultModel(header, vertices);
    }
}
```

### Cube Builder

We build on the new class to construct a model for a cube:







The cube vertices are specified as a simple array:

```java
private static final Point[] VERTICES = {
    // Front
    new Point(-1, -1, 1),
    new Point(-1, +1, 1),
    new Point(+1, -1, 1),
    new Point(+1, +1, 1),

    // Back
    new Point(+1, -1, -1),
    new Point(+1, +1, -1),
    new Point(-1, -1, -1),
    new Point(-1, +1, -1),
};
```

Each face is comprised of a quad of indices into the vertices array:

```java
private static final int[][] FACES = {
    { 0, 1, 2, 3 }, // Front
    { 4, 5, 6, 7 }, // Back
    { 6, 7, 0, 1 }, // Left
    { 2, 3, 4, 5 }, // Right
    { 6, 0, 4, 2 }, // Top
    { 1, 7, 3, 5 }, // Bottom
};
```

Each face is a quad consisting of two triangles specified by the following constants:

```java
public final class Quad {
    public static final List<Integer> LEFT = List.of(0, 1, 2);
    public static final List<Integer> RIGHT = List.of(2, 1, 3);
    public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);
}
```

Note that the triangles have alternate winding orders (exactly the same as we did for the quad in the previous chapter).

The triangle indices are aggregated into a single array per face:

```java
private static final int[] TRIANGLES = Stream.concat(Quad.LEFT.stream(), Quad.RIGHT.stream()).mapToInt(Integer::intValue).toArray();
```

In the build method we can now construct the vertices for each face and build the model:

```java
public Model build() {
    for(int[] face : FACES) {
        for(int corner : TRIANGLES) {
            // Lookup cube vertex for this triangle
            final int index = face[corner];
            final Point pos = VERTICES[index].scale(size);
    
            // Lookup texture coordinate for this corner
            final Coordinate2D tc = Quad.COORDINATES.get(corner);
    
            // Build vertex
            final Vertex v = new Vertex.Builder()
                .position(pos)
                .coords(tc)
                .build();
    
            // Add quad vertex to model
            builder.add(v);
        }
    }
    return builder.build();
}
```

> We could have implemented a more cunning approach using a triangle-strip wrapped around the cube (for example) which would perhaps result in slightly more efficient storage and rendering performance, but for such a trivial model it's hardly worth the trouble.

Note that at the time of writing we hard-code the vertex layout and do not include model normals.

### Input Assembly

Since the cube uses triangles (as opposed to the previous default of a _strip_ of triangles) we need to implement the _input assembly pipeline stage_ builder, which is very simple:

```java
public class InputAssemblyStageBuilder extends AbstractPipelineBuilder<VkPipelineInputAssemblyStateCreateInfo> {
    private VkPrimitiveTopology topology = VkPrimitiveTopology.TRIANGLE_STRIP;
    private boolean restart;
    
    public InputAssemblyStageBuilder topology(Primitive primitive) {
        this.topology = primitive.topology();
        return this;
    }

    public InputAssemblyStageBuilder restart(boolean restart) {
        this.restart = restart;
        return this;
    }
    
    @Override
    protected VkPipelineInputAssemblyStateCreateInfo result() {
        final var info = new VkPipelineInputAssemblyStateCreateInfo();
        info.topology = topology;
        info.primitiveRestartEnable = VulkanBoolean.of(restart);
        return info;
    }
}
```

This new stage configuration is added to the pipeline:

```java
Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .assembly()
        .topology(cube.primitive())
        .build()
    ...
```

### Integration #2

We can now create a cube model:

```java
Model cube = new CubeBuilder().build();
ByteBuffer vertices = cube.vertices();
```

Load it into the staging buffer:

```java
VulkanBuffer staging = VulkanBuffer.staging(dev, vertices.limit());
staging.load(vertices);
```

Not forgetting to size the destination VBO accordingly:

```java
VulkanBuffer dest = new VulkanBuffer.Builder(dev)
    .length(vertices.limit())
    ...
```

And also update the draw command:

```java
Command draw = (api, handle) -> api.vkCmdDraw(handle, cube.count(), 1, 0, 0);
```

When we run the code it should look roughly the same as the quad demo since we will be looking at the front face of the cube.

Next we will apply a rotation to the cube by implemented the following factory on the `Matrix` class to generate a rotation about a given axis:

```java
public static Matrix rotation(Vector axis, float angle) {
    final Builder rot = new Builder().identity();
    final float sin = MathsUtil.sin(angle);
    final float cos = MathsUtil.cos(angle);
    if(Vector.X_AXIS.equals(axis)) {
        rot.set(1, 1, cos);
        rot.set(1, 2, sin);
        rot.set(2, 1, -sin);
        rot.set(2, 2, cos);
    }
    else
    if(Vector.Y_AXIS.equals(axis)) {
        rot.set(0, 0, cos);
        rot.set(0, 2, -sin);
        rot.set(2, 0, sin);
        rot.set(2, 2, cos);
    }
    else
    if(Vector.Z_AXIS.equals(axis)) {
        rot.set(0, 0, cos);
        rot.set(0, 1, -sin);
        rot.set(1, 0, sin);
        rot.set(1, 1, cos);
    }
    else {
        throw new UnsupportedOperationException("Arbitrary rotation axis not supported");
    }
    return rot.build();
}
```

We add the following temporary matrix to implement a static rotation about the X-Y axis:

```java
Matrix rotX = Matrix.rotation(Vector.X_AXIS, MathsUtil.toRadians(30));
Matrix rotY = Matrix.rotation(Vector.Y_AXIS, MathsUtil.toRadians(30));
Matrix rot = rotX.multiply(rotY);
```

And modify the view transform to compose all three matrices:

```java
Matrix matrix = proj.multiply(view).multiply(rot);
uniform.load(matrix);
```

Note that the order of the multiplications is important since matrix multiplication is non-commutative.

The above should give us this:

![Rotated Cube](cube.png)

To animate the cube we need some sort of loop to render multiple frames and logic to modify the view matrix over time. 
For the moment we bodge a temporary time-based loop that applies the matrix and exits the loop after a couple of rotations:

```java
long period = 5000;
long end = System.currentTimeMillis() + period * 2;
Matrix rotX = Matrix.rotation(Vector.X_AXIS, MathsUtil.toRadians(45));

while(true) {
    // Stop loop
    final long now = System.currentTimeMillis();
    if(now >= end) {
        break;
    }

    // Update rotation
    final float angle = (now % period) * MathsUtil.TWO_PI / period;
    final Matrix rotY = Matrix.rotation(Vector.Y_AXIS, angle);
    final Matrix rot = rotY.multiply(rotX);
    uniform.load(rot, rot.length(), 2 * rot.length());

    // Acquire next frame
    int index = swapchain.acquire();

    // Render frame
    new Work.Builder()
        .add(render)
        .build()
        .submit();
    
    // TODO
    presentQueue.waitIdle();
}
```

Notes:

- The code to calculate the _angle_ interpolates a 5 second period onto the unit circle.

- This loop _should_ be generating a number of validation errors on every frame - we will address these issues in the next chapter.

Hopefully when we run the demo we can now finally see the goal for this chapter: the proverbial rotating textured cube.

Huzzah!

---

## Summary

In this chapter we implemented perspective projection to render a 3D rotating cube.

To support this demo we created:

- The matrix class.

- Uniform buffers.

- The model class and builder.

---

## References

[^projection]: [OpenGL matrices tutorial](http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/)

