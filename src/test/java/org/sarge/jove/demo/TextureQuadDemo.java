package org.sarge.jove.demo;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Set;

import org.sarge.jove.common.Bufferable;
import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Coordinate.Coordinate2D;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.ImageData;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.platform.desktop.Desktop;
import org.sarge.jove.platform.desktop.Window;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.api.VulkanLibrary;
import org.sarge.jove.platform.vulkan.common.Command;
import org.sarge.jove.platform.vulkan.common.Queue;
import org.sarge.jove.platform.vulkan.common.ValidationLayer;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.LogicalDevice.Semaphore;
import org.sarge.jove.platform.vulkan.core.Message.HandlerBuilder;
import org.sarge.jove.platform.vulkan.core.Shader.ShaderLoader;
import org.sarge.jove.platform.vulkan.memory.MemoryProperties;
import org.sarge.jove.platform.vulkan.pipeline.Barrier;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.PipelineLayout;
import org.sarge.jove.platform.vulkan.render.DescriptorSet;
import org.sarge.jove.platform.vulkan.render.FrameBuffer;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.render.Sampler;
import org.sarge.jove.platform.vulkan.render.Swapchain;
import org.sarge.jove.platform.vulkan.util.FormatHelper;
import org.sarge.jove.platform.vulkan.util.VulkanHelper;
import org.sarge.jove.util.DataSource;
import org.sarge.jove.util.ResourceLoader;

public class TextureQuadDemo {
	public static View texture(LogicalDevice dev, Command.Pool pool) throws IOException {
		// Load image
		final Path dir = Paths.get("./src/test/resources");
		final var src = DataSource.of(dir);
		final var loader = ResourceLoader.of(src, new ImageData.Loader());
		final ImageData image = loader.load("thiswayup.png");
		final VkFormat format = FormatHelper.format(image.layout());		// R8G8B8A8_SRGB

		// Copy image to staging buffer
		final int len = 4 * image.size().width() * image.size().height() * image.layout().bytes(); // TODO - property of image
		final VulkanBuffer staging = VulkanBuffer.staging(dev, len);
		image.data().write(staging.memory().map()); // TODO - ugly

		// Init image descriptor
		final Image.Descriptor descriptor = new Image.Descriptor.Builder()
				.format(format)
				.extents(Image.Extents.of(image.size()))		// TODO - helper
				.aspect(VkImageAspect.COLOR)
				.build();

		// Init image memory properties
		final MemoryProperties<VkImageUsage> props = new MemoryProperties.Builder()
				.required(VkMemoryPropertyFlag.DEVICE_LOCAL)
				.usage(VkImageUsage.TRANSFER_DST)
				.usage(VkImageUsage.SAMPLED)
				.build();

		// Create texture
		final Image texture = new Image.Builder()
				.descriptor(descriptor)
				.properties(props)
				.build(dev);

		// Transition texture ready for copying
		new Barrier.Builder()
				.source(VkPipelineStage.TOP_OF_PIPE)
				.destination(VkPipelineStage.TRANSFER)
				.barrier(texture)
					.newLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.destination(VkAccess.TRANSFER_WRITE)
					.build()
				.build()
				.submit(pool);

		// Copy staging to texture
		new ImageCopyCommand.Builder()
				.image(texture)
				.buffer(staging)
				.layout(VkImageLayout.TRANSFER_DST_OPTIMAL)
				.build()
				.submit(pool);

		// Release staging
		staging.destroy();

		// Transition texture ready for sampling
		// TODO - source flag & access flag and old-layout could be initialised from previous barrier?
		new Barrier.Builder()
				.source(VkPipelineStage.TRANSFER)
				.destination(VkPipelineStage.FRAGMENT_SHADER)
				.barrier(texture)
					.oldLayout(VkImageLayout.TRANSFER_DST_OPTIMAL)
					.newLayout(VkImageLayout.SHADER_READ_ONLY_OPTIMAL)
					.source(VkAccess.TRANSFER_WRITE)
					.destination(VkAccess.SHADER_READ)
					.build()
				.build()
				.submit(pool);

		return texture.view();
	}

