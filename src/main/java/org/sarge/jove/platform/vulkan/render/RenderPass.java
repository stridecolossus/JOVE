package org.sarge.jove.platform.vulkan.render;

import static org.sarge.jove.platform.vulkan.core.VulkanLibrary.check;
import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.common.Handle;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.*;
import org.sarge.jove.platform.vulkan.core.Command.Buffer;
import org.sarge.jove.platform.vulkan.core.VulkanLibrary;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.Subpass;
import org.sarge.jove.platform.vulkan.render.RenderPass.Builder.Subpass.Dependency;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;

import com.sun.jna.Pointer;
import com.sun.jna.ptr.PointerByReference;

/**
 * A <i>render pass</i> is comprised of a number of sub-passes.
 * @see Subpass
 * @author Sarge
 */
public class RenderPass extends AbstractVulkanObject {
	private final List<Attachment> attachments;

	/**
	 * Constructor.
	 * @param handle			Render pass handle
	 * @param dev				Logical device
	 * @param attachments		Attachments
	 */
	RenderPass(Handle handle, DeviceContext dev, List<Attachment> attachments) {
		super(handle, dev);
		this.attachments = List.copyOf(attachments);
	}

	/**
	 * @return Attachments used in this render-pass
	 */
	public List<Attachment> attachments() {
		return attachments;
	}

	@Override
	protected Destructor<RenderPass> destructor(VulkanLibrary lib) {
		return lib::vkDestroyRenderPass;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.appendSuper(super.toString())
				.append(attachments)
				.build();
	}

	/**
	 * Builder for a render-pass.
	 * <p>
	 * Example:
	 * <pre>
	 * RenderPass pass = new RenderPass.Builder()
	 *     .subpass()
	 *         .colour(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
	 *         .depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
	 *         .build()
	 *     .build(dev);
	 * </pre>
	 */
	public static class Builder {
		private final List<Subpass> subpasses = new ArrayList<>();
		private final List<Attachment> attachments = new ArrayList<>();

		/**
		 * @return New subpass
		 */
		public Subpass subpass() {
			final Subpass subpass = new Subpass(subpasses.size());
			subpasses.add(subpass);
			return subpass;
		}

		/**
		 * Constructs this render pass.
		 * @return New render pass
		 * @throws IllegalArgumentException if the list of sub-passes is empty or a dependency refers to a missing subpass
		 */
		public RenderPass build(DeviceContext dev) {
			// Validate
			if(subpasses.isEmpty()) throw new IllegalArgumentException("At least one subpass must be specified");
			subpasses.forEach(Subpass::verify);

			// Init render pass descriptor
			final var info = new VkRenderPassCreateInfo();

			// Add attachments
			info.attachmentCount = attachments.size();
			info.pAttachments = StructureCollector.pointer(attachments, new VkAttachmentDescription(), Attachment::populate);

			// Add sub-passes
			info.subpassCount = subpasses.size();
			info.pSubpasses = StructureCollector.pointer(subpasses, new VkSubpassDescription(), Subpass::populate);

			// Add dependencies
			final List<Dependency> dependencies = subpasses.stream().flatMap(Subpass::dependencies).toList();
			info.dependencyCount = dependencies.size();
			info.pDependencies = StructureCollector.pointer(dependencies, new VkSubpassDependency(), Dependency::populate);

			// Allocate render pass
			final VulkanLibrary lib = dev.library();
			final PointerByReference ref = dev.factory().pointer();
			check(lib.vkCreateRenderPass(dev, info, null, ref));

			// Create render pass
			return new RenderPass(new Handle(ref), dev, attachments);
		}

		/**
		 * A <i>sub-pass</i> specifies a stage of a render-pass.
		 */
		public class Subpass {
			private final int index;
			private final List<Dependency> dependencies = new ArrayList<>();
			private final List<Reference> colour = new ArrayList<>();
			private Reference depth;

			/**
			 * Constructor.
			 * @param index Subpass index
			 */
			private Subpass(int index) {
				this.index = zeroOrMore(index);
			}

			/**
			 * @return Sub-pass dependencies
			 */
			private Stream<Dependency> dependencies() {
				return dependencies.stream();
			}

