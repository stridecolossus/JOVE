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
