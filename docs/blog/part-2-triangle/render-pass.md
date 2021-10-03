---
title: Presentation
---

## Overview

render pass
frame buffer
dependency injection
integration

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

Note that the attachment class wraps an instance of the underlying Vulkan descriptor, which avoids having to replicate the fields and implement a large constructor.  However JNA does not support cloning of a structure out-of-the-box so the `populate` method is performs a field-by-field copy.

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
}
```

Creating the attachment is straight-forward:

```java
public Attachment build() {
    // Validate
    if(desc.format == null) throw new IllegalArgumentException("No format specified for attachment");
    if(desc.finalLayout == null) throw new IllegalArgumentException("No final layout specified");

    // Create attachment
    final Attachment attachment = new Attachment(desc);

    // Prevent fiddling
    desc = null;

    return attachment;
}
```

### Render Pass

The render pass domain object is relatively simple:

```java
public class RenderPass extends AbstractVulkanObject {
    private final List<Attachment> attachments;

    private RenderPass(Pointer handle, LogicalDevice dev, List<Attachment> attachments) {
        super(handle, dev);
        this.attachments = List.copyOf(attachments);
    }

    public List<Attachment> attachments() {
        return attachments;
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyRenderPass;
    }
}
```

Next we implement a builder for the render pass that constructs the dependant components:

```java
public static class Builder {
    private final List<Attachment> attachments = new ArrayList<>();
    private final List<SubPassBuilder> subpasses = new ArrayList<>();
    private final List<DependencyBuilder> dependencies = new ArrayList<>();

    /**
     * Adds an attachment to this render pass.
     * @param attachment Attachment to add
     * @return Attachment index
     */
    private int add(Attachment attachment) {
        Check.notNull(attachment);
        if(!attachments.contains(attachment)) {
            attachments.add(attachment);
        }
        return attachments.indexOf(attachment);
    }
}
```

Notes:

* Sub-passes and dependencies are _nested_ builders implemented as local classes.

* Attachments are not explicitly added to the render pass builder (the `add` method is private) but are instead registered as a side-effect of configuring the sub-passes (see below).

* Therefore attachment indices are automatically allocated rather than being explicitly configured by the user (a constraint that seems valid at this stage).

* We will address the `DependencyBuilder` later as sub-pass dependencies are not needed at this stage of development.

The `build` method follows the usual pattern to construct the render pass:

```java
public RenderPass build(LogicalDevice dev) {
    // Validate
    if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one sub-pass must be specified");
    assert !attachments.isEmpty();

    // Create render pass descriptor
    final VkRenderPassCreateInfo info = new VkRenderPassCreateInfo();

    // Add attachments
    info.attachmentCount = attachments.size();
    info.pAttachments = StructureHelper.first(attachments, VkAttachmentDescription::new, Attachment::populate);

    // Add sub-passes
    info.subpassCount = subpasses.size();
    info.pSubpasses = StructureHelper.first(subpasses, VkSubpassDescription::new, SubPassBuilder::populate);

    // Add dependencies
    info.dependencyCount = dependencies.size();
    info.pDependencies = StructureHelper.first(dependencies, VkSubpassDependency::new, DependencyBuilder::populate);

    // Allocate render pass
    final VulkanLibrary lib = dev.library();
    final PointerByReference pass = lib.factory().pointer();
    check(lib.vkCreateRenderPass(dev.handle(), info, null, pass));

    // Create render pass
    return new RenderPass(pass.getValue(), dev, attachments);
}
```

### Sub-Pass

A sub-pass is configured by invoking the following factory on the render pass builder:

```java
public static class Builder {
    /**
     * Starts a sub-pass.
     * @return New sub-pass builder
     */
    public SubPassBuilder subpass() {
        return new SubPassBuilder();
    }
}
```

The builder constructs a list of _references_ to the colour and optional depth-buffer attachments comprising the sub-pass:

```java
public class SubPassBuilder {
    private final List<Reference> colours = new ArrayList<>();
    private Reference depth;
}
```

The `Reference` is a simple transient record:

```java
private record Reference(int index, VkImageLayout layout) {
    private void populate(VkAttachmentReference ref) {
        ref.attachment = index;
        ref.layout = layout;
    }
}
```

Adding an attachment to the sub-pass also registers the attachment in the render pass using the `add` method:

```java
public SubPassBuilder colour(Attachment attachment, VkImageLayout layout) {
    final int index = add(attachment);
    colours.add(new Reference(index, layout));
    return this;
}

