package org.sarge.jove.platform.vulkan;

import com.sun.jna.Pointer;

/**
 * A <i>surface</i> is used to render to a window.
 * @author Sarge
 */
public class Surface { // extends PointerHandle {

	public Pointer handle() {
		return null;
	}


//	/**
//	 * Constructor.
//	 * @param surface		Surface handle
//	 * @param dev			Physical device
//	 * @see FrameworkDesktopService#surface(Pointer, Pointer)
//	 */
//	public static Surface create(Pointer surface, VulkanInstance instance, PhysicalDevice dev) {
//		// Get surface capabilities
//		final Vulkan vulkan = dev.vulkan();
//		final VulkanLibrary lib = vulkan.api();
//		final VkSurfaceCapabilitiesKHR caps = new VkSurfaceCapabilitiesKHR.ByReference();
//		check(lib.vkGetPhysicalDeviceSurfaceCapabilitiesKHR(dev.handle(), surface, caps));
//
//		// Get supported formats
//		final VulkanFunction<VkSurfaceFormatKHR> func = (count, array) -> lib.vkGetPhysicalDeviceSurfaceFormatsKHR(dev.handle(), surface, count, array);
//		final var formats = Arrays.asList(VulkanFunction.enumerate(func, vulkan.factory().integer(), new VkSurfaceFormatKHR()));
//
//		// Get supported presentation modes
//		final var modes = loadModes(lib, dev, surface);
//
//		// Create surface
//		return new Surface(surface, instance, caps, formats, modes);
//	}
//
//	/**
//	 * @return Supported presentation modes
//	 */
//	// TODO - can VulkanFunction::enumerate be extended to support this?
//	private static Set<VkPresentModeKHR> loadModes(VulkanLibrary lib, PhysicalDevice dev, Pointer surface) {
//		// Count number of modes
//		final IntByReference count = new IntByReference(1);
//		lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), surface, count, null);
//
//		// Retrieve modes
//		final int[] modes = new int[count.getValue()];
//		if(count.getValue() > 0) {
//			lib.vkGetPhysicalDeviceSurfacePresentModesKHR(dev.handle(), surface, count, modes);
//		}
//
//		// Convert to enumeration set
//		return Arrays.stream(modes).mapToObj(n -> IntegerEnumeration.map(VkPresentModeKHR.class, n)).collect(toSet());
//	}
//
//	private final VulkanInstance instance;
//	private final VkSurfaceCapabilitiesKHR caps;
//	private final List<VkSurfaceFormatKHR> formats;
//	private final Set<VkPresentModeKHR> modes;
//
//	/**
//	 * Constructor.
//	 * @param handle		Handle
//	 * @param instance		Vulkan instance
//	 * @param caps 			Surface capabilities
//	 * @param formats		Supported formats
//	 * @param modes			Supported presentation modes
//	 */
//	protected Surface(Pointer handle, VulkanInstance instance, VkSurfaceCapabilitiesKHR caps, List<VkSurfaceFormatKHR> formats, Set<VkPresentModeKHR> modes) {
//		super(handle);
//		this.instance = notNull(instance);
//		this.caps = notNull(caps);
//		this.formats = List.copyOf(formats);
//		this.modes = Set.copyOf(modes);
//	}
//
//	/**
//	 * @return Capabilities of this surface
//	 */
//	public VkSurfaceCapabilitiesKHR capabilities() {
//		return caps;
//	}
//
//	/**
//	 * @return Supported surface formats
//	 */
//	public List<VkSurfaceFormatKHR> formats() {
//		return formats;
//	}
//
//	/**
//	 * @return Supported presentation modes
//	 */
//	public Set<VkPresentModeKHR> modes() {
//		return modes;
//	}
//
//	@Override
//	public synchronized void destroy() {
//		final VulkanLibrarySurface lib = instance.vulkan().api();
//		lib.vkDestroySurfaceKHR(instance.handle(), super.handle(), null);
//	}
}
