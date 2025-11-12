package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.MockVulkanLibrary;

class DrawCommandTest {
	private static class MockDrawLibrary extends MockVulkanLibrary {
		private DrawCommand result;

		@Override
		public void vkCmdDraw(Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
			result = new DrawCommand(vertexCount, instanceCount, firstVertex, firstInstance, null, this);
		}

		@Override
		public void vkCmdDrawIndexed(Buffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance) {
			result = new DrawCommand(indexCount, instanceCount, firstIndex, firstVertex, firstInstance, this);
		}
	}

	private MockDrawLibrary library;

	@BeforeEach
	void before() {
		library = new MockDrawLibrary();
	}

	@Test
	void draw() {
		final DrawCommand draw = new DrawCommand.Builder()
				.vertexCount(1)
				.build(library);

		draw.execute(null);
		assertEquals(draw, library.result);
	}

	@Test
	void indexed() {
		final DrawCommand indexed = new DrawCommand.Builder()
				.vertexCount(1)
				.indexed()
				.build(library);

		indexed.execute(null);
		assertEquals(indexed, library.result);
	}
}
