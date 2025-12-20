package org.sarge.jove.platform.vulkan.render;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.*;
import org.sarge.jove.geometry.Point;
import org.sarge.jove.model.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.*;

class DrawCommandTest {
	@SuppressWarnings("unused")
	private static class MockDrawLibrary extends MockLibrary {
		public void vkCmdDraw(Buffer commandBuffer, int vertexCount, int instanceCount, int firstVertex, int firstInstance) {
			assertEquals(3, vertexCount);
			assertEquals(1, instanceCount);
			assertEquals(0, firstVertex);
			assertEquals(0, firstInstance);
		}

		public void vkCmdDrawIndexed(Buffer commandBuffer, int indexCount, int instanceCount, int firstIndex, int firstVertex, int firstInstance) {
			assertEquals(3, indexCount);
			assertEquals(1, instanceCount);
			assertEquals(0, firstIndex);
			assertEquals(0, firstVertex);
			assertEquals(0, firstInstance);
		}
	}

	private LogicalDevice device;
	private Mockery mockery;

	@BeforeEach
	void before() {
		mockery = new Mockery(new MockDrawLibrary(), DrawCommand.Library.class);
		device = new MockLogicalDevice(mockery.proxy());
	}

	@Test
	void draw() {
		final DrawCommand draw = DrawCommand.of(3, device);
		draw.execute(null);
		assertEquals(1, mockery.mock("vkCmdDraw").count());
	}

	@Test
	void indexed() {
		final DrawCommand indexed = new DrawCommand.Builder()
				.vertexCount(3)
				.indexed()
				.build(device);

		indexed.execute(null);
		assertEquals(1, mockery.mock("vkCmdDrawIndexed").count());
	}

	@Test
	void mesh() {
		final var mesh = new IndexedMesh(Primitive.TRIANGLE, Point.LAYOUT);
		mesh.add(new Vertex(Point.ORIGIN));
		mesh.add(0);
		mesh.add(0);
		mesh.add(0);

		final DrawCommand expected = new DrawCommand(3, 1, 0, 0, 0, mockery.proxy());
		assertEquals(expected, DrawCommand.of(mesh, device));
	}
}
