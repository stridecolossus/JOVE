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
import org.sarge.jove.platform.vulkan.pipeline.FrameBuffer;
import org.sarge.jove.platform.vulkan.pipeline.Pipeline;
import org.sarge.jove.platform.vulkan.pipeline.RenderPass;
import org.sarge.jove.platform.vulkan.pipeline.SwapChain;
import org.sarge.jove.platform.vulkan.util.FormatBuilder;
import org.sarge.jove.util.DataSource;

import com.sun.jna.ptr.PointerByReference;

public class RotatingCubeDemo {


	public static Image texture(LogicalDevice dev) throws IOException {
		// Load image
		final File file = new File("./src/test/resources"); // /thiswayup.jpg");
		//final BufferedImage bufferedImage = ImageIO.read(Files.newInputStream(file.toPath()));

		final ImageData.Loader loader = new ImageData.Loader(DataSource.file(file));
		final ImageData image = loader.load("thiswayup.jpg");

		/*
		System.out.println("IMAGE");
		System.out.println("type="+bufferedImage.getType()); // 5 = BufferedImage: TYPE_3BYTE_BGR
		System.out.println("w="+bufferedImage.getWidth()+" h="+bufferedImage.getHeight()); // 128 x 128
		System.out.println("trans="+bufferedImage.getTransparency()); // Transparency: 1 = opaque (no alpha), 2 = 0 or 1, 3 = 0..1
		System.out.println("trans="+bufferedImage.getAlphaRaster()); // null

		System.out.println("MODEL");
		System.out.println("model="+bufferedImage.getColorModel());
		System.out.println("transfer="+bufferedImage.getColorModel().getTransferType()); // 0 = DataBuffer: TYPE_BYTE
		System.out.println("colours="+bufferedImage.getColorModel().getNumColorComponents()); // 3
		System.out.println("bits="+Arrays.toString(bufferedImage.getColorModel().getComponentSize())); // [8, 8, 8]
		System.out.println("num="+bufferedImage.getColorModel().getNumComponents()); // 3

		System.out.println("SPACE");
		System.out.println("space="+bufferedImage.getColorModel().getColorSpace());
		System.out.println("num="+bufferedImage.getColorModel().getColorSpace().getNumComponents()); // 3
		System.out.println("type="+bufferedImage.getColorModel().getColorSpace().getType()); // 5 = ColorSpace: TYPE_RGB
		*/

//		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
//
//		private BufferedImage ApplyTransparency(BufferedImage image, Image mask)
//		{
//		    BufferedImage dest = new BufferedImage(
//		            image.getWidth(), image.getHeight(),
//		            BufferedImage.TYPE_INT_ARGB);
//		    Graphics2D g2 = dest.createGraphics();
//		    g2.drawImage(image, 0, 0, null);
//		    AlphaComposite ac = AlphaComposite.getInstance(AlphaComposite.DST_IN, 1.0F);
//		    g2.setComposite(ac);
//		    g2.drawImage(mask, 0, 0, null);
//		    g2.dispose();
//		    return dest;
//		}

		// Allocate staging buffer
		final ByteBuffer bb = image.buffer();
//		final int size = bufferedImage.getWidth() * bufferedImage.getHeight() * 3; // alpha?
		final VertexBuffer staging = VertexBuffer.staging(dev, bb.capacity());

//		// Convert to NIO buffer
//		final DataBufferByte data = (DataBufferByte) bufferedImage.getRaster().getDataBuffer();
//		final ByteBuffer bb = ByteBuffer.wrap(data.getData());
//		// TODO - bb.asShortBuffer().put(ShortBuffer.wrap(data)); etc

		// Copy image to staging
		staging.load(bb);

		// Create Vulkan image
		final Image texture = new Image.Builder(dev)
				.extents(Image.Extents.of(image.size()))
//				.format(VkFormat.VK_FORMAT_R8G8B8A8_SRGB)
				.format(VkFormat.VK_FORMAT_R8G8B8A8_SRGB) // UNORM?
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT)
				.usage(VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT)
				.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
				.build();

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
		final Command copyCommand = staging.copy(dest);
		final Command.Buffer copyBuffer = copyPool.allocate(copyCommand);
		Work.submit(copyBuffer, true);

		//////////////////

		texture(dev);

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
