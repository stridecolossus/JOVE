package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.geometry.TextureCoordinate.Coordinate2D;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Work.ImmediateCommand;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet;
import org.sarge.jove.platform.vulkan.pipeline.DescriptorSet.Update;
import org.sarge.jove.platform.vulkan.pipeline.FrameBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass;
import org.sarge.jove.platform.vulkan.pipeline.Sampler;
import org.sarge.jove.platform.vulkan.pipeline.SwapChain;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.util.DataSource;

public class TextureQuadDemo {


	public static View texture(LogicalDevice dev, Command.Pool pool) throws IOException {
		// Load image
		final Path dir = Paths.get("./src/test/resources"); // /thiswayup.jpg");
		final var src = DataSource.of(dir);
		final var loader = DataSource.loader(src, new ImageData.Loader());
//		final ImageData image = loader.load("heightmap.gif"); // "thiswayup.jpg");
		final ImageData image = loader.load("thiswayup.png");
		final VkFormat format = FormatBuilder.format(image);
		//System.out.println(format);

		// Copy image to staging buffer
		final VertexBuffer staging = VertexBuffer.staging(dev, image.data().limit());
		staging.load(image.data());

		// Create texture
		final Image texture = new Image.Builder(dev)
				.extents(Image.Extents.of(image.size()))
				.format(format)
				.aspect(VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT)
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
					.build()
				.build()
				.submit(pool, true);

		// Copy staging to texture
		new ImageCopyCommand.Builder()
				.buffer(staging)
				.image(texture)
				.layout(VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL)
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

		final View view = View.of(dev, texture);

		return view;
	}

