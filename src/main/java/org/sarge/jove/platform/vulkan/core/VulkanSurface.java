package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Predicate;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;

/**
 * A <i>vulkan surface</i> composes a rendering surface derived from a GLFW window and the Vulkan instance.
 * TODO
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
		// Init surface format
		final var format = new VkSurfaceFormatKHR();
		format.colorSpace = VkColorSpaceKHR.SRGB_NONLINEAR_KHR;

		// Init default swapchain image format
		format.format = new FormatBuilder()
				.components("BGRA")
				.bytes(1)
				.signed(false)
				.type(FormatBuilder.NumericFormat.NORM)
				.build();

		return format;
	}

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
		this.instance = requireNonNull(instance);
		this.library = requireNonNull(library);
	}

	/**
	 * Determines whether presentation to this surface is supported by the given device and queue family.
	 * @param device Physical device
	 * @param family Queue family
	 * @return Whether supports presentation
	 */
	public boolean isPresentationSupported(PhysicalDevice device, WorkQueue.Family family) {
		final var supported = new IntegerReference();
		library.vkGetPhysicalDeviceSurfaceSupportKHR(device, family.index(), this.handle(), supported);
		return NativeBooleanTransformer.isTrue(supported.get());
	}

	@Override
	protected void release() {
		library.vkDestroySurfaceKHR(instance, this, null);
	}

	/**
	 * Properties of this surface.
	 */
	public class Properties {
		private final PhysicalDevice device;

		/**
		 * Constructor.
		 * @param device Selected physical device
		 */
		public Properties(PhysicalDevice device) {
			this.device = requireNonNull(device);
		}

		/**
		 * @return This surface
		 */
		public VulkanSurface surface() {
			return VulkanSurface.this;
		}

		/**
		 * @return Capabilities of this surface
		 */
		public VkSurfaceCapabilitiesKHR capabilities() {
			final var capabilities = new VkSurfaceCapabilitiesKHR();
			library.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(device, VulkanSurface.this, capabilities);
			return capabilities;
		}

		/**
		 * @return Formats supported by this surface
		 */
		public List<VkSurfaceFormatKHR> formats() {
			final VulkanFunction<VkSurfaceFormatKHR[]> formats = (count, array) -> library.vkGetPhysicalDeviceSurfaceFormatsKHR(device, VulkanSurface.this, count, array);
			final VkSurfaceFormatKHR[] array = VulkanFunction.invoke(formats, VkSurfaceFormatKHR[]::new);
			return List.of(array);
		}

		/**
		 * Helper.
		 * Selects a supported surface format, falls back to the given default (if any), or otherwise returns {@link #defaultSurfaceFormat()}.
		 * @param matcher		Matches surface formats
		 * @param def			Optional default surface format
		 * @return Selected surface format
		 * @see #equals(VkSurfaceFormatKHR)
		 */
		public VkSurfaceFormatKHR select(Predicate<VkSurfaceFormatKHR> matcher, VkSurfaceFormatKHR def) {
			return this
					.formats()
					.stream()
					.filter(matcher)
					.findAny()
					.or(() -> Optional.ofNullable(def))
					.orElseGet(VulkanSurface::defaultSurfaceFormat);
		}

		/**
		 * Helper.
		 * Creates an equals predicate for the given surface format.
		 * @param format Surface format
		 * @return Equals predicate
		 * @implNote This method overrides the default behaviour of native structures which are compared by <b>identity</b>
		 */
		public static Predicate<VkSurfaceFormatKHR> equals(VkSurfaceFormatKHR format) {
			return that -> (that.format == format.format) && (that.colorSpace == format.colorSpace);
		}

		/**
		 * @return Presentation modes supported by this surface
		 */
		public List<VkPresentModeKHR> modes() {
			final VulkanFunction<VkPresentModeKHR[]> function = (count, array) -> library.vkGetPhysicalDeviceSurfacePresentModesKHR(device, VulkanSurface.this, count, array);
			final VkPresentModeKHR[] modes = VulkanFunction.invoke(function, VkPresentModeKHR[]::new);
			return List.of(modes);
		}

		/**
		 * Helper.
		 * Selects a preferred presentation mode from the given candidates or falls back to {@link #DEFAULT_PRESENTATION_MODE}.
		 * @param modes Candidate presentation modes
		 * @return Selected presentation mode
		 */
		public VkPresentModeKHR select(List<VkPresentModeKHR> modes) {
			final var available = this.modes();
			return modes
					.stream()
					.filter(available::contains)
					.findAny()
					.orElse(DEFAULT_PRESENTATION_MODE);
		}
	}

	/**
	 * Adapter that caches the properties of this surface.
	 */
	public class PropertiesAdapter extends Properties {
		private final VkSurfaceCapabilitiesKHR capabilities;
		private final List<VkSurfaceFormatKHR> formats;
		private final List<VkPresentModeKHR> modes;

		/**
		 * Constructor.
		 * @param device Selected physical device
		 */
		public PropertiesAdapter(PhysicalDevice device) {
			super(device);
			this.capabilities = super.capabilities();
			this.formats = super.formats();
			this.modes = super.modes();
		}

		@Override
		public VkSurfaceCapabilitiesKHR capabilities() {
			return capabilities;
		}

		@Override
		public List<VkSurfaceFormatKHR> formats() {
			return formats;
		}

		@Override
		public List<VkPresentModeKHR> modes() {
			return modes;
		}
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
		VkResult vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Handle surface, IntegerReference supported);

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
