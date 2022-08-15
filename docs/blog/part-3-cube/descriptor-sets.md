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

The final piece of functionality needed to support texture sampling is the _descriptor set_.

* A descriptor set is comprised of the _resources_ used by the pipeline during rendering (samplers, uniform buffers, etc).

* Sets are allocated from a _descriptor set pool_ (similar to commands).

* A _descriptor set layout_ specifies the resource bindings for a descriptor set.

The pipeline layout will also be refactored to configure the descriptor sets used by the pipeline.

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

The layout is tackled first:

```java
public static class Layout extends AbstractVulkanObject {
    private final Collection<Binding> bindings;

    @Override
    protected Destructor<Layout> destructor(VulkanLibrary lib) {
        return lib::vkDestroyDescriptorSetLayout;
    }
}
```

A layout is comprised of a number of indexed _resource bindings_ defined as follows:

```java
public record Binding(int binding, VkDescriptorType type, int count, Set<VkShaderStage> stages)
```

Where _count_ is the size of the resource (which can be an array) and _stages_ specifies where the resource is used in the pipeline.

A convenience builder is added to the binding and the following method populates the corresponding Vulkan structure:

```java
private void populate(VkDescriptorSetLayoutBinding info) {
    info.binding = binding;
    info.descriptorType = type;
    info.descriptorCount = count;
    info.stageFlags = IntegerEnumeration.reduce(stages);
}
```

Layouts are created by a factory method:

```java
public static Layout create(LogicalDevice dev, List<Binding> bindings) {
    // Init layout descriptor
    var info = new VkDescriptorSetLayoutCreateInfo();
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

Next up is the descriptor set pool:

```java
public static class Pool extends AbstractVulkanObject {
    @Override
    protected Destructor<Pool> destructor(VulkanLibrary lib) {
        return lib::vkDestroyDescriptorPool;
    }
}
```

Descriptor sets are requested from the pool as follows:

```java
public Collection<DescriptorSet> allocate(List<Layout> layouts) {
    // Build allocation descriptor
    int count = layouts.size();
    var info = new VkDescriptorSetAllocateInfo();
    info.descriptorPool = this.handle();
    info.descriptorSetCount = count;
    info.pSetLayouts = NativeObject.toArray(layouts);

    // Allocate descriptors sets
    DeviceContext dev = this.device();
    VulkanLibrary lib = dev.library();
    Pointer[] handles = new Pointer[count];
    check(lib.vkAllocateDescriptorSets(dev, info, handles));
    ...
}
```

The returned handles are transformed to the domain object:

```java
List<DescriptorSet> allocated = IntStream
    .range(0, count)
    .mapToObj(n -> new DescriptorSet(handles[n], layouts.get(n)))
    .toList();
```

Descriptor sets can also be programatically released back to the pool:

```java
public void free(Collection<DescriptorSet> sets) {
    DeviceContext dev = this.device();
    check(dev.library().vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.array(sets)));
}
```

Finally the pool can be reset to recycle all descriptor sets back to the pool:

```java
public void reset() {
    final DeviceContext dev = this.device();
    check(dev.library().vkResetDescriptorPool(dev, this, 0));
}
```

Note that allocated descriptor sets are automatically released when the pool is destroyed.

### Builder

A builder is used to construct and configure a descriptor set pool:

```java
public static class Builder {
    private final Map<VkDescriptorType, Integer> pool = new HashMap<>();
    private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
    private Integer max;
}
```

The _pool_ member is a table specifying the available number of each type of descriptor set:

```java
public Builder add(VkDescriptorType type, int count) {
    pool.put(type, count);
    return this;
}
```

The _max_ member configures the maximum number of descriptors that can be allocated from the pool, allowing the implementation to potentially optimise and pre-allocate the pool.

In the `build` method the maximum possible number of descriptor sets is derived from the table:

```java
int limit = pool
    .values()
    .stream()
    .mapToInt(Integer::intValue)
    .max()
    .orElseThrow(() -> new IllegalArgumentException(...));
