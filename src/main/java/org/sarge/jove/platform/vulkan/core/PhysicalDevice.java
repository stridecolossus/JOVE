package org.sarge.jove.platform.vulkan.core;

import static java.util.stream.Collectors.toList;
import static org.sarge.jove.platform.vulkan.api.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiPredicate;
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
import org.sarge.jove.platform.vulkan.util.VulkanBoolean;
import org.sarge.jove.platform.vulkan.util.VulkanFunction;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;
import org.sarge.lib.util.LazySupplier;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.IntByReference;

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

	/**
	 * Tests whether this device supports presentation to the given surface.
	 * @param surface		Rendering surface
	 * @param family		Queue family
	 * @return Whether presentation is supported by the given family
	 */
	public boolean isPresentationSupported(Handle surface, Family family) {
		final VulkanLibrary lib = this.library();
		final IntByReference supported = lib.factory().integer();
		check(lib.vkGetPhysicalDeviceSurfaceSupportKHR(this.handle(), family.index(), surface, supported));
		return VulkanBoolean.of(supported.getValue()).toBoolean();
	}

	/**
	 * A <i>selector</i> is used to select a physical device that supports some required queue capability (such as presentation or rendering).
	 * <p>
	 * Selectors are used in the following steps during initialisation of the devices:
	 * <ol>
	 * <li>as a predicate to test whether a physical device supports the required queue</li>
	 * <li>to retrieve the relevant queue from the logical device</li>
	 * </ol>
	 * <p>
	 * Note that the selected queue {@link #family()} is determined as a side-effect of using the selector as a predicate when choosing the physical device.
	 * <p>
	 * Example to select the presentation queue:
	 * <pre>
	 * 	// Create selector
	 * 	Handle surface = ...
	 * 	Selector selector = Selector.of(surface);
	 *
	 * 	// Select device that supports presentation
	 * 	PhysicalDevice physical = devices.stream().filter(selector).findAny().orElseThrow();
	 *
	 * 	// Retrieve presentation queue
	 * 	LogicalDevice dev = ...
	 * 	Queue presentation = dev.queue(selector.family());
	 * </pre>
	 * <p>
	 */
	public static class Selector implements Predicate<PhysicalDevice> {
		/**
		 * Creates a selector for the queue that supports presentation to the given surface.
		 * @param surface Rendering surface
		 * @return Presentation queue selector
		 */
		public static Selector of(Handle surface) {
			final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> dev.isPresentationSupported(surface, family);
			return new Selector(predicate);
		}

		/**
		 * Creates a selector for the queue that supports the given capabilities.
		 * @param flags Required queue capabilities
		 * @return Queue selector
		 */
		public static Selector of(VkQueueFlag... flags) {
			final var list = Arrays.asList(flags);
			final BiPredicate<PhysicalDevice, Family> predicate = (dev, family) -> family.flags().containsAll(list);
			return new Selector(predicate);
		}

		private final BiPredicate<PhysicalDevice, Family> predicate;
		private Optional<Family> family = Optional.empty();

		/**
		 * Constructor.
		 * @param predicate Queue family predicate
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
			// Build filter for this device
			final Predicate<Family> filter = family -> predicate.test(dev, family);

			// Retrieve matching queue family
			family = dev.families.stream().filter(filter).findAny();

			// Selector passes if the queue is found
			return family.isPresent();
		}
	}

	/**
	 * Device properties.
	 */
	public class Properties {
		private final VkPhysicalDeviceProperties struct = new VkPhysicalDeviceProperties();

		private Properties() {
			final VulkanLibrary lib = PhysicalDevice.this.library();
			lib.vkGetPhysicalDeviceProperties(handle, struct);
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
		 * @return Device limits
		 */
		public VkPhysicalDeviceLimits limits() {
			return struct.limits;
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
