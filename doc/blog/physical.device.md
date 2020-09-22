
# Class outline

```java
public class PhysicalDevice {
	/**
	 * Enumerates the physical devices for the given instance.
	 * @param instance Vulkan instance
	 * @return Physical devices
	 */
	public static PhysicalDevice devices(Instance instance) {
		return null;
	}

	private final Pointer handle;
	private final Vulkan vulkan;
	private final List<QueueFamily> families;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param vulkan		Vulkan
	 * @param families		Queue families
	 */
	PhysicalDevice(Pointer handle, Vulkan vulkan, List<QueueFamily> families) {
		this.handle = notNull(handle);
		this.vulkan = notNull(vulkan);
		this.families = List.copyOf(families);
	}

	/**
	 * @return Device handle
	 */
	public Pointer handle() {
		return handle;
	}

	/**
	 * @return Vulkan
	 */
	public Vulkan vulkan() {
		return vulkan;
	}

	/**
	 * @return Queue families for this device
	 */
	public List<QueueFamily> families() {
		return families;
	}

	/**
	 * @return Properties of this device
	 */
	public VkPhysicalDeviceProperties properties() {
		return null;
	}

	/**
	 * @return Memory properties of this device
	 */
	public VkPhysicalDeviceMemoryProperties memory() {
		return null;
	}

	/**
	 * @return Features supported by this device
	 */
	public VkPhysicalDeviceFeatures features() {
		return null;
	}

	/**
	 * @return Extensions supported by this device
	 */
	public Set<String> extensions() {
		return null;
	}

	/**
	 * @return Validation layers supported by this device
	 */
	public ValidationLayer layers() {
		return null;
	}
```

# Device functions

```java
	/**
	 * @return Properties of this device
	 */
	public VkPhysicalDeviceProperties properties() {
		final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		vulkan.api().vkGetPhysicalDeviceProperties(handle, props);
		return props;
	}
```

# Refactor extensions/layers

```java
	/**
	 * @return Extensions supported by this device
	 */
	public Set<String> extensions() {
		final VulkanFunction<VkExtensionProperties> func = (count, ext) -> vulkan.api().vkEnumerateDeviceExtensionProperties(handle, null, count, ext);
		return vulkan.extensions(func);
	}
```

```java
	/**
	 * @return Extensions supported by this Vulkan implementation
	 */
	public Set<String> extensions() {
		final VulkanFunction<VkExtensionProperties> func = (count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
		return extensions(func);
	}

	/**
	 * Helper - Enumerates extensions using the given API function.
	 * @return Extensions
	 */
	Set<String> extensions(VulkanFunction<VkExtensionProperties> func) {
		// Enumerate extensions
		final var extensions = VulkanFunction.enumerate(func, factory.integer(), new VkExtensionProperties());

		// Convert to string-set
		return Arrays
				.stream(extensions)
				.map(e -> e.extensionName)
				.map(Native::toString)
				.collect(toSet());
	}
```
