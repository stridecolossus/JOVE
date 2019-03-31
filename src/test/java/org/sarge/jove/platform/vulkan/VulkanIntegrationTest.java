package org.sarge.jove.platform.vulkan;

import static org.junit.Assert.assertNotNull;
import static org.sarge.jove.platform.vulkan.VulkanLibrary.check;
import static org.sarge.lib.util.Check.notNull;

import java.io.File;
import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.sarge.jove.common.Colour;
import org.sarge.jove.common.Dimensions;
import org.sarge.jove.common.Rectangle;
import org.sarge.jove.common.ScreenCoordinate;
import org.sarge.jove.control.Event;
import org.sarge.jove.geometry.Matrix;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.DataBuffer;
import org.sarge.jove.model.Model;
import org.sarge.jove.model.Primitive;
import org.sarge.jove.model.Vertex;
import org.sarge.jove.model.Vertex.MutableVertex;
import org.sarge.jove.platform.DesktopService;
import org.sarge.jove.platform.Device;
import org.sarge.jove.platform.IntegerEnumeration;
import org.sarge.jove.platform.Resource.PointerHandle;
import org.sarge.jove.platform.Service;
import org.sarge.jove.platform.Service.ServiceException;
import org.sarge.jove.platform.Window;
import org.sarge.jove.platform.glfw.FrameworkDesktopService;
import org.sarge.jove.platform.vulkan.Feature.Extension;
import org.sarge.jove.platform.vulkan.Feature.ValidationLayer;
import org.sarge.jove.platform.vulkan.FrameState.FrameTracker;
import org.sarge.jove.platform.vulkan.FrameState.FrameTracker.DefaultFrameTracker;
import org.sarge.jove.platform.vulkan.PhysicalDevice.QueueFamily;
import org.sarge.jove.texture.DefaultImage;
import org.sarge.jove.texture.Image;
import org.sarge.jove.texture.TextureCoordinate;
import org.sarge.jove.util.BufferFactory;
import org.sarge.lib.collection.StrictSet;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

public class VulkanIntegrationTest {
	private Vulkan vulkan;
	private VulkanLibrary lib;
	private LogicalDevice dev;
	private QueueFamily graphics, present, transfer;
	private Command.Pool pool;

	public static void main(String[] args) throws Exception {
		final VulkanIntegrationTest test = new VulkanIntegrationTest();
		test.run();
	}

