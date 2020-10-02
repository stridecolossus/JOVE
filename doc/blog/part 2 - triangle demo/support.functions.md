# Examples

```java
		// Instance
		return (count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
		
		// Device
		(count, ext) -> api.vkEnumerateDeviceExtensionProperties(handle, null, count, ext);
```

# Factor out API call

```java
@FunctionalInterface
public interface VulkanFunction<T> {
	/**
	 * Vulkan API method that retrieves an array of the given type.
	 * @param count Return-by-reference count of the number of array elements
	 * @param array Array instance (<code>null</code> to retrieve just the count)
	 * @return Vulkan result code
	 */
	int enumerate(IntByReference count, T array);
}
```

# Adapter for arrays

```java
	static <T extends Structure> Supplier<T[]> create(VulkanFunction<T> func, IntByReference count, T identity) {
		return () -> {
			// Count number of values
			check(func.enumerate(count, null));

			// Retrieve values
			if(count.getValue() > 0) {
				final T[] array = (T[]) identity.toArray(count.getValue());
				check(func.enumerate(count, array[0]));
				return array;
			}
			else {
				return (T[]) Array.newInstance(identity.getClass(), 0);
			}
		};
	}
```

# Re-factor support methods in instance

```java
	/**
	 * @return Extensions function
	 */
	public VulkanFunction<VkExtensionProperties> extensions() {
		return (count, array) -> api.vkEnumerateInstanceExtensionProperties(null, count, array);
	}

	/**
	 * @return Validation layers function
	 */
	public VulkanFunction<VkLayerProperties> layers() {
		return (count, array) -> api.vkEnumerateInstanceLayerProperties(count, array);
	}
```

# Abstract common code to helper

```java
public abstract class Support<T extends Structure, R> {
	/**
	 * Enumerates and maps a set of supported data.
	 * @param func			Vulkan function
	 * @param count			Counter
	 * @param identity		Identity instance
	 * @return Results
	 */
	protected Set<R> enumerate(VulkanFunction<T> func, IntByReference count, T identity) {
	}

	/**
	 * Transforms a structure to the resultant type.
	 * @param obj Structure
	 * @return Transformed result
	 */
	protected abstract R map(T obj);
```

# Implementation for extensions

```java
	/**
	 * Implementation for supporting extensions.
	 */
	public static class Extensions extends Support<VkExtensionProperties, String> {
		@Override
		public Set<String> enumerate(VulkanFunction<VkExtensionProperties> func) {
			return enumerate(func, new IntByReference(), new VkExtensionProperties());
		}

		@Override
		protected String map(VkExtensionProperties ext) {
			return Native.toString(ext.extensionName);
		}
	}
```

# Easier to test

```java
public class SupportTest {
	private VulkanFunction<VkExtensionProperties> func;
	private IntByReference count;
	private VkExtensionProperties identity;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		count = new IntByReference(1);
		identity = new VkExtensionProperties();
		func = mock(VulkanFunction.class);
	}

	@Test
	void enumerate() {
		// Create support adapter that enumerates extension names
		final Support<VkExtensionProperties, String> support = new Support<>() {
			@Override
			protected String map(VkExtensionProperties obj) {
				return "string";
			}
		};

		// Enumerate extensions and check results
		final Set<String> results = support.enumerate(func, count, identity);
		assertEquals(Set.of("string"), results);
		verify(func).enumerate(count, identity);
	}

	@Test
	void extensions() {
		final Extensions extensions = new Extensions();
		final Set<String> results = extensions.enumerate(func, count, identity);
		assertNotNull(results);
		assertEquals(1, results.size());
		verify(func).enumerate(count, identity);
	}
}
```
