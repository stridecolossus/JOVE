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

We will also introduce a new framework to address the convoluted structure of the demo application.

---

## Render Pass

### Overview

A _render pass_ is comprised of the following:

* A group of _attachments_ that define the structure and format of the frame buffers.

* One-or-more _sub passes_ that operate sequentially on the attachments.

* A number of _dependencies_ that specify how the sub-passes are linked to allow the hardware to synchronise the process.

Notes:

* A render pass with multiple sub-passes is used (for example) to implement post-processing effects.

* Sub-pass dependencies is deferred to a later chapter as they are no needed at this stage of development.

Generation of the Vulkan descriptors for a render pass is slightly complicated since we prefer to represent the data as an object graph, whereas the resultant descriptors use indices for the relationships between the various components.  Where feasible the code to populate the descriptors is co-located with the relevant type, however the attachment and dependency indices will require a separate helper class.

### Attachments

An attachment is essentially a wrapper for the underlying code-generated structure:

```java
public class Attachment {
    private final VkAttachmentDescription desc;

    private Attachment(VkAttachmentDescription desc) {
        this.desc = notNull(desc);
    }
}
```

When creating the render pass the contiguous array of attachments is populated by the following method:

```java
public void populate(VkAttachmentDescription attachment) {
    attachment.format = desc.format;
    attachment.samples = desc.samples;
    attachment.loadOp = desc.loadOp;
    attachment.storeOp = desc.storeOp;
    attachment.stencilLoadOp = desc.stencilLoadOp;
    attachment.stencilStoreOp = desc.stencilStoreOp;
    attachment.initialLayout = desc.initialLayout;
    attachment.finalLayout = desc.finalLayout;
}
```

Note that the attachment class wraps an instance of the underlying Vulkan descriptor, which avoids having to replicate the fields and implement a large constructor.  However JNA does not support cloning of a structure out-of-the-box so the `populate` method unfortunately has to perform a field-by-field copy.

The builder initialises most of the attachment properties to sensible defaults:

```java
public static class Builder {
    private VkAttachmentDescription desc = new VkAttachmentDescription();

    public Builder() {
        samples(VkSampleCountFlag.COUNT_1);
        load(VkAttachmentLoadOp.DONT_CARE);
        store(VkAttachmentStoreOp.DONT_CARE);
        stencilLoad(VkAttachmentLoadOp.DONT_CARE);
        stencilStore(VkAttachmentStoreOp.DONT_CARE);
        initialLayout(VkImageLayout.UNDEFINED);
    }
    
    ...
    
    public Attachment build() {
        return new Attachment(desc);
    }
}
```

### Render Pass

A sub-pass is comprised of the colour and depth-stencil attachments used for that stage of rendering:

```java
public class Subpass {
    public record Reference(Attachment attachment, VkImageLayout layout) {
    }
    
    private final List<Reference> colour;
    private final Optional<Reference> depth;
}
```

Notes:

* The sub-pass is a transient object only used to create and configure the render-pass.

* The constructor also validates the integrity of the attachments (not shown).

* The obligatory builder is also added to create and configure a sub-pass.

The render pass domain object itself is relatively simple:

```java
public class RenderPass extends AbstractVulkanObject {
    private final List<Attachment> attachments;

    @Override
    protected Destructor<RenderPass> destructor(VulkanLibrary lib) {
        return lib::vkDestroyRenderPass;
    }
    
    static class Group {
        ...
    }
}
```

A render-pass is created via a factory method given a list of sub-passes:

```java
public static RenderPass create(DeviceContext dev, List<Subpass> subpasses) {
    Group group = new Group(subpasses);
    var info = new VkRenderPassCreateInfo();
    ...
}
```

The `Group` helper class (detailed below) aggregates the total set of attachments for the render pass and populates the relevant descriptor properties:

```java
List<Attachment> attachments = group.attachments();
info.attachmentCount = attachments.size();
info.pAttachments = StructureHelper.pointer(attachments, VkAttachmentDescription::new, Attachment::populate);
```

Next the sub-passes are populated (again using the helper):

```java
info.subpassCount = subpasses.size();
info.pSubpasses = StructureHelper.pointer(subpasses, VkSubpassDescription::new, group::populate);
```

And finally the API is invoked to instantiate the render pass:

```java
VulkanLibrary lib = dev.library();
PointerByReference pass = dev.factory().pointer();
check(lib.vkCreateRenderPass(dev, info, null, pass));
return new RenderPass(pass.getValue(), dev, attachments);
```

### Group

The `Group` helper class encapsulates the complexity of the attachment indices (and later on the sub-pass dependencies):

```java
static class Group {
    private final List<Subpass> subpasses;
    private final List<Reference> references;

    Group(List<Subpass> subpasses) {
        this.subpasses = List.copyOf(notEmpty(subpasses));
        this.references = references(subpasses);
    }
}
```

The total set of unique attachment references for a render pass are aggregated as follows:

```java
private static List<Reference> references(List<Subpass> subpasses) {
    return subpasses
        .stream()
        .map(Subpass::attachments)
        .flatMap(List::stream)
        .distinct()
        .toList();
}
```

Which uses the following new accessor on the sub-pass:

```java
public List<Reference> attachments() {
    List<Reference> attachments = new ArrayList<>(colour);
    depth.ifPresent(attachments::add);
    return attachments;
}
```

The overall set of attachments can then be retrieved from the helper and used to populate the render pass descriptor:

```java
public List<Attachment> attachments() {
    return references
        .stream()
        .map(Reference::attachment)
        .toList();
}
```

