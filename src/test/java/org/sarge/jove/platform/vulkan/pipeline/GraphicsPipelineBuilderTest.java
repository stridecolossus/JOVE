package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.render.*;
import org.sarge.jove.util.BitMask;

import com.sun.jna.Pointer;

class GraphicsPipelineBuilderTest {
	private GraphicsPipelineBuilder builder;
	private RenderPass pass;
	private VkGraphicsPipelineCreateInfo info;
	private PipelineLayout layout;
	private Shader shader;
	private DeviceContext dev;

	@BeforeEach
	void before() {
		// TODO - messy
		final Subpass subpass = new Subpass();
		final Attachment attachment = new Attachment.Builder(VkFormat.B8G8R8A8_SINT).finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL).build();
		subpass.colour(attachment);

		dev = new MockDeviceContext();
		pass = RenderPass.create(dev, List.of(subpass));
		builder = new GraphicsPipelineBuilder(pass);
		info = new VkGraphicsPipelineCreateInfo();
		layout = new PipelineLayout.Builder().build(dev);
		shader = Shader.create(dev, new byte[0]);
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
		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader));
		builder.viewport(new Rectangle(new Dimensions(2, 3)));
		builder.populate(BitMask.of(), layout, new Handle(1), 2, info);
		assertEquals(BitMask.of(), info.flags);
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
		assertThrows(IllegalStateException.class, () -> builder.populate(BitMask.of(), layout, null, -1, info));
	}

	@DisplayName("A graphics pipeline must configure at least one viewport")
	@Test
	void viewports() {
		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader));
		assertThrows(IllegalArgumentException.class, () -> builder.populate(BitMask.of(), layout, null, -1, info));
	}

	@DisplayName("A graphics pipeline cannot contain a duplicate shader stage")
	@Test
	void duplicate() {
		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader));
		assertThrows(IllegalArgumentException.class, () -> builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader)));
	}

	@Test
	void create() {
		builder.create(dev, null, new VkGraphicsPipelineCreateInfo[]{info}, new Pointer[1]);
		verify(dev.library()).vkCreateGraphicsPipelines(dev, null, 1, new VkGraphicsPipelineCreateInfo[]{info}, null, new Pointer[1]);
	}
}
