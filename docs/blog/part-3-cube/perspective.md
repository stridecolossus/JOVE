---
title: Perspective Projection
---

## Overview

In the final chapter in this section we will introduce _perspective projection_ so that fragments that are more distant in the scene appear correctly foreshortened.

We will implement the _matrix_ class which is a central component of 3D graphics.

> Of course we could utilise a third-party matrices library but one of the aims of this project is to build (and learn) from first principles.

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

The matrix data is implemented as a 2D floating-point array.  This is certainly not the most efficient implementation in terms of memory (since each row is itself an object) but it is the simplest to implement.  We anticipate this implementation will only be used in cases where we are constructing a matrix and are not concerned about efficiency (such as perspective projection).

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

Note that the row-column indices are transposed to output the matrix in _column major_ order which is the default expected by Vulkan.

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

For further background reading see [OpenGL matrices tutorial](http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/)

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

Note that we are using a buffer that is visible to the host (i.e. the application) which is less efficient than device-local memory.  However we will eventually be updating the matrix every frame to apply a rotation animation, this approach is more logical than continually copying via a staging buffer.

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

Note that for the moment we are using the same uniform buffer for all descriptor sets since our render loop is essentially single threaded.

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

We can now replace the identity matrix with a perspective projection:

```java
@Bean
public static Matrix matrix(Swapchain swapchain) {
    Matrix projection = Projection.DEFAULT.matrix(0.1f, 100, swapchain.extents());
    ...
}
```

Next we apply a _view transform_ to the demo representing the viewers position and orientation (i.e. the camera).

First we add the following methods to the matrix builder to populate a row or column of the matrix:

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

Next we introduce the _vector_ domain object:

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

The view transform matrix consists of translation and rotation components.  The translation moves the eye position (or camera) one unit out of the screen (or moves the scene one unit into the screen, whichever way you look at it):

```java
Matrix trans = new Matrix.Builder()
    .identity()
    .column(3, new Point(0, 0, -1))
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

Finally we compose these two view matrices to create the view transform which is then multiplied (see below) with the perspective projection:

```java
Matrix view = rot.multiply(trans);
return projection.multiply(view);
```

Notes:

* The Y axis is inverted for Vulkan (points _down_ the screen).

* The Z axis points _out_ from the screen.

* The order of the operations is important since matrix multiplication is non-commutative.

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

Each element of the resultant matrix is the _sum_ of a _row_ multiplied by the corresponding _column_ in the right-hand matrix.

For example, given the following matrices:

```
[a b]   [c .]
[. .]   [d .]
```

The result for element [0, 0] is `a * c + b * d` (and similarly for the rest of the matrix).

### Integration #2

To test the perspective projection we modify the vertex data to move one edge of the quad into the screen by fiddling the Z coordinate of the right-hand vertex positions:

```java
new Point(-0.5f, -0.5f, 0)
new Point(-0.5f, +0.5f, 0)
new Point(+0.5f, -0.5f, -0.5f)
new Point(+0.5f, +0.5f, -0.5f)
```

If the transformation code is correct we should now see the quad in 3D with the right-hand edge sloping away from the viewer like an open door:

![Perspective Quad](perspective.png)

---

## Cube Model

### Model

We next introduce the _model_ class which composes vertex data and associated properties:

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

The interleaved vertex buffer is generated from the model in the same manner as we did for the hard-coded quad:

```java
public Bufferable vertices() {
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

### Cube Builder

To construct a model we provide the ubiquitous builder which is essentially a wrapper for a mutable list of vertices:

```java
public class ModelBuilder {
    private final List<Vertex> vertices = new ArrayList<>();
    private final List<Layout> layouts;
    private Primitive primitive = Primitive.TRIANGLE_STRIP;

    ...

    public DefaultModel build() {
        Header header = new Header(layouts, primitive, vertices.size());
        return new DefaultModel(header, vertices);
    }
}
```

To construct the cube we extend the model builder:

```java
public class CubeBuilder extends ModelBuilder {
    private float size = MathsUtil.HALF;

    public CubeBuilder(List<Layout> layouts) {
        super(layouts);
        super.primitive(Primitive.TRIANGLES);
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

Each quad is comprised of two triangles specified as follows:

```java
public final class Quad {
    public static final List<Integer> LEFT = List.of(0, 1, 2);
    public static final List<Integer> RIGHT = List.of(2, 1, 3);
    public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);
}
```

Note that __both__ triangles have a counter clockwise winding order since the cube will be rendered using the triangle primitive (as opposed to a strip of triangles we have used up until now).

The triangle indices are aggregated into a single concatenated array:

```java
private static final int[] TRIANGLES = Stream
    .of(Quad.LEFT, Quad.RIGHT)
    .flatMap(List::stream)
    .mapToInt(Integer::intValue)
    .toArray();
