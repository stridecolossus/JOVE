package org.sarge.jove.platform.vulkan;

import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import com.sun.jna.Pointer;

/**
 * A <i>surface</i> is used to render to a window.
 * @author Sarge
 */
public class Surface {
	private final Pointer surface;
	private final PhysicalDevice dev;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param dev			Device
	 */
	public Surface(Pointer surface, PhysicalDevice dev) {
		this.dev = notNull(dev);
		this.surface = notNull(surface);
	}

	/**
	 * @return Surface handle
	 */
	Pointer handle() {
		return surface;
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		final VulkanLibrary lib = dev.instance().library();
		final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR.ByReference();
		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.handle(), surface, caps));
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public Collection<VkSurfaceFormatKHR> formats() {
		final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.handle(), surface, count, array);
		final var formats = VulkanFunction.enumerate(func, dev.instance().library(), new VkSurfaceFormatKHR());
		return Arrays.asList(formats);
	}

	/**
	 * @return Presentation modes supported by this surface
	 */
	public Set<VkPresentModeKHR> modes() {
		// TODO - will this actually work? note cannot use int[] as generic
		final VulkanFunction<VkPresentModeKHR[]> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), surface, count, array);
		final VkPresentModeKHR[] formats = VulkanFunction.enumerate(func, dev.instance().library(), VkPresentModeKHR[]::new);
		return new HashSet<>(Arrays.asList(formats));
	}

	/**
	 * Destroys this surface.
	 */
	public synchronized void destroy() {
		final Instance instance = dev.instance();
		final VulkanLibrarySurface lib = instance.library();
		lib.vkDestroySurfaceKHR(instance.handle(), surface, null);
	}
}
