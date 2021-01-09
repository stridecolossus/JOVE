---
title: Perspective Projection
---

## Overview

We now have almost all the Vulkan functionality required to implement the proverbial rotating textured cube. 
The next step is to introduce perspective projection so that fragments that are more distant in the scene appear correctly foreshortened.

For this we will need:

- a builder for a cube model.

- a rotation animation.

- a matrix class.

- the perspective projection matrix.

- uniform buffers to pass the matrix to the shader.

---

## Perspective Projection

### Enter The Matrix

Matrices are central to 3D graphics programming - we will create a new matrix domain class to support perspective projection and the rotation animation.

> We could of course easily use a third-party maths library but the point of this project is to learn the basics so we craft our own implementation from scratch.  Note that we will not attempt an explanation of matrix mathematics here, there are plenty of excellent tutorials[^matrix] that we have used to implement our own solution.

We first establish the following requirements and constraints:

- immutable (the default for all domain classes where logical and feasible).

- limit to _square_ matrices (i.e. same width and height) which makes things a lot simpler.

- support an arbitrary _order_ (or size) of matrices.

With the above in mind the first-cut matrix class is as follows:

```java
public final class Matrix implements Bufferable {
    private final int order;
    private final float[] matrix;

    public Matrix(float[] matrix) {
        this(order(matrix.length), Arrays.copyOf(matrix, matrix.length));
    }

    private Matrix(int order, float[] matrix) {
        this.order = order;
        this.matrix = matrix;
    }

    /**
     * Determines the order of the matrix.
     * @param len Matrix length
     * @return Order
     * @throws IllegalArgumentException if the given length is not square
     */
    private static int order(int len) {
        return switch(len) {
            case 1 -> 1;
            case 4 -> 2;
            case 9 -> 3;
            case 16 -> 4;
            default -> {
                final int order = (int) MathsUtil.sqrt(len);
                if(len != order * order) throw new IllegalArgumentException("Matrix must be square");
                yield order;
            }
        };
    }

    public int order() {
        return order;
    }
}
```

The matrix is represented as a one-dimensional array in _column-major_ order (matching the default layout for matrices in GLSL shaders).

Indices into the array are calculated by the following helper:

```java
private static int index(int order, int row, int col) {
    return row + order * col;
}
```

For example to lookup a matrix element:

```java
public float get(int row, int col) {
    final int index = index(order, row, col);
    return matrix[index];
}
```

The matrix is also `Bufferable` so it can be copied to an NIO buffer:

```java
@Override
public void buffer(ByteBuffer buffer) {
    for(float f : matrix) {
        buffer.putFloat(f);
    }
}
```

We provide an array constructor but in general a matrix will be created using a builder:

```java
public static class Builder {
    private final int order;
    private float[] matrix;

    public Builder(int order) {
        this.order = oneOrMore(order);
        this.matrix = new float[order * order];
    }

    ...
    
    public Matrix build() {
        return new Matrix(order, matrix);
    }
}
```

Which provides methods to initialise an empty matrix to identity:

```java
public Builder identity() {
    for(int n = 0; n < order; ++n) {
        set(n, n, 1);
    }
    return this;
}
```

And set a matrix element:

```java
public Builder set(int row, int col, float value) {
    final int index = index(order, row, col);
    matrix[index] = value;
    return this;
}
```

### Perspective Projection

We can now use this functionality to build the perspective projection matrix:

```java
public interface Projection {
    /**
     * Calculates the frustum half-height for this projection.
     * @param dim Viewport dimensions
     * @return Frustum half-height
     */
    float height(Dimensions dim);

    /**
     * Builds the matrix for this projection.
     * @param near      Near plane
     * @param far       Far plane
     * @param dim       Viewport dimensions
     * @return Projection matrix
     */
    Matrix matrix(float near, float far, Dimensions dim);

    /**
     * Perspective projection with a 60 degree FOV.
     */
    Projection DEFAULT = perspective(MathsUtil.toRadians(60));

    /**
     * Creates a perspective projection.
     * @param fov Field-of-view (radians)
     */
    static Projection perspective(float fov) {
        return new Projection() {
            private final float height = MathsUtil.tan(fov * MathsUtil.HALF);

            @Override
            public float height(Dimensions dim) {
                return height;
            }

            @Override
            public Matrix matrix(float near, float far, Dimensions dim) {
                final float f = 1 / height;
                final float range = near - far;
                return new Matrix.Builder()
                    .set(0, 0, f / dim.ratio())
                    .set(1, 1, -f)
                    .set(2, 2, far / range)
                    .set(2, 3, (near * far) / range)
                    .set(3, 2, -1)
                    .build();
            }
        };
    }
}
```

