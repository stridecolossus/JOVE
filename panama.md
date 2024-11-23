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

It had always been the intention at some point to reimplement the Vulkan native layer using the FFM (Foreign Function and Memory) library developed as part of project [Panama](https://openjdk.java.net/projects/panama/).  The reasons for delaying this rework are discussed in [code generation](/jove-blog/blog/part-1-intro/code-generation#alternatives) at the very beginning of this blog.

With the LTS release of JDK 21 the FFM library was elevated to a preview feature (rather than requiring an incubator build) and much more information and tutorials became available online.  So it was finally time to replace JNA with the more future proofed (and safer) FFM solution.

The fundamental decision is whether to use the `jextract` tool to generate the Java bindings for the native libraries or to implement some proprietary solution.  To resolve this question we will use the tool to generate the Vulkan API and then compare and contrast with a small, hard-crafted application built using FFM from first principles.  Once this important decision has been resolved we can then determine the refactoring requirements.

---

# Design

## Exploring Panama

The first step is to gain some familiarity with the FFM library and again GLFW is chosen as a guinea pig.  This API is relatively simple in comparison to Vulkan which is front-loaded with complexities such as marshalling of structures, by-reference types, callbacks for the diagnostic handler, etc.

We start by creating a throwaway application that first loads the native library:

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

1. Lookup the symbol for the method by name.
2. Build a `FunctionDescriptor` specifying the method signature.
3. Combine both of these to link a `MethodHandle` to the native method.
4. Invoke the method with an array of the appropriate arguments.

This is wrapped up into a quick-and-dirty helper:

```java
private Object invoke(String name, Object[] args, MemoryLayout returnType, MemoryLayout... signature) throws Throwable {
    MemorySegment symbol = lookup.find(name).orElseThrow();
    var descriptor = returnType == null ? FunctionDescriptor.ofVoid(signature) : FunctionDescriptor.of(returnType, signature);
    MethodHandle handle = linker.downcallHandle(symbol, descriptor);
    return handle.invokeWithArguments(args);
}
```

The GLFW library can now be initialised, which in this case returns an integer success code:

```java
System.out.println("glfwInit=" + demo.invoke("glfwInit", null, JAVA_INT));
```

Vulkan support can be queried:

```java
System.out.println("glfwVulkanSupported=" + demo.invoke("glfwVulkanSupported", null, ValueLayout.JAVA_BOOLEAN));
```

The GLFW version string can also be retrieved:

```java
MemorySegment version = (MemorySegment) demo.invoke("glfwGetVersionString", null, ADDRESS);
System.out.println("glfwGetVersionString=" + version.reinterpret(Integer.MAX_VALUE).getString(0));
```

Note that the returned pointer has to be _reinterpreted_ to expand the bounds of the off-heap memory (in this case with an undefined length for a null-terminated character array).

Retrieving the supporting Vulkan extensions is also slightly complex since the API method returns a pointer to a string-array and uses a _by reference_ integer to return the size of the array as a side-effect.  The array length parameter is implemented with a `ValueLayout.JAVA_INT` memory layout:

```java
MemorySegment count = arena.allocate(JAVA_INT);
```

Invoking the extensions method returns the array pointer and the length of the array can then be extracted from the reference:

```java
MemorySegment extensions = (MemorySegment) demo.invoke("glfwGetRequiredInstanceExtensions", new Object[]{count}, ADDRESS, ADDRESS);
int length = count.get(JAVA_INT, 0);
System.out.println("glfwGetRequiredInstanceExtensions=" + length);
```

The returned pointer is reinterpreted as an array and each element is extracted and transformed to a Java string:

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

## Solution Decision

Generating the FFM bindings is relatively trivial and takes a couple of seconds:

`jextract --source \VulkanSDK\1.2.154.1\Include\vulkan\vulkan.h`

With some experience of using FFM and the `jextract` generated Vulkan API, we can attempt to extrapolate how JOVE could be refactored and make some observations.

The advantages of jextract are obvious:

* Proven and supported technology.

* Automated (in particular the FFM memory layouts for native structures).

However there are some disadvantages:

* The generated API is polluted by ugly internals such as field handles, structure sizes, helper methods, etc.

* API methods and structures are implemented in terms of FFM types or anonymous primitives.

* In particular enumerations are implemented as static integer accessors (!) with all the implied type-safety and documentation concerns that led us to abandon LWJGL in the first place.

* Means throwing away the existing code generated enumerations and structures.

Alternatively a _proxy_ implementation of a given native API could abstrac over custom FFM bindings, with a new framework to marshal between these two layers.

The advantages of this approach are:

* The native API and structures are expressed in domain terms.

* Proper enumerations.

* Retains all the existing API interfaces and code-generated enumerations and structures.

On the other hand:

* Additional development effect to implement the marshalling framework.

* Proprietary solution with the potential for unknown problems further down the line.

Despite the advantages of jextract the hybrid solution is preferred:

* Retains the large number of code generated enumerations and structures (and especially the type-safety and self-documentation aspects).

* The additional effort required to implement the proxy-based abstraction layer should be relatively simple.

* In any case the application and/or JOVE would _still_ need to transform domain types to/from the FFM equivalents even if `jextract` was chosen to generate the API.

The API generated by `jextract` will be retained as it will be a useful resource particularly for the memory layout of structures.

## Analysis

With this decision made (for better or worse) the scope of the new framework can be derived based on the above requirements.

The following table summarises the types that the framework will need to support:

| type                | layout        | data layout         | return      | by reference    |
| ----                | ------        | -----------         | ------      | ------------    |
| primitive           | primitive     | n/a                 | yes         | no              |
| String              | address       | string              | yes         | no              |
| IntEnum             | int           | n/a                 | yes         | no              |
| BitMask             | int           | n/a                 | yes         | no              |
| integer reference   | address       | int                 | no          | special         |
| pointer reference   | address       | address             | no          | special         |
| Handle              | address       | n/a                 | yes         | no              |
| NativeObject        | address       | n/a                 | no          | no              |
| NativeStructure     | address       | structure layout    | yes         | yes             |
| arrays              | address       | element layout      | special     | yes             |

Where:

* the _type_ is a Java or JOVE type.

* _layout_ is the corresponding FFM memory layout.

* _data layout_ specifies the structure of the off-heap memory pointed to by the address (if any).

* _return_ indicates whether the native type can logically be returned from a native method.

* and _by reference_ indicates method parameters that can be populated by the native layer as a by-reference side effect (see below).

From the above the following observations can be made:

1. The supported types are a mixture of primitives, built-in Java types (strings, arrays) and JOVE types.  Marshalling between these types and the corresponding FFM equivalents will therefore be the responsibility of a _native mapper_ implemented as a _companion_ class for each supported type.

2. Integer and pointer by-reference types will continue to be treated as special cases since these are already integral to JOVE (based on the existing JNA equivalents) and in particular there is no logical analog in Java for an integer-by-reference.

3. Supported reference types (indicated by `address` in the above table) compose an FFM pointer to off-heap memory which needs to be allocated at some point.  Ideally this will be handled by the framework with the memory being allocated on-demand by the mapper implementation.  This also nicely allows supported types to be instantiated _without_ a dependency on an `Arena` (which would be annoying).

4. Structures will require a `StructLayout` derived from its fields.

5. Native structures are really intended only as data carriers during marshalling, however a structure essentially becomes 'fixed' once its off-heap memory has been allocated and populated by the framework, any subsequent field modifications would be silently ignored (since the fields are public).  This feels like a problem waiting to happen, particularly if a structure is exposed to the application.  We could take the opportunity to refactor _all_ structures to properly encapsulate the fields and possibly introduce code-generated builders.  For the moment the code-generated structures will remain as-is, the framework could by changed to _always_ populate the off-heap data if this does become an issue.

6. Structures and arrays are compound types that will require 'recursive' marshalling.

7. An array can be returned from a native method, e.g. `glfwGetRequiredInstanceExtensions`.  The problem is that the _length_ of the returned array is unknown, the array itself can only be accessed once a length is provided (usually a by-reference integer in the same method).  Therefore arrays returned from the native layer _cannot_ be modelled as a Java array and instead will require custom handling.  Note that returned arrays are only used by the GLFW library.

8. GLFW makes heavy use of callbacks for device polling which will require substantial factoring to FFM upcall stubs.

9. Finally there are a couple of other edge-cases for by-reference parameters which are discussed in the next section.

Note that the notion of native mappers to transform method arguments is already sort of provided by Panama.  Native methods are implemented as general method handles which support mutating method arguments and return values out of the box.  The original intention was to make use of this functionality, unfortunately it proved very difficult in practice for all but the most trivial cases, most examples do not get any further than simple static method adapters probably for this reason.  However this is definitely an option to revisit when the refactoring process (and our understanding) is more mature.

## Reference Types

There are two edge-cases for by-reference parameters:

* A structure parameter can be returned by-reference, e.g. `vkGetPhysicalDeviceProperties` returns a `VkPhysicalDeviceProperties` instance.  An 'empty' structure instance is created by the application and the native library assumes that the off-heap memory has been allocated as required.

* Similarly for the elements of an array, e.g. `vkEnumeratePhysicalDevices` returns an array of device handles.  The application creates an empty array which is sized accordingly beforehand (generally via the _two stage invocation_ mechanism) with off-heap memory allocated for each element.  Note that memory is assumed to be a contiguous block in this case.

In both cases the Java type is instantiated by the application but is _populated_ by the native layer.  This implies that the off-heap memory is allocated by the framework but the actual data is empty (default structure fields, null array elements), with the returned data being demarshalled _after_ the method invocation.  Additionally there is presumably no point in marshalling an empty structure or array to the native layer.

The problem here is that one cannot determine from the method signature whether a parameter is being passed _by value_ (i.e. data populated by the application which is essentially immutable as far as marshalling is concerned) or is intended to be passed _by reference_ and populated by the native layer.  JNA used the rather cumbersome `ByReference` marker interface, which required _every_ structure to also have by-value and by-reference implementations, combined with fiddly special-case application code to switch behaviour depending on each use-case.

The most obvious solution is to implement a custom `@Returned` annotation to explicitly identify by-reference parameters.

## Framework

From the above the required components for the new framework are:

* A _native factory_ which is responsible for creating a proxy implementation of a native API.

* A set of _native mappers_ for the identified supported types responsible for 1. allocation of off-heap memory and 2. marshalling to/from the equivalent FFM representation.

* A _native method_ class that composes an FFM method handle and the native mappers for its parameter signature and optional return type.

* The `@Returned` annotation and logic to unmarshal a by-reference parameter after method invocation.

* Automated generation of structure memory layouts and support for marshalling structure fields.

* Handling for arrays of supported types (including by-reference array parameters).

---

# Implementation

## Overview

Integration of the new framework into JOVE requires the following changes as a bare minimum:

* Removal of the JNA library.

* Implementation of the new framework components outlined above.

* Refactoring of JOVE types that are explicitly dependant on native pointers (namely the `Handle` class).

* Implementation of replacements for basic types needed to support the framework: strings, by-reference types and enumerations.

* Refactoring of the native libraries in terms of the new framework.

* A temporary hack to remove GLFW device polling.

This 'big bang' approach should (hopefully) get the refactored JOVE code to a point where it compiles but will certainly not run.

The plan is then to iteratively reintroduce each JOVE component to a new demo application and refactor accordingly.  As already noted, Vulkan is very front-loaded with complexity, so additional framework support will be implemented as required for each component.

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

Where appropriate API methods are identified by the following filter:

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

The factory next creates an _invocation handler_ that delegates API calls to the appropriate native method instance:

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

### Native Methods

To fully implement the native method class the builder requires the method signature and return type in order to link the FFM method handle:

Looking ahead we will also need to programatically construct native methods (i.e. not using reflection) for the diagnostics handler (and possibly for other use cases).  Therefore the native method will be agnostic to the source of the method address.  This is a little more work but better separates concerns between the various collaborating classes.

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

Where the temporary `layout` method maps a Java type to its corresponding native layout:

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
        MemoryLayout m = returnType.mapper.layout();
        return FunctionDescriptor.of(m, layout);
    }
}
```

The `build` method constructs the method handle from the signature and instantiates a new instance:

```java
public NativeMethod build() {
    MemoryLayout[] layout = layout();
    FunctionDescriptor descriptor = descriptor(layout);
    MethodHandle handle = linker.downcallHandle(address, descriptor);
    return new NativeMethod(handle);
}
```

The factory helper then uses this builder to construct a native method for each API method:

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

Finally the native method is refactored to delegate to the method handle:

```java
public class NativeMethod {
    private MemoryHandle handle;
    
