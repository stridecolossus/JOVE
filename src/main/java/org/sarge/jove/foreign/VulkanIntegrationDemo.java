package org.sarge.jove.foreign;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.LogManager;

import org.sarge.jove.common.*;
import org.sarge.jove.control.*;
import org.sarge.jove.control.Button.ButtonEvent;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;
import org.sarge.jove.model.Coordinate.Coordinate2D;
import org.sarge.jove.platform.desktop.*;
import org.sarge.jove.platform.desktop.Window.Hint;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.RequiredQueue;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.Selector;
import org.sarge.jove.platform.vulkan.core.WorkQueue.Family;
import org.sarge.jove.platform.vulkan.image.ClearValue.ColourClearValue;
import org.sarge.jove.platform.vulkan.memory.*;
import org.sarge.jove.platform.vulkan.pipeline.*;
import org.sarge.jove.platform.vulkan.pipeline.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.pipeline.VertexInputStage.*;
import org.sarge.jove.platform.vulkan.present.*;
import org.sarge.jove.platform.vulkan.present.ImageCountSwapchainConfiguration.Policy;
import org.sarge.jove.platform.vulkan.present.SwapchainManager.SwapchainConfiguration;
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

		//////////////////

		final var running = new AtomicBoolean(true);
		final var exit = new Action<>("exit", ButtonEvent.class, _ -> running.set(false));
		final var dump = new Action<>("dump", ButtonEvent.class, System.out::println);
		final var bindings = new ActionBindings(List.of(exit, dump));
		final var keyboard = new KeyboardDevice(window);
		final var adapter = new ButtonDeviceAdapter(keyboard);
		//final int escape = KeyTable.defaultKeyTable().code("ESCAPE");
		bindings.bind(exit, adapter.button(256));		// TODO - should be actual button/template?
		bindings.bind(dump, adapter.button(65));

		//////////////////

		System.out.println("Initialising Vulkan...");
		final var vulkan = Vulkan.create();

		System.out.println("Creating instance...");
		final Instance instance = new Instance.Builder()
				.name("VulkanIntegrationDemo")
				.extension(DiagnosticHandler.EXTENSION)
				.extensions(extensions)
				.layer(DiagnosticHandler.STANDARD_VALIDATION)
				.build(vulkan);