	public static void main(String[] args) throws Exception {
		// Open desktop
		final Desktop desktop = Desktop.create();
		if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");

		// Create window
		final var descriptor = new Window.Descriptor.Builder()
				.title("demo")
				.size(new Dimensions(1280, 760))
				.property(Window.Property.DISABLE_OPENGL)
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
		instance.handlers().add(handler);

		// Lookup surface
		final Handle surfaceHandle = window.surface(instance.handle());

		// Create queue family predicates
		final var graphicsPredicate = Queue.Family.predicate(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT);
		final var transferPredicate = Queue.Family.predicate(VkQueueFlag.VK_QUEUE_TRANSFER_BIT);
		final var presentationPredicate = Queue.Family.predicate(surfaceHandle);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(PhysicalDevice.predicate(graphicsPredicate))
				.filter(PhysicalDevice.predicate(transferPredicate))
				.filter(PhysicalDevice.predicate(presentationPredicate))
				.findAny()
				.orElseThrow(() -> new RuntimeException("No GPU available"));

		// Lookup required queues
		final Queue.Family graphicsFamily = gpu.family(graphicsPredicate);
		final Queue.Family transferFamily = gpu.family(transferPredicate);
		final Queue.Family presentFamily = gpu.family(presentationPredicate);

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				//.queue(graphics) TODO!!!
				.queue(transferFamily)
				.queue(presentFamily)
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
		final SwapChain chain = new SwapChain.Builder(dev, surface)
				.count(2)
				.format(format)
				.space(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
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
//				.dependency()
//					.source(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
//					.destination(0)
//					.destination(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
//					.destination(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
//					.build()
				.build();

		// Load shaders
		final Path dir = new File("./src/test/resources/demo/texture.quad").toPath(); // TODO - root + resolve
		final var src = DataSource.of(dir);
		final var shaderLoader = DataSource.loader(src, Shader.loader(dev));
		final Shader vert = shaderLoader.load("spv.quad.vert");
		final Shader frag = shaderLoader.load("spv.quad.frag");

		//////////////////

		// Build triangle vertices
		final Vertex[] vertices = {
				new Vertex.Builder().position(new Point(-0.5f, -0.5f, 0)).coords(Coordinate2D.TOP_LEFT).build(),
				new Vertex.Builder().position(new Point(-0.5f, +0.5f, 0)).coords(Coordinate2D.BOTTOM_LEFT).build(),
				new Vertex.Builder().position(new Point(+0.5f, -0.5f, 0)).coords(Coordinate2D.TOP_RIGHT).build(),
				new Vertex.Builder().position(new Point(+0.5f, +0.5f, 0)).coords(Coordinate2D.BOTTOM_RIGHT).build(),
		};

		// Define vertex layout
		final Vertex.Layout layout = new Vertex.Layout(Vertex.Component.POSITION, Vertex.Component.TEXTURE_COORDINATE);

		// Create interleaved buffer
		final ByteBuffer bb = ByteBuffer.allocate(vertices.length * layout.size() * Float.BYTES).order(ByteOrder.nativeOrder());
		for(Vertex v : vertices) {
			layout.buffer(v, bb);
		}
		bb.rewind();

		// Create staging VBO
		final VertexBuffer staging = VertexBuffer.staging(dev, bb.limit());

		// Load to staging
		staging.load(bb);

		// Create device VBO
		final VertexBuffer dest = new VertexBuffer.Builder(dev)
				.length(bb.limit())
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
				.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

		// Copy
		final Command.Pool copyPool = Command.Pool.create(dev.queue(transferFamily));
		ImmediateCommand.of(staging.copy(dest)).submit(copyPool, true);

		staging.destroy();

		//////////////////

		final Command.Pool graphicsPool = Command.Pool.create(dev.queue(graphicsFamily));

		final View texture = texture(dev, graphicsPool);

		// Create descriptor layout
		final DescriptorSet.Layout.Binding binding = new DescriptorSet.Layout.Binding.Builder()
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.build();

		final DescriptorSet.Layout setLayout = DescriptorSet.Layout.create(dev, List.of(binding));

		// Create pool
		final DescriptorSet.Pool setPool = new DescriptorSet.Pool.Builder(dev)
				.add(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER, 3)
				.max(3)
				.build();

		final List<DescriptorSet> descriptors = setPool.allocate(setLayout, chain.views().size());

		// Create sampler
		final Sampler sampler = new Sampler.Builder(dev).build();
		final var res = sampler.resource(texture);
		final Collection<Update<?>> updates = descriptors.stream().map(set -> set.update(binding, res)).collect(toList());
		DescriptorSet.update(dev, updates);
//		for(DescriptorSet set : descriptors) {
//			final var write = set.update(binding, res);
//			DescriptorSet.update(dev, Set.of(write));
//			//DescriptorSet.Update.apply(dev, List.of(write));
//		}
//		// Apply sampler to the descriptor sets
//		new DescriptorSet.Update.Builder()
//			.descriptors(descriptors)
//			.add(0, sampler.update(texture))
//			.update(dev);

		//////////////////

		// Create pipeline layout
		final Pipeline.Layout pipelineLayout = new Pipeline.Layout.Builder(dev)
				.add(setLayout)
				.build();

		// Create pipeline
		final Pipeline pipeline = new Pipeline.Builder(dev)
				.layout(pipelineLayout)
				.pass(pass)
				.input()
					.binding(layout)
					.build()
				.viewport(new Rectangle(chain.extents()))
				.rasterizer()
					.cullMode(VkCullModeFlag.VK_CULL_MODE_NONE)
					.build()
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
				.map(view -> FrameBuffer.create(List.of(view), pass))
				.collect(toList());

		// Create command pool
		final Queue presentQueue = dev.queue(presentFamily);
		final Command.Pool pool = Command.Pool.create(presentQueue);
		final List<Command.Buffer> commands = pool.allocate(buffers.size());

		// Record render commands
		final Command draw = (api, handle) -> api.vkCmdDraw(handle, vertices.length, 1, 0, 0);		// TODO - builder
		for(int n = 0; n < commands.size(); ++n) {
			final Command.Buffer cb = commands.get(n);
			cb
				.begin()
					.add(pass.begin(buffers.get(n)))
					.add(pipeline.bind())
					.add(dest.bindVertexBuffer())
					.add(descriptors.get(n).bind(pipelineLayout))
					.add(draw)
					.add(RenderPass.END_COMMAND)
				.end();
		}

//		final Semaphore ready = Semaphore.create(dev);
//		final Semaphore finished = Semaphore.create(dev);

		for(int n = 0; n < 25; ++n) {
			final int index = chain.acquire(null, null);

			new Work.Builder()
					.add(commands.get(index))
//					.wait(ready)
//					.signal(finished)
					.stage(VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT)
					.build()
					.submit();

			presentQueue.waitIdle();
			Thread.sleep(50);

			chain.present(presentQueue, null);

			presentQueue.waitIdle();
			Thread.sleep(50);

//			dev.queue(present).waitIdle();
		}

			//Thread.sleep(2500);

		//////////////

		// Destroy window
		surface.destroy();
		window.destroy();
		desktop.destroy();

		final Image.DefaultImage img = (Image.DefaultImage) texture.image();
		img.destroy();
		texture.destroy();
		sampler.destroy();

		setPool.destroy();
		setLayout.destroy();

		pool.destroy();
		copyPool.destroy();
		graphicsPool.destroy();

		vert.destroy();
		frag.destroy();

		// Destroy render pass
		buffers.forEach(FrameBuffer::destroy);
		pass.destroy();

		// Destroy pipeline
		pipelineLayout.destroy();
		pipeline.destroy();
		chain.destroy();

		// Destroy device
		dev.destroy();
		instance.destroy();
	}
}
