package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.platform.vulkan.util.AbstractVulkanTest;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;

class GraphicsPipelineBuilderTest extends AbstractVulkanTest {
	private GraphicsPipelineBuilder builder;
	private RenderPass pass;
	private VkGraphicsPipelineCreateInfo info;
	private PipelineLayout layout;

	@BeforeEach
	void before() {
		pass = mock(RenderPass.class);
		builder = new GraphicsPipelineBuilder(pass);
		info = new VkGraphicsPipelineCreateInfo();
		layout = mock(PipelineLayout.class);
	}

	@Test
	void type() {
		assertEquals(VkPipelineBindPoint.GRAPHICS, builder.type());
	}

	@Test
	void identity() {
		assertEquals(true, builder.identity().dataEquals(info));
	}

	@Test
	void populate() {
		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, mock(Shader.class)));
		builder.viewport(new Rectangle(new Dimensions(2, 3)));
		builder.populate(new BitMask<>(0), layout, new Handle(1), 2, info);
		assertEquals(new BitMask<>(0), info.flags);
		assertEquals(layout.handle(), info.layout);
		assertEquals(new Handle(1), info.basePipelineHandle);
		assertEquals(2, info.basePipelineIndex);
		assertEquals(1, info.stageCount);
		assertNotNull(info.pStages);
		assertNotNull(info.pVertexInputState);
		assertNotNull(info.pInputAssemblyState);
		assertNotNull(info.pRasterizationState);
		assertNotNull(info.pMultisampleState);
		assertNotNull(info.pDepthStencilState);
		assertNotNull(info.pColorBlendState);
		assertNull(info.pTessellationState);
		assertNull(info.pDynamicState);
	}

	@DisplayName("A graphics pipeline must contain a vertex shader")
	@Test
	void vertex() {
		builder.viewport(new Rectangle(new Dimensions(2, 3)));
		assertThrows(IllegalStateException.class, () -> builder.populate(new BitMask<>(0), layout, null, -1, info));
	}

	@DisplayName("A graphics pipeline must configure at least one viewport")
	@Test
	void viewports() {
		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, mock(Shader.class)));
		assertThrows(IllegalArgumentException.class, () -> builder.populate(new BitMask<>(0), layout, null, -1, info));
	}

	@DisplayName("A graphics pipeline cannot contain a duplicate shader stage")
	@Test
	void duplicate() {
		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, mock(Shader.class)));
		assertThrows(IllegalArgumentException.class, () -> builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, mock(Shader.class))));
	}

	@Test
	void create() {
		builder.create(dev, null, new VkGraphicsPipelineCreateInfo[]{info}, new Pointer[1]);
		verify(lib).vkCreateGraphicsPipelines(dev, null, 1, new VkGraphicsPipelineCreateInfo[]{info}, null, new Pointer[1]);
	}
}
