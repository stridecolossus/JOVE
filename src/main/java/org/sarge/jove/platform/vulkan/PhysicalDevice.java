package org.sarge.jove.platform.vulkan;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Predicate;

import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.vulkan.Feature.Supported;
import org.sarge.jove.util.StructureHelper;
import org.sarge.lib.util.AbstractEqualsObject;
import org.sarge.lib.util.AbstractObject;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

/**
 * A <i>physical device</i> is a Vulkan system component such as a GPU.
 * @author Sarge
 */
public class PhysicalDevice extends AbstractObject {
	/**
	 * Queue family.
	 */
	public class QueueFamily extends AbstractEqualsObject {
		private final int count;
		private final Set<VkQueueFlag> flags;

		/**
		 * Constructor.
		 * @param props Properties
		 */
		private QueueFamily(VkQueueFamilyProperties props) {
			this.count = props.queueCount;
			this.flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
		}

		/**
		 * @return Number of queues in this family
		 */
		public int count() {
			return count;
		}

		/**
		 * @return Queue flags
		 */
		public Set<VkQueueFlag> flags() {
			return flags;
		}

		/**
		 * @return Queue index
		 */
		public int index() {
			return PhysicalDevice.this.families().indexOf(this);
		}

		/**
		 * Tests whether this family supports presentation to the given surface.
		 * @param surface Surface
		 * @return Whether presentation is supported
		 */
		public boolean isPresentationSupported(Surface surface) {
			final IntByReference supported = vulkan.factory().integer();
			check(vulkan.library().vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice.this.handle(), index(), surface.handle(), supported));
			return VulkanBoolean.of(supported.getValue()).isTrue();
		}
	}

	/**
	 * Memory selection helper.
	 */
	public static class MemorySelector {
		private final VkPhysicalDeviceMemoryProperties props;

		/**
		 * Constructor.
		 * @param props
		 */
		protected MemorySelector(VkPhysicalDeviceMemoryProperties props) {
			this.props = notNull(props);
		}

		/**
		 * Finds a memory type for the given memory properties.
		 * @param flags Memory properties
		 * @return Memory type index
		 * @throws ServiceException if no suitable memory type is available
		 */
		public int findMemoryType(Set<VkMemoryPropertyFlag> flags) {
			final int mask = IntegerEnumeration.mask(flags);
			for(int n = 0; n < props.memoryTypeCount; ++n) {
				if(props.memoryTypes[n].propertyFlags == mask) {
					return n;
				}
			}
			throw new ServiceException("No memory type available for specified memory properties:" + flags);
		}
	}

	/**
	 * Creates a physical device and retrieves associated data.
	 * @param handle 		Device handle
	 * @param vulkan		Vulkan context
	 * @return Physical device
	 */
	static PhysicalDevice create(Pointer handle, Vulkan vulkan) {
		// Get device properties
		final VulkanLibrarySystem lib = vulkan.library();
		final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		lib.vkGetPhysicalDeviceProperties(handle, props);

		// Get device features
		final VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		lib.vkGetPhysicalDeviceFeatures(handle, features);

		// Get memory properties
		final VkPhysicalDeviceMemoryProperties mem = new VkPhysicalDeviceMemoryProperties();
		lib.vkGetPhysicalDeviceMemoryProperties(handle, mem);

		// Get queue families
		final VulkanFunction<VkQueueFamilyProperties> func = (count, array) -> {
			lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array);
			return VulkanLibrary.SUCCESS;
		};
		final var families = VulkanFunction.enumerate(func, vulkan.factory().integer(), new VkQueueFamilyProperties());

		// Enumerate device-specific extensions and layers
		final VulkanFunction<VkExtensionProperties> extensions = (count, ext) -> lib.vkEnumerateDeviceExtensionProperties(handle, null, count, ext);
		final VulkanFunction<VkLayerProperties> layers = (count, ext) -> lib.vkEnumerateDeviceLayerProperties(handle, count, ext);
		final Supported supported = new Supported(extensions, layers, vulkan.factory());

		// Create device
		return new PhysicalDevice(handle, vulkan, props, mem, features, Arrays.asList(families), supported);
	}

	static List<PhysicalDevice> create(VulkanInstance instance) {
		final var devices = instance.devices();
		final Vulkan vulkan = instance.vulkan();
		return devices.stream().map(handle -> PhysicalDevice.create(handle, vulkan)).collect(toList());
	}

	private final Pointer handle;
	private final Vulkan vulkan;
	private final VkPhysicalDeviceProperties props;
	private final VkPhysicalDeviceMemoryProperties mem;
	private final VkPhysicalDeviceFeatures features;
	private final List<QueueFamily> families;
	private final Supported supported;

	/**
	 * Constructor.
	 * @param handle			Device handle
	 * @param vulkan			Vulkan context
	 * @param props				Device properties
	 * @param mem				Memory properties
	 * @param features			Features
	 * @param families			Queue families
	 * @param supported			Supported device features
	 */
	PhysicalDevice(Pointer handle, Vulkan vulkan, VkPhysicalDeviceProperties props, VkPhysicalDeviceMemoryProperties mem, VkPhysicalDeviceFeatures features, List<VkQueueFamilyProperties> families, Supported supported) {
		this.handle = notNull(handle);
		this.vulkan = notNull(vulkan);
		this.props = notNull(props);
		this.mem = notNull(mem);
		this.features = notNull(features);
		this.families = families.stream().map(QueueFamily::new).collect(toList());
		this.supported = notNull(supported);
	}

	/**
	 * @return Handle
	 */
	Pointer handle() {
		return handle;
	}

	/**
	 * @return Vulkan context
	 */
	Vulkan vulkan() {
		return vulkan;
	}

	/**
	 * @return Type of this device
	 */
	public VkPhysicalDeviceType type() {
		return props.deviceType;
	}

	/**
	 * @return Properties of this device
	 */
	public VkPhysicalDeviceProperties properties() {
		return StructureHelper.copy(props, new VkPhysicalDeviceProperties()); // TODO - nasty
	}

	/**
	 * @return Memory selector for this device
	 */
	public MemorySelector selector() {
		return new MemorySelector(mem);
	}

	/**
	 * @return Queue families provided by this device
	 */
	public List<QueueFamily> families() {
		return families;
	}

	/**
	 * @return Supported extensions and layers
	 */
	public Supported supported() {
		return supported;
	}

	// TODO
	// boolean supports(VkPhysicalDeviceFeatures required)

	/**
	 * Enumerates the required features that this device does <b>not</b> support.
	 * @param required Required features
	 * @return Unsupported feature names
	 */
	public Set<String> enumerateUnsupportedFeatures(VkPhysicalDeviceFeatures required) {
		// Extracts a feature flag from the given structure
		final BiFunction<VkPhysicalDeviceFeatures, Field, VulkanBoolean> getter = (struct, field) -> {
			try {
				return (VulkanBoolean) field.get(struct);
			}
			catch(Exception e) {
				throw new RuntimeException(e);
			}
		};

		// Tests whether a feature is required
		final Predicate<Field> isRequired = field -> {
			final VulkanBoolean bool = getter.apply(required, field);
			if(bool == null) return false;
			return bool.isTrue();
		};

		// Tests whether a required feature is supported
		final Predicate<Field> isSupported = field -> {
			final VulkanBoolean bool = getter.apply(features, field);
			if(bool == null) return false;
			return bool.isTrue();
		};

		// Enumerate unsupported fields
		return StructureHelper.fields(required)
			.filter(isRequired)
			.filter(isSupported.negate())
			.map(Field::getName)
			.collect(toSet());
	}
}