The above implementation is based on this[^projection] tutorial.

### Uniform Buffers

To pass the perspective matrix to the shader we will next implement a _uniform buffer object_ (or UBO), a descriptor set resource implemented as a vertex buffer.

We add a new factory method to the buffer class to create the uniform buffer resource:

```java
public DescriptorSet.Resource uniform() {
    require(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT);

    return new DescriptorSet.Resource() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        }

        @Override
        public void populate(VkWriteDescriptorSet write) {
            final var info = new VkDescriptorBufferInfo();
            info.buffer = handle();
            info.offset = 0;
            info.range = len;
            write.pBufferInfo = info;
        }
    };
}
```

We also take the opportunity to store the usage flags in the domain object and add the `require` invariant test (as above) to the relevant buffer methods.

### Integration #1

As an intermediate step we will apply the identity matrix to the demo to test the uniform buffer before we start messing around with perspective projection.

First we add constants for a 4-order identity matrix:

```java
public final class Matrix implements Transform, Bufferable {
    /**
     * Default order size for a matrix.
     */
    public static final int DEFAULT_ORDER = 4;

    /**
     * Default identity matrix.
     */
    public static final Matrix IDENTITY = identity(DEFAULT_ORDER);

    public static Matrix identity(int order) {
        return new Builder(order).identity().build();
    }
}
```

Next we create a new uniform buffer and load the identity matrix:

```java
VulkanBuffer uniform = new VulkanBuffer.Builder(dev)
    .length(Matrix.IDENTITY.length())
    .usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
    .build();

uniform.load(Matrix.IDENTITY);
```

Note that we are using a buffer that is visible to the host (i.e. the application) which is less efficient than a device-local buffer - eventually the matrix will be updated every frame (for the rotation) so a device-local / staging buffer buffer would just add extra complexity and overhead.

Next we add a second binding to the descriptor set layout for the uniform buffer:

```java
Binding samplerBinding = ...

Binding uniformBinding = new Binding.Builder()
    .binding(1)
    .type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
    .stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
    .build();

Layout layout = Layout.create(dev, List.of(samplerBinding, uniformBinding));
```

Register the new resource type with the descriptor set pool:

```java
Pool pool = new Pool.Builder(dev)
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 2)
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 2)
    .max(2)
    .build();
```

And finally update the descriptor sets:

```java
Resource uniformResource = uniform.uniform();
for(DescriptorSet set : descriptors) {
    ...
    set.entry(uniformBinding).set(uniform);
}
```

Note that we are using the same uniform buffer for each descriptor set - this is fine for the moment since we are not changing the matrix between frames. 
In future chapters we will create a separate uniform buffer for each frame.

The last step is to modify the vertex shader to use the matrix which involves:
1. Adding a new `layout` declaration for the uniform buffer which contains a 4-order matrix:
2. Multiplying the vertex position by the matrix.

The vertex shader should look like the following:

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

If all goes well we should still see the flat textured quad since the identity matrix essentially does nothing.

### View Transform

We can now use the matrix class and the perspective projection to apply a _view transform_ to the demo.

First we create the _view_ (or _camera_) transformation matrices:

```java
final Matrix rot = new Matrix.Builder()
    .identity()
    .row(0, Vector.X_AXIS)
    .row(1, Vector.Y_AXIS.invert())
    .row(2, Vector.Z_AXIS)
    .build();

final Matrix trans = new Matrix.Builder()
    .identity()
    .column(3, new Point(0, 0, -1))
    .build();

final Matrix view = rot.multiply(trans);
```

The _rot_ matrix is the camera orientation and _trans_ is the camera (or eye) position which moves the camera one unit 'out' of the screen 
(or moves the scene one unit into the screen, whichever way you look at it).

Notes:
- The Y (or up) direction is inverted for Vulkan.
- The Z axis points _out_ from the screen towards the viewer.
- Later on we will wrap the view transform into a camera class.

We introduce the _vector_ domain class at this point:

```java
public final class Vector extends Tuple {
    public static final Vector X_AXIS = new Vector(1, 0, 0);
    public static final Vector Y_AXIS = new Vector(0, 1, 0);
    public static final Vector Z_AXIS = new Vector(0, 0, 1);

    public Vector(float x, float y, float z) {
        super(x, y, z);
    }

    public Vector invert() {
        return new Vector(-x, -y, -z);
    }
}
```

Next we replace the identity matrix we used above with the actual perspective projection, multiply it with the view transforma, and upload the result to the uniform buffer:

```java
final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
final Matrix matrix = proj.multiply(view);
uniform.load(matrix);
```

