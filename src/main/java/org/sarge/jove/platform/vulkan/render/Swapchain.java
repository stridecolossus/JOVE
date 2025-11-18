package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireOneOrMore;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.util.EnumMask;
import org.sarge.jove.util.IntEnum.ReverseMapping;

/**
 * A <i>swapchain</i> presents rendered images to a {@link VulkanSurface}.
 * <p>
 * A swapchain is comprised of an array of colour image <i>attachments</i>.
 * Note that the swapchain images are created and managed by Vulkan, however the application is responsible for allocating and releasing the {@link View} for each attachment.
 * <p>
 * The process of rendering a frame is comprised of two operations:
 * <ol>
 * <li>Acquire the index of the next swapchain image using {@link #acquire(VulkanSemaphore, Fence)}</li>
 * <li>Present a rendered frame to the surface using {@link #present(WorkQueue, int, Set)}</li>
 * </ol>
 * <p>
 * @author Sarge
 */
public class Swapchain extends VulkanObject {
	/**
	 * Swap-chain extension name.
	 */
	public static final String EXTENSION = "VK_KHR_swapchain";

	private static final ReverseMapping<VkResult> MAPPING = ReverseMapping.mapping(VkResult.class);

	private final Library library;
	private final VkFormat format;
	private final Dimensions extents;
	private final List<View> attachments;
	private int latest;

	/**
	 * Constructor.
	 * @param handle 			Swapchain handle
	 * @param device			Logical device
	 * @param library			Swapchain library
	 * @param format			Image format
	 * @param extents			Image extents
	 * @param attachments		Attachments
	 */
	Swapchain(Handle handle, LogicalDevice device, Library library, VkFormat format, Dimensions extents, List<View> attachments) {
		super(handle, device);
		this.library = requireNonNull(library);
		this.format = requireNonNull(format);
		this.extents = requireNonNull(extents);
		this.attachments = List.copyOf(attachments);
	}

	/**
	 * @return Swapchain Image format
	 */
	public VkFormat format() {
		return format;
	}

	/**
	 * @return Swapchain extents
	 */
	public Dimensions extents() {
		return extents;
	}

	/**
	 * @return Colour attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	/**
	 * @return Last rendered swapchain image
	 */
	public View latest() {
		return attachments.get(latest);
	}

	/**
	 * Acquires the next swapchain image.
	 * @param semaphore		Optional semaphore signalled when the frame has been acquired
	 * @param fence			Optional fence
	 * @return Image index
	 * @throws IllegalArgumentException if both the semaphore and fence are {@code null}
	 * @throws Invalidated if the swapchain image is {@link VkResult#ERROR_OUT_OF_DATE_KHR}
	 */
	public int acquire(VulkanSemaphore semaphore, Fence fence) throws Invalidated {
		// Validate
		if((semaphore == null) && (fence == null)) {
			throw new IllegalArgumentException("Either a semaphore or fence must be provided");
		}

		// Retrieve next image index
		final var index = new IntegerReference();
		final int code = library.vkAcquireNextImageKHR(this.device(), this, Long.MAX_VALUE, semaphore, fence, index);
		final VkResult result = MAPPING.map(code);

		// Check result
		switch(result) {
			case SUCCESS, SUBOPTIMAL_KHR -> latest = index.get();
			case ERROR_OUT_OF_DATE_KHR -> throw new Invalidated(result);
			default -> throw new VulkanException(result);
		}

		return latest;
	}

	/**
	 * Presents the next frame to this swapchain.
	 * @param queue				Presentation queue
	 * @param index				Swapchain image index
	 * @param semaphore			Wait semaphore
	 * @throws Invalidated if the image cannot be presented
	 * @see PresentationTaskBuilder
	 * @see #present(LogicalDevice, WorkQueue, VkPresentInfoKHR)
	 */
	public void present(WorkQueue queue, int index, VulkanSemaphore semaphore) throws Invalidated {
		final var builder = new PresentationTaskBuilder();
		builder.image(this, index);

		if(semaphore != null) {
			builder.wait(semaphore);
		}

		present(library, queue, builder.build());
	}

