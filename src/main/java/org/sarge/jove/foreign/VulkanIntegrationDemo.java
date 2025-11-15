package org.sarge.jove.foreign;

import java.nio.file.Path;
import java.util.*;
import java.util.logging.LogManager;

import org.sarge.jove.common.*;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.render.*;

public class VulkanIntegrationDemo {
	void main() throws Exception {
		System.out.println("Initialising logging...");
		try(final var config = VulkanIntegrationDemo.class.getResourceAsStream("/logging.properties")) {
			LogManager.getLogManager().readConfiguration(config);
		}

		System.out.println("Initialising GLFW...");
		final var desktop = Desktop.create();
		System.out.println("version=" + desktop.version());
		System.out.println("Vulkan=" + desktop.isVulkanSupported());

		final var extensions = desktop.extensions();
		System.out.println("extensions=" + extensions);

//		final var monitors = Monitor.monitors(desktop);
//		System.out.println(monitors);
//		monitors.stream().map(m -> m.mode(desktop)).forEach(System.out::println);

		System.out.println("Opening window...");
		final Window window = new Window.Builder()
				.title("DesktopTestTemp")
				.size(new Dimensions(1024, 768))
				.hint(Hint.CLIENT_API, 0)
				.hint(Hint.VISIBLE, 1)
				.build(desktop);

		System.out.println("Initialising Vulkan...");
		final var vulkan = Vulkan.create();

//		System.out.println("Supported instance layers...");
//		for(VkLayerProperties layer : Instance.layers(vulkan.get())) {
//			System.out.println(layer.layerName);
//		}
//		for(VkExtensionProperties ext : Instance.extensions(vulkan.get())) {
//			System.out.println("  " + ext.extensionName);
//		}

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanIntegrationDemo")
				.extension(DiagnosticHandler.EXTENSION)
				.extensions(extensions)
				.layer(Vulkan.STANDARD_VALIDATION)
				.build(vulkan);

		System.out.println("Attaching diagnostic handler...");
		final DiagnosticHandler handler = new DiagnosticHandler.Builder().build(instance, DefaultRegistry.create());			// TODO - registry -> library?
		// TODO - option to exit on errors?

		System.out.println("Getting surface...");
		//final Handle handle = window.surface(instance.handle());
		final var surface = new VulkanSurface(window, instance, vulkan);

		System.out.println("Enumerating devices...");
		final Selector graphicsSelector = Selector.queue(VkQueueFlag.GRAPHICS);
		final Selector presentationSelector = new Selector(surface::isPresentationSupported);
		final PhysicalDevice physical = PhysicalDevice.enumerate(instance, vulkan)
				.filter(graphicsSelector)
				.filter(presentationSelector)
				.toList()
				.getFirst();

		System.out.println("Extracting families...");
		final Family graphicsFamily = graphicsSelector.family(physical);
		final Family presentationFamily = presentationSelector.family(physical);
		System.out.println("graphics=" + graphicsFamily);
		System.out.println("presentation" + presentationFamily);

//		System.out.println("Supported device layers...");
//		for(VkLayerProperties layer : physical.layers()) {
//			System.out.println("  " + layer.layerName);
//		}
//		System.out.println("Supported device extensions...");
//		for(VkExtensionProperties ext : physical.extensions("VK*")) {
//			System.out.println("  " + ext.extensionName);
//		}

		System.out.println("Device properties...");
		final var props = physical.properties();
		System.out.println("name="+props.deviceName);
		System.out.println("type="+props.deviceType);
		System.out.println("bufferImageGranularity="+props.limits.bufferImageGranularity);
		System.out.println("maxPushConstantsSize="+props.limits.maxPushConstantsSize);

//		System.out.println("Retrieving device memory properties...");
//		final var memory = physical.memory();
//		for(int n = 0; n < memory.memoryHeapCount; ++n) {
//			System.out.println("- heap size=%d flags=%s".formatted(memory.memoryHeaps[n].size, memory.memoryHeaps[n].flags.enumerate(ReverseMapping.mapping(VkMemoryHeapFlag.class))));
//		}
//		for(int n = 0; n < memory.memoryTypeCount; ++n) {
//			System.out.println("- type heap=%s props=%s".formatted(memory.memoryTypes[n].heapIndex, memory.memoryTypes[n].propertyFlags.enumerate(ReverseMapping.mapping(VkMemoryProperty.class))));
//		}

		System.out.println("Creating logical device...");
		final var device = new LogicalDevice.Builder(physical)
				.extension(Swapchain.EXTENSION)
				.queue(new RequiredQueue(graphicsFamily))
				.build(vulkan);

		final WorkQueue graphicsQueue = device.queues().get(graphicsFamily).getFirst();
		final WorkQueue presentationQueue = device.queues().get(presentationFamily).getFirst();
		System.out.println("graphics=" + graphicsQueue);
		System.out.println("presentation=" + presentationQueue);

		System.out.println("Creating swapchain...");
		final var swapchain = new Swapchain.Builder(surface.new PropertiesAdapter(physical))
				.clipped(true)
				.presentation(VkPresentModeKHR.MAILBOX_KHR)
				.clear(new Colour(0.6f, 0.6f, 0.6f, 1))
				.build(device);

		// Shaders
		System.out.println("Creating shaders...");
		final var shaderLoader = new ShaderLoader(device);
		final Shader vertex = shaderLoader.load(Path.of("../Demo/Triangle/src/main/resources/spv.triangle.vert"));
		final Shader fragment = shaderLoader.load(Path.of("../Demo/Triangle/src/main/resources/spv.triangle.frag"));

		// Render pass
		System.out.println("Building render pass...");
		final var colour = Attachment.colour(swapchain.format());
		final Subpass subpass = new Subpass.Builder()
				.colour(colour)
				.build();
		final RenderPass pass = new RenderPass.Builder()
				.add(subpass)
				.build(device);

		// Pipeline
		System.out.println("Building pipeline...");
		final var pipelineLayout = new PipelineLayout.Builder().build(device);
		final var pipelineBuilder = new GraphicsPipelineBuilder();
		pipelineBuilder.assembly().topology(Primitive.TRIANGLE);
		pipelineBuilder.viewport().viewportAndScissor(swapchain.extents().rectangle());
		pipelineBuilder.rasterizer().winding(VkFrontFace.CLOCKWISE);
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, vertex));
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStage.FRAGMENT, fragment));
		final Pipeline pipeline = pipelineBuilder
				.pass(pass)
				.layout(pipelineLayout)
				.build(device);

		// Command Pool
		System.out.println("Creating command pool...");
		final var commandPool = Command.Pool.create(device, graphicsQueue, VkCommandPoolCreateFlag.RESET_COMMAND_BUFFER);

		// Frame buffers
		System.out.println("Building frame buffers...");
		final var group = new FrameBuffer.Group(swapchain, pass, List.of());

		// Sequence
		System.out.println("Recording render sequence...");
		final var draw = DrawCommand.draw(3, device);
		final var sequence = commandPool.allocate(group.size(), true);
		for(int n = 0; n < group.size(); ++n) {
			final Command.Buffer cb = sequence.get(n);
			final FrameBuffer fb = group.get(n);
    		cb
        		.begin()
        			.add(fb.begin())
        				.add(pipeline.bind())
        				.add(draw)
        			.add(fb.end())
        		.end();
		}

		// Render
		System.out.println("Rendering...");
		final var available = VulkanSemaphore.create(device);
		final var ready = VulkanSemaphore.create(device);
		final var fence = Fence.create(device);
		for(int n = 0; n < 10; ++n) {
    		final int index = swapchain.acquire(available, null);
    		device.waitIdle();

    		new Work.Builder()
    				.add(sequence.get(index))
    				.wait(available, Set.of(VkPipelineStage.COLOR_ATTACHMENT_OUTPUT))
    				.signal(ready)
    				.build()
    				.submit(fence);
    		device.waitIdle();

    		swapchain.present(presentationQueue, index, ready);
    		device.waitIdle();

    		fence.reset();
    		Thread.sleep(50);
		}

		// Cleanup
		System.out.println("Cleanup...");
		///////////////
		available.destroy();
		ready.destroy();
		fence.destroy();
		///////////////
		commandPool.destroy();
		pipeline.destroy();
		pipelineLayout.destroy();
		group.destroy();
		pass.destroy();
		fragment.destroy();
		vertex.destroy();
		swapchain.destroy();
		device.destroy();
		surface.destroy();
		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("DONE");
	}
}
