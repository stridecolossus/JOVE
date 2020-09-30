package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ColourBlendStageBuilderTest {
	private ColourBlendStageBuilder builder;

	@BeforeEach
	void before() {
		builder = new ColourBlendStageBuilder();
	}

	@Test
	void create() {
		// Build descriptor
		final var info = builder
				.attachment()
				.enabled(true)
				.result();

		// Check descriptor
		assertNotNull(info);
		assertEquals(2, info.attachmentCount);
		assertNotNull(info.pAttachments);

		// TODO - how to test attachments?
	}
}
