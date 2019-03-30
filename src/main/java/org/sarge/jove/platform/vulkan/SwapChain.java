package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;
import static org.sarge.lib.util.Check.range;

import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Vulkan.ReferenceFactory;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>swap chain</i> presents rendered images to a {@link Surface}.
 * @author Sarge
 */
public class SwapChain extends LogicalDeviceHandle {
	private final VkFormat format;
	private final Dimensions extent;
	private final List<ImageView> views;
	private final IntByReference index;

	/**
	 * Constructor.
	 * @param handle 		Swap-chain handle
	 * @param dev			Logical device
	 * @param format		Image format
	 * @param extent		Image extent
	 * @param views			Image views
	 */
	SwapChain(Pointer handle, LogicalDevice dev, VkFormat format, Dimensions extent, List<ImageView> views) {
		super(handle, dev, lib -> lib::vkDestroySwapchainKHR);
		this.format = notNull(format);
		this.extent = notNull(extent);
		this.views = List.copyOf(views);
		this.index = dev.parent().vulkan().factory().integer();
	}

	/**
	 * @return Image format
	 */
	public VkFormat format() {
		return format;
	}

	/**
	 * @return Swap-chain extent
	 */
	public Dimensions extent() {
		return extent;
	}

	/**
	 * @return Image views
	 */
	public List<ImageView> images() {
		return views;
	}

	/**
	 * Acquires the next image in this swap-chain.
	 * @param semaphore		Optional semaphore
	 * @param fence			Optional fence
	 */
	public int next(PointerHandle semaphore, Fence fence) {
		final VulkanLibrarySwapChain lib = dev.parent().vulkan().library();
		lib.vkAcquireNextImageKHR(dev.handle(), super.handle(), Long.MAX_VALUE, toPointer(semaphore), toPointer(fence), index);
		return index.getValue();
	}

	private static Pointer toPointer(PointerHandle handle) {
		if(handle == null) {
			return null;
		}
		else {
			return handle.handle();
		}
	}

