package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.Semaphore;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor;
import org.sarge.jove.platform.vulkan.image.ImageExtents;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.jove.util.MathsUtil;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

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
	/**
	 * Default swapchain image format.
	 */
	public static final VkFormat DEFAULT_FORMAT = new FormatBuilder()
			.components("BGRA")
			.bytes(1)
			.signed(false)
			.type(FormatBuilder.Type.NORM)
			.build();

	/**
	 * Default swapchain colour-space.
	 */
	public static final VkColorSpaceKHR DEFAULT_COLOUR_SPACE = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;

	/**
	 * Default presentation mode (FIFO, guaranteed on all Vulkan implementations).
	 */
	public static final VkPresentModeKHR DEFAULT_PRESENTATION_MODE = VkPresentModeKHR.FIFO_KHR;

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
	 * @param attachments		Colour Attachments
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
	 * @return Colour attachments
	 */
	public List<View> attachments() {
		return attachments;
	}

	/**
	 * Acquires the next image in this swap-chain.
	 * @param semaphore		Optional semaphore signalled when the frame has been acquired
	 * @param fence			Optional fence
	 * @return Image index
	 * @throws IllegalArgumentException if both the semaphore and fence are {@code null}
	 */
	public int acquire(Semaphore semaphore, Fence fence) {
		if((semaphore == null) && (fence == null)) throw new IllegalArgumentException("Either semaphore or fence must be provided");
		final DeviceContext dev = super.device();
		final VulkanLibrary lib = dev.library();
		check(lib.vkAcquireNextImageKHR(dev, this, Long.MAX_VALUE, semaphore, fence, index));
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
	 */
	public void present(Queue queue, int index, Set<Semaphore> semaphores) {
		// Create presentation descriptor
		final VkPresentInfoKHR info = new VkPresentInfoKHR();

		// Populate wait semaphores
		info.waitSemaphoreCount = semaphores.size();
		info.pWaitSemaphores = NativeObject.array(semaphores);

		// Populate swap-chain
		info.swapchainCount = 1;
		info.pSwapchains = NativeObject.array(List.of(this));

		// Set image indices
		info.pImageIndices = NativeObject.array(new int[]{index});

		// Present frame
		final VulkanLibrary lib = device().library();
		check(lib.vkQueuePresentKHR(queue, info));
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

	/**
	 * Helper - Selects a preferred presentation mode or fall back to {@link #DEFAULT_PRESENTATION_MODE}.
	 * @param props			Surface properties
	 * @param modes			Preferred presentation modes
	 * @return Selected presentation mode
	 */
	public static VkPresentModeKHR mode(Surface.Properties props, VkPresentModeKHR... modes) {
		final Set<VkPresentModeKHR> available = props.modes();
		return Arrays
				.stream(modes)
				.filter(available::contains)
				.findAny()
				.orElse(DEFAULT_PRESENTATION_MODE);
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		private final LogicalDevice dev;
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private final Surface.Properties props;
		private final VkSurfaceCapabilitiesKHR caps;
		private ClearValue clear = ClearValue.NONE;

		/**
		 * Constructor.
		 * @param dev			Logical device
		 * @param props			Rendering surface properties
		 */
		public Builder(LogicalDevice dev, Surface.Properties props) {
			if(dev.parent() != props.device()) throw new IllegalArgumentException("Mismatched device and surface");
			this.dev = notNull(dev);
			this.props = notNull(props);
			this.caps = props.capabilities();
			this.info.surface = props.surface().handle();
			init();
		}

		/**
		 * Initialises the swap-chain descriptor.
		 */
		private void init() {
			extent(caps.currentExtent.width, caps.currentExtent.height);
			count(caps.minImageCount);
			transform(caps.currentTransform);
			format(DEFAULT_FORMAT);
			space(DEFAULT_COLOUR_SPACE);
			arrays(1);
			mode(VkSharingMode.EXCLUSIVE);
			usage(VkImageUsage.COLOR_ATTACHMENT);
			alpha(VkCompositeAlphaFlagKHR.OPAQUE);
			presentation(DEFAULT_PRESENTATION_MODE);
			clipped(true);
		}

		private static void validate(int mask, IntegerEnumeration e) {
			if(!MathsUtil.isMask(e.value(), mask)) throw new IllegalArgumentException("Unsupported property: " + e);
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
			format(format.format);
			space(format.colorSpace);
			return this;
		}

		/**
		 * Sets the image format.
		 * @param format Image format
		 */
		public Builder format(VkFormat format) {
			info.imageFormat = notNull(format);
			return this;
		}

		/**
		 * Sets the colour-space.
		 * @param space Colour-space
		 */
		public Builder space(VkColorSpaceKHR space) {
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
			if(min.isLargerThan(extent)) throw new IllegalArgumentException("Extent is smaller than the supported minimum");

			// Check maximum extent
			final Dimensions max = new Dimensions(caps.maxImageExtent.width, caps.maxImageExtent.height);
			if(extent.isLargerThan(max)) throw new IllegalArgumentException("Extent is larger than the supported maximum");

			// Populate extents
			extent(extent.width(), extent.height());
			return this;
		}
		// TODO - constrain by actual resolution using glfwGetFramebufferSize()

		/**
		 * Helper - Populates the swapchain extents.
		 */
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
		public Builder usage(VkImageUsage usage) {
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
			if(!props.modes().contains(mode)) throw new IllegalArgumentException("Unsupported presentation mode: " + mode);
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
			props
					.formats()
					.stream()
					.filter(f -> f.format == info.imageFormat)
					.filter(f -> f.colorSpace == info.imageColorSpace)
					.findAny()
					.orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported surface format: format=%s space=%s", info.imageFormat, info.imageColorSpace)));

			// TODO
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;
			info.oldSwapchain = null;

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
					.extents(new ImageExtents(extents))
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
