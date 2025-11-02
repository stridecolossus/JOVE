package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
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
		// Create surface format
		final var struct = new VkSurfaceFormatKHR();
		struct.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;

		// Init default swapchain image format
		struct.format = new FormatBuilder()
				.components("BGRA")
				.bytes(1)
				.signed(false)
				.type(FormatBuilder.NumericFormat.NORM)
				.build();

		return struct;
	}

	private final Handle instance;
	private final PhysicalDevice device;
	private final Library lib;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param instance		Vulkan instance handle
	 * @param device		Physical device
	 * @param lib			Surface library
	 */
	public VulkanSurface(Handle surface, Handle instance, PhysicalDevice device, Library lib) {
		super(surface);
		this.instance = requireNonNull(instance);
		this.device = requireNonNull(device);
		this.lib = requireNonNull(lib);
	}

	/**
	 * @param family Queue family
	 * @return Whether this surface supports presentation for the given queue family
	 */
	public boolean isPresentationSupported(Family family) {
		final var supported = new IntegerReference();
		lib.vkGetPhysicalDeviceSurfaceSupportKHR(device, family.index(), this, supported);
		return supported.get() == 1;		// TODO - boolean ref?
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		final var caps = new VkSurfaceCapabilitiesKHR();
		lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, this, caps);
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public List<VkSurfaceFormatKHR> formats() {
		final VulkanFunction<VkSurfaceFormatKHR[]> formats = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(device, this, count, array);
		final VkSurfaceFormatKHR[] array = VulkanFunction.invoke(formats, VkSurfaceFormatKHR[]::new);
		return List.of(array);
	}

	/**
	 * Helper - Selects the preferred surface format that supports the given format and colour-space or falls back to the {@link #defaultSurfaceFormat()}.
	 * @param format				Surface format
	 * @param space					Colour space
	 * @param defaultFormat			Default surface format or {@code null} for the {@link #defaultSurfaceFormat()}
	 * @return Selected surface format
	 */
	public VkSurfaceFormatKHR format(VkFormat format, VkColorSpaceKHR space, VkSurfaceFormatKHR defaultFormat) {
		return format(format, space)
				.or(() -> Optional.ofNullable(defaultFormat))
				.orElseGet(VulkanSurface::defaultSurfaceFormat);
	}

	/**
	 * Finds the surface format matching the given specification.
	 * @param format		Surface format
	 * @param space			Colour space
	 * @return Surface format
	 */
	public Optional<VkSurfaceFormatKHR> format(VkFormat format, VkColorSpaceKHR space) {
		return this
				.formats()
				.stream()
				.filter(f -> f.format == format)
				.filter(f -> f.colorSpace == space)
				.findAny();
	}

	/**
	 * @return Presentation modes supported by this surface
	 */
	public Set<VkPresentModeKHR> modes() {
		final VulkanFunction<VkPresentModeKHR[]> function = (count, array) -> lib.vkGetPhysicalDeviceSurfacePresentModesKHR(device, this, count, array);
		final VkPresentModeKHR[] modes = VulkanFunction.invoke(function, VkPresentModeKHR[]::new);
		return Set.of(modes);
	}

	/**
	 * Helper - Selects a preferred presentation mode from the given candidates or falls back to the {@link #DEFAULT_PRESENTATION_MODE}.
	 * @param candidates Preferred presentation modes
	 * @return Selected presentation mode
	 */
	public VkPresentModeKHR mode(VkPresentModeKHR... candidates) {
		final Set<VkPresentModeKHR> available = this.modes();
		return Arrays
				.stream(candidates)
				.filter(available::contains)
				.findAny()
				.orElse(DEFAULT_PRESENTATION_MODE);
	}

	@Override
	protected void release() {
		lib.vkDestroySurfaceKHR(instance, this, null);
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
		VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, VulkanSurface surface, IntegerReference supported);

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
		VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, NativeReference<Integer> count, @Returned VkSurfaceFormatKHR[] formats);

		/**
		 * Queries the supported presentation modes.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param modes				Supported presentation modes
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, VkPresentModeKHR[] modes);

		/**
		 * Destroys a surface.
		 * @param instance			Vulkan instance
		 * @param surface			Surface
		 * @param allocator			Allocator
		 */
		void vkDestroySurfaceKHR(Handle instance, VulkanSurface surface, Handle allocator);
	}
}
