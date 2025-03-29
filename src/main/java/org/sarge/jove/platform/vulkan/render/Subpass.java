package org.sarge.jove.platform.vulkan.render;

import static java.util.Objects.requireNonNull;
import static org.sarge.lib.Validation.requireZeroOrMore;

import java.util.*;
import java.util.stream.Stream;

import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.platform.vulkan.common.DeviceContext;
import org.sarge.jove.util.EnumMask;

/**
 * A <i>sub-pass</i> is a transient, mutable specification for the stage of a {@link RenderPass}.
 * <p>
 * Attachments used during this subpass are added by the {@link #colour(Reference)} or {@link #depth(Reference)} methods.
 * <p>
 * Subpass dependencies are configured by the {@link #dependency()} factory method.
 * <p>
 * @author Sarge
 */
public class Subpass {
	private final List<Dependency> dependencies = new ArrayList<>();
	private final List<Reference> colour = new ArrayList<>();
	private Reference depth;
	private Integer index;

	/**
	 * Patches the index of this subpass.
	 * @param index Subpass index
	 */
	void init(int index) {
		assert this.index == null;
		this.index = requireZeroOrMore(index);
	}

	/**
	 * @return Attachments used by this subpass
	 * @throws IllegalArgumentException if this subpass is empty
	 */
	Stream<Reference> attachments() {
		if(depth == null) {
			if(colour.isEmpty()) throw new IllegalArgumentException("No attachments specified");
			return colour.stream();
		}
		else {
			if(contains(depth.attachment)) throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
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
	 * Populates the descriptor for this subpass.
	 */
	void populate(VkSubpassDescription descriptor) {
		// Init descriptor
		descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

		// Populate colour attachments
		descriptor.colorAttachmentCount = colour.size();
		descriptor.pColorAttachments = null; // StructureCollector.pointer(colour, new VkAttachmentReference(), Reference::populate);

		// Populate depth attachment
		if(depth != null) {
			final var ref = new VkAttachmentReference();
			depth.populate(ref);
			descriptor.pDepthStencilAttachment = ref;
		}
	}

	/**
	 * Convenience factory to create a render pass comprised of just this subpass.
	 * @param dev Logical device
	 * @return Render pass
	 */
	public RenderPass create(DeviceContext dev) {
		return RenderPass.create(dev, List.of(this));
	}

	/**
	 * A <i>reference</i> specifies an attachment used by this subpass.
	 */
	public static class Reference {
		private final Attachment attachment;
		private final VkImageLayout layout;
		private Integer num;

		/**
		 * Constructor.
		 * @param attachment		Attachment
		 * @param layout			Image layout
		 */
		public Reference(Attachment attachment, VkImageLayout layout) {
			this.attachment = requireNonNull(attachment);
			this.layout = requireNonNull(layout);
		}

		/**
		 * @return Attachment
		 */
		public Attachment attachment() {
			return attachment;
		}

		/**
		 * Patches the attachment index for this reference.
		 * @param index Attachment index
		 */
		void init(int index) {
			assert num == null;
			num = requireZeroOrMore(index);
		}

		/**
		 * Populates the descriptor for this attachment reference.
		 */
		void populate(VkAttachmentReference ref) {
			ref.attachment = num;
			ref.layout = layout;
		}

		@Override
		public int hashCode() {
			return Objects.hash(attachment, layout);
		}

		@Override
		public boolean equals(Object obj) {
			return
					(obj == this) ||
					(obj instanceof Reference that) &&
					this.attachment.equals(that.attachment) &&
					(this.layout == that.layout);
		}
	}

	/**
	 * Adds a colour attachment.
	 * @param colour Colour attachment
	 * @param layout Layout
	 * @throws IllegalArgumentException for a duplicate colour attachment
	 */
	public Subpass colour(Reference ref) {
		if(contains(ref.attachment)) throw new IllegalArgumentException("Colour attachment must be unique: " + colour);
		this.colour.add(ref);
		return this;
	}

	/**
	 * Convenience method to add a colour attachment with a {@link VkImageLayout#COLOR_ATTACHMENT_OPTIMAL} layout.
	 * @see #colour(Reference)
	 */
	public Subpass colour(Attachment colour) {
		return colour(new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL));
	}

	/**
	 * Sets the depth-stencil attachment.
	 * @param ref Attachment reference
	 * @throws IllegalArgumentException if the depth-stencil has already been specified
	 */
	public Subpass depth(Reference ref) {
		if(this.depth != null) throw new IllegalArgumentException("Depth-stencil attachment has already been specified");
		this.depth = requireNonNull(ref);
		return this;
	}

	/**
	 * Starts a new subpass dependency builder.
	 * @return New subpass dependency builder
	 */
	public Dependency dependency() {
		final var dependency = new Dependency();
		dependencies.add(dependency);
		return dependency;
	}

	/**
	 * @return Whether the given attachment is used by this subpass
	 */
	private boolean contains(Attachment attachment) {
		return colour.stream().map(Reference::attachment).anyMatch(attachment::equals);
	}

	@Override
	public boolean equals(Object obj) {
		return obj == this;
	}

	/**
	 * A <i>subpass dependency</i> configures this subpass to be dependant on a previous stage of the render pass.
	 * <p>
	 * A subpass can be dependant on the <i>implicit</i> subpass before or after the render pass using the {@link #external()} method.
	 * <br>
	 * A subpass can also be self referential.
	 * See <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#synchronization-pipeline-barriers-subpass-self-dependencies">self-dependency</a>.
	 * <p>
	 * Note that a subpass dependency <b>must</b> specify the {@link #source()} and {@link #destination()} pipeline stages of the dependency.
	 * <p>
	 */
	public class Dependency {
		/**
		 * Implicit subpass before or after the render pass.
		 */
		private static final Subpass VK_SUBPASS_EXTERNAL = new Subpass();

		static {
			VK_SUBPASS_EXTERNAL.index = ~0;
		}

		private Subpass dependency;
		private final Set<VkDependencyFlag> flags = new HashSet<>();
		private final Properties src = new Properties();
		private final Properties dest = new Properties();

		/**
		 * Sets the dependant subpass.
		 * @param dependency Dependant subpass
		 */
		public Dependency subpass(Subpass dependency) {
			this.dependency = requireNonNull(dependency);
			return this;
		}

		/**
		 * Sets the dependant subpass.
		 * @param dependency Dependant subpass
		 */
		public Dependency external() {
			this.dependency = VK_SUBPASS_EXTERNAL;
			return this;
		}

		/**
		 * Adds a dependency flag.
		 * @param flag Dependency flag
		 */
		public Dependency flag(VkDependencyFlag flag) {
			requireNonNull(flag);
			flags.add(flag);
			return this;
		}

		/**
		 * Builder for the source or destination properties of this dependency.
		 */
		public class Properties {
			private final Set<VkPipelineStage> stages = new HashSet<>();
			private final Set<VkAccess> access = new HashSet<>();

			/**
			 * Adds a pipeline stage to this dependency.
			 * @param stage Pipeline stage
			 */
			public Properties stage(VkPipelineStage stage) {
				requireNonNull(stage);
				this.stages.add(stage);
				return this;
			}

			/**
			 * Adds an access flag to this dependency.
			 * @param access Access flag
			 */
			public Properties access(VkAccess access) {
				requireNonNull(access);
				this.access.add(access);
				return this;
			}

			/**
			 * Constructs these dependency properties.
			 * @throws IllegalArgumentException if the pipeline stages are empty
			 */
			public Dependency build() {
				if(stages.isEmpty()) throw new IllegalArgumentException("Pipeline stages cannot be empty");
				return Dependency.this;
			}
		}

		/**
		 * @return Source properties
		 */
		public Properties source() {
			return src;
		}

		/**
		 * @return Destination properties, i.e. for this subpass
		 */
		public Properties destination() {
			return dest;
		}

		/**
		 * Constructs this dependency.
		 * @return Subpass
		 * @throws IllegalArgumentException if the dependant subpass has not been configured
		 * @throws IllegalArgumentException if the source or destination properties are empty
		 */
		public Subpass build() {
			if(dependency == null) throw new IllegalArgumentException("Dependant subpass must be configured");
			src.build();
			dest.build();
			return Subpass.this;
		}

		/**
		 * Populates the descriptor for this dependency.
		 * @throws IllegalArgumentException if the dependant subpass is not included in the render pass
		 */
		void populate(VkSubpassDependency info) {
			if(dependency.index == null) throw new IllegalArgumentException("Missing dependant subpass: " + dependency);
			info.dependencyFlags = new EnumMask<>(flags);
			info.srcSubpass = dependency.index;
			info.dstSubpass = Subpass.this.index;
			info.srcStageMask = new EnumMask<>(src.stages);
			info.srcAccessMask = new EnumMask<>(src.access);
			info.dstStageMask = new EnumMask<>(dest.stages);
			info.dstAccessMask = new EnumMask<>(dest.access);
		}
	}
}
