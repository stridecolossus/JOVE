package org.sarge.jove.platform.vulkan.render;

class FrameProcessorTest {
// TODO
//	private FrameProcessor proc;
//	private Swapchain swapchain;
//	private FrameBuilder builder;
//
//	@BeforeEach
//	void before() {
//		// Create swapchain with two images
//		final View attachment = mock(View.class);
//		swapchain = mock(Swapchain.class);
//		when(swapchain.device()).thenReturn(dev);
//		when(swapchain.attachments()).thenReturn(List.of(attachment, attachment));
//
//		// Init render task builder
//		builder = mock(FrameBuilder.class);
//
//		// TODO - needed to avoid semaphores with same handle being used twice, nasty?
//		final ReferenceFactory factory = new ReferenceFactory() {
//			private int index;
//
//			@Override
//			public IntByReference integer() {
//				return null;
//			}
//
//			@Override
//			public PointerByReference pointer() {
//				return new PointerByReference(new Pointer(++index));
//			}
//		};
//		when(dev.factory()).thenReturn(factory);
//
//		// Create controller
//		proc = new FrameProcessor(swapchain, builder, 2);
//	}
//
//	@Test
//	void render() {
//		// Init command buffer
//		final Pool pool = mock(Pool.class);
//		final Buffer buffer = mock(Buffer.class);
//		when(buffer.handle()).thenReturn(new Handle(4));
//		when(buffer.isReady()).thenReturn(true);
//		when(buffer.pool()).thenReturn(pool);
//		when(pool.device()).thenReturn(dev);
//
//		// Create presentation queue
//		final WorkQueue queue = new WorkQueue(new Handle(5), new Family(1, 2, Set.of()));
//		when(pool.queue()).thenReturn(queue);
//
//		// Init frame builder
//		final RenderSequence seq = mock(RenderSequence.class);
//		when(builder.build(0, seq)).thenReturn(buffer);
//
//		final Frame.Listener listener = mock(Frame.Listener.class);
//		proc.add(listener);
//
//		// Render frame
//		proc.render(seq);
//
//		// TODO - how to test what the frame actually does?
//		// verify(swapchain).acquire(null, null)
//
//		// TODO - factor this out somehow?
//		//verify(listener).frame(anyLong(), anyLong());
//	}
//
//	@Test
//	void destroy() {
//		proc.destroy();
//	}
}
