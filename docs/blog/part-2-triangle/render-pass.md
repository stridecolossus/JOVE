---
title: The Render Pass
---

---

## Contents

- [Overview](#overview)
- [Render Pass](#render-pass)
- [Dependency Injection](#dependency-injection)

---

## Overview

In this chapter we will develop the remaining components required to complete the presentation functionality started in the previous chapter.

A _render pass_ is comprised of the following:

* A set of _attachments_ that define the structure and format of the frame buffer images.

* One-or-more _sub passes_ that operate sequentially on the attachments.

* A number of _dependencies_ that specify how the sub-passes are linked to allow the hardware to synchronise the process.

A render pass with multiple sub-passes is generally used to implement post-processing effects.
Subpass dependencies are deferred to a later chapter as they are not needed for this stage of development.

A new framework library will also be introduced to address the convoluted structure of the demo application.

---

## Render Pass

### Attachments

An attachment is essentially a wrapper for the underlying Vulkan structure:

```java
public record Attachment(VkFormat format, VkSampleCount samples, Operations colour, Operations stencil, VkImageLayout before, VkImageLayout after) {
    /**
     * Convenience wrapper for load-store operations.
     */
    public record Operations(VkAttachmentLoadOp load, VkAttachmentStoreOp store) {
    }
}
```

Where _colour_ and _stencil_ are the load-store operations for the colour and depth-stencil attachments respectively.

When creating the render pass the contiguous array of attachments is populated by the following method:

```java
void populate(VkAttachmentDescription attachment) {
    attachment.format = format;
    attachment.samples = samples;
    attachment.loadOp = colour.load;
    attachment.storeOp = colour.store;
    attachment.stencilLoadOp = stencil.load;
    attachment.stencilStoreOp = stencil.store;
    attachment.initialLayout = before;
    attachment.finalLayout = after;
}
```

A convenience builder is implemented that initialises the attachment properties to sensible defaults:

```java
public static class Builder {
    private VkFormat format;
    private VkSampleCount samples = VkSampleCount.COUNT_1;
    private VkAttachmentLoadOp load = VkAttachmentLoadOp.DONT_CARE;
    private VkAttachmentStoreOp store = VkAttachmentStoreOp.DONT_CARE;
    private VkAttachmentLoadOp stencilLoad = VkAttachmentLoadOp.DONT_CARE;
    private VkAttachmentStoreOp stencilStore = VkAttachmentStoreOp.DONT_CARE;
    private VkImageLayout before = VkImageLayout.UNDEFINED;
    private VkImageLayout after;
}
```

### Subpass

A _subpass_ is a mutable type comprising the attachments used in that stage of rendering:

```java
public class Subpass {
    private final List<Reference> colour = new ArrayList<>();
    private Reference depth;
}
```

A colour attachment can be added to the subpass:

```java
public Subpass colour(Attachment colour, VkImageLayout layout) {
    this.colour.add(new Reference(colour, layout));
    return this;
}
```

Where a `Reference` is a inner wrapper for a referenced attachment used in the subpass:

```java
record Reference(Attachment attachment, VkImageLayout layout) {
    void populate(VkAttachmentReference descriptor) {
        descriptor.attachment = ...
        descriptor.layout = layout;
    }
}
```

Population of the `attachment` field index is detailed below.

The single depth-stencil attachment is added similarly:

```java
public Subpass depth(Attachment depth, VkImageLayout layout) {
    this.depth = new Reference(depth, layout);
    return this;
}
```

Finally the descriptor for the subpass is populated as follows:

```java
void populate(VkSubpassDescription descriptor) {
    // Init descriptor
    descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

    // Populate colour attachments
    descriptor.colorAttachmentCount = colour.size();
    descriptor.pColorAttachments = StructureHelper.pointer(colour, VkAttachmentReference::new, Reference::populate);

    // Populate depth attachment
    if(depth != null) {
        var ref = new VkAttachmentReference();
        depth.populate(ref);
        descriptor.pDepthStencilAttachment = ref;
    }
}
```

### Render Pass

The domain class for a render pass contains the overall set of attachments used by its sub-passes:

```java
public class RenderPass extends AbstractVulkanObject {
    private final List<Attachment> attachments;

    @Override
    protected Destructor<RenderPass> destructor(VulkanLibrary lib) {
        return lib::vkDestroyRenderPass;
    }
}
```

And is constructed via a builder:

```java
public static class Builder {
    private final List<Subpass> subpasses = new ArrayList<>();

    /**
     * @return New subpass builder
     */
    public Subpass subpass() {
        Subpass subpass = ...
        subpasses.add(subpass);
        return subpass;
    }
}
```

We prefer to specify the render pass by an object graph of the sub-passes and attachments, whereas the underlying Vulkan descriptors use indices to refer to attachments (and later on sub-pass dependencies).  However all of these are transient objects that have no relevance once the render pass has been instantiated.  Additionally the application does not care about the attachment indices, unlike (for example) vertex attributes which are dependant on the shader layout.

Therefore the render pass builder _allocates_ attachment indices by the introduction of the over-ridden `allocate` method:

```java
public static class Builder {
    ...
    private int ref;

    public Subpass subpass() {
        Subpass subpass = new Subpass() {
            @Override
            protected int allocate() {
                return ref++;
            }
        
            @Override
            public Builder build() {
                return Builder.this;
            }
        };
        ...
    }
}
```

An `index` member is added to the attachment `Reference` and initialised accordingly:

```java
public Subpass colour(Attachment colour, VkImageLayout layout) {
    int index = allocate();
    this.colour.add(new Reference(index, colour, layout));
    return this;
}
```

This makes the render pass and subpass slightly inter-dependant but effectively hides the indices whilst allowing both classes to be tested in isolation.

In the render pass builder the overall set of attachments is first aggregated from the sub-passes:

```java
public RenderPass build(DeviceContext dev) {
    List<Attachment> attachments = subpasses
        .stream()
        .flatMap(Subpass::references)
        .distinct()
        .map(Reference::attachment)
        .toList();
}
```

Where each subpass enumerates the attachments that it uses:

```java
Stream<Reference> references() {
    if(depth == null) {
        return colour.stream();
    }
    else {
        return Stream.concat(colour.stream(), Stream.of(depth));
    }
}
```

Next the attachments are populated in the create descriptor for the render pass:

```java
var info = new VkRenderPassCreateInfo();
info.attachmentCount = attachments.size();
info.pAttachments = StructureHelper.pointer(attachments, VkAttachmentDescription::new, Attachment::populate);
```

Followed by the sub-passes:

```java
info.subpassCount = subpasses.size();
info.pSubpasses = StructureHelper.pointer(subpasses, VkSubpassDescription::new, Subpass::populate);
```

And finally the render pass is instantiated:

```java
VulkanLibrary lib = dev.library();
PointerByReference pass = dev.factory().pointer();
check(lib.vkCreateRenderPass(dev, info, null, pass));
return new RenderPass(pass.getValue(), dev, attachments);
```

The API for the render pass is simple:

```java
interface Library {
    int  vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pRenderPass);
    void vkDestroyRenderPass(LogicalDevice device, RenderPass renderPass, Pointer pAllocator);
}
```

### Frame Buffers

The final component required for rendering is the _frame buffer_ which composes the attachments used in the render pass:

```java
public class FrameBuffer extends AbstractVulkanObject {
    private final RenderPass pass;
    private final List<View> attachments;
    private final Dimensions extents;

    @Override
    protected Destructor<FrameBuffer> destructor(VulkanLibrary lib) {
        return lib::vkDestroyFramebuffer;
    }
}
```

A frame buffer is created using the following factory that as usual first populates a Vulkan descriptor:

```java
public static FrameBuffer create(RenderPass pass, Dimensions extents, List<View> attachments) {
    var info = new VkFramebufferCreateInfo();
    info.renderPass = pass.handle();
    info.attachmentCount = attachments.size();
    info.pAttachments = Handle.toArray(attachments);
    info.width = extents.width();
    info.height = extents.height();
    info.layers = 1;
    ...
}
```

The frame buffer is then created via the API and wrapped by a new domain object instance:

```java
DeviceContext dev = pass.device();
VulkanLibrary lib = dev.library();
PointerByReference buffer = dev.factory().pointer();
check(lib.vkCreateFramebuffer(dev, info, null, buffer));
return new FrameBuffer(buffer.getValue(), dev, pass, attachments, extents);
```

Frame buffer functionality will be developed further when rendering is addressed in the next chapter.

The API for frame buffers is also simple:

```java
interface Library {
    int  vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);
    void vkDestroyFramebuffer(LogicalDevice device, FrameBuffer framebuffer, Pointer pAllocator);
}
```

### Integration

We now have all the components required to implement a double-buffered swapchain and render pass for the demo application.

```java
Swapchain swapchain = new Swapchain.Builder(dev, surface)
    .count(2)
    .build();
```

The render pass consists of a single colour attachment which is cleared before rendering and transitioned to a layout ready for presentation:

```java
VkFormat format = new FormatBuilder()
    .components(FormatBuilder.BGRA)
    .bytes(1)
    .signed(false)
    .type(FormatBuilder.Type.NORM)
    .build();

Attachment attachment = new Attachment.Builder()
    .format(format)
    .load(VkAttachmentLoadOp.CLEAR)
    .store(VkAttachmentStoreOp.STORE)
    .finalLayout(VkImageLayout.PRESENT_SRC_KHR)
    .build();
```

The render pass consists of a single sub-pass to render the colour attachment:

```java
return new RenderPass.Builder()
    .subpass()
        .colour(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
        .build()
    .build();
```

Finally a frame buffer is created:

```java
View view = swapchain.views().get(0);
FrameBuffer fb = FrameBuffer.create(pass, swapchain.extents(), view);
```

Although the swapchain supports multiple attachments the demo will use a single frame-buffer until a proper rendering loop is implemented later.

---

## Dependency Injection

### Background

Although we are perhaps half-way to our goal of rendering a triangle it is already apparent that the demo is becoming unwieldy:

* The main application code is one large, imperative method.

* The nature of a Vulkan application means that the various collaborating components are inherently highly inter-dependant, we are forced to structure the code based on the inter-dependencies resulting in convoluted and brittle code.

* All components are created and managed in a single source file rather than being factoring out to discrete, coherent classes.

What we have is a 'God class' which will become harder to navigate and maintain as more code is added to the demo.  

The obvious solution is to use a _dependency injection_ framework that manages the components and dependencies for us, freeing development to focus on each component in relative isolation.  For this we will use [Spring Boot](https://spring.io/projects/spring-boot) which is one of the most popular and best supported dependency injection frameworks.

Note that only the demo applications will be dependant on this framework and not the JOVE library itself.

### Project

The [Spring Initializr](https://start.spring.io/) is used to generate a POM and main class for a new Spring-based project:

```java
@SpringBootApplication
public class TriangleDemo {
    public static void main(String[] args) {
        SpringApplication.run(TriangleDemo.class, args);
    }
}
```

This should run the new application though it obviously doesn't do anything yet.

Spring Boot is essentially a _container_ for the components that comprise the application, and is responsible for handling the lifecycle of each component and _auto-wiring_ the dependencies at instantiation time.

When the application is started Spring performs a _component scan_ of the project to identify the _beans_ to be managed by the container, which by default starts at the package containing the main class.

We start by factoring out the various desktop components into a Spring _configuration_ class:

```java
@Configuration
class DesktopConfiguration {
    @Bean
    public static Desktop desktop() {
        Desktop desktop = Desktop.create();
        if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");
        return desktop;
    }

    @Bean
    public static Window window(Desktop desktop) {
        return new Window.Builder()
            .title("TriangleDemo")
            .size(new Dimensions(1024, 768))
            .property(Window.Hint.DISABLE_OPENGL)
            .build(desktop);
    }

    @Bean
    public static Handle surface(Instance instance, Window window) {
        return window.surface(instance.handle());
    }
}
```

The `@Configuration` annotation denotes a class containing a number of `@Bean` methods that create components to be managed by the container.

Here we can see the benefits of using dependency injection:

* The code to instantiate each component is now factored out into neater, more concise factory methods.

* The dependencies of each component are _declared_ in the signature of each bean method and the container _injects_ the relevant arguments for us (or throws an error if a dependency cannot be resolved).

* Components are instantiated by the container in logical order inferred from the dependencies, e.g. for the troublesome surface handle.

* Note that Spring beans are generally singleton instances.

This should result in code that is both simpler to develop and (more importantly) considerably easier to refactor and fix.  In particular we no longer need to be concerned about dependencies when components are added or modified.

However one disadvantage of this approach is that the swapchain cannot be easily recreated when it is invalidated, e.g. when the window is minimised or resized.  This functionality is deferred to a future chapter.

### Integration

The configuration class for the Vulkan library and instance is relatively trivial:

```java
@Configuration
class VulkanConfiguration {
    @Bean
    public static VulkanLibrary library() {
        return VulkanLibrary.create();
    }

    @Bean
    public static Instance instance(VulkanLibrary lib, Desktop desktop, @Value("${application.title}") String title) {
        // Create instance
        Instance instance = new Instance.Builder()
            .name(title)
            .extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
            .extensions(desktop.extensions())
            .layer(ValidationLayer.STANDARD_VALIDATION)
            .build(lib);
        
        // Attach diagnostics handler
        ...
            
        return instance;
    }
}
```

The `@Value` annotation retrieves a configuration property which is usually specified in the `application.properties` file:

```java
application.title: Triangle Demo
```

We refactor the window title in the desktop configuration class similarly.

The next configuration class factors out the code to instantiate the physical and logical devices:

```java
@Configuration
class DeviceConfiguration {
    private final Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);
    private final Selector presentation;

    public DeviceConfiguration(Handle surface) {
        presentation = Selector.of(surface);
    }
}
```

In this case _constructor injection_ is used to inject the surface handle for the `presentation` queue selector.

The bean methods for the devices are straight-forward:

```java
@Bean
public PhysicalDevice physical(Instance instance) {
    return PhysicalDevice
        .devices(instance)
        .filter(graphics)
        .filter(presentation)
        .findAny()
        .orElseThrow(() -> new RuntimeException("No suitable physical device available"));
}

@Bean
public LogicalDevice device(PhysicalDevice dev) {
    return new LogicalDevice.Builder(dev)
        .extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
        .layer(ValidationLayer.STANDARD_VALIDATION)
        .queue(graphics.select(dev))
        .queue(presentation.select(dev))
        .build();
}
```

Finally the resultant work queues are exposed:

```java
@Bean
public Queue graphics(LogicalDevice dev) {
    return dev.queue(graphics.select(dev.parent()));
}

@Bean
public Queue presentation(LogicalDevice dev) {
    return dev.queue(presentation.select(dev.parent()));
}
```

The swapchain, render pass and frame buffers are instantiated similarly.

### Cleanup

Spring offers another bonus when cleaning up the various Vulkan components on application termination.  The following bean processor releases all native JOVE objects:

```java
@Bean
static DestructionAwareBeanPostProcessor destroyer() {
    return new DestructionAwareBeanPostProcessor() {
        @Override
        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
            if(bean instanceof TransientNativeObject obj && !obj.isDestroyed()) {
                obj.destroy();
            }
        }
    };
}
```

Alternatively the container invokes _inferred_ public destructor methods named `close` or `shutdown` on registered beans, though we prefer the more explicit approach.

Finally the container also ensures that components are destroyed in the correct reverse order (inferred from the dependencies) removing another responsibility from the developer.

Nice.

---

## Summary

In this chapter the final components required for presentation were implemented and Spring Boot was introduced to manage the various inter-dependant Vulkan components.

