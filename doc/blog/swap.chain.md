# Swap Chain

```java
public class SwapChain {
	SwapChain(Pointer handle, LogicalDevice dev, VkFormat format, Dimensions extents, List<View> views) {
	}

	Pointer handle() {
	}

	public VkFormat format() {
	}

	public Dimensions extents() {
	}

	public List<View> images() {
	}

	/**
	 * Acquires the next image in this swap-chain.
	 */
	public int next() {
	}

	/**
	 * Presents the next frame.
	 * @param queue Presentation queue
	 */
	public void present(LogicalDevice.Queue queue) {
	}

	public void destroy() {
	}
}
```

# Initialise Swap Chain from Surface

```java
public static class Builder {
	private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
	private final Surface surface;
	private final VkSurfaceCapabilitiesKHR caps;
	private final Collection<VkSurfaceFormatKHR> formats;

	public Builder(Surface surface) {
		this.surface = notNull(surface);
		this.caps = surface.capabilities();
		this.formats = surface.formats();
		init();
	}

	private void init() {
		// Set surface
		info.surface = surface.handle();

		// Init from surface capabilities
		count(caps.minImageCount);
		transform(caps.currentTransform);
		info.imageExtent = caps.currentExtent;

		// Init default fields
		format(VkFormat.VK_FORMAT_R8G8B8A8_UNORM);
		space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
		arrays(1);
		mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE);
		usage(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
		alpha(VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
		present(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR);
		clipped(true);

		// TODO
		info.queueFamilyIndexCount = 0;
		info.pQueueFamilyIndices = null;
		info.oldSwapchain = null;
	}
	
	...
}
```

# Image

```java
public class Image {
	public record Extents(int width, int height, int depth) {
	}

	private final Pointer handle;
	private final LogicalDevice dev;
	private final VkFormat format;
	private final Extents extents;
	private final Set<VkImageAspectFlag> aspect;

	private VkImageLayout layout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;

	public Image(Pointer handle, LogicalDevice dev, VkFormat format, Extents extents, Set<VkImageAspectFlag> aspect) {
	}
}
```

# Image View

```java
public class View {
	private final Pointer handle;
	private final Image image;

	...

	public static class Builder {
		...
		
		public View build() {
			// Validate
			if(image == null) throw new IllegalArgumentException("Image not populated");

			// Allocate image view
			final LogicalDevice dev = image.device();
			final VulkanLibrary lib = dev.library();
			final PointerByReference view = lib.factory().pointer();
			check(lib.vkCreateImageView(dev.handle(), info, null, view));

			// Create image view
			return new View(view.getValue(), image);
		}
	}
}
```

# API

```java
interface VulkanLibrarySwapChain {
	int vkCreateSwapchainKHR(Pointer device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);
	void vkDestroySwapchainKHR(Pointer device, Pointer swapchain, Pointer pAllocator);
	int vkGetSwapchainImagesKHR(Pointer device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);
	int vkAcquireNextImageKHR(Pointer device, Pointer swapchain, long timeout, Pointer semaphore, Pointer fence, IntByReference pImageIndex);
}

interface VulkanLibraryImage {
	int vkCreateImage(Pointer device, VkImageCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pImage);
	void vkDestroyImage(Pointer device, Pointer image, Pointer pAllocator);
	int vkCreateImageView(Pointer device, VkImageViewCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pView);
	void vkDestroyImageView(Pointer device, Pointer imageView, Pointer pAllocator);
}
```


# Builder

```java
public SwapChain build() {
	// Allocate swap-chain
	final LogicalDevice dev = surface.device();
	final VulkanLibrary lib = dev.library();
	final ReferenceFactory factory = lib.factory();
	final PointerByReference chain = factory.pointer();
	check(lib.vkCreateSwapchainKHR(dev.handle(), info, null, chain));

	// Get swap-chain image views
	final VulkanFunction<Pointer[]> func = (api, count, array) -> api.vkGetSwapchainImagesKHR(dev.handle(), chain.getValue(), count, array);
	final var handles = VulkanFunction.enumerate(func, lib, factory::pointers);
	final var views = Arrays.stream(handles).map(this::view).collect(toList());

	// Create swap-chain
	final Dimensions extent = new Dimensions(info.imageExtent.width, info.imageExtent.height);
	return new SwapChain(chain.getValue(), dev, info.imageFormat, extent, views);
}

private View view(Pointer handle) {
	final Image.Extents extents = new Image.Extents(info.imageExtent.width, info.imageExtent.height);
	final Image image = new Image(handle, surface.device(), info.imageFormat, extents, Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));
	return image.view();
}
```

# Validation

```java
/**
 * Sets the surface transform.
 * @param transform Surface transform
 * @throws IllegalArgumentException if the transform is not supported by the surface
 */
public Builder transform(VkSurfaceTransformFlagKHR transform) {
	if(!IntegerEnumeration.contains(caps.supportedTransforms, transform)) {
		throw new IllegalArgumentException("Transform not supported: " + transform);
	}
	info.preTransform = notNull(transform);
	return this;
}
```

# Format Builder

```java
public VkFormat build() {
	// Build component layout
	final StringBuilder layout = new StringBuilder();
	for(int n = 0; n < num; ++n) {
		layout.append(components.charAt(n));
		layout.append(bytes * Byte.SIZE);
	}

	// Build format string
	final String format = new StringJoiner("_")
		.add("VK_FORMAT")
		.add(layout.toString())
		.add((signed ? "S" : "U") + type.name().toUpperCase())
		.toString();

	// Lookup format
	return VkFormat.valueOf(format.toString());
}
```

# Integration Test

```java
// Specify required image format
final VkFormat format = new FormatBuilder()
		.components(FormatBuilder.BGRA)
		.bytes(1)
		.signed(false)
		.type(Vertex.Component.Type.NORM)
		.build();

// Create swap-chain
final SwapChain chain = new SwapChain.Builder(surface)
		.format(format)
		.count(2)
		.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
		.build();
```
