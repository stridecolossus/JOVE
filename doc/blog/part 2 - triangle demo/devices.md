# Overview

Now we have a Vulkan instance we can use it to enumerate the physical devices available on the local hardware and then find one that satisfies the requirements of the application.

A physical device represents a hardware component that supports Vulkan, such as the GPU in your PC.  Each device also specifies a number of queue families that define the capabilities of work that can be performed by that device (such as rendering, data transfer, etc).

For this we will need:

domain objects for the physical device and its associated queue families
some sort of factory to enumerate the devices for a given instance
accessors to retrieve the properties of a specific device

This will be the first time we will be retrieving arrays of data from Vulkan, specifically:
the array of physical devices handles for the instance
an array of structures specifying the capabilities of the queue families for each device

As we will see there is a common pattern here (and throughout the Vulkan API) that we will attempt to abstract.


# Let's get Physical

The first step is an outline domain class for the physical device itself:

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

And a local class for the queue families:

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
Enumerating the Physical Devices
To enumerate the physical devices we allocate an integer-by-reference from the factory and invoke vkEnumeratePhysicalDevices API method twice:

determine the number of results (the array parameter is null) and allocate the array
populate the array (with the same count variable)
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
Next we implement the private create() factory method to enumerate the queue families for each device using the same approach of invoking the API method twice:

private static PhysicalDevice create(Pointer handle, Instance instance) {
	// Count number of families
	final VulkanLibrary lib = instance.library();
	final IntByReference count = lib.factory().integer();
	lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, null);

	// Retrieve families
	final VkQueueFamilyProperties[] array;
	if(count.getValue() > 0) {
		array = (T[]) new VkQueueFamilyProperties().toArray(count.getValue());
		lib.vkGetPhysicalDeviceQueueFamilyProperties(handle, count, array[0]);
	}
	else {
		array = (T[]) Array.newInstance(VkQueueFamilyProperties.class, 0);
	}
	
	// Create device
	return new PhysicalDevice(handle, instance, families);
}
Notes:

in this case we use the toArray() method on an instance of VkQueueFamilyProperties to allocate the array (this is how JNA works for structure arrays).

we pass the first element of the array to the API method (i.e. our array is equivalent to a native pointer-to-structure)

the vkGetPhysicalDeviceQueueFamilyProperties method does not return a result code for some reason (the only exception we have come across)
Finally we add a local helper method to the device constructor to transform the JNA structure to a QueueFamily domain object and to allocate the family index:

private List<QueueFamily> build(VkQueueFamilyProperties[] families) {
	return IntStream
		.range(0, families.length)
		.mapToObj(n -> new QueueFamily(n, families[n]))
		.collect(toList());
}
Device Properties
The physical device is used to lookup various properties that will be used later in development:

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
These properties could be retrieved up-front when we create the physical device but since they are probably only ever going to be used once then making the methods on-demand feels more logical.  The exception is the memory properties which we may well wrap into some sort of helper later on.
Two-step Invocation
The physical device also exposes the available extensions and validation layers similar to those globally available prior to instantiating the Vulkan instance.  An application could use these to make decisions on which extensions to request depending on which are available, or fail with an error, or just assume they are available (not recommended).  This decision is out-of-scope for our library but we do need to provide some means of querying the available extensions and layers which we glossed over earlier.

Looking at the API and the various structures we can see that roughly the same logic is used in all cases.  This approach of invoking the same method twice is common when retrieving arrays from native libraries and is used throughout Vulkan - we refer to this pattern as two-step invocation.

The obvious starting point is to abstract the commonality when invoking an API method which we address by introducing a functional interface:

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
The interface is generic and can be used to refer (for example) to the API method for the extensions supported by a physical device:

VulkanFunction<VkExtensionProperties> func = (api, count, extensions) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, extensions);
where handle is the physical device handle.

We can then add a generic helper method to this interface that performs the two-step invocation given this function:

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
Which can then be used to enumerate and transform the available extensions:

VulkanFunction<VkExtensionProperties> func = (api, count, extensions) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, extensions);
VkExtensionProperties identity = new VkExtensionProperties();
VkExtensionProperties[] array = VulkanFunction.enumerate(func, lib, identity);
Set<String> extensions = Arrays.stream(array).map(e -> Native.toString(e.extensionName)).collect(toSet());
There are several benefits to this (somewhat complicated) abstraction:

we refer to the API method once (less chance of messing it up)
we have a helper function that avoids code-duplication
we centralise the allocation of the count value and the checks on the API return code
Implementation notes:

VulkanFunction provides two enumerate() methods (arbitrary array and JNA structure array) are there are subtle differences in implementation.

we have to bodge the function for vkGetPhysicalDeviceQueueFamilyProperties to explicitly return SUCCESS
Now we can refactor the create() method in the physical device and implement the available extensions and validation layers in this class and the Vulkan API:

We add the following to the physical device:

public VulkanFunction<VkExtensionProperties> extensions() {
	return (api, count, extensions) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, extensions);
}

public VulkanFunction<VkLayerProperties> layers() {
	return (api, count, layers) -> api.vkEnumerateDeviceLayerProperties(handle, count, layers);
}
and similarly for the globally available extensions and layers in the Vulkan API itself:

VulkanFunction<VkExtensionProperties> EXTENSIONS = (api, count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
VulkanFunction<VkLayerProperties> LAYERS = (api, count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
Finally we create the Support adapter class which is used to take either of these and transform the results appropriately (we also add a helper to the ValidationLayer class).

Summary
In this post we enumerated the physical devices and their associated queue families for a given Vulkan instance.

We introduced the VulkanFunction to simplify API methods that use the two-step invocation approach.

We also implemented the available extensions and validation layers both at the global and device level.

