# Descriptor Set

```java
public class DescriptorSet {
	private final Handle handle;
	private final Layout layout;

	...

	/**
	 * Updates this descriptor set for the given sampler.
	 * @param binding Binding index
	 * @param sampler Sampler
	 * @param view    Image view
	 */
	public void sampler(int binding, Sampler sampler, View view) {
	}

	/**
	 * Creates a pipeline bind command for this descriptor set.
	 * @param layout Pipeline layout
	 * @return New bind command
	 */
	public Command bind(Pipeline.Layout layout) {
	}

	/**
	 * A <i>descriptor set pool</i> is used to allocate and manage a group of descriptor sets.
	 */
	public static class Pool extends AbstractVulkanObject {
		/**
		 * Allocates descriptor-sets for the given layouts.
		 * @return New descriptor-sets
		 * @throws IllegalArgumentException if the requested number of sets exceeds the maximum for this pool
		 */
		public List<DescriptorSet> allocate(List<Layout> layouts) {
		}
	}

	/**
	 * Layout for a descriptor-set.
	 */
	public static class Layout extends AbstractVulkanObject {
	}
}
```

# Layout Builder

```java
public static class Builder {
	private final LinkedList<VkDescriptorSetLayoutBinding> bindings = new LinkedList<>();

	/**
	 * Starts a new layout binding.
	 * @return New layout binding builder
	 */
	public LayoutBindingBuilder binding() {
		return new LayoutBindingBuilder().binding(next());
	}

	/**
	 * @return Next available binding index
	 */
	private int next() {
		if(bindings.isEmpty()) {
			return 0;
		}
		else {
			final VkDescriptorSetLayoutBinding prev = bindings.getLast();
			return prev.binding + 1;
		}
	}

	/**
	 * Constructs this descriptor-set layout.
	 * @return New layout
	 */
	public Layout build() {
		// Init layout descriptor
		final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
		info.bindingCount = bindings.size();
		info.pBindings = StructureHelper.structures(bindings);

		// Allocate layout
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = lib.factory().pointer();
		check(lib.vkCreateDescriptorSetLayout(dev.handle(), info, null, handle));

		// Create layout
		return new Layout(handle.getValue(), dev, bindings);
	}
}
```

# Layout Binding Builder

```java
public class LayoutBindingBuilder {
	private final Set<VkShaderStageFlag> stages = new HashSet<>();
	private int index;
	private VkDescriptorType type;
	private int count = 1;

	private LayoutBindingBuilder() {
	}

	...

	public Builder build() {
		// Validate
		if(type == null) throw new IllegalArgumentException("Descriptor type not specified");
		if(stages.isEmpty()) throw new IllegalArgumentException("No pipeline stage(s) specified");

		// Build binding descriptor
		final VkDescriptorSetLayoutBinding binding = new VkDescriptorSetLayoutBinding();
		binding.binding = index;
		binding.descriptorType = type;
		binding.descriptorCount = count;
		binding.stageFlags = IntegerEnumeration.mask(stages);

		// Add binding
		bindings.add(binding);
		return Builder.this;
	}
}
```

# Pool Builder

```java
public static class Builder {
	private final LogicalDevice dev;
	private final List<VkDescriptorPoolSize> entries = new ArrayList<>();
	private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
	private int max = 1;

	/**
	 * Adds a number of available sets to this pool.
	 * @param type		Descriptor set type
	 * @param count		Number of available sets of this type
	 */
	public Builder add(VkDescriptorType type, int count) {
			final VkDescriptorPoolSize entry = new VkDescriptorPoolSize();
			entry.type = notNull(type);
			entry.descriptorCount = oneOrMore(count);
			entries.add(entry);
			return this;
	}

	...

	public Pool build() {
		// Init pool descriptor
		final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
		info.flags = IntegerEnumeration.mask(flags);
		info.poolSizeCount = entries.size();
		info.pPoolSizes = StructureHelper.structures(entries);
		info.maxSets = max;

		// Allocate pool
		final VulkanLibrary lib = dev.library();
		final PointerByReference handle = lib.factory().pointer();
		check(lib.vkCreateDescriptorPool(dev.handle(), info, null, handle));

		// Create pool
		return new Pool(handle.getValue(), dev, max);
	}
}
```

# Pool Allocation

```java
public List<DescriptorSet> allocate(List<Layout> layouts) {
	// Build allocation descriptor
	final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
	info.descriptorPool = this.handle();
	info.descriptorSetCount = layouts.size();
	info.pSetLayouts = toPointerArray(layouts);

	// Allocate descriptors sets
	final LogicalDevice dev = this.device();
	final VulkanLibrary lib = dev.library();
	final Pointer[] handles = lib.factory().pointers(layouts.size());
	check(lib.vkAllocateDescriptorSets(dev.handle(), info, handles));

	// Create descriptor sets
	final List<DescriptorSet> sets = new ArrayList<>(handles.length);
	for(int n = 0; n < handles.length; ++n) {
		final Handle handle = new Handle(handles[n]);
		final DescriptorSet set = new DescriptorSet(handle, layouts.get(n));
		sets.add(set);
	}

	return sets;
}

public void free(Collection<DescriptorSet> sets) {
	this.sets.removeAll(sets);
	final LogicalDevice dev = this.device();
	check(dev.library().vkFreeDescriptorSets(dev.handle(), this.handle(), sets.size(), Handle.toArray(sets, DescriptorSet::handle)));
}

public synchronized void free() {
	final LogicalDevice dev = this.device();
	check(dev.library().vkResetDescriptorPool(dev.handle(), this.handle(), 0));
	sets.clear();
}
```

