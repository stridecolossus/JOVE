package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.IntegerEnumeration;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>physical device</i> represents a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice {
	/**
	 * Queue family implementation.
	 */
	public class QueueFamily {
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
			this.hash = new HashCodeBuilder().append(index).append(parent()).hashCode();
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
		public boolean isPresentationSupported(Surface surface) {
			final VulkanLibrary lib = instance.library();
			final IntByReference supported = lib.factory().integer();
			check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice.this.handle(), index(), surface.handle(), supported));
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
					this.parent() == that.parent() &&
					this.index() == that.index();
		}

		private PhysicalDevice parent() {
			return PhysicalDevice.this;
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

	private final Pointer handle;
	private final Instance instance;
	private final List<QueueFamily> families;

	/**
	 * Constructor.
	 * @param handle		Device handle
	 * @param instance		Parent instance
	 * @param families		Queue families
	 */
	PhysicalDevice(Pointer handle, Instance instance, VkQueueFamilyProperties[] families) {
		this.handle = notNull(handle);
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
	public Pointer handle() {
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
	 * @return Device properties
	 */
	public VkPhysicalDeviceProperties properties() {
		final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		instance.library().vkGetPhysicalDeviceProperties(handle, props);
		return props;
	}

	/**
	 * @return Memory properties of this device
	 */
	public VkPhysicalDeviceMemoryProperties memory() {
		final VkPhysicalDeviceMemoryProperties mem = new VkPhysicalDeviceMemoryProperties();
		instance.library().vkGetPhysicalDeviceMemoryProperties(handle, mem);
		return mem;
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
}
