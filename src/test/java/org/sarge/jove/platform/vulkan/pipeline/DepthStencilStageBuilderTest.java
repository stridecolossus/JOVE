package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import java.util.Set;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.pipeline.DepthStencilStageBuilder.*;
import org.sarge.jove.util.BitMask;

class DepthStencilStageBuilderTest {
	private DepthStencilStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new DepthStencilStageBuilder();
	}

	@Test
	void build() {
		// Build descriptor
		final var result = builder
				.enable()
				.compare(VkCompareOp.GREATER)
				.bounds(0.5f, 1)
				.write()
				.get();

		// Check descriptor
		assertEquals(0, result.flags);
		assertEquals(true, result.depthTestEnable);
		assertEquals(VkCompareOp.GREATER, result.depthCompareOp);
		assertEquals(true, result.depthBoundsTestEnable);
		assertEquals(0.5f, result.minDepthBounds);
		assertEquals(1f, result.maxDepthBounds);
		assertEquals(true, result.depthWriteEnable);
		//assertEquals(false, result.stencilTestEnable);
	}

	@Nested
	class StencilStateBuilderTests {
		private StencilStateBuilder state;

		@BeforeEach
		void before() {
			state = new StencilStateBuilder();
		}

		@Test
		void build() {
			final VkStencilOpState result = state
					.fail(VkStencilOp.REPLACE)
					.pass(VkStencilOp.INVERT)
					.depthFail(VkStencilOp.ZERO)
					.mask(StencilMaskType.COMPARE, 1)
					.mask(StencilMaskType.WRITE, 2)
					.mask(StencilMaskType.REFERENCE, 3)
					.build();

			assertEquals(VkStencilOp.REPLACE, result.failOp);
			assertEquals(VkStencilOp.INVERT, result.passOp);
			assertEquals(VkStencilOp.ZERO, result.depthFailOp);
			assertEquals(1, result.compareMask);
			assertEquals(2, result.writeMask);
			assertEquals(3, result.reference);
		}

		@Test
		void empty() {
			final VkStencilOpState result = state.build();
			assertEquals(VkStencilOp.KEEP, result.failOp);
			assertEquals(VkStencilOp.KEEP, result.passOp);
			assertEquals(VkStencilOp.KEEP, result.depthFailOp);
			assertEquals(0, result.compareMask);
			assertEquals(0, result.writeMask);
			assertEquals(0, result.reference);
		}
	}

	@Nested
	class DynamicStateTests {
		private VulkanLibrary lib;
		private Command.Buffer buffer;

		@BeforeEach
		void before() {
			lib = mock(VulkanLibrary.class);
			buffer = mock(Command.Buffer.class);
		}

    	@Test
    	void setDynamicDepthBounds() {
    		final Command cmd = builder.setDynamicDepthBounds(0.5f, 1);
    		cmd.record(lib, buffer);
    		verify(lib).vkCmdSetDepthBounds(buffer, 0.5f, 1f);
    	}

    	@Test
    	void setDynamicStencilCompareMask() {
    		final Command cmd = builder.setDynamicStencilCompareMask(StencilMaskType.WRITE, Set.of(VkStencilFaceFlag.FRONT), 42);
    		cmd.record(lib, buffer);
    		verify(lib).vkCmdSetStencilWriteMask(buffer, BitMask.reduce(VkStencilFaceFlag.FRONT), 42);
    	}
    }
}
