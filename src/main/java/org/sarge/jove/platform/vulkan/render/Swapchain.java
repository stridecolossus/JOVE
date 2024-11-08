package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.image.Image.Descriptor;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.*;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

/**
 * A <i>swapchain</i> presents rendered images to a {@link Surface}.
 * <p>
 * A swapchain is comprised of an array of colour image <i>attachments</i>.
 * Note that the swapchain images are created and managed by Vulkan, however the application is responsible for allocating and releasing the {@link View} for each attachment.
 * <p>
 * The process of rendering a frame is comprised of two operations:
 * <ol>
 * <li>Acquire the index of the next swapchain image using {@link #acquire(Semaphore, Fence)}</li>
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

	private final VkFormat format;
	private final Dimensions extents;
	private final List<View> attachments;
	private int latest;

	/**
	 * Constructor.
	 * @param handle 			Swapchain handle
	 * @param device			Logical device
	 * @param format			Image format
	 * @param extents			Image extents
	 * @param attachments		Attachments
	 */
	Swapchain(Handle handle, DeviceContext device, VkFormat format, Dimensions extents, List<View> attachments) {
		super(handle, device);
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
	// TODO - mutable?

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
	public int acquire(Semaphore semaphore, Fence fence) throws SwapchainInvalidated {
		// Validate
		if((semaphore == null) && (fence == null)) {
			throw new IllegalArgumentException("Either a semaphore or fence must be provided");
		}

		// Retrieve next image index
		final DeviceContext dev = super.device();
		final VulkanLibrary lib = dev.library();
		final IntByReference index = dev.factory().integer();
		final VkResult result = lib.vkAcquireNextImageKHR(dev, this, Long.MAX_VALUE, semaphore, fence, index);

		// Check result
		switch(result) {
			case SUCCESS, SUBOPTIMAL_KHR -> latest = index.getValue();
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
	 * @see #present(DeviceContext, WorkQueue, VkPresentInfoKHR)
	 */
	public void present(WorkQueue queue, int index, Semaphore semaphore) throws SwapchainInvalidated {
		final VkPresentInfoKHR info = new PresentTaskBuilder()
				.image(this, index)
				.wait(semaphore)
				.build();

		present(this.device(), queue, info);
	}

	/**
	 * Presents multiple swapchain images to the given presentation queue.
	 * @param dev			Logical device
	 * @param queue			Presentation queue
	 * @param info			Presentation task
	 * @throws SwapchainInvalidated if a swapchain image is {@link VkResult#ERROR_OUT_OF_DATE_KHR} or {@link VkResult#SUBOPTIMAL_KHR}
	 * @see PresentTaskBuilder
	 */
	public static void present(DeviceContext dev, WorkQueue queue, VkPresentInfoKHR info) throws SwapchainInvalidated {
		final VulkanLibrary lib = dev.library();
		final VkResult result = lib.vkQueuePresentKHR(queue, info);
		switch(result) {
			case ERROR_OUT_OF_DATE_KHR, SUBOPTIMAL_KHR -> throw new SwapchainInvalidated(result);
			default -> check(result.value());
		}
	}

	/**
	 * The <i>presentation task builder</i> is used to construct the descriptor for swapchain presentation.
	 * @see Swapchain#present(DeviceContext, WorkQueue, VkPresentInfoKHR)
	 */
	public static class PresentTaskBuilder {
		private final Map<Swapchain, Integer> images = new LinkedHashMap<>();
		private final Set<Semaphore> semaphores = new HashSet<>();

		/**
		 * Adds a swapchain image to be presented.
		 * @param swapchain		Swapchain
		 * @param index			Image index
		 * @throws IllegalArgumentException for a duplicate swapchain
		 */
		public PresentTaskBuilder image(Swapchain swapchain, int index) {
			if(images.containsKey(swapchain)) throw new IllegalArgumentException("Duplicate swapchain: " + swapchain);
			images.put(swapchain, index);
			return this;
		}

		/**
		 * Adds a wait semaphore.
		 * @param semaphore Wait semaphore
		 */
		public PresentTaskBuilder wait(Semaphore semaphore) {
			semaphores.add(requireNonNull(semaphore));
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
			info.pWaitSemaphores = NativeObject.array(semaphores);

			// Populate swapchain
			info.swapchainCount = images.size();
			info.pSwapchains = NativeObject.array(images.keySet());

			// Set image indices
			final int[] array = images.values().stream().mapToInt(Integer::intValue).toArray();
			info.pImageIndices = new PointerToIntArray(array);

			return info;
		}
	}

	@Override
	protected Destructor<Swapchain> destructor(VulkanLibrary lib) {
		return lib::vkDestroySwapchainKHR;
	}

	@Override
	protected void release() {
		attachments.forEach(View::destroy);
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Surface surface;
		private final Set<VkSwapchainCreateFlagKHR> flags = new HashSet<>();
		private final Set<VkImageUsageFlag> usage = new HashSet<>();
		private VkSurfaceCapabilitiesKHR caps;
		private ColourClearValue clear;

		/**
		 * Constructor.
		 * @param surface Rendering surface
		 */
		public Builder(Surface surface) {
			this.surface = requireNonNull(surface);
			update();
			init();
		}

		// TODO - IF we recreate the swapchain then the surface capabilities need to be refreshed
		public void update() {
			caps = surface.capabilities();
		}

		/**
		 * Initialises the swapchain descriptor.
		 */
		private void init() {
			set(Surface.dimensions(caps.currentExtent));
			count(caps.minImageCount);
			transform(caps.currentTransform);
			format(Surface.defaultSurfaceFormat());
			arrays(1);
			mode(VkSharingMode.EXCLUSIVE);
			usage(VkImageUsageFlag.COLOR_ATTACHMENT);
			alpha(VkCompositeAlphaFlagKHR.OPAQUE);
			presentation(Surface.DEFAULT_PRESENTATION_MODE);
			clipped(true);
		}

		private static <E extends IntEnum> void validate(BitMask<E> mask, E e) {
			if(!mask.contains(e)) {
				throw new IllegalArgumentException("Unsupported property: " + e);
			}
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
		 * Sets the number of images.
		 * @param num Number of images
		 * @throws IllegalArgumentException if the number of image is not supported by the surface
		 * @see VkSurfaceCapabilitiesKHR
		 */
		public Builder count(int num) {
			if((num < caps.minImageCount) || (num > caps.maxImageCount)) {
				throw new IllegalArgumentException("Invalid number of images: num=%d range=%d/%d".formatted(num, caps.minImageCount, caps.maxImageCount));
			}
			info.minImageCount = num;
			return this;
		}

		/**
		 * Sets the surface format.
		 * @param format Surface format
		 * @throws IllegalArgumentException if the given format is not supported by the surface
		 */
		public Builder format(VkSurfaceFormatKHR format) {
			if(surface.format(format.format, format.colorSpace).isEmpty()) {
				throw new IllegalArgumentException(String.format("Unsupported surface format: format=%s space=%s", format.format, format.colorSpace));
			}
			info.imageFormat = requireNonNull(format.format);
			info.imageColorSpace = requireNonNull(format.colorSpace);
			return this;
		}

		/**
		 * Sets the image extent.
		 * @param extents Image extent
		 */
		public Builder extent(Dimensions extents) {
			// Check minimum extent
			final Dimensions min = Surface.dimensions(caps.minImageExtent);
			if(min.compareTo(extents) < 0) throw new IllegalArgumentException("Extent is smaller than the supported minimum");

			// Check maximum extent
			final Dimensions max = Surface.dimensions(caps.maxImageExtent);
			if(extents.compareTo(max) > 0) throw new IllegalArgumentException("Extent is larger than the supported maximum");

			// Populate extents
			set(extents);
			return this;
		}

		private void set(Dimensions extents) {
			info.imageExtent.width = extents.width();
			info.imageExtent.height = extents.height();
		}
		// TODO - constrain by actual resolution using glfwGetFramebufferSize()

		/**
		 * Sets the number of image array layers.
		 * @param layers Number of image array layers
		 * @throws IllegalArgumentException if the given number of array layers exceeds the maximum supported by the surface
		 * @see VkSurfaceCapabilitiesKHR#maxImageArrayLayers
		 */
		public Builder arrays(int layers) {
			if((layers < 1) || (layers > caps.maxImageArrayLayers)) throw new IllegalArgumentException("Invalid number of layers: layers=%d max=%d".formatted(layers, caps.maxImageArrayLayers));
			info.imageArrayLayers = layers;
			return this;
		}

		/**
		 * Sets the image usage flag.
		 * @param usage Image usage
		 * @throws IllegalArgumentException if the usage flag is not supported by the surface
		 */
		public Builder usage(VkImageUsageFlag usage) {
			validate(caps.supportedUsageFlags, usage);
			this.usage.add(usage);
			return this;
		}

		/**
		 * Sets the image sharing mode.
		 * @param mode Sharing mode
		 */
		public Builder mode(VkSharingMode mode) {
			info.imageSharingMode = requireNonNull(mode);
			return this;
		}

		/**
		 * Sets the surface transform.
		 * @param transform Surface transform
		 * @throws IllegalArgumentException if the transform is not supported by the surface
		 */
		public Builder transform(VkSurfaceTransformFlagKHR transform) {
			validate(caps.supportedTransforms, transform);
			info.preTransform = requireNonNull(transform);
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the given alpha function is not supported by the surface
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			validate(caps.supportedCompositeAlpha, alpha);
			info.compositeAlpha = requireNonNull(alpha);
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if the mode is not supported by the surface
		 * @see Surface#modes()
		 */
		public Builder presentation(VkPresentModeKHR mode) {
			if(!surface.modes().contains(mode)) throw new IllegalArgumentException("Unsupported presentation mode: " + mode);
			info.presentMode = requireNonNull(mode);
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
		 * Sets the clear colour for the swapchain images (default is no clear operation).
		 * @param clear Clear colour
		 */
		public Builder clear(Colour clear) {
			this.clear = new ColourClearValue(clear);
			return this;
		}

		/**
		 * Constructs this swapchain.
		 * @param Logical device
		 * @return New swapchain
		 * @throws IllegalArgumentException if the image format or colour-space is not supported by the surface
		 */
		public Swapchain build(DeviceContext dev) {
			// Init swapchain descriptor
			info.flags = new BitMask<>(flags);
			info.surface = surface.handle();
			info.imageUsage = new BitMask<>(usage);
			info.oldSwapchain = null; // TODO

			// TODO
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;

			// Create swapchain
			final VulkanLibrary lib = dev.library();
			final ReferenceFactory factory = dev.factory();
			final PointerByReference ref = factory.pointer();
			check(lib.vkCreateSwapchainKHR(dev, info, null, ref));

			// Retrieve swapchain images
			final Handle handle = new Handle(ref);
			final VulkanFunction<Pointer[]> images = (count, array) -> lib.vkGetSwapchainImagesKHR(dev, handle, count, array);
			final IntByReference count = factory.integer();
			final Pointer[] handles = VulkanFunction.invoke(images, count, Pointer[]::new);

			// Init swapchain image descriptor
			final Dimensions extents = new Dimensions(info.imageExtent.width, info.imageExtent.height);
			final Descriptor descriptor = new Descriptor.Builder()
					.format(info.imageFormat)
					.extents(extents)
					.aspect(VkImageAspect.COLOR)
					.build();

			// Create image views
			final var views = Arrays
					.stream(handles)
					.map(Handle::new)
					.map(image -> new SwapChainImage(image, descriptor))
					.map(image -> new View.Builder(image).build(dev))
					.toList();

			// Init clear operation
			for(View view : views) {
				view.clear(clear);
			}

			// Create swapchain
			return new Swapchain(handle, dev, info.imageFormat, extents, views);
		}

		/**
		 * Implementation for a swapchain image.
		 */
		private record SwapChainImage(Handle handle, Descriptor descriptor) implements Image {
			@Override
			public boolean equals(Object obj) {
				return obj == this;
			}
		}
	}

	/**
	 * Swapchain API.
	 */
	interface Library {
		/**
		 * Creates a swapchain for the given device.
		 * @param device			Logical device
		 * @param pCreateInfo		Swapchain descriptor
		 * @param pAllocator		Allocator
		 * @param pSwapchain		Returned swapchain
		 * @return Result
		 */
		int vkCreateSwapchainKHR(DeviceContext device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);

		/**
		 * Destroys a swapchain.
		 * @param device			Logical device
		 * @param swapchain			Swapchain
		 * @param pAllocator		Allocator
		 */
		void vkDestroySwapchainKHR(DeviceContext device, Swapchain swapchain, Pointer pAllocator);

		/**
		 * Retrieves swapchain image handles.
		 * @param device					Logical device
		 * @param swapchain					Swapchain handle
		 * @param pSwapchainImageCount		Number of images
		 * @param pSwapchainImages			Image handles
		 * @return Result code
		 */
		int vkGetSwapchainImagesKHR(DeviceContext device, Handle swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);

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
		VkResult vkAcquireNextImageKHR(DeviceContext device, Swapchain swapchain, long timeout, Semaphore semaphore, Fence fence, IntByReference pImageIndex);

		/**
		 * Presents to the swapchain.
		 * @param queue					Presentation queue
		 * @param pPresentInfo			Pointer to descriptor
		 * @return Result
		 */
		VkResult vkQueuePresentKHR(WorkQueue queue, VkPresentInfoKHR pPresentInfo);
	}
}
