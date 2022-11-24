package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.nio.ByteBuffer;
import java.util.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;

class ProgrammableShaderStageTest {
	private ProgrammableShaderStage stage;
	private VkPipelineShaderStageCreateInfo info;
	private Shader shader;

	@BeforeEach
	void before() {
		shader = mock(Shader.class);
		stage = new ProgrammableShaderStage(VkShaderStage.VERTEX, shader);
		info = new VkPipelineShaderStageCreateInfo();
		when(shader.handle()).thenReturn(new Handle(1));
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
			builder.constants(Map.of(1, 2));
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

    @DisplayName("Shader specialisation constants...")
    @Nested
    class SpecialisationConstantTests {
		private ProgrammableShaderStage.Builder builder;

		@BeforeEach
		void before() {
			builder = new ProgrammableShaderStage.Builder(VkShaderStage.VERTEX);
			builder.shader(shader);
		}

    	@DisplayName("can be constructed from an indexed map of values")
    	@Test
    	void constants() {
    		// Build constants table
    		final Map<Integer, Object> map = Map.of(
    				1, 1,
    				2, 2f,
    				3, true
    		);

    		// Create constants
    		builder.constants(new LinkedHashMap<>(map));
    		builder.build().populate(info);
    		assertNotNull(info.pSpecializationInfo.pMapEntries);
    		assertEquals(3, info.pSpecializationInfo.mapEntryCount);
    		assertEquals(4 + 4 + 4, info.pSpecializationInfo.dataSize);

    		// Check data buffer
    		final ByteBuffer bb = ByteBuffer.allocate(12);
    		bb.putInt(1);
    		bb.putFloat(2f);
    		bb.putInt(1);
    		assertEquals(bb, info.pSpecializationInfo.pData);
    	}

    	@DisplayName("can be empty")
    	@Test
    	void empty() {
    		builder.constants(Map.of());
    		builder.build().populate(info);
    		stage.populate(info);
    		assertEquals(null, info.pSpecializationInfo);
    	}

    	@DisplayName("must have a supported data type")
    	@Test
    	void invalid() {
    		assertThrows(IllegalArgumentException.class, () -> builder.constants(Map.of(1, "doh")));
    	}
    }
}
