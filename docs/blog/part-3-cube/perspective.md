---
title: Perspective Projection
---

## Overview

We now have almost all the Vulkan functionality required to implement the proverbial rotating textured cube.  The next step is to introduce perspective projection so that fragments that are more distant in the scene appear correctly foreshortened.

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

We could of course easily use a third-party maths library but the point of this project is to learn the basics so we craft our own implementation from scratch.  Note that we will not attempt an explanation of matrix mathematics here as there many excellent tutorials that we have used to implement our own solution (TODO links to references).  

We determine the following requirements for our matrix class:
- immutable - this is our default approach for all domain objects where appropriate and feasible.
- column-major - following the Vulkan standard.
- arbitrary matrix _order_ (the size of the matrix).
- limited to square matrices, i.e. same width and height - basically this makes things much simpler.

With the above in mind the first-cut matrix class is as follows:

```java
public final class Matrix implements Bufferable {
    /**
     * Creates an identity matrix.
     * @param order Matrix order
     * @return Identity matrix
     */
    public static Matrix identity(int order) {
    }

    private final int order;
    private final float[] matrix;

    /**
     * Constructor.
     * @param matrix    Column-major matrix elements
     * @param order     Matrix order
     * @throws IllegalArgumentException if the length of the given array does not match the specified matrix order
     */
    public Matrix(int order, float[] matrix) {
        if(matrix.length != order * order) throw new IllegalArgumentException("Invalid matrix length");
        this.order = oneOrMore(order);
        this.matrix = Arrays.copyOf(matrix, matrix.length);
    }

    public int order() {
        return order;
    }

    /**
     * Retrieves a matrix element.
     * @param row Matrix row
     * @param col Column
     * @return Matrix element
     * @throws ArrayIndexOutOfBoundsException if the row or column is out-of-bounds
     */
    public float get(int row, int col) {
    }
    
    @Override
    public void buffer(FloatBuffer buffer) {
    }

    /**
     * Multiplies two matrices.
     * @param m Matrix
     * @return New matrix
     * @throws IllegalArgumentException if the given matrix is not of the same order as this matrix
     */
    public Matrix multiply(Matrix m) {
    }
}
```

The matrix is represented as a one-dimensional array in column-major order.  We could have implemented a 2D array but Java multi-dimensional arrays are objects in their own right which seems overkill for such a small amount of data.  Alternatively each element could be a separate class member but that would make the code verbose and error-prone (the supposed benefits are questionable for a JVM based implementation).

The matrix is `Bufferable` so we can upload it to an NIO buffer, unfortunately direct buffers do not support all the array operations so we have to implement a loop:

```java
@Override
public void buffer(ByteBuffer buffer) {
    for(float f : matrix) {
        buffer.putFloat(f);
    }
}
```

We provide an array constructor but in general it will be considerably easier to create a matrix using a builder:

```java
public static class Builder {
    private final float[] matrix;
    private final int order;

    public Builder(int order) {
        this.order = oneOrMore(order);
        this.matrix = new float[order * order];
    }
    
    public Builder set(int row, int col, float value) {
        final int index = row + order * col;
        matrix[index] = value;
        return this;
    }

    public Matrix build() {
        return new Matrix(order, matrix);
    }
}
```

A matrix is initialised to the identity as follows:

```java
public Builder identity() {
    for(int n = 0; n < order; ++n) {
        set(n, n, 1);
    }
    return this;
}
```

We add the following to complete implementation of the matrix for the moment:
- A multiply() operation to compose matrices (not shown here).
- Convenience helpers and constants for 4-order matrices (which is the most commonly requirement).
- Helpers to populate a row or column with a given tuple.

### Perspective Projection

With the matrix builder in place we can now implement the perspective projection:

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
     * Perspective projection with a 90 degree FOV.
     */
    Projection DEFAULT = perspective(MathsUtil.HALF_PI);

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

Again we are not going into the details of how the matrix is calculated but this[^projection] tutorial was very helpful.

---

## Uniform Buffer Objects

To pass a matrix to the shader we will next implement a _uniform buffer object_ which is a descriptor set resource (as the sampler was in the previous chapter).  Note that a uniform buffer (or UBO) is basically just a vertex buffer that has a more specialised purpose.

The process of updating the resources in a descriptor set was a quick-and-dirty implementation in the previous chapter.  We will re-factor this code to support multiple updates in one step and add support for uniform buffers.

Descriptor sets are a particularly complex and difficult aspect of Vulkan (at least they were for this author).  We will present the solution we implemented and discuss some of the design issues and complexities after.

### Resources

We first create an abstraction for descriptor set _resources_ defined as follows:

