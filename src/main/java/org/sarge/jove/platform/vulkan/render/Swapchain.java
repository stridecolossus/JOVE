package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.AbstractVulkanObject;
import org.sarge.jove.platform.vulkan.common.ClearValue;
import org.sarge.jove.platform.vulkan.common.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.core.Fence;
import org.sarge.jove.platform.vulkan.core.LogicalDevice;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.Surface;
import org.sarge.jove.platform.vulkan.image.Image;
import org.sarge.jove.platform.vulkan.image.ImageDescriptor;
import org.sarge.jove.platform.vulkan.image.ImageExtents;
import org.sarge.jove.platform.vulkan.image.View;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.platform.vulkan.util.ReferenceFactory;
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.lib.util.Check;

import com.sun.jna.Memory;
import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>swapchain</i> presents rendered images to a {@link Surface}.
 * @author Sarge
 */
public class Swapchain extends AbstractVulkanObject {
	/**
	 * Default swapchain image format.
	 */
	public static final VkFormat DEFAULT_FORMAT = new FormatBuilder()
			.template(FormatBuilder.BGRA)
			.bytes(1)
			.signed(false)
			.type(FormatBuilder.Type.NORMALIZED)
			.build();
	// TODO - should be VkFormat.B8G8R8A8_SRGB; i.e. is it SRBG or UNORM?

	/**
	 * Default swapchain colour-space.
	 */
	public static final VkColorSpaceKHR DEFAULT_COLOUR_SPACE = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;

