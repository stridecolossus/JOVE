---
title: Descriptor Sets
---

## Overview

The final piece of functionality we need to implement texture sampling is the _descriptor set_.

- A descriptor set is comprised of the _resources_ used by the pipeline during rendering (samplers, uniform buffers, etc).

- Sets are allocated from a _descriptor set pool_ (similar to commands).

- A _descriptor set layout_ specifies the resource bindings for a descriptor set.

We will also need to slightly refactor the pipeline layout which is used to configure the descriptor sets in the rendering process.

> Descriptor sets are one of the more complex aspects of a Vulkan application (or at least they proved to be for the author).  We have attempted to come up with a design that reflects the flexibility that the Vulkan designers were clearly aiming for whilst providing a relatively developer-friendly API.

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

Where _count_ is the size of the resource (i.e. which can be an array) and _stages_ specifies where the resource is used in the pipeline.

We implement a convenience builder for the binding and add a populate method for the binding descriptor which is used below:

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
    final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
    info.bindingCount = bindings.size();
    info.pBindings = StructureHelper.first(bindings, VkDescriptorSetLayoutBinding::new, Binding::populate);

    // Allocate layout
    final VulkanLibrary lib = dev.library();
    final PointerByReference handle = lib.factory().pointer();
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
    final int size = layouts.size();
    if(sets.size() + size > max) {
        throw new IllegalArgumentException(...);
    }

    // Build allocation descriptor
    final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
    info.descriptorPool = this.handle();
    info.descriptorSetCount = size;
    info.pSetLayouts = NativeObject.toArray(layouts);

    // Allocate descriptors sets
    final DeviceContext dev = this.device();
    final VulkanLibrary lib = dev.library();
    final Pointer[] handles = lib.factory().array(size);
    check(lib.vkAllocateDescriptorSets(dev, info, handles));
    ...
}
```

The returned handles are transformed to the domain object:

```java
// Create descriptor sets
final IntFunction<DescriptorSet> ctor = index -> {
    final Handle handle = new Handle(handles[index]);
    return new DescriptorSet(handle, layouts.get(index));
};
final var allocated = IntStream
        .range(0, handles.length)
        .mapToObj(ctor)
        .collect(toList());

// Record sets allocated by this pool
sets.addAll(allocated);
return allocated;
```

Note that descriptor sets are managed by the pool and are automatically released when the pool is destroyed.

We also implement the other API methods for management of the pool:

```java
public synchronized void free(Collection<DescriptorSet> sets) {
    ...
    final DeviceContext dev = this.device();
    check(dev.library().vkFreeDescriptorSets(dev, this, sets.size(), NativeObject.toArray(sets)));
}

public synchronized void free() {
    if(sets.isEmpty()) throw new IllegalArgumentException(...);
    final DeviceContext dev = this.device();
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

The _pool_ is a table of the pool sizes for each descriptor type:

```java
public Builder add(VkDescriptorType type, int count) {
    Check.notNull(type);
    Check.oneOrMore(count);
    pool.put(type, count);
    return this;
}
```

The build method transforms the table to an array and populates the descriptor for the pool:

```java
// Init pool descriptor
final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
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

The size of the table is also verified against the specified _max_ property (not shown).

Finally we invoke the API to allocate the pool and create the domain object:

```java
// Allocate pool
final VulkanLibrary lib = dev.library();
final PointerByReference handle = lib.factory().pointer();
check(lib.vkCreateDescriptorPool(dev, info, null, handle));

// Create pool
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
            final var info = new VkDescriptorImageInfo();
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

Note that the map is initialised in the constructor by instantiating a new `Entry` for each binding.

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
    final Entry entry = entry(binding);
    entry.res = notNull(res);
    entry.dirty = true;
}
```

### Updates


    private Stream<Entry> modified() {
        return entries
                .values()
                .stream()
                .filter(Entry::isDirty);
    }



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

    // Update flag
    assert dirty;
    dirty = false;
}
```





Now we can allocate a descriptor set and initialise its resources, the final step is to apply the changes:

```java
public static void update(LogicalDevice dev, Collection<DescriptorSet> descriptors) {
    // Enumerate dirty resources
    final var writes = descriptors
        .stream()
        .flatMap(set -> set.entries.values().stream())
        .filter(Entry::isDirty)
        .collect(new StructureCollector<>(VkWriteDescriptorSet::new, Entry::populate));

    // Ignore if nothing to update
    if(writes == null) {
        return;
    }

    // Apply update
    dev.library().vkUpdateDescriptorSets(dev.handle(), writes.length, writes, 0, null);
}
```