    public Object invoke(Object[] args) {
        return handle.invoke(args);
    }
}
```

To exercise the new framework the following demo code defines a (very) cut-down GLFW library dependant only on integer primitives:

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

This is then used to exercise the basic GLFW calls:

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
     * @return Native memory layout
     */
    MemoryLayout layout();
    
    /**
     * Marshals the given value to its native representation.
     * @param value         Value to marshal
     * @param context       Native context
     * @return Native value
     */
    Object marshal(T value, NativeContext context);
}
```

Where the `NativeContext` composes the services required to allocate off-heap memory during marshalling:

```java
public record NativeContext(SegmentAllocator allocator, NativeMapperRegistry registry)
```

The framework can now be made configurable by the introduction of a _registry_ of supported native mappers:

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

The native mapper for a given type can be looked up from the registry:

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

A native type can also be unmarshalled from a native method with a return value:

```java
interface ReturnMapper<T, R> {
    /**
     * Unmarshals a native return value.
     * @param value Native return value
     * @return Return value
     */
    Object unmarshal(R value);
}
```

The native method is first extended to also compose mappers for the method signature and the optional return type:

```java
public class NativeMethod {
    private final MethodHandle handle;
    private final NativeMapper[] signature;
    private final ReturnMapper returns;
}    
```

