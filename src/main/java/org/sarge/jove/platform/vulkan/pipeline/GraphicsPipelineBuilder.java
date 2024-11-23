package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;

import java.util.*;

import org.sarge.jove.common.*;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.pipeline.ViewportStageBuilder.Viewport;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.util.BitMask;

/**
 * Builder for a graphics pipeline.
 * @author Sarge
 */
public class GraphicsPipelineBuilder implements DelegatePipelineBuilder<VkGraphicsPipelineCreateInfo> {
	// Properties
	private final RenderPass pass;
	private final Map<VkShaderStage, ProgrammableShaderStage> shaders = new HashMap<>();

	// Fixed function stages
	private final VertexInputStageBuilder input = new VertexInputStageBuilder();
	private final AssemblyStageBuilder assembly = new AssemblyStageBuilder();
	private final TesselationStageBuilder tesselation = new TesselationStageBuilder();
	private final ViewportStageBuilder viewport = new ViewportStageBuilder();
	private final RasterizerStageBuilder raster = new RasterizerStageBuilder();
	private final MultiSampleStageBuilder multi = new MultiSampleStageBuilder();
	private final DepthStencilStageBuilder depth = new DepthStencilStageBuilder();
	private final ColourBlendStageBuilder blend = new ColourBlendStageBuilder();
	private final DynamicStateStageBuilder dynamic = new DynamicStateStageBuilder();

	/**
	 * Constructor.
	 * @param pass Render pass
	 */
	public GraphicsPipelineBuilder(RenderPass pass) {
		this.pass = requireNonNull(pass);
		init();
	}

	private void init() {
		input.parent(this);
		assembly.parent(this);
		tesselation.parent(this);
		viewport.parent(this);
		raster.parent(this);
		multi.parent(this);
		depth.parent(this);
		blend.parent(this);
		dynamic.parent(this);
	}

	/**
	 * @return Builder for the vertex input stage
	 */
	public VertexInputStageBuilder input() {
		return input;
	}

	/**
	 * @return Builder for the input assembly stage
	 */
	public AssemblyStageBuilder assembly() {
		return assembly;
	}

	/**
	 * @return Builder for the tesselation stage
	 */
	public TesselationStageBuilder tesselation() {
		return tesselation;
	}

	/**
	 * @return Builder for the viewport stage
	 */
	public ViewportStageBuilder viewport() {
		return viewport;
	}

	/**
	 * Convenience helper to add a viewport and scissor rectangle with the same dimensions.
	 * @param rect Viewport/scissor rectangle
	 */
	public GraphicsPipelineBuilder viewport(Rectangle rect) {
		viewport.viewport(new Viewport(rect));
		viewport.scissor(rect);
		return this;
	}

	/**
	 * @return Builder for the rasterizer stage
	 */
	public RasterizerStageBuilder rasterizer() {
		return raster;
	}

	/**
	 * @return Builder for the multi-sample stage
	 */
	public MultiSampleStageBuilder multi() {
		return multi;
	}

	/**
	 * @return Builder for the depth-stencil stage
	 */
	public DepthStencilStageBuilder depth() {
		return depth;
	}

	/**
	 * @return Builder for the colour-blend stage
	 */
	public ColourBlendStageBuilder blend() {
		return blend;
	}

	/**
	 * @return Builder for the dynamic state stage
	 */
	public DynamicStateStageBuilder dynamic() {
		return dynamic;
	}

	/**
	 * Adds a shader stage.
	 * @param shader Shader stage
	 */
	public GraphicsPipelineBuilder shader(ProgrammableShaderStage shader) {
		final VkShaderStage stage = shader.stage();
		if(shaders.containsKey(stage)) throw new IllegalArgumentException("Duplicate shader stage: " + stage);
		shaders.put(stage, shader);
		return this;
	}

	@Override
	public VkPipelineBindPoint type() {
		return VkPipelineBindPoint.GRAPHICS;
	}

	@Override
	public VkGraphicsPipelineCreateInfo identity() {
		return new VkGraphicsPipelineCreateInfo();
	}

	@Override
	public void populate(BitMask<VkPipelineCreateFlag> flags, PipelineLayout layout, Handle base, int parent, VkGraphicsPipelineCreateInfo info) {
		// Init descriptor
		info.flags = flags;
		info.layout = layout.handle();
		info.renderPass = pass.handle();
		info.subpass = 0;		// TODO - subpass?

		// Init shader pipeline stages
		if(!shaders.containsKey(VkShaderStage.VERTEX)) throw new IllegalStateException("No vertex shader specified");
		info.stageCount = shaders.size();
		info.pStages = null; // TODO StructureCollector.pointer(shaders.values(), new VkPipelineShaderStageCreateInfo(), ProgrammableShaderStage::populate);

		// Init fixed function stages
		info.pVertexInputState = input.get();
		info.pInputAssemblyState = assembly.get();
		info.pTessellationState = tesselation.get();
		info.pViewportState = viewport.get();
		info.pRasterizationState = raster.get();
		info.pMultisampleState = multi.get();
		info.pDepthStencilState = depth.get();
		info.pColorBlendState = blend.get();			// TODO - check number of blend attachments = framebuffers
		info.pDynamicState = dynamic.get();

		// Init derivative pipelines
		info.basePipelineHandle = base;
		info.basePipelineIndex = parent;
	}

	@Override
	public int create(DeviceContext dev, PipelineCache cache, VkGraphicsPipelineCreateInfo[] array, Handle[] handles) {
		return dev.vulkan().library().vkCreateGraphicsPipelines(dev, cache, array.length, array, null, handles);
	}
}
