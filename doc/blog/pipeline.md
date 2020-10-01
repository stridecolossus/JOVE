# Pipeline Builder

```java
public static class Builder {
	// Properties
	private final LogicalDevice dev;
	private PipelineLayout.Builder layout;
	private final Map<VkShaderStageFlag, VkPipelineShaderStageCreateInfo> shaders = new HashMap<>();

	// Fixed function builders
	private final VertexInputStageBuilder input = new VertexInputStageBuilder();
	...

	public Builder(LogicalDevice dev) {
		this.dev = notNull(dev);
		init();
	}
	
	private void init() {
		layout.parent(this);
		input.parent(this);
		...
	}

	public VertexInputStageBuilder input() {
		return input;
	}

	...
	
	public Pipeline build() {
		// Create descriptor
		final VkGraphicsPipelineCreateInfo pipeline = new VkGraphicsPipelineCreateInfo();

		// Init layout
		pipeline.layout = layout.result().handle();

		// Init shader pipeline stages
		pipeline.stageCount = shaders.size();
		pipeline.pStages = StructureHelper.structures(shaders.values());

		// Init fixed function pipeline stages
		pipeline.pVertexInputState = input.result();
		...

		// Allocate pipeline
		final VulkanLibrary lib = dev.library();
		final Pointer[] pipelines = lib.factory().pointers(1);
		check(lib.vkCreateGraphicsPipelines(dev.handle(), null, 1, new VkGraphicsPipelineCreateInfo[]{pipeline}, null, pipelines));

		// Create pipeline
		return new Pipeline(pipelines[0], dev);
	}
}
```

# Base-class

```java
abstract class AbstractPipelineStageBuilder<T> {
	private Pipeline.Builder parent;

	protected void parent(Pipeline.Builder parent) {
		this.parent = notNull(parent);
	}

	protected abstract T result();

	public Pipeline.Builder build() {
		return parent;
	}
}
```

# Nested Builder

```java
public class VertexInputStageBuilder extends AbstractPipelineStageBuilder<VkPipelineVertexInputStateCreateInfo> {
	@Override
	protected VkPipelineVertexInputStateCreateInfo result() {
		final var info = new VkPipelineVertexInputStateCreateInfo();
		...
		return info;
	}
}
```

# Test

```java
class BuilderTest {
	private Pipeline.Builder builder;
	private Rectangle rect;

	@BeforeEach
	void before() {
		builder = new Pipeline.Builder(dev);
		rect = new Rectangle(new Dimensions(3, 4));
	}

	@Test
	void build() {
		pipeline = builder
				.viewport()
					.viewport(rect)
					.scissor(rect)
					.build()
				.shader()
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
					.shader(mock(Shader.class))
					.build()
				.build();

		...
	}
}
```

# Handle class

```java
public final class Handle {
	/**
	 * Native type converter for a handle.
	 */
	public static final TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Pointer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value == null) {
				return null;
			}
			else {
				final Handle handle = (Handle) value;
				return handle.handle;
			}
		}

		@Override
		public Object fromNative(Object value, FromNativeContext context) {
			if(value == null) {
				return null;
			}
			else {
				return new Handle((Pointer) value);
			}
		}
	};

	private final Pointer handle;

	public Handle(Pointer handle) {
		this.handle = notNull(handle);
	}
}
```

# Refactor API

```java
void vkDestroyInstance(Handle instance, Handle allocator);
```

# Vulkan Object

```java
public abstract class AbstractVulkanObject {
	@FunctionalInterface
	public interface Destructor {
		/**
		 * Destroys this object.
		 * @param dev				Logical device
		 * @param handle			Handle
		 * @param allocator		Allocator
		 */
		void destroy(Handle dev, Handle handle, Handle allocator);
	}

	private Handle handle;
	private final LogicalDevice dev;
	private final Destructor destructor;

	...

	public synchronized void destroy() {
		if(isDestroyed()) throw new IllegalStateException("Object has already been destroyed: " + this);
		destructor.destroy(dev.handle(), handle, null);
		handle = null;
	}
}
```

# Using Vulkan Object

```java
	Pipeline(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyPipeline);
	}
```

# Refactor message handler

