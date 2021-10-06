---
title: Enumerations
---

## Background

When we first started using the code generated enumerations we realised there were a couple of flaws in our thinking:

1. Many of the Vulkan enumerations are not a set of contiguous values and/or are actually bit-fields.  What we really needed was some mechanism to map _from_ a native value to the relevant enumeration constant that worked for _all_ generated enumerations.

2. For bit-field enumerations in particular we also require some means of building a bit-field _mask_ from a collection of constants and performing the reverse operation.

3. A native enumeration is implemented as an integer value and was mapped to `int` in the code generated structures and API methods - this is error prone and not self-documenting.

For a library with a handful of enumerations this would be a minor issue that could be worked around, but something more practical was required for the large number of Vulkan enumerations.

## Solution

Although it is not common practice a Java enumeration __can__ implement an interface.  Indeed our IDE will not code-complete an interface on an enumeration presumably because it thinks it is not legal Java.  We leverage this technique to define a sort of base class for the code-generated enumerations such that we can implement helper methods to handle the mapping issues.

The [interface](https://github.com/stridecolossus/JOVE/blob/master/src/main/java/org/sarge/jove/common/IntegerEnumeration.java) itself is trivial:

```java
public interface IntegerEnumeration {
    /**
     * @return Enum literal
     */
    int value();
}
```

We can now add a static helper to the new interface to map a native value to the corresponding enumeration constant:

```java
static <E extends IntegerEnumeration> E map(Class<E> clazz, int value) { ... }
```

And methods to transform a bit-field mask to the enumeration:

```java
static <E extends IntegerEnumeration> Set<E> enumerate(Class<E> clazz, int mask) { ... }
```

Or to build a mask from a collection of constants:

```java
static <E extends IntegerEnumeration> int mask(Collection<E> values) { ... }
```

Note that we probably _could_ have implemented these helpers as `default` methods on the interface, which would have simplified the method signatures and implementation, but that seemed an abuse of the default method mechanic.

## Implementation

To implement the helper methods we need the reverse mapping from the native values to the enumeration constants.

We first implement a cache indexed by the enumeration class itself:

```java
final class Cache {
    /**
     * Singleton instance.
     */
    private static final Cache CACHE = new Cache();

    private final Map<Class<? extends IntegerEnumeration>, Entry> cache = new ConcurrentHashMap<>();
}
```

Each `Entry` in the cache is an instance of a local class containing the reverse mappings for that enumeration:

```java
private static class Entry {
    private final Map<Integer, ? extends IntegerEnumeration> map;
}
```

The mappings are built in the constructor:

```java
private Entry(Class<? extends IntegerEnumeration> clazz) {
    final IntegerEnumeration[] array = clazz.getEnumConstants();
    this.map = Arrays.stream(array).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));
}
```

Which is initialised on-demand by the following accessor in the cache:

```java
private Entry get(Class<? extends IntegerEnumeration> clazz) {
    return cache.computeIfAbsent(clazz, Entry::new);
}
```

The enumeration constant for a given native value can now be looked up from a cache entry:

```java
private <E extends IntegerEnumeration> E get(int value) {
    final E result = (E) map.get(value);
    if(result == null) throw new IllegalArgumentException(...);
    return result;
}
```

The public helper methods in the integer enumeration interface are implemented using the cache, for example to map a native value to the corresponding constant:

```java
static <E extends IntegerEnumeration> E map(Class<E> clazz, int value) {
    return Cache.CACHE.get(clazz).get(value);
}
```

Notes:

* The reverse mapping silently ignores constants with duplicate native values (this should not be a problem).

* The `CACHE` is a singleton instance.

* Unfortunately the cache class is publicly visible but cannot be instantiated or invoked outside of the package.

## Bit-Fields

Building a bit-field mask from a collection of constants is relatively trivial:

```java
static <E extends IntegerEnumeration> int mask(Collection<E> values) {
    return values
        .stream()
        .distinct()
        .mapToInt(IntegerEnumeration::value)
        .reduce(0, (a, b) -> a | b);
}
```

However the reverse operation is slightly more complex:

```java
static <E extends IntegerEnumeration> Set<E> enumerate(Class<E> clazz, int mask) {
    final var entry = Cache.CACHE.get(clazz);
    final Set<E> values = new TreeSet<>();
    final int max = Integer.highestOneBit(mask);
    for(int n = 0; n < max; ++n) {
        final int value = 1 << n;
        if((value & mask) == value) {
            values.add(entry.get(value));
        }
    }
    return values;
}
```

Note the use of a `TreeSet` to ensure the resultant collection is in ascending value order (which simplifies some test cases).

## Type Converter

To refer to integer enumerations by type (rather than an anonymous `int`) we implement a JNA _type converter_ that maps a Java type to/from its native equivalent:

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
        final Class<?> type = context.getTargetType();
        if(!IntegerEnumeration.class.isAssignableFrom(type)) throw new IllegalStateException(...);
        final var entry = Cache.CACHE.get((Class<? extends IntegerEnumeration>) type);
        return entry.get((int) nativeValue);
    }
};
```

The converter is registered with a global JNA _type mapper_ in the Vulkan library:

```java
public interface VulkanLibrary ... {
    TypeMapper MAPPER = mapper();

    private static TypeMapper mapper() {
        final DefaultTypeMapper mapper = new DefaultTypeMapper();
        mapper.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
        ...
        return mapper;
    }
}
```

The JNA library is configured with this type mapper at instantiation-time so that the new enumerations can be used in Vulkan API methods:

```java
static VulkanLibrary create() {
    ...
    return Native.load(name, VulkanLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, MAPPER));
}
```

The only fly in the ointment is that this mapper also needs to be applied to __every__ JNA structure in its constructor.

We introduce an intermediate base-class for Vulkan structures:

```java
abstract class VulkanStructure extends Structure {
    protected VulkanStructure() {
        super(MAPPER);
    }
}
```

Note that this new base-class __must__ be defined as a member of the JNA library for the mapper to work correctly.

Finally we modify the structure template accordingly and re-generate the code.

## Default Value

The final complication when mapping from a native enumeration value is that a default or unspecified value (i.e. zero) may not be a valid enumeration constant.

We introduce a _zero_ value to the cache entry which is initialised in the constructor:

```java
private static class Entry {
    private final Object zero;

    private Entry(Class<? extends IntegerEnumeration> clazz) {
        ...
        
        // Determine zero value
        final Object def = map.get(0);
        this.zero = def == null ? array[0] : def;
    }
}
```

This default value is mapped from the zero enumeration constant if present or arbitrarily selected as the first constant.

In the type converter we can now safely handle invalid or unspecified native values:

```java
public Object fromNative(Object nativeValue, FromNativeContext context) {
    // Lookup enumeration
    final var entry = ...

    // Map native value
    final int value = (int) nativeValue;
    if(value == 0) {
        return entry.zero();
    }
    else {
        return entry.get(value);
    }
}
```
