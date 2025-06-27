package org.sarge.jove.platform.vulkan.pipeline;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import java.util.*;

import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.RenderPass;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>graphics pipeline builder</i> creates a rendering pipeline.
 * <p>
 * Notes:
 * <ul>
 * <li>The pipeline <b>must</b> contain a {@link VkShaderStage#VERTEX} shader stage</li>
 * <li>A pipeline can be <i>derived</i> from an existing <i>parent</i> by configuring the {@link #parent(Pipeline)} pipeline</li>
 * <li>A group of pipelines can be constructed in one operation using the {@link #build(GraphicsPipelineBuilder[], PipelineCache, DeviceContext)} variant</li>
 * <li>Pipelines can also be derived from a sibling within an array by configuring the {@link #sibling(int)} index</li>
 * </ul>
 * @author Sarge
 */
public class GraphicsPipelineBuilder {
	// Pipeline properties
	private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
	private PipelineLayout layout;
	private RenderPass pass;
	private Handle parent;
	private int sibling = -1;

	// Programmable shader stages
	private final Map<VkShaderStage, ProgrammableShaderStage> shaders = new HashMap<>();

	// Fixed function stages
	private final VertexInputStage input = new VertexInputStage();
	private final InputAssemblyStage assembly = new InputAssemblyStage();
	private final TesselationStage tesselation = new TesselationStage();
	private final ViewportStage viewport = new ViewportStage();
	private final RasterizerStage raster = new RasterizerStage();
	private final MultiSampleStage multi = new MultiSampleStage();
	private final DepthStencilStage depth = new DepthStencilStage();
	private final ColourBlendStage blend = new ColourBlendStage();
	private final DynamicStateStage dynamic = new DynamicStateStage();

	/**
	 * Sets the render of for this pipelines.
	 * @param pass Render pass
	 */
	public GraphicsPipelineBuilder pass(RenderPass pass) {
		this.pass = pass;
		return this;
	}

	/**
	 * Sets the layout of this pipeline.
	 * @param layout Pipeline layout
	 */
	public GraphicsPipelineBuilder layout(PipelineLayout layout) {
		this.layout = layout;
		return this;
	}

	/**
	 * Sets this pipeline as a parent pipeline that can be used to derive further pipelines.
	 * @see VkPipelineCreateFlag#ALLOW_DERIVATIVES
	 * @see #parent(Pipeline)
	 */
	public GraphicsPipelineBuilder allowDerivatives() {
		flags.add(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
		return this;
	}

	/**
	 * Sets this pipeline as a derivative from the given parent pipeline.
	 * @param parent Parent pipeline
	 * @throws IllegalArgumentException if {@link #parent} does not allow derivatives
	 * @throws IllegalStateException if this pipeline is already configured as a derivative
	 * @see #allowDerivatives()
	 * @see VkPipelineCreateFlag#DERIVATIVE
	 */
	public GraphicsPipelineBuilder parent(Pipeline parent) {
		if(!parent.isParent()) throw new IllegalArgumentException("Pipeline does not allow derivatives: " + parent);
		this.parent = parent.handle();
		derivative();
		return this;
	}

	/**
	 * Sets the index of the sibling from which pipeline is derived when creating an array of pipelines.
	 * @param sibling Sibling index
	 * @throws IllegalStateException if this pipeline is already configured as a derivative
	 * @see #build(List, DeviceContext)
	 */
	public GraphicsPipelineBuilder sibling(int sibling) {
		this.sibling = requireZeroOrMore(sibling);
		derivative();
		return this;
	}

	/**
	 * Sets this pipeline as a derivative.
	 */
	private void derivative() {
		if(flags.contains(VkPipelineCreateFlag.DERIVATIVE)) throw new IllegalStateException("Pipeline already configured as a derivative");
		flags.add(VkPipelineCreateFlag.DERIVATIVE);
	}

	/**
	 * @return Builder for the vertex input stage
	 */
	public VertexInputStage input() {
		return input;
	}

	/**
	 * @return Builder for the input assembly stage
	 */
	public InputAssemblyStage assembly() {
		return assembly;
	}

	/**
	 * @return Builder for the tesselation stage
	 */
	public TesselationStage tesselation() {
		return tesselation;
	}

	/**
	 * @return Builder for the viewport stage
	 */
	public ViewportStage viewport() {
		return viewport;
	}

	/**
	 * @return Builder for the rasterizer stage
	 */
	public RasterizerStage rasterizer() {
		return raster;
	}

	/**
	 * @return Builder for the multi-sample stage
	 */
	public MultiSampleStage multi() {
		return multi;
	}

	/**
	 * @return Builder for the depth-stencil stage
	 */
	public DepthStencilStage depth() {
		return depth;
	}

	/**
	 * @return Builder for the colour-blend stage
	 */
	public ColourBlendStage blend() {
		return blend;
	}

	/**
	 * @return Builder for the dynamic state stage
	 */
	public DynamicStateStage dynamic() {
		return dynamic;
	}

	/**
	 * Adds a shader stage.
	 * @param shader Shader stage
	 * @throws IllegalStateException for a duplicate shader stage
	 */
	public GraphicsPipelineBuilder shader(ProgrammableShaderStage shader) {
		final VkShaderStage stage = shader.stage();
		if(shaders.containsKey(stage)) throw new IllegalStateException("Duplicate shader stage: " + stage);
		shaders.put(stage, shader);
		return this;
	}

	/**
	 * @return Create descriptor for this pipeline
	 */
	private VkGraphicsPipelineCreateInfo populate() {
		// Validate
		requireNonNull(pass);
		requireNonNull(layout);

		// Init descriptor
		final var info = new VkGraphicsPipelineCreateInfo();
		info.flags = new EnumMask<>(flags);
		info.layout = layout.handle();
		info.renderPass = pass.handle();
		info.subpass = 0; // TODO

		// Init shader pipeline stages
		if(!shaders.containsKey(VkShaderStage.VERTEX)) throw new IllegalStateException("No vertex shader specified");
		info.stageCount = shaders.size();
		info.pStages = shaders.values().stream().map(ProgrammableShaderStage::descriptor).toArray(VkPipelineShaderStageCreateInfo[]::new);

		// Init fixed function stages
		info.pVertexInputState = input.descriptor();
		info.pInputAssemblyState = assembly.descriptor();
		info.pTessellationState = tesselation.descriptor();
		info.pViewportState = viewport.descriptor();
		info.pRasterizationState = raster.descriptor();
		info.pMultisampleState = multi.descriptor();
		info.pDepthStencilState = depth.descriptor();
		info.pColorBlendState = blend.descriptor();			// TODO - check number of blend attachments = framebuffers
		info.pDynamicState = dynamic.descriptor();

		// Init derivative pipelines
		info.basePipelineHandle = parent;
		info.basePipelineIndex = sibling;

		return info;
	}

	/**
	 * Builds this graphics pipeline.
	 * @param dev Logical device
	 * @return Graphics pipeline
	 * @throws IndexOutOfBoundsException if this pipeline is configured to derive from a sibling
	 * @see #build(GraphicsPipelineBuilder[], DeviceContext)
	 */
	public Pipeline build(DeviceContext dev) {
		final var array = build(new GraphicsPipelineBuilder[]{this}, null, dev);
		return array[0];
	}

	/**
	 * Builds an array of pipelines in one operation.
	 * @param builders		Pipeline builders
	 * @param cache			Optional pipeline cache
	 * @param dev			Logical device
	 * @return Pipelines
	 * @throws IllegalStateException if a {@link VkShaderStage#VERTEX} programmable shader stage is not configured for any pipeline
	 * @throws IndexOutOfBoundsException if the sibling index is invalid for a derived pipeline
	 * @see #sibling(int)
	 */
	public static Pipeline[] build(GraphicsPipelineBuilder[] builders, PipelineCache cache, DeviceContext dev) {
		// Validate pipeline siblings
		for(int n = 0; n < builders.length; ++n) {
			final int sibling = builders[n].sibling;
			if(sibling >= 0) {
				if(sibling >= builders.length) throw new IndexOutOfBoundsException("Invalid sibling index: sibling=%d pipeline=%d".formatted(sibling, n));
				if(sibling >= n) throw new IndexOutOfBoundsException("Sibling index must refer to a previous pipeline in an array: pipeline=" + n);
				// TODO - self?
			}
		}

		// Build descriptors
		final var descriptors = Arrays
				.stream(builders)
				.map(GraphicsPipelineBuilder::populate)
				.toArray(VkGraphicsPipelineCreateInfo[]::new);

		// Create native pipelines
		final Handle[] handles = new Handle[builders.length];
		final VulkanLibrary lib = dev.vulkan().library();
//		lib.vkCreateGraphicsPipelines(dev, cache, descriptors.length, descriptors, null, handles);

		// Construct pipelines
		final Pipeline[] pipelines = new Pipeline[builders.length];
		Arrays.setAll(pipelines, n -> pipeline(handles[n], dev, builders[n]));

		return pipelines;
	}

	/**
	 * Constructs a pipeline.
	 */
	private static Pipeline pipeline(Handle handle, DeviceContext dev, GraphicsPipelineBuilder builder) {
		final boolean parent = builder.flags.contains(VkPipelineCreateFlag.ALLOW_DERIVATIVES);
		return new Pipeline(handle, dev, VkPipelineBindPoint.GRAPHICS, builder.layout, parent);
	}
}
