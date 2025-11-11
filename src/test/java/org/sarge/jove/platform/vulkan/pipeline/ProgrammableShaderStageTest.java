package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.core.MockLogicalDevice;

class ProgrammableShaderStageTest {
	private ProgrammableShaderStage stage;

	@BeforeEach
	void before() {
		stage = new ProgrammableShaderStage.Builder()
				.stage(VkShaderStage.VERTEX)
				.shader(new MockShader(new MockLogicalDevice()))
				.name("name")
				.constants(new SpecialisationConstants(Map.of(1, 2)))
				.build();
	}

	@Test
	void descriptor() {
		final VkPipelineShaderStageCreateInfo info = stage.descriptor();
		assertEquals(0, info.flags);
		assertEquals(VkShaderStage.VERTEX, info.stage);
		assertNotNull(info.module);
		assertEquals("name", info.pName);
		assertNotNull(info.pSpecializationInfo);
	}
}
