    ---
title: Improvements
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
- [Device Limits](#device-limits)

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

Where `write` is a local helper method:

```java
private static void write(byte[] array, ByteBuffer bb) {
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

Finally in the same vein another new factory method is added to wrap a JNA structure:

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
            write(array, bb);
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
    return func.invoke(count, BufferHelper::allocate);
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

The terrain shaders contain a number of hard coded parameters (such as the tesselation factor and height scalar) which would ideally be programatically configured (possibly from a properties file).

Additionally in general we prefer to centralise common or shared parameters to avoid hard-coding the same information in multiple locations or having to replicate shaders for different parameters.

Vulkan provides _specialisation constants_ for these requirements which parameterise a shader when it is instantiated.

For example in the evaluation shader the hard-coded height scale is replaced with the following constant declaration:

```glsl
layout(constant_id=1) const float HeightScale = 2.5;
```

Note that the constant also has a default value if it is not explicitly configured by the application.

The descriptor for a set of specialisation constants is constructed via a new factory method:

```java
public static VkSpecializationInfo build(Map<Integer, Object> constants) {
    // Skip if empty
    if(constants.isEmpty()) {
        return null;
    }
    ...
}
```

Each constant generates a separate child descriptor:

```java
var info = new VkSpecializationInfo();
info.mapEntryCount = constants.size();
info.pMapEntries = StructureHelper.pointer(constants.entrySet(), VkSpecializationMapEntry::new, populate);
```

Where `populate` is factored out to the following function:

```java
var populate = new BiConsumer<Entry<Integer, Object>, VkSpecializationMapEntry>() {
    private int len = 0;

    @Override
    public void accept(Entry<Integer, Object> entry, VkSpecializationMapEntry out) {
        ...
    }
};
```

This function populates the descriptor for each entry and calculates the buffer offset and total length as a side-effect:

```java
// Init constant
int size = size(entry.getValue());
out.size = size;
out.constantID = entry.getKey();

// Update buffer offset
out.offset = len;
len += size;
```

Where `size` is a local helper:

```java
int size() {
    return switch(value) {
        case Integer n -> Integer.BYTES;
        case Float f -> Float.BYTES;
        case Boolean b -> Integer.BYTES;
        default -> throw new UnsupportedOperationException();
    };
}
```

The constants are then written to a data buffer:

```java
ByteBuffer buffer = BufferHelper.allocate(populate.len);
for(Object value : constants.values()) {
    switch(value) {
        case Integer n -> buffer.putInt(n);
        case Float f -> buffer.putFloat(f);
        case Boolean b -> {
            int bool = VulkanBoolean.of(b).toInteger();
            buffer.putInt(bool);
        }
        default -> throw new RuntimeException();
    }
}
```

And finally this data is added to the descriptor:

```java
info.dataSize = populate.len;
info.pData = buffer;
```

Notes:

* Only scalar and boolean values are supported.

* Booleans are represented as integer values.

Specialisation constants are configured in the pipeline:

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

The set of constants used in both tesselation shaders is initialised in the pipeline configuration class:

```java
class PipelineConfiguration {
    private final VkSpecializationInfo constants = Shader.build(Map.of(0, 20f, 1, 2.5f));
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

* and some extensions that are out-of-scope for this project.

A _query_ is generally implemented as a pair of commands that wrap a segment of a rendering sequence, except for timestamps which consist of a single atomic command.
Queries are allocated from a _query pool_ which is essentially an array of available _slots_ for the results.

After execution the results of the query can be retrieved on-demand to an arbitrary NIO buffer or copied asynchronously to a Vulkan buffer.

First a new JNA library is created for the query API:

```java
interface Library {
    int  vkCreateQueryPool(DeviceContext device, VkQueryPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pQueryPool);
    void vkDestroyQueryPool(DeviceContext device, Pool queryPool, Pointer pAllocator);
    void vkCmdResetQueryPool(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount);
    void vkCmdBeginQuery(Buffer commandBuffer, Pool queryPool, int query, int flags);
    void vkCmdEndQuery(Buffer commandBuffer, Pool queryPool, int query);
    void vkCmdWriteTimestamp(Buffer commandBuffer, VkPipelineStage pipelineStage, Pool queryPool, int query);
    int  vkGetQueryPoolResults(DeviceContext device, Pool queryPool, int firstQuery, int queryCount, long dataSize, ByteBuffer pData, long stride, int flags);
    void vkCmdCopyQueryPoolResults(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, int flags);
}
```

### Queries

The outline class for the query framework is as follows:

```java
public interface Query {
    /**
     * Default implementation for a measurement query wrapping a portion of the render sequence.
     */
    interface DefaultQuery extends Query {
    }

    /**
     * Timestamp query.
     */
    interface Timestamp extends Query {
    }

    public static class Pool extends AbstractVulkanObject {
        private final VkQueryType type;
        private final int slots;
    }
}
```

A query pool is instantiated via a factory method:

```java
public static Pool create(DeviceContext dev, VkQueryType type, int slots, VkQueryPipelineStatisticFlag... stats) {
    // Init create descriptor
    var info = new VkQueryPoolCreateInfo();
    info.queryType = notNull(type);
    info.queryCount = oneOrMore(slots);
    info.pipelineStatistics = IntegerEnumeration.reduce(stats);

    // Instantiate query pool
    PointerByReference ref = dev.factory().pointer();
    VulkanLibrary lib = dev.library();
    check(lib.vkCreateQueryPool(dev, info, null, ref));

    // Create pool
    return new Pool(ref.getValue(), dev, type, slots);
}
```

Note that the `stats` collection parameter is only valid for a `PIPELINE_STATISTICS` query.

Query instances can then be allocated from the pool, the default implementation is used to wrap a segment of the rendering sequence:

```java
public DefaultQuery query(int slot) {
    return new DefaultQuery() {
        @Override
        public Command begin(VkQueryControlFlag... flags) {
            int mask = IntegerEnumeration.reduce(flags);
            return (lib, buffer) -> lib.vkCmdBeginQuery(buffer, Pool.this, slot, mask);
        }

        @Override
        public Command end() {
            return (lib, buffer) -> lib.vkCmdEndQuery(buffer, Pool.this, slot);
        }
    };
}
```

Similarly for timestamp queries:

```java
public Timestamp timestamp(int slot) {
    return new Timestamp() {
        @Override
        public Command timestamp(VkPipelineStage stage) {
            return (lib, buffer) -> lib.vkCmdWriteTimestamp(buffer, stage, Pool.this, slot);
        }
    };
}
```

Finally query slots (or the entire pool) must be reset before execution:

```java
public Command reset(int start, int num) {
    return (lib, buffer) -> lib.vkCmdResetQueryPool(buffer, this, start, num);
}
```

Note that this command __must__ be invoked before the start of a render pass.

### Results Builder

Retrieval of the results of a query is somewhat complicated:

* There are two supported mechanisms: on-demand or asynchronous copy to a buffer.

* Multiple results can be retrieved in one operation by specifying a _range_ of query slots, i.e. essentially an array of results.

* Results can be integer or long data types.

Therefore configuration of the results is specified by a builder instantiated from the query pool:

```java
public class Pool ... {
    public ResultBuilder result() {
        return new ResultBuilder(this);
    }
}
```

The builder specifies the data type and the range of query slots to be retrieved:

```java
public static class ResultBuilder {
    private final Pool pool;
    private int start;
    private int count;
    private long stride = Integer.BYTES;
    private final Set<VkQueryResultFlag> flags = new HashSet<>();
}
```

A convenience setter is provided to initialise the results to a long data type:

```java
public ResultBuilder longs() {
    flag(VkQueryResultFlag.LONG);
    stride(Long.BYTES);
    return this;
}
```

Since query results can be retrieved using two different mechanisms the builder provides __two__ build methods.
For the case where the results are retrieved on-demand the application provides an NIO buffer:

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

The `validate` method checks that the `stride` is a multiple of the specified data type and builds the flags bit-mask:

```java
private int validate() {
    // Validate query range
    if(start + count > pool.slots) {
        throw new IllegalArgumentException(...);
    }

    // Validate stride
    long multiple = flags.contains(VkQueryResultFlag.LONG) ? Long.BYTES : Integer.BYTES;
    if((stride % multiple) != 0) {
        throw new IllegalArgumentException(...);
    }

    // Build flags mask
    return IntegerEnumeration.reduce(flags);
}
```

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

### Integration

The query framework is used in the terrain demo to further verify the tesselation process using a pipeline statistics query.

First we create a query pool that counts the number of vertices generated by the tesselator:

```java
@Bean
public Pool queryPool(LogicalDevice dev) {
    return new Pool.Builder()
        .type(VkQueryType.PIPELINE_STATISTICS)
        .statistic(VkQueryPipelineStatisticFlag.VERTEX_SHADER_INVOCATIONS)
        .slots(1)
        .build(dev);
}
```

Next a query instance is allocated from the pool:

```java
@Bean
public Query query(Query.Pool pool) {
    return pool.query(1);
}
```

The query wraps the render pass in the command sequence:

```java
buffer
    .add(pool.reset())
    .add(frame.begin())
        .add(query.begin())
            ...
        .add(query.end())
    .add(FrameBuffer.END)
```

Again note the pool is `reset` before the start of the pass.

Finally the query results are configured as a frame listener to dump the vertex count to the console:

```java
@Bean
public FrameListener queryResults() {
    ByteBuffer results = BufferHelper.allocate(Integer.BYTES);
    Consumer<ByteBuffer> consumer = pool.result().build();
    return () -> {
        consumer.accept(results);
        System.out.println(results.getInt());
    };
}
```

The terrain demo application should now output the number of the vertices generated on each frame.  Obviously this is a very crude implementation just to illustrate the query framework.

---

## Device Limits

The `VkPhysicalDeviceLimits` structure specifies various limits supported by the hardware, this is wrapped by a new helper class:

```java
public class DeviceLimits {
    private final VkPhysicalDeviceLimits limits;
    private final DeviceFeatures features;
}
```

For convenience the supported device features are also incorporated into this new class and can be enforced as required:

```java
public void require(String name) {
    if(!features.features().contains(name)) {
        throw new IllegalStateException("Feature not supported: " + name);
    }
}
```

A device limit can be queried from the structure by name:

```java
public <T> T value(String name) {
    return (T) limits.readField(name);
}
```

The reason for implementing limits by name is two-fold:

1. The `readField` approach avoids the problem of the underlying JNA structure being totally mutable.

2. It is assumed that applications will prefer to query by name rather than coding for specific structure fields.

Some device limits are a _quantised_ range of permissible values which can be retrieved by the following helper:

```java
public float[] range(String name, String granularity) {
    // Lookup range bounds
    float[] bounds = value(name);
    float min = bounds[0];
    float max = bounds[1];

    // Lookup granularity step
    float step = value(granularity);

    // Determine number of values
    int num = (int) ((max - min) / step);

    // Build quantised range
    float[] range = new float[num + 1];
    for(int n = 0; n < num; ++n) {
        range[n] = min + n * step;
    }
    range[num] = max;

    return range;
}
```

Where _name_ is the limit and _granularity_ specifies the step size, e.g. `pointSizeRange` and `pointSizeGranularity` for the range of valid point primitives.

The device limits are lazily retrieved from the logical device:

```java
public class LogicalDevice ... {
    ...
    private final Supplier<DeviceLimits> limits = new LazySupplier<>(this::loadLimits);

    private DeviceLimits loadLimits() {
        VkPhysicalDeviceProperties props = parent.properties();
        return new DeviceLimits(props.limits, features);
    }

    @Override
    public DeviceLimits limits() {
        return limits.get();
    }
}
```

For example, the builder for an indirect draw command can now validate that the command configuration is supported by the hardware:

```java
DeviceLimits limits = buffer.device().limits();
int max = limits.value("maxDrawIndirectCount");
limits.require("multiDrawIndirect");
if(count > max) throw new IllegalArgumentException(...);
```

Although the validation layer would also trap this problem when the command is _executed_ the above code applies the validation at _instantiation_ time (which may be earlier).

---

## Summary

In this chapter we implemented the following framework enhancements:

* Push constants

* Pipeline derivation

* A persistent pipeline cache.

* Support for specialisation constants.

* A new framework to support pipeline queries.

* Support for device limits.

