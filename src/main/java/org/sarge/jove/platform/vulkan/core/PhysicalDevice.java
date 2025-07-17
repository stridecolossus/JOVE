package org.sarge.jove.platform.vulkan.core;

import static java.util.Objects.requireNonNull;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.*;

import org.sarge.jove.common.*;
import org.sarge.jove.foreign.*;
import org.sarge.jove.foreign.NativeReference.IntegerReference;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.util.*;

/**
 * A <i>physical device</i> represents a hardware component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject {
	private final Handle handle;
	private final VulkanLibrary vulkan;
	private final List<Family> families;
	private final SupportedFeatures features;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param vulkan		Vulkan library
	 * @param families		Queue families
	 * @param features		Features supported by this device
	 */
	PhysicalDevice(Handle handle, VulkanLibrary vulkan, List<Family> families, SupportedFeatures features) {
		this.handle = requireNonNull(handle);
		this.vulkan = requireNonNull(vulkan);
		this.families = List.copyOf(families);
		this.features = requireNonNull(features);
	}

	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Vulkan library
	 */
	public VulkanLibrary vulkan() {
		return vulkan;
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
		vulkan.vkGetPhysicalDeviceProperties(this, properties);
		return properties;
	}

	/**
	 * Retrieves the memory properties of this device.
	 * @return Device memory properties
	 */
	public VkPhysicalDeviceMemoryProperties memory() {
    	final var properties = new VkPhysicalDeviceMemoryProperties();
    	vulkan.vkGetPhysicalDeviceMemoryProperties(this, properties);
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
		final VulkanFunction<VkExtensionProperties[]> enumerate = (count, array) -> vulkan.vkEnumerateDeviceExtensionProperties(this, null, count, array);
		VulkanFunction.invoke(enumerate, VkExtensionProperties[]::new);
//		return Extensions.extensions(count, function);
		return null; // TODO
	}

	/**
	 * @return Validation layers supported by this device
	 * @deprecated Since 1.0.13 device-only layers are deprecated
	 * @see VulkanLibrary#vkEnumerateDeviceLayerProperties(PhysicalDevice, IntByReference, VkLayerProperties)
	 */
	@Deprecated
	public Collection<ValidationLayer> layers() {
		final VulkanFunction<VkLayerProperties[]> enumerate = (count, array) -> vulkan.vkEnumerateDeviceLayerProperties(this, count, array);
		final VkLayerProperties[] layers = VulkanFunction.invoke(enumerate, VkLayerProperties[]::new);
		return Arrays
				.stream(layers)
				.map(ValidationLayer::of)
				.toList();

	}

	/**
	 * Tests whether this device supports presentation to the given surface.
	 * @param surface		Rendering surface
	 * @param family		Queue family
	 * @return Whether presentation is supported by the given family
	 * @see Selector#of(Surface)
	 */
	public boolean isPresentationSupported(Handle surface, Family family) {
		final var supported = new IntegerReference();
		vulkan.vkGetPhysicalDeviceSurfaceSupportKHR(this, family.index(), surface, supported);
		return supported.get() == 1;
	}

	/**
	 * Retrieves the supported properties of the given format.
	 * @param format Format
	 * @return Format properties
	 */
	public VkFormatProperties properties(VkFormat format) {
		final var props = new VkFormatProperties();
		vulkan.vkGetPhysicalDeviceFormatProperties(this, format, props);
		return props;
	}

	/**
	 * Enumerates the physical devices for the given instance.
	 * @param instance Vulkan instance
	 * @return Devices
	 * @see Selector
	 * @see #predicate(DeviceFeatures)
	 */
	public static Stream<PhysicalDevice> enumerate(Instance instance) {
		// Enumerate physical devices
		final VulkanLibrary vulkan = instance.vulkan();
		final VulkanFunction<Handle[]> enumerate = (count, devices) -> vulkan.vkEnumeratePhysicalDevices(instance, count, devices);
		final Handle[] devices = VulkanFunction.invoke(enumerate, Handle[]::new);

		// Helper
		final var builder = new Object() {
			/**
			 * @return Physical device
			 */
			PhysicalDevice device(Handle handle) {
				final List<Family> families = families(handle);
				final SupportedFeatures features = features(handle);
//				final SupportedFeatures features = new SupportedFeatures(new VkPhysicalDeviceFeatures());
				return new PhysicalDevice(handle, vulkan, families, features);
			}

			/**
			 * @return Queue families for the given device
			 */
			private List<Family> families(Handle device) {
				final VulkanFunction<VkQueueFamilyProperties[]> function = (count, array) -> vulkan.vkGetPhysicalDeviceQueueFamilyProperties(device, count, array);
				final VkQueueFamilyProperties[] properties = VulkanFunction.invoke(function, VkQueueFamilyProperties[]::new);
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
				vulkan.vkGetPhysicalDeviceFeatures(handle, features);
				return new SupportedFeatures(features);
			}
		};

		// Build physical devices
		return Arrays.stream(devices).map(builder::device);
	}

	/**
	 * Helper - Creates a device predicate for the given required features.
	 * @param required Required features
	 * @return New device predicate
	 */
	public static Predicate<PhysicalDevice> predicate(DeviceFeatures required) {
		return dev -> dev.features().contains(required);
	}
	// TODO - ? DeviceFeatures implements Predicate<PhysicalDevice>

	/**
	 * A <i>device selector</i> is used to select physical devices matching the requirements of the application.
	 * <p>
	 * The {@link #select(PhysicalDevice)} method retrieves the matching queue family for the selected device.
	 * <p>
	 * This class provides factory methods for the general use cases:
	 * <ul>
	 * <li>{@link Selector#family(VkQueueFlag...)} matches devices that support a set of required queue properties</li>
	 * <li>{@link Selector#presentation(Handle)} matches devices that support <i>presentation</i> to a given Vulkan surface</li>
	 * </ul>
	 * <p>
	 */
	public static abstract class Selector implements Predicate<PhysicalDevice> {
		/**
		 * Selects devices with a queue that matches the given flags.
		 * @param flags Queue flags
		 * @return Queue selector
		 */
		public static Selector family(VkQueueFlag... flags) {
			return new Selector() {
				private final Set<VkQueueFlag> required = Set.of(flags);

				@Override
				protected boolean matches(PhysicalDevice device, Family family) {
					return family.flags().containsAll(required);
				}
			};
		}

		/**
		 * Selects devices that support presentation.
		 * @param surface Vulkan surface
		 * @return Presentation selector
		 * @see PhysicalDevice#isPresentationSupported(Handle, Family)
		 */
		public static Selector presentation(Handle surface) {
			return new Selector() {
				@Override
				protected boolean matches(PhysicalDevice device, Family family) {
					return device.isPresentationSupported(surface, family);
				}
			};
		}

		private final Map<PhysicalDevice, Family> results = new HashMap<>();

		/**
		 * Matches this selector.
		 * @param device Physical device
		 * @param family Queue family
		 * @return Whether matched
		 */
		protected abstract boolean matches(PhysicalDevice device, Family family);

		@Override
		public boolean test(PhysicalDevice device) {
			return device
					.families
					.stream()
					.filter(family -> matches(device, family))
					.peek(match -> results.put(device, match))
					.findAny()
					.isPresent();
		}

		/**
		 * Selects the queue family matching this selector from the given device.
		 * Note this method assumes the device has been matched by this selector as a side-effect.
		 * @param dev Device
		 * @return Queue family
		 * @throws NoSuchElementException if the device does not contain a matching queue family
		 */
		public Family select(PhysicalDevice device) {
			final Family family = results.get(device);
			if(family == null) throw new UnsupportedOperationException();
			return family;
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
		VkResult vkEnumeratePhysicalDevices(Instance instance, NativeReference<Integer> pPhysicalDeviceCount, @Returned Handle[] devices);

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
		void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntegerReference pQueueFamilyPropertyCount, @Returned VkQueueFamilyProperties[] props);

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
