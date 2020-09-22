# Layer record

```java
public record Layer(String name, int version) {
	public Layer {
		Check.notEmpty(name);
		Check.oneOrMore(version);
	}

	public boolean isMember(Set<Layer> layers) {
		if(layers.contains(this)) {
			return true;
		}

		return layers
				.stream()
				.filter(layer -> layer.name.equals(name))
				.anyMatch(this::matches);
	}

	private boolean matches(Layer that) {
		return version >= that.version;
	}
}
```

# Instance API

```java
interface InstanceLibrary {
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

	/**
	 * Enumerates the available extension properties.
	 * @param filter		Extension name filter or <code>null</code> for all
	 * @param count			Number of extensions
	 * @param extensions	Extensions
	 * @return Result
	 */
	int vkEnumerateInstanceExtensionProperties(String pLayerName, IntByReference count, VkExtensionProperties extensions);

	/**
	 * Enumerates the available validation layers.
	 * @param count			Number of layers
	 * @param layers		Layers
	 * @return Result
	 */
	int vkEnumerateInstanceLayerProperties(IntByReference count, VkLayerProperties layers);
}
```

# Aggregated API

```java
interface VulkanLibrary extends Library, InstanceLibrary {
```

# Extensions

```java
	/**
	 * @return Available extensions
	 */
	public Set<String> extensions() {
		// Determine number of extensions
		final IntByReference count = new IntByReference();
		int result = api.vkEnumerateInstanceExtensionProperties(null, count, null);
		if(result != VkResult.VK_SUCCESS.value()) throw new RuntimeException(String.format("Vulkan error: %d", result));

		// Allocate extensions array
		final VkExtensionProperties[] extensions = (VkExtensionProperties[]) new VkExtensionProperties().toArray(count.getValue());

		// Retrieve extensions
		result = api.vkEnumerateInstanceExtensionProperties(null, count, extensions[0]);
		if(result != VkResult.VK_SUCCESS.value()) throw new RuntimeException(String.format("Vulkan error: %d", result));

		// Extract name and convert to set
		return Arrays.stream(extensions)
				.map(e -> e.extensionName)
				.map(Native::toString)
				.collect(toSet());
	}
```