	/**
	 * Presents the next frame.
	 * @param frame Frame
	 * @param queue Presentation queue
	 */
	public void present(FrameState frame, WorkQueue queue) {
		// Create presentation descriptor
		final VkPresentInfoKHR info = new VkPresentInfoKHR();

		// Add semaphore
		final PointerHandle semaphore = frame.finished();
		if(semaphore != null) {
			info.waitSemaphoreCount = 1;
			info.pWaitSemaphores = StructureHelper.pointers(Arrays.asList(semaphore.handle()));
		}

		// Add swap-chains
		// TODO - multiple
		info.swapchainCount = 1;
		info.pSwapchains = StructureHelper.pointers(List.of(super.handle()));

		// Set image indices
		info.pImageIndices = StructureHelper.integers(new int[]{index.getValue()});

		// Present frame
		// TODO - check
		final VulkanLibrary lib = dev.parent().vulkan().library();
		lib.vkQueuePresentKHR(queue.handle(), new VkPresentInfoKHR[]{info});
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final Surface surface;
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();

		/**
		 * Constructor.
		 * <p>
		 * The following swap-chain descriptor fields are initialised from the surface capabilities:
		 * <ul>
		 * <li>minimum number of swap-chain images</li>
		 * <li>extent</li>
		 * <li>transform</li>
		 * </ul>
		 * @param dev			Device
		 * @param surface		Surface
		 * @see Surface#capabilities()
		 */
		public Builder(LogicalDevice dev, Surface surface) {
			this.dev = notNull(dev);
			this.surface = notNull(surface);
			init();
		}

		/**
		 * Initialises the swap-chain descriptor.
		 */
		private void init() {
			final VkSurfaceCapabilitiesKHR caps = surface.capabilities();
			// TODO - could invoke builder methods here => caps would HAVE to be valid
			info.surface = surface.handle();
			info.minImageCount = caps.minImageCount + 1;
			info.imageFormat = VkFormat.VK_FORMAT_R8G8B8A8_UNORM;
			info.imageColorSpace = VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR;
			info.imageExtent = caps.currentExtent;
			info.imageArrayLayers = 1;
			info.imageUsage = VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT;
			info.imageSharingMode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;
			info.preTransform = caps.currentTransform;
			info.compositeAlpha = VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR;
			info.presentMode = VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR;
			info.clipped = VulkanBoolean.FALSE;
			info.oldSwapchain = null;
		}

		/**
		 * Sets the number of images.
		 * @param num Number of images
		 * @throws IllegalArgumentException if the number of image is not supported by the surface
		 * @see VkSurfaceCapabilitiesKHR#minImageCount
		 * @see VkSurfaceCapabilitiesKHR#maxImageCount
		 */
		public Builder count(int num) {
			final VkSurfaceCapabilitiesKHR caps = surface.capabilities();
			info.minImageCount = range(num, caps.minImageCount, caps.maxImageCount);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Image format
		 * @throws IllegalArgumentException if the format is not supported by the surface
		 * @see VkSurfaceFormatKHR
		 */
		public Builder format(VkFormat format) {
			if(!surface.formats().stream().anyMatch(f -> f.format == format)) throw new IllegalArgumentException("Unsupported image format: " + format);
			info.imageFormat = format;
			return this;
		}

		/**
		 * Sets the colour-space.
		 * @param space Colour-space
		 * @throws IllegalArgumentException if the colour-space is not supported by the surface
		 * @see VkSurfaceFormatKHR
		 */
		public Builder colour(VkColorSpaceKHR space) {
			if(!surface.formats().stream().anyMatch(f -> f.colorSpace == space)) throw new IllegalArgumentException("Unsupported surface colour-space: " + space);
			info.imageColorSpace = space;
			return this;
		}

		/**
		 * Sets the image extent.
		 * @param extent Image extent
		 */
		public Builder extent(Dimensions extent) {
			// Check minimum extent
			final VkSurfaceCapabilitiesKHR caps = surface.capabilities();
			final Dimensions min = new Dimensions(caps.minImageExtent.width, caps.minImageExtent.height);
			if(min.exceeds(extent)) throw new IllegalArgumentException("Extent is smaller than the supported minimum");

			// Check maximum extent
			final Dimensions max = new Dimensions(caps.maxImageExtent.width, caps.maxImageExtent.height);
			if(extent.exceeds(max)) throw new IllegalArgumentException("Extent is larger than the supported maximum");

			// Set extent
			final VkExtent2D result = new VkExtent2D();
			result.width = extent.width();
			result.height = extent.height();
			info.imageExtent = result;

			return this;
		}

		/**
		 * Sets the number of image array layers.
		 * @param layers Number of image array layers
		 * @throws IllegalArgumentException if the given number of array layers exceeds the maximum supported by the surface
		 * @see VkSurfaceCapabilitiesKHR#maxImageArrayLayers
		 */
		public Builder arrays(int layers) {
			range(layers, 1, surface.capabilities().maxImageArrayLayers);
			info.imageArrayLayers = layers;
			return this;
		}

		/**
		 * Sets the image usage flag.
		 * @param usage Image usage
		 * @throws IllegalArgumentException if the usage flag is not supported by the surface
		 */
		public Builder usage(VkImageUsageFlag usage) {
			final int value = usage.value();
			if((surface.capabilities().supportedUsageFlags & value) == 0) throw new IllegalArgumentException("Usage not supported: " + usage);
			info.imageUsage = usage;
			return this;
		}

		/**
		 * Sets the image sharing mode.
		 * @param mode Sharing mode
		 */
		public Builder mode(VkSharingMode mode) {
			info.imageSharingMode = notNull(mode);
			return this;
		}

		//info.queueFamilyIndexCount = 0;
		//info.pQueueFamilyIndices = null;
		// TODO

		/**
		 * Sets the surface transform.
		 * @param transform Surface transform
		 * @throws IllegalArgumentException if the transform is not supported by the surface
		 */
		public Builder transform(VkSurfaceTransformFlagKHR transform) {
			final int value = transform.value();
			if((surface.capabilities().supportedTransforms & value) == 0) throw new IllegalArgumentException("Transform not supported: " + transform);
			info.preTransform = transform;
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the given alpha function is not supported
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			final int value = alpha.value();
			if((surface.capabilities().supportedCompositeAlpha & value) == 0) throw new IllegalArgumentException("Compositive alpha not supported: " + alpha);
			info.compositeAlpha = alpha;
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if the given mode is not supported by the surface
		 * @see Surface#modes()
		 */
		public Builder presentation(VkPresentModeKHR mode) {
			final var modes = surface.modes();
			if(!modes.contains(mode)) throw new IllegalArgumentException("Presentation mode not supported: " + mode);
			info.presentMode = mode;
			return this;
		}

		/**
		 * Sets whether the surface can be clipped.
		 * @param clipped Whether clipped
		 */
		public Builder clipped(boolean clipped) {
			//info.clipped = clipped;
			return this;
		}

		// TODO - old swap chain

		/**
		 * Constructs this swap-chain.
		 * @return New swap-chain
		 */
		public SwapChain build() {
			// Check mandatory fields
			if(info.imageExtent == null) throw new ServiceException("No extent specified for swap-chain");
			// TODO - check format matches surface.formats

			// Allocate swap-chain
			final Vulkan vulkan = dev.parent().vulkan();
			final VulkanLibrary lib = vulkan.library();
			final ReferenceFactory factory = vulkan.factory();
			final PointerByReference chain = factory.reference();
			check(lib.vkCreateSwapchainKHR(dev.handle(), info, null, chain));

			// Get swap-chain image views
			final VulkanFunction<Pointer[]> func = (count, array) -> lib.vkGetSwapchainImagesKHR(dev.handle(), chain.getValue(), count, array);
			final var handles = VulkanFunction.array(func, factory.integer(), factory::pointers);
			final var views = Arrays.stream(handles).map(this::image).map(this::view).collect(toList());

			// Create swap-chain
			final Dimensions extent = new Dimensions(info.imageExtent.width, info.imageExtent.height);
			return new SwapChain(chain.getValue(), dev, info.imageFormat, extent, views);
		}

		// TODO

		private VulkanImage image(Pointer handle) {
			// TODO - format.value()
			return new VulkanImage(handle, info.imageFormat, info.imageExtent);
		}

		private ImageView view(VulkanImage image) {
			return new ImageView.Builder(dev, image)
				.build();
		}
	}
}