And the `invoke` method is modified to marshal the arguments before invocation and to unmarshal the return value:

```java
public Object invoke(Object[] args, NativeContext context) {
    Object[] actual = marshal(args, context);
    Object result = handle.invoke(actual);
    return unmarshalReturnValue(result);
}
```

Which delegates to the following helper to marshal the arguments:

```java
private Object[] marshal(Object[] args, NativeContext context) {
    if(args == null) {
        return null;
    }

    Object[] mapped = new Object[args.length];
    for(int n = 0; n < mapped.length; ++n) {
        if(args[n] != null) {
            mapped[n] = signature[n].marshal(args[n], context);
        }
    }

    return mapped;
}
```

And similarly for the return value:

```java
private Object unmarshalReturnValue(Object value) {
    if(returns == null) {
        return null;
    }
    else {
        return returns.returns().get().unmarshal(value);
    }
}
```

The builder is refactored accordingly to lookup the mapper for each parameter (and similarly for the return value):

```java
public Builder parameter(Class<?> type) {
    NativeMapper<?> mapper = registry.mapper(type).orElseThrow(...);
    signature.add(mapper);
    return this;
}
```

In the builder the memory layout of the method signature is now derived from the mappers:

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
    if(returns == null) {
        return FunctionDescriptor.ofVoid(layout);
    }
    else {
        return FunctionDescriptor.of(returns.layout(), layout);
    }
}
```

### Null

Thus far the framework assumes all marshalled values are non-null, which is fine for primitives but needs to be explicitly handled for reference types.

A second method is added to the native mapper which marshals a `null` domain value:

```java
default Object marshallNull(Class<?> extends T> type) {
    return MemorySegment.NULL;
}
```

A helper method is added to the context class to switch accordingly:

```java
public Object marshal(NativeMapper mapper, Object value, Class<?> type) {
    if(value == null) {
        return mapper.marshallNull(type);
    }
    else {
        return mapper.marshal(value, this);
    }
}
```

Finally the marshalling of the return value is also extended to handle null values:

```java
private Object marshalReturnValue(Object value) {
    if(returns == null) {
        return null;
    }
    else
    if(MemorySegment.NULL.equals(value)) {
        return null;
    }
    else {
        ...
    }
}
```

This should slightly simplify the implementation of the various native mappers and hopefully makes the logic more explicit.

### Integration

/////////////////////

TODO....

To support the cut-down GLFW library a default registry factory method is added:

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
    public Object marshal(T value, NativeContext __) {
        return value;
    }

    @Override
    public Object unmarshal(R value, Class<?> type) {
        return value;
    }
}
```