public SubPassBuilder depth(Attachment depth, VkImageLayout layout) {
    if(this.depth != null) throw new IllegalArgumentException("Depth attachment already configured");
    final int index = add(depth);
    this.depth = new Reference(index, layout);
    return this;
}
```

The nested builder adds _itself_ to the list of sub-passes and returns control to the _parent_ builder:

```java
public Builder build() {
    if((depth == null) && colours.isEmpty()) throw new IllegalArgumentException("No attachments specified in sub-pass");
    subpasses.add(this);
    return Builder.this;
}
```

Finally we implement the following to populate a sub-pass descriptor (used in the parent builder):

```java
void populate(VkSubpassDescription desc) {
    // Init descriptor
    desc.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

    // Populate colour attachments
    desc.colorAttachmentCount = colours.size();
    desc.pColorAttachments = StructureHelper.first(colours, VkAttachmentReference::new, Reference::populate);

    // Populate depth attachment
    if(depth != null) {
        desc.pDepthStencilAttachment = new VkAttachmentReference();
        depth.populate(desc.pDepthStencilAttachment);
    }
}
```

### Frame Buffers

The final component we require for rendering is the _frame buffer_ that composes the attachments used in the render pass:

```java
public class FrameBuffer extends AbstractVulkanObject {
    private final RenderPass pass;
    private final List<View> attachments;
    private final Dimensions extents;

    /**
     * Constructor.
     * @param handle            Handle
     * @param dev               Logical device
     * @param pass              Render pass
     * @param attachments       Image attachments
     * @param extents           Image extents
     */
    private FrameBuffer(Pointer handle, LogicalDevice dev, RenderPass pass, List<View> attachments, Dimensions extents) {
        super(handle, dev);
        this.extents = notNull(extents);
        this.attachments = List.copyOf(notEmpty(attachments));
        this.pass = notNull(pass);
    }

    /**
     * @return Image attachments
     */
    public List<View> attachments() {
        return attachments;
    }

    @Override
    protected Destructor destructor(VulkanLibrary lib) {
        return lib::vkDestroyFramebuffer;
    }
}
```

A frame buffer is created using the following factory:

```java
public static FrameBuffer create(RenderPass pass, Dimensions extents, List<View> attachments) {
    // Build descriptor
    final VkFramebufferCreateInfo info = new VkFramebufferCreateInfo();
    info.renderPass = pass.handle();
    info.attachmentCount = attachments.size();
    info.pAttachments = Handle.toArray(attachments);
    info.width = extents.width();
    info.height = extents.height();
    info.layers = 1;

    // Allocate frame buffer
    final DeviceContext dev = pass.device();
    final VulkanLibrary lib = dev.library();
    final PointerByReference buffer = lib.factory().pointer();
    check(lib.vkCreateFramebuffer(dev.handle(), info, null, buffer));

    // Create frame buffer
    return new FrameBuffer(buffer.getValue(), dev, pass, attachments, extents);
}
```

Notes:

* The `create` method performs a considerable amount of validation that is omitted for clarity.

* The frame buffer initially has no functionality but will be expanded when we actually address rendering.

* TODO - make this explicitly dependant on swapchain?

TODO - Handle::toArray()

### Integration

We now have all the components we require to add the swapchain and render pass to the demo application.

```java
// Create double-buffer swapchain
Swapchain swapchain = new Swapchain.Builder(dev, surface)
    .count(2)
    .build();
```

The render pass consists of a single colour attachment which is cleared before rendering and transitioned to a layout ready for presentation:

```java
// Create colour attachment
Attachment attachment = new Attachment.Builder()
    .format(VkFormat.B8G8R8A8_UNORM)
    .load(VkAttachmentLoadOp.CLEAR)
    .store(VkAttachmentStoreOp.STORE)
    .finalLayout(VkImageLayout.PRESENT_SRC_KHR)
    .build();
