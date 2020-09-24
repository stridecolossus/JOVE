# API first-cut

```java
interface VulkanLibrary extends Library {
	static VulkanLibrary create() {
		return Native.load(library(), VulkanLibrary.class);
	}

	private static String library() {
		switch(Platform.getOSType()) {
		case Platform.WINDOWS:		return "vulkan-1";
		case Platform.LINUX:		return "libvulkan";
		default:					throw new UnsupportedOperationException("Unsupported platform: " + Platform.getOSType());
		}
	}
}
```

# Instance API

```java
interface VulkanLibraryInstance {
	/**
	 * Creates a vulkan instance.
	 * @param info			Instance descriptor
	 * @param allocator		Allocator
	 * @param instance		Returned instance
	 * @return Result
	 */
	int vkCreateInstance(VkInstanceCreateInfo info, Pointer allocator, PointerByReference instance);

	/**
	 * Destroys the vulkan instance.
	 * @param instance		Instance handle
	 * @param allocator		Allocator
	 */
	void vkDestroyInstance(Pointer instance, Pointer allocator);
}
```

# API grouping

```java
interface VulkanLibrary extends Library, VulkanLibraryInstance {
```

# Instance class

```java
public class Instance {
	private final VulkanLibrary api;
	private final Pointer handle;

	private Instance(VulkanLibrary api, Pointer handle) {
		this.api = notNull(api);
		this.handle = notNull(handle);
	}

	VulkanLibrary api() {
		return api;
	}

	public Pointer handle() {
		return handle;
	}

	public synchronized void destroy() {
		api.vkDestroyInstance(handle, null);
	}
}
```

# Builder

```java
public static class Builder {
	private VulkanLibrary api;
	private String name;
	private Version ver = VulkanLibrary.VERSION;
	private final Set<String> extensions = new HashSet<>();
	private final Set<ValidationLayer> layers = new HashSet<>();

	public Builder vulkan(VulkanLibrary api) {
		this.api = notNull(api);
		return this;
	}

	public Builder name(String name) {
		this.name = notEmpty(name);
		return this;
	}

	public Builder version(Version ver) {
		this.ver = notNull(ver);
		return this;
	}

	public Builder extension(String ext) {
		Check.notEmpty(ext);
		extensions.add(ext);
		return this;
	}

	public Builder layer(ValidationLayer layer) {
		Check.notNull(layer);
		layers.add(layer);
		return this;
	}

	public Instance build() {
	}
}
```

# Creating the instance

```java
// Init application descriptor
final VkApplicationInfo app = new VkApplicationInfo();
app.pEngineName = "JOVE";
app.pApplicationName = name;
app.apiVersion = ver.toInteger();

// Init instance descriptor
final VkInstanceCreateInfo info = new VkInstanceCreateInfo();
info.pApplicationInfo = app;

// Populate required extensions
info.ppEnabledExtensionNames = new StringArray(extensions.toArray(String[]::new));
info.enabledExtensionCount = extensions.size();

// Populate required layers
final String[] layerNames = layers.stream().map(ValidationLayer::name).toArray(String[]::new);
info.ppEnabledLayerNames = new StringArray(layerNames);
info.enabledLayerCount = layerNames.length;

// Create instance
final PointerByReference handle = new PointerByReference();
api.vkCreateInstance(info, null, handle);

// Create instance wrapper
return new Instance(api, handle.getValue());
```

# Supporting functionality

```java
interface VulkanLibrary ... {
	/**
	 * Vulkan API version.
	 */
	Version VERSION = new Version(1, 0, 2);

	/**
	 * Successful result code.
	 */
	int SUCCESS = VkResult.VK_SUCCESS.value();

	/**
	 * Debug utility extension.
	 */
	String EXTENSION_DEBUG_UTILS = "VK_EXT_debug_utils";

	/**
	 * Swap-chain extension.
	 */
	String EXTENSION_SWAP_CHAIN = "VK_KHR_swapchain";

	/**
	 * Identifier for a Vulkan integration test.
	 */
	String INTEGRATION_TEST = "vulkan-integration-test";

	...

	/**
	 * @return Factory for pass-by-reference types used by this API
	 * @see ReferenceFactory#DEFAULT
	 */
	default ReferenceFactory factory() {
		return ReferenceFactory.DEFAULT;
	}

	/**
	 * Checks the result of a Vulkan operation.
	 * @param result Result code
	 * @throws VulkanException if the given result is not {@link VkResult#VK_SUCCESS}
	 */
	static void check(int result) {
		if(result != SUCCESS) {
			throw new VulkanException(result);
		}
	}
```

# Improvements to builder

```java
// Create instance
final PointerByReference handle = api.factory().pointer();
check(api.vkCreateInstance(info, null, handle));
```

# Test

```java
public class InstanceTest {
	private VulkanLibrary api;
	private Instance instance;

	@BeforeEach
	void before() {
		// Init API
		api = mock(VulkanLibrary.class);
		when(api.factory()).thenReturn(new MockReferenceFactory());

		// Create instance
		instance = new Instance.Builder()
				.vulkan(api)
				.name("test")
				.extension("ext")
				.layer(new ValidationLayer("layer"))
				.build();
	}
```

# Integration test

```java
	@Tag(INTEGRATION_TEST)
	@Test
	void build() {
		// Create real API
		api = VulkanLibrary.create();

		// Create instance
		instance = new Instance.Builder()
				.vulkan(api)
				.name("test")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();

		// Check instance
		assertNotNull(instance);
		assertNotNull(instance.handle());
		assertEquals(api, instance.api());

		// Destroy instance
		instance.destroy();
	}
```
