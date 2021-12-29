---
title: The Render Pass
---

## Overview

In this chapter we will develop the remaining components required to complete the presentation functionality started in the previous chapter.

We will also introduce a _dependency injection_ framework to simplify development of a Vulkan application.

---

## Render Pass

### Overview

A _render pass_ is comprised of the following:

* A group of _attachments_ that define the structure and format of the frame buffers.

* One-or-more _sub passes_ that operate sequentially on the attachments (multiple sub passes are used to implement post processing effects).

* A number of _dependencies_ that specify how the sub-passes are linked (to allow the hardware to synchronise the process).

We will creates builders for each of these and comprise the new components into a render pass domain object.

### Attachments

We start with a new domain object and builder for an attachment which is essentially a wrapper for the code-generated structure:

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

Note that the attachment class wraps an instance of the underlying Vulkan descriptor, which avoids having to replicate the fields and implement a large constructor.  JNA does not support cloning of a structure out-of-the-box so the `populate` method unfortunately has to perform a field-by-field copy.

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

We next implement the sub-pass domain object comprised of the colour and depth-stencil attachments used for that stage of rendering:

```java
public class Subpass {
    public record Reference(Attachment attachment, VkImageLayout layout) {
    }
    
    private final List<Reference> colour;
    private final Optional<Reference> depth;
}
```

Notes:

* We also implement the obligatory builder to create and configure a sub-pass.

* The sub-pass is a transient object only used to create and configure the render-pass (see below).

The render pass domain object itself is relatively simple:

```java
public class RenderPass extends AbstractVulkanObject {
    private final List<Attachment> attachments;

    private RenderPass(Pointer handle, LogicalDevice dev, List<Attachment> attachments) {
        super(handle, dev);
        this.attachments = attachments;
    }

    public List<Attachment> attachments() {
        return attachments;
    }

    @Override
    protected Destructor<RenderPass> destructor(VulkanLibrary lib) {
        return lib::vkDestroyRenderPass;
    }
}
```

A render-pass is created via a factory method given a list of sub-passes:

```java
public static RenderPass create(DeviceContext dev, List<Subpass> subpasses) {
    // Build render-pass descriptor
    if(subpasses.isEmpty()) throw new IllegalArgumentException(...);
    Helper helper = new Helper(subpasses);
    VkRenderPassCreateInfo info = helper.populate();

    // Allocate render pass
    VulkanLibrary lib = dev.library();
    PointerByReference pass = dev.factory().pointer();
    check(lib.vkCreateRenderPass(dev, info, null, pass));

    // Create render pass
    return new RenderPass(pass.getValue(), dev, helper.attachments);
}
```

We cover the purpose of the _helper_ below.

Finally we create the new API for the render pass:

```java
interface Library {
    int  vkCreateRenderPass(LogicalDevice device, VkRenderPassCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pRenderPass);
    void vkDestroyRenderPass(LogicalDevice device, RenderPass renderPass, Pointer pAllocator);
}
```

Generating the Vulkan descriptor for the render-pass is actually somewhat convoluted for several reasons:

* The sub-pass descriptor refers to the attachments by index, whereas we would like to use explicit object references where possible.

* This is exacerbated further when we implement sub-pass dependencies in a later chapter.

* We would also like to expose the total set of attachments for a render-pass.

After trying a few approaches we decided to introduce the helper class that wraps up the process of building the descriptor and manages the attachment indices.  Ideally we would prefer that each domain object is responsible for populating its corresponding Vulkan descriptor but that is not viable in this case.

The helper class is defined as follows:

```java
private static class Helper {
    private final List<Subpass> subpasses;
    private final List<Attachment> attachments;

    private Helper(List<Subpass> subpasses) {
        this.subpasses = subpasses;
        this.attachments = attachments(subpasses);
    }
}
```

The total list of attachments used by the render-pass is enumerated by flattening the attachments of each sub-pass and omitting duplicates:

```java
private static List<Attachment> attachments(List<Subpass> subpasses) {
    return subpasses
        .stream()
        .flatMap(Subpass::attachments)
        .distinct()
        .collect(toList());
}
```

This list is used to lookup the indices for the attachment indices and as the final argument to the constructor of the render-pass.

The `populate` method generates the render-pass descriptor:

```java
private VkRenderPassCreateInfo populate() {
    // Create render pass descriptor
    VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

    // Add attachments
    info.attachmentCount = attachments.size();
    info.pAttachments = StructureHelper.pointer(attachments, VkAttachmentDescription::new, Attachment::populate);

    // Add sub-passes
    info.subpassCount = subpasses.size();
    info.pSubpasses = StructureHelper.pointer(subpasses, VkSubpassDescription::new, this::subpass);

    return info;
}
```

The descriptor for each sub-pass is populated as follows:

```java
private void subpass(Subpass subpass, VkSubpassDescription desc) {
    // Init descriptor
    desc.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

    // Populate colour attachments
    List<Reference> colour = subpass.colour();
    desc.colorAttachmentCount = colour.size();
    desc.pColorAttachments = StructureHelper.pointer(colour, VkAttachmentReference::new, this::reference);

    // Populate depth attachment
    desc.pDepthStencilAttachment = subpass.depth().map(this::depth).orElse(null);
}
```

The attachment references lookup the relevant index from the `attachments` member of the helper:

```java
private void reference(Reference ref, VkAttachmentReference desc) {
    desc.attachment = attachments.indexOf(ref.attachment());
    desc.layout = ref.layout();
}
```

Finally we add a special case helper for the single, optional depth-stencil attachment reference:

```java
private VkAttachmentReference depth(Reference depth) {
    VkAttachmentReference ref = new VkAttachmentReference();
    reference(depth, ref);
    return ref;
}
```

### Frame Buffers

The final component we require for rendering is the _frame buffer_ that composes the attachments used in the render pass:

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

A frame buffer is created using the following factory that as usual first populates a Vulkan descriptor (validation omitted for brevity):

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

Frame buffer functionality will be developed when we address rendering.

The API for frame buffers is simple:

```java
interface Library {
    int  vkCreateFramebuffer(LogicalDevice device, VkFramebufferCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pFramebuffer);
    void vkDestroyFramebuffer(LogicalDevice device, FrameBuffer framebuffer, Pointer pAllocator);
}
```

### Integration

We now have all the components we require to add a double-buffered swapchain and render pass to the demo application.

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

We next create the render pass consisting of a single sub-pass that renders the colour attachment:

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