Finally we modify the vertex data to move one edge of the quad into the screen by modifying the Z coordinate of the right-hand vertices:

```java
new Point(-0.5f, -0.5f, 0))
new Point(-0.5f, +0.5f, 0))
new Point(+0.5f, -0.5f, -0.5f))
new Point(+0.5f, +0.5f, -0.5f))
```

If the transformation code is correct we should now see the quad in 3D with the right-hand edge sloping away from the viewer like an open door:

![Perspective Quad](perspective.png)

---

## Cube Model

We are now going to replace the hard-coded quad with a cube model which will require:

1. A new domain class for a general model that encapsulates vertex data.

2. A builder to construct a model.

3. And a more specialised implementation to construct the cube.

### Model Builder

Initially the model class is quite straight-forward (we will be adding more functionality as we go):

```java
public class Model {
    private final Primitive primitive;
    private final Vertex.Layout layout;
    private final List<Vertex> vertices;

    ...

    /**
     * @return Number of vertices in this model
     */
    public int count() {
        return vertices.size();
    }

    /**
     * @return Vertex buffer
     */
    public ByteBuffer vertices() {
        ...
    }
}
```

To avoid fiddling the code-generated `VkPrimitiveTopology` enumeration we implement a wrapper:

```java
public enum Primitive {
    /**
     * Triangles.
     */
    TRIANGLES(3, VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST),
    
    ...
    
    /**
     * @return Number of vertices per primitive
     */
    public int size() {
        return size;
    }

    /**
     * @return Vulkan primitive topology
     */
    public VkPrimitiveTopology topology() {
        return topology;
    }
}    
```

The new enumeration provides the following additional helper methods that are used when constructing models:

```java
/**
 * @return Whether this primitive is a strip
 */
public boolean isStrip() {
    return switch(this) {
        case TRIANGLE_STRIP, TRIANGLE_FAN, LINE_STRIP -> true;
        default -> false;
    };
}

/**
 * @return Whether this primitive supports face normals
 */
public boolean hasNormals() {
    return switch(this) {
        case TRIANGLES, TRIANGLE_STRIP, TRIANGLE_FAN -> true;
        default -> false;
    };
}

/**
 * @param count Number of vertices
 * @return Whether the given number of vertices is valid for this primitive
 */
public boolean isValidVertexCount(int count) {
    if(isStrip()) {
        return (count == 0) || (count >= size);
    }
    else {
        return (count % size) == 0;
    }
}
```

The approach for generating the interleaved buffer is the same as the existing quad demo:

```java
public ByteBuffer vertices() {
    // Allocate buffer
    final int len = vertices.size() * layout.size() * Float.BYTES;
    vertexBuffer = Bufferable.allocate(len);

    // Buffer vertices
    for(Vertex v : vertices) {
        layout.buffer(v, vertexBuffer);
    }

    // Prepare VBO
    return vertexBuffer.rewind();
}
```

Next we add a builder to the model:

```java
class Builder {
    private Primitive primitive = Primitive.TRIANGLE_STRIP;
    private Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION);
    private final List<Vertex> vertices = new ArrayList<>();
    
    ...
    
    public Model build() {
        return new Model(primitive, layout, vertices);
    }
}
```

The only notable method is `add` which validates the model vertices:

```java
public Builder add(Vertex vertex) {
    if(!layout.matches(vertex)) throw new IllegalArgumentException(...);
    vertices.add(vertex);
    return this;
}
```

### Cube Builder

We can now build on the new model class to construct a cube:

```java
public class CubeBuilder {
    private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE);
    
    private final Model.Builder builder = new Model.Builder().primitive(Primitive.TRIANGLES).layout(LAYOUT);
    private float size = 1;
    
    ...
    
    public Model build() {
    }
}
```

The `build` method creates two _triangles_ for each face of the cube:

```java

public Model build() {
    for(int[] face : FACES) {
        for(int corner : TRIANGLES) {
            ...
        }
    }
    return builder.build();
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

The two triangles for each face are specified by the following constants:

```java
public final class Quad {
    public static final List<Integer> LEFT = List.of(0, 1, 2);
    public static final List<Integer> RIGHT = List.of(2, 1, 3);
    public static final List<Coordinate2D> COORDINATES = List.of(TOP_LEFT, BOTTOM_LEFT, TOP_RIGHT, BOTTOM_RIGHT);
}
```

Note that triangles have alternate winding orders (exactly the same as we did for the quad in the previous chapter).

In the loop we can now construct the vertices for each face and build the model:

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
    private VkPrimitiveTopology topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
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

