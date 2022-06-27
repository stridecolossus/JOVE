package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.util.*;
import org.sarge.jove.util.IntegerEnumeration;
import org.sarge.lib.util.LazySupplier;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>physical device</i> represents a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject {
	/**
	 * Enumerates the physical devices for the given instance.
	 * @param instance Vulkan instance
	 * @return Physical devices
	 */
	public static Stream<PhysicalDevice> devices(Instance instance) {
		final VulkanFunction<Pointer[]> func = (count, devices) -> instance.library().vkEnumeratePhysicalDevices(instance, count, devices);
		final IntByReference count = instance.factory().integer();
		final Pointer[] handles = VulkanFunction.invoke(func, count, Pointer[]::new);
		return Arrays.stream(handles).map(ptr -> create(ptr, instance));
	}

	/**
	 * Creates and initialises a physical device with the given handle.
	 * @param handle Device handle
	 * @return New physical device
	 */
	private static PhysicalDevice create(Pointer handle, Instance instance) {
		// Enumerate queue families for this device (for some reason the return type is void)
		final VulkanFunction<VkQueueFamilyProperties> func = (count, array) -> {
			instance.library().vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array);
			return VulkanLibrary.SUCCESS;
		};
		final IntByReference count = instance.factory().integer();
		final VkQueueFamilyProperties[] props = VulkanFunction.invoke(func, count, VkQueueFamilyProperties::new);

		// Create queue families
		final List<Family> families = IntStream
				.range(0, props.length)
				.mapToObj(n -> family(n, props[n]))
				.collect(toList());

		// Create device
		return new PhysicalDevice(handle, instance, families);
	}

	/**
	 * Creates a queue family.
	 * @param index 	Family index
	 * @param props		Queue properties
	 * @return New queue family
	 */
	private static Family family(int index, VkQueueFamilyProperties props) {
		final Set<VkQueueFlag> flags = IntegerEnumeration.mapping(VkQueueFlag.class).enumerate(props.queueFlags);
		return new Family(index, props.queueCount, flags);
	}

	/**
	 * Helper - Creates a device predicate for the given required features.
	 * @param features Required features
	 * @return New device predicate
	 */
	public static Predicate<PhysicalDevice> predicate(DeviceFeatures features) {
		return dev -> dev.features().contains(features);
	}

	private final Handle handle;
	private final Instance instance;
	private final List<Family> families;
	private final Supplier<Properties> props = new LazySupplier<>(Properties::new);
	private final Supplier<DeviceFeatures> features = new LazySupplier<>(this::loadFeatures);

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param instance		Parent instance
	 * @param families		Queue family descriptors
	 */
	PhysicalDevice(Pointer handle, Instance instance, List<Family> families) {
		this.handle = new Handle(handle);
		this.instance = notNull(instance);
		this.families = List.copyOf(families);
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
	 * Tests whether this device supports presentation to the given surface.
	 * @param surface		Rendering surface
	 * @param family		Queue family
	 * @return Whether presentation is supported by the given family
	 * @see Selector#of(Surface)
	 */
	public boolean isPresentationSupported(Handle surface, Family family) {
		final VulkanLibrary lib = instance.library();
		final IntByReference supported = instance.factory().integer();
		check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(this, family.index(), surface, supported));
		return VulkanBoolean.of(supported.getValue()).isTrue();
	}

	/**
	 * A <i>selector</i> is used to select a physical device that supports some required capability (such as presentation or rendering).
	 * <p>
	 * Selectors are used in the following steps during initialisation of the devices:
	 * <ol>
	 * <li>as a predicate to test whether a physical device supports the required queue</li>
	 * <li>to retrieve the relevant queue from the logical device</li>
	 * </ol>
	 * <p>
	 * Notes:
	 * <ul>
	 * <li>The selected queue {@link #family()} is determined as a side-effect when choosing the physical device</li>
	 * <li>The same queue may be returned by different selectors depending on the hardware</li>
	 * </ul>
	 * <p>
	 * Example to select a device that supports rendering:
	 * <pre>
	 * // Create selector for a device that supports presentation
	 * Surface surface = ...
	 * Selector presentation = Selector.of(surface);
	 *
	 * // Create selector for a device with a graphics work queue
	 * Selector graphics = Selector.of(Set.of(VkQueueFlag.GRAPHICS));
	 *
	 * // Select device
	 * PhysicalDevice physical = devices
	 *     .stream()
	 *     .filter(graphics)
	 *     .filter(presentation)
	 *     .findAny()
	 *     .orElseThrow();
	 *
	 * // Retrieve queues
	 * LogicalDevice dev = ...
	 * Queue presentationQueue = dev.queue(presentation.family());
	 * Queue graphicsQueue = dev.queue(graphics.family());
	 * </pre>
	 */
	public static class Selector implements Predicate<PhysicalDevice> {
		/**
		 * Creates a selector for a device that supports presentation to the given surface.
		 * @param surface Rendering surface
		 * @return Presentation selector
		 * @see PhysicalDevice#isPresentationSupported(Surface, Family)
		 */
		public static Selector surface(Handle surface) {
			final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> dev.isPresentationSupported(surface, family);
			return new Selector(predicate);
		}

		/**
		 * Creates a selector for a device that provides a work queue supporting the given flags.
		 * @param flags Required queue flags
		 * @return Queue selector
		 */
		public static Selector of(Set<VkQueueFlag> flags) {
			final var copy = Set.copyOf(flags);
			final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> family.flags().containsAll(copy);
			return new Selector(predicate);
		}

		private final BiPredicate<PhysicalDevice, Family> predicate;
		private Optional<Family> family = Optional.empty();

		/**
		 * Constructor.
		 * @param predicate
		 */
		public Selector(BiPredicate<PhysicalDevice, Family> predicate) {
			this.predicate = notNull(predicate);
		}

		/**
		 * @return Selected queue family
		 * @throws NoSuchElementException if no queue was selected
		 */
		public Family family() {
			return family.orElseThrow();
		}

		@Override
		public boolean test(PhysicalDevice dev) {
			family = dev.families.stream().filter(f -> predicate.test(dev, f)).findAny();
			return family.isPresent();
		}
	}

	/**
	 * Device properties.
	 */
	public class Properties {
		private final VkPhysicalDeviceProperties struct = new VkPhysicalDeviceProperties();

		private Properties() {
			final VulkanLibrary lib = instance.library();
			lib.vkGetPhysicalDeviceProperties(PhysicalDevice.this, struct);
		}

		/**
		 * @return Device name
		 */
		public String name() {
			return new String(struct.deviceName);
		}

		/**
		 * @return Device type
		 */
		public VkPhysicalDeviceType type() {
			return struct.deviceType;
		}

		/**
		 * The <i>pipeline cache ID</i> is a universally unique identifier for this device.
		 * @return Pipeline cache ID
		 */
		public String cache() {
			return new String(struct.pipelineCacheUUID);
		}

		/**
		 * Note that the returned descriptor is mutable, this method clones a new instance on <b>every</b> invocation.
		 * @return Device limits
		 */
		public VkPhysicalDeviceLimits limits() {
			return struct.limits.copy();
		}
	}

	/**
	 * @return Device properties
	 */
	public Properties properties() {
		return props.get();
	}

	/**
	 * @return Features supported by this device
	 */
	public DeviceFeatures features() {
		return features.get();
	}

	/**
	 * @return Features supported by this device
	 */
	private DeviceFeatures loadFeatures() {
		final var struct = new VkPhysicalDeviceFeatures();
		instance.library().vkGetPhysicalDeviceFeatures(this, struct);
		return DeviceFeatures.of(struct);
	}

	/**
	 * @return Extensions supported by this device
	 */
	public Set<String> extensions() {
		final VulkanLibrary lib = instance.library();
		final VulkanFunction<VkExtensionProperties> func = (count, array) -> lib.vkEnumerateDeviceExtensionProperties(this, null, count, array);
		final IntByReference count = instance.factory().integer();
		return Extension.extensions(lib, count, func);
	}

	/**
	 * @return Validation layers supported by this device
	 */
	public Set<ValidationLayer> layers() {
		final VulkanLibrary lib = instance.library();
		final VulkanFunction<VkLayerProperties> func = (count, array) -> lib.vkEnumerateDeviceLayerProperties(this, count, array);
		final IntByReference count = instance.factory().integer();
		return ValidationLayer.layers(lib, count, func);
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
	public String toString() {
		return new ToStringBuilder(this)
				.append("handle", handle)
				.append("instance", instance)
				.append("families", families.size())
				.build();
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
		 * @param props			Properties
		 */
		void vkGetPhysicalDeviceProperties(PhysicalDevice device, VkPhysicalDeviceProperties props);

		/**
		 * Retrieves the memory properties of the given physical device.
		 * @param device				Device
		 * @param pMemoryProperties		Returned memory properties
		 */
		void vkGetPhysicalDeviceMemoryProperties(PhysicalDevice device, VkPhysicalDeviceMemoryProperties pMemoryProperties);

		/**
		 * Retrieves the features of the given physical device.
		 * @param device		Device handle
		 * @param features		Features
		 */
		void vkGetPhysicalDeviceFeatures(PhysicalDevice device, VkPhysicalDeviceFeatures features);

		/**
		 * Enumerates the queue families of a device.
		 * @param device		Device handle
		 * @param count			Number of devices
		 * @param props			Queue family properties
		 */
		void vkGetPhysicalDeviceQueueFamilyProperties(Pointer device, IntByReference count, VkQueueFamilyProperties props);

		/**
		 * Enumerates device-specific extension properties.
		 * @param device		Physical device handle
		 * @param layer			Layer name or <tt>null</tt> for all
		 * @param count			Number of extensions
		 * @param extensions	Returned extensions
		 * @return Result
		 */
		int vkEnumerateDeviceExtensionProperties(PhysicalDevice device, String layer, IntByReference count, VkExtensionProperties extensions);

		/**
		 * Enumerates device-specific validation layers.
		 * @param device		Physical device handle
		 * @param count			Number of layers
		 * @param extensions	Returned layers
		 * @return Result
		 */
		int vkEnumerateDeviceLayerProperties(PhysicalDevice device, IntByReference count, VkLayerProperties layers);

		/**
		 * Retrieves supported properties of the given format.
		 * @param device		Physical device handle
		 * @param format		Format
		 * @param props			Format properties
		 */
		void vkGetPhysicalDeviceFormatProperties(PhysicalDevice device, VkFormat format, VkFormatProperties props);
	}
}
