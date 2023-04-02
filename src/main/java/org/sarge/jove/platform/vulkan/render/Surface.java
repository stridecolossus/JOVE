package org.sarge.jove.platform.vulkan.render;

import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Supplier;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.platform.vulkan.util.VulkanFunction.StructureVulkanFunction;
import org.sarge.jove.util.IntEnum;
import org.sarge.lib.util.LazySupplier;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>surface</i> defines the capabilities of a Vulkan rendering surface.
 * @author Sarge
 */
public class Surface extends TransientNativeObject {
	/**
	 * Default presentation mode (FIFO, guaranteed on all Vulkan implementations).
	 */
	public static final VkPresentModeKHR DEFAULT_PRESENTATION_MODE = VkPresentModeKHR.FIFO_KHR;

	/**
	 * @return Default surface format (sRGB non-linear)
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

	/**
	 * Helper - Converts the given Vulkan extents to a dimensions.
	 * @param extents Extents
	 * @return Dimensions
	 */
	public static Dimensions dimensions(VkExtent2D extents) {
		return new Dimensions(extents.width, extents.height);
	}

	private final PhysicalDevice dev;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param dev			Physical device
	 */
	public Surface(Handle surface, PhysicalDevice dev) {
		super(surface);
		this.dev = notNull(dev);
	}

	@Override
	protected void release() {
		final Instance instance = dev.instance();
		final VulkanLibrary lib = instance.library();
		lib.vkDestroySurfaceKHR(instance, this, null);
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		final VulkanLibrary lib = dev.instance().library();
		final var caps = new VkSurfaceCapabilitiesKHR();
		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, this, caps));
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public List<VkSurfaceFormatKHR> formats() {
		final Instance instance = dev.instance();
		final VulkanLibrary lib = instance.library();
		final StructureVulkanFunction<VkSurfaceFormatKHR> func = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, this, count, array);
		final IntByReference count = instance.factory().integer();
		final VkSurfaceFormatKHR[] array = func.invoke(count, new VkSurfaceFormatKHR());
		return Arrays.asList(array);
	}

	/**
	 * Helper - Selects the preferred surface format that supports the given format and colour-space or falls back to the {@link #defaultSurfaceFormat()}.
	 * @param format		Surface format
	 * @param space			Colour space
	 * @param def			Default surface format or {@code null} for the {@link #defaultSurfaceFormat()}
	 * @return Selected surface format
	 */
	public VkSurfaceFormatKHR format(VkFormat format, VkColorSpaceKHR space, VkSurfaceFormatKHR def) {
		return format(format, space)
				.or(() -> Optional.ofNullable(def))
				.orElseGet(Surface::defaultSurfaceFormat);
	}

	/**
	 * Finds the surface format matching the given specification.
	 * @param format		Surface format
	 * @param space			Colour space
	 * @return Surface format
	 */
	Optional<VkSurfaceFormatKHR> format(VkFormat format, VkColorSpaceKHR space) {
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
		// Retrieve array of presentation modes
		final Instance instance = dev.instance();
		final VulkanLibrary lib = instance.library();
		final VulkanFunction<int[]> func = (count, array) -> lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, this, count, array);
		final IntByReference count = instance.factory().integer();
		final int[] array = func.invoke(count, int[]::new);

		// Convert to enumeration
		final var mapping = IntEnum.reverse(VkPresentModeKHR.class);
		return Arrays
				.stream(array)
				.mapToObj(mapping::map)
				.collect(toSet());
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

	/**
	 * Creates a cached instance of this surface that minimises calls to the API.
	 * @return Cached surface
	 */
	public Surface cached() {
		return new Surface(handle, dev) {
			private final Supplier<VkSurfaceCapabilitiesKHR> caps = new LazySupplier<>(super::capabilities);
			private final Supplier<List<VkSurfaceFormatKHR>> formats = new LazySupplier<>(super::formats);
			private final Supplier<Set<VkPresentModeKHR>> modes = new LazySupplier<>(super::modes);

			@Override
			public VkSurfaceCapabilitiesKHR capabilities() {
				return caps.get();
			}

			@Override
			public List<VkSurfaceFormatKHR> formats() {
				return formats.get();
			}

			@Override
			public Set<VkPresentModeKHR> modes() {
				return modes.get();
			}
		};
	}

	/**
	 * Surface API.
	 */
	interface Library {
		/**
		 * Queries whether a queue family supports presentation to the given surface.
		 * @param device				Physical device handle
		 * @param queueFamilyIndex		Queue family
		 * @param surface				Vulkan surface
		 * @param supported				Returned boolean flag
		 * @return Result
		 */
		int vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Handle surface, IntByReference supported);

		/**
		 * Retrieves the capabilities of a surface.
		 * @param device			Physical device
		 * @param surface			Surface handle
		 * @param caps				Returned capabilities
		 * @return Result
		 */
		int vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, Surface surface, VkSurfaceCapabilitiesKHR caps);

		/**
		 * Queries the supported surface formats.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param formats			Supported formats
		 * @return Result
		 */
		int vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, Surface surface, IntByReference count, VkSurfaceFormatKHR formats);

		/**
		 * Queries the supported presentation modes.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param modes				Supported presentation modes
		 * @return Result
		 * @see VkPresentModeKHR
		 */
		int vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, Surface surface, IntByReference count, int[] modes);

		/**
		 * Destroys a surface.
		 * @param instance			Vulkan instance
		 * @param surface			Surface
		 * @param allocator			Allocator
		 */
		void vkDestroySurfaceKHR(Instance instance, Surface surface, Pointer allocator);
	}
}