/////////////////////

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
public static final class HandleNativeMapper extends AbstractNativeMapper<Handle> implements ReturnMapper<Handle, MemorySegment> {
    public HandleNativeMapper() {
        super(Handle.class);
    }

    @Override
    public MemorySegment marshal(Handle handle, NativeContext context) {
        return handle.address;
    }

    @Override
    public Handle unmarshal(MemorySegment address, Class<? extends Handle> type) {
        return new Handle(address);
    }
}
```

### Reference Types

The replacement for a by-reference integer is as follows:

```java
public final class IntegerReference {
    private MemorySegment address;

    public int value() {
        if(address == null) {
            return 0;
        }
        else {
            return address.get(JAVA_INT, 0);
        }
    }
}
```

And the companion native mapper allocates the off-heap memory as required:

```java
public static final class IntegerReferenceNativeMapper extends AbstractNativeMapper<IntegerReference> {
    public IntegerReferenceNativeMapper() {
        super(IntegerReference.class);
    }

    @Override
    public MemorySegment marshal(IntegerReference ref, NativeContext context) {
        if(ref.address == null) {
            ref.address = context.allocator().allocate(JAVA_INT);
        }
        return ref.address;
    }

    @Override
    public MemorySegment marshalNull(Class<? extends IntegerReference> type) {
        throw new UnsupportedOperationException();
    }
}
```

The by-reference pointer type and companion mapper are implemented similarly:

```java
public final class PointerReference {
    private MemorySegment address;

