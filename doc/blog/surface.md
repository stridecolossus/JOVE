# Window surface

```java
public Pointer surface(Pointer vulkan, Supplier<PointerByReference> ref) {
	final PointerByReference handle = ref.get();
	final int result = instance.glfwCreateWindowSurface(vulkan, super.handle(), null, handle);
	if(result != 0) {
		throw new ServiceException("Cannot create Vulkan surface: result=" + result);
	}
	return handle.getValue();
}
```

# Surface class

```java
public class Surface {
	private final Pointer surface;
	private final PhysicalDevice dev;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param dev			Device
	 */
	public Surface(Pointer surface, PhysicalDevice dev) {
		this.dev = notNull(dev);
		this.surface = notNull(surface);
	}

	/**
	 * @return Surface handle
	 */
	Pointer handle() {
		return surface;
	}

	/**
	 * Destroys this surface.
	 */
	public synchronized void destroy() {
		final Instance instance = dev.instance();
		final VulkanLibrarySurface lib = instance.library();
		lib.vkDestroySurfaceKHR(instance.handle(), surface, null);
	}
}
```

# Accessors

```java
public VkSurfaceCapabilitiesKHR capabilities() {
	final VulkanLibrary lib = dev.instance().library();
	final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR.ByReference();
	check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.handle(), surface, caps));
	return caps;
}

public Collection<VkSurfaceFormatKHR> formats() {
	final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.handle(), surface, count, array);
	final var formats = VulkanFunction.enumerate(func, dev.instance().library(), new VkSurfaceFormatKHR());
	return Arrays.asList(formats);
}

public Set<VkPresentModeKHR> modes() {
	final VulkanFunction<VkPresentModeKHR[]> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), surface, count, array);
	final VkPresentModeKHR[] formats = VulkanFunction.enumerate(func, dev.instance().library(), VkPresentModeKHR[]::new);
	return new HashSet<>(Arrays.asList(formats));
}
```

# Testing for presentation support

```java
public boolean isPresentationSupported(Pointer surface) {
	final VulkanLibrary lib = instance.library();
	final IntByReference supported = lib.factory().integer();
	check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice.this.handle(), index(), surface, supported));
	return VulkanBoolean.of(supported.getValue()).isTrue();
}
```

# Helpers

```java
	/**
	 * Helper - Creates a predicate for a queue family matching the given flag.
	 * @param flag Queue family flag
	 * @return Predicate
	 * @see PhysicalDevice#find(Predicate, String)
	 */
	public static Predicate<QueueFamily> flag(VkQueueFlag flag) {
		Check.notNull(flag);
		return family -> family.flags.contains(flag);
	}

	/**
	 * Helper - Finds a matching queue family for this device.
	 * @param test			Queue family predicate
	 * @param message		Error message
	 * @return Matching queue family
	 * @throws ServiceException with the given message if a matching queue is not present
	 */
	public QueueFamily find(Predicate<QueueFamily> test, String message) throws ServiceException {
		return families.stream().filter(test).findAny().orElseThrow(() -> new ServiceException(message));
	}
```