```

And the table is logically verified against this limit, or initialised to the table size if unspecified:

```java
if(max == null) {
    max = limit;
}
else
if(limit > max) {
    throw new IllegalArgumentException(...);
}
```

Next the Vulkan descriptor for the pool is configured:

```java
var info = new VkDescriptorPoolCreateInfo();
info.flags = IntegerEnumeration.reduce(flags);
info.poolSizeCount = entries.size();
info.pPoolSizes = StructureCollector.toPointer(pool.entrySet(), VkDescriptorPoolSize::new, Builder::populate);
info.maxSets = max;
```

Which uses the following helper to populate each entry in the table:

```java
private static void populate(Entry<VkDescriptorType, Integer> entry, VkDescriptorPoolSize size) {
    size.type = entry.getKey();
    size.descriptorCount = entry.getValue();
}
```

Finally the pool is instantiated:

```java
VulkanLibrary lib = dev.library();
PointerByReference handle = dev.factory().pointer();
check(lib.vkCreateDescriptorPool(dev, info, null, handle));
return new Pool(handle.getValue(), dev, max);
```

### Resources

A descriptor set is essentially a mutable map of _resources_ indexed by the bindings in the layout.

A resource is defined as follows:

```java
public interface DescriptorResource {
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

A factory can now be implemented to create a resource for a sampler:

```java
public DescriptorResource resource(View texture) {
    return new Resource() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.COMBINED_IMAGE_SAMPLER;
        }

        @Override
        public void populate(VkWriteDescriptorSet write) {
            var info = new VkDescriptorImageInfo();
            info.imageLayout = VkImageLayout.SHADER_READ_ONLY_OPTIMAL;
            info.sampler = Sampler.this.handle();
            info.imageView = view.handle();
            write.pImageInfo = info;
        }
    };
}
```

Note that the same structure is used to update __all__ types of resource, in this case the `pImageInfo` field is populated for the texture sampler.

Finally the descriptor set domain object can be implemented:

```java
public class DescriptorSet implements NativeObject {
    private final Handle handle;
    private final Layout layout;
    private final Map<Binding, DescriptorResource> entries;
    private final Set<Binding> modified = new HashSet<>();
}
```

All bindings are initialised as _modified_ in the constructor:

```java
public DescriptorSet(...) {
    ...
    modified.addAll(layout.bindings());
}
```

A resource can now be bound to the descriptor set:

```java
public void set(Binding binding, DescriptorResource res) {
    // Check binding belongs to this set
    if(!layout.bindings().values().contains(binding)) {
        throw new IllegalArgumentException(...);
    }

    // Check expected resource
    if(binding.type() != res.type()) {
        throw new IllegalArgumentException(...);
    }

    // Update entry
    entries.put(binding, res);
    modified.add(binding);
}
```

### Updates

Applying the updated resources to the descriptor sets is slightly complicated:

1. Each modified descriptor set requires a separate `VkWriteDescriptorSet` structure.

2. The Vulkan descriptor for each update is dependant on the descriptor set, the binding _and_ the resource.

3. Updates are applied as a batch operation.

First a transient type is implemented that composes a modified entry:

```java
private record Modified(DescriptorSet set, Binding binding, DescriptorResource res) {
    private Modified {
        if(res == null) throw new IllegalStateException(...);
    }
}
```

The modified resources can then be enumerated from a descriptor set:

```java
private Stream<Modified> modified() {
    return modified
        .stream()
        .map(e -> new Modified(this, e, entries.get(e)));
}
```

The Vulkan descriptor for each modified entry is populated as follows:

```java
private void populate(VkWriteDescriptorSet write) {
    // Init write descriptor
    write.dstBinding = binding.index();
    write.descriptorType = binding.type();
    write.dstSet = set.handle();
    write.descriptorCount = 1;
    write.dstArrayElement = 0;

    // Init resource descriptor
    res.populate(write);
}
```

To apply a batch of updates the total set of modified sets is first enumerated:

```java
public static int update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
    // Enumerate modified resources
    List<Modified> modified = descriptors
        .stream()
        .flatMap(DescriptorSet::modified)
        .toList();

    // Ignore if nothing to update
    if(modified.isEmpty()) {
        return 0;
    }
    
    ...

    return writes.length;
}
```

The set of updates is transformed to an array of Vulkan descriptors and the API is invoked to apply the changes:

```java
VkWriteDescriptorSet[] writes = StructureHelper.array(modified, VkWriteDescriptorSet::new, Modified::populate);
dev.library().vkUpdateDescriptorSets(dev, writes.length, writes, 0, null);
```

Finally the `modified` descriptor sets are cleared:

```java
modified
    .stream()
    .map(Modified::set)
    .map(e -> e.modified)
    .forEach(Set::clear);
```

A new library is created for the API methods in this chapter:

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

### Pipeline Layout

The previous demo implemented a bare-bones pipeline layout which is now fleshed out to support descriptor sets.

A new member is added to the builder for the pipeline layout:

```java
public static class Builder {
    private final List<DescriptorSet.Layout> sets = new ArrayList<>();

    public Builder add(DescriptorSet.Layout layout) {
        sets.add(layout);
        return this;
    }
}
```

Which is populated in the build method:

```java
info.setLayoutCount = sets.size();
info.pSetLayouts = Handle.toArray(sets);
```

Finally a new command factory is implemented to bind a group of descriptor sets to the pipeline:

```java
public static Command bind(Pipeline.Layout layout, Collection<DescriptorSet> sets) {
    return (api, cmd) -> api.vkCmdBindDescriptorSets(
        cmd,
        VkPipelineBindPoint.GRAPHICS,
        layout,
        0,
        sets.size(),
        NativeObject.toArray(sets),
        0,
        null
    );
}
```

### Descriptor Sets

To apply the texture in the demo we first implement a new configuration class to instantiate a descriptor set layout for the texture sampler:

```java
@Configuration
public class DescriptorConfiguration {
    @Autowired private LogicalDevice dev;

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

Next a descriptor set pool is created that can allocate a sampler resource:

```java
@Bean
public Pool pool() {
    return new Pool.Builder()
        .add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 1)
        .max(1)
        .build(dev);
}
```

Finally the descriptor set is allocated and populated with the sampler resource:

```java
@Bean
public DescriptorSet descriptor(Pool pool, Layout layout, Sampler sampler, View texture) {
    DescriptorSet descriptor = pool.allocate(layout, 1);
    Resource res = sampler.resource(texture);
    descriptor.set(binding, res);
    return descriptor;
}
```

Note that for the moment we are employing a single frame buffer, descriptor set and command buffer, since the demo is only generating a single frame.

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

And finally the descriptor set is bound to the pipeline in the render sequence:

```java
.begin()
    .add(frame.begin())
        .add(pipeline.bind())
        .add(vbo.bind())
        .add(descriptor.bind(pipeline.layout()))
        .add(draw)
    .add(FrameBuffer.END)