    public Handle handle() {
        if(address == null) throw new IllegalStateException(...);
        MemorySegment handle = pointer.address().get(ADDRESS, 0);
        return new Handle(handle);
    }

    public static final class PointerReferenceNativeMapper extends AbstractNativeMapper<PointerReference> {
        public PointerReferenceNativeMapper() {
            super(PointerReference.class, ADDRESS);
        }

        @Override
        public MemorySegment marshal(IntegerReference ref, NativeContext context) {
            if(ref.address == null) {
                ref.address = context.allocator().allocate(ADDRESS);
            }
            return ref.address;
        }

        ...
    }
}
```

Notes:

* These types are enforced to be non-null and cannot be returned from a native method.

* The integer reference may be extended to support all primitive types, for the moment only integers are required to support GLFW and Vulkan.

### Strings

A Java string is the only non-JOVE type required to support the new framework (other than primitives and arrays), therefore the native mapper is a stand-alone class rather than a companion in this case:

```java
public final class StringNativeMapper extends AbstractNativeMapper<String> implements ReturnMapper<String, MemorySegment> {
    public StringNativeMapper() {
        super(String.class, ValueLayout.ADDRESS);
    }

    @Override
    public MemorySegment marshal(String str, NativeContext context) {
        ...
    }

    @Override
    public String unmarshal(MemorySegment address, Class<? extends String> type) {
        return address.reinterpret(Integer.MAX_VALUE).getString(0);
    }
}
```

The mapper caches strings that have been marshalled to the native layer:

```java
public MemorySegment marshal(String str, NativeContext context) {
    var allocator = context.allocator();
    return cache.computeIfAbsent(str, allocator::allocateFrom);
}
```

Cached entries are removed if the off-heap memory has dropped out of scope:

```java
private final WeakHashMap<String, MemorySegment> cache = new WeakHashMap<>() {
    @Override
    public MemorySegment get(Object key) {
        MemorySegment address = super.get(key);
        if(address == null) {
            return null;
        }
        else
        if(!address.scope().isAlive()) {
            remove(key);
            return null;
        }
        else {
            return address;
        }
    }
};
```

Eventually the cache will be re-implemented using soft references (to reduce memory consumption) which will also likely require synchronisation.

### Enumerations

Integer enumerations are slightly different to the other JOVE types in that the values are reference types but the native representation is a primitive integer.  Additionally the mapper needs to account for default or `null` enumeration constants, implying that it must also be privy to the actual _target_ type of a method parameter or return value.  Therefore the `marshalNull` and `unmarshal` methods are altered to provide the target type.

Otherwise the mapper essentially clones the existing JNA type converter:

```java
public class IntEnumNativeMapper extends AbstractNativeMapper<IntEnum> implements ReturnMapper<IntEnum, Integer> {
    public IntEnumNativeMapper() {
        super(IntEnum.class);
    }

