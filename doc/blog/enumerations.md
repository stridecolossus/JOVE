# Example

```C
typedef enum VkImageUsageFlagBits {
    VK_IMAGE_USAGE_TRANSFER_SRC_BIT = 0x00000001,
    VK_IMAGE_USAGE_TRANSFER_DST_BIT = 0x00000002,
    VK_IMAGE_USAGE_SAMPLED_BIT = 0x00000004,
    VK_IMAGE_USAGE_STORAGE_BIT = 0x00000008,
    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT = 0x00000010,
    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT = 0x00000020,
    VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT = 0x00000040,
    VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT = 0x00000080,
    VK_IMAGE_USAGE_SHADING_RATE_IMAGE_BIT_NV = 0x00000100,
    VK_IMAGE_USAGE_FRAGMENT_DENSITY_MAP_BIT_EXT = 0x00000200,
    VK_IMAGE_USAGE_FLAG_BITS_MAX_ENUM = 0x7FFFFFFF
} VkImageUsageFlagBits;

typedef VkFlags VkImageUsageFlags;
```

# Interface outline

```java
public interface IntegerEnumeration {
	/**
	 * @return Enum literal
	 */
	int value();

	/**
	 * Maps an enumeration literal to the corresponding enumeration constant.
	 * @param clazz Enumeration class
	 * @param value Literal
	 * @return Constant
	 * @throws IllegalArgumentException if the enumeration does not contain the given value
	 */
	static <E extends IntegerEnumeration> E map(Class<E> clazz, int value) {
	}

	/**
	 * Tests whether an integer mask contains the given enumeration value.
	 * @param mask			Mask
	 * @param constant		Enumeration constant
	 * @return Whether is present
	 */
	static <E extends IntegerEnumeration> boolean contains(int mask, E constant) {
	}

	/**
	 * Converts an integer mask to a set of enumeration constants.
	 * @param clazz		Enumeration class
	 * @param mask		Mask
	 * @return Constants
	 */
	static <E extends IntegerEnumeration> Set<E> enumerate(Class<E> clazz, int mask) {
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Mask
	 * @see #MASK
	 */
	static <E extends IntegerEnumeration> int mask(Collection<E> values) {
	}

	/**
	 * Builds an integer mask from the given enumeration constants.
	 * @param values Enumeration constants
	 * @return Mask
	 */
	static <E extends IntegerEnumeration> int mask(E... values) {
	}
```

# Enumeration

```java
public enum VkImageUsageFlag implements IntegerEnumeration {
 	VK_IMAGE_USAGE_TRANSFER_SRC_BIT(1), 	
 	VK_IMAGE_USAGE_TRANSFER_DST_BIT(2), 	
 	VK_IMAGE_USAGE_SAMPLED_BIT(4), 	
 	VK_IMAGE_USAGE_STORAGE_BIT(8), 	
 	VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT(16), 	
 	VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT(32), 	
 	VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT(64), 	
 	VK_IMAGE_USAGE_INPUT_ATTACHMENT_BIT(128), 	
 	VK_IMAGE_USAGE_SHADING_RATE_IMAGE_BIT_NV(256), 	
 	VK_IMAGE_USAGE_FRAGMENT_DENSITY_MAP_BIT_EXT(512), 	
 	VK_IMAGE_USAGE_FLAG_BITS_MAX_ENUM(2147483647); 	

	private final int value;
	
	private VkImageUsageFlag(int value) {
		this.value = value;
	}

	@Override
	public int value() {
		return value;
	}
}
```

# Converter

```java
	/**
	 * Converts an integer enumeration to/from a native <code>int</code> value.
	 * @see <a href="http://technofovea.com/blog/archives/815">enumeration type converter example</a>
	 */
	TypeConverter CONVERTER = new TypeConverter() {
		@Override
		public Class<?> nativeType() {
			return Integer.class;
		}

		@Override
		public Object toNative(Object value, ToNativeContext context) {
			if(value == null) {
				return 0;
			}
			else {
				final IntegerEnumeration e = (IntegerEnumeration) value;
				return e.value();
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public Object fromNative(Object nativeValue, FromNativeContext context) {
			final Class<?> type = context.getTargetType();
			if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new IllegalStateException("Invalid native enumeration class: " + type.getSimpleName());
			final Class<? extends IntegerEnumeration> clazz = (Class<? extends IntegerEnumeration>) type;
			return Cache.CACHE.get(clazz).get((int) nativeValue);
		}
	};
```

# Enumerate

```java
	static <E extends IntegerEnumeration> Set<E> enumerate(Class<E> clazz, int mask) {
		final Cache.Entry entry = Cache.CACHE.get(clazz);
		final Set<E> set = new HashSet<>();
		final int max = Integer.highestOneBit(mask);
		for(int n = 0; n < max; ++n) {
			final int value = 1 << n;
			if((value & mask) == value) {
				set.add(entry.get(value));
			}
		}
		return set;
	}
```

# Cache

```java
	final class Cache {
		private static final Cache CACHE = new Cache();

		/**
		 * Cache entry.
		 */
		private class Entry {
			private final Class<? extends IntegerEnumeration> clazz;
			private final Map<Integer, IntegerEnumeration> map;

			/**
			 * Constructor.
			 * @param clazz Enumeration class
			 */
			private Entry(Class<? extends IntegerEnumeration> clazz) {
				this.clazz = notNull(clazz);
				this.map = Arrays.stream(clazz.getEnumConstants()).collect(toMap(IntegerEnumeration::value, Function.identity()));
			}

			/**
			 * Looks up an integer enumeration constant by value.
			 * @param value Value
			 * @return Enumeration constant
			 */
			@SuppressWarnings("unchecked")
			private <E extends IntegerEnumeration> E get(int value) {
				final E result = (E) map.get(value);
				if(result == null) throw new IllegalArgumentException(String.format("Unknown enumeration value: enum=%s value=%d", clazz.getSimpleName(), value));
				return result;
			}
		}

		private final Map<Class<? extends IntegerEnumeration>, Entry> cache = new ConcurrentHashMap<>();

		private Cache() {
		}

		/**
		 * Looks up or creates a cache entry for the given enumeration class.
		 * @param clazz Enumeration class
		 * @return Cache entry
		 */
		private Entry get(Class<? extends IntegerEnumeration> clazz) {
			return cache.computeIfAbsent(clazz, Entry::new);
		}
	}
```

# Base-class

```java
interface VulkanLibrary {
	abstract class VulkanStructure extends Structure {
		private static final DefaultTypeMapper MAPPER = new DefaultTypeMapper();

		static {
			MAPPER.addTypeConverter(VulkanBoolean.class, VulkanBoolean.CONVERTER);
			MAPPER.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
		}

		protected VulkanStructure() {
			super(MAPPER);
		}
	}
```

# Structure

```java
public class VkSwapchainCreateInfoKHR extends VulkanStructure {
	...
	public VkImageUsageFlag imageUsage;
```

# Method

```java
void vkCmdBindPipeline(Pointer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pointer pipeline);
```