//		System.out.println("Attaching diagnostic handler...");
//		final DiagnosticHandler handler = new DiagnosticHandler.Builder().build(instance, DefaultRegistry.create());			// TODO - registry -> library?
//		// TODO - option to exit on errors?

		System.out.println("Getting surface...");
		final var surface = new VulkanSurface(window, instance, vulkan);

		System.out.println("Enumerating devices...");
		final Selector graphicsSelector = Selector.queue(VkQueueFlags.GRAPHICS);
		final Selector presentationSelector = new Selector(surface::isPresentationSupported);
		final PhysicalDevice physical = PhysicalDevice
				.enumerate(instance, vulkan)
				.filter(graphicsSelector)
				.filter(presentationSelector)
				.findAny()
				.orElseThrow();

		System.out.println("Extracting families...");
		final Family graphicsFamily = graphicsSelector.family(physical);
		final Family presentationFamily = presentationSelector.family(physical);
		System.out.println("graphics=" + graphicsFamily);
		System.out.println("presentation" + presentationFamily);

		System.out.println("Device properties...");
		final var props = physical.properties();
		System.out.println("name="+props.deviceName);
		System.out.println("type="+props.deviceType);
		System.out.println("bufferImageGranularity="+props.limits.bufferImageGranularity);
		System.out.println("maxPushConstantsSize="+props.limits.maxPushConstantsSize);
		System.out.println("maxMemoryAllocationCount="+props.limits.maxMemoryAllocationCount);

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
				.layer(DiagnosticHandler.STANDARD_VALIDATION)
				.queue(new RequiredQueue(graphicsFamily))
				.build(vulkan);

		final WorkQueue graphicsQueue = device.queues().get(graphicsFamily).getFirst();
		final WorkQueue presentationQueue = device.queues().get(presentationFamily).getFirst();
		System.out.println("graphics=" + graphicsQueue);
		System.out.println("presentation=" + presentationQueue);

		System.out.println("Creating swapchain...");
		final var properties = surface.properties(physical);
		final var builder = new Swapchain.Builder()
				.clipped(true)
				.init(properties.capabilities());

		System.out.println("Creating swapchain factory...");
		final SwapchainConfiguration[] configuration = {
				new ImageCountSwapchainConfiguration(Policy.MIN),
				new SurfaceFormatSwapchainConfiguration(VkFormat.B8G8R8A8_UNORM, VkColorSpaceKHR.SRGB_NONLINEAR_KHR),
				new PresentationModeSwapchainConfiguration(List.of(VkPresentModeKHR.MAILBOX_KHR)),
				new SharingModeSwapchainConfiguration(List.of(graphicsFamily, presentationFamily)),
				new ExtentSwapchainConfiguration()
		};
		final var factory = new SwapchainManager(device, properties, builder, List.of(configuration));

		// Shaders
		System.out.println("Creating shaders...");
		final var shaderLoader = new ShaderLoader(device);
		final Shader vertex = shaderLoader.load(Path.of("../Demo/RotatingCube/src/main/resources/spv.quad.vert"));
		final Shader fragment = shaderLoader.load(Path.of("../Demo/RotatingCube/src/main/resources/spv.quad.faked.frag"));

		// Render pass
		System.out.println("Building render pass...");
		final var colour = AttachmentDescription.colour(factory.swapchain().format());
		final var attachment = factory.attachment(colour);
		attachment.clear(new ColourClearValue(new Colour(0.4f, 0.4f, 0.4f)));
		final Subpass subpass = new Subpass(Set.of(), List.of(AttachmentReference.of(attachment)));
		final RenderPass pass = new RenderPass.Builder()
				.add(subpass)
				.build(device);

		///////////////

		final VertexBinding binding = new VertexBinding.Builder()
        		.attribute(new VertexAttribute(0, VkFormat.R32G32B32_SFLOAT, 0))
        		.attribute(new VertexAttribute(1, VkFormat.R32G32_SFLOAT, 3 * 4))
        		.stride((3 + 2) * 4)
				.build();

		final VulkanBuffer b = vbo(device, physical, graphicsQueue);
		final var vbo = new VertexBuffer(b);

		///////////////

		// Pipeline
		System.out.println("Building pipeline...");
		final var pipelineLayout = new PipelineLayout.Builder().build(device);
		final var pipelineBuilder = new GraphicsPipelineBuilder();
		///////////////
		pipelineBuilder.input().add(binding);
		pipelineBuilder.assembly().topology(Primitive.TRIANGLE_STRIP);
		///////////////
		pipelineBuilder.viewport().viewportAndScissor(new Rectangle(factory.swapchain().extents()));
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStageFlags.VERTEX, vertex));
		pipelineBuilder.shader(new ProgrammableShaderStage(VkShaderStageFlags.FRAGMENT, fragment));
		final Pipeline pipeline = pipelineBuilder
				.pass(pass)
				.layout(pipelineLayout)
				.build(device);

		// Command Pool
		System.out.println("Creating command pool...");
		final var pool = Command.Pool.create(device, graphicsQueue, VkCommandPoolCreateFlags.RESET_COMMAND_BUFFER);

		// Sequence
		System.out.println("Recording render sequence...");
		final var draw = DrawCommand.of(4, device);
		final RenderSequence sequence = (_, buffer) -> {
			buffer.add(pipeline.bind());
			buffer.add(vbo.bind(0));
			buffer.add(draw);
		};
		final var composer = new FrameComposer(pool, sequence);
		final var framebuffers = new Framebuffer.Factory(pass);
		final var render = new RenderTask(factory, framebuffers::create, composer);

		// Render...
		System.out.println("Rendering...");
		final var tracker = new Frame.Tracker();
		final var counter = new FrameCounter();
		tracker.add(counter);
		try(var loop = new RenderLoop(render, tracker)) {
			loop.start();
			while(running.get()) {
				Thread.sleep(50);
				desktop.poll();
			}
			loop.stop();
		}
		System.out.println(counter);

		// Cleanup
		System.out.println("Cleanup...");
		device.waitIdle();