```

We next create the render pass consisting of a single sub-pass that renders the colour attachment:

```java
// Create render pass
RenderPass pass = new RenderPass.Builder()
    .subpass()
        .colour(attachment, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
        .build()
    .build(dev);
```

Finally we create the frame buffers:

```java
```

TODO

---

## Dependency Injection

### Background

Although we are perhaps half-way to our goal of rendering a triangle it is already apparent that our demo code is becoming unwieldy:

* The demo code is currently a single large, imperative method (sometimes referred to as a 'God' class or method).

* The nature of a Vulkan application means that each component is inherently dependant on previously created objects, e.g. frame buffers are dependant on the swapchain and render pass.

As we develop the demo this method becomes larger, harder to navigate, and the inter-dependencies are often obscured by the shear amount of code.

We _could_ attempt to factor out the construction of each component into factory methods, but we would still need some 'master' controller comprising the main components which would be required to invoke the factory methods in the correct order.

The obvious solution is use a _dependency injection_ framework that manages all the components and dependencies, freeing us to focus on developing each component in relative isolation.

For this we use [Spring Boot](https://spring.io/projects/spring-boot) which is one of the most popular and best supported dependency injection frameworks (and also one we have used extensively elsewhere).

### Project

We use the [Spring Initializr](https://start.spring.io/) to generate a Maven POM for a new demo project.

This new project will be separate from the main JOVE library so we manually add a Maven dependency:

```
<dependencies>
    ...
    <dependency>
        <groupId>org.sarge.jove</groupId>
        <artifactId>JOVE</artifactId>
        <version>1.0.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

We also fiddle the `jvmArguments` for the compiler plugin to support JVM preview features:

```
<build>
    <plugins>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <jvmArguments>
                    --enable-preview -XX:+ShowCodeDetailsInExceptionMessages
                </jvmArguments>
            </configuration>
        </plugin>
    </plugins>
</build>
```

The main class is generated for us:

```java
package org.sarge.jove.demo.triangle;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class TriangleDemo {
    public static void main(String[] args) {
        ToStringBuilder.setDefaultStyle(ToStringStyle.SHORT_PREFIX_STYLE);
        SpringApplication.run(TriangleDemo.class, args);
    }
}
```

Although we also add the line to set the Apache `ToStringBuilder` style.

This should run the new application though it obviously doesn't do anything yet.

### Desktop

We start by factoring out the various desktop components into a Spring configuration class:

```java
@Configuration
class DesktopConfiguration {
    private final Desktop desktop = Desktop.create();
    private final Window window;

    public DesktopConfiguration() {
        window = new Window.Builder()
                .title("TriangleDemo")
                .size(new Dimensions(1024, 768))
                .property(Window.Property.DISABLE_OPENGL)
                .build(desktop);
    }

    @PostConstruct
    void validate() {
        if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");
    }

    @Bean
    public Desktop desktop() {
        return desktop;
    }

    @Bean
    public Window window() {
        return window;
    }

    @PreDestroy
    void destroy() {
        window.destroy();
        desktop.destroy();
    }
}
```

Notes:

* The `@Bean` annotation registers components used elsewhere to the Spring container.

* The `@PostConstruct` annotation is used to tell the container to invoke the associated method _after_ the object has been instantiated by the container.

* The `@PreDestroy` annotation is used to invoke the `destroy` method when the application is terminated.

* Therefore `desktop` and `window` are members of the configuration class.

### Vulkan

The configuration class for the Vulkan library and instance is relatively trivial:

```java
@Configuration
@SuppressWarnings("static-method")
class VulkanConfiguration {
    private final VulkanLibrary lib = VulkanLibrary.create();

    @Bean
    public Instance instance(Desktop desktop) {
        return new Instance.Builder()
                .name("TriangleDemo")
                .extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
                .extensions(desktop.extensions())
                .layer(ValidationLayer.STANDARD_VALIDATION)
                .build(lib);
    }

    @Bean
    Handle surface(Instance instance, Window window) {
        return window.surface(instance.handle());
    }
}
```

Here we see the benefits of using dependency injection:

* The code to instantiate each component is now factored out into neater, more concise factory methods.

* We only need to specify the dependant components in the signature of each method which the container injects for us.

* We no longer need to worry about the ordering of component instantiation which is also handled by the container.

This should hopefully result in code that is both simpler to develop and (more importantly) considerably easier to refactor and fix.

### Devices

The code to instantiate the physical and logical device is as follows:

```java
@Configuration
class DeviceConfiguration {
    private final Selector graphics = Selector.of(VkQueueFlag.GRAPHICS);
    private final Selector presentation;

    public DeviceConfiguration(Handle surface) {
        presentation = Selector.of(surface);
    }

    @Bean
    public PhysicalDevice physical(Instance instance) {
        return PhysicalDevice
                .devices(instance)
                .filter(graphics)
                .filter(presentation)
                .findAny()
                .orElseThrow(() -> new RuntimeException("No suitable physical device available"));
    }

    @SuppressWarnings("static-method")
    @Bean
    public Surface surface(Handle handle, PhysicalDevice dev) {
        return new Surface(handle, dev);
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

    @Bean
    public Queue graphics(LogicalDevice dev) {
        return dev.queue(graphics.family());
    }

    @Bean
    public Queue presentation(LogicalDevice dev) {
        return dev.queue(presentation.family());
    }
}
```

### Cleanup

TODO

---

## Summary

In this chapter 



