package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.core.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.Image;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.View;
import org.sarge.jove.platform.vulkan.util.ExtentHelper;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.StructureHelper;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>swap chain</i> presents rendered images to a {@link Surface}.
 * @author Sarge
 */
public class SwapChain extends AbstractVulkanObject {
	private final VkFormat format;
	private final Dimensions extents;
	private final List<View> views;
	private final IntByReference index = new IntByReference();

	/**
	 * Constructor.
	 * @param handle 		Swap-chain handle
	 * @param dev			Logical device
	 * @param format		Image format
	 * @param views			Image views
	 */
	SwapChain(Pointer handle, LogicalDevice dev, VkFormat format, List<View> views) {
		super(handle, dev, dev.library()::vkDestroySwapchainKHR);
		final Image.Extents dim = views.get(0).descriptor().extents();
		this.format = notNull(format);
		this.extents = new Dimensions(dim.width(), dim.height());
		this.views = List.copyOf(views);
	}

	/**
	 * @return Image format
	 */
	public VkFormat format() {
		return format;
	}

	/**
	 * @return Swap-chain extents
	 */
	public Dimensions extents() {
		return extents;
	}

	/**
	 * @return Image views
	 */
	public List<View> views() {
		return views;
	}

	/**
	 * Acquires the next image in this swap-chain.
	 * @param semaphore		Optional semaphore
	 * @param fence			Optional fence
	 */
	public int acquire(Semaphore semaphore, Fence fence) {
		check(device().library().vkAcquireNextImageKHR(device().handle(), this.handle(), Long.MAX_VALUE, null /*semaphore.handle()*/, null, index)); // toPointer(semaphore), toPointer(fence), index);
		return index.getValue();
	}