	/**
	 * Presents multiple swapchain images to the given presentation queue.
	 * @param library		Swapchain library
	 * @param queue			Presentation queue
	 * @param info			Presentation task
	 * @throws Invalidated if a swapchain image is {@link VkResult#ERROR_OUT_OF_DATE_KHR} or {@link VkResult#SUBOPTIMAL_KHR}
	 * @see PresentationTaskBuilder
	 */
	public static void present(Library library, WorkQueue queue, VkPresentInfoKHR info) throws Invalidated {
		final int code = library.vkQueuePresentKHR(queue, info);
		final VkResult result = MAPPING.map(code);
		if(result != VkResult.SUCCESS) {
    		switch(result) {
    			case ERROR_OUT_OF_DATE_KHR, SUBOPTIMAL_KHR -> throw new Invalidated(result);
    			default -> throw new VulkanException(result);
    		}
		}
	}

	@Override
	protected Destructor<Swapchain> destructor() {
		return library::vkDestroySwapchainKHR;
	}

	@Override
	protected void release() {
		attachments.forEach(View::destroy);
	}

	/**
	 * Indicates that this swapchain has been invalidated, generally caused by the window being resized or minimised.
	 */
	public static final class Invalidated extends VulkanException {
		private final VkResult result;

		Invalidated(VkResult result) {
			super(result);
			this.result = result;
		}

		public VkResult result() {
			return result;
		}
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Set<VkSwapchainCreateFlagKHR> flags = new HashSet<>();
		private final Set<VkImageUsageFlag> usage = new HashSet<>();
		private ColourClearValue clear;

		public Builder() {
			init();
			usage(VkImageUsageFlag.COLOR_ATTACHMENT);
		}

		private void init() {
			info.imageFormat = VkFormat.UNDEFINED;
			info.preTransform = VkSurfaceTransformFlagKHR.IDENTITY_KHR;
			info.imageArrayLayers = 1;
			info.compositeAlpha = VkCompositeAlphaFlagKHR.OPAQUE;
			info.imageSharingMode = VkSharingMode.EXCLUSIVE;
			info.presentMode = VulkanSurface.DEFAULT_PRESENTATION_MODE;
			info.clipped = true;
		}

		/**
		 * Initialises some of the properties of this swapchain according to the given surface capabilities.
		 * TODO - list
		 * @param capabilities Surface capabilities
		 */
		public Builder init(VkSurfaceCapabilitiesKHR capabilities) {
			info.minImageCount = capabilities.minImageCount;
			info.preTransform = capabilities.currentTransform;
			info.imageExtent = capabilities.currentExtent;
			return this;
		}

		/**
		 * Adds a creation flag for this swapchain.
		 * @param flag Creation flag
		 */
		public Builder flag(VkSwapchainCreateFlagKHR flag) {
			requireNonNull(flag);
			flags.add(flag);
			return this;
		}

		/**
		 * Sets the minimum number of images.
		 * @param count Number of images
		 */
		public Builder count(int count) {
			info.minImageCount = requireOneOrMore(count);
			return this;
		}

		/**
		 * Sets the surface format.
		 * @param format Surface format
		 */
		public Builder format(VkSurfaceFormatKHR format) {
			info.imageFormat = format.format;
			info.imageColorSpace = format.colorSpace;
			return this;
		}

		/**
		 * Sets the image extents.
		 * @param extents Image extents
		 */
		public Builder extent(Dimensions extent) {
			return extent(VulkanUtility.extent(extent));
		}

		/**
		 * Sets the image extents.
		 * @param extents Image extents
		 */
		public Builder extent(VkExtent2D extent) {
			info.imageExtent = requireNonNull(extent);
			return this;
		}

		/**
		 * Sets the number of image array layers.
		 * @param layers Number of image array layers
		 */
		public Builder arrays(int layers) {
			info.imageArrayLayers = requireOneOrMore(layers);
			return this;
		}

