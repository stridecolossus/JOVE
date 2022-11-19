package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.BitMask;

public class ColourBlendPipelineStageBuilderTest {
	private static final float[] CONSTANTS = {1, 2, 3, 4};

	private ColourBlendPipelineStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new ColourBlendPipelineStageBuilder();
	}

	@Test
	void create() {
		// Build descriptor
		final var info = builder
				.enable(true)
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
		assertEquals(true, attachment.blendEnable);
		assertEquals(new BitMask<>(15), attachment.colorWriteMask);
		assertEquals(VkBlendOp.SUBTRACT, attachment.colorBlendOp);
		assertEquals(VkBlendFactor.SRC_ALPHA, attachment.srcColorBlendFactor);
		assertEquals(VkBlendFactor.DST_ALPHA, attachment.dstColorBlendFactor);
		assertEquals(VkBlendOp.MAX, attachment.alphaBlendOp);
		assertEquals(VkBlendFactor.ONE, attachment.srcAlphaBlendFactor);
		assertEquals(VkBlendFactor.ONE, attachment.dstAlphaBlendFactor);
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
}