```

In the `build` method we iterate over the array of faces to lookup the vertex components for each triangle:

```java
public Model build() {
    for(int face = 0; face < FACES.length; ++face) {
        for(int corner : TRIANGLES) {
            int index = FACES[face][corner];
            Point pos = VERTICES[index].scale(size);
            Vector normal = NORMALS[face];
            Coordinate coord = Quad.COORDINATES.get(corner);
            Colour col = COLOURS[face];
            ...
        }
    }
    return super.build();
}
```

Each vertex is then transformed (see below) to the target layout and added to the cube model:

```java
Vertex vertex = Vertex.of(pos, normal, coord, col);
Vertex transformed = vertex.transform(layouts);
add(transformed);
```

### Vertex Transformation

The cube builder creates vertices containing all components (position, normal, texture coordinate, colour), however for the demo application we only require the vertex position and texture coordinates.  Additionally applications may require vertex components to be re-ordered if (for example) the shader cannot be modified to suit a given model.

Therefore we introduce a _transform_ method to the `Vertex` class to apply a new layout:

```java
public Vertex transform(List<Layout> layouts) {
    return layouts
        .stream()
        .map(this::map)
        .collect(collectingAndThen(toList(), Vertex::new));
}
```

The vertex component matching each entry in the layout transformation is determined as follows:

```java
private Component map(Layout layout) {
    for(Component c : components) {
        if(c.layout() == layout) {
            return c;
        }
    }
    throw new IllegalArgumentException(...);
}
```

Notes:

* The resultant vertex component is matched by _identity_ of the layout since (for example) points and vector normals have equivalent layouts but are different instances.

* The component is the _first_ matching layout of the transformation, i.e. the behaviour of duplicate layouts is undefined.

### Integration #3

In the demo we replace the hard-coded quad vertices with a cube model:

```java
public class VertexBufferConfiguration {
    @Bean
    public static Model cube() {
        return new CubeBuilder().build();
    }
}
```

The model is injected into the `vbo` bean and loaded into the staging buffer:

```java
VulkanBuffer staging = VulkanBuffer.staging(dev, allocator, model.vertices());
```

To configure the drawing primitive we implement the _input assembly pipeline stage_ which is quite trivial:

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

Finally we modify the draw command in the render sequence:

```java
int count = model.header().count();
Command draw = (api, handle) -> api.vkCmdDraw(handle, count, 1, 0, 0);
```

When we run the code it should look roughly the same as the quad demo since we will be looking at the front face of the cube.

### Rotation

To apply a rotation to the cube we implement the following factory on the matrix class to generate a rotation about an axis:

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

Note that for the moment we only support the pre-defined axes.

In the camera configuration we use this new method to compose a temporary, static rotation about the X-Y axis:

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

Again note the order of operations - here we essentially apply the rotation to the model, then the view transform, and finally the perspective projection.

We should now be able to see the fully 3D cube:

![Rotated Cube](cube.png)

### Animation

To animate the cube rotation we first add a temporary `while` loop to render multiple frames which terminates after a configurable period:

```java
long period = cfg.getPeriod();
long start = System.currentTimeMillis();
while(true) {
    // Stop after a couple of rotations
    final long time = System.currentTimeMillis() - start;
    if(time > 3 * period) {
        break;
    }
    ...
}
```

Next we remove the temporary rotation added above and build a model matrix on every frame.

We animate the horizontal rotation angle by interpolating the period onto the unit-circle:

```java
float angle = (time % period) * MathsUtil.TWO_PI / period;
Matrix h = Matrix.rotation(Vector.Y, angle);
```

This is combined with a fixed vertical rotation so we can see the top and bottom faces of the cube:

```java
Matrix v = Matrix.rotation(Vector.X, MathsUtil.toRadians(30));
Matrix model = h.multiply(v);
```

Finally we inject the projection-view matrix and uniform buffer objects so we can compose the final matrix to be passed to the shader:

```java
Matrix m = matrix.multiply(model);
uniform.load(m);
```

Hopefully we can now finally see the goal for this chapter: the proverbial rotating textured cube.

Huzzah!

Note that there are still a couple of problems with the render loop that we will address in the next few chapters:

* The GLFW event queue thread is still blocked (we cannot move the window).

* The render loop will generate validation errors on every frame because no synchronisation has been configured.

* This demo will overload the hardware as we are not applying any limits on the frame rate.

---

## Summary

In this chapter we rendered a 3D rotating textured cube.

We implemented the following:

* The matrix class

* Perspective projection

* Uniform buffers

* Builders for a general model and cubes

---

