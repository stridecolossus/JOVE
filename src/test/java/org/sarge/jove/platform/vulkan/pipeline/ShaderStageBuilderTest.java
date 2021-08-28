package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.VkPipelineShaderStageCreateInfo;
import org.sarge.jove.platform.vulkan.VkShaderStage;
import org.sarge.jove.platform.vulkan.core.Shader;

import com.sun.jna.Pointer;

public class ShaderStageBuilderTest {
	private ShaderStageBuilder builder;
	private Shader shader;

	@BeforeEach
	void before() {
		// Create builder
		builder = new ShaderStageBuilder();
		builder.init();

		// Create shader module
		shader = mock(Shader.class);
		when(shader.handle()).thenReturn(new Handle(new Pointer(42)));
	}

	@Test
	void constructor() {
		assertEquals(0, builder.size());
	}

	@Test
	void build() {
		// Create a shader stage
		builder
				.stage(VkShaderStage.VERTEX)
				.name("name")
				.shader(shader)
				.build();

		// Check builder
		assertEquals(1, builder.size());

		// Check descriptor
		final VkPipelineShaderStageCreateInfo info = builder.get();
		assertNotNull(info);
		assertEquals(0, info.flags);
		assertEquals("name", info.pName);
		assertEquals(VkShaderStage.VERTEX, info.stage);
		assertEquals(shader.handle(), info.module);
	}

	@Test
	void initAlreadyInitialised() {
		assertThrows(IllegalStateException.class, () -> builder.init());
	}

	@Test
	void buildShaderStageNotSpecified() {
		builder.shader(shader);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void buildShaderModuleNotSpecified() {
		builder.stage(VkShaderStage.FRAGMENT);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}

	@Test
	void buildVertexShaderNotSpecified() {
		builder.stage(VkShaderStage.FRAGMENT).shader(shader).build();
		assertThrows(IllegalStateException.class, () -> builder.get());
	}

	@Test
	void buildDuplicateStage() {
		builder.stage(VkShaderStage.FRAGMENT).shader(shader).build();
		builder.init();
		builder.stage(VkShaderStage.FRAGMENT).shader(shader);
		assertThrows(IllegalArgumentException.class, () -> builder.build());
	}
}