	/**
	 * Presents the next frame.
	 * @param
	 * @param queue Presentation queue
	 */
	public void present(LogicalDevice.Queue queue, Semaphore semaphore) {
		// Create presentation descriptor
		final VkPresentInfoKHR info = new VkPresentInfoKHR();

		// Add semaphore
//		final PointerHandle semaphore = frame.finished();
//		if(semaphore != null) {
//			info.waitSemaphoreCount = 1;
//			info.pWaitSemaphores = StructureHelper.pointers(Arrays.asList(semaphore.handle()));
//		}

//		info.waitSemaphoreCount = 1; // semaphores.size();
//		info.pWaitSemaphores = Handle.memory(new Handle[]{semaphore.handle()});

		// Add swap-chains
		info.swapchainCount = 1;
		info.pSwapchains = Handle.memory(new Handle[]{this.handle()});

		// Set image indices
		info.pImageIndices = StructureHelper.integers(new int[]{index.getValue()});

		// Present frame
		final VulkanLibrary lib = device().library();
		check(lib.vkQueuePresentKHR(queue.handle(), new VkPresentInfoKHR[]{info}));
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Surface surface;
		private final VkSurfaceCapabilitiesKHR caps;
		private final Collection<VkSurfaceFormatKHR> formats;

		/**
		 * Constructor.
		 * <p>
		 * The following swap-chain descriptor fields are initialised from the surface capabilities:
		 * <ul>
		 * <li>minimum number of swap-chain images</li>
		 * <li>extent</li>
		 * <li>transform</li>
		 * </ul>
		 * @see Surface#capabilities()
		 */
		public Builder(Surface surface) {
			this.surface = notNull(surface);
			this.caps = surface.capabilities();
			this.formats = surface.formats();
			init();
		}

		/**
		 * Initialises the swap-chain descriptor.
		 */
		private void init() {
			// Set surface
			info.surface = surface.handle();

			// Init from surface capabilities
			count(caps.minImageCount);
			transform(caps.currentTransform);

			// Init swapchain extents
			info.imageExtent = caps.currentExtent;

			// Init default fields
			space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR);
			arrays(1);
			mode(VkSharingMode.VK_SHARING_MODE_EXCLUSIVE); // or concurrent?
			usage(VkImageUsageFlag.VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);
			alpha(VkCompositeAlphaFlagKHR.VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
			present(VkPresentModeKHR.VK_PRESENT_MODE_FIFO_KHR);
			clipped(true);

			// TODO
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;
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
			info.minImageCount = Check.range(num, caps.minImageCount, caps.maxImageCount);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Image format
		 * @throws IllegalArgumentException if the format is not supported by the surface
		 * @see VkSurfaceFormatKHR
		 */
		public Builder format(VkFormat format) {
			if(!formats.stream().map(f -> f.format).anyMatch(format::equals)) throw new IllegalArgumentException("Unsupported image format: " + format);
			info.imageFormat = notNull(format);
			return this;
		}

		/**
		 * Sets the colour-space.
		 * @param space Colour-space
		 * @throws IllegalArgumentException if the colour-space is not supported by the surface
		 * @see VkSurfaceFormatKHR
		 */
		public Builder space(VkColorSpaceKHR space) {
			if(!formats.stream().map(f -> f.colorSpace).anyMatch(space::equals)) throw new IllegalArgumentException("Unsupported surface colour-space: " + space);
			info.imageColorSpace = notNull(space);
			return this;
		}

		/**
		 * Sets the image extent.
		 * @param extent Image extent
		 */
		public Builder extent(Dimensions extent) {
			// Check minimum extent
			final Dimensions min = new Dimensions(caps.minImageExtent.width, caps.minImageExtent.height);
			if(min.exceeds(extent)) throw new IllegalArgumentException("Extent is smaller than the supported minimum");

			// Check maximum extent
			final Dimensions max = new Dimensions(caps.maxImageExtent.width, caps.maxImageExtent.height);
			if(extent.exceeds(max)) throw new IllegalArgumentException("Extent is larger than the supported maximum");

			// Init extents
			info.imageExtent = ExtentHelper.of(extent);

			return this;
		}

		/**
		 * Sets the number of image array layers.
		 * @param layers Number of image array layers
		 * @throws IllegalArgumentException if the given number of array layers exceeds the maximum supported by the surface
		 * @see VkSurfaceCapabilitiesKHR#maxImageArrayLayers
		 */
		public Builder arrays(int layers) {
			Check.range(layers, 1, caps.maxImageArrayLayers);
			info.imageArrayLayers = layers;
			return this;
		}

		/**
		 * Sets the image usage flag.
		 * @param usage Image usage
		 * @throws IllegalArgumentException if the usage flag is not supported by the surface
		 */
		public Builder usage(VkImageUsageFlag usage) {
			if(!IntegerEnumeration.contains(caps.supportedUsageFlags, usage)) throw new IllegalArgumentException("Usage not supported: " + usage);
			info.imageUsage = notNull(usage);
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

		/**
		 * Sets the surface transform.
		 * @param transform Surface transform
		 * @throws IllegalArgumentException if the transform is not supported by the surface
		 */
		public Builder transform(VkSurfaceTransformFlagKHR transform) {
			if(!IntegerEnumeration.contains(caps.supportedTransforms, transform)) throw new IllegalArgumentException("Transform not supported: " + transform);
			info.preTransform = notNull(transform);
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the given alpha function is not supported
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			if(!IntegerEnumeration.contains(caps.supportedCompositeAlpha, alpha)) throw new IllegalArgumentException("Compositive alpha not supported: " + alpha);
			info.compositeAlpha = notNull(alpha);
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if the given mode is not supported by the surface
		 * @see Surface#modes()
		 */
		public Builder present(VkPresentModeKHR mode) {
			if(!surface.modes().contains(mode)) throw new IllegalArgumentException("Presentation mode not supported: " + mode);
			info.presentMode = notNull(mode);
			return this;
		}

		/**
		 * Sets whether the surface can be clipped.
		 * @param clipped Whether clipped
		 */
		public Builder clipped(boolean clipped) {
			info.clipped = VulkanBoolean.of(clipped);
			return this;
		}

		/**
		 * Constructs this swap-chain.
		 * @return New swap-chain
		 * @throws IllegalArgumentException if the image format has not been specified
		 */
		public SwapChain build() {
			// Validate
			if(info.imageFormat == null) throw new IllegalArgumentException("Image format not specified");

			// Allocate swap-chain
			final LogicalDevice dev = surface.device();
			final VulkanLibrary lib = dev.library();
			final ReferenceFactory factory = lib.factory();
			final PointerByReference chain = factory.pointer();
			check(lib.vkCreateSwapchainKHR(dev.handle(), info, null, chain));

			// Get swap-chain images
			final VulkanFunction<Pointer[]> func = (api, count, array) -> api.vkGetSwapchainImagesKHR(dev.handle(), chain.getValue(), count, array);
			final var handles = VulkanFunction.enumerate(func, lib, factory::pointers);

			// Create image views
			final Image.Extents extents = new Image.Extents(info.imageExtent.width, info.imageExtent.height);
			final var views = Arrays
					.stream(handles)
					.map(Handle::new)
					.map(image -> view(image, extents))
					.collect(toList());

			// Create swap-chain
			return new SwapChain(chain.getValue(), dev, info.imageFormat, views);
		}

		/**
		 * Creates an image-view from the given swapchain image.
		 * @return New swapchain image-view
		 */
		private View view(Handle handle, Image.Extents extents) {
			final Image.Descriptor descriptor = new Image.Descriptor(handle, VkImageType.VK_IMAGE_TYPE_2D, info.imageFormat, extents, Set.of(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT));
			return new View.Builder(surface.device()).image(descriptor).build();
		}
	}
}
