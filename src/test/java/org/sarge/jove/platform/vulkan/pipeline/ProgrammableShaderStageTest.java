package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;

class ProgrammableShaderStageTest {
	private ProgrammableShaderStage stage;
	private Shader shader;

	@BeforeEach
	void before() {
		shader = Shader.create(new MockDeviceContext(), new byte[0]); // TODO - MockShader?

		stage = new ProgrammableShaderStage.Builder()
				.stage(VkShaderStage.VERTEX)
				.name("name")
				.constants(new SpecialisationConstants(Map.of(1, 2)))
				.build();
	}

	@Test
	void descriptor() {
		final VkPipelineShaderStageCreateInfo info = stage.descriptor();
		assertEquals(0, info.flags);
		assertEquals(VkShaderStage.VERTEX, info.stage);
		assertEquals(shader.handle(), info.module);
		assertEquals("main", info.pName);
		assertEquals(null, info.pSpecializationInfo);
	}
}
