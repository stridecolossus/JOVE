Overview
We now have almost all the Vulkan functionality required to implement the proverbial rotating textured cube.  The next step is to introduce perspective projection so that objects that are more distant in the scene appear correctly foreshortened.

For this we will need:

a builder for a cube model

a rotation animation

a matrix class

the perspective projection matrix

uniform buffers to pass the matrix to the shader


The Matrix
Matrices are central to 3D graphics programming - we will create a new matrix domain class to support perspective projection and the rotation animation.

Of course we could easily use a third-party maths library but the point of this project is to learn the basics so we craft our own implementation from scratch.  Note that we do not attempt an explanation of matrix mathematics here as there many excellent tutorials that we have used to implement our own solution (TODO links to references).  

We determine the following requirements for our matrix class:

immutable - this is our general approach for all domain objects where logical
column-major - following the Vulkan standard
supports arbitrary matrix order (the 'size' of the matrix)
limited to square matrices, i.e. same width and height - basically this makes things much simpler
With the above in mind the first-cut outline is as follows:

public final class Matrix implements Bufferable {
    /**
     * Default order size for a matrix.
     */
    public static final int DEFAULT_ORDER = 4;

    /**
     * Identity for an order four matrix.
     */
    public static final Matrix IDENTITY = identity(DEFAULT_ORDER);

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
        buffer.put(matrix);
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
The matrix is Bufferable so we can easily upload the matrix array to a vertex buffer.  We will also need a multiply operation so we can compose multiple matrix transforms (e.g. to combine the camera and perspective views).

Implementation notes:

The matrix is represented as a one-dimensional array in column-major order.

We could have implemented the data as a 2D array but Java multi-dimensional arrays are objects in their own right which seems overkill for such a small amount of data.

Alternatively each element could be a separate class member but that would make the code verbose and error-prone (likely copy-and-paste cock ups).  You will still find people insisting (without any evidence) this is a more efficient approach because the members will map to CPU data registers.
The constructor creates the matrix from a given array but to avoid having to mess with array-manipulation we implement a builder that allows the user to populate individual elements, rows and columns:
public static class Builder {
    private final float[] matrix;
    private final int order;

    /**
     * Constructor for a matrix of the given order.
     * @param order Matrix order
     */
    public Builder(int order) {
        this.order = oneOrMore(order);
        this.matrix = new float[order * order];
    }

    /**
     * Constructor for a matrix with an order {@link Matrix#DEFAULT_ORDER}.
     */
    public Builder() {
        this(DEFAULT_ORDER);
    }

    /**
     * Initialises this matrix to the identity matrix.
     */
    public Builder identity() {
        for(int n = 0; n < order; ++n) {
            set(n, n, 1);
        }
        return this;
    }

    /**
     * Sets a matrix element.
     * @param row       Row
     * @param col       Column
     * @param value     Matrix element
     * @throws ArrayIndexOutOfBoundsException if the row or column is out-of-bounds
     */
    public Builder set(int row, int col, float value) {
        final int index = row + order * col;
        matrix[index] = value;
        return this;
    }

    /**
     * Sets a matrix row to the given vector.
     * @param row Row index
     * @param vec Vector
     * @throws ArrayIndexOutOfBoundsException if the row is out-of-bounds
     */
    public Builder row(int row, Tuple vec) {
        set(row, 0, vec.x);
        set(row, 1, vec.y);
        set(row, 2, vec.z);
        return this;
    }

    /**
     * Sets a matrix column to the given vector.
     * @param col Column index
     * @param vec Vector
     * @throws ArrayIndexOutOfBoundsException if the column is out-of-bounds
     */
    public Builder column(int col, Tuple vec) {
        set(0, col, vec.x);
        set(1, col, vec.y);
        set(2, col, vec.z);
        return this;
    }