```java
public interface Resource<T extends Structure> {
    /**
     * @return Descriptor type
     */
    VkDescriptorType type();

    /**
     * @return Identity instance
     */
    Supplier<T> identity();

    /**
     * Populates the update descriptor for this resource.
     * @param descriptor Update descriptor
     */
    void populate(T descriptor);

    /**
     * Adds this update to the given write descriptor.
     * @param descriptor        Update descriptor
     * @param write             Write descriptor
     */
    void apply(T descriptor, VkWriteDescriptorSet write);
}
```

We refactor the temporary code in the last chapter and move it to the sampler class:

```java
/**
 * Creates a descriptor set resource for this sampler with the given texture image.
 * @param view Texture image
 * @return Sampler resource
 */
public Resource<VkDescriptorImageInfo> resource(View view) {
    return new Resource<>() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        }

        @Override
        public Supplier<VkDescriptorImageInfo> identity() {
            return VkDescriptorImageInfo::new;
        }

        @Override
        public void populate(VkDescriptorImageInfo info) {
            info.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            info.sampler = Sampler.this.handle();
            info.imageView = view.handle();
        }

        @Override
        public void apply(VkDescriptorImageInfo descriptor, VkWriteDescriptorSet write) {
            write.pImageInfo = descriptor;
        }
    };
}
```

To support uniform buffers we add a similar implementation in the vertex buffer class:

```java
/**
 * Creates a uniform buffer resource for this vertex buffer.
 * @return Uniform buffer resource
 */
public Resource<VkDescriptorBufferInfo> uniform() {
    return new Resource<>() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER;
        }

        @Override
        public Supplier<VkDescriptorBufferInfo> identity() {
            return VkDescriptorBufferInfo::new;
        }

        public void populate(VkDescriptorBufferInfo info) {
            info.buffer = VertexBuffer.this.handle();
            info.offset = 0;
            info.range = len;
        }

        @Override
        public void apply(VkDescriptorBufferInfo descriptor, VkWriteDescriptorSet write) {
            write.pBufferInfo = descriptor;
        }
    };
}
```

The apply() method in both implementations sets the relevant pointer array field in the `VkWriteDescriptorSet` descriptor.

### Descriptor Set Updates

We next add a new local class to the descriptor set that specifies a set of resource _updates_ to be applied:

```java
public class Update<T extends Structure> {
    private final Layout.Binding binding;
    private final Collection<Resource<T>> res;

    /**
     * Constructor.
     * @param binding       Binding
     * @param updates       Resources to updates
     */
    private Update(Layout.Binding binding, Collection<Resource<T>> res) {
        this.binding = notNull(binding);
        this.res = Set.copyOf(res);
    }

    /**
     * Populates the given write descriptor.
     * @param write Write descriptor
     */
    void populate(VkWriteDescriptorSet write) {
    }
}
```

The populate() method is used to fill a write descriptor for an update.

We first initialise the write descriptor based on the binding:

```java
// Init write descriptor
write.dstBinding = binding.binding;
write.descriptorType = binding.type;
write.dstSet = DescriptorSet.this.handle();
write.dstArrayElement = 0;
```

Next we transform the updates to an array:

```java
// Add resource array
write.descriptorCount = updates.size();
final Resource<T> instance = updates.iterator().next();
final T array = VulkanStructure.populate(instance.identity(), updates, Resource::populate);
```

Note that we use an _arbitrary_ entry from the collection of updates to retrieve the identity instance for the array.

Finally we invoke the apply() method in the resource to set the resultant array or object to the relevant field in the descriptor:

```java
instance.apply(array, write);
```

We add the following factory method to the descriptor set to create an update for a given set of resources:

```java
public <T extends Structure> Update<T> update(Layout.Binding binding, Collection<Resource<T>> updates) {
    return new Update<>(binding, updates);
}
```

### Applying Updates

To apply an update or a group of update we add another relatively simple builder:

```
public static class UpdateBuilder {
    private final List<Update<?>> updates = new ArrayList<>();

    /**
     * Adds a group of updates to the given descriptor sets.
     * @param <T> Resource type
     * @param sets          Descriptor sets to update
     * @param binding       Binding
     * @param res           Resources
     */
    public <T extends Structure> UpdateBuilder add(Collection<DescriptorSet> sets, Binding binding, Collection<Resource<T>> res) {
        for(DescriptorSet set : sets) {
            final Update<?> update = set.update(binding, res);
            updates.add(update);
        }
        return this;
    }
}
```

The apply() method converts the updates to an array and invokes the API to apply the changes:

```java
public void apply(LogicalDevice dev) {
    if(updates.isEmpty()) throw new IllegalArgumentException("Empty updates");
    final var array = VulkanStructure.populateArray(VkWriteDescriptorSet::new, updates, Update::populate);
    dev.library().vkUpdateDescriptorSets(dev.handle(), array.length, array, 0, null);
}
```

