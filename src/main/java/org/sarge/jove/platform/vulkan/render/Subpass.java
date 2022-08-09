package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.*;

import java.util.*;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.tuple.Pair;
import org.sarge.jove.platform.vulkan.*;
import org.sarge.jove.util.*;
import org.sarge.lib.util.*;

/**
 * A <i>sub-pass</i> specifies a stage of the render-pass.
 * <p>
 * Each sub-pass consists of:
 * <ul>
 * <li>the attachments used in the sub-pass</li>
 * <li>image layout transitions</li>
 * <li>dependencies on previous sub-passes in the render-pass</li>
 * </ul>
 * <p>
 * A sub-pass can also be one of the following special cases:
 * <ul>
 * <li>{@link #EXTERNAL} for the implicit sub-pass before or after the render-pass</li>
 * <li>{@link #SELF} for a self-referential sub-pass</li>
 * </ul>
 * <p>
 * Usage:
 * <p>
 * <pre>
 * // Create a sub-pass
 * Reference col = new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
 * Reference depth = new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL);
 * Subpass subpass = new Subpass(List.of(col), depth);
 *
 * // Add a dependency to the sub-pass
 * subpass
 *     .dependency()
 * 	       .subpass(Subpass.EXTERNAL)
 * 	       .source()
 * 	           .stage(VkPipelineStage.FRAGMENT_SHADER)
 * 	           .access(VkAccess.SHADER_READ)
 * 	           .build()
 * 	       .destination()
 * 	           .stage(VkPipelineStage.FRAGMENT_SHADER)
 * 	           .build()
 * 	       .build()
 * </pre>
 * <p>
 * @author Sarge
 */
public class Subpass {
	/**
	 * Implicit sub-pass before or after the render-pass.
	 */
	public static final Subpass EXTERNAL = new Subpass() {
		@Override
		public String toString() {
			return "EXTERNAL";
		}
	};

	/**
	 * Special-case for a self-referential sub-pass dependency.
	 * @see <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#synchronization-pipeline-barriers-subpass-self-dependencies">self-dependency</a>
	 */
	public static final Subpass SELF = new Subpass() {
		@Override
		public String toString() {
			return "SELF";
		}
	};

