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

```
final int uniformLength = 4 * 4 * Float.BYTES;		// TODO - one 4x4 matrix, from matrix? some sort of descriptor?

final VertexBuffer uniform = new VertexBuffer.Builder(dev)
	.length(uniformLength)
	.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
	.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
	.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
	.build();
```

# Upload

```
final ByteBuffer proj = BufferFactory.byteBuffer(uniformLength);
// TODO - Projection.DEFAULT.matrix(1, 2, rect.size()).buffer(proj.asFloatBuffer());
Matrix.IDENTITY.buffer(proj.asFloatBuffer());
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
	.add(1, uniform)
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

# Perspective Transform

```java
final Matrix projection = Projection.DEFAULT.matrix(0.1f, 100, rect.size());
final Matrix matrix = projection.multiply(view);
matrix.buffer(proj.asFloatBuffer());
```

# Vertices

```java
new Point(-0.5f, -0.5f, 0))			.coords(Coordinate2D.TOP_LEFT)
new Point(-0.5f, +0.5f, 0))			.coords(Coordinate2D.BOTTOM_LEFT)
new Point(+0.5f, -0.5f, -0.5f))		.coords(Coordinate2D.TOP_RIGHT)
new Point(+0.5f, +0.5f, -0.5f))		.coords(Coordinate2D.BOTTOM_RIGHT)
```