    /**
     * Constructs this matrix.
     * @return New matrix
     */
    public Matrix build() {
        return new Matrix(order, matrix);
    }
}
The multiply() and identity() methods can now be implemented using the builder:

public static Matrix identity(int order) {
    return new Builder(order).identity().build();
}

...

private int index(int row, int col) {
    return row + order * col;
}

public float get(int row, int col) {
    return matrix[index(row, col)];
}

public Matrix multiply(Matrix m) {
    if(m.order != order) throw new IllegalArgumentException("Cannot multiply matrices with different sizes");

    final float[] result = new float[matrix.length];
    for(int r = 0; r < order; ++r) {
        for(int c = 0; c < order; ++c) {
            float total = 0;
            for(int n = 0; n < order; ++n) {
                total += get(r, n) * m.get(n, c);
            }
            result[index(c, r)] = total;
        }
    }

    return new Matrix(order, result);
}
The index() helper determines the index within the column-major array for a given row and column.



Get Some Perspective
With the matrix builder in place we can now implement the perspective projection:

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
Again we are not going into the details of how the matrix is calculated here 

TODO REFERENCES



Uniform Buffers
A uniform buffer object (or UBO) is used to pass 'global' data to a shader, in this case the projection matrix.  We will create a vertex buffer that is used to pass this data to the hardware and update the descriptor set code to support uniform buffers.

The process of updating the resources in a descriptor set was a quick-and-dirty implementation in the previous chapter.  We will re-factor this code to support multiple updates in one step and add support for uniform buffers.

Firstly we define a descriptor update that initialises a resource in a descriptor set:

public static abstract class Update {
    private final VkDescriptorType type;

    /**
     * Constructor.
     * @param type Expected descriptor type
     */
    protected Update(VkDescriptorType type) {
        this.type = notNull(type);
    }

    /**
     * @return Expected descriptor type for this update
     */
    public VkDescriptorType type() {
        return type;
    }

    /**
     * Populates the given descriptor for this update.
     * @param write Update write descriptor
     */
    protected abstract void apply(VkWriteDescriptorSet write);
}
This is a skeleton implementation that populates the relevant field of a VkWriteDescriptorSet descriptor for a specific update.  We can now move the code for applying a sampler update to the Sampler class:

public DescriptorSet.Update update(View view) {
    // Create update descriptor
    final VkDescriptorImageInfo image = new VkDescriptorImageInfo();
    image.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
    image.imageView = view.handle();
    image.sampler = this.handle();

    // Create update wrapper
    return new DescriptorSet.Update(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER) {
        @Override
        protected void apply(VkWriteDescriptorSet write) {
            write.pImageInfo = StructureHelper.structures(List.of(image));
        }
    };
}
Next we refactor the remaining code into a sort of builder that allows the user to specify a number of descriptor set updates in one step:

public static class Builder {
    private static record Entry(DescriptorSet set, VkDescriptorSetLayoutBinding binding, Update update) {
    }

    private final List<Entry> updates = new ArrayList<>();
    private Collection<DescriptorSet> sets;

    public Builder descriptor(DescriptorSet set) {
        return descriptors(List.of(set));
    }

    public Builder descriptors(Collection<DescriptorSet> sets) {
        this.sets = Set.copyOf(notEmpty(sets));
        return this;
    }

    public Builder add(int index, Update update) {
        if(sets == null) ...

        for(DescriptorSet set : sets) {
            // Lookup binding descriptor for this set
            final VkDescriptorSetLayoutBinding binding = set.layout().binding(index);
            if(binding == null) ...

            // Add entry
            updates.add(new Entry(set, binding, update));
        }

        return this;
    }