	/**
	 * Convenience factory for a sub-pass with a single {@link VkImageLayout#COLOR_ATTACHMENT_OPTIMAL} attachment.
	 * @param colour Colour attachment
	 * @return New default sub-pass
	 */
	public static Subpass of(Attachment colour) {
		final Reference ref = new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL);
		return new Subpass(List.of(ref), null);
	}

	private final List<Reference> colour;
	private final Reference depth;
	private final List<Dependency> dependencies = new ArrayList<>();
	private Integer index;

	/**
	 * Constructor.
	 * @param colour			Colour attachments
	 * @param depth				Optional depth-stencil attachment
	 * @param dependencies		Sub-pass dependencies
	 * @throws IllegalArgumentException if the sub-pass is empty or contains a duplicate attachment
	 */
	public Subpass(List<Reference> colour, Reference depth) {
		this.colour = List.copyOf(colour);
		this.depth = depth;
		verify();
	}

	private void verify() {
		if(colour.isEmpty() && (depth == null)) {
			throw new IllegalArgumentException("No attachments specified");
		}

		if(!Utility.distinct(colour)) {
			throw new IllegalArgumentException("Colour attachments cannot contain duplicates");
		}

		if((depth != null) && colour.contains(depth)) {
			throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
		}
	}

	private Subpass() {
		this.colour = List.of();
		this.depth = null;
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
	 * Adds a dependency to this sub-pass.
	 * @return New dependency builder
	 */
	public Dependency.Builder dependency() {
		return new Dependency.Builder(this);
	}

	/**
	 * Initialises the index of this sub-pass.
	 */
	void index(int index) {
//		assert this.index == null;
		this.index = zeroOrMore(index);
	}

	/**
	 * Populates a descriptor for this sub-pass.
	 * @param subpass			Sub-pass
	 * @param descriptor		Descriptor to populate
	 */
	public void populate(VkSubpassDescription descriptor) {
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

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("colour", colour)
				.append("depth", depth)
				.append("dependencies", dependencies)
				.build();
	}

	/**
	 * A <i>reference</i> specifies an attachment used during this sub-pass.
	 */
	public static class Reference {
		private final Attachment attachment;
		private final VkImageLayout layout;
		private Integer ref;

		/**
		 * Constructor.
		 * @param attachment		Attachment
		 * @param layout			Image layout
		 */
		public Reference(Attachment attachment, VkImageLayout layout) {
			this.attachment = notNull(attachment);
			this.layout = notNull(layout);
		}

		/**
		 * @return Attachment
		 */
		Attachment attachment() {
			return attachment;
		}

		/**
		 * Sets the attachment index.
		 */
		void index(int index) {
//			assert this.ref == null;
			this.ref = zeroOrMore(index);
		}

		/**
		 * Populates the descriptor for this attachment reference.
		 */
		void populate(VkAttachmentReference descriptor) {
			assert this.ref != null;
			descriptor.attachment = ref;
			descriptor.layout = layout;
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
	 * The <i>dependency properties</i> specifies the pipeline stage(s) and access requirements for the source or destination of a dependency.
	 */
	public record Properties(Set<VkPipelineStage> stages, Set<VkAccess> access) {
		/**
		 * Constructor.
		 * @param stages	Pipeline stages
		 * @param access	Access flags
		 * @throws IllegalArgumentException if {@link #stages} is empty
		 */
		public Properties {
			if(stages.isEmpty()) throw new IllegalArgumentException("At least one pipeline stage must be specified");
			stages = Set.copyOf(stages);
			access = Set.copyOf(access);
		}

		/**
		 * Builder for the properties of a dependency.
		 */
		public static class Builder {
			private final Dependency.Builder parent;
			private final Set<VkPipelineStage> stages = new HashSet<>();
			private final Set<VkAccess> access = new HashSet<>();

			private Builder(Dependency.Builder parent) {
				this.parent = parent;
			}

			/**
			 * Adds a pipeline stage for this dependency.
			 * @param stage Pipeline stage
			 */
			public Builder stage(VkPipelineStage stage) {
				stages.add(notNull(stage));
				return this;
			}

			/**
			 * Adds an access flag for this dependency.
			 * @param access Access flag
			 */
			public Builder access(VkAccess access) {
				this.access.add(notNull(access));
				return this;
			}

			/**
			 * Starts a new dependency on this sub-pass
			 * @return New sub-pass dependency builder
			 */
			public Dependency.Builder build() {
				return parent;
			}

			/**
			 * Creates the dependency record.
			 */
			private Properties create() {
				return new Properties(stages, access);
			}
		}
	}

	/**
	 * A <i>sub-pass dependency</i> defines a dependency on a previous sub-pass.
	 */
	public record Dependency(Pair<Subpass, Properties> source, Pair<Subpass, Properties> destination) {
		/**
		 * Index of the implicit sub-pass before or after the render pass.
		 */
		private static final int VK_SUBPASS_EXTERNAL = (~0);

		/**
		 * Constructor.
		 * @param source			Source
		 * @param destination		Destination, i.e. this sub-pass
		 */
		public Dependency {
			Check.notNull(source);
			Check.notNull(destination);
		}

		/**
		 * Populates a descriptor for this dependency.
		 */
		void populate(VkSubpassDependency dependency) {
			final int index = destination.getLeft().index;
			final Properties src = source.getRight();
			final Properties dest = destination.getRight();

			dependency.srcSubpass = source(index);
			dependency.dstSubpass = index;
			dependency.srcStageMask = IntegerEnumeration.reduce(src.stages);
			dependency.srcAccessMask = IntegerEnumeration.reduce(src.access);
			dependency.dstStageMask = IntegerEnumeration.reduce(dest.stages);
			dependency.dstAccessMask = IntegerEnumeration.reduce(dest.access);
		}

		/**
		 * Determines the index of the source sub-pass.
		 */
		private int source(int dest) {
			final Subpass src = source.getLeft();
			if(src == Subpass.EXTERNAL) {
				return VK_SUBPASS_EXTERNAL;
			}
			else
			if(src == Subpass.SELF) {
				return dest;
			}
			else {
				if(src.index == null) throw new IllegalArgumentException("Invalid source TODO");
				return src.index;
			}
		}

		/**
		 * Builder for a dependency on this sub-pass.
		 */
		public static class Builder {
			private final Subpass parent;
			private final Properties.Builder src = new Properties.Builder(this);
			private final Properties.Builder dest = new Properties.Builder(this);
			private Subpass subpass;

			private Builder(Subpass parent) {
				this.parent = parent;
			}

			/**
			 * Sets the dependant sub-pass.
			 * @param subpass Dependency
			 */
			public Builder subpass(Subpass subpass) {
				this.subpass = notNull(subpass);
				return this;
			}

			/**
			 * @return Builder for the source properties
			 */
			public Properties.Builder source() {
				return src;
			}

			/**
			 * @return Builder for the destination properties (i.e. this sub-pass)
			 */
			public Properties.Builder destination() {
				return dest;
			}

			/**
			 * Constructs this sub-pass dependency.
			 * @return Parent builder
			 */
			public Subpass build() {
				final Dependency dependency = new Dependency(Pair.of(subpass, src.create()), Pair.of(parent, dest.create()));
				parent.dependencies.add(dependency);
				return parent;
			}
		}
	}
}
