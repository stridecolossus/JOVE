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
public static Predicate<QueueFamily> predicate(VkQueueFlag... flags) {
	Check.notNull(flags);
	return family -> family.flags.containsAll(Arrays.asList(flags));
}

public static Predicate<PhysicalDevice> predicate(Predicate<QueueFamily> predicate) {
	return dev -> dev.families.stream().anyMatch(predicate);
}

public static Predicate<PhysicalDevice> predicatePresentationSupported(Pointer surface) {
	return predicate(family -> family.isPresentationSupported(surface));
}

...

public QueueFamily find(Predicate<QueueFamily> test, String message) throws ServiceException {
	return families.stream().filter(test).findAny().orElseThrow(() -> new ServiceException(message));
}

```

# Demo

```java
// Open desktop
final Desktop desktop = Desktop.create();
if(!desktop.isVulkanSupported()) throw new ServiceException("Vulkan not supported");

// Create window
final Window window = new Window.Builder()
		.title("demo")
		.size(new Dimensions(1280, 760))
		.property(Window.Property.DISABLE_OPENGL)
		.build();

// Init Vulkan
final VulkanLibrary lib = VulkanLibrary.create();

// Create instance
final Instance instance = new Instance.Builder()
		.vulkan(lib)
		.name("test")
		.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
		.extensions(desktop.extensions())
		.layer(ValidationLayer.STANDARD_VALIDATION)
		.build();

// Lookup surface
final Pointer surfaceHandle = window.surface(instance.handle(), PointerByReference::new);

// Create queue family predicates
final var graphicsPredicate = PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
final var transferPredicate = PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_TRANSFER_BIT);

// Find GPU
final PhysicalDevice gpu = PhysicalDevice
		.devices(instance)
		.filter(PhysicalDevice.predicate(graphicsPredicate))
		.filter(PhysicalDevice.predicate(transferPredicate))
		.filter(PhysicalDevice.predicatePresentationSupported(surfaceHandle))
		.findAny()
		.orElseThrow(() -> new ServiceException("No GPU available"));

// Lookup required queues
final QueueFamily graphics = gpu.find(graphicsPredicate, "Graphics family not available");
final QueueFamily transfer = gpu.find(transferPredicate, "Transfer family not available");

// Create device
final LogicalDevice dev = new LogicalDevice.Builder()
		.parent(gpu)
		.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
		.layer(ValidationLayer.STANDARD_VALIDATION)
		.queue(graphics)
		.queue(transfer)
		.build();

// Create rendering surface
final Surface surface = new Surface(surfaceHandle, gpu);

//////////////

// Destroy window
surface.destroy();
window.destroy();
desktop.close();

// Destroy device
dev.destroy();
instance.destroy();
```