# Initialise Sampler

```java
public void sampler(int binding, Sampler sampler, View view) {
	final VkDescriptorImageInfo image = new VkDescriptorImageInfo();
	image.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
	image.imageView = view.handle();
	image.sampler = sampler.handle();

	final VkWriteDescriptorSet write = new VkWriteDescriptorSet();
	write.dstBinding = binding;
	write.descriptorType = VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
	write.dstSet = this.handle();
	write.descriptorCount = 1;
	write.dstArrayElement = 0;	
	write.pImageInfo = StructureHelper.structures(List.of(image));

	dev.library().vkUpdateDescriptorSets(dev.handle(), 1, new VkWriteDescriptorSet[]{write}, 0, null);
}
```

# Integration

```java
// Create descriptor layout
final DescriptorSet.Layout layout = new DescriptorSet.Layout.Builder(dev)
	.binding()
		.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
		.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
		.build()
	.build();

// Create pool
final DescriptorSet.Pool pool = new DescriptorSet.Pool.Builder(dev)
	.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 3)
	.max(3)
	.build();

// Create sets
final var sets = pool.allocate(layout, 3);

// Create sampler
final Sampler sampler = new Sampler.Builder(dev).build();

for(DescriptorSet set : descriptors) {
	set.sampler(0, sampler, texture);
}
```

# Integration - pipeline

```java
final Pipeline.Layout pipelineLayout = new Pipeline.Layout.Builder(dev)
		.add(setLayout)
		.build();

final Pipeline pipeline = new Pipeline.Builder(dev)
		.layout(pipelineLayout)
		.pass(pass)
		.input()
			.binding(layout)
			.build()
		...
		.build();
```

# Integration - draw

```java
final Command draw = (api, handle) -> api.vkCmdDraw(handle, 4, 1, 0, 0);		// TODO - builder
```

# Integration - rendering

```java
.begin()
	.add(pass.begin(buffers.get(n), rect, grey))
	.add(pipeline.bind())
	.add(dest.bind())
	.add(descriptors.get(n).bind(pipelineLayout))
	.add(draw)
	.add(RenderPass.END_COMMAND)
.end();
```

# Integration - coloured quad

```java
final Vertex[] vertices = {
		new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
		new Vertex.Builder().position(new Point(-0.5f, +0.5f, 0)).colour(new Colour(0, 1, 0, 1)).build(),
		new Vertex.Builder().position(new Point(+0.5f, -0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
		new Vertex.Builder().position(new Point(+0.5f, +0.5f, 0)).colour(new Colour(1, 1, 1, 1)).build(),
};
```

# Integration - texture coordinates

```
new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).coords(Coordinate2D.TOP_LEFT).build(),
new Vertex.Builder().position(new Point(-0.5f, +0.5f, 0)).coords(Coordinate2D.BOTTOM_LEFT).build(),
new Vertex.Builder().position(new Point(+0.5f, -0.5f, 0)).coords(Coordinate2D.TOP_RIGHT).build(),
new Vertex.Builder().position(new Point(+0.5f, +0.5f, 0)).coords(Coordinate2D.BOTTOM_RIGHT).build(),
```

# Integration - vertex layout

```
final Vertex.Layout layout = new Vertex.Layout(List.of(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE));
```

# Vertex shader

```C
#version 450
#extension GL_ARB_separate_shader_objects : enable

layout(location = 0) in vec3 inPosition;
layout(location = 1) in vec2 inTexCoord;

layout(location = 0) out vec2 outTexCoord;

void main() {
    gl_Position = vec4(inPosition, 1.0);
    outTexCoord = inTexCoord;
}
```

# Fragment Shader

```C
#version 450 core

layout(binding = 0) uniform sampler2D texSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 outColor;

void main(void) {
    outColor = texture(texSampler, texCoord);
}
```

# Texture Coordinates

```java
public interface TextureCoordinate extends Bufferable {
	class Coordinate1D implements TextureCoordinate {
		public final float u;

		public Coordinate1D(float u) {
			this.u = u;
		}

		@Override
		public final void buffer(FloatBuffer buffer) {
			buffer.put(u);
		}
	}

	class Coordinate2D extends Coordinate1D {
		/**
		 * Size of 2D coordinates.
		 */
		public static final int SIZE = 2;

		/**
		 * Quad coordinates.
		 */
		public static final Coordinate2D
			TOP_LEFT		= new Coordinate2D(0, 0),
			BOTTOM_LEFT 	= new Coordinate2D(0, 1),
			TOP_RIGHT 		= new Coordinate2D(1, 0),
			BOTTOM_RIGHT 	= new Coordinate2D(1, 1);

		public final float v;

		@Override
		public final void buffer(FloatBuffer buffer) {
			buffer.put(u).put(v);
		}
	}

	class Coordinate3D extends Coordinate2D {
		public final float w;
		
		...
	}
}
```