	/**
	 * Default presentation mode (guaranteed on all Vulkan implementations).
	 */
	public static final VkPresentModeKHR DEFAULT_PRESENTATION_MODE = VkPresentModeKHR.FIFO_KHR;

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
	Swapchain(Pointer handle, LogicalDevice dev, VkFormat format, Dimensions extents, List<View> views) {
		super(handle, dev);
		this.format = notNull(format);
		this.extents = notNull(extents);
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
	 * @param semaphore		Optional semaphore signalled when the frame has been acquired
	 * @param fence			Optional fence
	 * @return Image index
	 * @throws IllegalArgumentException if both the semaphore and fence are {@code null}
	 */
	public int acquire(Semaphore semaphore, Fence fence) {
//		if((semaphore == null) && (fence == null)) throw new IllegalArgumentException("Either semaphore or fence must be provided");
		final DeviceContext dev = device();
		final VulkanLibrary lib = dev.library();
		check(lib.vkAcquireNextImageKHR(dev.handle(), this.handle(), Long.MAX_VALUE, NativeObject.ofNullable(semaphore), NativeObject.ofNullable(fence), index));
		return index.getValue();
	}

	/**
	 * Presents the next frame.
	 * @param queue				Presentation queue
	 * @param semaphores		Wait semaphores
	 */
	public void present(Queue queue, Set<Semaphore> semaphores) {
		// Create presentation descriptor
		final VkPresentInfoKHR info = new VkPresentInfoKHR();

		// Populate wait semaphores
		info.waitSemaphoreCount = semaphores.size();
		info.pWaitSemaphores = Handle.toArray(semaphores);

		// Populate swap-chain
		info.swapchainCount = 1;
		info.pSwapchains = Handle.toArray(List.of(this));

		// Set image indices
		final int[] array = new int[]{index.getValue()};
		final Memory mem = new Memory(array.length * Integer.BYTES);
		mem.write(0, array, 0, array.length);
		info.pImageIndices = mem;

		// Present frame
		final VulkanLibrary lib = device().library();
		check(lib.vkQueuePresentKHR(queue.handle(), info));
	}
	// TODO - cache descriptor -> factory -> work submit?

	@Override
	protected Destructor destructor(VulkanLibrary lib) {
		return lib::vkDestroySwapchainKHR;
	}

	@Override
	protected void release() {
		views.forEach(View::close);
	}

	/**
	 * Builder for a swap chain.
	 */
	public static class Builder {
		// Properties
		private final LogicalDevice dev;
		private final VkSwapchainCreateInfoKHR info = new VkSwapchainCreateInfoKHR();
		private ClearValue clear = ClearValue.NONE;

		// Surface constraints
		private final VkSurfaceCapabilitiesKHR caps;
		private final Collection<VkSurfaceFormatKHR> formats;
		private final Set<VkPresentModeKHR> modes;

		/**
		 * Constructor.
		 * @param dev			Logical device
		 * @param surface		Rendering surface
		 * <p>
		 * The following swap-chain descriptor fields are initialised from the surface capabilities:
		 * <ul>
		 * <li>minimum number of swap-chain images</li>
		 * <li>extent</li>
		 * <li>transform</li>
		 * </ul>
		 * @see Surface#capabilities()
		 */
		public Builder(LogicalDevice dev, Surface surface) {
			this.dev = notNull(dev);
			this.caps = surface.capabilities();
			this.formats = surface.formats();
			this.modes = surface.modes();
			init();
			info.surface = surface.handle();
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
			mode(DEFAULT_PRESENTATION_MODE);
			clipped(true);
		}

		/**
		 * Tests whether an integer mask contains the given enumeration value.
		 * @param mask			Mask
		 * @param constant		Enumeration constant
		 * @return Whether the mask contains the given constant
		 */
		private static <E extends IntegerEnumeration> boolean contains(int mask, E constant) {
			return (constant.value() & mask) != 0;
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
		 * Sets the image format.
		 * @param format Image format
		 * @see VkSurfaceFormatKHR
		 */
		public Builder format(VkFormat format) {
			info.imageFormat = notNull(format);
			return this;
		}

		/**
		 * Sets the colour-space.
		 * @param space Colour-space
		 * @see VkSurfaceFormatKHR
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
			if(!contains(caps.supportedUsageFlags, usage)) throw new IllegalArgumentException("Usage not supported: " + usage);
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
			if(!contains(caps.supportedTransforms, transform)) throw new IllegalArgumentException("Transform not supported: " + transform);
			info.preTransform = notNull(transform);
			return this;
		}

		/**
		 * Sets the surface alpha function.
		 * @param alpha Alpha function
		 * @throws IllegalArgumentException if the given alpha function is not supported
		 */
		public Builder alpha(VkCompositeAlphaFlagKHR alpha) {
			if(!contains(caps.supportedCompositeAlpha, alpha)) throw new IllegalArgumentException("Compositive alpha not supported: " + alpha);
			info.compositeAlpha = notNull(alpha);
			return this;
		}

		/**
		 * Sets the presentation mode.
		 * @param mode Presentation mode
		 * @throws IllegalArgumentException if the given mode is not supported by the surface
		 * @see Surface#modes()
		 */
		public Builder mode(VkPresentModeKHR mode) {
			if(!modes.contains(mode)) throw new IllegalArgumentException("Presentation mode not supported: " + mode);
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
		 * @throws IllegalArgumentException if the image format and colour-space are not supported by the given surface
		 */
		public Swapchain build() {
			// Check image format and colour-space are supported by the surface
			formats
					.stream()
					.filter(f -> f.format == info.imageFormat)
					.filter(f -> f.colorSpace == info.imageColorSpace)
					.findAny()
					.orElseThrow(() -> new IllegalArgumentException(String.format("Unsupported swapchain format: format=%s space=%s", info.imageFormat, info.imageColorSpace)));

			// TODO
			info.queueFamilyIndexCount = 0;
			info.pQueueFamilyIndices = null;
			info.oldSwapchain = null;

			// Create swapchain
			final VulkanLibrary lib = dev.library();
			final ReferenceFactory factory = lib.factory();
			final PointerByReference chain = factory.pointer();
			check(lib.vkCreateSwapchainKHR(dev.handle(), info, null, chain));

			// Retrieve swapchain images
			final VulkanFunction<Pointer[]> func = (api, count, array) -> api.vkGetSwapchainImagesKHR(dev.handle(), chain.getValue(), count, array);
			final var handles = VulkanFunction.enumerate(func, lib, factory::array);

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
					.map(Image::view)
					.collect(toList());

			// Init clear value
			for(View view : views) {
				view.clear(clear);
			}

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
}
