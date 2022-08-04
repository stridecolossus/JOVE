---
title: Perspective Projection
---

---

## Contents

- [Overview](#overview)
- [View Transformation](#view-transformation)
- [Cube Model](#cube-model)

---

## Overview

In this final chapter of this section we introduce the following:

* A _view transform_ representing the viewers position and orientation.

* _perspective projection_ so that fragments in the scene appear correctly foreshortened.

This will require the following new components:

* The _matrix_ class.

* A perspective projection matrix.

* A _uniform buffer_ to pass the matrix to the shader.

The demo will then be extended to render a rotating texture cube which will also require:

* A builder for a general _model_.

* A specific builder for a cube model.

* A rotation matrix.

* A simple loop to render multiple frames.

---

## View Transformation

### Enter The Matrix

The view transform and perspective projection are both implemented as a 4-by-4 matrix structured as follows:

```
Rx Ry Rz Tx
Yx Yy Yz Ty
Dx Dy Dz Tz
0  0  0  1
```

Where the top-left 3-by-3 segment of the matrix is the rotation component and the right-hand column _T_ is the translation.

In camera terms _R_ is the _right_ axis, _Y_ is _up_ and _D_ is the _direction_ of the view.

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

The matrix data is implemented as a 2D floating-point array, this is certainly not the most efficient implementation in terms of memory (since each row is itself an object) but it is the simplest to implement.

A matrix is also a bufferable object:

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

Note that the row-column indices are transposed to output the matrix in _column major_ order which is the default expected by Vulkan.

Matrices are constructed by a companion builder:

```java
public static class Builder {
    private float[][] matrix;

    public Builder(int order) {
        matrix = new float[order][order];
    }
    
    public Matrix build() {
        return new Matrix(matrix);
    }
}
```

Matrix elements are populated using the following method:

```java
public Builder set(int row, int col, float value) {
    matrix[row][col] = value;
    return this;
}
```

Which is also used to initialise the _identity_ matrix:

```java
public Builder identity() {
    for(int n = 0; n < matrix.length; ++n) {
        set(n, n, 1);
    }
    return this;
}
```

Further matrix functionality will be added as the chapter progresses.

For further background reading see [OpenGL matrices tutorial](http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/)

> Obviously we could have simply employed a third-party matrices library but one of our project goals is to learn from first principles.

### Uniform Buffers

Matrices are passed to a shader via a _uniform buffer_ which is a descriptor set resource implemented as a general Vulkan buffer.

The resource is created using the following new factory method on the `VulkanBuffer` class:

```java
public DescriptorResource uniform() {
    require(VkBufferUsage.UNIFORM_BUFFER);

    return new DescriptorResource() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.UNIFORM_BUFFER;
        }

        @Override
        public void populate(VkWriteDescriptorSet write) {
            var info = new VkDescriptorBufferInfo();
            info.buffer = handle();
            info.offset = offset;
            info.range = length();
            write.pBufferInfo = info;
        }
    };
}
```

As a first step the identity matrix is applied to the quad vertices to test the uniform buffer:

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
    var props = new MemoryProperties.Builder<VkBufferUsage>()
        .usage(VkBufferUsage.UNIFORM_BUFFER)
        .required(VkMemoryProperty.HOST_VISIBLE)
        .required(VkMemoryProperty.HOST_COHERENT)
        .build();

    VulkanBuffer uniform = VulkanBuffer.create(dev, allocator, matrix.length(), props);
    uniform.load(matrix);

    return uniform;
}
```

Where `load` is a new helper method to make the process of loading data into the buffer slightly more convenient:

```java
public void load(Bufferable data) {
    Region region = mem.region().orElseGet(mem::map);
    ByteBuffer bb = region.buffer();
    data.buffer(bb);
}
```

Note that in this case the uniform buffer is visible to the host (i.e. the application) which is less performant than device-local memory.
However this allows the application to update the matrix as required without the overhead of copying via an intermediate staging buffer.

A second binding is required in the descriptor set layout for the uniform buffer:

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
    return new Pool.Builder()
        .add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, count)
        .add(VkDescriptorType.UNIFORM_BUFFER, count)
        .max(1)
        .build(dev);
}
```

And finally the uniform buffer resource is initialised in the `descriptors` bean:

```java
DescriptorSet.set(descriptors, uniformBinding, uniform.uniform());
```

To use the uniform buffer in the shader the following layout declaration is added:

```glsl
layout(binding=1) uniform ubo {
    mat4 matrix;
};
```

The matrix is applied to each vertex:

```glsl
gl_Position = matrix * vec4(inPosition, 1.0);
```

Note that a _homogeneous_ vector (consisting of four components) is created to multiply the vertex position by the matrix.

The vertex shader should now look like this:

```glsl
#version 450

layout(binding=1) uniform ubo {
    mat4 matrix;
};

layout(location=0) in vec3 inPosition;
layout(location=1) in vec2 inTexCoord;

layout(location=0) out vec2 outTexCoord;

void main() {
    gl_Position = matrix * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
```

If all goes well the demo should still render the same flat textured quad since the identity matrix essentially changes nothing.

### View Transform

To construct the view transform the _vector_ domain object is introduced which is the second `Tuple` implementation:

```java
public final class Vector extends Tuple {
    public static final Vector X = new Vector(1, 0, 0);
    public static final Vector Y = new Vector(0, 1, 0);
    public static final Vector Z = new Vector(0, 0, 1);

    ...
    
    public Vector invert() {
        return new Vector(-x, -y, -z);
    }
}
```

The following methods are added to the matrix builder to populate a row or column of the matrix:

```java
public Builder row(int row, Vector vec) {
    set(row, 0, vec.x);
    set(row, 1, vec.y);
    set(row, 2, vec.z);
    return this;
}

public Builder column(int col, Vector vec) {
    set(0, col, vec.x);
    set(1, col, vec.y);
    set(2, col, vec.z);
    return this;
}
```

The view transform matrix is comprised of translation and rotation components multiplied together:

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

The elements of the resultant matrix are the _sum_ of each _row_ multiplied by the corresponding _column_ in the right-hand matrix.

For example, given the following matrices:

```
[a b]   [c .]
[. .]   [d .]
```

The result for element [0, 0] is `ac + bd` (and similarly for the rest of the matrix).

To test the view transform matrix, in the `matrix` bean the eye position (or camera) is moved one unit to the right (or move the scene one unit to the left, whichever way you look at it):

```java
Matrix trans = new Matrix.Builder()
    .identity()
    .column(3, new Point(1, 0, 0))
    .build();
```

The rotation component of the matrix is initialised to the three camera axes:

```java
Matrix rot = new Matrix.Builder()
    .row(0, Vector.X)
    .row(1, Vector.Y.invert())
    .row(2, Vector.Z)
    .build();
```

Finally the two components are multiplied together to compose the view transform matrix:

```java
return rot.multiply(trans);
```

Notes:

* The Y axis is inverted for Vulkan (points _down_ the screen).

* The Z axis points _out_ from the screen.

* The order of the operations is important since matrix multiplication is non-commutative.

* The view-transform will be wrapped into a camera class in a future chapter.

When running the demo the triangle should now be rendered to the right of the screen.

### Perspective Projection

A _projection_ matrix is used to apply perspective or orthographic projection to the scene:

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

A _perspective projection_ is based on the field-of-view (FOV) which is analogous to the focus of a camera:

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

Notes:

* This code is based on the example from the Vulkan Cookbook.

* The matrix assumes the Y axis points __down__ the viewport.

* `MathsUtil` is a utility class providing common constants, wrappers for trigonometric functions, and helpers for various mathematical use cases.

A convenience constant is added for a perspective projection with a default field-of-view:

```java
Projection DEFAULT = perspective(MathsUtil.toRadians(60));
```

The perspective projection is composed with the view transform to build the final matrix:

```java
@Configuration
public class CameraConfiguration {
    private Matrix projection;

    public CameraConfiguration(Swapchain swapchain) {
        projection = Projection.DEFAULT.matrix(0.1f, 100, swapchain.extents());
    }

    @Bean
    public Matrix matrix() {
        ...
        return projection.multiply(rot).multiply(trans);
    }
}
```

To exercise the perspective projection one edge of the quad is moved into the screen by fiddling the Z coordinate of the right-hand vertices:

```java
new Point(-0.5f, -0.5f, 0)
new Point(-0.5f, +0.5f, 0)
new Point(+0.5f, -0.5f, -0.5f)
new Point(+0.5f, +0.5f, -0.5f)
```

If the transformation code is correct we should now see the quad in 3D with the right-hand edge sloping away from the viewer like an open door:

![Perspective Quad](perspective.png)

Note that the translation component of the view transform was reverted back to the centre of the screen.

---

## Cube Model

### Model

The cube will be represented by a _model_ which composes vertex data and the properties of the model:

```java
public class Model {
    /**
     * Descriptor for this model.
     */
    public record Header(Primitive primitive, int count, List<Layout> layout) {
    }

    private final Header header;
    private final Bufferable vertices;
}
```

Where _count_ is the draw count for the model and _layout_ specifies the structure of each vertex in the model.

The model header is factored into a separate domain class since the actual model vertices (and later the index buffer) are not required once the data has been loaded to the hardware.

Rather than fiddling the code-generated `VkPrimitiveTopology` enumeration a wrapper is implemented for drawing primitives which can then provide additional helpers:

```java
public enum Primitive {
    TRIANGLES(3, VkPrimitiveTopology.TRIANGLE_LIST),
    TRIANGLE_STRIP(3, VkPrimitiveTopology.TRIANGLE_STRIP),
    ...

    private final int size;
    private final VkPrimitiveTopology topology;
    
    /**
     * @return Whether this primitive is a strip
     */
    public boolean isStrip() {
        return switch(this) {
            case TRIANGLE_STRIP, TRIANGLE_FAN, LINE_STRIP -> true;
            default -> false;
        };
    }
}
```

A model is constructed by a companion builder:

```java
public static class Builder {
    private Primitive primitive = Primitive.TRIANGLE_STRIP;
    private final List<Layout> layout = new ArrayList<>();
    private final List<Vertex> vertices = new ArrayList<>();
    
    ...
    
    public Model build() {    
        Header header = new Header(primitive, vertices.size(), layout);
        Bufferable data = vertices();
        return new Model(header, data);
    }
}
```

The interleaved vertex buffer is generated from the mutable model in the same manner as the hard-coded quad previously:

```java
public Bufferable vertices() {
    return new Bufferable() {
        @Override
        public int length() {
            return Layout.stride(layout) * vertices.size();
        }

        @Override
        public void buffer(ByteBuffer bb) {
            for(Bufferable b : vertices) {
                b.buffer(bb);
            }
        }
    };
}
```

### Cube Builder

The new model implementation is then used to construct a cube:

```java
public class CubeBuilder {
    private float size = MathsUtil.HALF;
    
    public Model build() {
        var model = new Model.Builder()
            .primitive(Primitive.TRIANGLES)
            .layout(Point.LAYOUT)
            .layout(Coordinate2D.LAYOUT);

        ...

        return model;
    }
}
```

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

Each face of the cube is comprised of a quad indexing into the vertices array:

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

A quad is comprised of two triangles specified as follows:

```java
public final class Quad {
    public static final List<Integer> LEFT = List.of(0, 1, 2);
    public static final List<Integer> RIGHT = List.of(2, 1, 3);
    public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);
}
```

Note that __both__ triangles have a counter clockwise winding order since the cube will be rendered using the triangle primitive (as opposed to a strip of triangles used so far).

The triangle indices are aggregated into a single concatenated array:

```java
private static final int[] TRIANGLES = Stream
    .of(Quad.LEFT, Quad.RIGHT)
    .flatMap(List::stream)
    .mapToInt(Integer::intValue)
    .toArray();
```

The `build` method iterates over the array of faces to lookup the vertex components for each triangle:

```java
for(int face = 0; face < FACES.length; ++face) {
    for(int corner : TRIANGLES) {
        int index = FACES[face][corner];
        Point pos = VERTICES[index].scale(size);
        Coordinate coord = Quad.COORDINATES.get(corner);
        ...
    }
}
```

Finally each vertex is added to the cube model:

```java
Vertex vertex = Vertex.of(pos, coord);
model.add(vertex);
```

### Integration

In the demo we replace the hard-coded quad vertices with the cube model:

```java
public class VertexBufferConfiguration {
    @Bean
    public static Model cube() {
        return new CubeBuilder().build();
    }
}
```

The cube is injected into the `vbo` bean and loaded into the staging buffer:

```java
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, model.vertices());
```

To configure the drawing primitive the relatively trivial _input assembly pipeline stage_ is implemented:

```java
public class InputAssemblyStageBuilder extends AbstractPipelineBuilder<VkPipelineInputAssemblyStateCreateInfo> {
    private VkPrimitiveTopology topology = VkPrimitiveTopology.TRIANGLE_STRIP;
    private boolean restart;
    
    ...

    @Override
    VkPipelineInputAssemblyStateCreateInfo get() {
        var info = new VkPipelineInputAssemblyStateCreateInfo();
        info.topology = topology;
        info.primitiveRestartEnable = VulkanBoolean.of(restart);
        return info;
    }
}
```

The vertex input and assembly stages are now specified by the cube model in the pipeline configuration:

```java
return new Pipeline.Builder()
    ...
    .input()
        .add(model.header().layout())
        .build()
    .assembly()
        .topology(model.header().primitive())
        .build()
    .build(dev);
```

Finally the draw command is updated in the render sequence:

```java
int count = model.header().count();
Command draw = (api, handle) -> api.vkCmdDraw(handle, count, 1, 0, 0);
```

When we run the demo it should look roughly the same since we will be looking at the front face of the cube.

### Rotation

To apply a rotation to the cube the following factory method is implemented on the matrix class:

```java
public static Matrix rotation(Vector axis, float angle) {
    Builder rot = new Builder().identity();
    float sin = MathsUtil.sin(angle);
    float cos = MathsUtil.cos(angle);
    if(Vector.X.equals(axis)) {
        rot.set(1, 1, cos);
        rot.set(1, 2, -sin);
        rot.set(2, 1, sin);
        rot.set(2, 2, cos);
    }
    else
    if(Vector.Y.equals(axis)) {
        ...
    }
    else
    if(Vector.Z.equals(axis)) {
        ...
    }
    else {
        throw new UnsupportedOperationException();
    }
    return rot.build();
}
```

In the camera configuration we compose a temporary, static rotation about the X-Y axis:

```java
float angle = MathsUtil.toRadians(30);
Matrix x = Matrix.rotation(Vector.X, angle);
Matrix y = Matrix.rotation(Vector.Y, angle);
Matrix model = x.multiply(y);
```

Which is multiplied with the projection and view transform to generate the final matrix:

```java
return projection.multiply(view).multiply(model);
```

Again note the order of operations: here the rotation is applied to the model, then the view transform, and finally the perspective projection.

We should now be able to see the fully 3D cube:

![Rotated Cube](cube.png)

### Animation

To animate the cube rotation we add a temporary loop to render multiple frames which terminates after a specified period:

```java
long period = 5000;
long start = System.currentTimeMillis();
while(true) {
    long time = System.currentTimeMillis() - start;
    if(time > period) {
        break;
    }
    ...
}
```

The temporary static rotation added previously is replaced by a matrix constructed on every frame.  First a horizontal rotation is animated by interpolating the period onto the unit-circle:

```java
float angle = (time % period) * MathsUtil.TWO_PI / period;
Matrix h = Matrix.rotation(Vector.Y, angle);
```

This is combined with a fixed vertical rotation so that the top and bottom faces of the cube can also be seen:

```java
Matrix v = Matrix.rotation(Vector.X, MathsUtil.toRadians(30));
Matrix model = h.multiply(v);
```

The projection-view and model matrices are then composed and loaded to the uniform buffer:

```java
Matrix m = matrix.multiply(model);
uniform.load(m);
```

In the loop the `Thread.sleep()` bodge is replaced with a second `pool.waitIdle()` call to block until each frame has been rendered.

Finally the presentation configuration is modified to create an array of frame buffers (one per swapchain image) to properly utilise the swapchain functionality.
Note that since the above loop is completely single-threaded a single frame buffer with a hard coded index would probably still work.

Hopefully we can now finally see the goal for this chapter: the proverbial rotating textured cube.

Huzzah!

Note that there are still several problems with this crude render loop that will be addressed in the next few chapters:

* The GLFW event queue thread is still blocked.

* The render loop will generate validation errors on every frame since synchronisation has not been configured.

* Warnings will also be generated because the array of frame buffers is not automatically released on termination.

---

## Summary

In this chapter we rendered a 3D rotating textured cube and implementing the following:

* The matrix class

* Perspective projection

* Uniform buffers

* Builders for a general model and cubes

* A rotation matrix.