			/**
			 * Populates the descriptor for this sub-pass.
			 */
			private void populate(VkSubpassDescription descriptor) {
				// Init descriptor
				descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

				// Populate colour attachments
				descriptor.colorAttachmentCount = colour.size();
				descriptor.pColorAttachments = StructureCollector.pointer(colour, new VkAttachmentReference(), Reference::populate);

				// Populate depth attachment
				if(depth != null) {
					final var ref = new VkAttachmentReference();
					depth.populate(ref);
					descriptor.pDepthStencilAttachment = ref;
				}
			}

			/**
			 * A <i>reference</i> specifies an attachment used by this subpass.
			 */
			private record Reference(int index, Attachment attachment, VkImageLayout layout) {
				/**
				 * Constructor.
				 * @param index				Reference index
				 * @param attachment		Attachment
				 * @param layout			Image layout
				 */
				private Reference {
					Check.zeroOrMore(index);
					Check.notNull(attachment);
					Check.notNull(layout);
				}

				/**
				 * Populates the descriptor for this attachment reference.
				 */
				private void populate(VkAttachmentReference descriptor) {
					descriptor.attachment = index;
					descriptor.layout = layout;
				}
			}

			/**
			 * Allocates an attachment reference.
			 */
			private Reference reference(Attachment attachment, VkImageLayout layout) {
				final int prev = attachments.indexOf(attachment);
				final int index;
				if(prev == -1) {
					index = attachments.size();
					attachments.add(attachment);
				}
				else {
					index = prev;
				}
				return new Reference(index, attachment, layout);
			}

			/**
			 * Adds a colour attachment.
			 * @param colour Attachment
			 * @param layout Layout
			 * @throws IllegalArgumentException for a duplicate attachment
			 */
			public Subpass colour(Attachment colour, VkImageLayout layout) {
				if(contains(colour)) throw new IllegalArgumentException("Colour attachment must be unique: " + colour);
				this.colour.add(reference(colour, layout));
				return this;
			}

