package org.sarge.jove.scene;

import static org.mockito.Mockito.*;

import java.util.function.Consumer;

import org.junit.jupiter.api.*;
import org.sarge.jove.model.Model;

public class RenderQueueTest {
	private RenderQueue queue;
	private ModelNode node;
	private Material mat;
	private Consumer<Renderable> consumer;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void before() {
		queue = new RenderQueue();

		mat = mock(Material.class);
		when(mat.queue()).thenReturn(queue);

		node = new ModelNode(mock(Model.class));
//		node.material(mat);

		consumer = mock(Consumer.class);
	}

	@DisplayName("A node can be added to the queue")
	@Test
	void add() {
		queue.add(node);
		queue.render(consumer);
		verify(consumer).accept(mat);
		verify(consumer).accept(node);
		verifyNoMoreInteractions(consumer);
	}

	@DisplayName("A previously added node can be removed from the queue")
	@Test
	void remove() {
		queue.add(node);
		queue.remove(node);
		queue.render(consumer);
		verifyNoInteractions(consumer);
	}
}
