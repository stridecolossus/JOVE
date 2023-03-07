package org.sarge.jove.platform.vulkan.pipeline;

import static org.sarge.jove.platform.vulkan.VkPipelineCreateFlag.*;
import static org.sarge.lib.util.Check.notNull;

import java.util.*;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.util.*;

import com.sun.jna.Pointer;

/**
 * A <i>pipeline</i> defines the configuration for graphics rendering or compute shaders.
 * @author Sarge
 */
public class Pipeline extends VulkanObject {
	private final VkPipelineBindPoint type;
	private final PipelineLayout layout;
	private final boolean parent;

	/**
	 * Constructor.
	 * @param handle		Pipeline handle
	 * @param dev			Device
	 * @param type			Pipeline type
	 * @param layout		Pipeline layout
	 * @param parent		Whether this is a parent pipeline that {@link VkPipelineCreateFlag#ALLOW_DERIVATIVES}
	 */
	Pipeline(Handle handle, DeviceContext dev, VkPipelineBindPoint type, PipelineLayout layout, boolean parent) {
		super(handle, dev);
		this.type = notNull(type);
		this.layout = notNull(layout);
		this.parent = parent;
	}

	/**
	 * @return Type of this pipeline (or the bind point)
	 */
	public VkPipelineBindPoint type() {
		return type;
	}

	/**
	 * @return Layout of this pipeline
	 */
	public PipelineLayout layout() {
		return layout;
	}

	/**
	 * @return Whether this is a parent pipeline
	 * @see VkPipelineCreateFlag#ALLOW_DERIVATIVES
	 */
	public boolean isAllowDerivatives() {
		return parent;
	}

	/**
	 * Creates a command to bind this pipeline.
	 * @return New bind pipeline command
	 */
	public Command bind() {
		return (lib, buffer) -> lib.vkCmdBindPipeline(buffer, type, Pipeline.this);
	}

	@Override
	protected Destructor<Pipeline> destructor(VulkanLibrary lib) {
		return lib::vkDestroyPipeline;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(type)
				.append(layout)
				.append("parent", parent)
				.build();
	}

	/**
	 * Builder for an array of pipelines.
	 * <p>
	 * The descriptor for a concrete pipeline is created by a {@link DelegatePipelineBuilder}.
	 * <p>
	 * Vulkan instantiates pipelines as an <i>array</i> in one operation.
	 * Additionally pipelines can be <i>derived</i> which may improve performance at creation and when switching pipeline state during rendering.
	 * <p>
	 * A pipeline can be derived from:
	 * <ol>
	 * <li>an existing pipeline using the {@link #derive(Pipeline)} method</li>
	 * <li>or from a <i>peer</i> builder within the array via {@link #derive(Builder)}</li>
	 * </ol>
	 * <p>
	 * In both cases the parent pipeline must be configured as {@link #allowDerivatives()} during creation.
	 * <p>
	 * @param <T> Pipeline type
	 */
	public static class Builder<T extends VulkanStructure> {
    	private final DelegatePipelineBuilder<T> delegate;
    	private final Set<VkPipelineCreateFlag> flags = new HashSet<>();
    	private final List<Builder<T>> builders = new ArrayList<>();
    	private Handle base;
    	private Builder<T> parent;

    	/**
    	 * Constructor.
    	 * @param delegate Delegate pipeline builder
    	 */
    	public Builder(DelegatePipelineBuilder<T> delegate) {
    		this.delegate = notNull(delegate);
    		this.builders.add(this);
    	}

    	/**
    	 * Adds a pipeline to be built as a <i>peer</i> of this pipeline.
    	 * Note that all peers are aggregated into this builder.
    	 * @param peer Peer pipeline builder
    	 */
    	public Builder<T> add(Builder<T> peer) {
    		builders.addAll(peer.builders);
    		peer.builders.clear();
    		return this;
    	}

    	/**
    	 * Sets a pipeline creation flag.
    	 * @param flag Pipeline creation flag
    	 */
    	public Builder<T> flag(VkPipelineCreateFlag flag) {
    		this.flags.add(notNull(flag));
    		return this;
    	}

    	/**
    	 * Sets this as a parent from which pipelines can be derived.
    	 * This is a synonym for {@link #flag(VkPipelineCreateFlag)} with a {@link VkPipelineCreateFlag#ALLOW_DERIVATIVES} flag.
    	 * @see #derive(Pipeline)
    	 * @see #derive(Builder)
    	 */
    	public Builder<T> allowDerivatives() {
    		return flag(ALLOW_DERIVATIVES);
    	}

    	/**
    	 * Sets this as a derived pipeline.
    	 * @throws IllegalArgumentException if this pipeline is already derived
    	 */
    	private void derive() {
    		if(flags.contains(DERIVATIVE)) throw new IllegalArgumentException("Pipeline is already a derivative");
    		flags.add(DERIVATIVE);
    	}

    	/**
    	 * Derives this pipeline from an existing base pipeline.
    	 * @param base Base pipeline
    	 * @throws IllegalArgumentException if {@link #parent} does not allow derivatives or this pipeline is already derived
    	 * @see #allowDerivatives()
    	 */
    	public Builder<T> derive(Pipeline base) {
    		if(!base.isAllowDerivatives()) throw new IllegalArgumentException("Invalid parent pipeline: " + base);
    		derive();
    		this.base = base.handle();
    		return this;
    	}