.end();
```

### Conclusion

To use the texture sampler we make the following changes to the fragment shader:

1. Add a layout declaration for a `uniform sampler2D` with the binding index specified in the descriptor set layout.

2. Invoke the built-in `texture` function to sample the texture with the coordinate passed from the vertex shader.

The fragment shader now looks like this:

```glsl
#version 450 core

layout(binding=0) uniform sampler2D texSampler;

layout(location=0) in vec2 texCoord;

layout(location=0) out vec4 outColor;

void main(void) {
    outColor = texture(texSampler, texCoord);
}
```

If all goes well we should finally see the textured quad:

![Textured Quad](textured-quad.png)

We have jumped through a number of hoops in the last couple of chapters and therefore there is plenty to go wrong.  The validation layer will provide excellent diagnostics if the application attempts any invalid operations (and often seems to be able to work around them).  However it is very easy to specify a 'valid' pipeline that results in a black rectangle.

Here are some of the problems encountered by the author:

* The image loader is very crude and somewhat brittle - the Java image _should_ be a `TYPE_4BYTE_BGR` corresponding to the `R8G8B8A8_UNORM` Vulkan format.  It is definitely worth using the debugger to step through image loading and the complex barrier transition logic.

* Ensure the image data is being copied to the vertex buffer and not the other way around (yes we really did this).

* Check that the descriptor set layout is bound to the pipeline and added to the render sequence.

---

## Summary

In this chapter we:

* Implemented descriptor sets to support a texture sampler resource.

* Applied texture sampling to the demo application.

