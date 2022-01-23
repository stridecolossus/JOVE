---
title: Pipeline Improvements
---

---

## Contents

- [Overview](#overview)
- [Push Constants](#push-constants)
- [Pipeline Derivation](#pipeline-derivation)
- [Pipeline Cache](#pipeline-cache)
- [Specialisation Constants](#specialisation-constants)
- [Queries](#queries)

---

## Overview

This chapter introduces various improvements to the pipeline and shader code used in the terrain tesselation demo:

* The addition of _push constants_ as an alternative and more efficient means of updating the view matrices.

* Enhancements to the pipeline builder to support _pipeline derivation_ for similar rendering use-cases.

* A _pipeline cache_ to improve the time taken to create pipelines.

* A builder for _specialisation constants_ to parameterise shader configuration.

---

## Push Constants

### Push Constant Range

Push constants are used to send data to shaders with some constraints:

* The maximum amount of data is usually relatively small (specified by the `maxPushConstantsSize` of the `VkPhysicalDeviceLimits` structure).

* Push constants are updated and stored within the command buffer itself.

* Push constants have alignment restrictions, see [vkCmdPushConstants](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/vkCmdPushConstants.html).

We start with a _push constant range_ which specifies a portion of the push constants and the shader stages where that data can be used:

```java
public record PushConstantRange(int offset, int size, Set<VkShaderStage> stages) {
    public int length() {
        return offset + size;
    }

    void populate(VkPushConstantRange range) {
        range.stageFlags = IntegerEnumeration.mask(stages);
        range.size = size;
        range.offset = offset;
    }
}
```

The _offset_ and _size_ of a push constant range must be a multiple of four bytes which is validated in the constructor:

```java
public PushConstantRange {
    ...
    validate(offset);
    validate(size);
}

static void validate(int size) {
    if((size % 4) != 0) throw new IllegalArgumentException(...);
}
```

The builder for the pipeline layout is modified to include a list of push constant ranges:

```java
public static class Builder {
    ...
    private final List<PushConstantRange> ranges = new ArrayList<>();

    public Builder add(PushConstantRange range) {
        ranges.add(range);
        return this;
    }
}
```

The ranges are populated in the usual manner:

```java
public PipelineLayout build(DeviceContext dev) {
    ...
    info.pushConstantRangeCount = ranges.size();
    info.pPushConstantRanges = StructureHelper.pointer(ranges, VkPushConstantRange::new, PushConstantRange::populate);
    ...
}
```

Note that multiple ranges can be specified which allows the application to update some or all of the push constants at different shader stages and also enables the hardware to perform optimisations.

In the `build` method we also determine the overall size of the push constants and associated shaders stages (which are added to the pipeline layout constructor):

```java
// Determine overall size of the push constants data
int max = ranges
    .stream()
    .mapToInt(PushConstantRange::length)
    .max()
    .orElse(0);

// Check that overall size is supported by the hardware
if(max > dev.limits().maxPushConstantsSize) throw new IllegalArgumentException(...);

// Enumerate pipeline stages
Set<VkShaderStage> stages = ranges
    .stream()
    .map(PushConstantRange::stages)
    .flatMap(Set::stream)
    .collect(toSet());

// Create layout
return new PipelineLayout(layout.getValue(), dev, max, stages);
```

Note that the size of the data buffer is validated against the hardware limit which is often quite small (256 bytes on the development environment).

These new properties are also used to validate the push constant update command which is addressed next.

### Update Command


Push constants are backed by a data buffer updated using a new command:

```java
public class PushConstantUpdateCommand implements Command {
    private final PipelineLayout layout;
    private final int offset;
    private final ByteBuffer data;
    private final int stages;

    @Override
    public void execute(VulkanLibrary lib, Buffer buffer) {
        data.rewind();
        lib.vkCmdPushConstants(buffer, layout, stages, offset, data.limit(), data);
    }
}
```

Notes:

* The data buffer is rewound before updates are applied, generally Vulkan seems to automatically rewind buffers as required (e.g. for updating the uniform buffer) but not in this case.

* The constructor applies validation (not shown) to verify alignments, buffer sizes, etc.

The new API method is added to the library for the pipeline layout:

```java
void vkCmdPushConstants(Buffer commandBuffer, PipelineLayout layout, int stageFlags, int offset, int size, ByteBuffer pValues);
```

The constructor is public but we also provide a builder:

```java
public static class Builder {
    private int offset;
    private ByteBuffer data;
    private final Set<VkShaderStage> stages = new HashSet<>();

    ...
    
    public PushConstantUpdateCommand build(PipelineLayout layout) {
        return new PushConstantUpdateCommand(layout, offset, data, stages);
    }
}
```

We provide builder methods to update all the push constants or an arbitrary _slice_ of the backing data buffer:

```java
public Builder data(ByteBuffer data, int offset, int size) {
    this.data = data.slice(offset, size);
    return this;
}
```

And the following convenience method to update a slice specified by a corresponding range:

```java
public Builder data(ByteBuffer data, PushConstantRange range) {
    return data(data, range.offset(), range.size());
}
```

Finally we add a convenience factory method to create a backing buffer appropriate to the pipeline layout:

```java
public static ByteBuffer data(PipelineLayout layout) {
    return BufferHelper.allocate(layout.max());
}
```

And a second helper to update the entire buffer:

```java
public static PushConstantUpdateCommand of(PipelineLayout layout) {
    ByteBuffer data = data(layout);
    return new PushConstantUpdateCommand(layout, 0, data, layout.stages());
}
```

### Integration

To use the push constants in the demo application the uniform buffer is first replaced with the following layout declaration in the vertex shader:

```glsl
layout(push_constant) uniform Matrices {
    mat4 model;
    mat4 view;
    mat4 projection;
};
```

In the pipeline configuration we remove the uniform buffer and replace it with a single push constant range sized to the three matrices:

```java
@Bean
PipelineLayout layout(DescriptorLayout layout) {
    int len = 3 * Matrix.IDENTITY.length();

    return new PipelineLayout.Builder()
        .add(layout)
        .add(new PushConstantRange(0, len, Set.of(VkShaderStage.VERTEX)))
        .build(dev);
}
```

Next we create a command to update the whole of the push constants buffer:

```java
@Bean
public static PushUpdateCommand update(PipelineLayout layout) {
    return PushUpdateCommand.of(layout);
}
```

In the camera configuration the push constants are updated once per frame:

```java
@Bean
public Task matrix(PushUpdateCommand update) {
    ByteBuffer data = update.data();
    return () -> {
        data.rewind();
        Matrix.IDENTITY.buffer(data);
        cam.matrix().buffer(data);
        projection.buffer(data);
    };
}
```

Finally the update command is added to the render sequence before starting the render pass.

---

## Pipeline Derivation

### Overview

The wireframe terrain model is useful for visually testing the tesselation shader but we would like to be able to toggle between filled and wireframe modes.  The _polygon mode_ is a property of the rasterizer pipeline stage which implies the demo needs _two_ pipelines to switch between modes.

As things stand we could configure two separate pipeline builders which would be identical except for the polygon mode, but this would require the common configuration to be replicated.  To avoid code duplication a _single_ builder could be used to create each pipeline in two separate operations, where the second pipeline _overrides_ the polygon mode.

However we note the following: 

1. The API method allows multiple pipelines to be created in one operation but the current implementation is limited to a single instance.

2. Vulkan supports _derivative_ pipelines which provide a hint to the hardware that a derived (or child) pipeline shares common properties with its parent, potentially improving performance when the pipelines are instantiated and when switching bindings in the render sequence.

To support all these use cases functionality will be added to the pipeline builder to support derivative pipelines and to allow configuration to be overridden.

### Derivative Pipelines

A pipeline that allows derivatives (i.e. the parent) is identified by a flag at instantiation-time:

```java
public static class Builder {
    private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
    private Handle baseHandle;

    ...

    public Builder flag(VkPipelineCreateFlag flag) {
        this.flags.add(flag);
        return this;
    }
    
    public Builder allowDerivatives() {
        return flag(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
    }
}
```

Note that the set of `flags` is also added to the pipeline domain object.

Vulkan offers two methods to derive pipelines:

1. Derive from an _existing_ pipeline instance.

2. Create an array of pipelines where derived pipelines specify the parent by _index_ within the array.

Note that these two mechanisms are mutually exclusive.

A pipeline derived from an existing parent instance is specified by the following new method on the builder:

```java
public Builder derive(Pipeline base) {
    if(!base.flags().contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) {
        throw new IllegalArgumentException(...);
    }
    baseHandle = base.handle();
    derivative(this);
    return this;
}
```

Where `derivative` is a trivial helper:

```java
private static void derivative(Builder builder) {
    builder.flags.add(VkPipelineCreateFlag.DERIVATIVE);
}
```

The base pipeline is populated in the `build` method:

```java
info.basePipelineHandle = baseHandle;
```

### Pipeline Peers

To support the second method to derive pipelines by index we first implement a new factory method to create multiple pipelines:

```java
public static List<Pipeline> build(List<Builder> builders, DeviceContext dev) {
    // Build array of descriptors
    VkGraphicsPipelineCreateInfo[] array = StructureHelper.array(builders, VkGraphicsPipelineCreateInfo::new, Builder::populate);

    // Allocate pipelines
    VulkanLibrary lib = dev.library();
    Pointer[] handles = new Pointer[array.length];
    check(lib.vkCreateGraphicsPipelines(dev, null, array.length, array, null, handles));

    // Create pipelines
    return IntStream
        .range(0, array.length)
        .mapToObj(n -> create(handles[n], list.get(n), dev))
        .collect(toList());
}
```

Where `create` is a local helper to instantiate each pipeline:

```java
private static Pipeline create(Pointer handle, Builder builder, DeviceContext dev) {
    return new Pipeline(handle, dev, builder.layout, builder.flags);
}
```

The code in the existing `build` method that constructs a pipeline descriptor is wrapped up into the `populate` helper, and the method is refactored using the new factory as a convenience for constructing a single pipeline.

Next the pipeline builder is extended to support derivation of a pipeline during construction:

```java
public static class Builder {
    private Builder base;
    private int baseIndex = -1;

    public Builder derive() {
        // Validate
        if(!flags.contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) throw new IllegalStateException(...);
    
        // Create derived builder
        Builder builder = new Builder();
        derivative(builder);
        builder.base = this;
        
        // Clone pipeline properties
        ...
        
        return builder;
    }
}
```

Note that this method creates a __new__ builder for the derivative pipeline with a reference to the its parent.

In the new build method we populate the `basePipelineIndex` in the pipeline descriptor by looking up the index from the array.  As a convenience we first ensure that the given list of builders also contains the parents:

```java
public static List<Pipeline> build(List<Builder> builders, PipelineCache cache, DeviceContext dev) {
    List<Builder> list = new ArrayList<>(builders);
    builders
        .stream()
        .map(b -> b.base)
        .filter(Objects::nonNull)
        .filter(Predicate.not(list::contains))
        .forEach(list::add);
            
    ...
}
```

Next the index of the parent is determined for derived pipelines:

```java
for(Builder b : list) {
    if(b.base != null) {
        b.baseIndex = list.indexOf(b.base);
        assert b.baseIndex >= 0;
    }
}
```

And finally the index is populated in the descriptor:

```java
if(base != null) {
    if(baseHandle != null) throw new IllegalArgumentException(...);
    assert baseIndex >= 0;
}
info.basePipelineHandle = baseHandle;
info.basePipelineIndex = baseIndex;
```

To allow the properties of a pipeline to be overridden the `derive` method clones from the parent builder:

```java
public Builder derive() {
    ...
    
    // Clone pipeline properties
    builder.layout = layout;
    builder.pass = pass;
    builder.flags.addAll(flags);

    // Clone pipeline stages
    for(ShaderStageBuilder b : shaders.values()) {
        builder.shaders.put(b.stage, new ShaderStageBuilder(b));
    }
    builder.input.init(input);
    builder.assembly.init(assembly);
    ...

    return builder;
}
```

The `init` method is added to the nested pipeline stage builders to clone the configuration.  We are also forced to refactor some of the nested builders that previously operated directly on the underlying Vulkan descriptor to support cloning (since JNA structures cannot easily be copied).

### Integration

For the demo we now have several alternatives available to create the pipelines.  We opt to derive the wireframe pipeline and exercise the code that instantiates multiple pipelines:

```java
@Bean
public List<Pipeline> pipelines(...) {
    // Init main pipeline
    var pipeline = new Pipeline.Builder()
    ...

    // Derive wireframe pipeline
    var wireframe = pipeline
        .derive()
        .rasterizer()
            .polygon(VkPolygonMode.LINE)
            .build();

    // Build pipelines
    return Pipeline.Builder.build(List.of(pipeline, wireframe), dev);
}
```

The configuration class for the render sequence is next modified as follows:
* Inject the _list_ of pipelines.
* Add an index member.
* Add a toggle handler that flips the index.
* Expose the handler as a bean.
* Bind it to the space bar.

Since the render sequence is recorded per frame we can now switch the pipeline at runtime.  Cool.

---

## Pipeline Cache

A _pipeline cache_ stores the results of pipeline construction and can be reused between pipelines and between runs of an application, allowing the hardware to possibly optimise pipeline construction.  

The domain object is relatively trivial:

```java
public class PipelineCache extends AbstractVulkanObject {
    @Override
    protected Destructor<PipelineCache> destructor(VulkanLibrary lib) {
        return lib::vkDestroyPipelineCache;
    }
}
```

A cache object is instantiated using a factory method:

```java
public static PipelineCache create(DeviceContext dev, byte[] data) {
    // Build create descriptor
    VkPipelineCacheCreateInfo info = new VkPipelineCacheCreateInfo();
    if(data != null) {
        info.initialDataSize = data.length;
        info.pInitialData = BufferHelper.buffer(data);
    }

    // Create cache
    VulkanLibrary lib = dev.library();
    PointerByReference ref = dev.factory().pointer();
    check(lib.vkCreatePipelineCache(dev, info, null, ref));

    // Create domain object
    return new PipelineCache(ref.getValue(), dev);
}
```

Where `data` is the previously persisted cache (the data itself is platform specific).

The following method retrieves the cache data after construction of the pipeline (generally before application termination):

```java
public ByteBuffer data() {
    DeviceContext dev = super.device();
    VulkanFunction<ByteBuffer> func = (count, data) -> dev.library().vkGetPipelineCacheData(dev, this, count, data);
    IntByReference count = dev.factory().integer();
    return VulkanFunction.invoke(func, count, BufferHelper::allocate);
}
```

Caches can also be merged such that a single instance can be reused across multiple pipelines:

```java
public void merge(Collection<PipelineCache> caches) {
    DeviceContext dev = super.device();
    VulkanLibrary lib = dev.library();
    check(lib.vkMergePipelineCaches(dev, this, caches.size(), NativeObject.array(caches)));
}
```

Finally we add a new API library for the cache:

```java
int  vkCreatePipelineCache(DeviceContext device, VkPipelineCacheCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pPipelineCache);
int  vkMergePipelineCaches(DeviceContext device, PipelineCache dstCache, int srcCacheCount, Pointer pSrcCaches);
int  vkGetPipelineCacheData(DeviceContext device, PipelineCache cache, IntByReference pDataSize, ByteBuffer pData);
void vkDestroyPipelineCache(DeviceContext device, PipelineCache cache, Pointer pAllocator);
```

To persist a cache we implement a simple loader:

```java
public static class Loader implements ResourceLoader<InputStream, PipelineCache> {
    private final DeviceContext dev;

    @Override
    public InputStream map(InputStream in) throws IOException {
        return in;
    }

    @Override
    public PipelineCache load(InputStream in) throws IOException {
        byte[] data = in.readAllBytes();
        return create(dev, data);
    }

    public void write(PipelineCache cache, OutputStream out) throws IOException {
        byte[] array = BufferHelper.array(cache.data());
        out.write(array);
    }
}
```

TODO - cache manager, file source helper, integration

---

## Specialisation Constants

The new shaders contain a number of hard coded parameters (such as the tesselation factor and height scalar) which would ideally be programatically configured (possibly from a properties file).

Additionally in general we would like to centralise common or shared parameters to avoid hard-coding the same information in multiple locations or having to replicate shaders with different parameters.

Vulkan provides _specialisation constants_ for these requirements which are be used to parameterise a shader when it is instantiated.

The descriptor for a set of specialisation constants is constructed using a new factory method on the shader class:

```java
public static VkSpecializationInfo constants(Map<Integer, Object> constants) {
    // Skip if empty
    if(constants.isEmpty()) {
        return null;
    }

    var info = new VkSpecializationInfo();
    ...

    return info;
}
```

Where _constants_ is a map of arbitrary values indexed by identifier.

The method first transforms the map to a list:

```java
List<Constant> table = constants.entrySet().stream().map(Constant::new).collect(toList());
```

Where each constant is an instance of a local class constructed from each map entry:

```java
class Constant {
    private final int id;
    private final Object value;
    private final int size;
    private int offset;

    private Constant(Entry<Integer, Object> entry) {
        this.id = entry.getKey();
        this.value = entry.getValue();
        this.size = size();
    }
}
```

The _size_ of each constant is determined as follows:

```java
private int size() {
    if(value instanceof Integer) {
        return Integer.BYTES;
    }
    else
    if(value instanceof Float) {
        return Float.BYTES;
    }
    else
    if(value instanceof Boolean) {
        return Integer.BYTES;
    }
    else {
        throw new IllegalArgumentException(...);
    }
}
```

Notes:

* Only scalar and boolean values are supported.

* Booleans are represented as integer values.

The _offset_ of each constant within the data buffer is determined by the following simple loop (which also calculates the total length of the buffer):

```java
int size = 0;
for(Constant e : table) {
    e.offset = size;
    size += e.size;
}
```

Each constant has a descriptor:

```java
var info = new VkSpecializationInfo();
info.mapEntryCount = constants.size();
info.pMapEntries = StructureHelper.pointer(table, VkSpecializationMapEntry::new, Constant::populate);
```

Which is populated from the local class:

```java
private void populate(VkSpecializationMapEntry entry) {
    entry.constantID = id;
    entry.offset = offset;
    entry.size = size;
}
```

Finally the data buffer for the specialisation constants is allocated and filled:

```java
info.dataSize = size;
info.pData = BufferHelper.allocate(size);
for(Constant entry : table) {
    entry.append(info.pData);
}
```

Where `append` is another helper on the local class:

```java
private void append(ByteBuffer buffer) {
    if(value instanceof Integer n) {
        buffer.putInt(n);
    }
    else
    if(value instanceof Float f) {
        buffer.putFloat(f);
    }
    else
    if(value instanceof Boolean b) {
        final VulkanBoolean bool = VulkanBoolean.of(b);
        buffer.putInt(bool.toInteger());
    }
    else {
        assert false;
    }
}
```

The specialisation constants are applied to a shader during pipeline configuration:

```java
public class ShaderStageBuilder {
    private VkSpecializationInfo constants;
    
    ...
    
    public ShaderStageBuilder constants(VkSpecializationInfo constants) {
        this.constants = notNull(constants);
        return this;
    }
 
    void populate(VkPipelineShaderStageCreateInfo info) {
        ...
        info.pSpecializationInfo = constants;
    }
}
```

For example in the evaluation shader we can replace the hard-coded height scale with the following constant declaration:

```glsl
layout(constant_id=1) const float HeightScale = 2.5;
```

Note that the constant also has a default value if it is not explicitly configured by the application.

The set of constants used in both tesselation shaders is initialised in the pipeline configuration class:

```java
class PipelineConfiguration {
    private final VkSpecializationInfo constants = Shader.constants(Map.of(0, 20f, 1, 2.5f));
}
```

Finally the relevant shaders are parameterised when the pipeline is constructed, for example:

```java
shader(VkShaderStage.TESSELLATION_EVALUATION)
    .shader(evaluation)
    .constants(constants)
    .build()
```

---

## Queries

TODO
occlusion queries
integration

```java
interface Library {
    int  vkCreateQueryPool(DeviceContext device, VkQueryPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pQueryPool);
    void vkDestroyQueryPool(DeviceContext device, Pool queryPool, Pointer pAllocator);
    void vkCmdResetQueryPool(Command.Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount);
    void vkCmdBeginQuery(Command.Buffer commandBuffer, Pool queryPool, int query, int flags);
    void vkCmdEndQuery(Command.Buffer commandBuffer, Pool queryPool, int query);
    void vkCmdWriteTimestamp(Command.Buffer commandBuffer, VkPipelineStage pipelineStage, Pool queryPool, int query);
    int  vkGetQueryPoolResults(DeviceContext device, Pool queryPool, int firstQuery, int queryCount, long dataSize, ByteBuffer pData, long stride, int flags);
    void vkCmdCopyQueryPoolResults(Command.Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, int flags);
}
```


---

## Summary

In this chapter we implemented the following framework enhancements:

* Push constants

* Pipeline derivation

* A persistent pipeline cache.

* Support for specialisation constants.

* A new framework to support pipeline queries.
