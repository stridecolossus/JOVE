package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.glfw.FrameworkDesktopService;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>surface</i> is used to render to a window.
 * @author Sarge
 */
public class Surface extends VulkanHandle {
	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param device		Physical device
	 * @see FrameworkDesktopService#surface(Pointer, Pointer)
	 */
	public static Surface create(Pointer surface, PhysicalDevice device) {
		// Get surface capabilities
		final Vulkan vulkan = Vulkan.instance();
		final VulkanLibrary lib = vulkan.library();
		final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR.ByReference();
		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device.handle(), surface, caps));

		// Get supported formats
		final VulkanFunction<VkSurfaceFormatKHR> func = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(device.handle(), surface, count, array);
		final var formats = Arrays.asList(VulkanFunction.enumerate(func, new VkSurfaceFormatKHR()));

		// Get supported presentation modes
		final var modes = loadModes(lib, device, surface);

		// Create surface
		final Pointer instance = device.instance().handle();
		final Destructor destructor = () -> lib.vkDestroySurfaceKHR(instance, surface, null);
		return new Surface(new VulkanHandle(surface, destructor), caps, formats, modes);
	}

	/**
	 * @return Supported presentation modes
	 */
	// TODO - can VulkanFunction::enumerate be extended to support this?
	private static Set<VkPresentModeKHR> loadModes(VulkanLibrary lib, PhysicalDevice dev, Pointer surface) {
		// Count number of modes
		final IntByReference count = new IntByReference(1);
		lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), surface, count, null);

		// Retrieve modes
		final int[] modes = new int[count.getValue()];
		if(count.getValue() > 0) {
			lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), surface, count, modes);
		}

		// Convert to enumeration set
		return Arrays.stream(modes).mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n)).collect(toSet());
	}

	private final VkSurfaceCapabilitiesKHR caps;
	private final List<VkSurfaceFormatKHR> formats;
	private final Set<VkPresentModeKHR> modes;

	/**
	 * Constructor.
	 * @param handle		Handle
	 * @param caps 			Surface capabilities
	 * @param formats		Supported formats
	 * @param modes			Supported presentation modes
	 */
	protected Surface(VulkanHandle handle, VkSurfaceCapabilitiesKHR caps, List<VkSurfaceFormatKHR> formats, Set<VkPresentModeKHR> modes) {
		super(handle);
		this.caps = notNull(caps);
		this.formats = List.copyOf(formats);
		this.modes = Set.copyOf(modes);
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		return caps;
	}

	/**
	 * @return Supported surface formats
	 */
	public List<VkSurfaceFormatKHR> formats() {
		return formats;
	}

	/**
	 * @return Supported presentation modes
	 */
	public Set<VkPresentModeKHR> modes() {
		return modes;
	}
}
