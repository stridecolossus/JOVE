package org.sarge.jove.platform.vulkan.render;

import static org.mockito.Mockito.*;

import java.util.*;
import java.util.function.Supplier;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;

class FrameComposerTest {
	private DeviceContext dev;
	private FrameComposer composer;
	private FrameBuffer frame;
	private FrameBuffer.Group group;
	private Supplier<Command.Buffer> factory;
	private Supplier<Command.Buffer> seq;
//	private Swapchain swapchain;

	@BeforeEach
	void before() {
		// Init device
		dev = new MockDeviceContext();

//		// Create render pass
//		final VkFormat format = VkFormat.B8G8R8A8_SRGB;
//		final Attachment attachment = new Attachment.Builder(format).finalLayout(VkImageLayout.PRESENT_SRC_KHR).build();
//		pass = new RenderPass(new Handle(1), dev, List.of(attachment));
//
//		// Configure frame buffer image
//		final Dimensions extents = new Dimensions(1, 2);
//		final var descriptor = new Image.Descriptor.Builder()
//				.format(format)
//				.aspect(VkImageAspect.COLOR)
//				.extents(extents)
//				.build();
//
//		// Create swapchain image
//		final Image image = mock(Image.class);
//		when(image.device()).thenReturn(dev);
//		when(image.descriptor()).thenReturn(descriptor);
//
//		// Create view
//		view = View.of(image);
//
//		swapchain = mock(Swapchain.class);

		frame = mock(FrameBuffer.class);
		when(frame.begin()).thenReturn(mock(Command.class));

		group = mock(FrameBuffer.Group.class);
		when(group.buffer(1)).thenReturn(frame);

		final var queue = new WorkQueue(new Handle(1), new WorkQueue.Family(1, 2, Set.of()));
		final var pool = Command.Pool.create(dev, queue);

		factory = pool::allocate;
//			final var buffer = mock(Command.Buffer.class);
//			when(buffer.isPrimary()).thenReturn(true);
//			when(buffer.isReady()).thenReturn(true);
//			//when(buffer.begin(VkCommandBufferUsage.ONE_TIME_SUBMIT)).thenReturn(buffer);
//			return pool.allocate(1, true);
//		};

		seq = () -> pool
				.allocate(1, false)
				.iterator()
				.next()
				.begin()
				.end();

//			final var buffer = mock(Command.Buffer.class);
//			when(buffer.isPrimary()).thenReturn(false);
//			when(buffer.isReady()).thenReturn(true);
//			return buffer;
//		}

		composer = new FrameComposer(group, factory, () -> List.of()); // seq);
	}

	@Test
	void compose() {

		final Command.Buffer render = composer.compose(1);
		// TODO
	}
}
