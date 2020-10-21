package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.sarge.jove.common.NativeObject.Handle;
import org.sarge.jove.platform.vulkan.VkShaderStageFlag;
import org.sarge.jove.platform.vulkan.core.Shader;

import com.sun.jna.Pointer;

public class ShaderStageBuilderTest {
	private ShaderStageBuilder builder;
	private Shader shader;

	@BeforeEach
	void before() {
		builder = new ShaderStageBuilder();
		shader = mock(Shader.class);
		when(shader.handle()).thenReturn(new Handle(new Pointer(42)));
	}

	@Test
	void create() {
		// Create a shader stage
		final var info = builder
				.stage(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT)
				.name("name")
				.shader(shader)
				.result();

		// Check descriptor
		assertNotNull(info);
		assertEquals("name", info.pName);
		assertEquals(VkShaderStageFlag.VK_SHADER_STAGE_FRAGMENT_BIT, info.stage);
		assertEquals(shader.handle(), info.module);
		assertEquals(0, info.flags);
	}
}
