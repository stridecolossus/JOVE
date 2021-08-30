package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.common.Queue.Family;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.util.DeviceFeatures;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;
import org.sarge.lib.util.LazySupplier;

import com.sun.jna.Pointer;

/**
 * A <i>physical device</i> represents a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject, DeviceContext {
	/**
	 * Enumerates the physical devices for the given instance.
	 * @param instance Vulkan instance
	 * @return Physical devices
	 */
	public static Stream<PhysicalDevice> devices(Instance instance) {
		final VulkanFunction<Pointer[]> func = (api, count, devices) -> api.vkEnumeratePhysicalDevices(instance.handle(), count, devices);
		final Pointer[] handles = VulkanFunction.enumerate(func, instance.library(), Pointer[]::new);
		return Arrays.stream(handles).map(ptr -> create(ptr, instance));
	}

	/**
	 * Creates and initialises a physical device with the given handle.
	 * @param handle Device handle
	 * @return New physical device
	 */
	private static PhysicalDevice create(Pointer handle, Instance instance) {
		// Enumerate queue families for this device (for some reason the return type is void)
		final VulkanFunction<VkQueueFamilyProperties> func = (api, count, array) -> {
			api.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array);
			return VulkanLibrary.SUCCESS;
		};
		final VkQueueFamilyProperties[] props = VulkanFunction.enumerate(func, instance.library(), VkQueueFamilyProperties::new);

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
		final Set<VkQueueFlag> flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
		return new Family(index, props.queueCount, flags);
	}

	private final Handle handle;
	private final Instance instance;
	private final List<Family> families;
	private final Supplier<Properties> props = new LazySupplier<>(Properties::new);
	private final Supplier<DeviceFeatures> features = new LazySupplier<>(this::loadFeatures);
	private Optional<Family> present;

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

	@Override
	public VulkanLibrary library() {
		return instance.library();
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

	// TODO
	public static Predicate<PhysicalDevice> filter(Predicate<Family> filter) {
		return dev -> dev.families().stream().anyMatch(filter);
	}

	/**
	 * Helper - Finds the queue family that supports presentation to the given surface.
	 * @param surface Surface handle
	 * @return Queue family that supports presentation
	 * @see Family#isPresentationSupport(DeviceContext, Handle)
	 */
	public Optional<Family> presentation(Handle surface) {
		if(present == null) {
			present = families.stream().filter(f -> f.isPresentationSupport(this, surface)).findAny();
		}
		return present;
	}

	/**
	 * Device properties.
	 */
	public class Properties {
		@SuppressWarnings("hiding")
		private final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		// TODO https://stackoverflow.com/questions/67519579/can-a-jna-structure-support-immutability

		private Properties() {
			final VulkanLibrary lib = PhysicalDevice.this.library();
			lib.vkGetPhysicalDeviceProperties(handle, props);
		}

		/**
		 * @return Device name
		 */
		public String name() {
			return new String(props.deviceName);
		}

		/**
		 * @return Device type
		 */
		public VkPhysicalDeviceType type() {
			return props.deviceType;
		}

		/**
		 * @return Device limits
		 */
		public VkPhysicalDeviceLimits limits() {
			return props.limits;
		}

		@Override
		public String toString() {
			return props.toString();
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
	 * Retrieves the features supported by this device.
	 */
	private DeviceFeatures loadFeatures() {
		final VulkanLibrary lib = this.library();
		final VkPhysicalDeviceFeatures struct = new VkPhysicalDeviceFeatures();
		lib.vkGetPhysicalDeviceFeatures(handle, struct);
		return new DeviceFeatures(struct);
	}

	/**
	 * @return Extensions supported by this device
	 */
	public Set<String> extensions() {
		final VulkanFunction<VkExtensionProperties> func = (api, count, array) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, array);
		return VulkanHelper.extensions(library(), func);
	}

	/**
	 * @return Validation layers supported by this device
	 */
	public Set<ValidationLayer> layers() {
		final VulkanFunction<VkLayerProperties> func = (api, count, array) -> api.vkEnumerateDeviceLayerProperties(handle, count, array);
		return ValidationLayer.enumerate(library(), func);
	}

	/**
	 * Retrieves the supported properties of the given format.
	 * @param format Format
	 * @return Format properties
	 */
	public VkFormatProperties properties(VkFormat format) {
		final var props = new VkFormatProperties();
		library().vkGetPhysicalDeviceFormatProperties(handle, format, props);
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
}
