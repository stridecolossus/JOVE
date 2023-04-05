package org.sarge.jove.platform.vulkan.render;

class SwapchainAdapterTest {

/**
 *
 * TODO
 * - this is very messy
 * - either everything needs to be Mockito based mocks
 * - or concrete domain types
 * - trying to mix is just too difficult in this case
 *
 */

//	private SwapchainAdapter adapter;
//	private DeviceContext dev;
//	private Swapchain swapchain;
//	private Swapchain.Builder builder;
//	private RenderPass pass;
//
//	@BeforeEach
//	void before() {
//		dev = new MockDeviceContext();
//
//		final var extents = new Dimensions(3, 4);
//
//		final var descriptor = new Image.Descriptor.Builder()
//				.format(VkFormat.R16G16B16_SFLOAT)
//				.extents(extents)
//				.aspect(VkImageAspect.COLOR)
//				.build();
//
//		final Image image = mock(Image.class);
//		when(image.descriptor()).thenReturn(descriptor);
//
//		final View view = new View.Builder(image).build(dev);
//
//
//		final var builder = new Swapchain.Builder(surface)
//
//
//
//		swapchain = mock(Swapchain.class);
//		when(swapchain.device()).thenReturn(dev);
//		when(swapchain.extents()).thenReturn(extents);
//		when(swapchain.attachments()).thenReturn(List.of(view, view));
//
//		builder = mock(Swapchain.Builder.class);
//		when(builder.build(dev)).thenReturn(swapchain);
//
//		final var attachment = new Attachment.Builder(VkFormat.R16G16B16_SFLOAT).finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL).build();
//		pass = new RenderPass(new Handle(1), dev, List.of(attachment));
//
//		adapter = new SwapchainAdapter(builder, pass, List.of());
//	}
//
//	@Test
//	void recreate() {
//		adapter.recreate();
//	}
}