///////////////
b.destroy();
///////////////
		render.destroy();
		pool.destroy();
		pipeline.destroy();
		pipelineLayout.destroy();
		pass.destroy();
		fragment.destroy();
		vertex.destroy();
		factory.destroy();
		device.destroy();
		surface.destroy();
//		handler.destroy();
		instance.destroy();
		window.destroy();
		desktop.destroy();

		System.out.println("DONE");
	}

	private static VulkanBuffer vbo(LogicalDevice device, PhysicalDevice physical, WorkQueue queue) {

		// Builds mesh

		final Vertex[] vertices = {
			new Vertex(new Point(-0.5f, -0.5f, 0), Coordinate2D.TOP_LEFT),
			new Vertex(new Point(-0.5f, +0.5f, 0), Coordinate2D.BOTTOM_LEFT),
			new Vertex(new Point(+0.5f, -0.5f, 0), Coordinate2D.TOP_RIGHT),
			new Vertex(new Point(+0.5f, +0.5f, 0), Coordinate2D.BOTTOM_RIGHT),
		};

		final var mesh = new MutableMesh(Primitive.TRIANGLE_STRIP, Point.LAYOUT, Coordinate2D.LAYOUT);
		for(Vertex v : vertices) {
			mesh.add(v);
		}

		final var data = mesh.vertices();

		// Init memory

		final var types = MemoryType.enumerate(physical.memory());
		final var allocator = new Allocator(device, types);

		// Create staging

//		final var stagingProperties = new MemoryProperties.Builder<VkBufferUsageFlag>()
//				.required(VkMemoryProperty.HOST_VISIBLE)
//				.optimal(VkMemoryProperty.DEVICE_LOCAL)
//				.usage(VkBufferUsageFlag.TRANSFER_SRC)
//				.build();
//
//		final var staging = VulkanBuffer.create(allocator, data.length(), stagingProperties); //  4 * (3 + 2) * 4, stagingProperties);

		final var staging = VulkanBuffer.staging(allocator, data.length());

		/*
		final var mem = staging.memory().map(0L, 80).segment(0L, 80);

		mem.set(ValueLayout.JAVA_FLOAT, 0L, -0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 4L, -0.5f);

		mem.set(ValueLayout.JAVA_FLOAT, 20L, -0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 24L, +0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 36L, 1);

		mem.set(ValueLayout.JAVA_FLOAT, 40L, +0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 44L, -0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 52L, 1);

		mem.set(ValueLayout.JAVA_FLOAT, 60L, +0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 64L, +0.5f);
		mem.set(ValueLayout.JAVA_FLOAT, 72L, 1);
		mem.set(ValueLayout.JAVA_FLOAT, 76L, 1);
		*/

		final ByteBuffer bb = staging.buffer();
		data.buffer(bb);

		// Create VBO
		final var destProperties = new MemoryProperties.Builder<VkBufferUsageFlags>()
				.required(VkMemoryPropertyFlags.DEVICE_LOCAL)
				.usage(VkBufferUsageFlags.TRANSFER_DST)
				.usage(VkBufferUsageFlags.VERTEX_BUFFER)
				.build();

		final var dest = VulkanBuffer.create(allocator, data.length(), destProperties);

		// Copy staging to VBO
		final var pool = Command.Pool.create(device, queue);
		Work.submit(staging.copy(dest), pool);

		staging.destroy();
		pool.destroy();

		return dest;
	}
}