Notes:

- A `VkWriteDescriptorSet` instance is created for **each** descriptor set.

- The API assumes multiple descriptor set updates will be applied together to reduce the number of invocations.

### Pipeline Layout

In the previous demo we created a bare-bones pipeline layout implementation which we now flesh out to support descriptor sets.

We update the pipeline layout builder:

```java
public static class Builder {
    private final List<DescriptorSet.Layout> sets = new ArrayList<>();

    public Builder add(DescriptorSet.Layout layout) {
        sets.add(notNull(layout));
        return this;
    }
}
```

And add the following code to the build method:

```java
info.setLayoutCount = sets.size();
info.pSetLayouts = Handle.toArray(sets);
```

Finally we implement the following method to bind a number of descriptor sets to the pipeline:

```java
public static Command bind(Pipeline.Layout layout, Collection<DescriptorSet> sets) {
    return (api, cmd) -> api.vkCmdBindDescriptorSets(
            cmd,
            VkPipelineBindPoint.GRAPHICS,
            layout.handle(),
            0,                  // First set
            sets.size(),
            Handle.toArray(sets),
            0,                  // Dynamic offset count
            null                // Dynamic offsets
    );
}
```

With an over-loaded variant for a single descriptor set instance.

---

## Texture Sampling Redux





### Integration

We now have all the components we need to apply the texture in the demo.

First we create a descriptor set layout for a sampler that will be used in the fragment shader:

```java
final var binding = new DescriptorSet.Binding.Builder()
    .type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
    .stage(VkShaderStageFlag.FRAGMENT)
    .build();

final var layout = DescriptorSet.Layout.create(dev, List.of(binding));
```

Which is added to the pipeline layout:

```java
final Pipeline.Layout pipelineLayout = new Pipeline.Layout.Builder(dev)
    .add(layout)
    .build();
```

Next we create a descriptor set pool (sized to the number of swapchain images):

```java
final var pool = new DescriptorSet.Pool.Builder(dev)
    .add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 2)
    .max(2)
    .build();
```

We allocate a descriptor set for each swapchain image:

```java
final var descriptors = pool.allocate(layout, 2);
```

And initialise each with the texture sampler:

```java
final Sampler sampler = new Sampler.Builder(dev).build();
final var resource = sampler.resource(texture);
for(DescriptorSet set : descriptors) {
    set.entry(binding).set(resource);
}
DescriptorSet.update(dev, descriptors);
```

And finally we bind the descriptor set in the rendering sequence:

```java
.begin()
    ...
    .add(descriptors.get(n).bind(pipelineLayout))
    .add(draw)
    .add(RenderPass.END_COMMAND)
.end();
```

### Texture Sampling

The only other change we need to make is to actually sample the texture in the fragment shader which involves:

1. Adding a layout declaration for a `uniform sampler2D` with the binding index we specified in the descriptor set.

2. Invoking the built-in `texture` function to sample the texture with the coordinate passed from the vertex shader.

The fragment shader should look like this:

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

There are a lot of steps in this chapter and therefore plenty that can go wrong.  Vulkan will generally throw a hissy fit if we attempt any invalid operations, e.g. forgetting to provide the target layout when performing an image transition.  However it is quite easy to specify a 'correct' pipeline and still end up with a black rectangle!  With so much going on behind the scenes this can be very difficult to diagnose - here are some possible failure cases:

- Verify that the image contains RGBA data once it is loaded and that it matches the expected Vulkan format which is `R8G8B8A8_UNORM` in this example.

- Ensure the texture alpha channel has a non-zero value.

- Double-check that the winding order is counter-clockwise and back-facing fragments are being discarded (or turn off face culling).

- Also check that the drawing primitive is a triangle strip.

- Walk through the process of loading and transitioning the image and ensure that the previous/next layouts are correct and that image has the `COLOR` aspect.

- Check that the image data is being copied to the vertex buffer and not the other way round (yes we really did this!)

- Ensure that the descriptor set and layout are bound to the pipeline and added to the rendering sequence.

---

## Summary

In this chapter we:

- Implemented descriptor sets to support a texture sampler resource.

- Applied texture sampling to the quad demo.

The API methods in this chapter are defined in the `VulkanLibraryDescriptorSet` JNA library interface.
