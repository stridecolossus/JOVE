---
title: Enumerations
---

---

## Contents

- [Background](#background)
- [Reverse Mapping](#reverse-mapping)
- [Type Conversion](#type-conversion)
- [Default Values](#default-values)

---

## Background

When we first started using the code generated enumerations we realised there were a couple of flaws in our thinking:

1. Many of the Vulkan enumerations are not a set of contiguous values and/or are actually bit-fields.  What we really needed was some mechanism to map _from_ a native value to the relevant enumeration constant that worked for __all__ generated enumerations.

2. For bit-field enumerations in particular we also require some means of building a bit-field _mask_ from a collection of constants and performing the reverse operation.

3. A native enumeration is implemented as an integer value and was mapped to `int` in the code generated structures and API methods - this is error prone and not self-documenting.

For a library with a handful of enumerations this would be a minor issue that could be worked around, but something more practical was required for the large number of Vulkan enumerations.

Although it is not common practice a Java enumeration __can__ implement an interface.  Indeed our IDE will not code-complete an interface on an enumeration presumably because it thinks it is not legal Java.  We leverage this technique to define a sort of base class for the code-generated enumerations such that we can implement helper methods to handle the mapping issues.

The [interface](https://github.com/stridecolossus/JOVE/blob/master/src/main/java/org/sarge/jove/util/IntegerEnumeration.java) itself is trivial:

```java
public interface IntegerEnumeration {
    /**
     * @return Enum literal
     */
    int value();
}
```

The following helper can now be implemented to build a bit-field mask from an arbitrary integer enumeration:

```java
static <E extends IntegerEnumeration> int mask(Collection<E> values) {
    return values
        .stream()
        .mapToInt(IntegerEnumeration::value)
        .sum();
}
```

## Reverse Mapping

For the other requirements we implement the _reverse mapping_ of the enumeration:

```java
final class ReverseMapping<E extends IntegerEnumeration> {
    private final Map<Integer, E> map;

    private ReverseMapping(Class<E> clazz) {
        E[] array = clazz.getEnumConstants();
        this.map = Arrays.stream(array).collect(toMap(IntegerEnumeration::value, Function.identity(), (a, b) -> a));
    }
}
```

Note that the reverse mapping silently ignores constants with duplicate native values (which should not be a problem).

The enumeration constant for a given native value can now be looked up from the reverse mapping:

```java
public E map(int value) {
    E constant = map.get(value);
    if(constant == null) throw new IllegalArgumentException(...);
    return constant;
}
```

Transforming a bit-field mask to the corresponding enumeration constants is slightly more involved:

```java
public TreeSet<E> enumerate(int mask) {
    return IntStream
        .range(0, Integer.highestOneBit(mask))
        .map(bit -> 1 << bit)
        .filter(value -> (value & mask) == value)
        .mapToObj(this::map)
        .collect(Collectors.toCollection(TreeSet::new));
}
```

Note the use of a `TreeSet` to ensure the resultant collection is in ascending order (which simplifies some test cases).

We add a factory method to the interface to retrieve the reverse mapping for a given enumeration:

```java
static <E extends IntegerEnumeration> ReverseMapping<E> mapping(Class<E> clazz) {
    return ReverseMapping.get(clazz);
}
```

The helper method creates and caches the reverse mappings on demand:

```java
final class ReverseMapping<E extends IntegerEnumeration> {
    private static final Map<Class<?>, ReverseMapping<?>> CACHE = new ConcurrentHashMap<>();

    private static <E extends IntegerEnumeration> ReverseMapping<E> get(Class<?> clazz) {
        return (ReverseMapping<E>) CACHE.computeIfAbsent(clazz, ReverseMapping::new);
    }
}
```

## Type Conversion

To use integer enumerations in API methods and structures we implement a JNA _type converter_ which maps a Java type to/from its native equivalent:

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
            IntegerEnumeration e = (IntegerEnumeration) value;
            return e.value();
        }
    }

    @Override
    public Object fromNative(Object nativeValue, FromNativeContext context) {
        ReverseMapping<?> mapping = ReverseMapping.get(type);
        return mapping.map(value);
    }
};
```

The converter is registered with a global JNA _type mapper_ in the Vulkan library:

```java
public interface VulkanLibrary ... {
    TypeMapper MAPPER = mapper();

    private static TypeMapper mapper() {
        DefaultTypeMapper mapper = new DefaultTypeMapper();
        mapper.addTypeConverter(IntegerEnumeration.class, IntegerEnumeration.CONVERTER);
        ...
        return mapper;
    }
}
```

The JNA library is configured with this type mapper at instantiation-time:

```java
static VulkanLibrary create() {
    ...
    return Native.load(name, VulkanLibrary.class, Map.of(Library.OPTION_TYPE_MAPPER, MAPPER));
}
```

The only fly in the ointment is that this mapper also needs to be applied to __every__ JNA structure to enable integer enumerations to be used as structure fields, therefore the following intermediate base-class is introduced for all code-generated structures:

```java
public abstract class VulkanStructure extends Structure {
    protected VulkanStructure() {
        super(MAPPER);
    }
}
```

## Default Values

The final complication when mapping from a native enumeration value is that a default or unspecified value (i.e. zero) may not be a valid enumeration constant.

We introduce a _zero_ value to the reverse mapping which is initialised in the constructor:

```java
final class ReverseMapping<E extends IntegerEnumeration> {
    ...
    private final E zero;

    private ReverseMapping(Class<E> clazz) {
        this.map = ...
        this.zero = map.getOrDefault(0, array[0]);
    }
```

This `zero` value is mapped from the enumeration constant with a zero value (if present) or is arbitrarily set to the first constant.

In the type converter we can now safely handle invalid or unspecified native values:

```java
public Object fromNative(Object nativeValue, FromNativeContext context) {
    final ReverseMapping<?> mapping = ReverseMapping.get(type);
    final int value = (int) nativeValue;
    if(value == 0) {
        return mapping.zero;
    }
    else {
        return mapping.map(value);
    }
}
```
