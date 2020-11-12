package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;

import com.sun.jna.Pointer;

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
		final VkQueueFamilyProperties[] families = VulkanFunction.enumerate(func, instance.library(), VkQueueFamilyProperties::new);

		// Create device
		return new PhysicalDevice(handle, instance, families);
	}

	/**
	 * Helper - Creates a device predicate that matches against the given the queue family filter.
	 * @param predicate Queue family predicate
	 * @return Device predicate
	 */
	public static Predicate<PhysicalDevice> predicate(Predicate<Queue.Family> predicate) {
		return dev -> dev.families.stream().anyMatch(predicate);
	}

	private final Handle handle;
	private final Instance instance;
	private final List<Queue.Family> families;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param instance		Parent instance
	 * @param families		Queue family descriptors
	 */
	PhysicalDevice(Pointer handle, Instance instance, VkQueueFamilyProperties[] families) {
		this.handle = new Handle(handle);
		this.instance = notNull(instance);
		this.families = IntStream.range(0, families.length).mapToObj(n -> family(n, families[n])).collect(toList());
	}

	/**
	 * Creates a queue family.
	 * @param index Family index
	 * @param props Properties
	 * @return New queue family
	 */
	private Queue.Family family(int index, VkQueueFamilyProperties props) {
		final var flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
		return new Queue.Family(this, index, props.queueCount, new HashSet<>(flags));
	}

	/**
	 * @return Device handle
	 */
	@Override
	public Handle handle() {
		return handle;
	}

	/**
	 * @return Parent instance
	 */
	public Instance instance() {
		return instance;
	}

	/**
	 * @return Queue families for this device
	 */
	public List<Queue.Family> families() {
		return families;
	}

	/**
	 * Helper - Finds a matching queue family for this device.
	 * @param test Queue family predicate
	 * @return Matching queue family
	 * @throws NoSuchElementException if a matching queue is not present
	 */
	public Queue.Family family(Predicate<Queue.Family> test) {
		return families.stream().filter(test).findAny().orElseThrow();
	}

	/**
	 * @return Device properties
	 */
	public VkPhysicalDeviceProperties properties() {
		final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		instance.library().vkGetPhysicalDeviceProperties(handle, props);
		return props;
	}

	/**
	 * @return Features supported by this device
	 */
	public VkPhysicalDeviceFeatures features() {
		final VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		instance.library().vkGetPhysicalDeviceFeatures(handle, features);
		return features;
	}

	/**
	 * @return Supported extensions function
	 */
	public VulkanFunction<VkExtensionProperties> extensions() {
		return (api, count, extensions) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, extensions);
	}

	/**
	 * @return Supported validation layers function
	 */
	public VulkanFunction<VkLayerProperties> layers() {
		return (api, count, layers) -> api.vkEnumerateDeviceLayerProperties(handle, count, layers);
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
