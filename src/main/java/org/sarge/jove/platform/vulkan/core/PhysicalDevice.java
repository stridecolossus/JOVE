package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;

/**
 * A <i>physical device</i> represents a hardware component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject {
	private final Handle handle;
	private final Library library;
	private final Instance instance;
	private final List<Family> families;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param families		Queue families
	 * @param instance		Vulkan instance
	 * @param library		Device library
	 */
	PhysicalDevice(Handle handle, List<Family> families, Instance instance, Library library) {
		this.handle = requireNonNull(handle);
		this.families = List.copyOf(families);
		this.instance = requireNonNull(instance);
		this.library = requireNonNull(library);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Vulkan instance
	 */
	public Instance instance() {
		return instance;
	}

	/**
	 * @return Queue families for this device
	 */
	public List<Family> families() {
		return families;
	}

	/**
	 * Retrieves the properties of this device.
	 * @return Device properties
	 */
	public VkPhysicalDeviceProperties properties() {
		final var properties = new VkPhysicalDeviceProperties();
		library.vkGetPhysicalDeviceProperties(this, properties);
		return properties;
	}

	/**
	 * Retrieves the memory properties of this device.
	 * @return Device memory properties
	 */
	public VkPhysicalDeviceMemoryProperties memory() {
    	final var properties = new VkPhysicalDeviceMemoryProperties();
    	library.vkGetPhysicalDeviceMemoryProperties(this, properties);
    	return properties;
	}

	/**
	 * @return Features supported by this device
	 */
	public DeviceFeatures features() {
		final var supported = new VkPhysicalDeviceFeatures();
		library.vkGetPhysicalDeviceFeatures(handle, supported);
		return DeviceFeatures.of(supported);
	}

	/**
	 * @param layer Optional layer name
	 * @return Extensions supported by this device
	 */
	public VkExtensionProperties[] extensions(String layer) {
		final VulkanFunction<VkExtensionProperties[]> enumerate = (count, array) -> library.vkEnumerateDeviceExtensionProperties(this, layer, count, array);
		return VulkanFunction.invoke(enumerate, VkExtensionProperties[]::new);
	}

	/**
	 * @return Validation layers supported by this device
	 * @deprecated Since 1.0.13 device-only layers are deprecated
	 * @see Library#vkEnumerateDeviceLayerProperties(PhysicalDevice, IntegerReference, VkLayerProperties[])
	 */
	@Deprecated
	public VkLayerProperties[] layers() {
		final VulkanFunction<VkLayerProperties[]> enumerate = (count, array) -> library.vkEnumerateDeviceLayerProperties(this, count, array);
		return VulkanFunction.invoke(enumerate, VkLayerProperties[]::new);
	}

	/**
	 * Retrieves the supported properties of the given format.
	 * @param format Format
	 * @return Format properties
	 */
	public VkFormatProperties properties(VkFormat format) {
		final var props = new VkFormatProperties();
		library.vkGetPhysicalDeviceFormatProperties(this, format, props);
		return props;
	}

	/**
	 * Enumerates the available physical devices.
	 * @param instance		Vulkan instance
	 * @param library		Device library
	 * @return Physical devices
	 */
	public static Stream<PhysicalDevice> enumerate(Instance instance, Library library) {
		// Builder for each device
		final var builder = new Object() {
			/**
			 * @param device Device handle
			 * @return Physical device
			 */
	    	public PhysicalDevice device(Handle device) {
	    		final List<Family> families = families(device);
	    		return new PhysicalDevice(device, families, instance, library);
	    	}

	    	/**
	    	 * @Return Queue families for the given device
	    	 */
	    	private List<Family> families(Handle device) {
	    		final VulkanFunction<VkQueueFamilyProperties[]> function = (count, array) -> library.vkGetPhysicalDeviceQueueFamilyProperties(device, count, array);
	    		final VkQueueFamilyProperties[] properties = VulkanFunction.invoke(function, VkQueueFamilyProperties[]::new);
	    		return IntStream
	    				.range(0, properties.length)
	    				.mapToObj(n -> Family.of(n, properties[n]))
	    				.toList();
	    	}
		};

		// Enumerate devices
		final VulkanFunction<Handle[]> enumerate = (count, devices) -> library.vkEnumeratePhysicalDevices(instance, count, devices);
		final Handle[] devices = VulkanFunction.invoke(enumerate, Handle[]::new);
		return Arrays.stream(devices).map(builder::device);
	}

	/**
	 * Helper.
	 * Creates a device filter for the given required features.
	 * @param required Required features
	 * @return Device features filter
	 */
	public static Predicate<PhysicalDevice> predicate(DeviceFeatures required) {
		return dev -> dev.features().contains(required);
	}

	/**
	 * A <i>device selector</i> is used to select a physical device with a queue family matching the requirements of the application.
	 * <p>
	 * The matching queue family is recorded as a side effect and can be retrieved using the {@link #family(PhysicalDevice)} method.
	 * <p>
	 * Note that the same queue family may be returned by multiple selectors, i.e. families often support multiple use cases.
	 */
	public static class Selector implements Predicate<PhysicalDevice> {
		private final BiPredicate<PhysicalDevice, Family> predicate;
		private final Map<PhysicalDevice, Family> matches = new HashMap<>();

		/**
		 * Constructor.
		 * @param predicate Underlying device-family predicate
		 */
		public Selector(BiPredicate<PhysicalDevice, Family> predicate) {
			this.predicate = requireNonNull(predicate);
		}

		@Override
		public boolean test(PhysicalDevice device) {
			return device
					.families()
					.stream()
					.filter(family -> predicate.test(device, family))
					.peek(family -> matches.putIfAbsent(device, family))
					.findAny()
					.isPresent();
		}

		/**
		 * Selects the matched queue family for the given device.
		 * @param device Physical device
		 * @return Matching queue family
		 */
		public Family family(PhysicalDevice device) {
			return matches.get(device);
		}

		/**
		 * Creates a selector that matches a device with a queue family that supports <b>all</b> the given flags.
		 * @param flags Required flags
		 * @return Queue selector
		 */
		public static Selector queue(VkQueueFlag... flags) {
			final var required = Set.of(flags);
			final BiPredicate<PhysicalDevice, Family> predicate = (_, family) -> family.flags().containsAll(required);
			return new Selector(predicate);
		}
	}

	/**
	 * Physical device API.
	 */
	interface Library {
		/**
		 * Enumerates the physical devices on this platform.
		 * @param instance					Vulkan instance
		 * @param pPhysicalDeviceCount		Number of devices
		 * @param devices					Device handles
		 * @return Result
		 */
		VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, @Updated Handle[] devices);

		/**
		 * Retrieves the properties of the given physical device.
		 * @param device		Device handle
		 * @param props			Device properties
		 */
		void vkGetPhysicalDeviceProperties(PhysicalDevice device, @Updated VkPhysicalDeviceProperties props);

		/**
		 * Retrieves the memory properties of the given physical device.
		 * @param device				Device
		 * @param pMemoryProperties		Memory properties
		 */
		void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, @Updated VkPhysicalDeviceMemoryProperties pMemoryProperties);

		/**
		 * Retrieves the supported features of the given physical device.
		 * @param device		Device handle
		 * @param features		Returned features
		 */
		void vkGetPhysicalDeviceFeatures(Handle device, @Updated VkPhysicalDeviceFeatures features);

		/**
		 * Enumerates the queue families of a device.
		 * @param device						Device handle
		 * @param pQueueFamilyPropertyCount		Number of queues family properties
		 * @param props							Queue family properties
		 */
		void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, @Updated VkQueueFamilyProperties[] pQueueFamilyProperties);

		/**
		 * Enumerates device-specific extension properties.
		 * @param device		Physical device handle
		 * @param layer			Layer name or {@code null} for extensions provided by the Vulkan implementation
		 * @param count			Number of extensions
		 * @param extensions	Returned extensions (pointer-to-array)
		 * @return Result
		 * @see Instance.Library#vkEnumerateInstanceExtensionProperties(String, IntegerReference, VkExtensionProperties[])
		 */
		VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, @Updated VkExtensionProperties[] extensions);

		/**
		 * Enumerates device-specific validation layers.
		 * @param device		Physical device handle
		 * @param count			Number of layers
		 * @param extensions	Returned layers (pointer-to-array)
		 * @return Result
		 * @deprecated Since 1.0.13 device-only layers are deprecated and this method <b>must</b> return the layers for the Vulkan implementation
		 * @see Instance.Library#vkEnumerateInstanceLayerProperties(IntegerReference, VkLayerProperties[])
		 */
		@Deprecated
		VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, @Updated VkLayerProperties[] layers);

		/**
		 * Retrieves supported properties of the given format.
		 * @param device		Physical device handle
		 * @param format		Format
		 * @param props			Returned format properties
		 */
		void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, @Updated VkFormatProperties props);
	}
}