	public void run() throws Exception {
		// Init GLFW
		final DesktopService desktop = desktop();
		final Window window = window(desktop);
		final String[] required = desktop.extensions();

		// Init Vulkan
		System.out.println("Initialising Vulkan");
		vulkan = Vulkan.create();
		lib = vulkan.library();

		System.out.println("Creating instance");
		final VulkanInstance instance = instance(required);

		System.out.println("Initialising debug handler");
		instance.handlerFactory()
			.builder()
			.init()
			//.severity(VkDebugUtilsMessageSeverityFlagEXT.VK_DEBUG_UTILS_MESSAGE_SEVERITY_INFO_BIT_EXT)
			.build();

		final PhysicalDevice physical = physical(instance);

		// Create surface
		final Surface surface = surface(instance, physical, desktop, window);

		// Determine queue families
		final var families = physical.families();
		transfer = families.stream().filter(f -> f.flags().contains(VkQueueFlag.VK_QUEUE_TRANSFER_BIT)).findAny().orElseThrow(() -> new IllegalArgumentException("No transfer queue"));
		graphics = families.stream().filter(f -> f.flags().contains(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT)).findAny().orElseThrow(() -> new IllegalArgumentException("No graphics queue"));
		present = families.stream().filter(f -> f.isPresentationSupported(surface)).findAny().orElseThrow(() -> new IllegalArgumentException("No presentation queue"));

		// Create logical device
		dev = logical(physical);

		///////////////////

		final SwapChain chain = chain(dev, surface);

		System.out.println("Creating shaders");
		final VulkanShader vert = VulkanShader.create(dev, Files.readAllBytes(new File("src/test/resources/quad.vert.spv").toPath()));
		final VulkanShader frag = VulkanShader.create(dev, Files.readAllBytes(new File("src/test/resources/quad.frag.spv").toPath()));

		final RenderPass pass = pass(chain.format());

		System.out.println("Creating command pool");
		pool = Command.Pool.create(dev, graphics);

		///////////////////

		final Model<?> model = model();
		final DataBuffer.Layout layout = DataBuffer.Layout.of(model.components());

		final VulkanDataBuffer vbo = vertexBuffer(model, layout);
		final VulkanDataBuffer indexBuffer = indexBuffer(model);

		///////////////////

		System.out.println("Allocating command buffers");
		final List<Command.Buffer> cmds = pool.allocate(3, true);

		System.out.println("Allocating descriptor set pool");
		final DescriptorSet.Pool setPool = new DescriptorSet.Pool.Builder(dev)
			.add(3, VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
			.add(3, VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
			.max(3)
			.build();

		System.out.println("Creating descriptor set layout");
		final DescriptorSet.Layout dsLayout = new DescriptorSet.Layout.Builder(dev)
			.binding(0)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
			.binding(1)
				.type(VkDescriptorType.VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
			.build();

		final Pipeline pipeline = pipeline(vert, frag, chain.extent(), pass, layout, dsLayout);

		System.out.println("Allocating descriptor sets");
		final DescriptorSet.Layout[] array = new DescriptorSet.Layout[cmds.size()];
		Arrays.fill(array, dsLayout);
		final DescriptorSet[] sets = setPool.allocate(Arrays.asList(array)).toArray(DescriptorSet[]::new);

		///////////////////

		final ImageView textureImageView = texture();
		final PointerHandle sampler = sampler();

		///////////////////

		// TODO - how to wrap 3 x FB and 3 x commands -> object?
		System.out.println("Creating frame buffer and commands");
		final FrameBuffer[] frameBuffers = new FrameBuffer[cmds.size()];
		final VulkanDataBuffer[] uniforms = new VulkanDataBuffer[cmds.size()];
		for(int n = 0; n < cmds.size(); ++n) {
			final FrameBuffer fb = frameBuffer(pass, chain.extent(), chain.images().get(n));
			frameBuffers[n] = fb;

			final VulkanDataBuffer uniform = uniform();
			uniforms[n] = uniform;
			// TODO
			sets[n].uniform(0, uniform, 0, 2 * 16 * Float.BYTES); // (~0L)); // 4);
			sets[n].sampler(1, textureImageView, sampler);

			final Command.Buffer cb = cmds.get(n);
			record(cb, fb, pass, pipeline, vbo, indexBuffer, sets[n]);
		}

		//////////////////

		System.out.println("Creating frame tracker");
		final WorkQueue queue = dev.queue(present, 0);
		final FrameTracker tracker = new DefaultFrameTracker(dev, 2, queue);

		final AtomicBoolean running = new AtomicBoolean(true);

		final Event.Handler hdlr = event -> {
			if(event.descriptor().id() == 256) {
				running.set(false);
			}
		};
		final Device<?> input = window.device();
		input.bind(Event.Category.BUTTON, hdlr);

		// Start render loop
		System.out.println("Starting render loop...");
		while(running.get()) {
			//
			final FrameState frame = tracker.waitReady();

			// Acquire next image
			final int index = chain.next(frame.available(), null);

			// Submit render command
			final Command.Buffer cmd = cmds.get(index);
			final FrameState next = tracker.submit(cmd);

			// Present next image
			chain.present(next, queue);

			// Poll input events
			window.poll();
		}

		/////

		System.out.println("Waiting...");
		lib.vkDeviceWaitIdle(dev.handle());

		System.out.println("Cleaning up...");

		tracker.destroy();

		pool.destroy();
		pipeline.destroy();
		pass.destroy();

		vert.destroy();
		frag.destroy();

		for(FrameBuffer fb : frameBuffers) {
			fb.destroy();
		}

		chain.destroy();
		surface.destroy();
		window.destroy();
		desktop.close();

		dev.destroy();
		instance.destroy();

		System.out.println("Finished");
	}

	// https://www.eshayne.com/jnaex/index.html?example=10
	// https://stackoverflow.com/questions/10109723/using-jna-to-access-a-struct-containing-an-array-of-structs

	/**
	 * Initialises the vulkan instance.
	 */
	private VulkanInstance instance(String[] extensions) {
		// Create builder
		System.out.println("Creating instance builder");
		final VulkanInstance.Builder builder = new VulkanInstance.Builder(vulkan)
			.extensions(extensions)
			.extension(Extension.DEBUG_UTILS)
			.layer(ValidationLayer.STANDARD_VALIDATION)
			.layer("VK_LAYER_VALVE_steam_overlay", 1);

		// Create instance
		System.out.println("Creating instance");
		final VulkanInstance instance = builder.build();
		assertNotNull(instance);

		return instance;
	}

	private PhysicalDevice physical(VulkanInstance instance) {
		System.out.println("Enumerating physical devices");
		final var devices = PhysicalDevice.create(instance);
		for(PhysicalDevice dev : devices) {
			// TODO
			//if(!dev.features().geometryShader) continue;
			return dev;
		}
		throw new ServiceException("No suitable device");
	}

//	private QueueFamily families(PhysicalDevice dev, Surface surface) {
//		System.out.println("Enumerating queue families");
//		final var families = dev.families();
//		for(QueueFamily family : families) {
//			// TODO - assumes same family for both, should select family for each requirement
//			if(!family.flags().contains(VkQueueFlag.VK_QUEUE_GRAPHICS_BIT)) continue;
//			if(!family.isPresentationSupported(surface)) continue;
//			return family;
//		}
//		throw new ServiceException("No suitable queue");
//	}

	private LogicalDevice logical(PhysicalDevice physical) {
		System.out.println("Creating logical device");

		final VkPhysicalDeviceFeatures features = new VkPhysicalDeviceFeatures();
		features.geometryShader = VulkanBoolean.TRUE;
		features.samplerAnisotropy = VulkanBoolean.TRUE;

		return new LogicalDevice.Builder(physical)
			.queue(graphics)
			.queue(present)
			.queue(transfer)
			.extension(Extension.SWAP_CHAIN)
			.layer(ValidationLayer.STANDARD_VALIDATION)
			//.layer("VK_LAYER_VALVE_steam_overlay", 1)
			.features(features)
			.build();
	}

	////////////////////////////////

	private DesktopService desktop() {
		System.out.println("Initialised desktop service");
		final DesktopService service = FrameworkDesktopService.create();
		if(!service.isVulkanSupported()) throw new Service.ServiceException("Vulkan not supported");
		return service;
	}

	private Window window(DesktopService service) {
		System.out.println("Creating window");
		final Set<Window.Descriptor.Property> props = Set.of(Window.Descriptor.Property.DISABLE_OPENGL);
		final Window.Descriptor descriptor = new Window.Descriptor("demo", new Dimensions(640, 480), null, props);
		return service.window(descriptor);
	}

	private Surface surface(VulkanInstance instance, PhysicalDevice dev, DesktopService service, Window window) {
		System.out.println("Creating surface");
		final PointerHandle handle = (PointerHandle) window; // TODO - assumes JNA pointer
		final Pointer ptr = service.surface(instance.handle(), handle.handle());
		return Surface.create(ptr, instance, dev);
	}

	private SwapChain chain(LogicalDevice dev, Surface surface) {
		System.out.println("Creating swap-chain");

		final VkFormat format = new VulkanHelper.FormatBuilder()
			.components(VulkanHelper.FormatBuilder.BGRA)
			.bytes(1)
			.signed(false)
			.type(Vertex.Component.Type.NORM)
			.build();

		return new SwapChain.Builder(dev, surface)
			.format(format)
			.colour(VkColorSpaceKHR.VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
			.build();
	}

	////////////////////////////////

	private RenderPass pass(VkFormat format) {
		System.out.println("Creating render pass");
		return new RenderPass.Builder(dev)
			.attachment()
				.format(format)
				.load(VkAttachmentLoadOp.VK_ATTACHMENT_LOAD_OP_CLEAR)
				.store(VkAttachmentStoreOp.VK_ATTACHMENT_STORE_OP_STORE)
				.finalLayout(VkImageLayout.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR)
				.build()
			.subpass()
				.attachment(0)			// TODO - or move this into the attachment builder somehow?
				.dependency()
					.sourceStage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destinationStage(VkPipelineStageFlag.VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT)
					.destinationAccess(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_READ_BIT)
					.destinationAccess(VkAccessFlag.VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT)
					.build()
				.build()
			.build();
	}

	private FrameBuffer frameBuffer(RenderPass pass, Dimensions extent, ImageView view) {
		return new FrameBuffer.Builder(dev, pass)
			.extent(extent)
			.view(view)
			.build();
	}

	private Pipeline pipeline(VulkanShader vert, VulkanShader frag, Dimensions extent, RenderPass pass, DataBuffer.Layout dataLayout, DescriptorSet.Layout dsLayout) {
		System.out.println("Creating pipeline layout");
		final Pipeline.Layout layout = new Pipeline.Layout.Builder(dev)
			.add(dsLayout)
			.build();

		System.out.println("Creating pipeline");
		final Rectangle rect = new Rectangle(new ScreenCoordinate(0, 0), extent);
		return new Pipeline.Builder(dev, pass)
			.layout(layout)
			.input()
				.binding(dataLayout)
				.build()
			.shader()
				.module(vert)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_VERTEX_BIT)
				.build()
			.shader()
				.module(frag)
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.build()
			.viewport()
				.viewport(rect)
				.scissor(rect)
				.build()
			.build();
	}

	////////////////////////////////

	private void record(Command.Buffer buffer, FrameBuffer fb, RenderPass pass, Pipeline pipeline, VulkanDataBuffer vbo, VulkanDataBuffer index, DescriptorSet ds) {
		System.out.println("Recording command");

		final Rectangle extent = new Rectangle(0, 0, 640, 480);
		final Colour[] clear = {new Colour(0.3f, 0.3f, 0.3f, 1)};

		// TODO - created from VBO?
		final Command draw = (api, cb) -> api.vkCmdDrawIndexed(cb, 4, 1, 0, 0, 0);
		//final Command draw = (api, cb) -> api.vkCmdDraw(cb, 3, 1, 0, 0);

		final Command bind = ds.bind(pipeline.layout());

		buffer
			.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_SIMULTANEOUS_USE_BIT)
			.add(pass.begin(fb, extent, clear))
			.add(pipeline.bind())
			.add(vbo.bindVertexBuffer())
			.add(index.bindIndexBuffer())
			.add(bind)
			.add(draw)
			.add(RenderPass.END_COMMAND)
			.end();
	}

	////////////////////////////////

	private Model<MutableVertex> model() {
		class ColourVertex extends MutableVertex {
			private final Colour col;
			//private final TextureCoordinate.Coordinate2D coords;

			public ColourVertex(Point pos, Colour col, TextureCoordinate.Coordinate2D coords) {
				super(pos);
				this.col = col;
				this.coords = coords;
			}

			@Override
			public int size() {
				return Point.SIZE + Colour.SIZE + 2;
			}

			@Override
			public void buffer(FloatBuffer buffer) {
				pos.buffer(buffer);
				col.buffer(buffer);
				coords.buffer(buffer);
			}
		}

		return new Model.Builder<>()
			.primitive(Primitive.TRIANGLE_STRIP)
			.component(Vertex.Component.COLOUR)
			.component(Vertex.Component.coordinate(2))
//			.add(new ColourVertex(new Point(-0.5f, -0.5f, 0), new Colour(1, 0, 0, 1), new TextureCoordinate.Coordinate2D(1, 0)))
//			.add(new ColourVertex(new Point(+0.5f, -0.5f, 0), new Colour(0, 1, 0, 1), new TextureCoordinate.Coordinate2D(0, 0)))
//			.add(new ColourVertex(new Point(+0.5f, +0.5f, 0), new Colour(0, 0, 1, 1), new TextureCoordinate.Coordinate2D(0, 1)))
//			.add(new ColourVertex(new Point(-0.5f, +0.5f, 0), new Colour(1, 1, 1, 1), new TextureCoordinate.Coordinate2D(1, 1)))

			.add(new ColourVertex(new Point(-0.5f, -0.5f, 0), new Colour(1, 0, 0, 1), new TextureCoordinate.Coordinate2D(0, 0)))
			.add(new ColourVertex(new Point(-0.5f, +0.5f, 0), new Colour(0, 1, 0, 1), new TextureCoordinate.Coordinate2D(0, 1)))
			.add(new ColourVertex(new Point(+0.5f, -0.5f, 0), new Colour(0, 0, 1, 1), new TextureCoordinate.Coordinate2D(1, 0)))
			.add(new ColourVertex(new Point(+0.5f, +0.5f, 0), new Colour(1, 1, 1, 1), new TextureCoordinate.Coordinate2D(1, 1)))
			.build();
	}

	////////////////////////////////

	private VulkanDataBuffer vertexBuffer(Model<?> model, DataBuffer.Layout layout) {
		System.out.println("Creating VBO");
		final long size = model.length() * layout.stride();
		final VulkanDataBuffer vbo = new VulkanDataBuffer.Builder(dev)
			.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_VERTEX_BUFFER_BIT)
			.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
			.length(size)
			.build();

		System.out.println("Buffering vertices");
		// TODO - helper
		// TODO - Bufferable -> bytes?
		final ByteBuffer bb = BufferFactory.byteBuffer((int) size);
		final FloatBuffer fb = bb.asFloatBuffer();
		model.vertices().forEach(v -> v.buffer(fb));

		copy(vbo, bb);

		return vbo;
	}

	private VulkanDataBuffer indexBuffer(Model<?> model) {
		System.out.println("Creating index buffer");
		final int len = 4 * Integer.BYTES;
		final VulkanDataBuffer index = new VulkanDataBuffer.Builder(dev)
			.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_INDEX_BUFFER_BIT)
			.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_DST_BIT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT)
			.length(len)
			.build();