    public void update(LogicalDevice dev) {
        // Allocate contiguous memory block of write descriptors for the updates
        final VkWriteDescriptorSet[] writes = (VkWriteDescriptorSet[]) new VkWriteDescriptorSet().toArray(updates.size());

        // Populate write descriptors
        for(int n = 0; n < writes.length; ++n) {
            // Populate write descriptor for this entry
            final Entry entry = updates.get(n);
            final VkWriteDescriptorSet write = writes[n];
            write.dstBinding = entry.binding.binding;
            write.descriptorType = entry.binding.descriptorType;
            write.dstSet = entry.set.handle();
            write.descriptorCount = 1;
            write.dstArrayElement = 0;

            // Populate update descriptor
            entry.update.apply(write);
        }

        // Apply updates
        dev.library().vkUpdateDescriptorSets(dev.handle(), writes.length, writes, 0, null);
    }
}

The process of updating a descriptor set is now:
Specify a group of descriptor set(s) to be updated via the appropriate setters.
Call add() to register an update for each descriptor set within a given binding.
Repeat for other descriptors sets.
Invoke update() to apply all the updates.
In the add() method we lookup and verify the relevant binding descriptor from the layout of each descriptor set.  We then create an instance of the new local Entry class which is a transient record of the binding-descriptor-update tuple for each update to be applied.

Each entry is transformed to a VkWriteDescriptorSet descriptor and passed to Vulkan in the update() method.  Note that we are required to populate a contiguous memory block for the out-going array.

This might seem a lot of effort (and it is) but descriptor sets are probably one of the more difficult aspects of Vulkan and this implementation should hopefully make things clearer both from the perspective of the developer and the user.  We will see this new class in action below.

We can now build on this re-factored solution to support uniform buffers by adding an updater to the vertex buffer class:

public DescriptorSet.Update update() {
    // Create uniform buffer descriptor
    final VkDescriptorBufferInfo uniform = new VkDescriptorBufferInfo();
    uniform.buffer = this.handle();
    uniform.offset = 0;
    uniform.range = len;

    // Create updater
    return new DescriptorSet.Update(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER) {
        @Override
        protected void apply(VkWriteDescriptorSet write) {
            write.pBufferInfo = StructureHelper.structures(List.of(uniform));
        }
    };
}
Much nicer.



Integration #1
As an intermediate step we will apply the perspective projection to the quad demo before we start messing around with the cube model and rotations.  For this test the uniform buffer is loaded once before rendering as we are not changing the data thereafter.  Eventually we will pass separate matrices for the perspective projection, model transform and view (or camera) transforms and update the UBO per-frame and per-model as necessary.

First we create a vertex buffer that will contain the uniform buffer object:

final int uniformLength = 4 * 4 * Float.BYTES;      // TODO - one 4x4 matrix, from matrix? some sort of descriptor?

final VertexBuffer uniform = new VertexBuffer.Builder(dev)
    .length(uniformLength)
    .usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
    .property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
    .build();

Note that this buffer is only visible to the host, i.e. we are not using any staging buffers or device-local memory here.

Initially we will use the identity matrix to test that the uniform buffer and shader are configured correctly before we try the actual projection matrix:

final ByteBuffer proj = BufferFactory.byteBuffer(uniformLength);
// TODO - Projection.DEFAULT.matrix(1, 2, rect.size()).buffer(proj.asFloatBuffer());
Matrix.IDENTITY.buffer(proj.asFloatBuffer());
uniform.load(proj);

Next we add an entry to the descriptor set layout for the uniform buffer:

final DescriptorSet.Layout setLayout = new DescriptorSet.Layout.Builder(dev)
    .binding(0)
        .type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
        .stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
        .build()
    .binding(1)
        .type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
        .stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
        .build()
    .build();

final DescriptorSet.Pool setPool = new DescriptorSet.Pool.Builder(dev)
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 3)
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER, 3)
    .max(2 * 3)
    .build();

And now we can use the nice new updater functionality to apply the sampler and uniform buffer to the descriptor sets:

new DescriptorSet.Update.Builder()
    .descriptors(descriptors)
    .add(0, sampler.update(texture))
    .add(1, uniform.update())
    .update(dev);

Note that we are using the same uniform buffer for all three descriptor sets, later on we will need a separate UBO for each swapchain image, but this is fine for the moment since we are not changing the matrix between frames.

Finally we modify the vertex shader to transform each vertex position by the projection matrix:

#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform ubo {
    mat4 proj;
};

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

void main() {
    gl_Position = proj * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}

If all goes well we should still see the flat textured quad since the matrix essentially does nothing.


View Transformation
We can now use the matrix class and the perspective projection to apply a view transformation to the demo.

First we create the view (or camera) transformation:

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

The rot matrix is the camera rotation and trans is the camera (or eye) position which moves the camera one unit 'out' of the screen (or moves the scene one unit into the screen, whichever way you look at it).

Notes:
The Y (or up) direction is inverted for Vulkan.
Matrix multiplication is non-commutative, i.e. the order of operations is important.
Later on we will wrap this code into a camera class.
We also introduce the vector domain class here:

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

Next we swap the commented lines above to use the actual perspective matrix and multiply the two together:

final Matrix projection = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
final Matrix matrix = projection.multiply(view);
matrix.buffer(proj.asFloatBuffer());

Finally we modify the vertex data to move one edge of the quad into the screen.

new Point(-0.5f, -0.5f, 0))     .coords(Coordinate2D.TOP_LEFT)
new Point(-0.5f, +0.5f, 0))     .coords(Coordinate2D.BOTTOM_LEFT)
new Point(+0.5f, -0.5f, -0.5f))     .coords(Coordinate2D.TOP_RIGHT)
new Point(+0.5f, +0.5f, -0.5f))     .coords(Coordinate2D.BOTTOM_RIGHT)