			/**
			 * Convenience method to add a colour attachment with a {@link VkImageLayout#COLOR_ATTACHMENT_OPTIMAL} layout.
			 * @param colour Colour attachment
			 * @throws IllegalArgumentException for a duplicate attachment
			 */
			public Subpass colour(Attachment colour) {
				return colour(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
			}

			/**
			 * Sets the depth-stencil attachment.
			 * @param depth		Attachment
			 * @param layout	Layout
			 * @throws IllegalArgumentException if the depth-stencil has already been specified
			 */
			public Subpass depth(Attachment depth, VkImageLayout layout) {
				if(this.depth != null) throw new IllegalArgumentException("Depth-stencil attachment has already been specified");
				this.depth = reference(depth, layout);
				return this;
			}

			/**
			 * Starts a new subpass dependency.
			 * @return New subpass dependency builder
			 */
			public Dependency dependency() {
				return new Dependency();
			}

			/**
			 * @return Whether the given attachment is used by this subpass
			 */
			private boolean contains(Attachment attachment) {
				return colour.stream().map(Reference::attachment).anyMatch(attachment::equals);
			}

			/**
			 * @throws IllegalArgumentException for an empty subpass or duplicate attachment
			 */
			private void verify() {
				if(colour.isEmpty() && (depth == null)) {
					throw new IllegalArgumentException("No attachments specified");
				}
				if((depth != null) && contains(depth.attachment)) {
					throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
				}
			}

			/**
			 * Constructs this subpass.
			 * @return Parent render-pass builder
			 */
			public RenderPass.Builder build() {
				verify();
				return RenderPass.Builder.this;
			}

			/**
			 * A <i>subpass dependency</i> configures this subpass to be dependant on a previous stage of the render-pass.
			 * <p>
			 * A subpass can be dependant on the implicit, external subpass using the {@link Dependency#external()} method.
			 * <br>
			 * A self-referential subpass is configured using {@link Dependency#self()}.
			 * <p>
			 */
			public class Dependency {
				/**
				 * Index of the implicit external subpass.
				 */
				private static final int VK_SUBPASS_EXTERNAL = (~0);

				private Integer subpass;
				private final Properties src = new Properties(this);
				private final Properties dest = new Properties(this);

				private Dependency() {
				}

				/**
				 * Sets the dependant subpass.
				 * @param subpass Dependant subpass
				 */
				public Dependency dependency(Subpass subpass) {
					this.subpass = subpass.index;
					return this;
				}

				/**
				 * Sets this as a dependency on the implicit external subpass before or after the render-pass.
				 */
				public Dependency external() {
					this.subpass = VK_SUBPASS_EXTERNAL;
					return this;
				}

				/**
				 * Sets this as a self-referential dependency.
				 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#synchronization-pipeline-barriers-subpass-self-dependencies">self-dependency</a>
				 */
				public Dependency self() {
					return dependency(Subpass.this);
				}

				/**
				 * @return Source properties
				 */
				public Properties source() {
					return src;
				}

				/**
				 * @return Destination properties, i.e. this subpass
				 */
				public Properties destination() {
					return dest;
				}

				/**
				 * Constructs this dependency.
				 * @throws IllegalArgumentException if the dependant subpass has not been specified
				 * @throws IllegalArgumentException if the source or destination pipeline stages is empty
				 */
				public Subpass build() {
					if(subpass == null) throw new IllegalArgumentException("Dependant subpass has not been configured");
					if(src.stages.isEmpty()) throw new IllegalArgumentException("Source stages cannot be empty");
					if(dest.stages.isEmpty()) throw new IllegalArgumentException("Destination stages cannot be empty");
					dependencies.add(this);
					return Subpass.this;
				}

				/**
				 * Populates the descriptor for this dependency.
				 */
				private void populate(VkSubpassDependency info) {
					info.srcSubpass = index;
					info.dstSubpass = Subpass.this.index;
					info.srcStageMask = IntegerEnumeration.reduce(src.stages);
					info.srcAccessMask = IntegerEnumeration.reduce(src.access);
					info.dstStageMask = IntegerEnumeration.reduce(dest.stages);
					info.dstAccessMask = IntegerEnumeration.reduce(dest.access);
				}
			}

			/**
			 * Source or destination properties of this dependency.
			 */
			public class Properties {
				private final Dependency dependency;
				private final Set<VkPipelineStage> stages = new HashSet<>();
				private final Set<VkAccess> access = new HashSet<>();

				private Properties(Dependency dependency) {
					this.dependency = dependency;
				}

				/**
				 * Adds a pipeline stage to this dependency.
				 * @param stage Pipeline stage
				 */
				public Properties stage(VkPipelineStage stage) {
					this.stages.add(notNull(stage));
					return this;
				}

				/**
				 * Adds an access flag to this dependency.
				 * @param access Access flag
				 */
				public Properties access(VkAccess access) {
					this.access.add(notNull(access));
					return this;
				}

				/**
				 * Constructs this set of dependency properties.
				 */
				public Dependency build() {
					return dependency;
				}
			}
		}
	}

	/**
	 * Render pass API.
	 */
	interface Library {
		/**
		 * Creates a render pass.
		 * @param device			Logical device
		 * @param pCreateInfo		Descriptor
		 * @param pAllocator		Allocator
		 * @param pRenderPass		Returned render pass
		 * @return Result
		 */
		int vkCreateRenderPass(DeviceContext device, VkRenderPassCreateInfo pCreateInfo, Pointer pAllocator, PointerByReference pRenderPass);

		/**
		 * Destroys a render pass.
		 * @param device			Logical device
		 * @param renderPass		Render pass
		 * @param pAllocator		Allocator
		 */
		void vkDestroyRenderPass(DeviceContext device, RenderPass renderPass, Pointer pAllocator);

		/**
		 * Begins a render pass.
		 * @param commandBuffer			Command buffer
		 * @param pRenderPassBegin		Descriptor
		 * @param contents				Sub-pass contents
		 */
		void vkCmdBeginRenderPass(Buffer commandBuffer, VkRenderPassBeginInfo pRenderPassBegin, VkSubpassContents contents);

		/**
		 * Ends a render pass.
		 * @param commandBuffer Command buffer
		 */
		void vkCmdEndRenderPass(Buffer commandBuffer);

		/**
		 * Starts the next sub-pass.
		 * @param commandBuffer			Command buffer
		 * @param contents				Sub-pass contents
		 */
		void vkCmdNextSubpass(Buffer commandBuffer, VkSubpassContents contents);
	}
}
