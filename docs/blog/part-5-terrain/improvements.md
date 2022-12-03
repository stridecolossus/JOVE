    ---
title: Improvements
---

---

## Contents

TODO - move pipeline derivation and queries to TERRAIN chapter

This chapter introduces various improvements to the pipeline and shader code used in the terrain tesselation demo.

- [Pipeline Derivation](#pipeline-derivation)
- [Pipeline Cache](#pipeline-cache)
- [Queries](#queries)

---

## Pipeline Derivation

### Overview

The wireframe terrain model is useful for visually testing the tesselation shader but we would like to be able to toggle between filled and wireframe modes.  The _polygon mode_ is a property of the rasterizer pipeline stage which implies the demo needs _two_ pipelines to switch between modes.  As things stand we _could_ use the same builder to create two pipelines with the second overriding the polygon mode.

However Vulkan supports _derivative_ pipelines which provide a hint to the hardware that a derived (or child) pipeline shares common properties with its parent, potentially improving performance when the pipelines are instantiated and when switching bindings in the render sequence.

### Derivative Pipelines

A pipeline that allows derivatives (i.e. the parent) is identified by a flag at instantiation-time:

```java
public static class Builder {
    private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
    private Handle base;

    ...

    public Builder flag(VkPipelineCreateFlag flag) {
        this.flags.add(flag);
        return this;
    }
    
    public Builder parent() {
        return flag(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
    }
}
```

Where `parent` configures a pipeline that can be derived from.

Note that the set of `flags` is also added to the pipeline domain object.

Vulkan offers two mutually exclusive methods to derive pipelines:

1. Derive from an _existing_ pipeline instance.

2. Create an array of pipelines where derivative _peer_ pipelines are specified by index with the array.

A pipeline derived from an existing parent instance is specified by the following new method on the builder:

```java
public Builder derive(Pipeline base) {
    check(base.flags());
    this.base = base.handle();
    return this;
}
```

Where the local `derive` helper validates that the parent supports derivatives and marks the pipeline as a derivative:

```java
private void derive(Set<VkPipelineCreateFlag> flags) {
    if(!flags.contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES)) throw new IllegalStateException(...);
    this.flags.add(VkPipelineCreateFlag.DERIVATIVE);
}
```

The base pipeline is populated in the `build` method:

```java
info.basePipelineHandle = base;
```

### Pipeline Peers

To support peer derivatives the pipeline builder is refactored to create an array of pipelines:

```java
public static List<Pipeline> build(List<Builder> builders, PipelineCache cache, DeviceContext dev) {
    // Build array of descriptors
    VkGraphicsPipelineCreateInfo[] array = StructureHelper.array(builders, VkGraphicsPipelineCreateInfo::new, Builder::populate);

    // Allocate pipelines
    VulkanLibrary lib = dev.library();
    Pointer[] handles = new Pointer[array.length];
    check(lib.vkCreateGraphicsPipelines(dev, cache, array.length, array, null, handles));

    // Create pipelines
    Pipeline[] pipelines = new Pipeline[array.length];
    for(int n = 0; n < array.length; ++n) {
        Builder builder = builders.get(n);
        pipelines[n] = new Pipeline(handles[n], dev, builder.layout, builder.flags);
    }
    return Arrays.asList(pipelines);
}
```

And the existing `build` method becomes a helper to instantiate a single pipeline.

A derivative peer pipeline can now be configured by a second `derive` overload:

```java
public Builder derive(Builder parent) {
    if(parent == this) throw new IllegalStateException();
    derive(parent.flags);
    this.parent = notNull(parent);
    return this;
}
```

The peer index is patched in the `build` method after the array has been populated:

```java
for(int n = 0; n < array.length; ++n) {
    Builder parent = builders.get(n).parent;
    if(parent == null) {
        continue;
    }
    int index = builders.indexOf(parent);
    if(index == -1) throw new IllegalArgumentException();
    array[n].basePipelineIndex = index;
}
```

Note that this second derivative approach currently does __not__ clone the pipeline builder configuration implying there may still be a large amount of code duplication (pipeline layout, the various stages, etc).  This may be something for future consideration.

### Integration

In the demo we now have several alternatives to switch between polygon modes, the configuration is modified to derive the wire-frame alternative from the existing pipeline:

```java
@Bean
public List<Pipeline> pipelines(...) {
    // Init main pipeline
    Pipeline pipeline = new Pipeline.Builder()
        ...
        .build();

    // Derive wireframe pipeline
    Pipeline wireframe = pipeline
        ...
        .derive(pipeline)
        .rasterizer()
            .polygon(VkPolygonMode.LINE)
            .build()
        .build();
}
```

In the render configuration the beans for the pipeline and command sequence are modified to generate new instances on each invocation rather than the previous singleton:

```java
private final AtomicInteger index = new AtomicInteger();

@Bean()
public RenderSequence(Pipeline[] pipelines) {
}
```

TODO 

And a toggle handler is bound to the space bar to allow the application to switch between the pipelines at runtime. Cool.

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
    void vkCmdBeginQuery(Buffer commandBuffer, Pool queryPool, int query, BitMask<VkQueryControlFlag> flags);
    void vkCmdEndQuery(Buffer commandBuffer, Pool queryPool, int query);
    void vkCmdWriteTimestamp(Buffer commandBuffer, VkPipelineStage pipelineStage, Pool queryPool, int query);
    int  vkGetQueryPoolResults(DeviceContext device, Pool queryPool, int firstQuery, int queryCount, long dataSize, ByteBuffer pData, long stride, BitMask<VkQueryResultFlag> flags);
    void vkCmdCopyQueryPoolResults(Buffer commandBuffer, Pool queryPool, int firstQuery, int queryCount, VulkanBuffer dstBuffer, long dstOffset, long stride, BitMask<VkQueryResultFlag> flags);
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
    info.pipelineStatistics = BitMask.reduce(stats);

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
            int mask = BitMask.reduce(flags);
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
private BitMask<VkQueryResultFlag> validate() {
    // Validate query range
    if(start + count > pool.slots) {
        throw new IllegalArgumentException();
    }

    // Validate stride
    long multiple = flags.contains(VkQueryResultFlag.LONG) ? Long.BYTES : Integer.BYTES;
    if((stride % multiple) != 0) {
        throw new IllegalArgumentException();
    }

    // Build flags mask
    return BitMask.reduce(flags);
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

## Summary

In this chapter we implemented the following framework enhancements:

* Push constants

* Pipeline derivation

* A persistent pipeline cache.

* Support for specialisation constants.

* A new framework to support pipeline queries.

* Support for device limits.

