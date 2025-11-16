package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.util.*;
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
	 * @see PresentTaskBuilder
	 * @see #present(LogicalDevice, WorkQueue, VkPresentInfoKHR)
	 */
	public void present(WorkQueue queue, int index, VulkanSemaphore semaphore) throws Invalidated {
		final var builder = new PresentTaskBuilder();
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
	 * @see PresentTaskBuilder
	 */
	public static void present(Library library, WorkQueue queue, VkPresentInfoKHR info) throws Invalidated {
		final int code = library.vkQueuePresentKHR(queue, info);
		final VkResult result = MAPPING.map(code);
		switch(result) {
			case ERROR_OUT_OF_DATE_KHR, SUBOPTIMAL_KHR -> throw new Invalidated(result);
			default -> {
				if(result != VkResult.SUCCESS) {
					throw new VulkanException(result);
				}
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
	 * Indicates that this swapchain has been invalidated.
	 * This is generally caused by the window being resized or minimised.
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
	 * The <i>presentation task builder</i> is used to construct the descriptor for swapchain presentation.
	 * @see Swapchain#present(LogicalDevice, WorkQueue, VkPresentInfoKHR)
	 */
	public static class PresentTaskBuilder {
		private final Map<Swapchain, Integer> images = new LinkedHashMap<>();
		private final Set<VulkanSemaphore> semaphores = new HashSet<>();

		/**
		 * Adds a swapchain image to be presented.
		 * @param swapchain		Swapchain
		 * @param index			Image index
		 * @throws IllegalArgumentException for a duplicate swapchain
		 */
		public PresentTaskBuilder image(Swapchain swapchain, int index) {
			if(images.containsKey(swapchain)) {
				throw new IllegalArgumentException("Duplicate swapchain: " + swapchain);
			}
			images.put(swapchain, index);
			return this;
		}

		/**
		 * Adds a wait semaphore.
		 * @param semaphore Wait semaphore
		 */
		public PresentTaskBuilder wait(VulkanSemaphore semaphore) {
			semaphores.add(semaphore);
			return this;
		}

		/**
		 * Constructs this presentation task.
		 * @return Presentation task
		 */
		public VkPresentInfoKHR build() {
			// Create presentation descriptor
			final var info = new VkPresentInfoKHR();

			// Populate wait semaphores
			info.waitSemaphoreCount = semaphores.size();
			info.pWaitSemaphores = NativeObject.handles(semaphores);

			// Populate swapchain
			info.swapchainCount = images.size();
			info.pSwapchains = NativeObject.handles(images.keySet());

			// Set image indices
			info.pImageIndices = images.values().stream().mapToInt(Integer::intValue).toArray();
			// TODO - quadruple check this is same order as the keys!!!

			// TODO - what is pResults for?

			return info;
		}
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final VulkanSurface.Properties properties;
		private final VkSurfaceCapabilitiesKHR capabilities;
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Set<VkSwapchainCreateFlagKHR> flags = new HashSet<>();
		private final Set<VkImageUsageFlag> usage = new HashSet<>();
		private ColourClearValue clear;

		/**
		 * Constructor.
		 * @param properties Surface properties
		 */
		public Builder(VulkanSurface.Properties properties) {
			this.properties = requireNonNull(properties);
			this.capabilities = properties.capabilities();
			init();
			format(VulkanSurface.defaultSurfaceFormat());
			usage(VkImageUsageFlag.COLOR_ATTACHMENT);
		}

		private void init() {
			info.surface = properties.surface().handle();
			info.minImageCount = capabilities.minImageCount;
			info.preTransform = capabilities.currentTransform;
			info.imageArrayLayers = 1;
			info.compositeAlpha = VkCompositeAlphaFlagKHR.OPAQUE;
			info.imageExtent = capabilities.currentExtent;
			info.imageSharingMode = VkSharingMode.EXCLUSIVE;
			info.presentMode = VulkanSurface.DEFAULT_PRESENTATION_MODE;
			info.clipped = true;
		}

		private static <E extends IntEnum> void validate(EnumMask<E> mask, E e) {
			if(!mask.contains(e.value())) {
				throw new IllegalArgumentException("Unsupported property: " + e);
			}
		}

		/**
		 * Adds a creation flag for this swapchain.
		 * @param flag Creation flag
		 */
		public Builder flag(VkSwapchainCreateFlagKHR flag) {
			flags.add(flag);
			return this;
		}

		/**
		 * Sets the minimum number of images.
		 * @param count Number of images
		 * @throws IllegalArgumentException if the number of images is not supported by the surface
		 */
		public Builder count(int count) {
			final int max = capabilities.maxImageCount;
			if((count < capabilities.minImageCount) || ((max > 0) && (count > max))) {
				throw new IllegalArgumentException("Invalid number of images: num=%d range=%d/%d".formatted(count, capabilities.minImageCount, capabilities.maxImageCount));
			}
			info.minImageCount = count;
			return this;
		}

		/**
		 * Sets the surface format.
		 * @param format Surface format
		 * @throws IllegalArgumentException if the given format is not supported by the surface
		 */
		public Builder format(VkSurfaceFormatKHR format) {
			final boolean valid = properties
					.formats()
					.stream()
					.anyMatch(VulkanSurface.Properties.equals(format));

			if(!valid) {
				throw new IllegalArgumentException(String.format("Unsupported surface format: format=%s space=%s", format.format, format.colorSpace));
			}

			info.imageFormat = format.format;
			info.imageColorSpace = format.colorSpace;

			return this;
		}

		/**
		 * Helper.
		 * @return Extents as width-height dimensions
		 */
		private static Dimensions toDimensions(VkExtent2D extents) {
			return new Dimensions(extents.width, extents.height);
		}

		/**
		 * Sets the image extents.
		 * @param extents Image extents
		 * @throws IllegalArgumentException if {@link #extents} does not match the min/max extents supported by the surface
		 */
		public Builder extents(Dimensions extents) {
			// Check minimum extent
			final Dimensions min = toDimensions(capabilities.minImageExtent);
			if(!extents.contains(min)) {
				throw new IllegalArgumentException("Extent is smaller than the supported minimum");
			}

			// Check maximum extent
			final Dimensions max = toDimensions(capabilities.maxImageExtent);
			if(!max.contains(extents)) {
				throw new IllegalArgumentException("Extent is larger than the supported maximum");
			}

			// Populate extents
			info.imageExtent.width = extents.width();
			info.imageExtent.height = extents.height();

			return this;
		}

		/**
		 * Sets the image extents of this swapchain to match the given window.
		 * @param window Window
		 */
		public Builder extents(Window window) {
			info.imageExtent = select(capabilities, window);
			return this;
		}
		// TODO - test, doc explanation

		private static VkExtent2D select(VkSurfaceCapabilitiesKHR capabilities, Window window) {
			// Try using the current extents of the surface
			if(capabilities.currentExtent.width < Integer.MAX_VALUE) {
				return capabilities.currentExtent;
			}

			// Otherwise query the actual surface size (in pixels)
			final Dimensions size = window.size();

			// Constraint to min/max extents
			final var min = capabilities.minImageExtent;
			final var max = capabilities.minImageExtent;
			final var extents = new VkExtent2D();
			extents.width = Math.clamp(size.width(), min.width, max.width);
			extents.height = Math.clamp(size.height(), min.height, max.height);
			return extents;
		}

		/**
		 * Sets the number of image array layers.
		 * @param layers Number of image array layers
		 * @throws IllegalArgumentException if the given number of array layers exceeds the maximum supported by the surface
		 */
		public Builder arrays(int layers) {
			if((layers < 1) || (layers > capabilities.maxImageArrayLayers)) {
				throw new IllegalArgumentException("Invalid number of layers: layers=%d max=%d".formatted(layers, capabilities.maxImageArrayLayers));
			}
			info.imageArrayLayers = layers;
			return this;
		}

		/**
		 * Sets the image usage flag.
		 * @param usage Image usage
		 * @throws IllegalArgumentException if {@link #usage} is not supported by the surface
		 */
		public Builder usage(VkImageUsageFlag usage) {
			validate(capabilities.supportedUsageFlags, usage);
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the sharing mode as {@link VkSharingMode#CONCURRENT} for the case where swapchain images can be shared across multiple queue families without ownership transfers.
		 * If a single queue family is used for both rendering and presentation the mode can remain as the default {@link VkSharingMode#EXCLUSIVE}.
		 * @param families Shared queue families
		 */
		public Builder concurrent(List<Family> families) {
			info.imageSharingMode = VkSharingMode.CONCURRENT;
			info.queueFamilyIndexCount = families.size();
			info.pQueueFamilyIndices = families.stream().mapToInt(Family::index).toArray();
			return this;
		}

		/**
		 * Sets the surface transform.
		 * @param transform Surface transform
		 * @throws IllegalArgumentException if {@link #transform} is not supported by the surface
		 */
		public Builder transform(VkSurfaceTransformFlagKHR transform) {
			validate(capabilities.supportedTransforms, transform);
			info.preTransform = transform;
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the {@link #alpha} function is not supported by the surface
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			validate(capabilities.supportedCompositeAlpha, alpha);
			info.compositeAlpha = alpha;
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if {@link #mode} is not supported by the surface
		 * @implNote The {@link #mode} is a Vulkan structure compared by <b>identity</b>, i.e. this method assumes the given mode has been retrieved via {@link VulkanSurface#modes()}
		 * @see VulkanSurface#modes()
		 */
		public Builder presentation(VkPresentModeKHR mode) {
			if(!properties.modes().contains(mode)) {
				throw new IllegalArgumentException("Unsupported presentation mode: " + mode);
			}
			info.presentMode = mode;
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
		 * Constructs this swapchain.
		 * @param device Logical device
		 * @return New swapchain
		 */
		public Swapchain build(LogicalDevice device) {
			// Initialise swapchain descriptor
			info.flags = new EnumMask<>(flags);
			info.imageUsage = new EnumMask<>(usage);

			// Create swapchain
			final Library library = device.library();
			final Pointer handle = new Pointer();
			library.vkCreateSwapchainKHR(device, info, null, handle);

			// Retrieve swapchain images
			final VulkanFunction<Handle[]> function = (count, array) -> library.vkGetSwapchainImagesKHR(device, handle.get(), count, array);
			final Handle[] images = VulkanFunction.invoke(function, Handle[]::new);

			// Build the common image descriptor for the views
			final var extents = toDimensions(info.imageExtent);
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
