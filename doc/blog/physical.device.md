# Class outline

```java
public class PhysicalDevice {
	public static Stream<PhysicalDevice> devices(Instance instance) {
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
}
```
# Queue Family

```java
public class QueueFamily {
	private final int count;
	private final int index;
	private final Set<VkQueueFlag> flags;

	/**
	 * Constructor.
	 * @param index		Family index
	 * @param props 	Properties
	 */
	private QueueFamily(int index, VkQueueFamilyProperties props) {
		this.count = props.queueCount;
		this.index = index;
		this.flags = IntegerEnumeration.enumerate(VkQueueFlag.class, props.queueFlags);
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
}
```

# Accessors

```java
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
}
```

# Enumerating the physical devices

```java
public static Stream<PhysicalDevice> devices(Instance instance) {
	// Determine array length
	final IntByReference count = lib.factory().integer();
	check(api.vkEnumeratePhysicalDevices(instance.handle(), count, null));

	// Allocate array
	final Pointer[] array = new Pointer[count.getValue()];

	// Retrieve array
	if(array.length > 0) {
		check(api.vkEnumeratePhysicalDevices(instance.handle(), count, array));
	}

	// Create devices
	return Arrays.stream(array).map(ptr -> create(ptr, instance));
}
```

# Retrieving the Queue Families

```java
private static PhysicalDevice create(Pointer handle, Instance instance) {
	// Count number of families
	final VulkanLibrary lib = instance.library();
	final IntByReference count = lib.factory().integer();
	lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, null);

	// Retrieve families
	final VkQueueFamilyProperties[] array;
	if(count.getValue() > 0) {
		array = (T[]) new VkQueueFamilyProperties().toArray(count.getValue());
		check(lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array[0]));
	}
	else {
		array = (T[]) Array.newInstance(VkQueueFamilyProperties.class, 0);
	}
	
	// Create device
	return new PhysicalDevice(handle, instance, families);
}
```

# Constructor

```java
	private List<QueueFamily> build(VkQueueFamilyProperties[] families) {
		return IntStream
				.range(0, families.length)
				.mapToObj(n -> new QueueFamily(n, families[n]))
				.collect(toList());
	}
```

# Vulkan Function

```java
@FunctionalInterface
public interface VulkanFunction<T> {
	/**
	 * Vulkan API method that retrieves an array of the given type.
	 * @param lib		Vulkan library
	 * @param count 	Return-by-reference count of the number of array elements
	 * @param array 	Array instance or <code>null</code> to retrieve size of the array
	 * @return Vulkan result code
	 */
	int enumerate(VulkanLibrary lib, IntByReference count, T array);
}
```

# Enumerate Array

```java
static <T> T[] enumerate(VulkanFunction<T[]> func, VulkanLibrary lib, IntFunction<T[]> factory) {
	// Determine array length
	final IntByReference count = lib.factory().integer();
	check(func.enumerate(lib, count, null));

	// Allocate array
	final T[] array = factory.apply(count.getValue());

	// Retrieve array
	if(array.length > 0) {
		check(func.enumerate(lib, count, array));
	}

	return array;
}
```

# Enumerate Structure

```java
static <T extends Structure> T[] enumerate(VulkanFunction<T> func, VulkanLibrary lib, T identity) {
	// Count number of values
	final IntByReference count = lib.factory().integer();
	check(func.enumerate(lib, count, null));

	// Retrieve values
	if(count.getValue() > 0) {
		final T[] array = (T[]) identity.toArray(count.getValue());
		check(func.enumerate(lib, count, array[0]));
		return array;
	}
	else {
		return (T[]) Array.newInstance(identity.getClass(), 0);
	}
}
```

# Support for device

```java
public VulkanFunction<VkExtensionProperties> extensions() {
	return (api, count, extensions) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, extensions);
}

public VulkanFunction<VkLayerProperties> layers() {
	return (api, count, layers) -> api.vkEnumerateDeviceLayerProperties(handle, count, layers);
}
```

# Support for instance

```java
VulkanFunction<VkExtensionProperties> EXTENSIONS = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
VulkanFunction<VkLayerProperties> LAYERS = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
```

# Support Helper

```java
public abstract class Support<T extends Structure, R> {
	/**
	 * Retrieves a set of supporting features.
	 * @param lib			Vulkan library
	 * @param func			Enumeration function
	 * @return Results
	 */
	public Set<R> enumerate(VulkanLibrary lib, VulkanFunction<T> func) {
		final T[] array = VulkanFunction.enumerate(func, lib, identity());
		return Arrays.stream(array).map(this::map).collect(toSet());
	}

	/**
	 * Factory for the identity structure.
	 * @return New identity structure
	 */
	protected abstract T identity();

	/**
	 * Converts a retrieved structure to the resultant type.
	 * @param struct Structure
	 * @return Converted result
	 */
	protected abstract R map(T struct);

	/**
	 * Implementation for supported extensions.
	 */
	public static class Extensions extends Support<VkExtensionProperties, String> {
		@Override
		protected VkExtensionProperties identity() {
			return new VkExtensionProperties();
		}

		@Override
		protected String map(VkExtensionProperties struct) {
			return Native.toString(struct.extensionName);
		}
	}
}
```
