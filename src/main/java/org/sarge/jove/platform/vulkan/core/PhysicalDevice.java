package org.sarge.jove.platform.vulkan.core;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.render.Surface;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.platform.vulkan.util.VulkanFunction.StructureVulkanFunction;
import org.sarge.jove.util.NativeBooleanConverter;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>physical device</i> represents a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject {
	private final Handle handle;
	private final Instance instance;
	private final List<Family> families;
	private final DeviceFeatures features;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param instance		Parent instance
	 * @param families		Queue families
	 * @param features		Features supported by this device
	 */
	PhysicalDevice(Handle handle, Instance instance, List<Family> families, DeviceFeatures features) {
		this.handle = notNull(handle);
		this.instance = notNull(instance);
		this.families = List.copyOf(families);
		this.features = notNull(features);
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
		final VulkanLibrary lib = instance.library();
		lib.vkGetPhysicalDeviceProperties(PhysicalDevice.this, props);
		return props;
	}

	/**
	 * @return Features supported by this device
	 */
	public DeviceFeatures features() {
		return features;
	}

	/**
	 * @return Extensions supported by this device
	 */
	public Set<String> extensions() {
		final VulkanLibrary lib = instance.library();
		final StructureVulkanFunction<VkExtensionProperties> func = (count, array) -> lib.vkEnumerateDeviceExtensionProperties(this, null, count, array);
		final IntByReference count = instance.factory().integer();
		return VulkanLibrary.extensions(count, func);
	}

	/**
	 * @return Validation layers supported by this device
	 * @deprecated Since 1.0.13 device-only layers are deprecated
	 * @see VulkanLibrary#vkEnumerateDeviceLayerProperties(PhysicalDevice, IntByReference, VkLayerProperties)
	 */
	@Deprecated
	public Set<ValidationLayer> layers() {
		final VulkanLibrary lib = instance.library();
		final StructureVulkanFunction<VkLayerProperties> func = (count, array) -> lib.vkEnumerateDeviceLayerProperties(this, count, array);
		final IntByReference count = instance.factory().integer();
		return ValidationLayer.layers(count, func);
	}

	/**
	 * Tests whether this device supports presentation to the given surface.
	 * @param surface		Rendering surface
	 * @param family		Queue family
	 * @return Whether presentation is supported by the given family
	 * @see Selector#of(Surface)
	 */
	public boolean isPresentationSupported(Handle surface, Family family) {
		// Retrieve supported flag
		final VulkanLibrary lib = instance.library();
		final IntByReference supported = instance.factory().integer();
		check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(this, family.index(), surface, supported));

		// Convert native integer to boolean
		final var converter = new NativeBooleanConverter();
		return converter.fromNative(supported.getValue(), null);
	}

	/**
	 * Retrieves the supported properties of the given format.
	 * @param format Format
	 * @return Format properties
	 */
	public VkFormatProperties properties(VkFormat format) {
		final var props = new VkFormatProperties();
		instance.library().vkGetPhysicalDeviceFormatProperties(this, format, props);
		return props;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof PhysicalDevice that) &&
				this.handle.equals(that.handle) &&
				this.instance.equals(that.instance) &&
				this.families.equals(that.families) &&
				this.features.equals(that.features);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", handle)
				.append("instance", instance)
				.append("families", families.size())
				.build();
	}

	/**
	 * Enumerates the physical devices for the given instance.
	 * @param instance Vulkan instance
	 * @return Devices
	 * @see Selector
	 * @see #predicate(DeviceFeatures)
	 */
	public static Stream<PhysicalDevice> devices(Instance instance) {
		// Enumerate device handles
		final VulkanFunction<Pointer[]> enumerate = (count, devices) -> instance.library().vkEnumeratePhysicalDevices(instance, count, devices);
		final IntByReference count = instance.factory().integer();
		final Pointer[] handles = enumerate.invoke(count, Pointer[]::new);

		// Init device constructor
		final Function<Handle, PhysicalDevice> ctor = handle -> {
			// Enumerate queue families for this device (for some reason the return type is void)
			final StructureVulkanFunction<VkQueueFamilyProperties> func = (n, array) -> {
				instance.library().vkGetPhysicalDeviceQueueFamilyProperties(handle, n, array);
				return VulkanLibrary.SUCCESS;
			};
			final IntByReference num = instance.factory().integer();
			final VkQueueFamilyProperties[] props = func.invoke(num, new VkQueueFamilyProperties());

			// Create queue families
			final List<Family> families = IntStream
					.range(0, props.length)
					.mapToObj(n -> Family.of(n, props[n]))
					.toList();

			// Retrieve device features
			final var features = new VkPhysicalDeviceFeatures();
			instance.library().vkGetPhysicalDeviceFeatures(handle, features);

			// Create device
			return new PhysicalDevice(handle, instance, families, DeviceFeatures.supported(features));
		};

		// Create devices
		return Arrays
				.stream(handles)
				.map(Handle::new)
				.map(ctor);
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
			this.predicate = notNull(predicate);
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
		 * @param instance		Vulkan instance
		 * @param count			Number of devices
		 * @param devices		Device handles
		 * @return Result
		 */
		int vkEnumeratePhysicalDevices(Instance instance, IntByReference count, Pointer[] devices);

		/**
		 * Retrieves the properties of the given physical device.
		 * @param device		Device handle
		 * @param props			Returned device properties
		 */
		void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props);

		/**
		 * Retrieves the memory properties of the given physical device.
		 * @param device				Device
		 * @param pMemoryProperties		Returned memory properties
		 */
		void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties);

		/**
		 * Retrieves the supported features of the given physical device.
		 * @param device		Device handle
		 * @param features		Returned features
		 */
		void vkGetPhysicalDeviceFeatures(Handle device, VkPhysicalDeviceFeatures features);

		/**
		 * Enumerates the queue families of a device.
		 * @param device		Device handle
		 * @param count			Number of properties
		 * @param props			Queue family properties (pointer-to-array)
		 */
		void vkGetPhysicalDeviceQueueFamilyProperties(Handle device, IntByReference count, VkQueueFamilyProperties props);

		/**
		 * Enumerates device-specific extension properties.
		 * @param device		Physical device handle
		 * @param layer			Layer name or {@code null} for extensions provided by the Vulkan implementation
		 * @param count			Number of extensions
		 * @param extensions	Returned extensions (pointer-to-array)
		 * @return Result
		 * @see Instance.Library#vkEnumerateInstanceExtensionProperties(String, IntByReference, VkExtensionProperties)
		 */
		int vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntByReference count, VkExtensionProperties extensions);

		/**
		 * Enumerates device-specific validation layers.
		 * @param device		Physical device handle
		 * @param count			Number of layers
		 * @param extensions	Returned layers (pointer-to-array)
		 * @return Result
		 * @deprecated Since 1.0.13 device-only layers are deprecated and this method <b>must</b> return the layers for the Vulkan implementation
		 * @see Instance.Library#vkEnumerateInstanceLayerProperties(IntByReference, VkLayerProperties)
		 */
		@Deprecated
		int vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntByReference count, VkLayerProperties layers);

		/**
		 * Retrieves supported properties of the given format.
		 * @param device		Physical device handle
		 * @param format		Format
		 * @param props			Returned format properties
		 */
		void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props);
	}
}
