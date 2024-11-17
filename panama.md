---
title: Panama Integration
---

---

# Contents

- [Overview](#overview)
- [Design](#design)
- [Implementation](#implementation)

---

# Overview

It had always been our intention to re-implement the Vulkan native layer using the FFM (Foreign Function and Memory) library developed as part of project [Panama](https://openjdk.java.net/projects/panama/).  The reasons for delaying this rework are discussed in [code generation](/jove-blog/blog/part-1-intro/code-generation#alternatives) at the very beginning of the blog.

With the LTS release of JDK 21 the FFM library was elevated to a preview feature (rather than requiring an incubator build), support was implemented in the IDE, and much more information and tutorials became available online.  So it was finally time to replace JNA with the more future proofed (and safer) FFM solution.

However FFM imposes some constraints on a possible implementation, in particular how the Vulkan API is generated.  There are several design decisions to be made that will influence the implementation, requiring some prototyping work and probably several false starts.

---

# Design

## Exploring Panama

The first step was to gain some familiarity with the FFM library and again we chose to use GLFW as a guinea pig since this API is relatively simple.

By comparison Vulkan is much more complex and is front-loaded with complexities such as marshalling of structures, by-reference types, arrays, callbacks for the diagnostic handler, etc.

TODO

```java
public class DesktopForeignDemo {
    private final Linker linker = Linker.nativeLinker();
    private final SymbolLookup lookup;

    public DesktopForeignDemo(SymbolLookup lookup) {
        this.lookup = lookup;
    }

    public static void main(String[] args) throws Throwable {
        try(Arena arena = Arena.ofConfined()) {
            // Init lookup service
            SymbolLookup lookup = SymbolLookup.libraryLookup("glfw3", arena);
            var demo = new DesktopForeignDemo(lookup);
            
            // Init GLFW
            ...
        }
    }
}
```

The process of invoking a native method using FFM is:

1. Load the native library via the `SymbolLookup` service.
2. Lookup the symbol for the method by name.
3. Build a `FunctionDescriptor` specifying the method signature.
4. Combine both of these to link a `MethodHandle` to the native method.
5. Invoke the method with an array of the appropriate arguments.

This is wrapped up into a quick-and-dirty helper:

```java
private Object invoke(String name, Object[] args, MemoryLayout returnType, MemoryLayout... signature) throws Throwable {
    MemorySegment symbol = lookup.find(name).orElseThrow();
    var descriptor = returnType == null ? FunctionDescriptor.ofVoid(signature) : FunctionDescriptor.of(returnType, signature);
    MethodHandle handle = linker.downcallHandle(symbol, descriptor);
    return handle.invokeWithArguments(args);
}
```

The GLFW library can now be initialised, which in this case also returns an integer success code:

```java
System.out.println("glfwInit=" + demo.invoke("glfwInit", null, JAVA_INT));
```

Basic GLFW properties can then be queried:

```java
// Display GLFW version
MemorySegment version = (MemorySegment) demo.invoke("glfwGetVersionString", null, ADDRESS);
System.out.println("glfwGetVersionString=" + version.reinterpret(Integer.MAX_VALUE).getString(0));

// Check Vulkan is supported
System.out.println("glfwVulkanSupported=" + demo.invoke("glfwVulkanSupported", null, ValueLayout.JAVA_BOOLEAN));
```

Note that the version string has to be _reinterpreted_ 
TODO
This is a little convoluted but is the way that FFM works.

Retrieving the supporting Vulkan extensions is somewhat more complex since the API method returns a _pointer_ to an array of strings and also uses a _by reference_ integer to return the size of the array as a side-effect.  This parameter is implemented as a `ValueLayout.JAVA_INT` memory layout:

```java
MemorySegment count = arena.allocate(JAVA_INT);
```

Invoking the extensions method returns the array pointer and the length of the array can then be extracted from the reference:

```java
MemorySegment extensions = (MemorySegment) demo.invoke("glfwGetRequiredInstanceExtensions", new Object[]{count}, ADDRESS, ADDRESS);
int length = count.get(JAVA_INT, 0);
System.out.println("glfwGetRequiredInstanceExtensions=" + length);
```

The returned pointer is reinterpreted as an array and each element is be extracted and transformed to a Java string:

```java
MemorySegment array = extensions.reinterpret(length * ADDRESS.byteSize());

for(int n = 0; n < length; ++n) {
    MemorySegment e = array.getAtIndex(ADDRESS, n);
    System.out.println("  " + e.reinterpret(Integer.MAX_VALUE).getString(0));
}
```

Finally GLFW can be closed:

```java
demo.invoke("glfwTerminate", null, null);
```

## Native Reference Types

Arrays in particular will require further thought

TODO

Note that this problem only applies to structures and arrays when used as a parameter to a native method.



1. An array of data constructed by the application that is passed to the native layer and is therefore essentially read-only during marshalling.

2. A _container_ for an array of data (usually pointers or structures) returned _by reference_ from the native layer, implying that the array needs to be post-processed _after_ the native invocation to marshal the returned data to each element of the container.

Similarly structures can either be read-only data passed to the native layer or an 'empty' structure to be _populated_ as a side effect of the method.

However it is not possible for the framework to determine which use-case is intended purely from the method signature or from the array or structure itself.  For example, a structure _could_ legitimately contain default values but is not intended as a by-reference parameter, similarly for an array with null elements.

Therefore the marshalling framework requires some approach that differentiates the use-cases.  Here are some possibilities:

1. Create new types (and native mappers) for every use-case explicitly.

2. Implement a _marker_ interface to identify by-reference types (the approach taken by JNA).

3. Use some other metadata mechanism such as an annotation.

4. Introduce a generic _container_ type that would compose a by-reference structure or array.

The first two approaches are ugly and not very extensible, the marker interface approach in particular only works for methods with a single by-reference structure parameter.

TODO


## Decisions


Note that the API will be defined in terms of Java and JOVE domain types, whereas the native methods will 





| type                | layout        | pointer layout      | return      | by reference    |
| ----                | ------        | --------------      | ------      | ------------    |
| primitive           | primitive     | n/a                 | yes         | no              |
| String              | pointer       | string              | yes         | no              |
| IntEnum             | int           | n/a                 | yes         | no              |
| BitMask             | int           | n/a                 | yes         | no              |
| IntegerReference    | pointer       | int                 | no          | special         |
| PointerReference    | pointer       | pointer             | no          | special         |
| Handle              | pointer       | n/a                 | yes         | yes             |
| NativeObject        | pointer       | n/a                 | no          | no              |
| NativeStructure     | pointer       | structure layout    | yes         | yes             |
| arrays              | pointer       | element layout      | yes         | no              |

Where:

* the _type_ is a JOVE type.

* _layout_ is the corresponding FFM memory layout.

* _pointer layout_ specifies the structure of the pointer memory (if any).

* _return_ indicates whether the native type can logically be returned from a native method.

* and _by reference_ indicates types that can be populated as a by-reference side effect (see above).

Notes:

* Structures and arrays are compound types that will require 'recursive' marshalling of structure fields and array elements.

* Integer and pointer references _will_ be handled by custom types and mappers since these are integral to the framework and are already present in JOVE.

All pointer types (indicated by a _layout_ of _pointer_ in the above table) will compose a FFM address allocated on-demand during marshalling.  
Although this adds a small amount of additional complexity
Since the address is allocated from an `Arena` this avoids having to introduce an implicit context or annoying additional constructor arguments.
TODO

whether to make structures a builder or use public fields as-is
ref to `layout` and why its needed (link below)


## Framework

With these decisions made (at least for now) the following components are required to implement the new framework:

* A registry of _native mappers_ that marshal Java types to/from the equivalent native representation.

* The _native method_ class that composes an FFM handle and the native mappers for its parameter signature and return type.

* A _native factory_ which is responsible for creating and initialising a native API defined as as a Java interface.

Note that instead of each JOVE type being responsible for marshalling its data, native mappers will be implemented as _companion_ classes to support non-extensible types such a strings, arrays, etc.






As well as refactoring of existing JOVE support types (such as the `Handle`) the following 

* structures + layout

* two-stage invocation + container




---


# Implementation

## Overview

Integration of the new framework into JOVE requires the following changes as a bare minimum:

* Removal of the JNA library.

* Implementation of the new framework components outlined above.

* Refactoring of JOVE types that are explicitly dependant on native pointers (namely the `Handle` class).

* Implementation of replacements for the basic types to support the framework: strings, by reference types, enumerations.

* Refactoring of the Vulkan API in terms of the new framework.

This 'big bang' approach should (hopefully) get the code to a point where it compiles but will certainly not run.

The plan is then to iteratively reintroduce each JOVE component to a new demo application and refactor accordingly.  As already noted Vulkan is very front-loaded with complexity, so additional framework support will be implemented as required for each component.

## Framework

### Native Factory

The logical starting point is the native factory:

```java
public class NativeFactory {
    public <T> T build(SymbolLookup lookup, Class<T> api) {
        if(!api.isInterface()) throw new IllegalArgumentException(...);
        Instance instance = new Instance(lookup);
        ...
    }
}
```

Where _api_ is the interface defining the native API and _instance_ is a local helper.

The factory enumerates the API methods via reflection and delegates to the helper:

```java
Map<Method, NativeMethod> methods = Arrays
    .stream(api.getMethods())
    .filter(NativeFactory::isNativeMethod)
    .collect(toMap(Function.identity(), instance::build));
```

Where appropriate API methods are identified by the following helper:

```java
private static boolean isNativeMethod(Method method) {
    int modifiers = method.getModifiers();
    return !Modifier.isStatic(modifiers);
}
```

The native method is initially an empty implementation:

```java
public class NativeMethod {
    private MemorySegment address;
    
    public Object invoke(Object[] args) {
        return null;
    }
}
```

The helper looks up the memory address of the native method and wraps it into a new instance:

```java
private class Instance {
    private final SymbolLookup lookup;

    public NativeMethod build(Method method) {
        MemorySegment address = address(method);
        return new NativeMethod(address);
    }

    private MemorySegment lookup(Method method) {
        return lookup
            .find(method.getName())
            .orElseThrow();
    }
};
```

The factory next creates a _invocation handler_ that delegates API calls to the appropriate native method instance:

```java
var handler = new InvocationHandler() {
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        NativeMethod delegate = methods.get(method);
        return delegate.invoke(args);
    }
};
```

And finally a proxy implementation of the API is created from the handler:

```java
ClassLoader loader = this.getClass().getClassLoader();
return (T) Proxy.newProxyInstance(loader, new Class<?>[]{api}, handler);
```

This proxy approach enables the API to be expressed in terms of Java and JOVE types, while the underlying native methods are implemented using FFM.  The remainder of the framework is concerned with marshalling between these two layers.

### Native Methods

To fully implement the native method class it will need to know the method signature and return type in order to link the FFM method handle.

Looking ahead we will also need to construct native methods from first principles (i.e. not using reflection) when addressing the diagnostics handler (and possibly for other use cases).  Therefore the native method will be agnostic to the source of the method address.  This is a little more work but better separates concerns between the various collaborating classes.

First a builder is added to construct a native method:

```java
public static class Builder {
    private final Linker linker = Linker.nativeLinker();
    private MemorySegment address;
    private Class<?> returnType;
    private final List<Class<?>> signature = new ArrayList<>();
}
```

The memory layout of the method is derived from its signature:

```java
private MemoryLayout[] layout() {
    return signature
        .stream()
        .map(this::layout)
        .toArray(MemoryLayout[]::new);
}
```

Where the temporary `layout` method maps a Java type to the layout of its corresponding native representation:

```java
private MemoryLayout layout(Class<?> type) {
    if(type == int.class) {
        return ValueLayout.JAVA_INT;
    }
    else {
        throw new IllegalArgumentException(...);
    }
}
```

This in turn is used to derive the function descriptor:

```java
private FunctionDescriptor descriptor(MemoryLayout[] layout) {
    if(returnType == null) {
        return FunctionDescriptor.ofVoid(layout);
    }
    else {
        final MemoryLayout m = returnType.mapper.layout();
        return FunctionDescriptor.of(m, layout);
    }
}
```

Finally the `build` method constructs the method handle and instantiates a method instance:

```java
public NativeMethod build() {
    MemoryLayout[] layout = layout();
    FunctionDescriptor descriptor = descriptor(layout);
    MethodHandle handle = linker.downcallHandle(address, descriptor);
    return new NativeMethod(handle, signature, returnType);
}
```

The factory helper uses the new builder to construct a native method for each API method:

```java
private NativeMethod build(MemorySegment address, Method method) {
    // Init method builder
    var builder = new NativeMethod.Builder()
        .address(address)
        .signature(method.getParameterTypes());

    // Set return type
    Class<?> returnType = method.getReturnType();
    if(returnType != void.class) {
        builder.returns(returnType);
    }

    // Construct native method
    return builder.build();
}
```

To exercise the new framework the following demo code defines a cut-down GLFW library dependant only on integer primitives:

```java
interface API {
    int         glfwInit();
    void        glfwTerminate();
    int         glfwVulkanSupported();
    //String    glfwGetVersionString();
    //String[]  glfwGetRequiredInstanceExtensions(&int count);
}
```

From which a proxy implementation can be generated using the new factory:

```java
try(Arena arena = Arena.ofConfined()) {
    var lookup = SymbolLookup.libraryLookup("glfw3", arena);
    var factory = new NativeFactory();
    API api = factory.build(lookup, API.class);
    ...
}
```

This is then used to exercise basic GLFW initialisation:

```java
System.out.println("init="+api.glfwInit());
System.out.println("Vulkan="+api.glfwVulkanSupported());
desktop.glfwTerminate();
```

### Native Mappers

The final piece of the framework is the _native mapper_ which defines a Java or JOVE type that can be marshalled to a native type:

```java
public interface NativeMapper<T> {
    /**
     * @return Type
     */
    Class<T> type();

    /**
     * @return Native type layout
     */
    MemoryLayout layout();
    
    /**
     * Marshals the given value to its native representation.
     * @param value         Value to marshal
     * @param context       Native context
     * @return Native value
     */
    Object toNative(T value, NativeContext context);
}
```

Where the `NativeContext` composes the services required to allocate off-heap memory during marshalling:

```java
public record NativeContext(SegmentAllocator allocator, NativeMapperRegistry registry)
```

The framework can now be made configurable by the introduction of a registry of supported native mappers:

```java
public class NativeMapperRegistry {
    private final Map<Class<?>, NativeMapper<?>> mappers = new HashMap<>();

    public void add(NativeMapper<?> mapper) {
        Class<?> type = mapper.type();
        if(type == null) throw new IllegalArgumentException(...);
        mappers.put(type, mapper);
    }

    public Optional<NativeMapper<?>> mapper(Class<?> type) {
        ...
    }
}
```

The registered native mapper for a given type can be looked up from the registry:

```java
public Optional<NativeMapper<?>> mapper(Class<?> type) {
    NativeMapper<?> mapper = mappers.get(type);
    if(mapper == null) {
        return find(type);
    }
    else {
        return Optional.of(mapper);
    }
}
```

Or a mapper can be found for a subclass of a supported type (the majority of the JOVE domain types):

```java
private Optional<NativeMapper<?>> find(Class<?> type) {
    return mappers
        .values()
        .stream()
        .filter(e -> e.type().isAssignableFrom(type))
        .findAny()
        .map(m -> register(m, type));
}
```

Where the subclass mapper is registered as a side-effect:

```java
private NativeMapper<?> register(NativeMapper<?> mapper, Class type) {
    mappers.put(type, mapper);
    return mapper;
}
```

A native type can also optionally be unmarshalled from a native method with a return value:

```java
interface ReturnMapper<T, R> extends NativeMapper<T> {
    /**
     * Unmarshals a native return value.
     * @param value     Native return value
     * @param type      Target type
     * @return Return value
     */
    Object fromNative(R value, Class<? extends T> type);
}
```

The native method builder can now be refactored in terms of native mappers, replacing the temporary `layout` helper above.

```java
public static class Builder {
    private final Linker linker = Linker.nativeLinker();
    private final NativeMapperRegistry registry;
    private MemorySegment address;
    private ReturnType<?> returnType;
    private final List<NativeParameter<?>> signature = new ArrayList<>();
}
```

The `ReturnType` and `NativeParameter` are private record types that compose a _target_ type and the corresponding native mapper.  The purpose of this should become clear later when we implement the various native mappers required to support JOVE.

The native mapper for a method parameter is now looked up from the registry:

```java
public Builder parameter(Class<?> type) {
    NativeMapper<?> mapper = registry.mapper(type).orElseThrow(...);
    signature.add(new NativeParameter<>(type, mapper));
    return this;
}
```

And similarly for the return type:

```java
public Builder returns(Class<?> type) {
    // Lookup native mapper
    NativeMapper<?> mapper = registry.mapper(type).orElseThrow(...);

    // Ensure can be returned
    if(!(mapper instanceof ReturnMapper returnMapper)) {
        throw new IllegalArgumentException(...);
    }

    // Set return type wrapper
    returnType = new ReturnType<>(type, returnMapper);

    return this;
}
```

In the `build` method the memory layout of the method signature is now derived from the mappers:

```java
private MemoryLayout[] layout() {
    return signature
        .stream()
        .map(p -> p.mapper)
        .map(NativeMapper::layout)
        .toArray(MemoryLayout[]::new);
}
```

And similarly for the function descriptor:

```java
private FunctionDescriptor descriptor(MemoryLayout[] layout) {
    if(returnType == null) {
        return FunctionDescriptor.ofVoid(layout);
    }
    else {
        MemoryLayout m = returnType.mapper.layout();
        return FunctionDescriptor.of(m, layout);
    }
}
```

### Null

Thus far the framework assumes all marshalled values are non-null, which is fine for primitives but needs to be explicitly handled for reference types.

A second method is added to the native mapper which marshals a `null` domain value:

```java
default Object toNativeNull(Class<?> extends T> type) {
    return MemorySegment.NULL;
}
```

A helper method is added to the context class to switch accordingly:

```java
public Object toNative(NativeMapper mapper, Object value, Class<?> type) {
    if(value == null) {
        return mapper.toNativeNull(type);
    }
    else {
        return mapper.toNative(value, this);
    }
}
```

Finally the marshalling of the method return value is also extended to handle null values:

```java
private Object marshalReturnValue(Object value) {
    if(returnType == null) {
        return null;
    }
    else
    if(MemorySegment.NULL.equals(value)) {
        return null;
    }
    else {
        return returnType.mapper.fromNative(value, returnType.type);
    }
}
```

This should slightly simplify the implementation of the various native mappers and hopefully makes the logic more explicit.

### Integration

To support the cut-down GLFW library used thus far a default registry factory method is added:

```java
public static NativeMapperRegistry create() {
    var registry = new NativeMapperRegistry();

    var primitives = Map.of(
        byte.class,     ValueLayout.JAVA_BYTE,
        char.class,     ValueLayout.JAVA_CHAR,
        boolean.class,  ValueLayout.JAVA_BOOLEAN,
        int.class,      ValueLayout.JAVA_INT,
        short.class,    ValueLayout.JAVA_SHORT,
        long.class,     ValueLayout.JAVA_LONG,
        float.class,    ValueLayout.JAVA_FLOAT,
        double.class,   ValueLayout.JAVA_DOUBLE
    );
    for(Class<?> type : primitives.keySet()) {
        ValueLayout layout = primitives.get(type);
        var mapper = new DefaultNativeMapper<>(type, layout);
        registry.add(mapper);
    }
    
    return registry;
}
```

Where a `DefaultNativeMapper` is a simple pass-through implementation:

```java
public class DefaultNativeMapper<T, R> implements ReturnMapper<T, R> {
    private final Class<T> type;
    private final MemoryLayout layout;

    @Override
    public Object toNative(T value, NativeContext __) {
        return value;
    }

    @Override
    public Object fromNative(R value, Class<?> type) {
        return value;
    }
}
```

The existing demo should still run with the minor benefit that the following method can now explicitly return a Java boolean:

```java
interface API {
    boolean glfwVulkanSupported();
}
```

## Framework Support

### Overview

With the basic framework in place, the following sections cover common functionality that can be implemented before actually tackling Vulkan itself:

* The `Handle` wrapper class.

* By-reference types.

* Integer enumerations.

* Strings.

### Handles

A JOVE `Handle` is an immutable, opaque wrapper for an FFM address:

```java
public final class Handle {
    private final MemorySegment address;

    public Handle(MemorySegment address) {
        this.address = address.asReadOnly();
    }

    public Handle(long address) {
        this(MemorySegment.ofAddress(address));
    }

    public MemorySegment address() {
        return MemorySegment.ofAddress(address.address());
    }
}
```

With a companion native mapper:

```java
public static final class HandleNativeMapper extends DefaultNativeMapper<Handle, MemorySegment> {
    public HandleNativeMapper() {
        super(Handle.class, ADDRESS);
    }

    @Override
    public MemorySegment toNative(Handle handle, NativeContext __) {
        return handle.address;
    }

    @Override
    public Handle fromNative(MemorySegment address, Class<? extends Handle> type) {
        return new Handle(address);
    }
}
```

### Reference Types

The following wrapper for a mutable pointer is first implemented to support by-reference types:

```java
public final class Pointer {
    private MemorySegment address;

    public boolean isAllocated() {
        return Objects.nonNull(address);
    }

    protected MemorySegment allocate(MemoryLayout layout, NativeContext context) {
        if(!isAllocated()) {
            address = context.allocator().allocate(layout);
        }
        return address;
    }
}
```

A pointer is composed into a replacement for by-reference integers:

```java
public final class IntegerReference {
    private final Pointer pointer = new Pointer();

    public int value() {
        if(pointer.isAllocated()) {
            return pointer.address().get(JAVA_INT, 0);
        }
        else {
            return 0;
        }
    }
}
```

And the companion native mapper allocates the off-heap memory as required:

```java
public static final class IntegerReferenceNativeMapper extends AbstractNativeMapper<IntegerReference> {
    public IntegerReferenceNativeMapper() {
        super(IntegerReference.class, JAVA_INT);
    }

    @Override
    public MemorySegment toNative(IntegerReference ref, NativeContext context) {
        return ref.pointer.allocate(JAVA_INT, context);
    }

    @Override
    public MemorySegment toNativeNull(Class<? extends IntegerReference> type) {
        throw new UnsupportedOperationException();
    }
}
```

The by-reference pointer type and companion mapper are implemented similarly:

```java
public final class PointerReference {
    private final Pointer pointer = new Pointer();

    public Handle handle() {
        if(!pointer.isAllocated()) throw new IllegalStateException(...);
        MemorySegment handle = pointer.address().get(ADDRESS, 0);
        return new Handle(handle);
    }

    public static final class PointerReferenceNativeMapper extends AbstractNativeMapper<PointerReference> {
        public PointerReferenceNativeMapper() {
            super(PointerReference.class, ADDRESS);
        }
        ...
    }
}
```

Note that we assume that integer and pointer references cannot logically be `null` or returned from a native method.

### Strings

A Java string is the only non-JOVE type required to support the new framework (other than primitives).  Therefore the native mapper is stand-alone rather than a companion class in this case:

```java
public final class StringNativeMapper extends DefaultNativeMapper<String, MemorySegment> {
    private final WeakHashMap<String, MemorySegment> cache = new WeakHashMap<>();

    public StringNativeMapper() {
        super(String.class, ValueLayout.ADDRESS);
    }

    @Override
    public MemorySegment toNative(String str, NativeContext context) {
        return cache.computeIfAbsent(str, __ -> context.allocator().allocateFrom(str));
    }

    @Override
    public String fromNative(MemorySegment address, Class<? extends String> type) {
        return unmarshal(address);
    }

    protected static String unmarshal(MemorySegment address) {
        return address.reinterpret(Integer.MAX_VALUE).getString(0);
    }
}
```

Note that this mapper caches marshalled strings.

### Enumerations

Integer enumerations are slightly different to the other JOVE types in that the values are reference types but the native representation is a primitive integer.  Additionally the mapper needs to account for default or `null` enumeration constants (which was one of the reasons for providing the _target_ type during marshalling).

The mapper for an enumeration essentially clones the existing JNA type converter:

```java
public class IntEnumNativeMapper extends DefaultNativeMapper<IntEnum, Integer> {
    public IntEnumNativeMapper() {
        super(IntEnum.class, JAVA_INT);
    }

    @Override
    public Integer toNative(IntEnum e, NativeContext context) {
        return e.value();
    }

    @Override
    public Integer toNativeNull(Class<? extends IntEnum> type) {
        return ReverseMapping
            .get(type)
            .defaultValue()
            .value();
    }

    @Override
    public IntEnum fromNative(Integer value, Class<? extends IntEnum> type) {
        ReverseMapping<?> mapping = ReverseMapping.get(type);
        if(value == 0) {
            return mapping.defaultValue();
        }
        else {
            return mapping.map(value);
        }
    }
}
```

The mapper for an enumeration bit mask is relatively trivial since the 'default' value for a mask is always zero and the actual target type has no relevance:

```java
public class BitMaskNativeMapper extends DefaultNativeMapper<BitMask, Integer> {
    public BitMaskNativeMapper() {
        super(BitMask.class, ValueLayout.JAVA_INT);
    }

    @Override
    public Integer toNative(BitMask value, NativeContext _) {
        return value.bits();
    }

    @Override
    public Integer toNativeNull(Class<? extends BitMask> _) {
        return 0;
    }

    @Override
    public BitMask<?> fromNative(Integer value, Class<? extends BitMask> _) {
        return new BitMask<>(value);
    }
}
```

## Integration

### Overview

With JNA removed and the basics of the new framework in place the Vulkan API can be refactored accordingly which entails:

* A global find-and-replace for the by-reference types.

* Adding the newly implemented native mappers to the default registry.

This initial refactoring work went much more smoothly than anticipated, with only a handful of compilation problems that were deferred until later in the process (which are covered below).  The existing code did a decent job of abstracting over JNA.

The approach from this point is to instantiate the Vulkan library and tackle each JOVE component in order starting with the `Instance` domain object.

This iterative approach implies:

* The overall Vulkan API will have to be temporarily cut down to the bare minimum required for the next component.

* Manual fiddling of structure classes until we are confident that the requirements for refactoring the code generator are clear.

* Much of the GLFW library will need to be temporarily removed since is very dependant on callbacks (up-call stubs in FFM parlance).

### Vulkan

As a quick diversion, the following new type is introduced to compose the various parts of the Vulkan implementation that were previously separate fields of the instance:

```java
public class Vulkan {
    private final VulkanLibrary lib;
    private final NativeMapperRegistry registry;
    private final ReferenceFactory factory;
}
```

With a `create` method to instantiate the library using the new framework:

```java
public static Vulkan create() {
    var registry = NativeMapperRegistry.create();
    var factory = new NativeFactory(registry);
    var lib = factory.build("vulkan-1", VulkanLibrary.class);
    return new Vulkan(lib, registry, new ReferenceFactory());
}
```

This is equivalent to the `Desktop` class for the GLFW implementation and replaces the `create` method on the existing `VulkanLibrary` interface (which had become a bit of dumping ground for helper methods and constants).

The demo can now be extended to instantiate the Vulkan library and configure the instance:

```java
Vulkan vulkan = Vulkan.create();

Instance instance = new Instance.Builder()
    .name("VulkanTest")
    .extension("VK_EXT_debug_utils")
    .layer(ValidationLayer.STANDARD_VALIDATION)
    .build(vulkan);
```

Note that for the moment the demo is not querying or registering the surface extensions for the operating system (since we have yet to implement support for arrays).

However the demo fails since it requires marshalling support for structures, the subject of the next section.

### Structures

A new type is created as a base-class for all native structures:

```java
public abstract class NativeStructure {
    private final Pointer pointer = new Pointer();

    /**
     * @return Memory layout of this structure
     */
    protected abstract StructLayout layout();
}
```

The memory layout of a native structure could theoretically be derived from the structure class itself (via reflection).  However the fields of a reflected Java class are (irritatingly) unordered (hence the `@FieldOrder` annotation required by JNA structures), therefore the abstract `layout` method is introduced that explicitly declares the native field order.  Eventually the structure layout will be constructed by the code generator (where the actual field order is available), for the moment the layouts are hand-crafted.

The companion native mapper maintains a cache of the _metadata_ for each structure type:

```java
public static class StructureNativeMapper extends DefaultNativeMapper<NativeStructure, MemorySegment> {
    private record Entry(StructLayout layout, List<FieldMapping> mappings) {
    }

    private final Map<Class<? extends NativeStructure>, Entry> entries = new HashMap<>();
    private final NativeMapperRegistry registry;

    public StructureNativeMapper() {
        super(NativeStructure.class, ValueLayout.ADDRESS);
    }

    private Entry entry(NativeStructure structure) {
        return entries.computeIfAbsent(structure.getClass(), __ -> create(structure));
    }
}
```

The `Entry` record composes the layout and _field mappings_ of a structure:

```java
private Entry create(NativeStructure structure) {
    StructLayout layout = structure.layout();
    Class<? extends NativeStructure> type = structure.getClass();
    List<FieldMapping> mappings = FieldMapping.build(layout, type, registry);
    return new Entry(layout, mappings);
}
```

A _field mapping_ encapsulates the mapping between a structure field and the corresponding off-heap field:

```java
class FieldMapping {
    private final Field field;
    private final VarHandle handle;
    private final NativeMapper<?> mapper;
}
```

Where:

* _field_ is a reflected structure field.

* _handle_ is the associated off-heap field.

* and _mapper_ is used to marshal field values.

The field mappings for a given native structure are enumerated via reflection:

```java
protected static List<FieldMapping> build(StructLayout layout, Class<? extends NativeStructure> type, NativeMapperRegistry registry) {
    var builder = new Object() {
        ...
    };
    
    return Arrays
        .stream(type.getDeclaredFields())
        .filter(FieldMapping::isStructureField)
        .map(builder::build)
        .toList();
}
```

Where a structure field is defined as a public, top-level member:

```java
private static boolean isStructureField(Field field) {
    int modifiers = field.getModifiers();
    return Modifier.isPublic(modifiers) && !Modifier.isStatic(modifiers);
}
```

The local, anonymous `builder` is responsible for constructing each field mapping:

```java
FieldMapping build(Field field) {
    VarHandle handle = layout.varHandle(PathElement.groupElement(field.getName());
    NativeMapper<?> mapper = registry.mapper(field).orElseThrow(...);
    return new FieldMapping(field, handle, mapper);
}
```

The mapper marshals a structure to the off-heap memory on-demand:

```java
public MemorySegment toNative(NativeStructure structure, NativeContext context) {
    Pointer pointer = structure.pointer;
    if(!pointer.isAllocated()) {
        populate(structure, context);
    }
    return pointer.address();
}
```

The `populate` method first allocates off-heap memory for the structure and then marshals each field:

```java
private void populate(NativeStructure structure, NativeContext context) {
    // Allocate off-heap structure memory
    Entry entry = entry(structure);
    MemorySegment address = structure.pointer.allocate(entry.layout, context);

    // Populate off-heap memory from structure
    for(FieldMapping m : entry.mappings) {
        m.toNative(structure, address, context);
    }
}
```

The process of marshalling each structure field is:

1. Retrieve the field value from the structure.

2. Marshal the value via the native mapper.

3. Set the native value in the off-heap memory.

This is performed by the following method on the field mapping class:

```java
void toNative(NativeStructure structure, MemorySegment address, NativeContext context) {
    Object value = field.get(structure);
    Object actual = context.toNative(mapper, value, field.getType());
    handle.set(address, 0L, actual);
}
```

In the reverse operation where a structure is returned from a native method, a new instance is created and the fields are copied from the off-heap address:

```java
public Object fromNative(MemorySegment address, Class<? extends NativeStructure> type) {
    // Create new structure
    var structure = type.getDeclaredConstructor().newInstance();
    Entry entry = entry(structure);

    // Populate structure from off-heap memory
    MemorySegment pointer = address.reinterpret(entry.layout.byteSize());
    for(FieldMapping m : entry.mappings) {
        m.fromNative(pointer, structure);
    }

    return structure;
}
```

Which delegates to the corresponding field mapping method:

```
void fromNative(MemorySegment address, NativeStructure structure) {
    Object value = handle.get(address, 0L);
    Object actual = mapper.fromNative(value, field.getType());
    set(structure, actual);
}
```

Notes:

* Marshalling is recursive since structures can be a graph of objects.

* As things stand a structure is essentially immutable once it has been marshalled to the native layer since the `populate` method is only invoked _once_ when the off-heap memory is allocated.  Native structures are really only intended to be transient carrier objects during an API call, however it is possible to modify structure data after it has been marshalled (which would be silently ignored).  This is an outstanding issue (or possibly a requirement) to be addressed in the future.

### Instance

Now that structures can be marshalled the `Instance` domain class can be refactored to use the new `Vulkan` root object:

```java
public class Instance extends TransientNativeObject {
    private final Vulkan vulkan;
    private final Collection<Handler> handlers = new ArrayList<>();
}
```

Along with the following temporary modifications:

* The diagnostics handler logic is removed.

* Similarly the `function` method that retrieves the function pointers for handlers.

* Extensions and validation layers are uninitialised.

The final step is to refactor the two structures used to configure the instance which requires:

* Refactoring the structures to inherit the new `NativeStucture` type.

* Removing any legacy JNA artifacts such the `FieldOrder` annotation or `ByReference` markers.

* Declaration of the FFM memory layouts.

Determining a structure layout requires the following:

* Deriving the FFM layout from each structure field.

* Naming each field such that it can be queried from the layout during marshalling.

* Adding alignment padding as required (see below).

The revised application details descriptor is as follows:

```java
public class VkApplicationInfo extends NativeStructure {
    public final VkStructureType sType = VkStructureType.APPLICATION_INFO;
    public Handle pNext;
    public String pApplicationName;
    public int applicationVersion;
    public String pEngineName;
    public int engineVersion;
    public int apiVersion;
}
```

And its corresponding memory layout:

```java
public StructLayout layout() {
    return MemoryLayout.structLayout(
        JAVA_INT.withName("sType"),
        PADDING,
        POINTER.withName("pNext"),
        POINTER.withName("pApplicationName"),
        JAVA_INT.withName("applicationVersion"),
        PADDING,
        POINTER.withName("pEngineName"),
        JAVA_INT.withName("engineVersion"),
        PADDING,
        JAVA_INT.withName("apiVersion"),
        PADDING
    );
}
```

The convenience `POINTER` constant defines a native pointer to other structures, strings or arrays:

```java
public abstract class NativeStructure {
    protected static final AddressLayout POINTER = ValueLayout.ADDRESS.withTargetLayout(MemoryLayout.sequenceLayout(Integer.MAX_VALUE, ValueLayout.JAVA_BYTE));
}
```

The `PADDING` is required to ensure fields are aligned to the word size of the structure (8 bytes by default):

```JAVA
protected static final MemoryLayout PADDING = MemoryLayout.paddingLayout(4);
```

The code generator will need to track the byte alignment and inject padding as required when generating each structure.

The layout for the instance descriptor is constructed similarly:

```java
return MemoryLayout.structLayout(
    JAVA_INT.withName("sType"),
    PADDING,
    POINTER.withName("pNext"),
    JAVA_INT.withName("flags"),
    PADDING,
    POINTER.withName("pApplicationInfo"),
    JAVA_INT.withName("enabledLayerCount"),
    PADDING,
    POINTER.withName("ppEnabledLayerNames"),
    JAVA_INT.withName("enabledExtensionCount"),
    PADDING,
    POINTER.withName("ppEnabledExtensionNames")
);
```

In the demo application the Vulkan instance should now be successfully instantiated and configured using the new framework.

A lot of work to get to the stage JOVE was at several years previous!

### Diagnostic Handler

Refactoring the diagnostic handler presents a couple of problems:

* The native methods to create and destroy a handler must be created programatically, since the address is a _function pointer_ instead of a symbol looked up from the native library.

* Ideally the handler methods would reuse the existing native framework, in particular the support for structures when creating the handler and unmarshalling of diagnostic reports.

* A handler requires an FFM callback stub in order to report diagnostic messages.

The majority of the existing handler code can remain as-is except for:

* Invocation of the create and destroy function pointers.

* Creation of the message callback.

* Unmarshalling a diagnostic report to a `Message` record.

First the `function` method of the `Instance` is reintroduced, which turns out to be trivial:

```java
public Handle function(String name) {
    Handle handle = vulkan.library().vkGetInstanceProcAddr(this, name);
    if(handle == null) throw new IllegalArgumentException(...);
    return handle;
}
```

This is used to retrieve the function pointer that creates the handler:

```java
private Handle create(VkDebugUtilsMessengerCreateInfoEXT info) {
    Handle create = instance.function("vkCreateDebugUtilsMessengerEXT");
    NativeMethod method = method(create);
    PointerReference ref = invoke(method, info);
    return ref.handle();
}
```

A native method is constructed from the function pointer:

```java
private NativeMethod method(Handle create) {
    Class<?>[] signature = {Instance.class, VkDebugUtilsMessengerCreateInfoEXT.class, Handle.class, PointerReference.class};

    return new NativeMethod.Builder(registry)
        .address(address)
        .returns(int.class)
        .signature(signature)
        .build();
}
```

And invoked with the relevant arguments:

```java
private PointerReference invoke(NativeMethod method, VkDebugUtilsMessengerCreateInfoEXT info) {
    PointerReference ref = instance.vulkan().factory().pointer();
    Object[] args = {instance, info, null, ref};
    Vulkan.check((int) Handler.invoke(instance, method, args, registry));
    return ref;
}
```

Which delegates to the following helper:

```java
private static Object invoke(Instance instance, NativeMethod method, Object[] args, NativeMapperRegistry registry) {
    var context = new NativeContext(Arena.ofAuto(), registry);
    return method.invoke(args, context);
}
```

The handler is destroyed in a similar fashion:

```java
protected void release() {
    Handle function = instance.function("vkDestroyDebugUtilsMessengerEXT");
    NativeMapperRegistry registry = instance.vulkan().registry();
    Class<?>[] signature = {Instance.class, Handler.class, Handle.class};
    NativeMethod destroy = new NativeMethod.Builder(registry).address(address).signature(signature).build();
    Object[] args = {instance, this, null};
    invoke(instance, destroy, args, registry);
}
```

This nicely reuses the existing framework rather than having to implement more FFM code from first principles.  This was the reason for the native method being intentionally agnostic to the source of the function address earlier.

To build the callback a new method is added to the existing class that provides the memory _address_ of the callback method:

```java
private static class MessageCallback {
    private final Consumer<Message> consumer;
    private final StructureNativeMapper mapper;

    MemorySegment address() {
        MethodHandle handle = handle();
        return link(handle.bindTo(this));
    }
}
```

Note that the callback _instance_ is bound to the method handle so that the `message` virtual method is invoked for a given callback:

```java
private static MethodHandle handle() {
    var type = MethodType.methodType(boolean.class, int.class, int.class, MemorySegment.class, MemorySegment.class);
    return MethodHandles.lookup().findVirtual(MessageCallback.class, "message", type);
}
```

This handle is then linked to an up-call stub with the method signature matching the `message` method:

```java
private static MemorySegment link(MethodHandle handle) {
    var descriptor = FunctionDescriptor.of(JAVA_BOOLEAN, JAVA_INT, JAVA_INT, ADDRESS, ADDRESS);
    return Linker.nativeLinker().upcallStub(handle, descriptor, Arena.ofAuto());
}
```

Finally the `build` method of the handler is modified to write the _address_ of the callback into the descriptor:

```java
public Handler build() {
    init();
    var callback = new MessageCallback(consumer, registry);
    var info = populate(callback.address());
    Handle handle = create(info);
    return new Handler(handle, instance);
}
```

The last required code change is to unmarshal the diagnostic report received by the `message` callback method and delegate to the message handler:

```java
public boolean message(int severity, int typeMask, MemorySegment pCallbackData, MemorySegment pUserData) {
    // Transform the message properties
    var types = new BitMask<VkDebugUtilsMessageType>(typeMask).enumerate(TYPE);
    var level = SEVERITY.map(severity);

    // Unmarshal the message structure
    var data = mapper.fromNative(pCallbackData, VkDebugUtilsMessengerCallbackData.class);

    // Handle message
    Message message = new Message(level, types, (VkDebugUtilsMessengerCallbackData) data);
    consumer.accept(message);

    return false;
}
```

Diagnostic handlers can now be attached to the instance as before.  A good test is to temporarily remove the code that releases the attached handlers when the instance is destroyed, which should result in Vulkan complaining about orphaned diagnostics handlers.

---