We also add a convenience apply() method implemented using the new builder to apply an update directly.

### Conclusion

The above may well seem overly complicated - and it's possible that we have over-engineered our solution - but as we have already observed descriptor sets are complex beasts:

- The same `VkWriteDescriptorSet` descriptor is used to update **all** types of resource.

- However only **one** type can be specified in any given update.

- Different fields in the descriptor are populated depending on the descriptor type (specified in the binding), e.g. `pImageInfo` for a sampler, hence the apply() method in the new `Resource` interface, i.e. the write descriptor is a sort of union type.

- Multiple resources can be applied to a descriptor set in one update, e.g. we could apply a sampler and a uniform buffer in one call.

- The API supports multiple updates to multiple descriptor sets in one invocation with the intention of reducing the number of calls when descriptors are volatile.

We have attempted to bear all the above in mind while trying to decouple the logic for the different types of resource (we only have two now but there will be more).

---

## Integration #1

### Creating the Uniform Buffer

As an intermediate step we will apply an identity matrix to the demo to test the uniform buffer before we start messing around with the perspective projection.

First we create a new vertex buffer for the uniform buffer and load the matrix:

```java
final VertexBuffer uniform = new VertexBuffer.Builder(dev)
    .length(Matrix.LENGTH)
    .usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
    .build();

uniform.load(Matrix.IDENTITY);
```

Note that this buffer is only visible to the host - eventually the matrix data in the uniform buffer will be updated every frame (i.e. for the rotation) so a staging buffer would just add extra complexity and overhead.

We also take the opportunity to slightly refactor the load() method of the vertex buffer to accept either a byte-buffer or a bufferable object:

```java
public void load(ByteBuffer buffer) {
    load(buffer, 0);
}

/**
 * Loads the given bufferable object to this vertex buffer at the specified offset.
 * @param buffer        Data buffer
 * @param offset        Offset into this vertex buffer (bytes)
 * @throws IllegalStateException if the size of the given buffer exceeds the length of this vertex buffer
 */
public void load(ByteBuffer buffer, long offset) {
    load(Bufferable.of(buffer), buffer.remaining(), offset);
}

/**
 * Loads the given bufferable object to this vertex buffer at the specified offset.
 * @param buffer        Bufferable object
 * @param len           Length of the object (bytes)
 * @param offset        Offset into this vertex buffer (bytes)
 * @throws IllegalStateException if the length of given bufferable object exceeds the length of this vertex buffer
 */
public void load(Bufferable obj, long len, long offset) {
    ...
}
```

### Adding the Uniform Buffer

Next we add a second binding to the descriptor set layout for the uniform buffer:

```java
Binding uniformBinding = new Binding.Builder()
    .binding(1)
    .type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
    .stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
    .build();

Layout layout = Layout.create(dev, List.of(samplerBinding, uniformBinding));
```

And add the new resource type to the descriptor set pool:

```java
Pool pool = new Pool.Builder(dev)
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 2)
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 2)
    .max(2 * 2)
    .build();
```

Finally we use the new functionality to apply the updates:

```java
new DescriptorSet.UpdateBuilder()
    .add(descriptors, samplerBinding, sampler.resource(texture))
    .add(descriptors, uniformBinding, uniform.resource())
    .apply(dev);
```

Note that we are using the same uniform buffer for each descriptor set - this is fine for the moment since we are not changing the matrix between frames.  In future chapters we will create a separate uniform buffer for each frame.

### Applying the Matrix

Finally we modify the vertex shader to use the matrix which involves:
1. Adding a new layout declaration for the uniform buffer which contains a 4-order matrix:
2. Multiplying the vertex position by the matrix.

The vertex shader should now be as follows:

```C
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

---

## View Transformation

We can now use the matrix class and the perspective projection to apply a view transformation to the demo.

First we create the view (or camera) transformation:

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

The _rot_ matrix is the camera orientation and _trans_ is the camera (or eye) position which moves the camera one unit 'out' of the screen (or moves the scene one unit into the screen, whichever way you look at it).

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

Next we replace the identity matrix with the perspective projection matrix, multiply it with the view transformation, and upload the result to the uniform buffer:

```java
final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
final Matrix matrix = proj.multiply(view);
uniform.load(matrix);
```

Finally we modify the vertex data to move one edge of the quad into the screen by modifying the Z coordinate of the bottom two vertices:

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

1. A new domain class for a general model that encapsulates vertex data and conversion to an NIO buffer.

2. A builder to construct the model.

3. And a more specialised implementation to construct the cube.

### The Model

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

The new primitive enumeration replicates `VkPrimitiveTopology` but provides some additional helper functionality (without having to modify the code-generated class).
In particular we add the following method to test whether the number of vertices in a given model is valid for the primitive:

```java
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

