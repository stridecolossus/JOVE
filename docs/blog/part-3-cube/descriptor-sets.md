---
title: Descriptor Sets
---

## Overview

The final piece of functionality we need to implement texture sampling is a _descriptor set_.

- A descriptor set is comprised of the _resources_ used by the pipeline during rendering (samplers, uniform buffers, etc).

- Sets are allocated from a _descriptor set pool_ (similar to commands).

- A _descriptor set layout_ specifies the resource bindings for a descriptor set.

---

## Descriptor Sets

### Domain Class

The first-cut class outline is as follows:

```java
public class DescriptorSet implements NativeObject {
    private final Handle handle;
    private final Layout layout;

    ...

    /**
     * Creates a pipeline bind command for this descriptor set.
     * @param layout Pipeline layout
     * @return New bind command
     */
    public Command bind(Pipeline.Layout layout) {
    }

    /**
     * A <i>descriptor set pool</i> is used to allocate and manage a group of descriptor sets.
     */
    public static class Pool extends AbstractVulkanObject {
    }

    /**
     * Layout for a descriptor-set.
     */
    public static class Layout extends AbstractVulkanObject {
    }
}
```

We tackle the layout first:

```java
public static class Layout extends AbstractVulkanObject {
    private final Map<Integer, Binding> bindings;

    /**
     * Constructor.
     * @param handle        Layout handle
     * @param dev           Logical device
     * @param bindings      Bindings
     */
    Layout(Pointer handle, LogicalDevice dev, List<Binding> bindings) {
        super(handle, dev, dev.library()::vkDestroyDescriptorSetLayout);
        Check.notEmpty(bindings);
        this.bindings = bindings.stream().collect(toMap(Binding::binding, Function.identity()));
    }
}
```

A layout is comprised of a number of resource bindings defined as follows:

```java
public static record Binding(int binding, VkDescriptorType type, int count, Set<VkShaderStageFlag> stages) {
}
```

We implement a convenience builder for the binding and add a populate method for the binding descriptor used below:

```java
private void populate(VkDescriptorSetLayoutBinding info) {
    info.binding = binding;
    info.descriptorType = type;
    info.descriptorCount = count;
    info.stageFlags = IntegerEnumeration.mask(stages);
}
```

A layout is created by a static factory method:

```java
public static Layout create(LogicalDevice dev, List<Binding> bindings) {
    // Init layout descriptor
    final VkDescriptorSetLayoutCreateInfo info = new VkDescriptorSetLayoutCreateInfo();
    info.bindingCount = bindings.size();
    info.pBindings = VulkanStructure.array(VkDescriptorSetLayoutBinding::new, bindings, Binding::populate);

    // Allocate layout
    final VulkanLibrary lib = dev.library();
    final PointerByReference handle = lib.factory().pointer();
    check(lib.vkCreateDescriptorSetLayout(dev.handle(), info, null, handle));

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

    /**
     * Constructor.
     * @param handle        Pool handle
     * @param dev           Logical device
     * @param max           Maximum number of descriptor sets
     */
    Pool(Pointer handle, LogicalDevice dev, int max) {
        super(handle, dev, dev.library()::vkDestroyDescriptorPool);
        this.max = oneOrMore(max);
    }
}
```

The _max_ member configures the maximum number of descriptors that can be allocated from the pool.

We add the following method to allocate a number of sets from the pool:

```java
public synchronized List<DescriptorSet> allocate(List<Layout> layouts) {
    // Check pool size
    if(sets.size() + layouts.size() > max) {
        throw new IllegalArgumentException(...);
    }

    // Build allocation descriptor
    final VkDescriptorSetAllocateInfo info = new VkDescriptorSetAllocateInfo();
    info.descriptorPool = this.handle();
    info.descriptorSetCount = layouts.size();
    info.pSetLayouts = Handle.toPointerArray(layouts);

    // Allocate descriptors sets
    final LogicalDevice dev = this.device();
    final VulkanLibrary lib = dev.library();
    final Pointer[] handles = lib.factory().pointers(layouts.size());
    check(lib.vkAllocateDescriptorSets(dev.handle(), info, handles));
    ...
}
```

And transform the returned descriptor set handles to the domain object:

```java
    ...
    
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
}
```

And corresponding methods to release descriptor sets:

```java
public synchronized void free(Collection<DescriptorSet> sets) {
    if(!this.sets.containsAll(sets)) throw new IllegalArgumentException(...);
    this.sets.removeAll(sets);
    final LogicalDevice dev = this.device();
    check(dev.library().vkFreeDescriptorSets(dev.handle(), this.handle(), sets.size(), Handle.toArray(sets)));
}

public synchronized void free() {
    if(sets.isEmpty()) throw new IllegalArgumentException("Pool is already empty");
    final LogicalDevice dev = this.device();
    check(dev.library().vkResetDescriptorPool(dev.handle(), this.handle(), 0));
    sets.clear();
}

@Override
protected void release() {
    sets.clear();
    super.release();
}
```

Note that descriptor sets are managed by the pool and are automatically released when the pool is destroyed.

To construct a pool we provide a builder:

```java
public static class Builder {
    private final LogicalDevice dev;
    private final Map<VkDescriptorType, Integer> entries = new HashMap<>();
    private final Set<VkDescriptorPoolCreateFlag> flags = new HashSet<>();
    private Integer max;
}
```

