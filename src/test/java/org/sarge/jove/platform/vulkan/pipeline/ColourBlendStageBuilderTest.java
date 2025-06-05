package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.util.EnumMask;

public class ColourBlendStageBuilderTest {
	private static final float[] CONSTANTS = {1, 2, 3, 4};

	private ColourBlendStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new ColourBlendStageBuilder();
	}

	@Test
	void create() {
		// Build descriptor
		final var info = builder
				.enable()
				.operation(VkLogicOp.COPY)
				.constants(CONSTANTS)
				.attachment()
					.mask("RGBA")
					.colour()
						.source(VkBlendFactor.SRC_ALPHA)
						.destination(VkBlendFactor.DST_ALPHA)
						.operation(VkBlendOp.SUBTRACT)
						.build()
					.alpha()
						.operation(VkBlendOp.MAX)
						.build()
					.build()
				.get();

		// Check descriptor
		assertEquals(0, info.flags);
		assertEquals(true, info.logicOpEnable);
		assertEquals(VkLogicOp.COPY, info.logicOp);
		assertArrayEquals(CONSTANTS, info.blendConstants);
		assertEquals(1, info.attachmentCount);
		assertNotNull(info.pAttachments);

		final VkPipelineColorBlendAttachmentState attachment = info.pAttachments;
		final var mask = List.of(VkColorComponent.values());
		assertEquals(true, attachment.blendEnable);
		assertEquals(new EnumMask<>(mask), attachment.colorWriteMask);
		assertEquals(VkBlendOp.SUBTRACT, attachment.colorBlendOp);
		assertEquals(VkBlendFactor.SRC_ALPHA, attachment.srcColorBlendFactor);
		assertEquals(VkBlendFactor.DST_ALPHA, attachment.dstColorBlendFactor);
		assertEquals(VkBlendOp.MAX, attachment.alphaBlendOp);
		assertEquals(VkBlendFactor.ONE, attachment.srcAlphaBlendFactor);
		assertEquals(VkBlendFactor.ZERO, attachment.dstAlphaBlendFactor);
	}

	@Test
	void disabled() {
		final var info = builder.get();
		assertEquals(false, info.logicOpEnable);
		assertEquals(1, info.attachmentCount);
	}

	@Test
	void invalid() {
		assertThrows(IllegalArgumentException.class, () -> builder.attachment().mask("cobblers"));
	}

	@Test
	void constants() {
		assertThrows(IndexOutOfBoundsException.class, () -> builder.constants(new float[0]));
	}

	@Test
	void setDynamicBlendConstants() {
		final var lib = mock(VulkanLibrary.class);
		final var buffer = mock(Command.CommandBuffer.class);
		final Command cmd = builder.setDynamicBlendConstants(CONSTANTS);
		cmd.execute(lib, buffer);
		verify(lib).vkCmdSetBlendConstants(buffer, CONSTANTS);
	}
}
