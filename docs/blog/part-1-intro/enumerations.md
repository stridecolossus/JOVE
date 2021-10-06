---
title: Enumerations
---


### Background

When we first started using the code generated enumerations we realised there were a couple of flaws in our thinking:

1. Many of the Vulkan enumerations are not a set of contiguous values and/or are actually bit-fields (as in the examples above). 
We needed some mechanism to map _from_ a native value to the relevant enumeration constant that worked for _all_ generated enumerations.

2. A native enumeration is implemented as an integer value and was being mapped to `int` in the code generated structures and API methods - this is error prone and not self-documenting.

For a library with a handful of enumerations this would be a minor issue that we could work around but we needed something more practical for the large number of Vulkan enumerations!

### Solution

Although it is not common practice a Java enumeration **can** implement an interface (indeed our IDE will not code-complete an interface on an enumeration presumably because it thinks it is not legal Java).  We leverage this technique to define a sort of base interface for the generated enumerations such that we can implement common helpers to handle the mapping issue.

The [interface](https://github.com/stridecolossus/JOVE/blob/master/src/main/java/org/sarge/jove/common/IntegerEnumeration.java) itself is trivial (already seen in the Velocity template above):

```java
public interface IntegerEnumeration {
    /**
     * @return Enum literal
     */
    int value();
}
```

We add the following static method that maps a native value to the corresponding enumeration constant:

```java
/**
 * Maps an enumeration literal to the corresponding enumeration constant.
 * @param clazz Enumeration class
 * @param value Literal
 * @return Constant
 * @throws IllegalArgumentException if the enumeration does not contain the given value
 */
static <E extends IntegerEnumeration> E map(Class<E> clazz, int value) {
    return Cache.CACHE.get(clazz).get(value);
}
```

The cache generates an _entry_ for each integer enumeration on request:

```java
final class Cache {
    /**
     * Singleton instance.
     */
    private static final Cache CACHE = new Cache();

    /**
     * Cache entry.
     */
    private class Entry {
        /**
         * Looks up the enumeration constant for the given value.
         * @param <E> Enumeration
         * @param value Constant value
         * @return Enumeration constant
         * @throws IllegalArgumentException for an unknown value
         */
        private <E extends IntegerEnumeration> E get(int value) {
            ...
        }
    }

    private final Map<Class<? extends IntegerEnumeration>, Entry> cache = new ConcurrentHashMap<>();

    private Cache() {
    }

    private Entry get(Class<? extends IntegerEnumeration> clazz) {
        return cache.computeIfAbsent(clazz, Entry::new);
    }
}
```

Each entry generates the reverse mapping:

```java
private class Entry {
    private final Map<Integer, ? extends IntegerEnumeration> map;
    private final Object zero;

    private Entry(Class<? extends IntegerEnumeration> clazz) {
        // Build reverse mapping
        final IntegerEnumeration[] array = clazz.getEnumConstants();
        this.map = Arrays.stream(array).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));

        // Determine zero value
        final Object def = map.get(0);
        this.zero = def == null ? array[0] : def;
    }

    private <E extends IntegerEnumeration> E get(int value) {
        final E result = (E) map.get(value);
        if(result == null) {
            throw new IllegalArgumentException(...);
        }
        return result;
    }

    private <E extends IntegerEnumeration> E zero() {
        return (E) zero;
    }
}
```

Notes:

- The _zero_ field is the _default_ constant used to instantiate an integer enumeration field in a JNA structure (since not all enumerations contain a zero value).

- Unfortunately the cache class is publicly visible but cannot be instantiated.

Finally we also add helpers to transform to/from a bit-field mask:

```java
IntBinaryOperator MASK = (a, b) -> a | b;

/**
 * Converts an integer mask to a set of enumeration constants.
 * @param clazz        Enumeration class
 * @param mask        Mask
 * @return Constants
 */
static <E extends IntegerEnumeration> Collection<E> enumerate(Class<E> clazz, int mask) {
    final var entry = Cache.CACHE.get(clazz);
    final List<E> values = new ArrayList<>();
    final int max = Integer.highestOneBit(mask);
    for(int n = 0; n < max; ++n) {
        final int value = 1 << n;
        if((value & mask) == value) {
            values.add(entry.get(value));
        }
    }
    return values;
}

/**
 * Builds an integer mask from the given enumeration constants.
 * @param values Enumeration constants
 * @return Mask
 */
static <E extends IntegerEnumeration> int mask(Collection<E> values) {
    return values.stream().distinct().mapToInt(IntegerEnumeration::value).reduce(0, MASK);
}
```

### Type Converter

For the second problem we employed the JNA _type converter_ mechanism that maps a Java type to/from a native type:

```java
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

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        // Lookup enumeration
        final Class<?> type = context.getTargetType();
        if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new IllegalStateException(...);
        final var entry = Cache.CACHE.get((Class<? extends IntegerEnumeration>) type);

        // Map native value
        final int value = (int) nativeValue;
        if(value == 0) {
            return entry.zero();
        }
        else {
            return entry.get(value);
        }
    }
}
```

Note that `fromNative` handles the case of a zero native value.

The only fly in the ointment is that we need to apply this converter to **every** JNA structure in its constructor, hence we introduce a base-class for all Vulkan structures:

```java
abstract class VulkanStructure extends Structure {
    protected VulkanStructure() {
        super(MAPPER);
    }
}
```

The mapper is also created as a member of the Vulkan API:

```java
public interface VulkanLibrary {
    TypeMapper MAPPER = mapper();
    
    private static TypeMapper mapper() {
        final DefaultTypeMapper mapper = new DefaultTypeMapper();
        mapper.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
        ...
        return mapper;
    }
    
    static VulkanLibrary create() {
        return Native.load(library(), VulkanLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, MAPPER));
    }

    abstract class VulkanStructure extends Structure {
        ...
    }
}
```

Notes:

- The new structure base-class **must** be defined as a member of the API for the mapper to work correctly.

- We use the the same mapper when instantiating the library so that the converter also applies to all API methods.

