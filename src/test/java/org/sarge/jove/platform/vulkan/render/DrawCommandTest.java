package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;

class DrawCommandTest {
	private static class MockDrawLibrary extends MockVulkanLibrary {
		private boolean draw, indexed;

		@Override
		public void vkCmdDraw(Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
			draw = true;
		}

		@Override
		public void vkCmdDrawIndexed(Buffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance) {
			indexed = true;
		}
	}

	private MockDrawLibrary library;
	private LogicalDevice device;

	@BeforeEach
	void before() {
		library = new MockDrawLibrary();
		device = new MockLogicalDevice(library);
	}

	@Test
	void draw() {
		final DrawCommand draw = DrawCommand.of(1, device);
		draw.execute(null);
		assertEquals(true, library.draw);
	}

	@Test
	void indexed() {
		final DrawCommand indexed = new DrawCommand.Builder()
				.vertexCount(1)
				.indexed()
				.build(device);

		indexed.execute(null);
		assertEquals(true, library.indexed);
	}

	@Test
	void mesh() {
		final var mesh = new IndexedMesh(Primitive.TRIANGLE, List.of(Point.LAYOUT));
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);

		final DrawCommand expected = new DrawCommand(3, 1, 0, 0, null, library);
		assertEquals(expected, DrawCommand.of(mesh, device));
	}
}
