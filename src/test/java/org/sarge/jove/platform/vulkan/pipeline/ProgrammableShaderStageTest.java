package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;

import org.junit.jupiter.api.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.MockDeviceContext;
import org.sarge.jove.platform.vulkan.pipeline.SpecialisationConstants.Constant.IntegerConstant;

class ProgrammableShaderStageTest {
	private ProgrammableShaderStage stage;
	private VkPipelineShaderStageCreateInfo info;
	private Shader shader;

	@BeforeEach
	void before() {
		shader = Shader.create(new MockDeviceContext(), new byte[0]);
		stage = new ProgrammableShaderStage(VkShaderStage.VERTEX, shader);
		info = new VkPipelineShaderStageCreateInfo();
	}

	@Test
	void constructor() {
		assertEquals(VkShaderStage.VERTEX, stage.stage());
	}

	@Test
	void populate() {
		stage.populate(info);
		assertEquals(0, info.flags);
		assertEquals(VkShaderStage.VERTEX, info.stage);
		assertEquals(shader.handle(), info.module);
		assertEquals("main", info.pName);
		assertEquals(null, info.pSpecializationInfo);
	}

	@Nested
	class BuilderTests {
		private ProgrammableShaderStage.Builder builder;

		@BeforeEach
		void before() {
			builder = new ProgrammableShaderStage.Builder(VkShaderStage.VERTEX);
		}

		@Test
		void build() {
			builder.shader(shader);
			builder.name("name");
			builder.constants(new SpecialisationConstants(Map.of(1, new IntegerConstant(2))));
			builder.build().populate(info);
			assertEquals(VkShaderStage.VERTEX, info.stage);
			assertEquals(shader.handle(), info.module);
			assertEquals("name", info.pName);
    		assertEquals(1, info.pSpecializationInfo.mapEntryCount);
    		assertNotNull(info.pSpecializationInfo.pMapEntries);
		}

		@Test
		void undefined() {
    		assertThrows(IllegalArgumentException.class, () -> builder.build());
		}
	}
}