```java
private void destroy(Pointer handle) {
	final Object[] args = new Object[]{Instance.this.handle, handle, null};
	destroy.invoke(Void.class, args, options());
}

/**
 * @return Type converter options
 */
private Map<String, ?> options() {
	return Map.of(Library.OPTION_TYPE_MAPPER, VulkanLibrary.MAPPER);
}
```

# Shader

```java
public static Shader create(LogicalDevice dev, byte[] code) {
	// Buffer shader code
	final ByteBuffer buffer = BufferFactory.byteBuffer(code.length);
	buffer.put(code);
	buffer.flip();

	// Create descriptor
	final VkShaderModuleCreateInfo info = new VkShaderModuleCreateInfo();
	info.codeSize = code.length;
	info.pCode = buffer;

	// Allocate shader
	final VulkanLibrary lib = dev.library();
	final PointerByReference shader = lib.factory().pointer();
	check(lib.vkCreateShaderModule(dev.handle(), info, null, shader));

	// Create shader
	return new Shader(shader.getValue(), dev);
}
```

# Shader Loader

```java
public static class Loader {
	public static Loader create(Path dir, LogicalDevice dev) {
		Check.notNull(dir);
		final Function<String, byte[]> loader = filename -> load(dir, filename);
		return new Loader(loader, dev);
	}

	private static byte[] load(Path dir, String filename) {
		final Path path = dir.resolve(filename);
		try(final var in = Files.newInputStream(path)) {
			return in.readAllBytes();
		}
		catch(IOException e) {
			throw new ServiceException("Error loading shader: " + path, e);
		}
	}

	private final Function<String, byte[]> loader;
	private final LogicalDevice dev;

	public Loader(Function<String, byte[]> loader, LogicalDevice dev) {
		this.loader = notNull(loader);
		this.dev = notNull(dev);
	}

	public Shader load(String filename) {
		return Shader.create(dev, loader.apply(filename));
	}
}
```

# Render Pass

```java
public class RenderPass extends AbstractVulkanObject {
	RenderPass(Pointer handle, LogicalDevice dev) {
		super(handle, dev, dev.library()::vkDestroyRenderPass);
	}

	public static class Builder {
		private final LogicalDevice dev;
		private final List<VkAttachmentDescription> attachments = new ArrayList<>();
		private final List<VkSubpassDescription> subpasses = new ArrayList<>();
		private final List<VkSubpassDependency> dependencies = new ArrayList<>();

		...

		public RenderPass build() {
			// Init render pass descriptor
			final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

			// Add attachments
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureHelper.structures(attachments);

			// Add sub passes
			info.subpassCount = subpasses.size();
			info.pSubpasses = StructureHelper.structures(subpasses);

			// Add dependencies
			info.dependencyCount = dependencies.size();
			info.pDependencies = StructureHelper.structures(dependencies);

			// Allocate render pass
			final VulkanLibrary lib = dev.library();
			final PointerByReference pass = lib.factory().pointer();
			check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

			// Create render pass
			return new RenderPass(pass.getValue(), dev);
		}
	}
}
```

# Nested Builders

```java
public AttachmentBuilder attachment() {
	return new AttachmentBuilder();
}

public class AttachmentBuilder {
	...
	
	public Builder build() {
		final VkAttachmentDescription info = new VkAttachmentDescription();
		...
		attachments.add(info);
		return Builder.this;
	}
}
```

# Integration

```java
// Create render pass
final RenderPass pass = new RenderPass.Builder(dev)
		.attachment()
			.format(format)
			.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
			.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
			.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
			.build()
		.subpass()
			.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
			.build()
		.build();

// Load shaders
final Shader.Loader loader = Shader.Loader.create("./src/test/resources/demo/triangle", dev);
final Shader vert = loader.load("spv.triangle.vert");
final Shader frag = loader.load("spv.triangle.frag");

// Create pipeline
final Rectangle rect = new Rectangle(chain.extents());
final Pipeline pipeline = new Pipeline.Builder(dev)
		.pass(pass)
		.viewport(rect)
		.shader()
			.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
			.shader(vert)
			.build()
		.shader()
			.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
			.shader(frag)
			.build()
		.build();
```
