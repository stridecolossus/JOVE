package org.sarge.jove.scene.graph;

public class RenderQueueTest {
//	private RenderQueue queue;
//	private Consumer<Renderable> consumer;
//	private ModelNode node;
//
//	@SuppressWarnings("unchecked")
//	@BeforeEach
//	void before() {
//		queue = new RenderQueue();
//		consumer = mock(Consumer.class);
//		node = new ModelNode(mock(Mesh.class));
//	}
//
//	@Test
//	void add() {
//		final Material mat = mock(Material.class);
//		final Renderable texture = mock(Renderable.class);
//		when(mat.texture()).thenReturn(texture);
//		node.material().set(mat);
//		queue.add(node);
//		queue.render(consumer);
//		verify(consumer).accept(mat);
//		verify(consumer).accept(texture);
//		verify(consumer).accept(node.mesh());
//	}
//
//	@Test
//	void undefined() {
//		assertThrows(IllegalStateException.class, () -> queue.add(node));
//	}
}
