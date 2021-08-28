package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.api.VulkanLibrarySurface;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.ptr.IntByReference;

/**
 * A <i>surface</i> defines the capabilities of a Vulkan rendering surface.
 * @author Sarge
 */
public class Surface extends AbstractTransientNativeObject {
	private final PhysicalDevice dev;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param dev			Device
	 */
	public Surface(Handle handle, PhysicalDevice dev) {
		super(handle);
		this.dev = notNull(dev);
	}

	/**
	 * @return Capabilities of this surface
	 */
	public VkSurfaceCapabilitiesKHR capabilities() {
		final VulkanLibrary lib = dev.library();
		final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.handle(), handle, caps));
		return caps;
	}

	/**
	 * @return Formats supported by this surface
	 */
	public Collection<VkSurfaceFormatKHR> formats() {
		final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.handle(), handle, count, array);
		final var formats = VulkanFunction.enumerate(func, dev.library(), VkSurfaceFormatKHR::new);
		return Arrays.stream(formats).collect(toList());
	}

	/**
	 * @return Presentation modes supported by this surface
	 */
	public Set<VkPresentModeKHR> modes() {
		// Count number of supported modes
		// TODO - API method returns the modes as an int[] and we cannot use VulkanFunction::enumerate for a primitive array
		final VulkanLibrary lib = dev.library();
		final IntByReference count = lib.factory().integer();
		check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), this.handle, count, null));

		// Retrieve modes
		final int[] array = new int[count.getValue()];
		check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), this.handle, count, array));

		// Convert to enumeration
		return Arrays
				.stream(array)
				.mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n))
				.collect(toSet());
	}

	@Override
	protected void release() {
		final Instance instance = dev.instance();
		final VulkanLibrarySurface lib = instance.library();
		lib.vkDestroySurfaceKHR(instance.handle(), handle, null);
	}
}
