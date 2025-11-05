package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.*;

/**
 * A <i>vulkan surface</i> defines the capabilities of a rendering surface.
 * @author Sarge
 */
public class VulkanSurface extends TransientNativeObject {
	/**
	 * Default presentation mode guaranteed on all Vulkan implementations.
	 * @see VkPresentModeKHR#FIFO_KHR
	 */
	public static final VkPresentModeKHR DEFAULT_PRESENTATION_MODE = VkPresentModeKHR.FIFO_KHR;

	/**
	 * @return Default surface format
	 * @see VkColorSpaceKHR#SRGB_NONLINEAR_KHR
	 */
	public static VkSurfaceFormatKHR defaultSurfaceFormat() {
		// Init surface format
		final var format = new VkSurfaceFormatKHR();
		format.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;

		// Init default swapchain image format
		format.format = new FormatBuilder()
				.components("BGRA")
				.bytes(1)
				.signed(false)
				.type(FormatBuilder.NumericFormat.NORM)
				.build();

		return format;
	}

	/**
	 * Creates a selector that matches a device with a queue family supporting presentation to the given surface.
	 * @param surface Vulkan surface
	 * @param library Surface library
	 * @return Presentation selector
	 */
	public static Selector presentation(Handle surface, Library library) {
		final BiPredicate<PhysicalDevice, Family> predicate = (device, family) -> {
			final var supported = new IntegerReference();
			library.vkGetPhysicalDeviceSurfaceSupportKHR(device, family.index(), surface, supported);
			return supported.get() == 1;
		};
		return new Selector(predicate);
	}

	private final PhysicalDevice device;
	private final Library library;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param device		Physical device
	 * @param lib			Surface library
	 */
	public VulkanSurface(Handle surface, PhysicalDevice device, Library lib) {
		super(surface);
		this.device = requireNonNull(device);
		this.library = requireNonNull(lib);
	}

	/**
	 * Creates an adapter that pre-loads the properties of this surface to minimise API calls.
	 * @return Surface with pre-loaded properties
	 */
	public VulkanSurface load() {
		final var capabilities = this.capabilities();
		final var formats = this.formats();
		final var modes = this.modes();

		return new VulkanSurface(this.handle(), device, library) {
			@Override
			public VkSurfaceCapabilitiesKHR capabilities() {
				return capabilities;
			}

			@Override
			public List<VkSurfaceFormatKHR> formats() {
				return formats;
			}

			@Override
			public List<VkPresentModeKHR> modes() {
				return modes;
			}
		};
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		final var caps = new VkSurfaceCapabilitiesKHR();
		library.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, this, caps);
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public List<VkSurfaceFormatKHR> formats() {
		final VulkanFunction<VkSurfaceFormatKHR[]> formats = (count, array) -> library.vkGetPhysicalDeviceSurfaceFormatsKHR(device, this, count, array);
		final VkSurfaceFormatKHR[] array = VulkanFunction.invoke(formats, VkSurfaceFormatKHR[]::new);
		return List.of(array);
	}

	/**
	 * Helper.
	 * Selects a surface format matching the given predicate or falls back to the provided default (if any) or finally the {@link #defaultSurfaceFormat()}.
	 * @param predicate		Surface format predicate
	 * @param def			Optional default surface format
	 * @return Selected surface format
	 * @see #equals(VkSurfaceFormatKHR)
	 */
	public VkSurfaceFormatKHR select(Predicate<VkSurfaceFormatKHR> predicate, VkSurfaceFormatKHR def) {
		return this
				.formats()
				.stream()
				.filter(predicate)
				.findAny()
				.or(() -> Optional.ofNullable(def))
				.orElseGet(VulkanSurface::defaultSurfaceFormat);
	}

	/**
	 * Creates a equals predicate for the given surface format.
	 * @param format Surface format
	 * @return Equals predicate
	 * @implNote Vulkan structures are compared by <b>identity</b> since the {@code equals} method is not implemented
	 */
	public static Predicate<VkSurfaceFormatKHR> equals(VkSurfaceFormatKHR format) {
		return that -> (that.format == format.format) && (that.colorSpace == format.colorSpace);
	}

	/**
	 * @return Presentation modes supported by this surface
	 */
	public List<VkPresentModeKHR> modes() {
		final VulkanFunction<VkPresentModeKHR[]> function = (count, array) -> library.vkGetPhysicalDeviceSurfacePresentModesKHR(device, this, count, array);
		final VkPresentModeKHR[] modes = VulkanFunction.invoke(function, VkPresentModeKHR[]::new);
		return List.of(modes);
	}

	/**
	 * Helper.
	 * Selects a preferred presentation mode from the given candidates or falls back to the {@link #DEFAULT_PRESENTATION_MODE}.
	 * @param modes Candidates presentation modes
	 * @return Selected presentation mode
	 */
	public VkPresentModeKHR select(List<VkPresentModeKHR> modes) {
		final var available = this.modes();
		return modes
				.stream()
				.filter(available::contains)
				.findAny()
				.orElse(DEFAULT_PRESENTATION_MODE);
	}

	@Override
	protected void release() {
		library.vkDestroySurfaceKHR(device.instance(), this, null);
	}

	/**
	 * Surface API.
	 */
	public interface Library {
		/**
		 * Queries whether a queue family supports presentation to the given surface.
		 * @param device				Physical device handle
		 * @param queueFamilyIndex		Queue family
		 * @param surface				Vulkan surface
		 * @param supported				Returned boolean flag
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Handle surface, IntegerReference supported);

		/**
		 * Retrieves the capabilities of a surface.
		 * @param device					Physical device
		 * @param surface					Surface
		 * @param pSurfaceCapabilities		Returned capabilities
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, @Returned VkSurfaceCapabilitiesKHR pSurfaceCapabilities);

		/**
		 * Queries the supported surface formats.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param formats			Supported formats
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, @Returned VkSurfaceFormatKHR[] formats);

		/**
		 * Queries the supported presentation modes.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param modes				Supported presentation modes
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, @Returned VkPresentModeKHR[] modes);

		/**
		 * Destroys a surface.
		 * @param instance			Vulkan instance
		 * @param surface			Surface
		 * @param allocator			Allocator
		 */
		void vkDestroySurfaceKHR(Instance instance, VulkanSurface surface, Handle allocator);
	}
}
