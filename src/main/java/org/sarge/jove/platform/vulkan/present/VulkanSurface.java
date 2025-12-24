package org.sarge.jove.platform.vulkan.present;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.function.Supplier;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

/**
 * A <i>vulkan surface</i> composes a rendering surface derived from a GLFW window and the Vulkan instance.
 * <p>
 * JOVE uses the GLFW {@code glfwCreateWindowSurface} API method to retrieve the handle to the rendering surface, rather than using Vulkan to create the surface using the platform-dependent extensions.
 * This considerably simplifies creation of the surface at the expense of introducing an interdependency between the GLFW window, the Vulkan instance and the physical device.
 * <p>
 * Therefore this implementation has two stages:
 * <p>
 * First the surface handle is queried from GLFW via the {@link Window#surface(Handle)} helper in the constructor.
 * A physical device can then be selected using {@link #isPresentationSupported(PhysicalDevice, Family)}.
 * <p>
 * Once a device has been selected, the {@link #properties(PhysicalDevice)} method retrieves the surface properties to enable configuration of the swapchain.
 * <p>
 * @author Sarge
 */
public class VulkanSurface extends AbstractNativeObject {
	private final Window window;
	private final Instance instance;
	private final Library library;

	/**
	 * Constructor.
	 * @param window		Window
	 * @param instance		Vulkan instance
	 * @param library		Surface library
	 */
	public VulkanSurface(Window window, Instance instance, Library library) {
		final Handle handle = window.surface(instance.handle());
		super(handle);
		this.window = requireNonNull(window);
		this.instance = requireNonNull(instance);
		this.library = requireNonNull(library);
	}

	/**
	 * @return Window
	 */
	public Window window() {
		return window;
	}

	/**
	 * Determines whether presentation to this surface is supported by the given device and queue family.
	 * @param device Physical device
	 * @param family Queue family
	 * @return Whether presentation to this surface is supported
	 */
	public boolean isPresentationSupported(PhysicalDevice device, Family family) {
		final var supported = new IntegerReference();
		library.vkGetPhysicalDeviceSurfaceSupportKHR(device, family.index(), this, supported);
		return NativeBooleanTransformer.isTrue(supported.get());
	}

	@Override
	protected void release() {
		library.vkDestroySurfaceKHR(instance, this, null);
	}

	/**
	 * Properties of this surface.
	 */
	public interface Properties {
		/**
		 * @return This surface
		 */
		VulkanSurface surface();

		/**
		 * @return Capabilities of this surface
		 */
		VkSurfaceCapabilitiesKHR capabilities();

		/**
		 * @return Formats supported by this surface
		 */
		List<VkSurfaceFormatKHR> formats();

		/**
		 * @return Presentation modes supported by this surface
		 */
		List<VkPresentModeKHR> modes();
	}

	/**
	 * Retrieves the surface properties for the given device.
	 * @param device Physical device
	 * @return Surface properties provider
	 */
	public Supplier<Properties> properties(PhysicalDevice device) {
		class PropertiesInstance implements Properties {
			@Override
			public VulkanSurface surface() {
				return VulkanSurface.this;
			}

			@Override
			public VkSurfaceCapabilitiesKHR capabilities() {
	    		final var capabilities = new VkSurfaceCapabilitiesKHR();
	    		library.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, VulkanSurface.this, capabilities);
	    		return capabilities;
	    	}

			@Override
			public List<VkSurfaceFormatKHR> formats() {
	    		final VulkanFunction<VkSurfaceFormatKHR[]> formats = (count, array) -> library.vkGetPhysicalDeviceSurfaceFormatsKHR(device, VulkanSurface.this, count, array);
	    		final VkSurfaceFormatKHR[] array = VulkanFunction.invoke(formats, VkSurfaceFormatKHR[]::new);
	    		return List.of(array);
	    	}

			@Override
			public List<VkPresentModeKHR> modes() {
				final VulkanFunction<VkPresentModeKHR[]> function = (count, array) -> library.vkGetPhysicalDeviceSurfacePresentModesKHR(device, VulkanSurface.this, count, array);
				final VkPresentModeKHR[] modes = VulkanFunction.invoke(function, VkPresentModeKHR[]::new);
				return List.of(modes);
			}
		}

		return () -> new PropertiesInstance();
	}

	/**
	 * Surface API.
	 */
	public // TODO
	interface Library {
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
		VkResult vkGetPhysicalDeviceSurfaceCapabilitiesKHR(PhysicalDevice device, VulkanSurface surface, @Updated VkSurfaceCapabilitiesKHR pSurfaceCapabilities);

		/**
		 * Queries the supported surface formats.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param formats			Supported formats
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfaceFormatsKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, @Updated VkSurfaceFormatKHR[] formats);

		/**
		 * Queries the supported presentation modes.
		 * @param device			Physical device
		 * @param surface			Surface
		 * @param count				Number of results
		 * @param modes				Supported presentation modes
		 * @return Result
		 */
		VkResult vkGetPhysicalDeviceSurfacePresentModesKHR(PhysicalDevice device, VulkanSurface surface, IntegerReference count, @Updated VkPresentModeKHR[] modes);

		/**
		 * Destroys a surface.
		 * @param instance			Vulkan instance
		 * @param surface			Surface
		 * @param allocator			Allocator
		 */
		void vkDestroySurfaceKHR(Instance instance, VulkanSurface surface, Handle allocator);
	}
}