Next we add a builder to the model, for the moment this has almost no functionality other than a test in the add() method to validate a vertex against the layout:

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
    private float size = 1;
    private boolean clockwise = false;
    
    ...
    
    public Model build() {
    }
}
```

The build() method create two _triangles_ for each face of the cube:

```java
private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE);

public Model build() {
    // Init builder
    final Model.Builder builder = new Model.Builder().primitive(Primitive.TRIANGLES).layout(LAYOUT);

    // Add two triangles for each cube face
    for(int[] face : FACES) {
        add(face, LEFT, builder);
        add(face, RIGHT, builder);
    }

    // Construct model
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

The two triangles are specified by indices into each face (exactly the same as we did for the quad in the previous chapter):

```
private static final int[] LEFT =  {0, 1, 2};
private static final int[] RIGHT = {2, 1, 3};
```

The method to add a triangle generates three vertices for each triangle and adds them to the model:

```java
private void add(int[] face, int[] triangle, Model.Builder builder) {
    for(int n = 0; n < 3; ++n) {
        // Lookup vertex position for this triangle
        final int index = face[triangle[n]];
        final Point pos = VERTICES[index].scale(size);

        // Lookup texture coordinate
        final Coordinate2D coord = QUAD[triangle[n]];

        // Build vertex
        final Vertex v = new Vertex.Builder()
                .position(pos)
                .coords(coord)
                .build();

        // Add to model
        builder.add(v);
    }
}
```

The texture coordinates are a simple array constructed from the pre-defined quad in the `Coordinate2D` class.

```java
private static final Coordinate2D[] QUAD = Coordinate2D.QUAD.toArray(Coordinate2D[]::new);
```

We could have implemented a more cunning approach using a triangle-strip wrapped around the cube which would perhaps result in more efficient storage and rendering performance, but for such a trivial model it's hardly worth the trouble.

### Input Assembly Pipeline Stage

Since the cube uses triangles (as opposed to the previous default of a _strip_ of triangles) we need to implement the _input assembly pipeline stage_ builder, which is very simple:

```java
public class InputAssemblyStageBuilder extends AbstractPipelineBuilder<VkPipelineInputAssemblyStateCreateInfo> {
    private VkPrimitiveTopology topology = VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
    private boolean restart;
    
    ...
    
    @Override
    protected VkPipelineInputAssemblyStateCreateInfo result() {
        final var info = new VkPipelineInputAssemblyStateCreateInfo();
        info.topology = topology;
        info.primitiveRestartEnable = VulkanBoolean.of(restart);
        return info;
    }
}
```

We add an over-loaded setter that maps a `Primitive` to the `VkPrimitiveTopology` equivalent.

This new stage configuration is added to the pipeline:

```java
Pipeline pipeline = new Pipeline.Builder(dev)
    ...
    .assembly()
        .topology(cube.primitive())
        .build()
    ...
```

---

## Integration #2

### Rendering the Cube

We can now create a cube model:

```java
Model cube = new CubeBuilder().build();
ByteBuffer vertices = cube.vertices();
```

load it into the staging buffer:

```java
VertexBuffer staging = VertexBuffer.staging(dev, vertices.limit());
staging.load(vertices);
```

not forgetting to size the destination VBO accordingly:

```java
VertexBuffer dest = new VertexBuffer.Builder(dev)
    .length(vertices.limit())
    ...
```

and also update the draw command:

```java
Command draw = (api, handle) -> api.vkCmdDraw(handle, cube.count(), 1, 0, 0);
```

When we run the code it should look roughly the same as the quad demo since we will be looking at the front face of the cube.

### Rotation

Next we will apply a rotation to the cube by implemented the following factory method on the matrix class to generate a rotation matrix about a given axis:

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

### Animation

To animate the rotation we modify the render loop to generate a rotation matrix on every frame:

```java
final long period = 5000;

for(int n = 0; n < 1000; ++n) {
    float angle = (System.currentTimeMillis() % period) * MathsUtil.TWO_PI / period;
    Matrix rot = Matrix.rotation(Vector.Y_AXIS, angle);
    Matrix matrix = proj.multiply(view).multiply(rot);
    uniform.load(matrix);

    int index = chain.acquire(null, null);
    
    ...
}
```

(after removing the static rotation we applied above).

Hopefully when we run the demo we can now finally see the goal for this chapter: the proverbial rotating textured cube.

Huzzah!

---

## Summary

In this chapter we implemented perspective projection to render a 3D rotating cube.

To support this demo we also implemented:

- the matrix class.

- uniform buffers.

- the model class and builder.

- an improved mechanism for updating descriptor sets.

---

## References

[^projection]: [OpenGL matrices tutorial](http://www.opengl-tutorial.org/beginners-tutorials/tutorial-3-matrices/)

