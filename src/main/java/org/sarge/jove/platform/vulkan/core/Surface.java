package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.Supplier;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.LazySupplier;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>surface</i> defines the capabilities of a Vulkan rendering surface.
 * @author Sarge
 */
public class Surface extends AbstractTransientNativeObject {
	private final PhysicalDevice dev;
	private final Supplier<List<VkSurfaceFormatKHR>> formats = new LazySupplier<>(this::loadFormats);
	private final Supplier<Set<VkPresentModeKHR>> modes = new LazySupplier<>(this::loadModes);

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param instance		Vulkan instance
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
		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev, Surface.this, caps));
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public List<VkSurfaceFormatKHR> formats() {
		return formats.get();
	}

	private List<VkSurfaceFormatKHR> loadFormats() {
		final Instance instance = dev.instance();
		final VulkanLibrary lib = instance.library();
		final VulkanFunction<VkSurfaceFormatKHR> func = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, Surface.this, count, array);
		final IntByReference count = instance.factory().integer();
		final VkSurfaceFormatKHR[] array = VulkanFunction.invoke(func, count, VkSurfaceFormatKHR::new);
		return Arrays.asList(array);
	}

	/**
	 * Helper - Selects the preferred surface format that supports the given format and colour-space or fall back to an arbitrary supported format.
	 * @param format		Surface format
	 * @param space			Colour space
	 * @return Selected surface format
	 */
	public VkSurfaceFormatKHR format(VkFormat format, VkColorSpaceKHR space) {
		final List<VkSurfaceFormatKHR> formats = this.formats();
		return formats
				.stream()
				.filter(f -> f.format == format)
				.filter(f -> f.colorSpace == space)
				.findAny()
				.orElse(formats.get(0));
	}

	/**
	 * @return Presentation modes supported by this surface
	 */
	public Set<VkPresentModeKHR> modes() {
		return modes.get();
	}

	private Set<VkPresentModeKHR> loadModes() {
		// Retrieve array of presentation modes
		final Instance instance = dev.instance();
		final VulkanLibrary lib = instance.library();
		final VulkanFunction<int[]> func = (count, array) -> lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, Surface.this, count, array);
		final IntByReference count = instance.factory().integer();
		final int[] array = VulkanFunction.invoke(func, count, int[]::new);

		// Convert to enumeration
		final var mapping = IntegerEnumeration.mapping(VkPresentModeKHR.class);
		return Arrays
				.stream(array)
				.mapToObj(mapping::map)
				.collect(toSet());
	}

	/**
	 * Surface API.
	 */
	interface Library {
		/**
		 * Queries whether a queue family supports presentation for the given surface.
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
