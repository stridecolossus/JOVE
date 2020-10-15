# Matrix Outline

```java
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
	 * @param matrix 	Column-major matrix elements
	 * @param order		Matrix order
	 * @throws IllegalArgumentException if the given array is empty or not a <i>square</i> matrix
	 */
	public Matrix(int order, float[] matrix) {
		if(matrix.length != order * order) throw new IllegalArgumentException("Matrix is not square");
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
	public long length() {
		return order * order * Float.BYTES;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(float f : matrix) {
			buffer.putFloat(f);
		}
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

# Builder

```java
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
	 * @param row 		Row
	 * @param col		Column
	 * @param value		Matrix element
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
```

# Multiply

```java
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
```

# Perspective Projection

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
	 * @param near		Near plane
	 * @param far		Far plane
	 * @param dim		Viewport dimensions
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

# Update

```java
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
```

# Update Sampler

```java
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
```

# Update Builder

```java
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
```

# Update UBO

```java
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
```

# Create Uniform Buffer

```java
final int uniformLength = 4 * 4 * Float.BYTES;		// TODO - one 4x4 matrix, from matrix? some sort of descriptor?

final VertexBuffer uniform = new VertexBuffer.Builder(dev)
	.length(uniformLength)
	.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
	.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
	.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
	.build();
```

# Upload

```java
// final Matrix proj = Projection.DEFAULT.matrix(1, 2, rect.size());
final Matrix proj = Matrix.IDENTITY;
uniform.load(proj);
```

# Update Descriptor Layout

```java
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
```

# Update Descriptors

```java
new DescriptorSet.Update.Builder()
	.descriptors(descriptors)
	.add(0, sampler.update(texture))
	.add(1, uniform.update())
	.update(dev);
```

# Vertex Shader

```C
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
```

# Vector

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

# View Transform

```java
final Matrix pos = new Matrix.Builder()
		.identity()
		.row(0, Vector.X_AXIS)
		.row(1, Vector.Y_AXIS.invert())
		.row(2, Vector.Z_AXIS)
		.build();

final Matrix trans = new Matrix.Builder()
		.identity()
		.column(3, new Point(0, 0, -1))
		.build();

final Matrix view = pos.multiply(trans);
```

# Perspective Transform

```java
final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
final Matrix matrix = proj.multiply(view);
uniform.load(matrix);
```

# Vertices

```java
new Point(-0.5f, -0.5f, 0))			.coords(Coordinate2D.TOP_LEFT)
new Point(-0.5f, +0.5f, 0))			.coords(Coordinate2D.BOTTOM_LEFT)
new Point(+0.5f, -0.5f, -0.5f))		.coords(Coordinate2D.TOP_RIGHT)
new Point(+0.5f, +0.5f, -0.5f))		.coords(Coordinate2D.BOTTOM_RIGHT)
```

# Model Builder

```java
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

	/**
	 * @return Number of vertices in this model
	 */
	public int size() {
		return vertices.size();
	}

	@Override
	public long length() {
		return vertices.size() * layout.size() * Float.BYTES;
	}

	@Override
	public void buffer(ByteBuffer buffer) {
		for(Vertex v : vertices) {
			for(Vertex.Component c : layout.components()) {
				c.map(v).buffer(buffer);
			}
		}
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
```

# Cube Builder

```java
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
	 * @param face			Quad face indices
	 * @param triangle		Triangle indices within this face
	 * @param builder		Builder
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
```

# Cube Model

```java
// Create cube
final Model cube = CubeBuilder.create();

// Create staging VBO
final VertexBuffer staging = VertexBuffer.staging(dev, cube.length());

// Load to staging
staging.load(cube);

...

final Command draw = (api, handle) -> api.vkCmdDraw(handle, cube.size(), 1, 0, 0);
```

# Primitive

```java
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
```

# Rotation Factory

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
		// TODO - from quaternion
	}
	return rot.build();
}
```

# Static Rotation

```java
Matrix rotX = Matrix.rotation(Vector.X_AXIS, MathsUtil.DEGREES_TO_RADIANS * 30);
Matrix rotY = Matrix.rotation(Vector.Y_AXIS, MathsUtil.DEGREES_TO_RADIANS * 30);
Matrix rot = rotX.multiply(rotY);

...

Matrix matrix = proj.multiply(view).multiply(rot);
```

# Split Matrices

```java
// Create UBO
final int uniformLength = 3 * 4 * 4 * Float.BYTES;
final VertexBuffer uniform = ...

// Upload projection matrix
final Matrix proj = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
uniform.load(proj, 0);

// Upload view matrix
...
final Matrix view = pos.multiply(trans);
uniform.load(view, view.length());
```

# Updated Shader

```C
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
```

# Animation

```java
// Init rotation animation
final int size = 4 * 4 * Float.BYTES;
final long period = 5000;
final Matrix rotX = Matrix.rotation(Vector.X_AXIS, MathsUtil.DEGREES_TO_RADIANS * 45);

// Render loop
for(int n = 0; n < 1000; ++n) {
	// Update rotation matrix
	final float angle = (System.currentTimeMillis() % period) * MathsUtil.TWO_PI / period;
	final Matrix rotY = Matrix.rotation(Vector.Y_AXIS, angle);
	final Matrix rot = rotY.multiply(rotX);
	uniform.load(rot, 2 * size);
	
	// Acquire next frame from the swapchain
	...
}
```