	public static void main(String[] args) throws Exception {
		// Open desktop
		final Desktop desktop = Desktop.create();
		if(!desktop.isVulkanSupported()) throw new RuntimeException("Vulkan not supported");

		// Create window
		final Window window = new Window.Builder(desktop)
				.title("demo")
				.size(new Dimensions(1280, 760))
				.property(Window.Property.DISABLE_OPENGL)
				.build();

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
		instance.attach(new HandlerBuilder().init().build());

		// Lookup surface
		final Handle surfaceHandle = window.surface(instance.handle());

		// Create queue family predicates
		final var graphics = Queue.Selector.of(VkQueueFlag.GRAPHICS);
		final var transfer = Queue.Selector.of(VkQueueFlag.TRANSFER);
		final var present  = Queue.Selector.of(surfaceHandle);

		// Find GPU
		final PhysicalDevice gpu = PhysicalDevice
				.devices(instance)
				.filter(graphics)
				.filter(transfer)
				.filter(present)
				.findAny()
				.orElseThrow(() -> new RuntimeException("No GPU available"));

		// Create device
		final LogicalDevice dev = new LogicalDevice.Builder(gpu)
				.extension(VulkanLibrary.EXTENSION_SWAP_CHAIN)
				.layer(ValidationLayer.STANDARD_VALIDATION)
				//.queue(graphics) TODO!!!
				.queue(transfer)
				.queue(present)
				.build();

		// Create rendering surface
		final Surface surface = new Surface(surfaceHandle, gpu);

		// Specify required image format
		// TODO - do we introduce format builder here or later?
		final VkFormat format = new FormatHelper()
				.template(FormatHelper.BGRA)
				.bytes(1)
				.signed(false)
				.type(FormatHelper.Type.NORMALIZED)
				.build();

		// Create swap-chain
		final Swapchain chain = new Swapchain.Builder(dev, surface)
				.count(2)
				.format(format)
				.space(VkColorSpaceKHR.SRGB_NONLINEAR_KHR)
				.clear(new Colour(0.3f, 0.3f, 0.3f, 1))
				.build();

		// Create render pass
		final RenderPass pass = new RenderPass.Builder(dev)
				.attachment()
					.format(format)
					.load(VkAttachmentLoadOp.CLEAR)
					.store(VkAttachmentStoreOp.STORE)
					.finalLayout(VkImageLayout.PRESENT_SRC_KHR)
					.build()
				.subpass()
					.colour(0, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
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
//		final var shaderLoader = DataSource.loader(new CompileShaderDataSource(DataSource.of(dir)), Shader.loader(dev));
		final DataSource src = DataSource.of(dir);
		final var loader = ResourceLoader.of(src, new ShaderLoader(dev));
		final Shader vert = loader.load("spv.quad.vert");
		final Shader frag = loader.load("spv.quad.frag");

		//////////////////

		// Build triangle vertices
		final var vertices = Bufferable.of(
				new Point(-0.5f, -0.5f, 0), Coordinate2D.TOP_LEFT,
				new Point(-0.5f, +0.5f, 0), Coordinate2D.BOTTOM_LEFT,
				new Point(+0.5f, -0.5f, 0), Coordinate2D.TOP_RIGHT,
				new Point(+0.5f, +0.5f, 0), Coordinate2D.BOTTOM_RIGHT
		);

		// Convert to buffer
		final int len = vertices.length();
		final ByteBuffer bb = VulkanHelper.buffer(len);		// TODO - blog: note native order
		vertices.buffer(bb);
		bb.rewind(); // TODO - should do this for us?

		// Load to staging
		final VulkanBuffer staging = VulkanBuffer.staging(dev, len);
		staging.memory().map().write(bb);

		// Create VBO
		final MemoryProperties<VkBufferUsage> props = new MemoryProperties.Builder()
				.required(VkMemoryPropertyFlag.DEVICE_LOCAL)
				.usage(VkBufferUsage.TRANSFER_DST)
				.usage(VkBufferUsage.VERTEX_BUFFER)
				.build();

		final VulkanBuffer dest = VulkanBuffer.create(dev, len, props);

		// Copy
		final Command.Pool copyPool = Command.Pool.create(transfer.queue(dev));
		Work.submit(staging.copy(dest), copyPool);

		staging.destroy();

		//////////////////

		final Command.Pool graphicsPool = Command.Pool.create(graphics.queue(dev));

		final View texture = texture(dev, graphicsPool);

		// Create descriptor layout
		final var binding = new DescriptorSet.Binding.Builder()
				.type(VkDescriptorType.COMBINED_IMAGE_SAMPLER)
				.stage(VkShaderStageFlag.FRAGMENT)
				.build();

		final DescriptorSet.Layout setLayout = DescriptorSet.Layout.create(dev, List.of(binding));

		// Create pool
		final DescriptorSet.Pool setPool = new DescriptorSet.Pool.Builder(dev)
				.add(VkDescriptorType.COMBINED_IMAGE_SAMPLER, 3)
				.max(3)
				.build();

		// Create descriptors
		final List<DescriptorSet> descriptors = setPool.allocate(setLayout, chain.views().size());

		// Add sampler
		final Sampler sampler = new Sampler.Builder(dev).build();
		final var res = sampler.resource(texture);
		for(DescriptorSet set : descriptors) {
			set.entry(binding).set(res);
		}
		DescriptorSet.update(dev, descriptors);

		//////////////////

		// Create pipeline layout
		final PipelineLayout pipelineLayout = new PipelineLayout.Builder(dev)
				.add(setLayout)
				.build();

		// Create pipeline
		final Pipeline pipeline = new Pipeline.Builder(dev)
				.layout(pipelineLayout)
				.pass(pass)
				.input()
					.binding()
						.stride((3 + 2) * Float.BYTES)
						.build()
					.attribute()
						.location(0)
						.format(VkFormat.R32G32B32_SFLOAT)
						.build()
					.attribute()
						.location(1)
						.format(VkFormat.R32G32_SFLOAT)
						.offset(3 * Float.BYTES)
						.build()
					.build()
				.rasterizer()
					.clockwise(true) // TODO, also need to flip
					.build()
				.viewport(chain.extents())
				.shader()
					.stage(VkShaderStageFlag.VERTEX)
					.shader(vert)
					.build()
				.shader()
					.stage(VkShaderStageFlag.FRAGMENT)
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
		final Queue presentQueue = present.queue(dev);
		final Command.Pool pool = Command.Pool.create(presentQueue);
		final List<Command.Buffer> commands = pool.allocate(buffers.size());

		// Record render commands
		final Command draw = (api, handle) -> api.vkCmdDraw(handle, len, 1, 0, 0);
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

		final Semaphore semaphore = dev.semaphore();
		for(int n = 0; n < 25; ++n) {
			final int index = chain.acquire(semaphore, null);

			new Work.Builder()
					.add(commands.get(index))
					.build()
					.submit(null);

			presentQueue.waitIdle();
			Thread.sleep(50);

			chain.present(presentQueue, Set.of(semaphore));

			presentQueue.waitIdle();
			Thread.sleep(50);
		}

		//////////////

		// Destroy window
		surface.destroy();
		window.destroy();
		desktop.destroy();

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
