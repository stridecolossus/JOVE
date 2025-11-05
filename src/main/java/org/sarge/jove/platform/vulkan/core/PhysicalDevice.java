package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceFeatures;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

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
		final var features = new VkPhysicalDeviceFeatures();
		library.vkGetPhysicalDeviceFeatures(handle, features);
		return DeviceFeatures.of(features);
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
	 * The <i>device enumeration helper</i> enumerates the available physical devices.
	 */
	public static class DeviceEnumerationHelper {
		private final Instance instance;
		private final Library library;

		/**
		 * Constructor.
		 * @param instance		Vulkan instance
		 * @param library		Device library
		 */
		public DeviceEnumerationHelper(Instance instance, Library library) {
			this.instance = requireNonNull(instance);
			this.library = requireNonNull(library);
		}

    	/**
    	 * Enumerates the physical devices for the given instance.
    	 * @param instance		Vulkan instance
    	 * @param library		Device library
    	 * @return Devices
    	 * @see Selector
    	 * @see #predicate(DeviceFeatures)
    	 */
    	public Stream<PhysicalDevice> enumerate() {
    		final VulkanFunction<Handle[]> enumerate = (count, devices) -> library.vkEnumeratePhysicalDevices(instance, count, devices);
    		final Handle[] devices = VulkanFunction.invoke(enumerate, Handle[]::new);

    		return Arrays
    				.stream(devices)
    				.map(this::device);
    	}

    	/**
    	 * Retrieves the queue families and creates a physical device.
    	 * @param device Device
    	 * @return Physical device
    	 */
    	private PhysicalDevice device(Handle device) {
    		// Retrieve queue families
    		final VulkanFunction<VkQueueFamilyProperties[]> function = (count, array) -> library.vkGetPhysicalDeviceQueueFamilyProperties(device, count, array);
    		final VkQueueFamilyProperties[] properties = VulkanFunction.invoke(function, VkQueueFamilyProperties[]::new);

    		// Convert to families
    		final List<Family> families = IntStream
    				.range(0, properties.length)
    				.mapToObj(n -> Family.of(n, properties[n]))
    				.toList();

    		// Create device
    		return new PhysicalDevice(device, families, instance, library);
    	}

    	/**
    	 * Helper - Creates a device predicate for the given required features.
    	 * @param required Required features
    	 * @return New device predicate
    	 */
    	public static Predicate<PhysicalDevice> predicate(DeviceFeatures required) {
    		return dev -> dev.features().contains(required);
    	}
	}

	/**
	 * A <i>device selector</i> is used to select a physical device with a queue family matching the requirements of the application.
	 * <p>
	 * Matching queue families are recorded as a side effect.
	 * The {@link #family(PhysicalDevice)} method retrieves the matching queue family for a given device.
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
					.peek(family -> matches.put(device, family))
					.findAny()
					.isPresent();
		}

		/**
		 * Selects the matching queue family for the given device.
		 * @param device Physical device
		 * @return Matching queue family
		 */
		public Family family(PhysicalDevice device) {
			return matches.get(device);
		}

		/**
		 * Creates a selector that matches a device with a family that supports <b>all</b> the given flags.
		 * @param flags Required flags
		 * @return Queue selector
		 */
		public static Selector queue(Set<VkQueueFlag> flags) {
			final BiPredicate<PhysicalDevice, Family> predicate = (_, family) -> family.flags().containsAll(flags);
			return new Selector(predicate);
		}
	}

	/**
	 * Vulkan physical device API.
	 */
	public interface Library {
		/**
		 * Enumerates the physical devices on this platform.
		 * @param instance					Vulkan instance
		 * @param pPhysicalDeviceCount		Number of devices
		 * @param devices					Device handles
		 * @return Result
		 */
		VkResult vkEnumeratePhysicalDevices(Instance instance, IntegerReference pPhysicalDeviceCount, @Returned Handle[] devices);

		/**
		 * Retrieves the properties of the given physical device.
		 * @param device		Device handle
		 * @param props			Device properties
		 */
		void vkGetPhysicalDeviceProperties(PhysicalDevice device, @Returned VkPhysicalDeviceProperties props);

		/**
		 * Retrieves the memory properties of the given physical device.
		 * @param device				Device
		 * @param pMemoryProperties		Memory properties
		 */
		void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, @Returned VkPhysicalDeviceMemoryProperties pMemoryProperties);

		/**
		 * Retrieves the supported features of the given physical device.
		 * @param device		Device handle
		 * @param features		Returned features
		 */
		void vkGetPhysicalDeviceFeatures(Handle device, @Returned VkPhysicalDeviceFeatures features);

		/**
		 * Enumerates the queue families of a device.
		 * @param device						Device handle
		 * @param pQueueFamilyPropertyCount		Number of queues family properties
		 * @param props							Queue family properties
		 */
		void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, @Returned VkQueueFamilyProperties[] pQueueFamilyProperties);

		/**
		 * Enumerates device-specific extension properties.
		 * @param device		Physical device handle
		 * @param layer			Layer name or {@code null} for extensions provided by the Vulkan implementation
		 * @param count			Number of extensions
		 * @param extensions	Returned extensions (pointer-to-array)
		 * @return Result
		 * @see Instance.Library#vkEnumerateInstanceExtensionProperties(String, IntegerReference, VkExtensionProperties[])
		 */
		VkResult vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntegerReference count, @Returned VkExtensionProperties[] extensions);

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
		VkResult vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntegerReference count, @Returned VkLayerProperties[] layers);

		/**
		 * Retrieves supported properties of the given format.
		 * @param device		Physical device handle
		 * @param format		Format
		 * @param props			Returned format properties
		 */
		void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, @Returned VkFormatProperties props);
	}
}