    	/**
    	 * Derives this pipeline from the given builder.
    	 * Note that {@link #parent} and its peers are automatically added to this builder if they are not already present.
    	 * @param parent Parent pipeline builder
    	 * @throws IllegalArgumentException if {@link #parent} is this builder, does not allow derivatives, or this pipeline is already derived
    	 * @see #allowDerivatives()
    	 * @see #add(Builder)
    	 */
    	public Builder<T> derive(Builder<T> parent) {
    		if(parent == this) throw new IllegalArgumentException("Cannot derive from self");
    		if(!parent.flags.contains(ALLOW_DERIVATIVES)) throw new IllegalArgumentException("Invalid peer pipeline: " + parent);
    		this.parent = notNull(parent);
    		add(parent);
    		derive();
    		return this;
    	}

    	/**
    	 * Builds this group of pipelines.
    	 * @param dev			Logical device
    	 * @param layout		Pipeline layout
    	 * @param cache			Optional pipeline cache
    	 * @return New pipelines
    	 */
    	public List<Pipeline> build(DeviceContext dev, PipelineLayout layout, PipelineCache cache) {
    		// Build descriptors and patch peer indices
    		final BiConsumer<Builder<T>, T> populate = (builder, out) -> {
    			final var flags = BitMask.reduce(builder.flags);
    			final int index = builder.parent == null ? -1 : builders.indexOf(builder.parent);
    			delegate.populate(flags, layout, builder.base, index, out);
    		};
    		final T[] array = StructureCollector.array(builders, delegate.identity(), populate);

    		// Instantiate pipelines
    		final Pointer[] handles = new Pointer[builders.size()];
    		final int result = delegate.create(dev, cache, array, handles);
    		VulkanLibrary.check(result);

    		// Create pipelines
    		final VkPipelineBindPoint type = delegate.type();
    		final Pipeline[] pipelines = new Pipeline[array.length];
    		for(int n = 0; n < array.length; ++n) {
    			final Builder<T> builder = builders.get(n);
    			final Handle handle = new Handle(handles[n]);
    			final boolean parent = builder.flags.contains(ALLOW_DERIVATIVES);
    			pipelines[n] = new Pipeline(handle, dev, type, layout, parent);
    		}
    		return Arrays.asList(pipelines);
    	}
    }

	/**
	 * Pipeline API.
	 */
	interface Library {
		/**
		 * Creates an array of graphics pipelines.
		 * @param device			Logical device
		 * @param pipelineCache		Optional pipeline cache
		 * @param createInfoCount	Number of pipelines to create
		 * @param pCreateInfos		Descriptor(s)
		 * @param pAllocator		Allocator
		 * @param pPipelines		Returned pipeline(s)
		 * @return Result
		 */
		int vkCreateGraphicsPipelines(DeviceContext device, PipelineCache pipelineCache, int createInfoCount, VkGraphicsPipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

		/**
		 * Creates an array of compute pipelines.
		 * @param device			Logical device
		 * @param pipelineCache		Optional pipeline cache
		 * @param createInfoCount	Number of pipelines to create
		 * @param pCreateInfos		Descriptor(s)
		 * @param pAllocator		Allocator
		 * @param pPipelines		Returned pipeline(s)
		 * @return Result
		 */
		int vkCreateComputePipelines(DeviceContext device, PipelineCache pipelineCache, int createInfoCount, VkComputePipelineCreateInfo[] pCreateInfos, Pointer pAllocator, Pointer[] pPipelines);

		/**
		 * Destroys a pipeline.
		 * @param device			Logical device
		 * @param pipeline			Pipeline
		 * @param pAllocator		Allocator
		 */
		void vkDestroyPipeline(DeviceContext device, Pipeline pipeline, Pointer pAllocator);

		/**
		 * Binds a pipeline to the render sequence.
		 * @param commandBuffer			Command buffer
		 * @param pipelineBindPoint		Bind-point
		 * @param pipeline				Pipeline to bind
		 */
		void vkCmdBindPipeline(Buffer commandBuffer, VkPipelineBindPoint pipelineBindPoint, Pipeline pipeline);

		/**
		 * Command to apply a pipeline barrier.
		 * @param commandBuffer						Command buffer
		 * @param srcStageMask						Source pipeline stages mask
		 * @param dstStageMask						Destination pipeline stages mask
		 * @param dependencyFlags					Dependency mask
		 * @param memoryBarrierCount				Number of memory barriers
		 * @param pMemoryBarriers					Memory barriers
		 * @param bufferMemoryBarrierCount			Number of buffer barriers
		 * @param pBufferMemoryBarriers				Buffer barriers
		 * @param imageMemoryBarrierCount			Number of image barriers
		 * @param pImageMemoryBarriers				Image barriers
		 */
		void vkCmdPipelineBarrier(Buffer commandBuffer, BitMask<VkPipelineStage> srcStageMask, BitMask<VkPipelineStage> dstStageMask, BitMask<VkDependencyFlag> dependencyFlags, int memoryBarrierCount, VkMemoryBarrier[] pMemoryBarriers, int bufferMemoryBarrierCount, VkBufferMemoryBarrier[] pBufferMemoryBarriers, int imageMemoryBarrierCount, VkImageMemoryBarrier[] pImageMemoryBarriers);
	}
}
