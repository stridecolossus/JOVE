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
 * Subpass subpass = new Subpass.Builder()
 *     .colour(new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
 *     .depth(new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL))
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
 * 	    .build();
 * </pre>
 * <p>
 * The {@link Group} is a helper class used to populate descriptors for a group of sub-passes comprising the render pass.
 * <p>
 * @author Sarge
 */
public class Subpass {
	/**
	 * Index of the implicit sub-pass before or after the render pass.
	 */
	private static final int VK_SUBPASS_EXTERNAL = (~0);

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

	private final List<Reference> colour;
	private final Optional<Reference> depth;
	private final List<Dependency> dependencies;

	/**
	 * Constructor.
	 * @param colour			Colour attachments
	 * @param depth				Depth-stencil attachment
	 * @param dependencies		Sub-pass dependencies
	 * @throws IllegalArgumentException if the sub-pass is empty or contains a duplicate attachment
	 */
	public Subpass(List<Reference> colour, Reference depth, List<Dependency> dependencies) {
		this.colour = List.copyOf(colour);
		this.depth = Optional.ofNullable(depth);
		this.dependencies = List.copyOf(dependencies);
		verify();
	}

	private void verify() {
		if(colour.isEmpty() && depth.isEmpty()) {
			throw new IllegalArgumentException("No attachments specified");
		}

		if(!Utility.distinct(colour)) {
			throw new IllegalArgumentException("Colour attachments cannot contain duplicates");
		}

		if(depth.filter(colour::contains).isPresent()) {
			throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
		}
	}

	private Subpass() {
		this.colour = List.of();
		this.depth = Optional.empty();
		this.dependencies = List.of();
	}

	/**
	 * @return Colour attachment(s)
	 */
	public List<Reference> colour() {
		return colour;
	}

	/**
	 * @return Depth-stencil attachment
	 */
	public Optional<Reference> depth() {
		return depth;
	}

	/**
	 * @return Sub-pass dependencies
	 */
	public Stream<Dependency> dependencies() {
		return dependencies.stream();
	}

	/**
	 * @return Attachments used by this sub-pass
	 */
	public List<Reference> attachments() {
		final List<Reference> attachments = new ArrayList<>(colour);
		depth.ifPresent(attachments::add);
		return attachments;
	}

