package org.sarge.jove.platform.vulkan.render;

import static org.sarge.lib.util.Check.notNull;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.sarge.jove.platform.vulkan.VkAccess;
import org.sarge.jove.platform.vulkan.VkImageLayout;
import org.sarge.jove.platform.vulkan.VkPipelineStage;
import org.sarge.jove.platform.vulkan.render.Subpass.SubpassDependency.Dependency;
import org.sarge.lib.util.Check;

/**
 * A <i>sub-pass</i> specifies a stage of the render-pass.
 * <p>
 * Each sub-pass consists of:
 * <ul>
 * <li>the attachments used in the sub-pass</li>
 * <li>image layout transitions</li>
 * <li>dependencies on other sub-passes in the render-pass</li>
 * </ul>
 * <p>
 * The {@link #EXTERNAL} sub-pass is a special case instance for the implicit sub-pass before or after the render-pass.
 * <p>
 * Usage:
 * <p>
 * <pre>
 * 	Attachment colour = ...
 * 	Attachment depth = ...
 *
 * 	Subpass subpass = new Subpass.Builder()
 * 		.colour(new Reference(colour, VkImageLayout.COLOR_ATTACHMENT_OPTIMAL))
 * 		.depth(new Reference(depth, VkImageLayout.DEPTH_STENCIL_ATTACHMENT_OPTIMAL))
 * 		.dependency()
 * 			.subpass(Subpass.EXTERNAL)
 * 			.source()
 * 				.stage(VkPipelineStage.FRAGMENT_SHADER)
 * 				.access(VkAccess.SHADER_READ)
 * 				.build()
 * 			.destination()
 * 				.stage(VkPipelineStage.FRAGMENT_SHADER)
 * 				.build()
 * 			.build()
 * 		.build();
 * </pre>
 * <p>
 * @author Sarge
 */
public class Subpass {
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
	}

	/**
	 * A <i>sub-pass dependency</i> defines a dependency on this sub-pass to a previous sub-pass.
	 */
	public record SubpassDependency(Subpass subpass, Dependency source, Dependency destination) {
		/**
		 * A <i>dependency</i> specifies the properties for the source or destination component of this sub-pass dependency.
		 */
		public static record Dependency(Set<VkPipelineStage> stages, Set<VkAccess> access) {
			/**
			 * Constructor.
			 * @param stages	Pipeline stages
			 * @param access	Access flags
			 */
			public Dependency {
				stages = Set.copyOf(stages);
				access = Set.copyOf(access);
				if(stages.isEmpty()) throw new IllegalArgumentException("At least one pipeline stage must be specified");
			}
		}

		/**
		 * Constructor.
		 * @param subpass			Dependant sub-pass
		 * @param source			Source dependency properties
		 * @param destination		Destination dependency properties (i.e. this sub-pass)
		 */
		public SubpassDependency {
			Check.notNull(subpass);
			Check.notNull(source);
			Check.notNull(destination);
		}
	}

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
	 */
	static final Subpass SELF = new Subpass();

	private final List<Reference> colour;
	private final Optional<Reference> depth;
	private final List<SubpassDependency> dependencies;

	/**
	 * Constructor.
	 * @param colour			Colour attachments
	 * @param depth				Depth-stencil attachment
	 * @param dependencies		Sub-pass dependencies
	 * @throws IllegalArgumentException if the sub-pass is empty
	 * @throws IllegalArgumentException if the sub-pass contains a depth-stencil attachment which is also used as a colour attachment
	 */
	public Subpass(List<Reference> colour, Reference depth, List<SubpassDependency> dependencies) {
		this.colour = List.copyOf(colour);
		this.depth = Optional.ofNullable(depth);
		this.dependencies = List.copyOf(dependencies);
		verify();
	}

	private void verify() {
		if(colour.isEmpty() && depth.isEmpty()) throw new IllegalArgumentException("No attachments specified");
		depth.map(Reference::attachment).ifPresent(this::verify);
	}

	private void verify(Attachment depth) {
		if(colour.stream().map(Reference::attachment).anyMatch(depth::equals)) throw new IllegalArgumentException("Depth-stencil cannot refer to a colour attachment");
	}

	private Subpass() {
		this.colour = List.of();
		this.depth = Optional.empty();
		this.dependencies = List.of();
	}

	/**
	 * @return Colour attachments
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
	public List<SubpassDependency> dependencies() {
		return dependencies;
	}

	/**
	 * @return Attachments used by this sub-pass
	 */
	Stream<Attachment> attachments() {
		final List<Reference> attachments = new ArrayList<>(colour);
		depth.ifPresent(attachments::add);
		return attachments.stream().map(Reference::attachment);
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
	 * Builder for a sub-pass.
	 */
	public static class Builder {
		private final List<SubpassDependency> dependencies = new ArrayList<>();
		private final List<Reference> colour = new ArrayList<>();
		private Reference depth;

		/**
		 * Builder for a dependency on this sub-pass.
		 */
		public class SubpassDependencyBuilder {
			/**
			 * Builder for the properties of a dependency.
			 */
			public class DependencyBuilder {
				private final Set<VkPipelineStage> stages = new HashSet<>();
				private final Set<VkAccess> access = new HashSet<>();

				/**
				 * Adds a pipeline stage for this dependency.
				 * @param stage Pipeline stage
				 */
				public DependencyBuilder stage(VkPipelineStage stage) {
					stages.add(notNull(stage));
					return this;
				}

				/**
				 * Adds an access flag for this dependency.
				 * @param access Access flag
				 */
				public DependencyBuilder access(VkAccess access) {
					this.access.add(notNull(access));
					return this;
				}

				/**
				 * Starts a new dependency on this sub-pass
				 * @return New sub-pass dependency builder
				 */
				public SubpassDependencyBuilder build() {
					return SubpassDependencyBuilder.this;
				}

				/**
				 * Creates the dependency record.
				 */
				private Dependency create() {
					return new Dependency(stages, access);
				}
			}

			private final DependencyBuilder src = new DependencyBuilder();
			private final DependencyBuilder dest = new DependencyBuilder();
			private Subpass subpass;

			/**
			 * Sets the dependant sub-pass.
			 * @param subpass Dependant sub-pass
			 */
			public SubpassDependencyBuilder subpass(Subpass subpass) {
				this.subpass = notNull(subpass);
				return this;
			}

			/**
			 * Configures this sub-pass this as a <a href="https://www.khronos.org/registry/vulkan/specs/1.2-extensions/html/vkspec.html#synchronization-pipeline-barriers-subpass-self-dependencies">self-dependency</a>.
			 */
			public SubpassDependencyBuilder self() {
				return subpass(SELF);
			}

			/**
			 * @return Builder for the source properties
			 */
			public DependencyBuilder source() {
				return src;
			}

			/**
			 * @return Builder for the destination properties (i.e. this sub-pass)
			 */
			public DependencyBuilder destination() {
				return dest;
			}

			/**
			 * Constructs this sub-pass dependency.
			 * @return Parent builder
			 */
			public Builder build() {
				final SubpassDependency dependency = new SubpassDependency(subpass, src.create(), dest.create());
				dependencies.add(dependency);
				return Builder.this;
			}
		}

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
		public SubpassDependencyBuilder dependency() {
			return new SubpassDependencyBuilder();
		}

		/**
		 * Constructs this sub-pass.
		 * @return New sub-pass
		 */
		public Subpass build() {
			return new Subpass(colour, depth, dependencies);
		}
	}
}
