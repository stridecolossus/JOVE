package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Handle;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.DesktopService;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkDesktopService;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.PhysicalDevice.QueueFamily;
import org.sarge.jove.platform.vulkan.core.Work.ImmediateCommand;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.pipeline.FrameBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass;
import org.sarge.jove.platform.vulkan.pipeline.SwapChain;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.platform.vulkan.util.FormatBuilder.Type;
import org.sarge.jove.util.DataSource;

import com.sun.jna.ptr.PointerByReference;

public class RotatingCubeDemo {


	public static Image texture(LogicalDevice dev, Command.Pool pool) throws IOException {
		// Load image
		final File dir = new File("./src/test/resources"); // /thiswayup.jpg");
		final ImageData.Loader loader = new ImageData.Loader(DataSource.file(dir));
//		final ImageData image = loader.load("heightmap.gif"); // "thiswayup.jpg");
		final ImageData image = loader.load("thiswayup.jpg");

		// Copy image to staging buffer
		final ByteBuffer bb = image.buffer();
		final VertexBuffer staging = VertexBuffer.staging(dev, bb.capacity());
		staging.load(bb);

		// Determine texture format for this image
		// TODO - helper on image builder?
		final VkFormat format = new FormatBuilder()
				.components(image.components().size())
				.bytes(1)
				.signed(false)
				.type(Type.NORMALIZED)
				.build();
				// VkFormat.VK_FORMAT_R8G8B8A8_SRGB|UNORM

		// Create texture
		final Image texture = new Image.Builder(dev)
				.extents(Image.Extents.of(image.size()))
				.format(format)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Transition texture ready for copying
		new Barrier.Builder()
				.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
				.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
				.barrier(texture)
					.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
					.destination(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
					// TODO - subresource copied?
					.build()
				.build()
				.submit(pool, true);

		// Copy staging to texture
		new ImageCopyCommand.Builder()
				.buffer(staging)
				.image(texture)
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
				.subresource()
					.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)		// TODO - init from image?
					.build()
				.build()
				.submit(pool, true);

		// Release staging
		staging.destroy();

		// Transition texture ready for sampling
		// TODO - source flag & access flag and old-layout could be initialised from previous barrier?
		new Barrier.Builder()
				.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT)
				.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT)
				.barrier(texture)
					.oldLayout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL)
					.source(VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT)
					.destination(VkAccessFlag.VK_ACCESS_SHADER_READ_BIT)
					.build()
				.build()
				.submit(pool, true);

		// Create sampler
		final Sampler sampler = new Sampler.Builder(dev).build();

		return null;
	}

	public static void main(String[] args) throws Exception {
		// Open desktop
		final DesktopService desktop = FrameworkDesktopService.create();
		if(!desktop.isVulkanSupported()) throw new ServiceException("Vulkan not supported");

		// Create window
		final var descriptor = new Window.Descriptor.Builder()
				.title("demo")
				.size(new Dimensions(1280, 760))
				.property(Window.Descriptor.Property.DISABLE_OPENGL)
				.build();
		final Window window = desktop.window(descriptor);
		// TODO - any point in separate Window class? does it help at all?

		// Init Vulkan
		final VulkanLibrary lib = VulkanLibrary.create();

		// Create instance
		final Instance instance = new Instance.Builder()
				.vulkan(lib)
				.name("test")
				.extension(VulkanLibrary.EXTENSION_DEBUG_UTILS)
				.extensions(desktop.extensions())
				.layer(ValidationLayer.STANDARD_VALIDATION)
				.build();

		// Attach message handler
		final var handler = new MessageHandler.Builder()
				.init()
				.callback(MessageHandler.CONSOLE)
				.build();
		instance.add(handler);

		// Lookup surface
		final Handle surfaceHandle = window.surface(instance.handle(), PointerByReference::new);

		// Create queue family predicates
		final var graphicsPredicate = PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
		final var transferPredicate = PhysicalDevice.predicate(VkQueueFlag.VK_QUEUE_TRANSFER_BIT);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(PhysicalDevice.predicate(graphicsPredicate))
				.filter(PhysicalDevice.predicate(transferPredicate))
				.filter(PhysicalDevice.predicatePresentationSupported(surfaceHandle))
				.findAny()
				.orElseThrow(() -> new ServiceException("No GPU available"));

		// Lookup required queues
//		final QueueFamily graphics = gpu.find(graphicsPredicate, "Graphics family not available");
		final QueueFamily transfer = gpu.find(transferPredicate, "Transfer family not available");
		final QueueFamily present = gpu.find(family -> family.isPresentationSupported(surfaceHandle), "Presentation family not available");

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder() // TODO - parent as ctor arg
				.parent(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
//				.queue(graphics)
				.queue(transfer)
				.queue(present)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, dev);

		// Specify required image format
		final VkFormat format = new FormatBuilder()
				.components(FormatBuilder.BGRA)
				.bytes(1)
				.signed(false)
				.type(FormatBuilder.Type.NORMALIZED)
				.build();

		// Create swap-chain
		final SwapChain chain = new SwapChain.Builder(surface)
				.count(2)
				.format(format)
				.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.build();

		// Create render pass
		final RenderPass pass = new RenderPass.Builder(dev)
				.attachment()
					.format(format)
					.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
					.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
					.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
					.build()
				.subpass()
					.colour(0, VkImageLayout.VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL)
					.build()
				.build();

		// Load shaders
		final Shader.Loader loader = Shader.Loader.create("./src/test/resources/demo/vertex.buffer", dev);
		final Shader vert = loader.load("spv.triangle.vert");
		final Shader frag = loader.load("spv.triangle.frag");

		//////////////////

		// Build triangle vertices
		final Vertex[] vertices = {
				new Vertex.Builder().position(new Point(0, -0.5f, 0)).colour(new Colour(1, 0, 0, 1)).build(),
				new Vertex.Builder().position(new Point(0.5f, 0.5f, 0)).colour(new Colour(0, 1,  0, 1)).build(),
				new Vertex.Builder().position(new Point(-0.5f, 0.5f, 0)).colour(new Colour(0, 0, 1, 1)).build(),
		};

		// Define vertex layout
		final Vertex.Layout layout = new Vertex.Layout(List.of(Vertex.Component.POSITION, Vertex.Component.COLOUR));

		// Buffer vertices
		final ByteBuffer bb = layout.buffer(Arrays.asList(vertices));

		// Create staging VBO
		final VertexBuffer staging = VertexBuffer.staging(dev, bb.capacity());

		// Load to staging
		staging.load(bb);

		// Create device VBO
		final VertexBuffer dest = new VertexBuffer.Builder(dev)
				.length(bb.capacity())
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Copy
		final Command.Pool copyPool = Command.Pool.create(dev.queue(transfer));
		ImmediateCommand.of(staging.copy(dest)).submit(copyPool, true);


//		return ImmediateCommand.of((api, buffer) -> api.vkCmdCopyBuffer(buffer, VertexBuffer.this.handle(), dest.handle(), 1, new VkBufferCopy[]{region}));

		//staging.copy(dest).submit(copyPool, true);

		//////////////////

		final Command.Pool graphicsPool = null; // TODO - Command.Pool.create(queue, flags)
		texture(dev, graphicsPool);

		//////////////////

		// Create pipeline
		final Rectangle rect = new Rectangle(chain.extents());
		final Pipeline pipeline = new Pipeline.Builder(dev)
				.pass(pass)
				.input()
					.binding(layout)
					.build()
				.viewport(rect)
				.shader()
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
					.shader(vert)
					.build()
				.shader()
					.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
					.shader(frag)
					.build()
				.build();

		// Create frame buffers
		final var buffers = chain
				.views()
				.stream()
				.map(view -> FrameBuffer.create(view, pass))
				.collect(toList());

		// Create command pool
		final LogicalDevice.Queue presentQueue = dev.queue(present);
		final Command.Pool pool = Command.Pool.create(presentQueue);
		final List<Command.Buffer> commands = pool.allocate(buffers.size());

		// Record render commands
		final Command draw = (api, handle) -> api.vkCmdDraw(handle, 3, 1, 0, 0);		// TODO - builder
		final Colour grey = new Colour(0.3f, 0.3f, 0.3f, 1);
		for(int n = 0; n < commands.size(); ++n) {
			final Command.Buffer cb = commands.get(n);
			cb
				.begin()
					.add(pass.begin(buffers.get(n), rect, grey))
					.add(pipeline.bind())
					.add(dest.bind())
					.add(draw)
					.add(RenderPass.END_COMMAND)
				.end();
		}

//		final Semaphore ready = Semaphore.create(dev);
//		final Semaphore finished = Semaphore.create(dev);

//		for(int n = 0; n < 100; ++n) {
			final int index = chain.acquire(null, null);

			new Work.Builder()
					.add(commands.get(index))
//					.wait(ready)
//					.signal(finished)
					.stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
					.build()
					.submit();

			presentQueue.waitIdle();

//			Thread.sleep(50);

			chain.present(presentQueue, null);


//			dev.queue(present).waitIdle();
//		}
			Thread.sleep(2500);

		//////////////

		// Destroy window
		surface.destroy();
		window.destroy();
		desktop.close();

		// Destroy render pass
		buffers.forEach(FrameBuffer::destroy);
		pool.destroy();
		pass.destroy();

		// Destroy pipeline
		vert.destroy();
		frag.destroy();
		pipeline.destroy();
		chain.destroy();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
