---
title: Descriptor Sets
---

---

## Contents

- [Overview](#overview)
- [Descriptor Sets](#descriptor-sets)
- [Integration](#integration)

---

## Overview

The final piece of functionality we need to support texture sampling is the _descriptor set_.

- A descriptor set is comprised of the _resources_ used by the pipeline during rendering (samplers, uniform buffers, etc).

- Sets are allocated from a _descriptor set pool_ (similar to commands).

- A _descriptor set layout_ specifies the resource bindings for a descriptor set.

We will also need to slightly refactor the pipeline layout which is used to configure the descriptor sets in the rendering process.

> Descriptor sets are one of the more complex aspects of a Vulkan application (or at least they proved to be for the author).  We have attempted to come up with a design that reflects the flexibility that the Vulkan designers were clearly aiming for while hopefully providing a relatively developer-friendly API.

---

## Descriptor Sets

### Layout

The first-cut class outline is as follows:

```java
public class DescriptorSet implements NativeObject {
    public static class Layout extends AbstractVulkanObject {
    }

    public static class Pool extends AbstractVulkanObject {
    }
}
```

We tackle the layout first:

```java
public static class Layout extends AbstractVulkanObject {
    private final Map<Integer, Binding> bindings;

    @Override
    protected Destructor<Layout> destructor(VulkanLibrary lib) {
        return lib::vkDestroyDescriptorSetLayout;
    }
}
```

A layout is comprised of a number of _resource bindings_ defined as follows:

```java
public static record Binding(int binding, VkDescriptorType type, int count, Set<VkShaderStage> stages) {
}
```

Where _count_ is the size of the resource (which can be an array) and _stages_ specifies where the resource is used in the pipeline.

We implement a convenience builder for the binding and add a populate method for the binding descriptor (used below):

```java
private void populate(VkDescriptorSetLayoutBinding info) {
    info.binding = binding;
    info.descriptorType = type;
    info.descriptorCount = count;
    info.stageFlags = IntegerEnumeration.mask(stages);
}
```

A layout is created by a factory method:

```java
public static Layout create(LogicalDevice dev, List<Binding> bindings) {
    // Init layout descriptor
    VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
    info.bindingCount = bindings.size();
    info.pBindings = StructureHelper.pointer(bindings, VkDescriptorSetLayoutBinding::new, Binding::populate);

    // Allocate layout
    VulkanLibrary lib = dev.library();
    PointerByReference handle = dev.factory().pointer();
    check(lib.vkCreateDescriptorSetLayout(dev, info, null, handle));

    // Create layout
    return new Layout(handle.getValue(), dev, bindings);
}
```

### Pool

Next we implement the descriptor set pool:

```java
public static class Pool extends AbstractVulkanObject {
    private final Set<DescriptorSet> sets = new HashSet<>();
    private final int max;

    @Override
    protected Destructor<Pool> destructor(VulkanLibrary lib) {
        return lib::vkDestroyDescriptorPool;
    }

    @Override
    protected void release() {
        sets.clear();
    }
}
```

The _max_ member configures the maximum number of descriptors that can be allocated from the pool, allowing the implementation to potentially optimise and pre-allocate the pool.

Descriptor sets are requested from the pool as follows:

```java
public synchronized List<DescriptorSet> allocate(List<Layout> layouts) {
    // Check pool size
    int size = layouts.size();
    if(sets.size() + size > max) {
        throw new IllegalArgumentException(...);
    }

    // Build allocation descriptor
    final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
    info.descriptorPool = this.handle();
    info.descriptorSetCount = size;
    info.pSetLayouts = NativeObject.toArray(layouts);

    // Allocate descriptors sets
    DeviceContext dev = this.device();
    VulkanLibrary lib = dev.library();
    Pointer[] handles = new Pointer[size];
    check(lib.vkAllocateDescriptorSets(dev, info, handles));
    ...
}
```

The returned handles are transformed to the domain object:

```java
// Create descriptor sets
IntFunction<DescriptorSet> ctor = index -> {
    Handle handle = new Handle(handles[index]);
    return new DescriptorSet(handle, layouts.get(index));
};
var allocated = IntStream
    .range(0, handles.length)
    .mapToObj(ctor)
    .collect(toList());

// Record sets allocated by this pool
sets.addAll(allocated);
return allocated;
```

Note that descriptor sets are managed by the pool and are automatically released when the pool is destroyed.  However the pool also tracks the allocated sets for validation purposes.

We also implement the following methods to release descriptor sets back to the pool:

```java
public synchronized void free(Collection<DescriptorSet> sets) {
    ...
    DeviceContext dev = this.device();
    check(dev.library().vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.toArray(sets)));
}

public synchronized void free() {
    if(sets.isEmpty()) throw new IllegalArgumentException(...);
    DeviceContext dev = this.device();
    check(dev.library().vkResetDescriptorPool(dev, this, 0));
    sets.clear();
}
```

### Builder

To construct a pool we provide a builder:

```java
public static class Builder {
    private final Map<VkDescriptorType, Integer> pool = new HashMap<>();
    private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
    private Integer max;
}
```

The _pool_ is a table of the number of each type of descriptor set available in the pool:

```java
public Builder add(VkDescriptorType type, int count) {
    pool.put(type, count);
    return this;
}
```

The build method transforms the table to an array and populates the descriptor for the pool:

```java
VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
info.flags = IntegerEnumeration.mask(flags);
info.poolSizeCount = entries.size();
info.pPoolSizes = StructureCollector.toPointer(pool.entrySet(), VkDescriptorPoolSize::new, Builder::populate);
info.maxSets = max;
```

Which uses the following helper:

```java
private static void populate(Map.Entry<VkDescriptorType, Integer> entry, VkDescriptorPoolSize size) {
    size.type = entry.getKey();
    size.descriptorCount = entry.getValue();
}
```

The size of the table is also logically verified against the _max_ property (if specified):

```java
// Determine logical maximum number of sets that can be allocated
int limit = pool
    .values()
    .stream()
    .mapToInt(Integer::intValue)
    .max()
    .orElseThrow(() -> new IllegalArgumentException(...));

// Initialise or validate the maximum number of sets
if(max == null) {
    max = limit;
}
else {
    if(limit > max) {
        throw new IllegalArgumentException(...);
    }
}
```

Finally we invoke the API to allocate the pool and create the domain object:

```java
VulkanLibrary lib = dev.library();
PointerByReference handle = dev.factory().pointer();
check(lib.vkCreateDescriptorPool(dev, info, null, handle));
return new Pool(handle.getValue(), dev, max);
```

### Resources

A descriptor set is essentially a mutable map of _resources_ indexed by the bindings in the layout.

We first define a descriptor set resource:

```java
public interface Resource {
    /**
     * @return Descriptor type
     */
    VkDescriptorType type();

    /**
     * Populates the write descriptor for this resource.
     * @param write Write descriptor
     */
    void populate(VkWriteDescriptorSet write);
}
```

We can now implement a factory for the descriptor set resource for the sampler:

```java
public Resource resource(View texture) {
    return new Resource() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.COMBINED_IMAGE_SAMPLER;
        }

        @Override
        public void populate(VkWriteDescriptorSet write) {
            // Create sampler descriptor
            var info = new VkDescriptorImageInfo();
            info.imageLayout = VkImageLayout.SHADER_READ_ONLY_OPTIMAL;
            info.sampler = Sampler.this.handle();
            info.imageView = texture.handle();

            // Add to write descriptor
            write.pImageInfo = info;
        }
    };
}
```

Note that the same `VkWriteDescriptorSet` descriptor is used for __all__ types of resource, in this case we populate the `pImageInfo` field for the texture sampler.

We can now finally implement the descriptor set domain object itself:

```java
public class DescriptorSet implements NativeObject {
    private final Handle handle;
    private final Layout layout;
    private final Map<Binding, Entry> entries;

    public DescriptorSet(Handle handle, Layout layout) {
        this.handle = notNull(handle);
        this.layout = notNull(layout);
        this.entries = layout.bindings.values().stream().collect(toMap(Function.identity(), Entry::new));
    }
}
```

Note that the `entries` map is initialised in the constructor by instantiating a new `Entry` for each binding (which also results in an immutable map).

An _entry_ is a local class that stores the descriptor set resource for a given binding:

```java
private class Entry {
    private final Binding binding;
    private Resource res;
    private boolean dirty = true;

    private Entry(Binding binding) {
        this.binding = notNull(binding);
    }
}
```

Which is a mutable property of the descriptor set:

```java
public void set(Binding binding, Resource res) {
    Entry entry = entry(binding);
    entry.res = notNull(res);
    entry.dirty = true;
}
```

### Updates

Applying the updated resources to the descriptor sets is slightly complicated:

1. Each modified descriptor set requires a `VkWriteDescriptorSet` structure.

2. This includes a pointer to a structure for the resource (which we have implemented as the `populate` method of the resource interface).

3. Updates are then applied as a batch operation.

We first add a helper to the descriptor set to retrieve the modified entries:

```java
private Stream<Entry> modified() {
    return entries
        .values()
        .stream()
        .filter(Entry::isDirty);
}
```

And the following code is added to `Entry` to populate a write descriptor for that modified resource:

```java
void populate(VkWriteDescriptorSet write) {
    // Validate
    if(res == null) {
        throw new IllegalStateException(...);
    }

    // Init write descriptor
    write.dstBinding = binding.index();
    write.descriptorType = binding.type();
    write.dstSet = DescriptorSet.this.handle();
    write.descriptorCount = 1;
    write.dstArrayElement = 0;

    // Populate resource
    res.populate(write);
}
```

To apply a batch of updates we first enumerate the modified resources and transform the results to an array of write descriptors:

```java
public static int update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
    var writes = descriptors
        .stream()
        .flatMap(DescriptorSet::modified)
        .peek(Entry::clear)
        .collect(StructureHelper.collector(VkWriteDescriptorSet::new, Entry::populate));
}
```

Notes:

* The write descriptor is dependant on the binding, the resource __and__ the descriptor set itself (hence `Entry` is a local class with a reference to the parent descriptor set).

* The `dirty` flag is reset as a side-effect of the update method (not pretty but works).

finally we invoke the API to apply the batch of updates:

```java
// Ignore if nothing to update
if((writes == null) || (writes.length == 0)) {
    return 0;
}

// Apply update
dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);

return writes.length;
```

### Pipeline Layout

In the previous demo we created a bare-bones pipeline layout implementation which we now flesh out to support descriptor sets.

We update the pipeline layout builder:

```java
public static class Builder {
    private final List<DescriptorSet.Layout> sets = new ArrayList<>();

    public Builder add(DescriptorSet.Layout layout) {
        sets.add(layout);
        return this;
    }
}
```

And add the following code to the build method (which were previously left as defaults):

```java
info.setLayoutCount = sets.size();
info.pSetLayouts = Handle.toArray(sets);
```

Finally we implement the following method on the descriptor set class to bind to the pipeline:

```java
public static Command bind(Pipeline.Layout layout, Collection<DescriptorSet> sets) {
    return (api, cmd) -> api.vkCmdBindDescriptorSets(
        cmd,
        VkPipelineBindPoint.GRAPHICS,
        layout,
        0,                  // First set
        sets.size(),
        NativeObject.toArray(sets),
        0,                  // Dynamic offset count
        null                // Dynamic offsets
    );
}
```

We also add an over-loaded variant for a single descriptor set instance.

### API

The API for descriptor sets is as follows:

```java
interface VulkanLibraryDescriptorSet {
    int  vkCreateDescriptorSetLayout(DeviceContext device, VkDescriptorSetLayoutCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pSetLayout);
    void vkDestroyDescriptorSetLayout(DeviceContext device, Layout descriptorSetLayout, Pointer pAllocator);

    int  vkCreateDescriptorPool(DeviceContext device, VkDescriptorPoolCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pDescriptorPool);
    void vkDestroyDescriptorPool(DeviceContext device, Pool descriptorPool, Pointer pAllocator);

    int  vkAllocateDescriptorSets(DeviceContext device, VkDescriptorSetAllocateInfo pAllocateInfo, Pointer[] pDescriptorSets);
    int  vkResetDescriptorPool(DeviceContext device, Pool descriptorPool, int flags);
    int  vkFreeDescriptorSets(DeviceContext device, Pool descriptorPool, int descriptorSetCount, Pointer pDescriptorSets);
    void vkUpdateDescriptorSets(DeviceContext device, int descriptorWriteCount, VkWriteDescriptorSet[] pDescriptorWrites, int descriptorCopyCount, VkCopyDescriptorSet[] pDescriptorCopies);

    void vkCmdBindDescriptorSets(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, PipelineLayout layout, int firstSet, int descriptorSetCount, Pointer pDescriptorSets, int dynamicOffsetCount, int[] pDynamicOffsets);
}
```

---

## Integration

We now have all the components required to apply the texture to the demo.

However we will first make a couple of improvements to move the configuration towards a proper render loop.

### Application Configuration

We first introduce a set of `ConfigurationProperties` for the demo:

```java
@Configuration
@ConfigurationProperties
public class ApplicationConfiguration {
    private String title;
    private int frames;
    private Colour col = Colour.BLACK;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getFrameCount() {
        return frames;
    }

    public void setFrameCount(int frames) {
        this.frames = frames;
    }

    public Colour getBackground() {
        return col;
    }

    public void setBackground(float[] col) {
        this.col = Colour.of(col);
    }
}
```

These properties map to the `application.properties` file:

```java
title: Rotating Cube Demo
frameCount: 2
background = 0.3, 0.3, 0.3
```

Notes:

* The properties class __must__ be a simple POJO (with old-school getters and setters) to be auto-magically populated by the framework.

* The `background` property (for the swapchain views) is a comma-separated list injected as an array.

The swapchain configuration is refactored accordingly:

```java
class PresentationConfiguration {
    @Autowired private LogicalDevice dev;
    @Autowired private ApplicationConfiguration cfg;

    @Bean
    public Swapchain swapchain(Surface surface) {
        return new Swapchain.Builder(dev, surface)
            .count(cfg.getFrameCount())
            .clear(cfg.getBackground())
            .build();
    }
}
```

We also refactor the frame buffers to a list (one per swapchain image):

```java
@Bean
public static List<FrameBuffer> buffers(Swapchain swapchain, RenderPass pass) {
    Dimensions extents = swapchain.extents();
    return swapchain
        .views()
        .stream()
        .map(view -> FrameBuffer.create(pass, extents, List.of(view)))
        .collect(toList());
}
```

Note that we will now see a validation error since the frame buffers will no longer be automatically destroyed by the container (the list does not have a `close` method).

We also refactor the application title in the window and instance configuration replacing the injected `@Value` parameter used previously.

### Descriptor Sets

We first create a new configuration class and instantiate a descriptor set layout for the texture sampler:

```java
@Configuration
public class DescriptorConfiguration {
    @Autowired private LogicalDevice dev;
    @Autowired private ApplicationConfiguration cfg;

    private final Binding binding = new Binding.Builder()
        .type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
        .stage(VkShaderStage.FRAGMENT)
        .build();

    @Bean
    public Layout layout() {
        return Layout.create(dev, List.of(binding));
    }
}
```

Next we create a descriptor set pool that can allocate two samplers (matching the number of frame buffers):

```java
@Bean
public Pool pool() {
    int count = cfg.getFrameCount();
    return new Pool.Builder()
        .add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, count)
        .max(count)
        .build(dev);
}
```

Finally we allocate a descriptor set for each frame buffer:

```java
@Bean
public List<DescriptorSet> descriptors(Pool pool, Layout layout, Sampler sampler, View texture) {
    var descriptors = pool.allocate(layout, cfg.getFrameCount());
    ...
    return descriptors;
}
```

And create a descriptor set resource for the texture sampler:

```java
Resource res = sampler.resource(texture);
```

Which is applied to each descriptor set:

```java
DescriptorSet.set(descriptors, binding, res);
DescriptorSet.update(dev, descriptors);
```

### Configuration

The descriptor set layout is registered with the pipeline:

```java
class PipelineConfiguration {
    @Bean
    PipelineLayout pipelineLayout(DescriptorSet.Layout layout) {
        return new PipelineLayout.Builder()
            .add(layout)
            .build(dev);
    }
}
```

Next we alter the render configuration to generate a _list_ of command buffers:

```java
public static List<Buffer> sequence(List<FrameBuffer> frames, Pipeline pipeline, VulkanBuffer vbo, List<DescriptorSet> sets, Pool graphics) {
    int count = frames.size();
    List<Buffer> buffers = graphics.allocate(count);

    ...

    return buffers;
}
```

A render sequence is recorded for each command buffer with a new command to bind the descriptor set:

```java
for(int n = 0; n < count; ++n) {
    FrameBuffer fb = frames.get(n);
    DescriptorSet ds = sets.get(n);
    buffers
        .get(n)
        .begin()
            .add(fb.begin())
            .add(pipeline.bind())
            .add(vbo.bindVertexBuffer())
            .add(ds.bind(pipeline.layout()))
            .add(draw)
            .add(FrameBuffer.END)
        .end();
}
```

This code is becoming a bit messy as we now have a loop iterating through three parallel lists:
- the frame buffer
- a descriptor set
- a command buffer for the render sequence 

In a later chapter we will introduce a further abstraction that composes these objects into a single list.

### Render Loop

In the render configuration we modify the code to repeatedly invoke frame rendering and presentation:

```java
long start = System.currentTimeMillis();
while(true) {
    // Stop after a second or so
    if(System.currentTimeMillis() - start > 1000) {
        break;
    }

    ...
}
```

We refactor the method signature to inject the _list_ of command buffers.  The code now looks up the render sequence for a frame given the `index` returned from the `acquire` step:

```java
Buffer frame = render.get(index);
```

Note that we could just use one command buffer since our render loop is essentially single-threaded.  However the above prepares us for properly synchronised code that utilises the multi-threaded nature of the Vulkan pipeline.

### Texture Sampling

To use the texture sampler we make the following changes to the fragment shader:

1. Add a layout declaration for a `uniform sampler2D` with the binding index we specified in the descriptor set layout.

2. Invoke the built-in `texture` function to sample the texture with the coordinate passed from the vertex shader.

The fragment shader now looks like this:

```glsl
#version 450 core

layout(binding = 0) uniform sampler2D texSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 outColor;

void main(void) {
    outColor = texture(texSampler, texCoord);
}
```

### Conclusion

If all goes well we should finally see the textured quad:

![Textured Quad](textured-quad.png)

We have jumped through a number of hoops in the last couple of chapters and therefore there is plenty to go wrong.  The validation layer will provide excellent diagnostics if the application attempts any invalid operations (and often seems to be able to work around them).  However it is very easy to specify a 'valid' pipeline that results in a black rectangle.

Here are some of the problems that we encountered:

* The image loader is very crude and somewhat brittle - the image _should_ be a `TYPE_4BYTE_BGR` which maps to the `R8G8B8A8_UNORM` Vulkan format.  It is definitely worth using the debugger to step through image loading and the complex barrier transition logic.

* Ensure the image data is being copied to the vertex buffer and not the other way around (yes we really did this).

* Check that the descriptor set layout is bound to the pipeline and added to the render sequence.

---

## Summary

In this chapter we:

- Implemented descriptor sets to support a texture sampler resource.

- Applied texture sampling to the demo application.

