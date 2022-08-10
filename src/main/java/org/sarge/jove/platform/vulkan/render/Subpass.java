package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.Check;


/**
 * A <i>sub-pass</i> specifies a stage of a render-pass.
 * <p>
 * A subpass comprises the colour and depth-stencil attachments:
 * <p>
 * <pre>
 * RenderPass pass = new RenderPass.Builder()
 *     .subpass()
 *         .colour(col, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL)
 *         .depth(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL)
 *         .build()
 *     .build(dev);</pre>
 * Subpass dependencies can be configured between the stages of a render-pass:
 * <p>
 * <pre>
 * // Create render pass
 * var builder = new RenderPass.Builder();
 *
 * // Create a dependant subpass
 * Subpass one = builder
 *     .subpass()
 *     .colour(col);
 *
 * // Create a second subpass with a dependency
 * Subpass two = builder
 *     .subpass()
 *     .colour(col)
 *     .dependency()
 *         .dependency(one)
 *         .source()
 *             .stage(VkPipelineStage.FRAGMENT_SHADER)
 *             .access(VkAccess.SHADER_READ)
 *             .build()
 *         .destination()
 *             .stage(VkPipelineStage.FRAGMENT_SHADER)
 *             .build()
 *         .build();
 *
 * // Create render pass
 * RenderPass pass = builder.build(dev);</pre>
 * <p>
 * A subpass can be dependant on the implicit, external subpass using the {@link Dependency#external()} method.
 * <br>
 * A self-referential subpass is configured using {@link Dependency#self()}.
 * <p>
 * @author Sarge
 */
public class Subpass {
	private final int index;
	private final List<Reference> colour = new ArrayList<>();
	private Reference depth;
	private final List<Dependency> dependencies = new ArrayList<>();

	/**
	 * Constructor.
	 * @param index Subpass index
	 */
	Subpass(int index) {
		this.index = zeroOrMore(index);
	}

	/**
	 * @return Attachments used by this sub-pass
	 */
	Stream<Reference> references() {
		if(depth == null) {
			return colour.stream();
		}
		else {
			return Stream.concat(colour.stream(), Stream.of(depth));
		}
	}

	/**
	 * @return Sub-pass dependencies
	 */
	Stream<Dependency> dependencies() {
		return dependencies.stream();
	}

	/**
	 * Populates a descriptor for this sub-pass.
	 * @param subpass			Sub-pass
	 * @param descriptor		Descriptor to populate
	 */
	void populate(VkSubpassDescription descriptor) {
		// Init descriptor
		descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

		// Populate colour attachments
		descriptor.colorAttachmentCount = colour.size();
		descriptor.pColorAttachments = StructureHelper.pointer(colour, VkAttachmentReference::new, Reference::populate);

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
	record Reference(int index, Attachment attachment, VkImageLayout layout) {
		/**
		 * Constructor.
		 * @param index				Reference index
		 * @param attachment		Attachment
		 * @param layout			Image layout
		 */
		Reference {
			Check.zeroOrMore(index);
			Check.notNull(attachment);
			Check.notNull(layout);
		}

		/**
		 * Populates the descriptor for this attachment reference.
		 */
		void populate(VkAttachmentReference descriptor) {
			descriptor.attachment = index;
			descriptor.layout = layout;
		}
	}

	/**
	 * @return Next attachment reference index
	 */
	protected int allocate() {
		throw new UnsupportedOperationException();
	}

	/**
	 * Adds a colour attachment.
	 * @param colour Attachment
	 * @param layout Layout
	 * @throws IllegalArgumentException for a duplicate attachment
	 */
	public Subpass colour(Attachment colour, VkImageLayout layout) {
		if(contains(colour)) throw new IllegalArgumentException("Colour attachment must be unique: " + colour);
		this.colour.add(new Reference(allocate(), colour, layout));
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
		this.depth = new Reference(allocate(), depth, layout);
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
	void verify() {
		if(colour.isEmpty() && (depth == null)) {
			throw new IllegalArgumentException("No attachments specified");
		}
		if((depth != null) && contains(depth.attachment)) {
			throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
		}
	}

	/**
	 * Constructs this subpass.
	 */
	public RenderPass.Builder build() {
		verify();
		return null;
	}

	/**
	 * Builder for a dependency on this subpass.
	 */
	public class Dependency {
		/**
		 * Index of the implicit external subpass.
		 */
		private static final int VK_SUBPASS_EXTERNAL = (~0);

		@SuppressWarnings("hiding")
		private Integer index;
		private final Properties src = new Properties(this);
		private final Properties dest = new Properties(this);

		/**
		 * Sets the dependant subpass.
		 * @param subpass Dependant subpass
		 */
		public Dependency dependency(Subpass subpass) {
			index = subpass.index;
			return this;
		}

		/**
		 * Sets this as a dependency on the implicit external subpass before or after this subpass.
		 */
		public Dependency external() {
			index = VK_SUBPASS_EXTERNAL;
			return this;
		}

		/**
		 * Sets this as a self-referential dependency.
		 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#synchronization-pipeline-barriers-subpass-self-dependencies">self-dependency</a>
		 */
		public Dependency self() {
			this.index = Subpass.this.index;
			return this;
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
			if(index == null) throw new IllegalArgumentException("Dependant subpass has not been configured");
			if(src.stages.isEmpty()) throw new IllegalArgumentException("Source stages cannot be empty");
			if(dest.stages.isEmpty()) throw new IllegalArgumentException("Destination stages cannot be empty");
			dependencies.add(this);
			return Subpass.this;
		}

		/**
		 * Populates the descriptor for this dependency.
		 * @param info Dependency descriptor
		 */
		void populate(VkSubpassDependency info) {
			info.srcSubpass = index;
			info.dstSubpass = Subpass.this.index;
			info.srcStageMask = IntegerEnumeration.reduce(src.stages);
			info.srcAccessMask = IntegerEnumeration.reduce(src.access);
			info.dstStageMask = IntegerEnumeration.reduce(dest.stages);
			info.dstAccessMask = IntegerEnumeration.reduce(dest.access);
		}
	}

	/**
	 * Builder for the source or destination properties of this dependency.
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
