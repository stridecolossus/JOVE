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
	private final Instance instance;

	/**
	 * Constructor.
	 * @param surface		Surface handle
	 * @param instance		Vulkan instance
	 */
	public Surface(Handle handle, Instance instance) {
		super(handle);
		this.instance = notNull(instance);
	}

	/**
	 * @param dev Physical device
	 * @return Properties of this surface/device
	 */
	public Properties properties(PhysicalDevice dev) {
		return new Properties(dev);
	}

	@Override
	protected void release() {
		final VulkanLibrarySurface lib = instance.library();
		lib.vkDestroySurfaceKHR(instance, this, null);
	}

	/**
	 * The <i>surface properties</i> is used to query the physical capabilities of this surface/device.
	 */
	public class Properties {
		private final PhysicalDevice physical;

		private Properties(PhysicalDevice physical) {
			this.physical = physical;
		}

		/**
		 * @return Capabilities of this surface
		 */
		public VkSurfaceCapabilitiesKHR capabilities() {
			final VulkanLibrary lib = instance.library();
			final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
			check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(physical, Surface.this, caps));
			return caps;
		}

		/**
		 * @return Formats supported by this surface
		 */
		public Collection<VkSurfaceFormatKHR> formats() {
			final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(physical, Surface.this, count, array);
			final VulkanLibrary lib = instance.library();
			final var formats = VulkanFunction.enumerate(func, lib, VkSurfaceFormatKHR::new);
			return Arrays.stream(formats).collect(toList());
		}

		/**
		 * @return Presentation modes supported by this surface
		 */
		public Set<VkPresentModeKHR> modes() {
			// Count number of supported modes
			// TODO - API method returns the modes as an int[] and we cannot use VulkanFunction::enumerate for a primitive array
			final VulkanLibrary lib = instance.library();
			final IntByReference count = lib.factory().integer();
			check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(physical, Surface.this, count, null));

			// Retrieve modes
			final int[] array = new int[count.getValue()];
			check(lib.vkGetPhysicalDeviceSurfacePresentModesKHR(physical, Surface.this, count, array));

			// Convert to enumeration
			return Arrays
					.stream(array)
					.mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n))
					.collect(toSet());
		}
	}
}