		/**
		 * Sets the image usage flag.
		 * @param usage Image usage
		 */
		public Builder usage(VkImageUsageFlag usage) {
			requireNonNull(usage);
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the sharing mode as {@link VkSharingMode#CONCURRENT} for the case where swapchain images are shared across multiple queue families without ownership transfers.
		 * If a single queue family is used for both rendering and presentation the mode can remain as the default {@link VkSharingMode#EXCLUSIVE}.
		 * @param families Shared queue families
		 */
		public Builder concurrent(Collection<Family> families) {
			info.imageSharingMode = VkSharingMode.CONCURRENT;
			info.queueFamilyIndexCount = families.size();
			info.pQueueFamilyIndices = families.stream().mapToInt(Family::index).toArray();
			return this;
		}

		/**
		 * Sets the surface transform.
		 * @param transform Surface transform
		 */
		public Builder transform(VkSurfaceTransformFlagKHR transform) {
			info.preTransform = requireNonNull(transform);
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			info.compositeAlpha = requireNonNull(alpha);
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 */
		public Builder presentation(VkPresentModeKHR mode) {
			info.presentMode = requireNonNull(mode);
			return this;
		}

		/**
		 * Sets whether the surface can be clipped, i.e. The surface is not a full screen window.
		 * @param clipped Whether this swapchain is clipped
		 */
		public Builder clipped(boolean clipped) {
			info.clipped = clipped;
			return this;
		}

		/**
		 * Sets the clear colour for the swapchain images.
		 * @param clear Clear colour
		 */
		public Builder clear(Colour clear) {
			this.clear = new ColourClearValue(clear);
			return this;
		}

		/**
		 * Helper.
		 * @return Extents as width-height dimensions
		 */
		private static Dimensions dimensions(VkExtent2D extents) {
			return new Dimensions(extents.width, extents.height);
		}

		private void validate(VulkanSurface.Properties properties) {
			final VkSurfaceCapabilitiesKHR capabilities = properties.capabilities();
			if((info.minImageCount < capabilities.minImageCount) || (info.minImageCount > capabilities.maxImageCount)) {
				throw new IllegalArgumentException("Image count %d out of range %d...%d".formatted(info.minImageCount, capabilities.minImageCount, capabilities.maxImageCount));
			}

			final var wrapper = new SurfaceFormatWrapper(info.imageFormat, info.imageColorSpace);
			if(!properties.formats().contains(wrapper)) {
				throw new IllegalArgumentException("Unsupported image format: " + wrapper);
			}

			final var extents = dimensions(info.imageExtent);
			final var min = dimensions(capabilities.minImageExtent);
			final var max = dimensions(capabilities.maxImageExtent);
			if(!extents.contains(min) || !max.contains(extents)) {
				throw new IllegalArgumentException("Invalid swapchain extents: actual=%s range=%s...%s".formatted(extents, min, max));
			}

			if(info.imageArrayLayers > capabilities.maxImageArrayLayers) {
				throw new IllegalArgumentException("Invalid number of layers: layers=%d max=%d".formatted(info.imageArrayLayers, capabilities.maxImageArrayLayers));
			}

			if(!capabilities.supportedUsageFlags.contains(info.imageUsage)) {
				throw new IllegalArgumentException("Unsupported image usage: " + info.imageUsage);
			}

			if(!capabilities.supportedTransforms.contains(info.preTransform.value())) {
				throw new IllegalArgumentException("Unsupported image transform: " + info.preTransform);
			}

			if(!capabilities.supportedCompositeAlpha.contains(info.compositeAlpha.value())) {
				throw new IllegalArgumentException("Unsupported composite alpha: " + info.compositeAlpha);
			}

			if(!properties.modes().contains(info.presentMode)) {
				throw new IllegalArgumentException("Unsupported presentation mode: " + info.presentMode);
			}

			if((info.imageSharingMode == VkSharingMode.EXCLUSIVE) ^ (info.queueFamilyIndexCount == 0)) {
				throw new IllegalArgumentException("Invalid sharing mode configuration");
			}
		}

		/**
		 * Constructs this swapchain.
		 * @param device Logical device
		 * @return New swapchain
		 * @throws IllegalArgumentException if the image format or extents have not been configured
		 * @throws IllegalArgumentException if any swapchain property is out-of-bounds or unsupported by the surface
		 */
		public Swapchain build(LogicalDevice device, VulkanSurface.Properties properties) {
			// Initialise swapchain descriptor
			requireNonNull(info.imageFormat, "Expected swapchain image format");
			requireNonNull(info.imageExtent, "Expected swapchain extent");
			info.surface = requireNonNull(properties.surface().handle());
			info.flags = new EnumMask<>(flags);
			info.imageUsage = new EnumMask<>(usage);
			validate(properties);

			// Create swapchain
			final Library library = device.library();
			final Pointer handle = new Pointer();
			library.vkCreateSwapchainKHR(device, info, null, handle);

			// Retrieve swapchain images
			final VulkanFunction<Handle[]> function = (count, array) -> library.vkGetSwapchainImagesKHR(device, handle.get(), count, array);
			final Handle[] images = VulkanFunction.invoke(function, Handle[]::new);

			// Build the common image descriptor for the views
			final var extents = dimensions(info.imageExtent);
			final var descriptor = new Image.Descriptor.Builder()
    				.format(info.imageFormat)
    				.extents(extents)
    				.aspect(VkImageAspect.COLOR)
    				.build();

			// Create image views
			final List<View> views = Arrays
					.stream(images)
					.map(image -> new SwapChainImage(image, descriptor))
					.map(image -> new View.Builder().build(device, image))
					.toList();

			// Initialise clear value
			if(clear != null) {
    			for(View view : views) {
    				view.clear(clear);
    			}
			}

			// Create swapchain instance
			return new Swapchain(handle.get(), device, library, info.imageFormat, extents, views);
		}

		/**
		 * Implementation for a swapchain image.
		 */
		private record SwapChainImage(Handle handle, Image.Descriptor descriptor) implements Image {
			@Override
			public boolean equals(Object obj) {
				return obj == this;
			}
		}
	}

	/**
	 * Swapchain API.
	 */
	public interface Library {
		/**
		 * Creates a swapchain for the given device.
		 * @param device			Logical device
		 * @param pCreateInfo		Swapchain descriptor
		 * @param pAllocator		Allocator
		 * @param pSwapchain		Returned swapchain handle
		 * @return Result
		 */
		VkResult vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Handle pAllocator, Pointer pSwapchain);

		/**
		 * Destroys a swapchain.
		 * @param device			Logical device
		 * @param swapchain			Swapchain
		 * @param pAllocator		Allocator
		 */
		void vkDestroySwapchainKHR(LogicalDevice device, Swapchain swapchain, Handle pAllocator);

		/**
		 * Retrieves swapchain image handles.
		 * @param device					Logical device
		 * @param swapchain					Swapchain handle
		 * @param pSwapchainImageCount		Number of images
		 * @param pSwapchainImages			Image handles
		 * @return Result code
		 */
		VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, @Updated Handle[] pSwapchainImages);

		/**
		 * Acquires the next image in the swapchain.
		 * @param device				Logical device
		 * @param swapchain				Swapchain
		 * @param timeout				Timeout (nanoseconds) or {@link Long#MAX_VALUE} to disable
		 * @param semaphore				Optional semaphore
		 * @param fence					Optional fence
		 * @param pImageIndex			Returned image index
		 * @return Success code
		 * @implNote Returns {@code int} since this method returns multiple success codes
		 */
		int vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex);

		/**
		 * Presents to the swapchain.
		 * @param queue					Presentation queue
		 * @param pPresentInfo			Descriptor
		 * @return Result
		 * @implNote Returns {@code int} since this method returns multiple success codes
		 */
		int vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo);
	}
}
