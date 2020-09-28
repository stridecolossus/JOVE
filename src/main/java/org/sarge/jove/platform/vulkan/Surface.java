package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>surface</i> is used to render to a window.
 * @author Sarge
 */
public class Surface {
	private final Pointer surface;
	private final LogicalDevice dev;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param dev			Device
	 */
	public Surface(Pointer surface, LogicalDevice dev) {
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
	 * @return Logical device
	 */
	public LogicalDevice device() {
		return dev;
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		final VulkanLibrary lib = dev.library();
		final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR.ByReference();
		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.parent().handle(), surface, caps));
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public Collection<VkSurfaceFormatKHR> formats() {
		final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.parent().handle(), surface, count, array);
		final var formats = VulkanFunction.enumerate(func, dev.library(), new VkSurfaceFormatKHR());
		return Arrays.asList(formats);
	}

	/**
	 * @return Presentation modes supported by this surface
	 */
	public Set<VkPresentModeKHR> modes() {
		// Count number of supported modes
		// TODO - API method returns the modes as an int[] and we cannot use VulkanFunction::enumerate for a primitive array
		final VulkanLibrary lib = dev.library();
		final Pointer handle = dev.parent().handle();
		final IntByReference count = lib.factory().integer();
		check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(handle, surface, count, null));

		// Retrieve modes
		final int[] array = new int[count.getValue()];
		check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(handle, surface, count, array));

		// Convert to enumeration
		return Arrays
				.stream(array)
				.mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n))
				.collect(toSet());
	}

	/**
	 * Destroys this surface.
	 */
	public synchronized void destroy() {
		final Instance instance = dev.parent().instance();
		final VulkanLibrarySurface lib = instance.library();
		lib.vkDestroySurfaceKHR(instance.handle(), surface, null);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}
