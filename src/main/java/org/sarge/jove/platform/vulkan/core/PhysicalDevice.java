package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.junit.jupiter.api.extension.Extensions;
import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.render.Surface;
import org.sarge.jove.platform.vulkan.util.*;

/**
 * A <i>physical device</i> represents a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject {
	private final Handle handle;
	private final Instance instance;
	private final List<Family> families;
	private final SupportedFeatures features;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param instance		Parent instance
	 * @param families		Queue families
	 * @param features		Features supported by this device
	 */
	PhysicalDevice(Handle handle, Instance instance, List<Family> families, SupportedFeatures features) {
		this.handle = requireNonNull(handle);
		this.instance = requireNonNull(instance);
		this.families = List.copyOf(families);
		this.features = requireNonNull(features);
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
		final var props = new VkPhysicalDeviceProperties();
//		lib.vkGetPhysicalDeviceProperties(this, props); // props);
		return props;
	}

	/**
	 * Retrieves the memory properties of this device.
	 * @return Device memory properties
	 */
	public VkPhysicalDeviceMemoryProperties memory() {
    	final var properties = new VkPhysicalDeviceMemoryProperties();
    	// TODO
//    	instance.vulkan().library().vkGetPhysicalDeviceMemoryProperties(this, properties);
    	return properties;
	}

	/**
	 * @return Features supported by this device
	 */
	public SupportedFeatures features() {
		return features;
	}

	/**
	 * @return Extensions supported by this device
	 * @see Extensions
	 */
	public Set<String> extensions() {
//		final VulkanLibrary lib = instance.library();
//		final VulkanFunction<VkExtensionProperties> function = (count, array) -> lib.vkEnumerateDeviceExtensionProperties(this, null, count, array);
//		final IntegerReference count = instance.vulkan().factory().integer();
//		return Extensions.extensions(count, function);
		return null;
	}

	/**
	 * @return Validation layers supported by this device
	 * @deprecated Since 1.0.13 device-only layers are deprecated
	 * @see VulkanLibrary#vkEnumerateDeviceLayerProperties(PhysicalDevice, IntByReference, VkLayerProperties)
	 */
	@Deprecated
	public Set<ValidationLayer> layers() {
//		final VulkanLibrary lib = instance.library();
//		final VulkanFunction<VkLayerProperties> func = (count, array) -> lib.vkEnumerateDeviceLayerProperties(this, count, array);
//		final IntegerReference count = instance.vulkan().factory().integer();
//		return ValidationLayer.layers(count, func);
		return null;
	}

	/**
	 * Tests whether this device supports presentation to the given surface.
	 * @param surface		Rendering surface
	 * @param family		Queue family
	 * @return Whether presentation is supported by the given family
	 * @see Selector#of(Surface)
	 */
	public boolean isPresentationSupported(Handle surface, Family family) {
		final Vulkan vulkan = instance.vulkan();
		final NativeReference<Integer> supported = vulkan.factory().integer();
		instance.vulkan().library().vkGetPhysicalDeviceSurfaceSupportKHR(this, family.index(), surface, supported);
		return supported.get() == 1;
	}

	/**
	 * Retrieves the supported properties of the given format.
	 * @param format Format
	 * @return Format properties
	 */
	public VkFormatProperties properties(VkFormat format) {
		final var props = new VkFormatProperties();
		instance.vulkan().library().vkGetPhysicalDeviceFormatProperties(this, format, props);
		return props;
	}

	/**
	 * Enumerates the physical devices for the given instance.
	 * @param instance Vulkan instance
	 * @return Devices
	 * @see Selector
	 * @see #predicate(DeviceFeatures)
	 */
	public static Stream<PhysicalDevice> devices(Instance instance) {
		// Enumerate physical devices
		final Vulkan vulkan = instance.vulkan();
		final VulkanLibrary lib = vulkan.library();
		final VulkanFunction<Handle[]> enumerate = (count, devices) -> lib.vkEnumeratePhysicalDevices(instance, count, devices);
		final Handle[] handles = vulkan.invoke(enumerate, Handle[]::new);

		// Helper
		final var builder = new Object() {
			/**
			 * @return Physical device
			 */
			PhysicalDevice device(Handle handle) {
				final List<Family> families = families(handle);
				final SupportedFeatures features = features(handle);
				return new PhysicalDevice(handle, instance, families, features);
			}

			/**
			 * @return Queue families for the given device
			 */
			private List<Family> families(Handle handle) {
				final VulkanFunction<VkQueueFamilyProperties[]> function = (count, array) -> lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array);
				final VkQueueFamilyProperties[] properties = vulkan.invoke(function, VkQueueFamilyProperties[]::new);
				return IntStream
						.range(0, properties.length)
						.mapToObj(n -> Family.of(n, properties[n]))
						.toList();
			}

			/**
			 * @return Supported features of the given device
			 */
			private SupportedFeatures features(Handle handle) {
				final var features = new VkPhysicalDeviceFeatures();
				lib.vkGetPhysicalDeviceFeatures(handle, features);
				return new SupportedFeatures(features);
			}
		};

		// Build physical devices
		return Arrays
				.stream(handles)
				.map(builder::device);
	}

	/**
	 * Helper - Creates a device predicate for the given required features.
	 * @param required Required features
	 * @return New device predicate
	 */
	public static Predicate<PhysicalDevice> predicate(DeviceFeatures required) {
		return dev -> dev.features().contains(required);
	}

	/**
	 * A <i>physical device selector</i> is a helper class used when selecting a physical device.
	 * <p>
	 * Selectors have two purposes:
	 * <ol>
	 * <li>selecting an appropriate physical device (note that a selector is itself a device predicate)</li>
	 * <li>retrieving matching queue families when configuring the logical device, see {@link Selector#select(PhysicalDevice)}</li>
	 * </ol>
	 * <p>
	 * The class provides factory methods for the general use cases:
	 * <ul>
	 * <li>{@link Selector#of(VkQueueFlag...)} is used to match devices that contain a queue family with the specified flags</li>
	 * <li>{@link Selector#of(Handle)} matches a device that supports <i>presentation</i> to a given Vulkan surface</li>
	 * </ul>
	 * <p>
	 * Example:
	 * <pre>
	 * // Create a selector for the graphics queue
	 * Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);
	 *
	 * // Create a selector for a device that supports presentation
	 * Handle surface = ...
	 * Selector presentation = Selector.of(surface);
	 *
	 * // Select matching physical device
	 * PhysicalDevice dev = new Enumerator(instance)
	 *     .devices()
	 *     .filter(graphics)
	 *     .filter(presentation)
	 *     .findAny()
	 *     .orElseThrow();
	 *
	 * // Retrieve queue families and create logical device
	 * Family graphicsFamily = graphics.select(dev);
	 * Family presentationFamily = presentation.select(dev);
	 * ...
	 * </pre>
	 * <p>
	 * @see Enumerator
	 */
	public static class Selector implements Predicate<PhysicalDevice> {
		/**
		 * Creates a selector for a device with a queue family matching the given set of flags.
		 * @param flags Queue flags to match
		 * @return New queue flags selector
		 */
		public static Selector of(VkQueueFlag... flags) {
			final var copy = Set.of(flags);
			final BiPredicate<PhysicalDevice, Family> predicate = (__, family) -> family.flags().containsAll(copy);
			return new Selector(predicate);
		}

		/**
		 * Creates a selector for a device that supports presentation.
		 * @param surface Vulkan surface
		 * @return New presentation selector
		 * @see PhysicalDevice#isPresentationSupported(Handle, Family)
		 */
		public static Selector of(Handle surface) {
			final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> dev.isPresentationSupported(surface, family);
			return new Selector(predicate);
		}

		private final BiPredicate<PhysicalDevice, Family> predicate;
		private final Map<PhysicalDevice, Optional<Family>> results = new HashMap<>();

		/**
		 * Constructor.
		 * @param predicate Queue family predicate
		 */
		public Selector(BiPredicate<PhysicalDevice, Family> predicate) {
			this.predicate = requireNonNull(predicate);
		}

		@Override
		public boolean test(PhysicalDevice dev) {
			return find(dev).isPresent();
		}

		/**
		 * Selects the queue family matching this selector from the given device.
		 * Note this method assumes the device has been matched by this selector as a side-effect.
		 * @param dev Device
		 * @return Queue family
		 * @throws NoSuchElementException if the device does not contain a matching queue family
		 */
		public Family select(PhysicalDevice dev) {
			return find(dev).orElseThrow();
		}

		/**
		 * Finds and caches the matching queue family from the given device.
		 * @param dev Device
		 * @return Queue family
		 */
		private Optional<Family> find(PhysicalDevice dev) {
			return results.computeIfAbsent(dev, this::findLocal);
		}

		/**
		 * Finds the matching queue family from the given device.
		 * @param dev Device
		 * @return Queue family
		 */
		private Optional<Family> findLocal(PhysicalDevice dev) {
			return dev
					.families
					.stream()
					.filter(family -> predicate.test(dev, family))
					.findAny();
		}
	}

	/**
	 * Vulkan physical device API.
	 */
	interface Library {
		/**
		 * Enumerates the physical devices on this platform.
		 * @param instance					Vulkan instance
		 * @param pPhysicalDeviceCount		Number of devices
		 * @param devices					Device handles
		 * @return Result
		 */
		int vkEnumeratePhysicalDevices(Instance instance, NativeReference<Integer> pPhysicalDeviceCount, @Returned Handle[] devices);

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
//		void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, @Returned VkPhysicalDeviceMemoryProperties pMemoryProperties);

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
		void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, NativeReference<Integer> pQueueFamilyPropertyCount, @Returned VkQueueFamilyProperties[] props);

		/**
		 * Enumerates device-specific extension properties.
		 * @param device		Physical device handle
		 * @param layer			Layer name or {@code null} for extensions provided by the Vulkan implementation
		 * @param count			Number of extensions
		 * @param extensions	Returned extensions (pointer-to-array)
		 * @return Result
		 * @see Instance.Library#vkEnumerateInstanceExtensionProperties(String, IntByReference, VkExtensionProperties)
		 */
//		int vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, NativeReference<Integer> count, @Returned VkExtensionProperties[] extensions);

		/**
		 * Enumerates device-specific validation layers.
		 * @param device		Physical device handle
		 * @param count			Number of layers
		 * @param extensions	Returned layers (pointer-to-array)
		 * @return Result
		 * @deprecated Since 1.0.13 device-only layers are deprecated and this method <b>must</b> return the layers for the Vulkan implementation
		 * @see Instance.Library#vkEnumerateInstanceLayerProperties(IntByReference, VkLayerProperties)
		 */
//		@Deprecated
//		int vkEnumerateDeviceLayerProperties(PhysicalDevice device, NativeReference<Integer> count, @Returned VkLayerProperties[] layers);

		/**
		 * Retrieves supported properties of the given format.
		 * @param device		Physical device handle
		 * @param format		Format
		 * @param props			Returned format properties
		 */
		void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, @Returned VkFormatProperties props);

		// TODO - temp copied from surface
		int vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice device, int queueFamilyIndex, Handle surface, NativeReference<Integer> supported);
	}
}
