package org.sarge.jove.platform.vulkan.memory;

public class RoutingAllocationServiceTest {
// TODO
//	private RoutingAllocationService service;
//	private Allocator def;
//
//	@BeforeEach
//	void before() {
//		def = mock(Allocator.class);
//		service = new RoutingAllocationService(mock(MemorySelector.class), def);
//	}
//
//	@DisplayName("An unmatched request is routed to the default allocator")
//	@Test
//	void unmatched() {
//		assertEquals(def, service.allocator(null));
//	}
//
//	@DisplayName("A matching request is routed to the specified allocator")
//	@Test
//	void route() {
//		final MemoryProperties<?> props = new MemoryProperties.Builder<>()
//				.usage(VkImageUsageFlag.COLOR_ATTACHMENT)
//				.required(VkMemoryProperty.HOST_VISIBLE)
//				.build();
//
//		final Allocator other = mock(Allocator.class);
//		service.route(props::equals, other);
//		assertEquals(other, service.allocator(props));
//	}
}