    @Override
    public MemoryLayout layout(Class<? extends IntEnum> type) {
        return ValueLayout.JAVA_INT;
    }

    @Override
    public Integer marshal(IntEnum e, NativeContext context) {
        return e.value();
    }

    @Override
    public Integer marshalNull(Class<? extends IntEnum> type) {
        return ReverseMapping
            .get(type)
            .defaultValue()
            .value();
    }

    @Override
    public IntEnum unmarshal(Integer value, Class<? extends IntEnum> type) {
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

To support the target type the native method is yet again refactored by the introduction of the following new adapter type:

```java
private static class NativeType {
    private final Class<?> type;
    private final NativeMapper<?> mapper;
    private final ReturnMapper returnMapper;
}
```

This composes the native mapper and the target type for _both_ method parameters and the return type and allows the code to tidied up slightly.

The return mapper can also be conveniently retrieved _once_ in the constructor:

```java
NativeType(Class<?> type, NativeMapper<?> mapper, boolean returns) {
    ...
    if(returns) {
        this.returnMapper = (ReturnMapper) mapper;
    }
    else {
        this.returnMapper = null;
    }
}
```

The mapper for an enumeration bit mask is relatively trivial since the 'default' value is always zero and the actual target type has no relevance:

```java
public class BitMaskNativeMapper extends AbstractNativeMapper<BitMask> implements ReturnMapper<BitMask, Integer> {
    public BitMaskNativeMapper() {
        super(BitMask.class);
    }

    @Override
    public MemoryLayout layout(Class<? extends BitMask> type) {
        return ValueLayout.JAVA_INT;
    }

    @Override
    public Integer marshal(BitMask value, NativeContext context) {
        return value.bits();
    }

    @Override
    public Integer marshalNull(Class<? extends BitMask> type) {
        return 0;
    }

    @Override
    public BitMask<?> unmarshal(Integer value, Class<? extends BitMask> type) {
        return new BitMask<>(value);
    }
}
```

## Integration

### Overview

With JNA removed and the basics of the new framework in place the Vulkan API can be refactored accordingly which entails:

* A global find-and-replace for the by-reference types.

* Adding the newly implemented native mappers to the default registry.

* Temporarily hacking out the GLFW device polling code.

This initial refactoring work went much more smoothly than anticipated, with only a handful of compilation problems that were deferred until later in the process (which are covered below).  The existing code did a decent job of abstracting over JNA.

The approach from this point is to instantiate the Vulkan library and tackle each JOVE component in order starting with the `Instance` domain object.

First the instance library definition is refactored:

```java
interface Library {
    int     vkCreateInstance(VkInstanceCreateInfo pCreateInfo, Handle pAllocator, PointerReference pInstance);
    void    vkDestroyInstance(Instance instance, Handle pAllocator);
    int     vkEnumerateInstanceExtensionProperties(String pLayerName, IntegerReference pPropertyCount, @Returned VkExtensionProperties[] pProperties);
    int     vkEnumerateInstanceLayerProperties(IntegerReference pPropertyCount, @Returned VkLayerProperties[] pProperties);
    Handle  vkGetInstanceProcAddr(Instance instance, String pName);
}
```

Note that structure array parameters are now explicitly specified as an array, previously these parameters were represented by the _first_ structure element due to the way that JNA treated arrays.  For the moment arrays are supported by a temporary native mapper implementation that does nothing.

The memory layout for the required structures will be hand-crafted until we are confident that the requirements for refactoring the code generator are clear.

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

However the demo will fail since it requires marshalling support for structures, the subject of the next section.

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
public static class StructureNativeMapper extends AbstractNativeMapper<NativeStructure> implements ReturnMapper<NativeStructure, MemorySegment> {
    private record Entry(StructLayout layout, List<FieldMapping> mappings) {
    }

    private final Map<Class<? extends NativeStructure>, Entry> entries = new HashMap<>();
    private final NativeMapperRegistry registry;

    public StructureNativeMapper() {
        super(NativeStructure.class);
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
public MemorySegment marshal(NativeStructure structure, NativeContext context) {
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
        m.marshal(structure, address, context);
    }
}
```

The process of marshalling each structure field is:

1. Retrieve the field value from the structure.

2. Marshal the value via the native mapper.

3. Set the native value in the off-heap memory.

This is performed by the following method on the field mapping class:

```java
void marshal(NativeStructure structure, MemorySegment address, NativeContext context) {
    Object value = field.get(structure);
    Object actual = context.marshal(mapper, value, field.getType());
    handle.set(address, 0L, actual);
}
```

In the reverse operation where a structure is returned from a native method, a new instance is created and the fields are copied from the off-heap address:

```java
public Object unmarshal(MemorySegment address, Class<? extends NativeStructure> type) {
    // Create new structure
    var structure = type.getDeclaredConstructor().newInstance();
    Entry entry = entry(structure);

    // Populate structure from off-heap memory
    MemorySegment pointer = address.reinterpret(entry.layout.byteSize());
    for(FieldMapping m : entry.mappings) {
        m.unmarshal(pointer, structure);
    }

    return structure;
}
```

Which delegates to the corresponding field mapping method:

```
void unmarshal(MemorySegment address, NativeStructure structure) {
    Object value = handle.get(address, 0L);
    Object actual = mapper.unmarshal(value, field.getType());
    set(structure, actual);
}
```

Notes:

* Marshalling is recursive since structures can be a graph of objects.

* As things stand a structure is essentially immutable once it has been marshalled to the native layer, since the `populate` method is only invoked _once_ when the off-heap memory is allocated.  Native structures are really only intended to be transient carrier objects during an API call, however it is possible to modify structure data after it has been marshalled (which would be silently ignored).  This is an outstanding issue (or possibly a requirement) to be addressed in the future.

### Instance

The `Instance` domain class can now be refactored along with the following temporary modifications:

* The diagnostics handler is temporarily removed.

* Similarly the `function` method that retrieves the function pointers for handlers.

* Extensions and validation layers are uninitialised pending array support.

The next step is to refactor the two structures used to configure the instance which requires:

* Refactoring the structures to inherit the new `NativeStucture` type.

* Removing any legacy JNA artifacts such the `FieldOrder` annotation or `ByReference` markers.

* Declaration of the hand-crafted FFM memory layouts.

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

These layouts were essentially copied from the equivalent code generated by the `jextract` tool earlier.  The code generator will also need to track the byte alignment of each structure and inject padding as required.

In the demo application the Vulkan instance should now be successfully instantiated and configured using the new framework.

A lot of work to get to the stage JOVE was at several years previous!

### Diagnostic Handler

Refactoring the diagnostic handler presents a few new challenges:

* The native methods to create and destroy a handler must be created programatically, since the address is a _function pointer_ instead of a symbol looked up from the native library.

* Ideally the handler methods will reuse the existing native framework, in particular the support for structures when creating the handler and unmarshalling of diagnostic reports.

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

This is used to retrieve and invoke the function pointer to create the handler:

```java
private Handle create(VkDebugUtilsMessengerCreateInfoEXT info) {
    Handle create = instance.function("vkCreateDebugUtilsMessengerEXT");
    NativeMethod method = create(create.address());
    PointerReference ref = invoke(method, info);
    return ref.handle();
}
```

The native method is constructed from the function pointer:

```java
private NativeMethod create(MemorySegment address) {
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
    Vulkan.check((int) invoke(method, args, registry));
    return ref;
}
```

Which delegates to the following helper:

```java
private static Object invoke(NativeMethod method, Object[] args, NativeMapperRegistry registry) {
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
    invoke(destroy, args, registry);
}
```

This approach nicely reuses the existing framework rather than having to implement more FFM code from first principles, and was the reason for the native method being intentionally agnostic to the source of the function address earlier.

To build the callback a new method is added to the existing class that provides the memory _address_ of the callback method:

```java
private static class MessageCallback {
    private final Consumer<Message> consumer;
    private final StructureNativeMapper mapper;

    MessageCallback(Consumer<Message> consumer, NativeMapperRegistry registry) {
        this.consumer = consumer;
        this.mapper = new StructureNativeMapper(registry);
    }

    MemorySegment address() {
        var signature = MethodType.methodType(boolean.class, int.class, int.class, MemorySegment.class, MemorySegment.class);
        MethodHandle handle = MethodHandles.lookup().findVirtual(MessageCallback.class, "message", signature);
        return link(handle.bindTo(this));
    }
}
```

Note that the method handle is bound to the callback _instance_ in order to delegate diagnostic reports to a specific handler.

This handle is then linked to an up-call stub with a method signature matching the `message` method:

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

The last required change is to unmarshal the diagnostic report received by the `message` callback before delegating to the message handler:

```java
public boolean message(int severity, int typeMask, MemorySegment pCallbackData, MemorySegment pUserData) {
    // Transform the message properties
    var types = new BitMask<VkDebugUtilsMessageType>(typeMask).enumerate(TYPE);
    var level = SEVERITY.map(severity);

    // Unmarshal the message structure
    var data = mapper.unmarshal(pCallbackData, VkDebugUtilsMessengerCallbackData.class);

    // Handle message
    Message message = new Message(level, types, (VkDebugUtilsMessengerCallbackData) data);
    consumer.accept(message);

    return false;
}
```

Diagnostic handlers can now be attached to the instance as before, which is important given the invasive changes being made to JOVE.  A good test of the message callback is to temporarily remove the code that releases the attached handlers when the instance is destroyed, which should result in Vulkan complaining about orphaned objects.

---

mapping:

field               ret ref field   layout          data            native
primitive           x   -   x       primitive       n/a             primitive
string              x   -   x       address         n/a             char* char[] byte[]
enum                x   -   x       int             n/a             int (enum)
int ref             -   *   -       address         int             &int
ptr ref             -   *   -       address         address         ptr
handle              x   -   x       address         n/a             ptr
array[T]            -   x   x       address         component       ???
handle[]            -   x   ?       address         address         ptr-ptr-array
structure           x   x   x       address         layout          structure
structure[]         -   x   x       address         layout[]        structure[] ptr-structure-array
primitive array     x   ?   x       address         ???             primitive[]
blob                ?   x   ?       address         ???             ptr

R = method return type
ref = by-reference parameter
field - structure field

special cases:

array[any] cannot be returned from a method => wrapper
int/ptr ref can *only* be used as by-reference parameters
primitives/enum cannot be by-reference
immutable cannot be by-reference (string, handle) => final classes?

marshal to-native:
-                   marshal             allocate            null
primitive           primitive           n/a                 -
string              address             allocateFrom()      x
enum                value()             n/a                 default
int ref             address             address(int)        -
ptr ref             address             address             -
handle              address()           n/a                 x
array[T]            address             n * component       x
handle[]            address             n * address         x
structure           fields              layout              x
structure[]         fields              n * layout          x
primitive array     ???                 ???                 x
blob                ???                 ???                 x

unmarshal return value:
-                   unmarshal           null
primitive           primitive           -
string              getString()         x
enum                reverse             default
handle              address             x
structure           fields              x

unmarshal by-reference parameter:
-                   unmarshal
array[T]            n * component
handle[]            n * address
structure           fields
structure[]         n * fields
primitive array     ???
blob                ???

=>

- collapse all un/marshal methods to one type and get rid of all the nasty instanceof/casts/checks
- maybe add characteristics?
- NativeMapper specified as Object *but* Abstract<T> => implementation are type-safe but usage is Object based?
- too many: mapper, NativeType, FieldMapping, adapter
- parameter mapper = extends mapper + target type, by-ref annotation
- return mapper = extends mapper + return type
- structure field mapper extends mapper + field, byte offset/size (remove handles altogether?)
- array component mapper?
