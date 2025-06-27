package org.sarge.jove.platform.vulkan.pipeline;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.*;
import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.pipeline.ViewportStage.Viewport;
import org.sarge.jove.platform.vulkan.render.RenderPass;

class GraphicsPipelineBuilderTest {
	private GraphicsPipelineBuilder builder;
	private DeviceContext dev;
	private RenderPass pass;
	private PipelineLayout layout;
	private Viewport viewport;
	private ProgrammableShaderStage vertex;

	@BeforeEach
	void before() {
		dev = new MockDeviceContext();
		pass = null; // TODO
		layout = null;
		viewport = new Viewport(new Rectangle(0, 0, 1024, 768));

		vertex = new ProgrammableShaderStage.Builder()
				.stage(VkShaderStage.VERTEX)
				.shader(null)
				.build();

		builder = new GraphicsPipelineBuilder();
	}

	@Test
	void build() {
		final Pipeline pipeline = builder
				.pass(pass)
				.layout(layout)
				.shader(vertex)
				.build(dev);

		assertEquals(false, pipeline.isParent());

		// TODO - verify create info
		// TODO - verify each fixed function descriptor?
	}

	@Test
	void pass() {
		assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
	}

	@Test
	void layout() {
		builder.pass(pass);
		assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
	}

	@Test
	void vertex() {
		builder.pass(pass);
		builder.layout(layout);
		builder.viewport().viewportAndScissor(viewport);
		assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
	}

	@Test
	void viewport() {
		builder.pass(pass);
		builder.layout(layout);
		builder.shader(vertex);
		assertThrows(IllegalArgumentException.class, () -> builder.build(dev));
	}

	@Test
	void derive() {
		final Pipeline parent = new Pipeline(new Handle(1), dev, VkPipelineBindPoint.GRAPHICS, layout, true);
		assertEquals(true, parent.isParent());

		final Pipeline pipeline = builder
				.pass(pass)
				.layout(layout)
				.shader(vertex)
				.parent(parent)
				.build(dev);

		assertEquals(false, pipeline.isParent());

		// TODO - verify derivative -> method
	}

	@Test
	void cache() {
		final PipelineCache cache = null;
		final Pipeline[] pipelines = GraphicsPipelineBuilder.build(new GraphicsPipelineBuilder[]{builder}, cache, dev);
		// TODO
	}

	@DisplayName("A pipeline derived from a sibling...")
	@Nested
	class SiblingTest {
		private GraphicsPipelineBuilder sibling;

		@BeforeEach
		void before() {
    		final var sibling = new GraphicsPipelineBuilder();
    		sibling.pass(pass);
    		sibling.layout(layout);
    		sibling.viewport().viewportAndScissor(viewport);
    		sibling.shader(vertex);
    		sibling.sibling(0);
		}

    	@DisplayName("can be created by constructing an array of pipelines")
    	@Test
    	void sibling() {
    		builder.allowDerivatives();
    		GraphicsPipelineBuilder.build(new GraphicsPipelineBuilder[]{builder, sibling}, null, dev);
    	}

    	@DisplayName("must refer to a parent pipeline that allows derivatives")
    	@Test
    	void siblingInvalidParent() {
    		assertThrows(IllegalArgumentException.class, () -> GraphicsPipelineBuilder.build(new GraphicsPipelineBuilder[]{builder, sibling}, null, dev));
    	}

    	@DisplayName("must specify a valid array index")
    	@Test
    	void siblingInvalidIndex() {
    		sibling.sibling(2);
    		assertThrows(IndexOutOfBoundsException.class, () -> GraphicsPipelineBuilder.build(new GraphicsPipelineBuilder[]{builder, sibling}, null, dev));
    	}

    	@DisplayName("cannot refer to itself as the parent")
    	@Test
    	void siblingSelf() {
    		sibling.sibling(1);
    		assertThrows(IndexOutOfBoundsException.class, () -> GraphicsPipelineBuilder.build(new GraphicsPipelineBuilder[]{builder, sibling}, null, dev));
    	}

    	@DisplayName("must be after the parent pipeline in the array")
    	@Test
    	void siblingPrevious() {
    		sibling.sibling(1);
    		GraphicsPipelineBuilder.build(new GraphicsPipelineBuilder[]{sibling, builder}, null, dev);
    	}
    }
}

//
//	private GraphicsPipelineBuilder builder;
//	private RenderPass pass;
//	private VkGraphicsPipelineCreateInfo info;
//	private PipelineLayout layout;
//	private Shader shader;
//	private DeviceContext dev;
//
//	@BeforeEach
//	void before() {
//		// TODO - messy
//		final Subpass subpass = new Subpass();
//		final Attachment attachment = new Attachment.Builder(VkFormat.B8G8R8A8_SINT).finalLayout(VkImageLayout.COLOR_ATTACHMENT_OPTIMAL).build();
//		subpass.colour(attachment);
//
//		dev = new MockDeviceContext();
//		pass = RenderPass.create(dev, List.of(subpass));
//		builder = new GraphicsPipelineBuilder(pass);
//		info = new VkGraphicsPipelineCreateInfo();
//		layout = new PipelineLayout.Builder().build(dev);
//		shader = Shader.create(dev, new byte[0]);
//	}
//
//	@Test
//	void type() {
//		assertEquals(VkPipelineBindPoint.GRAPHICS, builder.type());
//	}
//
//	@Test
//	void identity() {
//		assertEquals(true, builder.alpha().dataEquals(info));
//	}
//
//	@Test
//	void populate() {
//		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader));
//		builder.viewport(new Rectangle(new Dimensions(2, 3)));
//		builder.populate(EnumMask.of(), layout, new Handle(1), 2, info);
//		assertEquals(EnumMask.of(), info.flags);
//		assertEquals(layout.handle(), info.layout);
//		assertEquals(new Handle(1), info.basePipelineHandle);
//		assertEquals(2, info.basePipelineIndex);
//		assertEquals(1, info.stageCount);
//		assertNotNull(info.pStages);
//		assertNotNull(info.pVertexInputState);
//		assertNotNull(info.pInputAssemblyState);
//		assertNotNull(info.pRasterizationState);
//		assertNotNull(info.pMultisampleState);
//		assertNotNull(info.pDepthStencilState);
//		assertNotNull(info.pColorBlendState);
//		assertNull(info.pTessellationState);
//		assertNull(info.pDynamicState);
//	}
//
//	@DisplayName("A graphics pipeline must contain a vertex shader")
//	@Test
//	void vertex() {
//		builder.viewport(new Rectangle(new Dimensions(2, 3)));
//		assertThrows(IllegalStateException.class, () -> builder.populate(EnumMask.of(), layout, null, -1, info));
//	}
//
//	@DisplayName("A graphics pipeline must configure at least one viewport")
//	@Test
//	void viewports() {
//		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader));
//		assertThrows(IllegalArgumentException.class, () -> builder.populate(EnumMask.of(), layout, null, -1, info));
//	}
//
//	@DisplayName("A graphics pipeline cannot contain a duplicate shader stage")
//	@Test
//	void duplicate() {
//		builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader));
//		assertThrows(IllegalArgumentException.class, () -> builder.shader(new ProgrammableShaderStage(VkShaderStage.VERTEX, shader)));
//	}
//
//	@Test
//	void create() {
//		builder.create(dev, null, new VkGraphicsPipelineCreateInfo[]{info}, new Pointer[1]);
//		verify(dev.library()).vkCreateGraphicsPipelines(dev, null, 1, new VkGraphicsPipelineCreateInfo[]{info}, null, new Pointer[1]);
//	}
//}
