package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;

class ComputePipelineBuilderTest {
	private ComputePipelineBuilder builder;
	private PipelineLayout layout;
	private ProgrammableShaderStage stage;
	private VkComputePipelineCreateInfo info;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		layout = new PipelineLayout.Builder().build(dev);
		stage = new ProgrammableShaderStage(VkShaderStage.VERTEX, Shader.create(dev, new byte[0]));
		builder = new ComputePipelineBuilder(stage);
		info = new VkComputePipelineCreateInfo();
	}

	@Test
	void type() {
		assertEquals(VkPipelineBindPoint.COMPUTE, builder.type());
	}

	@Test
	void identity() {
		assertEquals(true, builder.identity().dataEquals(info));
	}

	@Test
	void populate() {
		builder.populate(BitMask.of(), layout, new Handle(1), 2, info);
		assertEquals(BitMask.of(), info.flags);
		assertEquals(layout.handle(), info.layout);
		assertEquals(new Handle(1), info.basePipelineHandle);
		assertEquals(2, info.basePipelineIndex);
		assertEquals(VkShaderStage.VERTEX, info.stage.stage);
	}

	@Test
	void create() {
		builder.create(dev, null, new VkComputePipelineCreateInfo[]{info}, new Pointer[1]);
		verify(dev.library()).vkCreateComputePipelines(dev, null, 1, new VkComputePipelineCreateInfo[]{info}, null, new Pointer[1]);
	}
}
