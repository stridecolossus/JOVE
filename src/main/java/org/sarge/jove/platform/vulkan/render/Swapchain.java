package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.io.ImageData.Extents;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.image.*;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.*;

/**
 * A <i>swapchain</i> presents rendered images to a {@link Surface}.
 * <p>
 * The process of rendering a frame is generally:
 * <ol>
 * <li>Acquire the next swapchain image via {@link #acquire(Semaphore, Fence)}</li>
 * <li>Wait for the previous frame to be rendered to that image (if in progress) using {@link #waitReady(int, Fence)}</li>
 * <li>Render the frame</li>
 * <li>Present the rendered frame to the surface using {@link #present(Queue, int, Set)}</li>
 * </ol>
 * <p>
 * Notes:
 * <ul>
 * <li>A swapchain is created and configured by the {@link Builder}</li>
 * <li>The swapchain is comprised of a number of colour attachments which can be accessed using {@link #attachments()}</li>
 * <li>The preferred presentation mode can be selected by the {@link #mode(Surface.Properties, VkPresentModeKHR...)} helper</li>
 * </ul>
 * <p>
 * @author Sarge
 */
public class Swapchain extends AbstractVulkanObject {
	private final VkFormat format;
	private final Dimensions extents;
	private final List<View> attachments;
	private final IntByReference index;
	private final Fence[] active;