If the transformation code is correct we should now see the quad in 3D with the right-hand edge sloping away from the viewer like an open door:







The Cube Model
We are now going to replace the hard-coded quad vertices with a cube model.

For this we will need:

a new model domain class that encapsulates vertex data and conversion to an NIO buffer

a builder to construct a model

a more specialised implementation to construct a cube.
The model class is fairly straight-forward (we will be adding more functionality as we go):

public class Model {
    public static Model of(Primitive primitive, Vertex.Layout layout, List<Vertex> vertices) {
        final var builder = new Model.Builder().primitive(primitive).layout(layout);
        vertices.forEach(builder::add);
        return builder.build();
    }

    private final Primitive primitive;
    private final Vertex.Layout layout;
    private final List<Vertex> vertices;

    ...

    public ByteBuffer buffer() {
        return layout.buffer(vertices);
    }

    public static class Builder {
        ...
    
        public Builder add(Vertex vertex) {
            if(!layout.matches(vertex)) throw new IllegalArgumentException(...);
            vertices.add(vertex);
            return this;
        }

        public Model build() {
            return new Model(primitive, layout, vertices);
        }
    }
}
The new Primitive enumeration specifies the topology of the model and provides helper methods to validate that the vertices are valid for a given primitive.

The model builder is then used to construct a cube model:

public class CubeBuilder {
    // Vertices
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

    // Face indices
    private static final int[][] FACES = {
        { 0, 1, 2, 3 }, // Front
        { 4, 5, 6, 7 }, // Back
        { 6, 7, 0, 1 }, // Left
        { 2, 3, 4, 5 }, // Right
        { 6, 0, 4, 2 }, // Top
        { 1, 7, 3, 5 }, // Bottom
    };

    // Triangle indices for a face
    private static final int[] LEFT = {0, 1, 2};
    private static final int[] RIGHT = {2, 1, 3};

    // Quad texture coordinates
    private static final Coordinate2D[] QUAD = Coordinate2D.QUAD.toArray(Coordinate2D[]::new);

    // Default layout
    private static final Vertex.Layout LAYOUT = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE);

    private float size = 1;
    private boolean clockwise = false;

    ...

    public Model build() {
        // Init builder
        final Model.Builder builder = new Model.Builder().primitive(Primitive.TRIANGLE_LIST).layout(LAYOUT);

        // Add two triangles for each cube face
        for(int[] face : FACES) {
            add(face, LEFT, builder);
            add(face, RIGHT, builder);
        }

        // Construct model
        return builder.build();
    }

    /**
     * Adds a triangle.
     * @param face          Quad face indices
     * @param triangle      Triangle indices within this face
     * @param builder       Builder
     */
    private void add(int[] face, int[] triangle, Model.Builder builder) {
        // Build triangle vertices
        final List<Vertex> vertices = new ArrayList<>(3);
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
            vertices.add(v);
        }

        // Reverse for clockwise winding order
        if(clockwise) {
            Collections.reverse(vertices);
        }

        // Add to model
        vertices.forEach(builder::add);
    }
}
This code is a quite convoluted and we may well return later to factor out any common code into the underlying builder and/or helpers.

We could have implemented a more cunning approach using a triangle-strip wrapped around the cube which would perhaps result in more efficient storage and rendering performance, but for such a trivial model it's hardly worth the trouble.

We can now remove the hard-coded quad and replace it with the cube model:

Model cube = CubeBuilder.create();
ByteBuffer bb = cube.buffer();
Command draw = (api, handle) -> api.vkCmdDraw(handle, cube.size(), 1, 0, 0);
Finally we need to change the topology in the pipeline:

public class InputAssemblyStageBuilder ... {
    ...
    
    public InputAssemblyStageBuilder topology(Primitive primitive) {
        return topology(map(primitive));
    }

    private static VkPrimitiveTopology map(Primitive primitive) {
        return switch(primitive) {
            case LINE_LIST -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_LIST;
            case LINE_STRIP -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_LINE_STRIP;
            case POINT_LIST -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_POINT_LIST;
            case TRIANGLE_FAN -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_FAN;
            case TRIANGLE_LIST-> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST;
            case TRIANGLE_STRIP -> VkPrimitiveTopology.VK_PRIMITIVE_TOPOLOGY_TRIANGLE_STRIP;
            default -> throw new UnsupportedOperationException("Unsupported drawing primitive: " + primitive);
        };
    }
}

