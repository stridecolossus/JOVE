package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import org.sarge.jove.common.AbstractTransientNativeObject;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.platform.vulkan.VkPresentModeKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceCapabilitiesKHR;
import org.sarge.jove.platform.vulkan.VkSurfaceFormatKHR;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.api.VulkanLibrarySurface;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.lib.util.LazySupplier;

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
		private final PhysicalDevice dev;
		private final Supplier<List<VkSurfaceFormatKHR>> formats = new LazySupplier<>(this::loadFormats);
		private final Supplier<Set<VkPresentModeKHR>> modes = new LazySupplier<>(this::loadModes);

		private Properties(PhysicalDevice dev) {
			this.dev = dev;
		}

		/**
		 * @return Physical device
		 */
		public PhysicalDevice device() {
			return dev;
		}

		/**
		 * @return Surface
		 */
		public Surface surface() {
			return Surface.this;
		}

		/**
		 * @return Capabilities of this surface
		 */
		public VkSurfaceCapabilitiesKHR capabilities() {
			final VulkanLibrary lib = instance.library();
			final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR();
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
			final VulkanFunction<VkSurfaceFormatKHR> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfaceFormatsKHR(dev, Surface.this, count, array);
			final VulkanLibrary lib = dev.instance().library();
			final VkSurfaceFormatKHR[] array = VulkanFunction.invoke(func, lib, VkSurfaceFormatKHR::new);
			return Arrays.stream(array).collect(toList());
		}

		/**
		 * @return Presentation modes supported by this surface
		 */
		public Set<VkPresentModeKHR> modes() {
			return modes.get();
		}

		public Set<VkPresentModeKHR> loadModes() {
			// Retrieve array of presentation modes
			final VulkanLibrary lib = instance.library();
			final VulkanFunction<int[]> func = (api, count, array) -> api.vkGetPhysicalDeviceSurfacePresentModesKHR(dev, Surface.this, count, array);
			final int[] array = VulkanFunction.invoke(func, lib, int[]::new);

			// Convert to enumeration
			return Arrays
					.stream(array)
					.mapToObj(n -> IntegerEnumeration.mapping(VkPresentModeKHR.class).map(n))
					.collect(toSet());
		}
	}
}
