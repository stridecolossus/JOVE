# Command

```java
@FunctionalInterface
public interface Command {
	/**
	 * Executes this command.
	 * @param lib		Vulkan library
	 * @param buffer 	Command buffer handle
	 */
	void execute(VulkanLibrary lib, Handle buffer);

	/**
	 * A <i>command buffer</i> is allocated by a {@link Pool} and used to record and execute commands.
	 */
	class Buffer {
	}

	/**
	 * A <i>command pool</i> allocates and maintains command buffers that are used to perform work on a given {@link Queue}.
	 */
	class Pool extends AbstractVulkanObject {
	}
}
```

# Fluid API

```java
// Create frame buffers
final var buffers = chain
		.views()
		.stream()
		.map(view -> FrameBuffer.create(view, pass))
		.collect(toList());

// Create command pool
final Command.Pool pool = Command.Pool.create(dev.queue(graphics));
final List<Command.Buffer> commands = pool.allocate(buffers.size());

// Record render commands
final Colour grey = new Colour(0.3f, 0.3f, 0.3f, 1);
final Command draw = (api, handle) -> api.vkCmdDraw(handle, 3, 1, 0, 0);
for(int n = 0; n < commands.size(); ++n) {
	commands.get(n)
		.begin()
			.add(pass.begin(buffers.get(n), rect, grey))
			.add(pipeline.bind())
			.add(draw)
			.add(RenderPass.END_COMMAND)
		.end();
}
```

# Command Buffer

```java
class Buffer {
	/**
	 * Buffer state.
	 */
	private enum State {
		UNDEFINED,
		RECORDING,
		READY,
	}

	private final Handle handle;
	private final Pool pool;

	private State state = State.UNDEFINED;

	...

	/**
	 * Starts command buffer recording.
	 * @param flags Flags
	 * @throws IllegalStateException if this buffer is not ready for recording
	 * @throws ServiceException if recording cannot be started
	 */
	public Buffer begin(VkCommandBufferUsageFlag... flags) {
		// Check buffer can be recorded
		if(state != State.UNDEFINED) throw new IllegalStateException("Buffer is not ready for recording");

		// Start buffer
		final VkCommandBufferBeginInfo info = new VkCommandBufferBeginInfo();
		info.flags = IntegerEnumeration.mask(flags);
		info.pInheritanceInfo = null;
		check(library().vkBeginCommandBuffer(handle, info));

		// Start recording
		state = State.RECORDING;
		return this;
	}

	/**
	 * Adds a command.
	 * @param cmd Command
	 * @throws IllegalStateException if this buffer is not recording
	 */
	public Buffer add(Command cmd) {
		if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording");
		cmd.execute(library(), handle);
		return this;
	}

	/**
	 * Ends recording.
	 * @throws IllegalStateException if this buffer is not recording
	 * @throws IllegalArgumentException if no commands have been recorded
	 */
	public void end() {
		if(state != State.RECORDING) throw new IllegalStateException("Buffer is not recording");
		check(library().vkEndCommandBuffer(handle));
		state = State.READY;
	}
}
```

# Pool

```java
class Pool extends AbstractVulkanObject {
	/**
	 * Creates a command pool for the given queue.
	 * @param queue		Work queue
	 * @param flags		Flags
	 */
	public static Pool create(Queue queue, VkCommandPoolCreateFlag... flags) {
		// Init pool descriptor
		final VkCommandPoolCreateInfo info = new VkCommandPoolCreateInfo();
		info.queueFamilyIndex = queue.family().index();
		info.flags = IntegerEnumeration.mask(Arrays.asList(flags));

		// Create pool
		final LogicalDevice dev = queue.device();
		final VulkanLibrary lib = dev.library();
		final PointerByReference pool = lib.factory().pointer();
		check(lib.vkCreateCommandPool(dev.handle(), info, null, pool));

		// Create pool
		return new Pool(pool.getValue(), queue);
	}

	private final Queue queue;
	private final Collection<Buffer> buffers = ConcurrentHashMap.newKeySet();

	...

	/**
	 * Allocates a number of command buffers from this pool.
	 * @param num			Number of buffers to allocate
	 * @param primary		Whether primary or secondary
	 * @return Allocated buffers
	 */
	public List<Buffer> allocate(int num, boolean primary) {
		// Init descriptor
		final VkCommandBufferAllocateInfo info = new VkCommandBufferAllocateInfo();
		info.level = primary ? VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_PRIMARY : VkCommandBufferLevel.VK_COMMAND_BUFFER_LEVEL_SECONDARY;
		info.commandBufferCount = num;
		info.commandPool = super.handle();

		// Allocate buffers
		final LogicalDevice dev = queue.device();
		final VulkanLibrary lib = dev.library();
		final Pointer[] handles = lib.factory().pointers(num);
		check(lib.vkAllocateCommandBuffers(dev.handle(), info, handles));

		// Create buffers
		final var list = Arrays
				.stream(handles)
				.map(ptr -> new Buffer(ptr, this))
				.collect(toList());

		// Register buffers
		buffers.addAll(list);

		return list;
	}

	@Override
	public synchronized void destroy() {
		buffers.clear();
		super.destroy();
	}
}
```

