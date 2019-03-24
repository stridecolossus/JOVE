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
			final Vulkan vulkan = Vulkan.instance();
			final IntByReference supported = vulkan.factory().integer();
			check(vulkan.library().vkGetPhysicalDeviceSurfaceSupportKHR(PhysicalDevice.this.handle(), index(), surface.handle(), supported));
			return VulkanBoolean.of(supported.getValue()).isTrue();
		}
	}

	/**
	 * Creates a physical device and retrieves associated data.
	 * @param handle 		Device handle
	 * @param instance		Parent instance
	 * @param factory		Reference factory
	 * @return Physical device
	 */
	static PhysicalDevice create(Pointer handle, VulkanInstance instance) {
		// Get device properties
		final Vulkan vulkan = Vulkan.instance();
		final VulkanLibrary lib = vulkan.library();
		final VkPhysicalDeviceProperties props = new VkPhysicalDeviceProperties();
		lib.vkGetPhysicalDeviceProperties(handle, props);

		// Get device features
		final VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		lib.vkGetPhysicalDeviceFeatures(handle, features);

		// Get queue families
		final VulkanFunction<VkQueueFamilyProperties> func = (count, array) -> {
			lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array);
			return VulkanLibrary.SUCCESS;
		};
		final var families = VulkanFunction.enumerate(func, new VkQueueFamilyProperties());

		// Enumerate device-specific extensions and layers
		final VulkanFunction<VkExtensionProperties> extensions = (count, ext) -> lib.vkEnumerateDeviceExtensionProperties(handle, null, count, ext);
		final VulkanFunction<VkLayerProperties> layers = (count, ext) -> lib.vkEnumerateDeviceLayerProperties(handle, count, ext);
		final Supported supported = new Supported(extensions, layers);

		// Create device
		return new PhysicalDevice(handle, instance, props, features, Arrays.asList(families), supported);
	}

	static List<PhysicalDevice> create(VulkanInstance instance) {
		final var devices = instance.devices();
		return devices.stream().map(handle -> PhysicalDevice.create(handle, instance)).collect(toList());
	}

	private final Pointer handle;
	private final VulkanInstance instance;
	private final VkPhysicalDeviceProperties props;
	private final VkPhysicalDeviceFeatures features;
	private final List<QueueFamily> families;
	private final Supported supported;

	/**
	 * Constructor.
	 * @param handle			Device handle
	 * @param lib				Vulkan instance
	 * @param props				Properties
	 * @param features			Features
	 * @param families			Queue families
	 * @param supported			Supported device features
	 */
	PhysicalDevice(Pointer handle, VulkanInstance instance, VkPhysicalDeviceProperties props, VkPhysicalDeviceFeatures features, List<VkQueueFamilyProperties> families, Supported supported) {
		this.handle = notNull(handle);
		this.instance = notNull(instance);
		this.props = notNull(props);
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
	 * @return Vulkan instance
	 */
	VulkanInstance instance() {
		return instance;
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
		return StructureHelper.copy(props, new VkPhysicalDeviceProperties());
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
