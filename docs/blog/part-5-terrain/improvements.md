---
title: Pipeline Improvements
---

---

## Contents

This chapter introduces various improvements to the pipeline and shader code used in the terrain tesselation demo.

- [Buffer Helper](#buffer-helper)
- [Push Constants](#push-constants)
- [Pipeline Derivation](#pipeline-derivation)
- [Pipeline Cache](#pipeline-cache)
- [Specialisation Constants](#specialisation-constants)
- [Queries](#queries)

---

## Buffer Helper

Most of the following improvements are dependant on a new utility class for NIO buffers which is introduced first.

Most Vulkan functionality that is dependant on a data buffer assumes a __direct__ NIO buffer, which is wrapped into the following convenience factory method:

```java
public final class BufferHelper {
    /**
     * Native byte order for a bufferable object.
     */
    public static final ByteOrder NATIVE_ORDER = ByteOrder.nativeOrder();

    private BufferHelper() {
    }

    public static ByteBuffer allocate(int len) {
        return ByteBuffer.allocateDirect(len).order(NATIVE_ORDER);
    }
}
```

The utility class also supports conversion of an NIO buffer to a byte array:

```java
public static byte[] array(ByteBuffer bb) {
    if(bb.isDirect()) {
        bb.rewind();
        int len = bb.limit();
        byte[] bytes = new byte[len];
        for(int n = 0; n < len; ++n) {
            bytes[n] = bb.get();
        }
        return bytes;
    }
    else {
        return bb.array();
    }
}
```

And the reverse operation to wrap an array with a buffer:

```java
public static ByteBuffer buffer(byte[] array) {
    ByteBuffer bb = allocate(array.length);
    write(array, bb);
    return bb;
}
```

Where `write` is a further utility method:

```java
public static void write(byte[] array, ByteBuffer bb) {
    if(bb.isDirect()) {
        for(byte b : array) {
            bb.put(b);
        }
    }
    else {
        bb.put(array);
    }
}
```

Note that direct NIO buffers generally do not support the optional bulk methods, hence the `isDirect` test in the `write` method.

Existing code that transforms to/from byte buffers is refactored using the new utility methods, e.g. shader SPIV code.

As a further convenience for applying updates to uniform buffers the following method can be used to insert a data element:

```java
public static void insert(int index, Bufferable data, ByteBuffer bb) {
    int pos = index * data.length();
    bb.position(pos);
    data.buffer(bb);
}
```

This is useful for buffers that are essentially an 'array' of some type of bufferable object (which we use below).

Finally in the same vein we add a new factory method to the bufferable class to wrap a JNA structure:

```java
static Bufferable of(Structure struct) {
    return new Bufferable() {
        @Override
        public int length() {
            return struct.size();
        }

        @Override
        public void buffer(ByteBuffer bb) {
            byte[] array = struct.getPointer().getByteArray(0, struct.size());
            BufferHelper.write(array, bb);
        }
    };
}
```

This allows arbitrary JNA structures to be used to populate buffers which will become useful in later chapters.

---

## Push Constants

### Push Constant Range

Push constants are an alternative and more efficient mechanism for transferring arbitrary data to shaders with some constraints:

* The maximum amount of data is usually relatively small.

* Push constants are updated and stored within the command buffer.

* Push constants have alignment restrictions.

See [vkCmdPushConstants](https://www.khronos.org/registry/vulkan/specs/1.2-extensions/man/html/vkCmdPushConstants.html).

We start with a _push constant range_ which specifies a portion of the push constants and the shader stages where that data can be used:

```java
public record PushConstantRange(int offset, int size, Set<VkShaderStage> stages) {
    public int length() {
        return offset + size;
    }

    void populate(VkPushConstantRange range) {
        range.stageFlags = IntegerEnumeration.reduce(stages);
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

In the `build` method for the pipeline layout we determine the _maximum_ length of the push ranges:

```java
int max = ranges
    .stream()
    .mapToInt(PushConstantRange::length)
    .max()
    .orElse(0);
```

This is validated (not shown) against the hardware limit specified by the `maxPushConstantsSize` of the `VkPhysicalDeviceLimits` structure, this value is usually quite small (256 bytes on our development environment).

Finally the set of shader stages is aggregated and added to the pipeline layout:

```java
Set<VkShaderStage> stages = ranges
    .stream()
    .map(PushConstantRange::stages)
    .flatMap(Set::stream)
    .collect(toSet());

return new PipelineLayout(layout.getValue(), dev, max, stages);
```

### Update Command

Push constants are backed by a data buffer which is updated using a new command:

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

TODO

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

A pipeline cache is a trivial opaque domain class:

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
    return switch(value) {
        case Integer n -> Integer.BYTES;
        case Float f -> Float.BYTES;
        case Boolean b -> Integer.BYTES;
        default -> throw new UnsupportedOperationException(...);
    };
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
void append(ByteBuffer buffer) {
    switch(value) {
        case Integer n -> buffer.putInt(n);
        case Float f -> buffer.putFloat(f);
        case Boolean b -> {
            VulkanBoolean bool = VulkanBoolean.of(b);
            buffer.putInt(bool.toInteger());
        }
        default -> throw new RuntimeException();
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

### Overview

Vulkan provides a _query_ API that allows an application to perform the following:
* _occlusion_ queries.
* retrieve pipeline statistics.
* inject timestamps into a command sequence.
* and some other extensions that are out-of-scope for this project.

A _query_ is generally implemented as a pair of commands that wrap a segment of a rendering sequence, except for timestamps which consist of a single atomic command.
Queries are allocated from a _query pool_ which is essentially an array of available _slots_ for the results (which are either integer or long values).

After execution the results of the query can be retrieved on-demand to an arbitrary NIO buffer or copied asynchronously to a `TRANSFER_DST` Vulkan buffer.

A new JNA library is added for the query API:

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

### Queries

We start with an outline class for a query and its parent pool:

```java
public class Query {
    private final int slot;
    private final Pool pool;

    public static class Pool extends AbstractVulkanObject {
        private final int slots;

        @Override
        protected Destructor<Pool> destructor(VulkanLibrary lib) {
            return lib::vkDestroyQueryPool;
        }
    }
}
```

A query instance is created by the following factory method on the pool:

```java
public Query query(int slot) {
    if(slot >= slots) throw new IllegalArgumentException(...);
    return new Query(slot, this);
}
```

Commands to perform the query can then be allocated from this instance:

```java
public Command begin(VkQueryControlFlag... flags) {
    int mask = IntegerEnumeration.reduce(flags);
    return (lib, buffer) -> lib.vkCmdBeginQuery(buffer, pool, slot, mask);
}

public Command end() {
    return (lib, buffer) -> lib.vkCmdEndQuery(buffer, pool, slot);
}

public Command timestamp(VkPipelineStage stage) {
    return (lib, buffer) -> lib.vkCmdWriteTimestamp(buffer, stage, pool, slot);
}
```

### Query Pool

A query pool is created as normal via a builder:

```java
public static class Builder {
    private VkQueryType type;
    private int slots = 1;
    private final Set<VkQueryPipelineStatisticFlag> stats = new HashSet<>();
    
    public Pool build(DeviceContext dev) {
        ...
    }
}
```

The `build` method first populates a descriptor for the pool:

```java
public Pool build(DeviceContext dev) {
    var info = new VkQueryPoolCreateInfo();
    info.queryType = type;
    info.queryCount = slots;
    ...
}
```

The `stats` property of the builder is only relevant for a `PIPELINE_STATISTICS` query:

```java
if(type == VkQueryType.PIPELINE_STATISTICS) {
    if(stats.isEmpty()) throw new IllegalArgumentException(...);
    info.pipelineStatistics = IntegerEnumeration.reduce(stats);
}
else {
    if(!stats.isEmpty()) throw new IllegalArgumentException(...);
}
```

Finally the pool is allocated via the API and wrapped by the domain object:

```java
// Instantiate query pool
PointerByReference ref = dev.factory().pointer();
VulkanLibrary lib = dev.library();
check(lib.vkCreateQueryPool(dev, info, null, ref));

// Create pool
return new Pool(ref.getValue(), dev, slots);
```

### Query Results

Retrieval of the results of one-or-more queries is slightly complicated:

* There are two supported mechanisms: on-demand or asynchronous copy to a buffer.

* Multiple results can be retrieved in one operation by specifying a _range_ of query slots, i.e. essentially an array of results.

* Results can be integer or long data types.

Therefore configuration of the results is also specified by a builder that is instantiated from the query pool:

```java
public class Pool ... {
    public ResultBuilder result() {
        return new ResultBuilder(this);
    }
}
```

The builder specifies the data type and the range of slots to be populated:

```java
public static class ResultBuilder {
    private final Pool pool;
    private int start;
    private int count;
    private long stride = Integer.BYTES;
    private final Set<VkQueryResultFlag> flags = new HashSet<>();
}
```

We also provide a convenience method to initialise the results to a long data type, which requires a specific query flag as well as setting the `stride` field appropriately:

```java
public ResultBuilder longs() {
    flag(VkQueryResultFlag.LONG);
    stride(Long.BYTES);
    return this;
}
```

Since query results can be retrieved using two different mechanisms the builder provides __two__ build methods, 
for the case where the results are retrieved on-demand the application provides an NIO buffer:

```java
public Consumer<ByteBuffer> build() {
    int mask = validate();
    DeviceContext dev = pool.device();
    Library lib = dev.library();

    return buffer -> {
        check(lib.vkGetQueryPoolResults(dev, pool, start, count, size, buffer, stride, mask));
    };
}
```

The `validate` method (not shown) checks that `stride` is a multiple of the specified data type and builds the flags bit-mask.

The second variant generates a command that is injected into the render sequence to asynchronously copy the results to a given Vulkan buffer:

```java
public Command build(VulkanBuffer buffer, long offset) {
    buffer.require(VkBufferUsageFlag.TRANSFER_DST);
    int mask = validate();
    return (lib, cmd) -> {
        lib.vkCmdCopyQueryPoolResults(cmd, pool, start, count, buffer, offset, stride, mask);
    };
}
```

Finally the query slots are reset before each execution using the following command factory on the pool class:

```java
public Command reset(int start, int num) {
    if(start + num > slots) throw new IllegalArgumentException(...);
    return (lib, buffer) -> lib.vkCmdResetQueryPool(buffer, this, start, num);
}
```

Note that the slots __must__ be reset before the query begins and this command __must__ be invoked before the start of a render pass.

### Integration

The query framework is used in the terrain demo to further verify the tesselation process using a pipeline statistics query.

First we create a query pool that counts the number of vertices generated by the tesselator:

```java
@Bean
public Query.Pool queryPool(LogicalDevice dev) {
    return new Pool.Builder()
        .type(VkQueryType.PIPELINE_STATISTICS)
        .statistic(VkQueryPipelineStatisticFlag.VERTEX_SHADER_INVOCATIONS)
        .slots(1)
        .build(dev);
}
```

And a query instance allocated from the pool:

```java
@Bean
public Query query(Query.Pool pool) {
    return pool.query(1);
}
```

The query wraps the render pass in the command sequence:

```java
buffer
    .add(query.pool().reset(0, 1))
    .add(frame.begin())
        .add(query.begin())
            ...
        .add(query.end())
    .add(FrameBuffer.END)
```

Again note that the reset command is invoked before the start of the render pass.

Finally we configure retrieval of the query results as a frame listener task that dumps the vertex count to the console:

```java
@Bean
public Task queryResults() {
    ByteBuffer results = BufferHelper.allocate(Integer.BYTES);
    Consumer<ByteBuffer> consumer = pool.result().build();
    return () -> {
        consumer.accept(results);
        System.out.println(results.getInt());
    };
}
```

The terrain demo application should now output the number of the vertices generated on each frame.  Obviously this is a very crude implementation to illustrate the query framework.

Note that frame listener tasks are invoked _before_ the render sequence so the query results will likely fail on the first invocation with a `VK_NOT_READY` result code.

---

## Summary

In this chapter we implemented the following framework enhancements:

* Push constants

* Pipeline derivation

* A persistent pipeline cache.

* Support for specialisation constants.

* A new framework to support pipeline queries.
