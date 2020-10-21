package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.jove.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.IntegerEnumeration;
import org.sarge.jove.common.NativeObject;
import org.sarge.jove.platform.vulkan.VkExtensionProperties;
import org.sarge.jove.platform.vulkan.VkLayerProperties;
import org.sarge.jove.platform.vulkan.VkMemoryPropertyFlag;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceFeatures;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceMemoryProperties;
import org.sarge.jove.platform.vulkan.VkPhysicalDeviceProperties;
import org.sarge.jove.platform.vulkan.VkQueueFamilyProperties;
import org.sarge.jove.platform.vulkan.VkQueueFlag;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.util.Check;
import org.sarge.jove.util.MathsUtil;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>physical device</i> represents a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice implements NativeObject {
	/**
	 * Queue family implementation.
	 */
	public class QueueFamily {
		/**
		 * Index for the <i>ignored</i> queue family.
		 */
		public static final int IGNORED = (~0);

		private final int count;
		private final int index;
		private final Set<VkQueueFlag> flags;
		private final transient int hash;

		/**
		 * Constructor.
		 * @param index		Family index
		 * @param props 	Properties
		 */
		private QueueFamily(int index, VkQueueFamilyProperties props) {
			this.count = props.queueCount;
			this.index = index;
			this.flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
			this.hash = new HashCodeBuilder().append(index).append(device()).hashCode();
		}

		/**
		 * @return Physical device of this queue family
		 */
		PhysicalDevice device() {
			return PhysicalDevice.this;
		}

		/**
		 * @return Number of queues in this family
		 */
		public int count() {
			return count;
		}

		/**
		 * @return Queue family index
		 */
		public int index() {
			return index;
		}

		/**
		 * @return Flags for this family
		 */
		public Set<VkQueueFlag> flags() {
			return flags;
		}

		/**
		 * @param surface Rendering surface
		 * @return Whether this family supports presentation to the given surface
		 */
		public boolean isPresentationSupported(Handle surface) {
			final VulkanLibrary lib = instance.library();
			final IntByReference supported = lib.factory().integer();
			check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(handle, index(), surface, supported));
			return VulkanBoolean.of(supported.getValue()).isTrue();
		}

		@Override
		public int hashCode() {
			return hash;
		}

		@Override
		public boolean equals(Object obj) {
			return
					obj instanceof QueueFamily that &&
					this.device() == that.device() &&
					this.index() == that.index();
		}

		@Override
		public String toString() {
			return ToStringBuilder.reflectionToString(this);
		}
	}

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
		final VkQueueFamilyProperties[] families = VulkanFunction.enumerate(func, instance.library(), new VkQueueFamilyProperties());

		// Create device
		return new PhysicalDevice(handle, instance, families);
	}

	/**
	 * Helper - Creates a predicate for a queue family matching the given flag.
	 * @param flags Queue family flag(s)
	 * @return Predicate
	 */
	public static Predicate<QueueFamily> predicate(VkQueueFlag... flags) {
		Check.notNull(flags);
		return family -> family.flags.containsAll(Arrays.asList(flags));
	}

	/**
	 * Helper - Creates a device predicate that matches against the given the queue family filter.
	 * @param predicate Queue family predicate
	 * @return Device predicate
	 */
	public static Predicate<PhysicalDevice> predicate(Predicate<QueueFamily> predicate) {
		return dev -> dev.families.stream().anyMatch(predicate);
	}

	/**
	 * Helper - Creates a device predicate for a device that supports presentation to the given surface.
	 * @param surface Surface handle
	 * @return Device predicate
	 */
	public static Predicate<PhysicalDevice> predicatePresentationSupported(Handle surface) {
		return predicate(family -> family.isPresentationSupported(surface));
	}

	private final Handle handle;
	private final Instance instance;
	private final List<QueueFamily> families;

	private VkPhysicalDeviceMemoryProperties mem;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param instance		Parent instance
	 * @param families		Queue families
	 */
	PhysicalDevice(Pointer handle, Instance instance, VkQueueFamilyProperties[] families) {
		this.handle = new Handle(handle);
		this.instance = notNull(instance);
		this.families = List.copyOf(build(families));
	}

	/**
	 * @return List of queue families from the given structure array
	 */
	private List<QueueFamily> build(VkQueueFamilyProperties[] families) {
		return IntStream
				.range(0, families.length)
				.mapToObj(n -> new QueueFamily(n, families[n]))
				.collect(toList());
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
	public List<QueueFamily> families() {
		return families;
	}

	/**
	 * Helper - Finds a matching queue family for this device.
	 * @param test			Queue family predicate
	 * @param message		Error message
	 * @return Matching queue family
	 * @throws ServiceException with the given message if a matching queue is not present
	 */
	public QueueFamily find(Predicate<QueueFamily> test, String message) {
		return families.stream().filter(test).findAny().orElseThrow(() -> new RuntimeException(message));
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
	 * @return Memory properties of this device
	 */
	public VkPhysicalDeviceMemoryProperties memory() {
		if(mem == null) {
			mem = new VkPhysicalDeviceMemoryProperties();
			instance.library().vkGetPhysicalDeviceMemoryProperties(handle, mem);
		}
		return mem;
	}

	/**
	 * Finds a memory type for the given memory properties.
	 * @param props Memory properties
	 * @return Memory type index
	 * @throws ServiceException if no suitable memory type is available
	 */
	public int findMemoryType(int filter, Set<VkMemoryPropertyFlag> props) {
		// Retrieve memory properties
		final var mem = this.memory();

		// Find matching memory type index
		final int mask = IntegerEnumeration.mask(props);
		for(int n = 0; n < mem.memoryTypeCount; ++n) {
			if(MathsUtil.isBit(filter, n) && MathsUtil.isMask(mem.memoryTypes[n].propertyFlags, mask)) {
				return n;
			}
		}

		// Otherwise memory not available for this device
		throw new RuntimeException("No memory type available for specified memory properties:" + props);
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
