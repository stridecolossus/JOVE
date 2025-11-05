package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.VulkanObject;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.*;

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
	 * Indicates that this swapchain has been invalidated when acquiring or presenting a frame, generally when the window is resized or minimised.
	 */
	public static final class SwapchainInvalidated extends VulkanException {
		private SwapchainInvalidated(VkResult result) {
			super(result);
		}
	}

	/**
	 * Acquires the next swapchain image.
	 * @param semaphore		Optional semaphore signalled when the frame has been acquired
	 * @param fence			Optional fence
	 * @return Image index
	 * @throws IllegalArgumentException if both the semaphore and fence are {@code null}
	 * @throws SwapchainInvalidated if the swapchain image is {@link VkResult#ERROR_OUT_OF_DATE_KHR}
	 */
	public int acquire(VulkanSemaphore semaphore, Fence fence) throws SwapchainInvalidated {
		// Validate
		if((semaphore == null) && (fence == null)) {
			throw new IllegalArgumentException("Either a semaphore or fence must be provided");
		}

		// Retrieve next image index
		final var index = new IntegerReference();
		final VkResult result = library.vkAcquireNextImageKHR(this.device(), this, Long.MAX_VALUE, semaphore, fence, index);

		// Check result
		switch(result) {
			case SUCCESS, SUBOPTIMAL_KHR -> latest = index.get();
			case ERROR_OUT_OF_DATE_KHR -> throw new SwapchainInvalidated(result);
			default -> throw new VulkanException(result);
		}

		return latest;
	}

	/**
	 * Presents the next frame to this swapchain.
	 * @param queue				Presentation queue
	 * @param index				Swapchain image index
	 * @param semaphore			Wait semaphore
	 * @throws SwapchainInvalidated if the image cannot be presented
	 * @see PresentTaskBuilder
	 * @see #present(LogicalDevice, WorkQueue, VkPresentInfoKHR)
	 */
	public void present(WorkQueue queue, int index, VulkanSemaphore semaphore) throws SwapchainInvalidated {
		final VkPresentInfoKHR info = new PresentTaskBuilder()
				.image(this, index)
				.wait(semaphore)
				.build();

		present(library, queue, info);
	}

	/**
	 * Presents multiple swapchain images to the given presentation queue.
	 * @param library		Swapchain library
	 * @param queue			Presentation queue
	 * @param info			Presentation task
	 * @throws SwapchainInvalidated if a swapchain image is {@link VkResult#ERROR_OUT_OF_DATE_KHR} or {@link VkResult#SUBOPTIMAL_KHR}
	 * @see PresentTaskBuilder
	 */
	public static void present(Library library, WorkQueue queue, VkPresentInfoKHR info) throws SwapchainInvalidated {
		final VkResult result = library.vkQueuePresentKHR(queue, info);
		switch(result) {
			case ERROR_OUT_OF_DATE_KHR, SUBOPTIMAL_KHR -> throw new SwapchainInvalidated(result);
			default -> result.value();
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
		private final VulkanSurface surface;
		private final VkSurfaceCapabilitiesKHR capabilities;
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Set<VkSwapchainCreateFlagKHR> flags = new HashSet<>();
		private final Set<VkImageUsageFlag> usage = new HashSet<>();
		private ColourClearValue clear;

		/**
		 * Constructor.
		 * @param surface Rendering surface
		 */
		public Builder(VulkanSurface surface) {
			this.surface = requireNonNull(surface);
			this.capabilities = surface.capabilities();
			init();
		}

		/**
		 * Initialises the swapchain descriptor.
		 */
		private void init() {
			extents(capabilities.currentExtent);
			count(capabilities.minImageCount);
			transform(capabilities.currentTransform);
			format(VulkanSurface.defaultSurfaceFormat());
			arrays(1);
			mode(VkSharingMode.EXCLUSIVE);
			usage(VkImageUsageFlag.COLOR_ATTACHMENT);
			alpha(VkCompositeAlphaFlagKHR.OPAQUE);
			presentation(VulkanSurface.DEFAULT_PRESENTATION_MODE);
			clipped(true);
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
		 * Sets the number of images.
		 * @param num Number of images
		 * @throws IllegalArgumentException if the number of image is not supported by the surface
		 * @see VkSurfaceCapabilitiesKHR
		 */
		public Builder count(int num) {
			if((num < capabilities.minImageCount) || (num > capabilities.maxImageCount)) {
				throw new IllegalArgumentException("Invalid number of images: num=%d range=%d/%d".formatted(num, capabilities.minImageCount, capabilities.maxImageCount));
			}
			info.minImageCount = num;
			return this;
		}

		/**
		 * Sets the surface format.
		 * @param format Surface format
		 * @throws IllegalArgumentException if the given format is not supported by the surface
		 * @see VulkanSurface#equals(VkSurfaceFormatKHR)
		 */
		public Builder format(VkSurfaceFormatKHR format) {
			final boolean valid = surface
					.formats()
					.stream()
					.anyMatch(VulkanSurface.equals(format));

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
		private static Dimensions dimensions(VkExtent2D extents) {
			return new Dimensions(extents.width, extents.height);
		}

		/**
		 * Sets the image extent.
		 * @param extents Image extent
		 */
		public Builder extent(Dimensions extents) {
			// Check minimum extent
			final Dimensions min = dimensions(capabilities.minImageExtent);
			if(!extents.contains(min)) {
				throw new IllegalArgumentException("Extent is smaller than the supported minimum");
			}

			// Check maximum extent
			final Dimensions max = dimensions(capabilities.maxImageExtent);
			if(!max.contains(extents)) {
				throw new IllegalArgumentException("Extent is larger than the supported maximum");
			}

			// Populate extents
			final var structure = new VkExtent2D();
			structure.width = extents.width();
			structure.height = extents.height();
			extents(structure);

			return this;
		}

		private void extents(VkExtent2D extents) {
			info.imageExtent = requireNonNull(extents);
			// TODO - constrain by actual resolution using glfwGetFramebufferSize()
		}

		/**
		 * Sets the number of image array layers.
		 * @param layers Number of image array layers
		 * @throws IllegalArgumentException if the given number of array layers exceeds the maximum supported by the surface
		 * @see VkSurfaceCapabilitiesKHR#maxImageArrayLayers
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
		 * @throws IllegalArgumentException if the usage flag is not supported by the surface
		 */
		public Builder usage(VkImageUsageFlag usage) {
			validate(capabilities.supportedUsageFlags, usage);
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the image sharing mode.
		 * @param mode Sharing mode
		 */
		public Builder mode(VkSharingMode mode) {
			info.imageSharingMode = mode;
			return this;
		}

		/**
		 * Sets the surface transform.
		 * @param transform Surface transform
		 * @throws IllegalArgumentException if the transform is not supported by the surface
		 */
		public Builder transform(VkSurfaceTransformFlagKHR transform) {
			validate(capabilities.supportedTransforms, transform);
			info.preTransform = transform;
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the given alpha function is not supported by the surface
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			validate(capabilities.supportedCompositeAlpha, alpha);
			info.compositeAlpha = alpha;
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @implNote The {@link #mode} is a Vulkan structure compared by <b>identity</b>, i.e. this method assumes the given mode has been retrieved via {@link VulkanSurface#modes()}
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if the mode is not supported by the surface
		 * @see VulkanSurface#modes()
		 */
		public Builder presentation(VkPresentModeKHR mode) {
			if(!surface.modes().contains(mode)) {
				throw new IllegalArgumentException("Unsupported presentation mode: " + mode);
			}
			info.presentMode = mode;
			return this;
		}

		/**
		 * Sets whether the surface can be clipped.
		 * @param clipped Whether clipped
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
		 * @throws IllegalArgumentException if the image format or colour-space is not supported by the surface
		 */
		public Swapchain build(LogicalDevice device) {
			// Initialise swapchain descriptor
			populate();

			// Create swapchain
			final Library library = device.library();
			final Pointer ref = new Pointer();
			library.vkCreateSwapchainKHR(device, info, null, ref);

			// Retrieve swapchain images
			final Handle handle = ref.get();
			final Handle[] images = images(library, device, handle);

			// Create image views
			final Dimensions extents = dimensions(info.imageExtent);
			final List<View> views = views(device, images, info.imageFormat, extents, clear);

			// Create swapchain instance
			return new Swapchain(handle, device, library, info.imageFormat, extents, views);
		}

		/**
		 * Initialises the swapchain descriptor.
		 */
		private void populate() {
			info.surface = surface.handle();
			info.flags = new EnumMask<>(flags);
			info.imageUsage = new EnumMask<>(usage);
			info.oldSwapchain = null; // TODO
			// TODO
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;
		}

		/**
		 * @return Swapchain images
		 */
		private static Handle[] images(Library library, LogicalDevice device, Handle swapchain) {
			final VulkanFunction<Handle[]> images = (count, array) -> library.vkGetSwapchainImagesKHR(device, swapchain, count, array);
			return VulkanFunction.invoke(images, Handle[]::new);
		}

		/**
		 * Builds the swapchain image views.
		 * @param device		Logical device
		 * @param images		Images
		 * @param format		Image format
		 * @param extents		Extents
		 * @param clear			Clear value
		 * @return Swapchain image views
		 */
		private static List<View> views(LogicalDevice device, Handle[] images, VkFormat format, Dimensions extents, ClearValue clear) {
			// Build a common image descriptor
			final Image.Descriptor descriptor = new Image.Descriptor.Builder()
    				.format(format)
    				.extents(extents)
    				.aspect(VkImageAspect.COLOR)
    				.build();

			// Create a view for each swapchain image
			return Arrays
					.stream(images)
					.map(handle -> new SwapChainImage(handle, descriptor))
					.map(image -> new View.Builder().build(device, image))
					.peek(view -> view.clear(clear))
					.toList();
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
		VkResult vkGetSwapchainImagesKHR(LogicalDevice device, Handle swapchain, IntegerReference pSwapchainImageCount, @Returned Handle[] pSwapchainImages);

		/**
		 * Acquires the next image in the swapchain.
		 * @param device				Logical device
		 * @param swapchain				Swapchain
		 * @param timeout				Timeout (nanoseconds) or {@link Long#MAX_VALUE} to disable
		 * @param semaphore				Optional semaphore
		 * @param fence					Optional fence
		 * @param pImageIndex			Returned image index
		 * @return Result
		 */
		VkResult vkAcquireNextImageKHR(LogicalDevice device, Swapchain swapchain, long timeout, VulkanSemaphore semaphore, Fence fence, IntegerReference pImageIndex);

		/**
		 * Presents to the swapchain.
		 * @param queue					Presentation queue
		 * @param pPresentInfo			Descriptor
		 * @return Result
		 */
		VkResult vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo);
	}
}