		final ByteBuffer bb = BufferFactory.byteBuffer(len);
		final IntBuffer fb = bb.asIntBuffer();
		fb.put(0);
		fb.put(1);
		fb.put(2);
		fb.put(3);

		copy(index, bb);

		return index;
	}

	private VulkanDataBuffer uniform() {
		System.out.println("Creating uniform buffer");
		final int len = 2 * 16 * Float.BYTES;
		final VulkanDataBuffer uniform = new VulkanDataBuffer.Builder(dev)
			.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
			.length(len)
			.build();

		System.out.println("Writing uniform buffer");
		final ByteBuffer bb = BufferFactory.byteBuffer(len);
		final FloatBuffer fb = bb.asFloatBuffer();
		Matrix.IDENTITY.buffer(fb);
		Matrix.IDENTITY.buffer(fb);
		uniform.push(bb);

		return uniform;
	}

	private void copy(DataBuffer buffer, ByteBuffer data) {
		System.out.println("Creating staging buffer");
		final long len = data.capacity();
		final VulkanDataBuffer staging = new VulkanDataBuffer.Builder(dev)
			.usage(VkBufferUsageFlag.VK_BUFFER_USAGE_TRANSFER_SRC_BIT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT)
			.property(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_HOST_COHERENT_BIT)
			.length(len)
			.build();

		System.out.println("Copying to staging");
		staging.push(data);

		System.out.println("Recording copy operation");
		final VkBufferCopy info = new VkBufferCopy();
		info.size = len;
		final Command copy = (lib, cb) -> {
			final PointerHandle dest = (PointerHandle) buffer; // TODO
			lib.vkCmdCopyBuffer(cb, staging.handle(), dest.handle(), 1, new VkBufferCopy[]{info});
		};
		final Command.Buffer cmd = pool.allocate(1, true).iterator().next();
		cmd
			.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
			.add(copy)
			.end();

		System.out.println("Copying...");
		final WorkQueue queue = dev.queue(transfer, 0);
		final WorkQueue.Work work = new WorkQueue.Work.Builder().add(cmd).build();
		queue.submit(work);
		queue.waitIdle();

		System.out.println("Releasing resources");
		staging.destroy();
		cmd.free();
	}

	//////////////////////////

	public ImageView texture() throws Exception {
		// Load texture image
		final Image image = new DefaultImage.Loader().load(new FileInputStream("src/test/resources/statue.jpg"));

		// Create texture
		//

		// Init texture dimensions
		final Dimensions dim = image.header().size();
		final VkExtent3D extent = new VkExtent3D();
		extent.width = dim.width;
		extent.height = dim.height;
		extent.depth = 1;

		// Init texture descriptor
		final VkImageCreateInfo info = new VkImageCreateInfo();
		info.imageType = VkImageType.VK_IMAGE_TYPE_2D;
		info.extent = extent;
		info.mipLevels = 1;
		info.arrayLayers = 1;
		info.samples = VkSampleCountFlag.VK_SAMPLE_COUNT_1_BIT.value();
		info.format = new VulkanHelper.FormatBuilder().bytes(1).signed(false).type(Vertex.Component.Type.NORM).build();
		info.tiling = VkImageTiling.VK_IMAGE_TILING_OPTIMAL;
		info.initialLayout = VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED;
		info.usage = IntegerEnumeration.mask(VkImageUsageFlag.VK_IMAGE_USAGE_TRANSFER_DST_BIT, VkImageUsageFlag.VK_IMAGE_USAGE_SAMPLED_BIT);
		info.sharingMode = VkSharingMode.VK_SHARING_MODE_EXCLUSIVE;

		// Allocate texture
		final PointerByReference handle = vulkan.factory().reference();
		check(lib.vkCreateImage(dev.handle(), info, null, handle));

		// Allocate texture memory
		//

		// Allocate texture memory
		final VkMemoryRequirements reqs = new VkMemoryRequirements();
		lib.vkGetImageMemoryRequirements(dev.handle(), handle.getValue(), reqs);

		// Determine memory type
		// TODO - factor our common code from here and VulkanDataBuffer -> helper, how to handle props?
		final Set<VkMemoryPropertyFlag> props = new StrictSet<>();
		props.add(VkMemoryPropertyFlag.VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT);
		final int type = dev.parent().selector().findMemoryType(props);

		// Allocate texture memory
		final PointerByReference mem = vulkan.factory().reference();
		final VkMemoryAllocateInfo alloc = new VkMemoryAllocateInfo();
		alloc.allocationSize = reqs.size;
		alloc.memoryTypeIndex = type;
		check(lib.vkAllocateMemory(dev.handle(), alloc, null, mem));

		// Bind memory
		check(lib.vkBindImageMemory(dev.handle(), handle.getValue(), mem.getValue(), 0L));

		// Copy image to staging buffer
		//
		final long len = dim.width * dim.height * 4; // TODO - from image format
		final VulkanDataBuffer staging = VulkanDataBuffer.staging(dev, len);
		staging.push(image.buffer());

		// TODO - destroy image?

		// Transition to destination
		//
		transition(handle.getValue(), VkImageLayout.VK_IMAGE_LAYOUT_UNDEFINED, VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, true);

		// Copy buffer to image
		//

		// Init copy descriptor
		final VkBufferImageCopy region = new VkBufferImageCopy();
		region.bufferOffset = 0;
		region.bufferRowLength = 0;
		region.bufferImageHeight = 0;
		region.imageSubresource = new VkImageSubresourceLayers();
		region.imageSubresource.aspectMask = VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value();
		region.imageSubresource.mipLevel = 0;
		region.imageSubresource.baseArrayLayer = 0;
		region.imageSubresource.layerCount = 1;
		region.imageOffset = new VkOffset3D();
		region.imageExtent = new VkExtent3D();
		region.imageExtent.width = dim.width;
		region.imageExtent.height = dim.height;
		region.imageExtent.depth = 1;

		// Copy texture
		final Command.Buffer cb = pool.allocate(1, true).iterator().next();
		cb
			.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
			.add((lib, cmd) -> lib.vkCmdCopyBufferToImage(cmd, staging.handle(), handle.getValue(), VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, 1, region))
			.end();
		final WorkQueue queue = dev.queue(transfer, 0);
		queue.submit(new WorkQueue.Work.Builder().add(cb).build());
		queue.waitIdle();
		cb.free();

		// Transition to final
		//
		transition(handle.getValue(), VkImageLayout.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VkImageLayout.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL, false);

		// Release staging buffer
		//
		staging.destroy();

		final VulkanImage texture = new VulkanImage(handle.getValue(), info.format, new VkExtent2D(image.header().size()));

		return new ImageView.Builder(dev, texture).build();
	}

	private void transition(Pointer image, VkImageLayout prev, VkImageLayout next, boolean first) {		// TODO - bodge
		// Init memory barrier descriptor
		final VkImageMemoryBarrier barrier = new VkImageMemoryBarrier();
		barrier.oldLayout = notNull(prev);
		barrier.newLayout = notNull(next);
		barrier.srcQueueFamilyIndex = -1;
		barrier.dstQueueFamilyIndex = -1;
		barrier.image = notNull(image);

		// Init range
		barrier.subresourceRange = new VkImageSubresourceRange();
		barrier.subresourceRange.aspectMask = VkImageAspectFlag.VK_IMAGE_ASPECT_COLOR_BIT.value();
		barrier.subresourceRange.baseMipLevel = 0;
		barrier.subresourceRange.levelCount = 1;
		barrier.subresourceRange.baseArrayLayer = 0;
		barrier.subresourceRange.layerCount = 1;

		// Init access flags
		VkPipelineStageFlag src, dest;
		if(first) {
			barrier.srcAccessMask = 0;
			barrier.dstAccessMask = VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT.value();
			src = VkPipelineStageFlag.VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
			dest = VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT;
		}
		else {
			barrier.srcAccessMask = VkAccessFlag.VK_ACCESS_TRANSFER_WRITE_BIT.value();
			barrier.dstAccessMask = VkAccessFlag.VK_ACCESS_SHADER_READ_BIT.value();
			src = VkPipelineStageFlag.VK_PIPELINE_STAGE_TRANSFER_BIT;
			dest = VkPipelineStageFlag.VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
		}

		// Apply barrier
		final Command.Buffer cb = pool.allocate(1, true).iterator().next();
		cb
			.begin(VkCommandBufferUsageFlag.VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT)
			.add((lib, cmd) -> lib.vkCmdPipelineBarrier(cmd, src.value(), dest.value(), 0, 0, null, 0, null, 1, new VkImageMemoryBarrier[]{barrier}))
			.end();
		final WorkQueue queue = dev.queue(transfer, 0);
		queue.submit(new WorkQueue.Work.Builder().add(cb).build());
		queue.waitIdle();
		cb.free();
	}

	public PointerHandle sampler() {
		// TODO - replicate texture.descriptor?

		final VkSamplerCreateInfo info = new VkSamplerCreateInfo();
		info.minFilter = VkFilter.VK_FILTER_LINEAR;
		info.magFilter = VkFilter.VK_FILTER_LINEAR;

		info.addressModeU = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT;
		info.addressModeV = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT;
		info.addressModeW = VkSamplerAddressMode.VK_SAMPLER_ADDRESS_MODE_REPEAT;
		info.borderColor = VkBorderColor.VK_BORDER_COLOR_FLOAT_OPAQUE_BLACK;

		info.anisotropyEnable = VulkanBoolean.TRUE;
		info.maxAnisotropy = 16;
		info.unnormalizedCoordinates = VulkanBoolean.FALSE;

		info.compareEnable = VulkanBoolean.FALSE;
		info.compareOp = VkCompareOp.VK_COMPARE_OP_ALWAYS;

		info.mipmapMode = VkSamplerMipmapMode.VK_SAMPLER_MIPMAP_MODE_LINEAR;
		info.mipLodBias = 0;
		info.minLod = 0;
		info.maxLod = 0;

		final PointerByReference sampler = vulkan.factory().reference();
		lib.vkCreateSampler(dev.handle(), info, null, sampler);

		// TODO - class
		return new PointerHandle(sampler.getValue());
	}

	///////////////////////
}