# Render Commands

```java
public static final Command END_COMMAND = (api, buffer) -> api.vkCmdEndRenderPass(buffer);

...

public Command begin(FrameBuffer buffer, Rectangle extent, Colour col) {
	// Create descriptor
	final VkRenderPassBeginInfo info = new VkRenderPassBeginInfo();
	info.renderPass = this.handle();
	info.framebuffer = buffer.handle();
	info.renderArea = ExtentHelper.of(extent);

	// Create clear colour
	final VkClearValue clear = new VkClearValue();
	clear.color = new VkClearColorValue();
	clear.color.float32 = col.toArray();

	// Add clear values
	info.clearValueCount = 1;
	info.pClearValues = StructureHelper.structures(List.of(clear));

	// Create command
	return (lib, ptr) -> lib.vkCmdBeginRenderPass(ptr, info, VkSubpassContents.VK_SUBPASS_CONTENTS_INLINE);
}
```

# Bind Pipeline Command

```java
public Command bind() {
	return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS, handle());
}
```

# Colour

```java
public record Colour(float red, float green, float blue, float alpha) {
	/**
	 * White colour.
	 */
	public static final Colour WHITE = new Colour(1, 1, 1, 1);

	/**
	 * Black colour.
	 */
	public static final Colour BLACK = new Colour(0, 0, 0, 1);

	public Colour {
		Check.isPercentile(red);
		Check.isPercentile(green);
		Check.isPercentile(blue);
		Check.isPercentile(alpha);
	}

	/**
	 * @return This colour as an RGBA array of floating-point values
	 */
	public float[] toArray() {
		return new float[]{red, green, blue, alpha};
	}

	@Override
	public String toString() {
		return Arrays.toString(toArray());
	}
}
```

# Frame Buffer

```java
public static FrameBuffer create(View view, RenderPass pass) {
	// Build descriptor
	final Image image = view.image();
	final Image.Extents extents = image.extents();
	final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
	info.renderPass = pass.handle();
	info.attachmentCount = 1;
	info.pAttachments = Handle.memory(new Handle[]{view.handle()});
	info.width = extents.width();
	info.height = extents.height();
	info.layers = 1; // TODO

	// Allocate frame buffer
	final LogicalDevice dev = view.device();
	final VulkanLibrary lib = dev.library();
	final PointerByReference buffer = lib.factory().pointer();
	check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

	// Create frame buffer
	return new FrameBuffer(buffer.getValue(), view);
}
```

# Work

```java
public interface Work {
	/**
	 * Submits this work to the given queue.
	 */
	void submit();

	public static class Builder {
		private final List<Handle> buffers = new ArrayList<>();
		private Queue queue;

		/**
		 * Adds a command buffer to be submitted.
		 * @param buffer Command buffer
		 * @throws IllegalArgumentException if all added command buffers do not share the same queue
		 */
		public Builder add(Command.Buffer buffer) {
			...
			buffers.add(buffer.handle());
			return this;
		}

		/**
		 * Constructs this work.
		 * @return New work
		 */
		public Work build() {
			// Create submission descriptor
			final VkSubmitInfo info = new VkSubmitInfo();

			// Populate command buffers
			info.commandBufferCount = buffers.size();
			info.pCommandBuffers = Handle.memory(buffers.toArray(Handle[]::new));

			// Create work
			return () -> {
				final VulkanLibrary lib = queue.device().library();
				check(lib.vkQueueSubmit(queue.handle(), 1, new VkSubmitInfo[]{info}, null));
			};
		}
	}
}
```

# Rendering

```java
// Create frame buffers
final var buffers = chain
		.views()
		.stream()
		.map(view -> FrameBuffer.create(view, pass))
		.collect(toList());

// Create command pool
final Command.Pool pool = Command.Pool.create(present);
final List<Command.Buffer> commands = pool.allocate(buffers.size());

// Record render commands
final Command draw = (api, handle) -> api.vkCmdDraw(handle, 3, 1, 0, 0);		// TODO - builder
final Colour grey = new Colour(0.3f, 0.3f, 0.3f, 1);
for(int n = 0; n < commands.size(); ++n) {
	commands
		.get(n)
		.begin()
			.add(pass.begin(buffers.get(n), rect, grey))
			.add(pipeline.bind())
			.add(draw)
			.add(RenderPass.END_COMMAND)
		.end();
}
```

# Render a frame

```java
chain.acquire(null, null);

new Work.Builder()
		.add(commands.get(index))
		.stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
		.build()
		.submit();

present.waitIdle();

chain.present(present, null);

Thread.sleep(1000);
```