Population of the descriptors for each sub-pass is also dependant on the attachment `references` member:

```java
public void populate(Subpass subpass, VkSubpassDescription descriptor) {
    // Init descriptor
    descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

    // Populate colour attachments
    descriptor.colorAttachmentCount = subpass.colour.size();
    descriptor.pColorAttachments = StructureHelper.pointer(subpass.colour, VkAttachmentReference::new, this::populate);

    // Populate depth attachment
    descriptor.pDepthStencilAttachment = subpass.depth.map(this::depth).orElse(null);
}
```

The local `populate` method looks up the index of an attachment and then delegates to the reference to complete the descriptor:

```java
private void populate(Reference ref, VkAttachmentReference descriptor) {
    final int index = references.indexOf(ref);
    ref.populate(index, descriptor);
}
```

The optional depth-stencil attachment is populated similarly:

```java
private VkAttachmentReference depth(Reference ref) {
    final var descriptor = new VkAttachmentReference();
    populate(ref, descriptor);
    return descriptor;
}
```

Finally we create a new API for the render pass:

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
// Allocate frame buffer
DeviceContext dev = pass.device();
VulkanLibrary lib = dev.library();
PointerByReference buffer = dev.factory().pointer();
check(lib.vkCreateFramebuffer(dev, info, null, buffer));

// Create frame buffer
return new FrameBuffer(buffer.getValue(), dev, pass, attachments, extents);
```

Frame buffer functionality will be developed further when rendering is address in the next chapter.

The API for frame buffers is simple:

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
Subpass subpass = new Subpass.Builder()
    .colour(new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
    .build();

return RenderPass.create(dev, List.of(subpass));
```

Finally we create the frame buffers:

```java
FrameBuffer[] buffers = swapchain
    .views()
    .stream()
    .map(view -> FrameBuffer.create(pass, swapchain.extents(), List.of(view))
    .toArray(FrameBuffer[]::new);
```

---

## Dependency Injection

### Background

Although we are perhaps half-way to our goal of rendering a triangle it is already apparent that our demo is becoming unwieldy:

* The main application code is one large, imperative method.

* The nature of a Vulkan application means that the various collaborating components are inherently highly inter-dependant, we are forced to structure the code based on the inter-dependencies resulting in convoluted and brittle code.

* All components are created and managed in a single source file rather than being factoring out to discrete, coherent classes.

What we have is a 'God class' which will become harder to navigate and maintain as we add more code to the demo.  We _could_ abandon OO design principles and just follow the often seen C/C++ practice of declaring all components as mutable class members in a single class, but this only hides (and obfuscates) the inter-dependencies and does not address the shear volume of code.

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

Spring Boot is essentially a _container_ for the components that comprise the application, and is responsible for handling the lifecycle of each component and _auto-wiring_ the dependencies at instantiation time.  When the application is started Spring performs a _component scan_ of the project to identify the _beans_ to be managed by the container, which by default starts at the package containing the main class.

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
            .property(Window.Property.DISABLE_OPENGL)
            .build(desktop);
    }

    @Bean
    public static Surface surface(Instance instance, Window window) {
        Handle handle = window.surface(instance.handle());
        return new Surface(handle, instance);
    }
}
```

The `@Configuration` annotation denotes a class that contains components to be managed by the container declared by the `@Bean` annotation.  Note that Spring beans are generally singleton instances.

Here we can see the benefits of using dependency injection:

* The code to instantiate each component is now factored out into neater, more concise factory methods.

* The dependencies of each component are _declared_ in the signature of each bean method and the container _injects_ the relevant arguments for us (or throws an error if a bean does not exist).

* Components are instantiated by the container in logical order inferred from the dependencies, e.g. for the troublesome surface handle.

This should result in code that is both simpler to develop and (more importantly) considerably easier to refactor and fix.  In particular we no longer need to be concerned about dependencies when new components are added or existing components are modified.

However one disadvantage of this approach is that we cannot easily recreate the swapchain when it is invalidated, e.g. when the window is minimised or resized.  This functionality is deferred to a future chapter.

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

    public DeviceConfiguration(Surface surface) {
        presentation = Selector.of(surface);
    }
}
```

In this case we also use _constructor injection_ to inject the surface handle for the `presentation` queue selector.

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
        .queue(graphics.family())
        .queue(presentation.family())
        .build();
}
```

Finally we expose the resultant work queues:

```java
@Bean
public Queue graphics(LogicalDevice dev) {
    return dev.queue(graphics.family());
}

@Bean
public Queue presentation(LogicalDevice dev) {
    return dev.queue(presentation.family());
}
```

The swapchain, render pass and frame buffers are instantiated similarly.

### Cleanup

Spring provides a couple of other bonuses when we address cleanup of the various Vulkan components.

The following bean processor is registered to release all native JOVE objects:

```java
@Bean
static DestructionAwareBeanPostProcessor destroyer() {
    return new DestructionAwareBeanPostProcessor() {
        @Override
        public void postProcessBeforeDestruction(Object bean, String beanName) throws BeansException {
            if(bean instanceof TransientNativeObject obj) {
                obj.destroy();
            }
        }
    };
}
```

Alternatively the container also invokes an _inferred_ public destructor method named `close` or `shutdown` on all registered beans, though we prefer the less obtrusive (and more explicit) approach using the bean processor.

Finally the container also ensures that components are destroyed in the correct reverse order (inferred from the dependencies) removing another responsibility from the developer.

Nice.

---

## Summary

In this chapter we implemented the render pass and frame-buffer components required for presentation.

We also introduced Spring Boot to manage the various inter-dependant Vulkan components.