	@Override
	public boolean equals(Object obj) {
		return
				(obj == this) ||
				(obj instanceof Subpass that) &&
				this.colour.equals(that.colour) &&
				this.depth.equals(that.depth) &&
				this.dependencies.equals(that.dependencies);
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
	public record Reference(Attachment attachment, VkImageLayout layout) {
		/**
		 * Constructor.
		 * @param attachment		Attachment
		 * @param layout			Image layout
		 */
		public Reference {
			Check.notNull(attachment);
			Check.notNull(layout);
		}

		/**
		 * Populates the descriptor for this attachment reference.
		 */
		void populate(int index, VkAttachmentReference descriptor) {
			descriptor.attachment = zeroOrMore(index);
			descriptor.layout = layout;
		}
	}

	/**
	 * Builder for a sub-pass.
	 */
	public static class Builder {
		private final List<Dependency> dependencies = new ArrayList<>();
		private final List<Reference> colour = new ArrayList<>();
		private Reference depth;

		/**
		 * Adds a colour attachment.
		 * @param colour Colour attachment reference
		 */
		public Builder colour(Reference colour) {
			this.colour.add(colour);
			return this;
		}

		/**
		 * Adds the depth-stencil attachment.
		 * @param depth Depth-stencil attachment reference
		 * @throws IllegalArgumentException if the depth-stencil attachment has already been added
		 */
		public Builder depth(Reference depth) {
			if(this.depth != null) throw new IllegalArgumentException("Depth-stencil attachment already specified");
			this.depth = notNull(depth);
			return this;
		}

		/**
		 * @return New builder for a dependency on this sub-pass
		 */
		public Dependency.Builder dependency() {
			return new Dependency.Builder(this);
		}

		/**
		 * Constructs this sub-pass.
		 * @return New sub-pass
		 */
		public Subpass build() {
			return new Subpass(colour, depth, dependencies);
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
	public record Dependency(Subpass subpass, Properties source, Properties destination) {
		/**
		 * Constructor.
		 * @param subpass			Source sub-pass
		 * @param source			Source dependency properties
		 * @param destination		Destination dependency properties (i.e. this sub-pass)
		 */
		public Dependency {
			Check.notNull(subpass);
			Check.notNull(source);
			Check.notNull(destination);
		}

		/**
		 * Populates a sub-pass dependency descriptor.
		 * @param src				Source sub-pass index
		 * @param dest				Destination index
		 * @param dependency		Descriptor to populate
		 */
		void populate(int src, int dest, VkSubpassDependency dependency) {
			dependency.srcSubpass = src;
			dependency.dstSubpass = zeroOrMore(dest);
			dependency.srcStageMask = IntegerEnumeration.reduce(source.stages);
			dependency.srcAccessMask = IntegerEnumeration.reduce(source.access);
			dependency.dstStageMask = IntegerEnumeration.reduce(destination.stages);
			dependency.dstAccessMask = IntegerEnumeration.reduce(destination.access);
		}

		/**
		 * Builder for a dependency on this sub-pass.
		 */
		public static class Builder {
			private final Subpass.Builder parent;
			private final Properties.Builder src = new Properties.Builder(this);
			private final Properties.Builder dest = new Properties.Builder(this);
			private Subpass subpass;

			private Builder(Subpass.Builder parent) {
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
			public Subpass.Builder build() {
				final Dependency dependency = new Dependency(subpass, src.create(), dest.create());
				parent.dependencies.add(dependency);
				return parent;
			}
		}
	}

	/**
	 * A <i>sub-pass group</i> is a helper class used to generate Vulkan descriptors for a render pass.
	 * <p>
	 * The render pass is represented as an object graph of the various sub-pass domain objects, whereas the resultant Vulkan descriptors use indices to represent the object relationships.
	 * This helper class attempts to mitigate this complexity by encapsulating index mapping whilst co-locating the population functions with the relevant type where possible.
	 * <p>
	 */
	static class Group {
		private final List<Subpass> subpasses;
		private final List<Reference> references;

		/**
		 * Constructor.
		 * @param subpasses Sub-passes in this group
		 */
		public Group(List<Subpass> subpasses) {
			this.subpasses = List.copyOf(notEmpty(subpasses));
			this.references = references(subpasses);
		}

		/**
		 * @return Unique attachment references in this group
		 */
		private static List<Reference> references(List<Subpass> subpasses) {
			return subpasses
					.stream()
					.map(Subpass::attachments)
					.flatMap(List::stream)
					.distinct()
					.toList();
		}

		/**
		 * @return Total attachments in this group
		 */
		public List<Attachment> attachments() {
			return references
					.stream()
					.map(Reference::attachment)
					.toList();
		}

		/**
		 * Populates a sub-pass descriptor.
		 * @param subpass			Sub-pass
		 * @param descriptor		Descriptor to populate
		 */
		public void populate(Subpass subpass, VkSubpassDescription descriptor) {
			// Init descriptor
			descriptor.pipelineBindPoint = VkPipelineBindPoint.GRAPHICS;

			// Populate colour attachments
			descriptor.colorAttachmentCount = subpass.colour.size();
			descriptor.pColorAttachments = StructureHelper.pointer(subpass.colour, VkAttachmentReference::new, this::populate);

			// Populate depth attachment
			descriptor.pDepthStencilAttachment = subpass.depth.map(this::depth).orElse(null);
		}

		/**
		 * Populates the descriptor for an attachment reference.
		 */
		private void populate(Reference ref, VkAttachmentReference descriptor) {
			final int index = references.indexOf(ref);
			ref.populate(index, descriptor);
		}

		/**
		 * Creates and populates a descriptor for the depth-stencil attachment.
		 */
		private VkAttachmentReference depth(Reference ref) {
			final var descriptor = new VkAttachmentReference();
			populate(ref, descriptor);
			return descriptor;
		}

		/**
		 * @return Sub-pass dependencies zipped with the <b>destination</b> sub-pass
		 */
		public List<Pair<Subpass, Dependency>> dependencies() {
			return subpasses
					.stream()
					.flatMap(subpass -> subpass.dependencies.stream().map(e -> Pair.of(subpass, e)))
					.toList();
		}

		/**
		 * Populates the descriptor for a sub-pass dependency.
		 * @throws IllegalArgumentException if the sub-pass is not present
		 */
		public void populate(Pair<Subpass, Dependency> entry, VkSubpassDependency descriptor) {
			// Lookup index of this sub-pass
			final Subpass subpass = entry.getLeft();
			final Dependency dependency = entry.getRight();
			final int dest = subpasses.indexOf(subpass);
			assert dest >= 0;

			// Determine index of the source sub-pass
			final int src = index(dependency.subpass, dest);

			// Populate descriptor
			dependency.populate(src, dest, descriptor);
		}

		/**
		 * Determines the index of the source sub-pass.
		 * @param src		Source sub-pass
		 * @param dest		Index of the destination (i.e. this) sub-pass (for the {@link Subpass#SELF} case)
		 * @return Source sub-pass index
		 * @throws IllegalArgumentException if the sub-pass is not present
		 */
		private int index(Subpass src, int dest) {
			if(src == Subpass.EXTERNAL) {
				return VK_SUBPASS_EXTERNAL;
			}
			else
			if(src == Subpass.SELF) {
				return dest;
			}
			else {
				final int index = subpasses.indexOf(src);
				if(index == -1) throw new IllegalArgumentException("Invalid subpass for this render pass: " + src);
				return index;
			}
		}
	}
}