	/**
	 * Constructor.
	 * @param handle 			Swapchain handle
	 * @param dev				Logical device
	 * @param format			Image format
	 * @param extents			Image extents
	 * @param attachments		Attachments
	 */
	Swapchain(Pointer handle, DeviceContext dev, VkFormat format, Dimensions extents, List<View> attachments) {
		super(handle, dev);
		this.format = notNull(format);
		this.extents = notNull(extents);
		this.attachments = List.copyOf(attachments);
		this.index = dev.factory().integer();
		this.active = new Fence[attachments.size()];
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
	 * @return Number of swapchain images
	 */
	public int count() {
		return attachments.size();
	}

	/**
	 * @return Attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	/**
	 * Indicates that the swapchain has been invalidated when acquiring or presenting a frame buffer.
	 * Note that {@link VkResult#SUBOPTIMAL_KHR} is considered as success
	 */
	public static final class SwapchainInvalidated extends VulkanException {
		private SwapchainInvalidated(int result) {
			super(result);
		}
	}

	private static void checkSwapchain(int result) {
		final boolean ok = (result == VulkanLibrary.SUCCESS) || (result == VkResult.SUBOPTIMAL_KHR.value());
		if(!ok) throw new SwapchainInvalidated(result);
	}

	/**
	 * Acquires the next image in this swap-chain.
	 * @param semaphore		Optional semaphore signalled when the frame has been acquired
	 * @param fence			Optional fence
	 * @return Image index
	 * @throws IllegalArgumentException if both the semaphore and fence are {@code null}
	 * @throws SwapchainInvalidated if the swapchain image cannot be acquired
	 */
	public int acquire(Semaphore semaphore, Fence fence) throws SwapchainInvalidated {
		if((semaphore == null) && (fence == null)) throw new IllegalArgumentException("Either semaphore or fence must be provided");
		final DeviceContext dev = super.device();
		final VulkanLibrary lib = dev.library();
		checkSwapchain(lib.vkAcquireNextImageKHR(dev, this, Long.MAX_VALUE, semaphore, fence, index));
		return index.getValue();
	}

	/**
	 * Waits for a previous image to be completed.
	 * @param index		Previous swapchain image index
	 * @param fence		Fence for next frame
	 */
	public void waitReady(int index, Fence fence) {
		final Fence prev = active[index];
		if(prev != null) {
			prev.waitReady();
		}
		active[index] = fence;
	}

	/**
	 * Presents the next frame.
	 * @param queue				Presentation queue
	 * @param index				Swapchain image index
	 * @param semaphores		Wait semaphores
	 * @throws SwapchainInvalidated if the swapchain image cannot be presented
	 */
	public void present(Queue queue, int index, Set<Semaphore> semaphores) throws SwapchainInvalidated {
		// Create presentation descriptor
		final VkPresentInfoKHR info = new VkPresentInfoKHR();

		// Populate wait semaphores
		info.waitSemaphoreCount = semaphores.size();
		info.pWaitSemaphores = NativeObject.array(semaphores);

		// Populate swap-chain
		info.swapchainCount = 1;
		info.pSwapchains = NativeObject.array(List.of(this));

		// Set image indices
		info.pImageIndices = new IntegerArray(new int[]{index});

		// Present frame
		final VulkanLibrary lib = device().library();
		checkSwapchain(lib.vkQueuePresentKHR(queue, info));
	}
	// TODO - cache descriptor -> factory -> work submit?

	@Override
	protected Destructor<Swapchain> destructor(VulkanLibrary lib) {
		return lib::vkDestroySwapchainKHR;
	}

	@Override
	protected void release() {
		attachments.forEach(View::destroy);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append("extents", extents)
				.append("format", format)
				.append("attachments", attachments.size())
				.build();
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Surface surface;
		private final VkSurfaceCapabilitiesKHR caps;
		private ClearValue clear;

		/**
		 * Constructor.
		 * @param dev			Logical device
		 * @param surface		Rendering surface
		 */
		public Builder(LogicalDevice dev, Surface surface) {
			this.dev = notNull(dev);
			this.surface = notNull(surface);
			this.caps = surface.capabilities();
			init();
		}

		/**
		 * Initialises the swap-chain descriptor.
		 */
		private void init() {
			extent(caps.currentExtent.width, caps.currentExtent.height);
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

		private static void validate(int bits, IntegerEnumeration e) {
			final Mask mask = new Mask(bits);
			if(!mask.contains(e.value())) throw new IllegalArgumentException("Unsupported property: " + e);
		}

		/**
		 * Sets the number of images.
		 * @param num Number of images
		 * @throws IllegalArgumentException if the number of image is not supported by the surface
		 * @see VkSurfaceCapabilitiesKHR
		 */
		public Builder count(int num) {
			info.minImageCount = Check.range(num, caps.minImageCount, caps.maxImageCount);
			return this;
		}

		/**
		 * Sets the surface format.
		 * @param format Surface format
		 */
		public Builder format(VkSurfaceFormatKHR format) {
			info.imageFormat = notNull(format.format);
			info.imageColorSpace = notNull(format.colorSpace);
			return this;
		}

		/**
		 * Sets the image extent.
		 * @param extent Image extent
		 */
		public Builder extent(Dimensions extent) {
			// Check minimum extent
			final Dimensions min = new Dimensions(caps.minImageExtent.width, caps.minImageExtent.height);
			if(min.compareTo(extent) < 0) throw new IllegalArgumentException("Extent is smaller than the supported minimum");

			// Check maximum extent
			final Dimensions max = new Dimensions(caps.maxImageExtent.width, caps.maxImageExtent.height);
			if(extent.compareTo(max) > 0) throw new IllegalArgumentException("Extent is larger than the supported maximum");

			// Populate extents
			extent(extent.width(), extent.height());
			return this;
		}
		// TODO - constrain by actual resolution using glfwGetFramebufferSize()

		private void extent(int w, int h) {
			info.imageExtent.width = w;
			info.imageExtent.height = h;
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
			validate(caps.supportedUsageFlags, usage);
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
			validate(caps.supportedTransforms, transform);
			info.preTransform = notNull(transform);
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the given alpha function is not supported by the surface
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			validate(caps.supportedCompositeAlpha, alpha);
			info.compositeAlpha = notNull(alpha);
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if the mode is not supported by the surface
		 * @see Surface.Properties#modes()
		 */
		public Builder presentation(VkPresentModeKHR mode) {
			if(!surface.modes().contains(mode)) throw new IllegalArgumentException("Unsupported presentation mode: " + mode);
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
		 * Sets the clear colour for the swapchain images (default is {@link Colour#BLACK}).
		 * @param clear Clear colour
		 */
		public Builder clear(Colour clear) {
			this.clear = new ColourClearValue(clear);
			return this;
		}

		/**
		 * Constructs this swap-chain.
		 * @return New swap-chain
		 * @throws IllegalArgumentException if the image format or colour-space is not supported by the surface
		 */
		public Swapchain build() {
			// Validate surface format
			if(surface.find(info.imageFormat, info.imageColorSpace).isEmpty()) {
				throw new IllegalArgumentException(String.format("Unsupported surface format: format=%s space=%s", info.imageFormat, info.imageColorSpace));
			}

			// Init swapchain descriptor
			info.surface = surface.handle();
			info.oldSwapchain = null; // TODO

			// TODO
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;

			// Create swapchain
			final VulkanLibrary lib = dev.library();
			final ReferenceFactory factory = dev.factory();
			final PointerByReference chain = factory.pointer();
			check(lib.vkCreateSwapchainKHR(dev, info, null, chain));

			// Retrieve swapchain images
			final VulkanFunction<Pointer[]> func = (count, array) -> lib.vkGetSwapchainImagesKHR(dev, chain.getValue(), count, array);
			final IntByReference count = factory.integer();
			final Pointer[] handles = VulkanFunction.invoke(func, count, Pointer[]::new);

			// Init swapchain image descriptor
			final Dimensions extents = new Dimensions(info.imageExtent.width, info.imageExtent.height);
			final ImageDescriptor descriptor = new ImageDescriptor.Builder()
					.format(info.imageFormat)
					.extents(new Extents(extents))
					.aspect(VkImageAspect.COLOR)
					.build();

			// Create image views
			final var views = Arrays
					.stream(handles)
					.map(Handle::new)
					.map(image -> new SwapChainImage(image, dev, descriptor))
					.map(View::of)
					.map(view -> view.clear(clear))
					.collect(toList());

			// Create domain object
			return new Swapchain(chain.getValue(), dev, info.imageFormat, extents, views);
		}

		/**
		 * Implementation for a swapchain image.
		 */
		private static class SwapChainImage implements Image {
			private final Handle handle;
			private final LogicalDevice dev;
			private final ImageDescriptor descriptor;

			/**
			 * Constructor.
			 * @param handle			Swapchain image
			 * @param dev				Logical device
			 * @param descriptor		Descriptor
			 */
			private SwapChainImage(Handle handle, LogicalDevice dev, ImageDescriptor descriptor) {
				this.handle = notNull(handle);
				this.dev = notNull(dev);
				this.descriptor = notNull(descriptor);
			}

			@Override
			public Handle handle() {
				return handle;
			}

			@Override
			public ImageDescriptor descriptor() {
				return descriptor;
			}

			@Override
			public LogicalDevice device() {
				return dev;
			}
		}
	}

	/**
	 * Swap-chain API.
	 */
	interface Library {
		/**
		 * Creates a swap-chain for the given device.
		 * @param device			Logical device
		 * @param pCreateInfo		Swap-chain descriptor
		 * @param pAllocator		Allocator
		 * @param pSwapchain		Returned swap-chain handle
		 * @return Result code
		 */
		int vkCreateSwapchainKHR(LogicalDevice device, VkSwapchainCreateInfoKHR pCreateInfo, Pointer pAllocator, PointerByReference pSwapchain);

		/**
		 * Destroys a swap-chain.
		 * @param device			Logical device
		 * @param swapchain			Swap-chain
		 * @param pAllocator		Allocator
		 */
		void vkDestroySwapchainKHR(DeviceContext device, Swapchain swapchain, Pointer pAllocator);

		/**
		 * Retrieves swap-chain image handles.
		 * @param device					Logical device
		 * @param swapchain					Swap-chain handle
		 * @param pSwapchainImageCount		Number of images
		 * @param pSwapchainImages			Image handles
		 * @return Result code
		 */
		int vkGetSwapchainImagesKHR(LogicalDevice device, Pointer swapchain, IntByReference pSwapchainImageCount, Pointer[] pSwapchainImages);

		/**
		 * Acquires the next image in the swap-chain.
		 * @param device				Logical device
		 * @param swapchain				Swap-chain
		 * @param timeout				Timeout (ns) or {@link Long#MAX_VALUE} to disable
		 * @param semaphore				Optional semaphore
		 * @param fence					Optional fence
		 * @param pImageIndex			Returned image index
		 * @return Result code
		 */
		int vkAcquireNextImageKHR(DeviceContext device, Swapchain swapchain, long timeout, Semaphore semaphore, Fence fence, IntByReference pImageIndex);

		/**
		 * Presents to the swapchain.
		 * @param queue					Presentation queue
		 * @param pPresentInfo			Pointer to descriptor
		 * @return Result code
		 */
		int vkQueuePresentKHR(Queue queue, VkPresentInfoKHR pPresentInfo);
	}
}