...

Pipeline pipeline = new Pipeline.Builder(dev)
    .assembly()
        .topology(cube.primitive())
        .build()
    ...
If we run the code now it should look roughly the same as the quad demo since we will be looking at one face of the cube.



Integration #2
We now bring everything together to complete this demo.

To make sure that the cube has been constructed correctly we can first apply a fixed rotation to the matrix before it is loaded to the uniform buffer.  For this we implement a factory method on the matrix class itself that generates a rotation matrix about a given axis (note that we disallow arbitrary axis rotations for the moment):

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
        // TODO - from quaternion
    }
    return rot.build();
}
The following code rotates the cube about the X-Y axis:

Matrix rotX = Matrix.rotation(Vector.X_AXIS, MathsUtil.DEGREES_TO_RADIANS * 30);
Matrix rotY = Matrix.rotation(Vector.Y_AXIS, MathsUtil.DEGREES_TO_RADIANS * 30);
Matrix rot = rotX.multiply(rotY);

...

Matrix matrix = proj.multiply(view).multiply(rot);
Note the order of the matrix operations when we compose the projection, view and rotation.

The above should give us this:



This matrix code in the demo is becoming quite messy, it's time to separate the three matrix components in the UBO so we can deal with each individually as necessary.  We resize the buffer to contain three matrices and add each separately:

// Create UBO
int uniformLength = 3 * 4 * 4 * Float.BYTES;
VertexBuffer uniform = ...
ByteBuffer bb = BufferFactory.byteBuffer(uniformLength);
FloatBuffer fb = bb.asFloatBuffer();

// Upload projection matrix
Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
proj.buffer(fb);

// Upload view matrix
...
view.buffer(fb);

// Apply rotation
Matrix.IDENTITY.buffer(fb);

// Update UBO
uniform.load(bb);
And we can now move the matrix multiplications to the shader:

#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(binding = 1) uniform ubo {
    mat4 proj;
    mat4 view;
    mat4 model;
};

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

void main() {
    gl_Position = proj * view * model * vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
We should still be able to see the rotated cube as previously.

To introduce a constant rotation we implement a temporary animation into the render loop which updates the model matrix once per frame:

// Init rotation animation
final int size = 4 * 4 * Float.BYTES;
final long period = 5000;
final ByteBuffer rotBuffer = BufferFactory.byteBuffer(size);
final Matrix rotX = Matrix.rotation(Vector.X_AXIS, MathsUtil.DEGREES_TO_RADIANS * 45);

// Render loop
for(int n = 0; n < 1000; ++n) {
    // Update rotation matrix
    final float angle = (System.currentTimeMillis() % period) * MathsUtil.TWO_PI / period;
    final Matrix rotY = Matrix.rotation(Vector.Y_AXIS, angle);
    final Matrix rot = rotY.multiply(rotX);
    rot.buffer(rotBuffer.asFloatBuffer());
    uniform.load(rotBuffer, 2 * size);
    rotBuffer.clear();
    
    // Acquire next frame from the swapchain
    ...
}
The projection and view matrices do not change after they are initialised but we need to update the rotation matrix per-frame.  To achieve this we added an over-loaded load() implementation to the vertex buffer that allows us to partially upload to the UBO:

public void load(ByteBuffer src) {
    load(src, 0);
}

public void load(ByteBuffer src, int offset) {
    // Check buffer
    final int size = src.remaining();
    if(offset + size > len) ...

    // Map buffer memory
    final LogicalDevice dev = this.device();
    final VulkanLibrary lib = dev.library();
    final PointerByReference data = lib.factory().pointer();
    check(lib.vkMapMemory(dev.handle(), mem, offset, size, 0, data));

    // Copy to memory
    final ByteBuffer bb = data.getValue().getByteBuffer(0, size);
    bb.put(src);

    // Cleanup
    lib.vkUnmapMemory(dev.handle(), mem);
}
We also remove the static X-Y rotation we applied previously.

Hopefully when we run the demo we can now finally see the goal for this chapter: the proverbial rotating textured cube.

Huzzah!



Summary
In this chapter we implemented perspective projection using the new matrix class and rendered a rotating cube rendered as a 3D object.

To support this demo we also implemented uniform buffers and re-factored the process of updating descriptor sets.