The _entries_ field is a table of the pool sizes for each descriptor type (we don't bother implementing a transient object):

```java
public Builder add(VkDescriptorType type, int count) {
    Check.notNull(type);
    Check.oneOrMore(count);
    entries.put(type, count);
    return this;
}
```

We add a local helper to transform an entry to the Vulkan descriptor:

```java
private static void populate(Map.Entry<VkDescriptorType, Integer> entry, VkDescriptorPoolSize size) {
    size.type = entry.getKey();
    size.descriptorCount = entry.getValue();
}
```

The `build` method first validates that the configured pool size does not exceed the maximum:

```java
// Determine logical maximum number of sets that can be allocated
final int limit = entries
    .values()
    .stream()
    .mapToInt(Integer::intValue)
    .max()
    .orElseThrow(() -> new IllegalArgumentException("No pool sizes specified"));

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

Next we transform the entries map to an array and populate the descriptor for the pool:

```java
// Init pool descriptor
final VkDescriptorPoolCreateInfo info = new VkDescriptorPoolCreateInfo();
info.flags = IntegerEnumeration.mask(flags);
info.poolSizeCount = entries.size();
info.pPoolSizes = StructureCollector.toPointer(entries.entrySet(), VkDescriptorPoolSize::new, Builder::populate);
info.maxSets = max;
```

Finally we invoke the API to allocate the pool and create the domain object:

```java
// Allocate pool
final VulkanLibrary lib = dev.library();
final PointerByReference handle = lib.factory().pointer();
check(lib.vkCreateDescriptorPool(dev.handle(), info, null, handle));

// Create pool
return new Pool(handle.getValue(), dev, max);
```

### Resources

With the framework for descriptor sets in place we next need functionality to update the _resources_ for a descriptor set.

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

The resources are wrapped by a new `Entry` class and indexed by the layout bindings:

```java
public class DescriptorSet {
    ...
    private final Map<Binding, Entry> entries;

    DescriptorSet(Handle handle, Layout layout) {
        this.handle = notNull(handle);
        this.layout = notNull(layout);
        this.entries = layout.bindings.values().stream().collect(toMap(Function.identity(), Entry::new));
    }

    public Entry entry(Binding binding) {
        final Entry entry = entries.get(binding);
        if(entry == null) throw new IllegalArgumentException(...);
        return entry;
    }
}
```

An `Entry` is a mutable holder for a descriptor set resource:

```java
public class Entry {
    private final Binding binding;
    private Resource res;
    private boolean dirty = true;

    private boolean isDirty() {
        if(dirty) {
            dirty = false;
            return true;
        }
        else {
            return false;
        }
    }

    public Optional<Resource> resource() {
        return Optional.ofNullable(res);
    }
}
```

The `dirty` flag is used later to identify resources that have been modified by the following mutator:

```java
public void set(Resource res) {
    if(res.type() != binding.type()) throw new IllegalArgumentException(...);
    this.res = notNull(res);
    this.dirty = true;
}
```

A descriptor set update is then populated as follows:

```java
private void populate(VkWriteDescriptorSet write) {
    // Init write descriptor
    write.dstBinding = binding.index();
    write.descriptorType = binding.type();
    write.dstSet = handle();
    write.descriptorCount = 1;
    write.dstArrayElement = 0;

    // Populate resource
    res.populate(write);
}
```

Note that this method also invokes the `populate` method of the resource itself.

### Updates

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
info.pSetLayouts = Handle.toPointerArray(sets);
```

Finally we implement the following method on the descriptor set class to bind it to the pipeline:

```java
public Command bind(Pipeline.Layout layout) {
    return bind(layout, List.of(this));
}
```

Which delegates to an over-loaded variant that binds multiple descriptor sets:

```java
public static Command bind(Pipeline.Layout layout, Collection<DescriptorSet> sets) {
    final Pointer[] handles = Handle.toArray(sets);

    return (api, cmd) -> api.vkCmdBindDescriptorSets(
            cmd,
            VkPipelineBindPoint.VK_PIPELINE_BIND_POINT_GRAPHICS,
            layout.handle(),
            0,                  // First set
            1,                  // Count
            handles,
            0,                  // Dynamic offset count
            null                // Dynamic offsets
    );
}
```

---

## Texture Sampling Redux

### Texture Resource

The final step in this chapter is to implement a descriptor set resource for the texture `Sampler`:

```java
public Resource resource(View texture) {
    return new Resource() {
        @Override
        public VkDescriptorType type() {
            return VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER;
        }

        @Override
        public void populate(VkWriteDescriptorSet write) {
            // Create sampler descriptor
            final var info = new VkDescriptorImageInfo();
            info.imageLayout = VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL;
            info.sampler = Sampler.this.handle();
            info.imageView = texture.handle();

            // Add to write descriptor
            write.pImageInfo = info;
        }
    };
}
```

Note that a `VkWriteDescriptorSet` is used for **all** types of resource - for textures we populate the `pImageInfo` field.

### Integration

We now have all the components we need to apply the texture in the demo.

First we create a descriptor set layout for a sampler that will be used in the fragment shader:

```java
final var binding = new DescriptorSet.Binding.Builder()
    .type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
    .stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
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
    .add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 2)
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

- Verify that the image contains RGBA data once it is loaded and that it matches the expected Vulkan format which is `VK_FORMAT_R8G8B8A8_UNORM` in this example.

- Ensure the texture alpha channel has a non-zero value.

- Double-check that the winding order is counter-clockwise and back-facing fragments are being discarded (or turn off face culling).

- Also check that the drawing primitive is a triangle strip.

- Walk through the process of loading and transitioning the image and ensure that the previous/next layouts are correct and that image has the `VK_IMAGE_ASPECT_COLOR_BIT` aspect.

- Check that the image data is being copied to the vertex buffer and not the other way round (yes we really did this!)

- Ensure that the descriptor set and layout are bound to the pipeline and added to the rendering sequence.

---

## Summary

In this chapter we:

- Implemented descriptor sets to support a texture sampler resource.

- Applied texture sampling to the quad demo.

The API methods in this chapter are defined in the `VulkanLibraryDescriptorSet` JNA library interface.
