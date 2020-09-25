# Logical Device

```java
public class LogicalDevice {
	private final Pointer handle;
	private final PhysicalDevice parent;
	private final Map<QueueFamily, List<Queue>> queues;

	/**
	 * Constructor.
	 * @param handle Device handle
	 * @param parent Parent physical device
	 * @param queues Work queues
	 */
	private LogicalDevice(Pointer handle, PhysicalDevice parent, List<Queue> queues) {
		this.handle = notNull(handle);
		this.parent = notNull(parent);
		this.queues = queues.stream().collect(groupingBy(Queue::family));
	}

	/**
	 * @return Device handle
	 */
	Pointer handle() {
		return handle;
	}

	/**
	 * @return Parent physical device
	 */
	public PhysicalDevice parent() {
		return parent;
	}

	/**
	 * @return Work queues for this device ordered by family
	 */
	public Map<QueueFamily, List<Queue>> queues() {
		return queues;
	}

	/**
	 * Destroys this device.
	 */
	public void destroy() {
		final VulkanLibrary api = parent.instance().library();
		check(api.vkDestroyDevice(handle, null));
	}
}
```

# Work Queue

```java
public static class Queue {
	private final Pointer queue;
	private final QueueFamily family;
	private final VulkanLibrary lib;

	/**
	 * Constructor.
	 * @param handle 	Queue handle
	 * @param family 	Queue family
	 * @param lib		Vulkan library
	 */
	private Queue(Pointer handle, QueueFamily family, VulkanLibrary lib) {
		this.queue = notNull(handle);
		this.family = notNull(family);
		this.lib = notNull(lib);
	}

	/**
	 * @return Queue handle
	 */
	Pointer handle() {
		return queue;
	}

	/**
	 * @return Queue family
	 */
	public QueueFamily family() {
		return family;
	}
}
```

# Builder

```java
public static class Builder {
	private PhysicalDevice parent;
	private VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
	private final Set<String> extensions = new HashSet<>();
	private final Set<String> layers = new HashSet<>();

	/**
	 * Sets the parent of this device.
	 * @param parent Parent physical device
	 */
	public Builder parent(PhysicalDevice parent) {
		this.parent = notNull(parent);
		return this;
	}

	/**
	 * Sets the required features for this device.
	 * @param features Required features
	 */
	public Builder features(VkPhysicalDeviceFeatures features) {
		this.features = notNull(features);
		return this;
	}

	/**
	 * Adds an extension required for this device.
	 * @param ext Extension name
	 */
	public Builder extension(String ext) {
		Check.notEmpty(ext);
		extensions.add(ext);
		return this;
	}

	/**
	 * Adds a validation layer required for this device.
	 * @param layer Validation layer
	 */
	public Builder layer(ValidationLayer layer) {
		layers.add(layer.name());
		return this;
	}

	public LogicalDevice build() {
	}
}
```

# Builder failure tests

```java
@Nested
class BuilderTests {
	private LogicalDevice.Builder builder;

	@BeforeEach
	void before() {
		builder = new LogicalDevice.Builder().parent(parent);
	}

	@Test
	void missingParent() {
		assertThrows(IllegalArgumentException.class, () -> new LogicalDevice.Builder().build());
	}

	@Test
	void invalidPriority() {
		assertThrows(IllegalArgumentException.class, () -> builder.queues(family, new float[]{999}).build());
	}

	@Test
	void invalidQueueCount() {
		assertThrows(IllegalArgumentException.class, () -> builder.queues(family, 3).build());
	}

	@Test
	void invalidQueueFamily() {
		assertThrows(IllegalArgumentException.class, () -> builder.queue(mock(QueueFamily.class)).build());
	}
}
```

# Queue Specification

```java
public Builder queues(QueueFamily family, float[] priorities) {
	// Allocate contiguous memory block for the priorities
	final Memory mem = new Memory(priorities.length * Float.BYTES);
	mem.write(0, priorities, 0, priorities.length);

	// Init descriptor
	final VkDeviceQueueCreateInfo info = new VkDeviceQueueCreateInfo();
	info.queueCount = priorities.length;
	info.queueFamilyIndex = family.index();
	info.pQueuePriorities = mem;

	// Add queue
	queues.add(new QueueWrapper(info, family));

	return this;
}
```

# Creating the logical device

```java
public LogicalDevice build() {
	// Create descriptor
	if(parent == null) throw new IllegalArgumentException("Parent physical device not specified");
	final VkDeviceCreateInfo info = new VkDeviceCreateInfo();

	// Add required features
	info.pEnabledFeatures = features;

	// Add required extensions
	info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
	info.enabledExtensionCount = extensions.size();

	// Add validation layers
	info.ppEnabledLayerNames = new StringArray(layers.toArray(String[]::new));
	info.enabledLayerCount = layers.size();

	// Add queue descriptors
	info.queueCreateInfoCount = queues.size();
	info.pQueueCreateInfos = StructureHelper.structures(queues.stream().map(QueueWrapper::info).collect(toList()));

	// Allocate device
	final VulkanLibrary lib = parent.instance().library();
	final PointerByReference logical = lib.factory().pointer();
	check(lib.vkCreateDevice(parent.handle(), info, null, logical));

	// Enumerate work queues
	final var list = queues
			.stream()
			.flatMap(q -> q.create(lib, logical.getValue()))
			.collect(toList());

	// Create logical device
	return new LogicalDevice(logical.getValue(), parent, list);
}
```

# Wrapper

```java
private record QueueWrapper(VkDeviceQueueCreateInfo info, QueueFamily family) {
	public Stream<Queue> create(VulkanLibrary lib, Pointer dev) {
		return IntStream
				.range(0, info.queueCount)
				.mapToObj(n -> create(n, lib, dev));
	}

	private Queue create(int index, VulkanLibrary lib, Pointer dev) {
		final PointerByReference handle = lib.factory().pointer();
		lib.vkGetDeviceQueue(dev, family.index(), index, handle);
		return new Queue(handle.getValue(), family, lib);
	}
}
```
